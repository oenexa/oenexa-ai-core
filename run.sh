#!/usr/bin/env bash
# ==============================================================================
# OENEXA AI Core - Unified Linux / Git Bash Service Launcher
# Repository: github.com/oenexa/oenexa-ai-core
# ==============================================================================
# Supports running the clean-architecture API, building bootJars,
# running tests, and managing local profiles.
# ==============================================================================

set -euo pipefail

# Determine script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Color definitions (auto-disabled if stdout is not a terminal)
if [ -t 1 ]; then
    COLOR_BLUE='\033[0;34m'
    COLOR_GREEN='\033[0;32m'
    COLOR_YELLOW='\033[1;33m'
    COLOR_RED='\033[0;31m'
    COLOR_CYAN='\033[0;36m'
    COLOR_BOLD='\033[1m'
    COLOR_RESET='\033[0m'
else
    COLOR_BLUE=''
    COLOR_GREEN=''
    COLOR_YELLOW=''
    COLOR_RED=''
    COLOR_CYAN=''
    COLOR_BOLD=''
    COLOR_RESET=''
fi

log_info() {
    echo -e "${COLOR_BLUE}[INFO]${COLOR_RESET} $*"
}

log_success() {
    echo -e "${COLOR_GREEN}[SUCCESS]${COLOR_RESET} $*"
}

log_warn() {
    echo -e "${COLOR_YELLOW}[WARN]${COLOR_RESET} $*"
}

log_error() {
    echo -e "${COLOR_RED}[ERROR]${COLOR_RESET} $*" >&2
}

print_banner() {
    echo -e "${COLOR_CYAN}${COLOR_BOLD}"
    cat << "EOF"
   ___  _______  ___________  _____ _
  / _ \|  ___| \ | | ____\ \/ / _ \ |
 | | | | |_  |  \| |  _|  \  / /_\ \|
 | |_| |  _| | |\  | |___ /  \  _  | |
  \___/|_|   |_| \_|_____/_/\_\_| |_|_|
  AI CORE - BACKEND SERVICE LAUNCHER
EOF
    echo -e "${COLOR_RESET}"
}

# Load environment file if present
load_env() {
    if [ -f "$SCRIPT_DIR/.env" ]; then
        log_info "Loading configuration from .env..."
        set -a
        # shellcheck disable=SC1091
        source "$SCRIPT_DIR/.env"
        set +a
    elif [ -f "$SCRIPT_DIR/.env.example" ]; then
        log_info "No .env found. Default development values will be used."
    fi
}

# Check Java runtime environment
check_java() {
    if ! command -v java >/dev/null 2>&1; then
        log_error "Java runtime not found in PATH."
        log_error "Please install JDK 21+ (JDK 25 recommended) and ensure 'java' is on PATH."
        exit 1
    fi

    local java_version_output
    java_version_output=$(java -version 2>&1 | head -n 1)
    log_info "Using Java runtime: ${java_version_output}"

    # Ensure gradlew has execute permissions
    if [ -f "$SCRIPT_DIR/gradlew" ] && [ ! -x "$SCRIPT_DIR/gradlew" ]; then
        chmod +x "$SCRIPT_DIR/gradlew" 2>/dev/null || true
    fi
}

show_help() {
    print_banner
    echo -e "${COLOR_BOLD}Usage:${COLOR_RESET} ./run.sh [command] [options...]"
    echo ""
    echo -e "${COLOR_BOLD}Commands:${COLOR_RESET}"
    echo "  api         Run OENEXA AI Core REST API (Port 8080, In-Memory H2 DB) [Default]"
    echo "  dev         Run API in development live mode with ./gradlew :api:bootRun"
    echo "  build       Build executable Spring Boot jar (:api:bootJar)"
    echo "  test        Execute all unit tests and JaCoCo coverage verification gates"
    echo "  clean       Clean build outputs and temporary caches"
    echo "  status      Check build artifacts and system readiness"
    echo "  docker      Manage Docker stack for both backend & frontend (up, down, dev, logs)"
    echo "  help        Display this help message"
    echo ""
    echo -e "${COLOR_BOLD}Environment Variables:${COLOR_RESET}"
    echo "  SPRING_PROFILES_ACTIVE  Spring profiles to activate (default: local)"
    echo "  SERVER_PORT             Port to listen on (default: 8080)"
    echo "  OENEXA_JWT_SECRET       JWT signing secret (min 32 bytes)"
    echo "  OENEXA_LLM_PROVIDER     LLM Provider: 'echo' (offline) or 'spring-ai' (OpenAI)"
    echo "  OPENAI_API_KEY          API key when OENEXA_LLM_PROVIDER=spring-ai"
    echo ""
    echo -e "${COLOR_BOLD}Examples:${COLOR_RESET}"
    echo "  ./run.sh                    # Start API service"
    echo "  ./run.sh api                # Start API service explicitly"
    echo "  ./run.sh dev                # Start with Gradle bootRun"
    echo "  ./run.sh test               # Run all test suites"
    echo "  ./run.sh api --server.port=9090  # Override Spring Boot port"
    echo ""
}

