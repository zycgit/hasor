#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

usage() {
    cat <<'EOF'
Usage:
  ./build.sh package [test] [gradle options...]
  ./build.sh install [test] [gradle options...]
  ./build.sh deploy  [test] [gradle options...]
  ./build.sh release [deploy] [test] [gradle options...]

Commands:
  package    Build packages. Equivalent to Maven package.
  install    Build packages and publish to Maven Local.
  deploy     Build packages, publish to Maven Local, then upload to Maven Central.
  release    Prepare a release commit and tag. With test, verify before deploy. With deploy, upload before next snapshot.
  test       Run tests. Tests are skipped unless this argument is present.

Deploy properties:
  Read from ~/.gradle/gradle.properties

Template:
  maven.central.username=
  maven.central.password=
  maven.central.signing_key=
  maven.central.signing_password=
  maven.central.publishing_type=USER_MANAGED
EOF
}

gradle_user_properties="${HOME}/.gradle/gradle.properties"

read_gradle_property() {
    local key="$1"
    [[ -f "$gradle_user_properties" ]] || return 0
    awk -F= -v key="$key" '
        $0 !~ /^[[:space:]]*(#|!|$)/ {
            prop=$1
            sub(/^[[:space:]]+/, "", prop)
            sub(/[[:space:]]+$/, "", prop)
            if (prop == key) {
                value=substr($0, index($0, "=") + 1)
                sub(/^[[:space:]]+/, "", value)
                sub(/[[:space:]]+$/, "", value)
                print value
                exit
            }
        }
    ' "$gradle_user_properties"
}

project_version() {
    sed -n 's/^version=//p' gradle.properties
}

release_version_of() {
    local version="$1"
    printf '%s\n' "${version%-SNAPSHOT}"
}

next_snapshot_of() {
    local release_version="$1"
    if [[ "$release_version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
        printf '%s.%s.%s-SNAPSHOT\n' "${BASH_REMATCH[1]}" "${BASH_REMATCH[2]}" "$((BASH_REMATCH[3] + 1))"
    else
        printf '%s-SNAPSHOT\n' "$release_version"
    fi
}

prompt_with_default() {
    local prompt="$1"
    local default_value="$2"
    local value
    read -r -p "${prompt} [${default_value}]: " value
    printf '%s\n' "${value:-$default_value}"
}

set_project_version() {
    local new_version="$1"
    sed -i "s/^version=.*/version=${new_version}/" gradle.properties
}

ensure_clean_worktree() {
    if [[ -n "$(git status --porcelain)" ]]; then
        echo "Release requires a clean git worktree." >&2
        git status --short >&2
        exit 1
    fi
}

prepare_release() {
    local release_deploy="$1"
    local run_tests="$2"
    shift
    shift
    local gradle_options=("$@")
    local current_version release_default release_version release_tag next_default next_version confirm

    ensure_clean_worktree
    current_version="$(project_version)"
    release_default="$(release_version_of "$current_version")"
    next_default="$(next_snapshot_of "$release_default")"

    echo "Current version: ${current_version}"
    release_version="$(prompt_with_default "Release version" "$release_default")"
    release_tag="Release.Hasor-${release_version}"
    next_default="$(next_snapshot_of "$release_version")"
    next_version="$(prompt_with_default "Next development version" "$next_default")"

    echo
    echo "Release plan:"
    echo "  release version: ${release_version}"
    echo "  release tag:     ${release_tag}"
    if [[ "$run_tests" == "true" ]]; then
        echo "  test:            yes, before deploy"
    fi
    if [[ "$release_deploy" == "true" ]]; then
        echo "  deploy:          yes, before next version"
    fi
    echo "  next version:    ${next_version}"
    read -r -p "Continue? [y/N]: " confirm
    if [[ "$confirm" != "y" && "$confirm" != "Y" ]]; then
        echo "Release cancelled."
        exit 1
    fi

    set_project_version "$release_version"
    if [[ "$run_tests" == "true" ]]; then
        if ! ./build.sh package test "${gradle_options[@]}"; then
            set_project_version "$current_version"
            echo "Release build failed. Version restored to ${current_version}." >&2
            exit 1
        fi
    else
        if ! ./build.sh package "${gradle_options[@]}"; then
            set_project_version "$current_version"
            echo "Release build failed. Version restored to ${current_version}." >&2
            exit 1
        fi
    fi
    git add gradle.properties
    git commit -m "Release ${release_version}"
    git tag -a "${release_tag}" -m "${release_tag}"

    if [[ "$release_deploy" == "true" ]]; then
        if ! ./build.sh deploy "${gradle_options[@]}"; then
            echo "Deploy failed. Version remains at ${release_version}; next version was not committed." >&2
            exit 1
        fi
    fi

    set_project_version "$next_version"
    git add gradle.properties
    git commit -m "Next development version ${next_version}"
}

if [[ "$#" -eq 0 ]]; then
    usage
    exit 0
fi

mode="package"
run_tests="false"
dry_run="false"
release_deploy="false"
gradle_args=()
plugin_tasks=()

for arg in "$@"; do
    case "$arg" in
        help|-h|--help)
            usage
            exit 0
            ;;
        package)
            mode="package"
            ;;
        install)
            mode="install"
            ;;
        deploy)
            if [[ "$mode" == "release" ]]; then
                release_deploy="true"
            else
                mode="deploy"
            fi
            ;;
        release)
            if [[ "$mode" == "deploy" ]]; then
                release_deploy="true"
            fi
            mode="release"
            ;;
        test)
            run_tests="true"
            ;;
        --dry-run)
            dry_run="true"
            gradle_args+=("$arg")
            ;;
        *)
            gradle_args+=("$arg")
            ;;
    esac
