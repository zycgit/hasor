#!/usr/bin/env python3
"""Build this documentation site and upload its static files to the selected OSS prefix."""

import mimetypes
import subprocess
import sys

from deploy_config import DOCUMENT_DIR, deployment_lock, load_config


def build_project():
    print("Building Chinese and English documentation...", flush=True)
    subprocess.run(["npm", "run", "build"], cwd=DOCUMENT_DIR, check=True)


def upload_to_oss(config):
    import oss2

    build_path = DOCUMENT_DIR / "build"
    if not (build_path / "index.html").is_file():
        raise FileNotFoundError("build/index.html is missing; build the documentation before uploading.")

    files = sorted(build_path.rglob("*"))
    if build_path.is_symlink() or any(file.is_symlink() for file in files):
        raise ValueError("The documentation build must not contain symbolic links.")
    bucket = oss2.Bucket(oss2.Auth(config["accessKeyId"], config["accessKeySecret"]),
                         config["endpoint"], config["bucketName"])
    uploaded = 0
    for file in files:
        if not file.is_file():
            continue
        object_name = config["prefix"] + file.relative_to(build_path).as_posix()
        content_type = mimetypes.guess_type(file.name)[0]
        headers = {"Content-Type": content_type} if content_type else {}
        try:
            bucket.put_object_from_file(object_name, str(file), headers=headers)
        except Exception as error:
            raise RuntimeError(f"OSS upload failed for {object_name}; CDN refresh must not proceed.") from error
        uploaded += 1
        print(f"Uploaded: {object_name}")
    print(f"Upload complete: {uploaded} files.")


def main():
    try:
        with deployment_lock():
            config = load_config()
            build_project()
            upload_to_oss(config)
    except Exception as error:
        print(f"Deployment failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
