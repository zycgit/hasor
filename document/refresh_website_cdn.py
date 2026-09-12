#!/usr/bin/env python3
"""Submit a directory refresh for the selected documentation site's CDN."""

import json
import sys

from deploy_config import deployment_lock, load_config


def refresh_cdn(config):
    from aliyunsdkcore.client import AcsClient
    from aliyunsdkcdn.request.v20180510 import RefreshObjectCachesRequest

    client = AcsClient(config["accessKeyId"], config["accessKeySecret"],
                       config.get("cdnRegionId", "cn-hangzhou"))
    request = RefreshObjectCachesRequest.RefreshObjectCachesRequest()
    request.set_ObjectPath(config["cdnUrl"].rstrip("/") + "/")
    request.set_ObjectType("Directory")
    response = json.loads(client.do_action_with_exception(request))
    print(f"CDN refresh submitted. RefreshTaskId: {response.get('RefreshTaskId')}")


def main():
    try:
        with deployment_lock():
            refresh_cdn(load_config())
    except Exception as error:
        print(f"CDN refresh failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
