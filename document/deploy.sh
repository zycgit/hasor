#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

for command_name in python3 npm; do
    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "Error: $command_name is required." >&2
        exit 1
    fi
done

# Keep deployment dependencies separate from the system Python environment.
python3 -m venv .deploy-venv
.deploy-venv/bin/python -m pip install --disable-pip-version-check -r requirements-deploy.txt

.deploy-venv/bin/python deploy.py

echo "Documentation uploaded and CDN refresh submitted successfully."
