"""Validate the shared documentation deployment configuration before publishing."""

from contextlib import contextmanager
import fcntl
import json
import os
from pathlib import Path
import re
from urllib.parse import urlsplit, urlunsplit

from deploy_site import SITE_NAME

DOCUMENT_DIR = Path(__file__).resolve().parent
CONFIG_FILE_NAME = ".hasor-docs-deploy.json"
CONFIG_ENV = "HASOR_DOCS_DEPLOY_CONFIG"


def require_fields(config, *names):
    if not isinstance(config, dict):
        raise ValueError("Configuration entries must be JSON objects.")
    missing = [name for name in names if not isinstance(config.get(name), str) or not config[name].strip()]
    if missing:
        raise ValueError("Missing configuration fields: " + ", ".join(missing))


def normalize_prefix(value):
    if not isinstance(value, str):
        raise ValueError("prefix must be a string, or empty for the bucket root.")
    value = value.strip("/")
    if value and any(part in ("", ".", "..") or not re.fullmatch(r"[A-Za-z0-9_.-]+", part)
                     for part in value.split("/")):
        raise ValueError("prefix must contain safe directory names separated by single slashes.")
    return value + "/" if value else ""


def overlaps(left, right):
    return left.startswith(right) or right.startswith(left)


def normalize_cdn_url(value):
    url = urlsplit(value)
    if (url.scheme not in ("http", "https") or not url.hostname or url.query or url.fragment
            or url.username or url.password or any(character.isspace() for character in value)):
        raise ValueError("cdnUrl must be an HTTP(S) directory URL without credentials, query or fragment.")
    prefix = normalize_prefix(url.path)
    port = url.port
    authority = (url.hostname.lower(), None if port in (None, 80, 443) else port)
    return urlunsplit((url.scheme, url.netloc, "/" + prefix, "", "")), authority, prefix


def validate_config(document, site_name):
    if not isinstance(document, dict) or not isinstance(document.get("credentials"), dict) or not isinstance(document.get("sites"), dict):
        raise ValueError("Configuration must contain credentials and sites objects; legacy flat configuration is not supported.")
    if site_name not in document["sites"]:
        raise ValueError(f"Missing configuration for site: {site_name}")
    resolved = {}
    targets = []
    for name, site in document["sites"].items():
        require_fields(site, "credential", "endpoint", "bucketName", "cdnUrl")
        credential = document["credentials"].get(site["credential"])
        require_fields(credential, "accessKeyId", "accessKeySecret")
        if site["bucketName"] != site["bucketName"].strip() or "/" in site["bucketName"]:
            raise ValueError("bucketName must be a bucket name, without spaces around it or a path.")
        endpoint = site["endpoint"]
        if "://" not in endpoint:
            endpoint = "https://" + endpoint
        endpoint_url = urlsplit(endpoint)
        if (endpoint_url.scheme not in ("http", "https") or not endpoint_url.hostname
                or endpoint_url.path not in ("", "/") or endpoint_url.query or endpoint_url.fragment
                or endpoint_url.username or endpoint_url.password):
            raise ValueError("endpoint must be an OSS HTTP(S) service endpoint without a path or credentials.")
        prefix = normalize_prefix(site.get("prefix", ""))
        cdn_url, cdn_authority, cdn_prefix = normalize_cdn_url(site["cdnUrl"])
        for previous_name, bucket, previous_prefix, previous_cdn, previous_cdn_prefix in targets:
            # Bucket names identify the destination even when endpoint aliases differ.
            if bucket == site["bucketName"] and overlaps(prefix, previous_prefix):
                raise ValueError(f"Overlapping OSS destinations: {previous_name} and {name}")
            if cdn_authority == previous_cdn and overlaps(cdn_prefix, previous_cdn_prefix):
                raise ValueError(f"Overlapping CDN refresh directories: {previous_name} and {name}")
        targets.append((name, site["bucketName"], prefix, cdn_authority, cdn_prefix))
        region = site.get("cdnRegionId", "cn-hangzhou")
        require_fields({"cdnRegionId": region}, "cdnRegionId")
        resolved[name] = {"siteName": name, "accessKeyId": credential["accessKeyId"],
                          "accessKeySecret": credential["accessKeySecret"], "endpoint": endpoint,
                          "bucketName": site["bucketName"], "prefix": prefix,
                          "cdnUrl": cdn_url, "cdnRegionId": region}
    return resolved[site_name]


def load_config():
    override = os.environ.get(CONFIG_ENV)
    config_path = Path(override).expanduser() if override else Path.home() / CONFIG_FILE_NAME
    try:
        document = json.loads(config_path.read_text(encoding="utf-8"))
    except (OSError, ValueError) as error:
        raise ValueError(f"Cannot read configuration: {config_path}. See oss-config.sample.json.") from error
    return validate_config(document, SITE_NAME)


@contextmanager
def deployment_lock():
    """Serialize this site across checkouts for the same OS user; never unlink a live lock."""
    directory = Path.home() / ".cache" / "hasor-docs-deploy"
    directory.mkdir(parents=True, exist_ok=True, mode=0o700)
    with (directory / (SITE_NAME + ".lock")).open("a") as lock_file:
        try:
            fcntl.flock(lock_file, fcntl.LOCK_EX | fcntl.LOCK_NB)
        except BlockingIOError as error:
            raise RuntimeError(f"A deployment for {SITE_NAME} is already running.") from error
        try:
            yield
        finally:
            fcntl.flock(lock_file, fcntl.LOCK_UN)
