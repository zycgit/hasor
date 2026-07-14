#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"

usage() {
    cat <<'EOF'
Usage:
  ./build.sh package [test] [gradle options...]
  ./build.sh install [test] [gradle options...]
  ./build.sh deploy  [test] [gradle options...]

Commands:
  package    Build packages. Equivalent to Maven package.
  install    Build packages and publish to Maven Local.
  deploy     Build packages, publish to Maven Local, then upload to Maven Central.
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

if [[ "$#" -eq 0 ]]; then
    usage
    exit 0
fi

mode="package"
run_tests="false"
dry_run="false"
gradle_args=(--parallel --max-workers 8)
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
            mode="deploy"
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

./gradlew "${tasks[@]}" "${gradle_args[@]}"
if [[ "${#plugin_tasks[@]}" -gt 0 ]]; then
    ./gradlew "${plugin_tasks[@]}" "${gradle_args[@]}"
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
