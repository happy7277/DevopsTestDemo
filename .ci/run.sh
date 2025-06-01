#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
set -a
source .ci/ci.env
set +a

java_build() { mvn -B -f demo-app/pom.xml "$@"; }
npm_demo() { npm --prefix demo-frontend "$@"; }
python_deps() { "$PYTHON_COMMAND" -m pip install --disable-pip-version-check -r demo-python/requirements.txt; }

compose_demo() (
  project="demo-${GITHUB_RUN_ID:-${CI_JOB_ID:-local}}"
  compose=(docker compose --project-name "$project")
  trap '"${compose[@]}" down --volumes --remove-orphans' EXIT
  if ! "${compose[@]}" up -d --build --wait mysql redis demo-app demo-python demo-frontend ||
    ! docker run --rm --network "${project}_default" -i python:3.11-slim python - < .ci/smoke.py; then
    "${compose[@]}" logs --tail 100
    return 1
  fi
)
case "${1:-}" in
  build) java_build clean compile; npm_demo ci; npm_demo run build ;;
  test)
    java_build test
    npm_demo ci
    npm_demo test
    python_deps
    (cd demo-python && "$PYTHON_COMMAND" -m pytest -q)
    ;;
  lint)
    npm_demo ci
    npm_demo run lint
    python_deps
    (cd demo-python && "$PYTHON_COMMAND" -m ruff check .)
    "$PYTHON_COMMAND" .ci/check-sizes.py
    ;;
  sonar)
    if [[ -n "${SONAR_HOST_URL:-}" && -n "${SONAR_TOKEN:-}" && -n "${SONAR_PROJECT_KEY:-}" ]]; then
      mvn -B verify
      mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:4.0.0.4121:sonar \
        "-Dsonar.projectKey=$SONAR_PROJECT_KEY" "-Dsonar.host.url=$SONAR_HOST_URL" \
        -Dsonar.qualitygate.wait=true
    elif [[ -n "${SONAR_HOST_URL:-}${SONAR_TOKEN:-}${SONAR_PROJECT_KEY:-}" ]]; then
      echo "Sonar requires SONAR_HOST_URL, SONAR_TOKEN and SONAR_PROJECT_KEY" >&2; exit 2
    else
      echo "Sonar scan skipped: configure SONAR_HOST_URL, SONAR_TOKEN and SONAR_PROJECT_KEY"
    fi
    ;;
  integration-test|deploy) compose_demo ;;
  ui-test)
    npm_demo ci
    (cd demo-frontend && npx playwright install --with-deps "$PLAYWRIGHT_BROWSER" && npm run test:ui)
    ;;
  package)
    java_build -DskipTests package
    npm_demo ci
    npm_demo run build
    mkdir -p artifacts
    cp demo-app/target/demo-app-1.0.0.jar artifacts/
    tar -czf artifacts/demo-frontend.tar.gz -C demo-frontend dist nginx.conf
    tar -czf artifacts/demo-python.tar.gz -C demo-python --exclude=__pycache__ app requirements.txt Dockerfile
    ;;
  *) echo "Usage: bash .ci/run.sh <stage>" >&2; exit 2 ;;
esac
