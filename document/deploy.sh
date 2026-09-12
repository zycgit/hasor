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
dependency_log=$(mktemp)
trap 'rm -f -- "$dependency_log"' EXIT

run_dependency_step() {
    local status
    if "$@" >"$dependency_log" 2>&1; then
        # Hide only pip's repetitive no-op notices; preserve installation and warning output.
        sed '/^Requirement already satisfied:/d' "$dependency_log"
        return 0
    else
        status=$?
        echo "Error: deployment dependency setup failed." >&2
        cat -- "$dependency_log" >&2
        return "$status"
    fi
}

run_dependency_step python3 -m venv .deploy-venv
run_dependency_step .deploy-venv/bin/python -m pip install --disable-pip-version-check -r requirements-deploy.txt

.deploy-venv/bin/python deploy.py

echo "Documentation uploaded and CDN refresh submitted successfully."
