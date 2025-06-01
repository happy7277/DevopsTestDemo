#!/usr/bin/env bash
set -euo pipefail

for tool in "$@"; do
  case "$tool" in
    java)
      apt-get update -qq
      apt-get install -y -qq openjdk-17-jdk-headless maven
      ;;
    node)
      version=22.15.0
      archive="node-v$version-linux-x64.tar.xz"
      curl --fail --silent --show-error --retry 3 "https://nodejs.org/dist/v$version/$archive" -o "/tmp/$archive"
      tar -xJf "/tmp/$archive" -C /usr/local --strip-components=1
      ;;
    docker)
      curl --fail --silent --show-error --retry 3 https://download.docker.com/linux/static/stable/x86_64/docker-27.5.1.tgz -o /tmp/docker.tgz
      tar -xzf /tmp/docker.tgz -C /usr/local/bin --strip-components=1 docker/docker
      mkdir -p /usr/local/lib/docker/cli-plugins
      curl --fail --silent --show-error --location --retry 3 \
        https://github.com/docker/compose/releases/download/v2.32.4/docker-compose-linux-x86_64 \
        -o /usr/local/lib/docker/cli-plugins/docker-compose
      chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
      ;;
    *) echo "Unknown tool: $tool" >&2; exit 2 ;;
  esac
done