done

if [[ "$mode" == "release" ]]; then
    if [[ "$dry_run" == "true" ]]; then
        echo "Release does not support --dry-run because it creates commits and tags." >&2
        exit 1
    fi
    prepare_release "$release_deploy" "$run_tests" "${gradle_args[@]}"
    exit 0
fi

tasks=(clean build)
if [[ "$mode" == "install" || "$mode" == "deploy" ]]; then
    tasks+=(publishToMavenLocal)
    plugin_tasks+=(--include-build hasor-boot/hasor-boot-gradle-plugin :hasor-boot-gradle-plugin:publishToMavenLocal)
fi
if [[ "$mode" == "deploy" ]]; then
    version="$(sed -n 's/^version=//p' gradle.properties)"
    if [[ "$version" == *-SNAPSHOT ]]; then
        echo "Maven Central deploy requires a release version, current version is ${version}." >&2
        exit 1
    fi

    central_username="$(read_gradle_property 'maven.central.username')"
    central_password="$(read_gradle_property 'maven.central.password')"
    publishing_type="$(read_gradle_property 'maven.central.publishing_type')"
    publishing_type="${publishing_type:-USER_MANAGED}"
    if [[ "$publishing_type" != "AUTOMATIC" && "$publishing_type" != "USER_MANAGED" ]]; then
        echo "maven.central.publishing_type must be AUTOMATIC or USER_MANAGED." >&2
        exit 1
    fi

    for prop_name in maven.central.username maven.central.password maven.central.signing_key; do
        prop_value="$(read_gradle_property "$prop_name")"
        if [[ -z "$prop_value" ]]; then
            echo "${prop_name} is required in ${gradle_user_properties} for deploy." >&2
            exit 1
        fi
    done

    central_dir="$PWD/build/central-bundle/repository"
    central_zip="$PWD/build/central-bundle/central-bundle.zip"
    tasks+=(publishAllPublicationsToCentralBundleRepository)
    plugin_tasks+=(--include-build hasor-boot/hasor-boot-gradle-plugin :hasor-boot-gradle-plugin:publishAllPublicationsToCentralBundleRepository)
    gradle_args+=("-PcentralRelease=true")
    gradle_args+=("-PcentralBundleDir=${central_dir}")
fi

if [[ "$run_tests" != "true" ]]; then
    gradle_args+=("-x" "test")
fi

has_parallel_option="false"
has_max_workers_option="false"
for arg in "${gradle_args[@]}"; do
    case "$arg" in
        --parallel|--no-parallel)
            has_parallel_option="true"
            ;;
        --max-workers|--max-workers=*)
            has_max_workers_option="true"
            ;;
    esac
done

gradle_defaults=()
if [[ "$has_parallel_option" == "false" ]]; then
    gradle_defaults+=(--parallel)
fi
if [[ "$has_max_workers_option" == "false" ]]; then
    gradle_defaults+=(--max-workers 8)
fi

if [[ "$mode" == "deploy" && "$dry_run" != "true" ]]; then
    rm -rf -- "$central_dir"
    rm -f -- "$central_zip"
fi

./gradlew "${tasks[@]}" "${gradle_defaults[@]}" "${gradle_args[@]}"
if [[ "${#plugin_tasks[@]}" -gt 0 ]]; then
    ./gradlew "${plugin_tasks[@]}" "${gradle_defaults[@]}" "${gradle_args[@]}"
fi

if [[ "$mode" == "deploy" && "$dry_run" != "true" ]]; then
    find "$central_dir" -type f \
        ! -name '*.asc' ! -name '*.md5' ! -name '*.sha1' ! -name '*.sha256' ! -name '*.sha512' \
        -print0 | while IFS= read -r -d '' file; do
            md5sum "$file" | awk '{print $1}' > "${file}.md5"
            sha1sum "$file" | awk '{print $1}' > "${file}.sha1"
            sha256sum "$file" | awk '{print $1}' > "${file}.sha256"
            sha512sum "$file" | awk '{print $1}' > "${file}.sha512"
        done

    rm -f "$central_zip"
    jar --create --no-manifest --file "$central_zip" -C "$central_dir" .

    token="$(printf '%s:%s' "$central_username" "$central_password" | base64 | tr -d '\n')"
    deploy_id="$(curl --fail --silent --show-error --request POST \
        --header "Authorization: Bearer ${token}" \
        --form "bundle=@${central_zip};type=application/octet-stream" \
        "https://central.sonatype.com/api/v1/publisher/upload?name=hasor-${version}&publishingType=${publishing_type}")"
    echo "Maven Central deployment id: ${deploy_id}"
fi
