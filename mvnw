#!/bin/sh
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec "$PROJECT_DIR/backend/mvnw" -f "$PROJECT_DIR/backend/pom.xml" "$@"

