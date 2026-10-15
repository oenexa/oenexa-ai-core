#!/usr/bin/env bash
# ==============================================================================
# OENEXA AI Engineer - Unified Docker Runner (Frontend & Backend)
# ==============================================================================
# Orchestrates Docker containers for both backend (Spring Boot 4 / Java 25)
# and frontend (React 18/19 Vite / Nginx), plus PostgreSQL 15 and Redis 7.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Color definitions
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
  DOCKER RUNNER - FRONTEND & BACKEND
EOF
    echo -e "${COLOR_RESET}"
}

check_docker() {
    if ! command -v docker >/dev/null 2>&1; then
        log_error "Docker is not installed or not in PATH."
        exit 1
    fi
    if ! docker info >/dev/null 2>&1; then
        log_error "Docker daemon is not running. Please start Docker Desktop or the docker service."
        exit 1
    fi
}

load_env() {
    if [ -f "$SCRIPT_DIR/.env" ]; then
        log_info "Loading environment overrides from .env..."
        set -a
        # shellcheck disable=SC1091
        source "$SCRIPT_DIR/.env"
        set +a
    elif [ -f "$SCRIPT_DIR/.env.example" ]; then
        log_info "Using default container parameters (create .env to customize)."
    fi
}

show_help() {
    print_banner
    echo -e "${COLOR_BOLD}Usage:${COLOR_RESET} ./docker-runner.sh [command] [options...]"
    echo ""
    echo -e "${COLOR_BOLD}Commands:${COLOR_RESET}"
    echo "  up             Build & start all containers (backend, frontend, postgres, redis) [Default]"
    echo "  dev            Start development mode with source mounting and hot-reloading"
    echo "  down           Stop and remove all running project containers and networks"
    echo "  build          Build or rebuild both backend and frontend container images"
    echo "  backend        Start only backend services (backend + postgres + redis)"
    echo "  frontend       Start only frontend service"
    echo "  logs [service] Stream container logs (default: all services; or: backend, frontend, postgres, redis)"
    echo "  status         Display status, health checks, and exposed endpoint URLs"
    echo "  restart [svc]  Restart all or specified container service"
    echo "  clean          Stop containers and purge volumes, caches, and orphaned networks"
    echo "  help           Display this help manual"
    echo ""
    echo -e "${COLOR_BOLD}Endpoints (when running):${COLOR_RESET}"
    echo "  Frontend UI:    http://localhost:3000"
    echo "  Backend API:    http://localhost:8080"
    echo "  Health Check:   http://localhost:8080/actuator/health"
    echo "  Modules Info:   http://localhost:8080/api/v1/modules"
    echo "  PostgreSQL:     localhost:5432 (oenexa_db / oenexa)"
    echo "  Redis:          localhost:6379"
    echo ""
    echo -e "${COLOR_BOLD}Examples:${COLOR_RESET}"
    echo "  ./docker-runner.sh up                 # Start entire stack in background"
    echo "  ./docker-runner.sh up --build         # Force rebuild of images before startup"
    echo "  ./docker-runner.sh logs backend -f    # Tail backend logs in real time"
    echo "  ./docker-runner.sh dev                # Start live reload dev stack"
    echo "  ./docker-runner.sh down               # Stop and clean up"
    echo ""
}

cmd_up() {
    print_banner
    check_docker
    load_env
    log_info "Starting OENEXA AI Engineer stack (Backend, Frontend, PostgreSQL, Redis)..."
    docker compose up -d "$@"
    log_success "All services launched successfully in background!"
    echo ""
    cmd_status
}

cmd_dev() {
    print_banner
    check_docker
    load_env
    log_info "Starting OENEXA AI Engineer in development mode (live reload)..."
    docker compose -f docker-compose.dev.yml up "$@"
}

cmd_down() {
    check_docker
    log_info "Stopping and removing OENEXA containers and networks..."
    docker compose down --remove-orphans "$@"
    if [ -f docker-compose.dev.yml ]; then
        docker compose -f docker-compose.dev.yml down --remove-orphans >/dev/null 2>&1 || true
    fi
    log_success "All containers stopped and cleaned up."
}

cmd_build() {
    check_docker
    load_env
    log_info "Building container images for both backend and frontend..."
    docker compose build "$@"
    log_success "Images built successfully!"
}

cmd_backend() {
    check_docker
    load_env
    log_info "Starting backend stack only (PostgreSQL, Redis, Backend API)..."
    docker compose up -d postgres redis backend "$@"
    log_success "Backend services running at http://localhost:8080"
}

cmd_frontend() {
    check_docker
    load_env
    log_info "Starting frontend service only..."
    docker compose up -d frontend "$@"
    log_success "Frontend running at http://localhost:3000"
}

cmd_logs() {
    check_docker
    if [ $# -eq 0 ]; then
        docker compose logs -f
    else
        docker compose logs "$@"
    fi
}

cmd_restart() {
    check_docker
    log_info "Restarting container services..."
    docker compose restart "$@"
    log_success "Restart complete."
}

cmd_clean() {
    check_docker
    log_warn "This will stop containers and delete project database volumes."
    read -r -p "Are you sure you want to proceed? [y/N] " response
    case "$response" in
        [yY][eE][sS]|[yY])
            docker compose down -v --remove-orphans
            log_success "Project volumes and containers purged."
            ;;
        *)
            log_info "Clean cancelled."
            ;;
    esac
}

cmd_status() {
    check_docker
    echo -e "${COLOR_BOLD}=== OENEXA Container Status ===${COLOR_RESET}"
    docker compose ps
    echo ""
    echo -e "${COLOR_BOLD}=== Service Endpoints ===${COLOR_RESET}"
    echo -e "  Frontend UI:  ${COLOR_CYAN}http://localhost:3000${COLOR_RESET}"
    echo -e "  Backend API:  ${COLOR_CYAN}http://localhost:8080${COLOR_RESET}"
    echo -e "  Actuator:     ${COLOR_CYAN}http://localhost:8080/actuator/health${COLOR_RESET}"
    echo -e "  PostgreSQL:   ${COLOR_CYAN}localhost:5432 (DB: oenexa_db, User: oenexa)${COLOR_RESET}"
    echo -e "  Redis:        ${COLOR_CYAN}localhost:6379${COLOR_RESET}"
    echo ""
}

# Entrypoint router
ACTION="${1:-up}"
if [ $# -gt 0 ]; then
    shift
fi

case "$ACTION" in
    up)
        cmd_up "$@"
        ;;
    dev)
        cmd_dev "$@"
        ;;
    down|stop)
        cmd_down "$@"
        ;;
    build)
        cmd_build "$@"
        ;;
    backend)
        cmd_backend "$@"
        ;;
    frontend)
        cmd_frontend "$@"
        ;;
    logs)
        cmd_logs "$@"
        ;;
    restart)
        cmd_restart "$@"
        ;;
    clean)
        cmd_clean "$@"
        ;;
    status|ps)
        cmd_status
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "Unknown command: '$ACTION'"
        echo "Run './docker-runner.sh help' for usage instructions."
        exit 1
        ;;
esac