# Run the API service (com.oenexa.api.OenexaApiApplication)
run_api() {
    print_banner
    check_java
    load_env

    export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
    export SERVER_PORT="${SERVER_PORT:-8080}"
    export OENEXA_JWT_SECRET="${OENEXA_JWT_SECRET:-oenexa-ai-core-jwt-secret-key-32bytes-min-sample}"

    local jar_file="$SCRIPT_DIR/api/build/libs/api-0.1.0-SNAPSHOT.jar"
    if [ ! -f "$jar_file" ]; then
        log_info "API jar not found. Building with Gradle (:api:bootJar)..."
        "$SCRIPT_DIR/gradlew" :api:bootJar
    fi

    log_success "Launching OENEXA AI Core API on port ${SERVER_PORT} [Profile: ${SPRING_PROFILES_ACTIVE}]..."
    exec java -Dfile.encoding=UTF-8 -jar "$jar_file" "$@"
}

# Run in Gradle development mode
run_dev() {
    print_banner
    check_java
    load_env

    export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
    export OENEXA_JWT_SECRET="${OENEXA_JWT_SECRET:-oenexa-ai-core-jwt-secret-key-32bytes-min-sample}"

    log_info "Starting OENEXA AI Core in development mode via Gradle bootRun..."
    exec "$SCRIPT_DIR/gradlew" :api:bootRun "$@"
}

# Build artifacts
build_project() {
    check_java
    log_info "Building executable bootJar for OENEXA AI Core..."
    "$SCRIPT_DIR/gradlew" :api:bootJar "$@"
    log_success "Build complete! BootJar generated in:"
    echo "  - api/build/libs/api-0.1.0-SNAPSHOT.jar"
}

# Run tests
run_tests() {
    check_java
    log_info "Running test suite and JaCoCo coverage verification across hexagonal modules..."
    "$SCRIPT_DIR/gradlew" check "$@"
    log_success "All tests and JaCoCo coverage gates passed successfully!"
}

# Clean project
clean_project() {
    log_info "Cleaning project build artifacts..."
    "$SCRIPT_DIR/gradlew" clean "$@"
    log_success "Clean completed."
}

# Check system status
check_status() {
    print_banner
    check_java
    load_env

    echo -e "${COLOR_BOLD}=== System Status ===${COLOR_RESET}"
    echo -n "Java Path: "
    command -v java
    java -version 2>&1 | head -n 1
    echo ""

    echo -e "${COLOR_BOLD}=== Build Artifacts ===${COLOR_RESET}"
    local jar="$SCRIPT_DIR/api/build/libs/api-0.1.0-SNAPSHOT.jar"
    if [ -f "$jar" ]; then
        local size
        size=$(du -h "$jar" | cut -f1)
        echo -e "  [${COLOR_GREEN}EXISTS${COLOR_RESET}] $(basename "$jar") (${size})"
    else
        echo -e "  [${COLOR_YELLOW}MISSING${COLOR_RESET}] $(basename "$jar") (Run './run.sh build' to generate)"
    fi
    echo ""
}

# Main command dispatch
COMMAND="${1:-api}"

if [ $# -gt 0 ]; then
    shift
fi

case "$COMMAND" in
    api|gateway|platform)
        run_api "$@"
        ;;
    dev)
        run_dev "$@"
        ;;
    build|assemble)
        build_project "$@"
        ;;
    test|check)
        run_tests "$@"
        ;;
    clean)
        clean_project "$@"
        ;;
    status)
        check_status
        ;;
    docker)
        exec "$SCRIPT_DIR/docker-runner.sh" "$@"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "Unknown command: $COMMAND"
        echo ""
        show_help
        exit 1
        ;;
esac
