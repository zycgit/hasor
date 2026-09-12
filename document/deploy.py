#!/usr/bin/env python3
"""Publish one configuration snapshot under a lock spanning build, upload and refresh."""

import sys

from deploy_config import deployment_lock, load_config
from deploy_to_oss import build_project, upload_to_oss
from refresh_website_cdn import refresh_cdn


def main():
    try:
        with deployment_lock():
            config = load_config()
            print(f"Publishing {config['siteName']} to oss://{config['bucketName']}/{config['prefix']}")
            build_project()
            upload_to_oss(config)
            refresh_cdn(config)
    except Exception as error:
        print(f"Publication failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
