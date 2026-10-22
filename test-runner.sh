#!/usr/bin/env bash
# =====================================================================
# OENEXA AI ENGINEER - Comprehensive Test Execution Suite (Bash / POSIX)
# Runs Unit Tests, BDD Cucumber Scenarios, and TDD Specifications Separately
# =====================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

TYPE="separate"
MODULE="all"
COVERAGE=false
RERUN=false

function print_help() {
    cat << EOF
OENEXA AI Test Runner (Bash):
  Usage: ./test-runner.sh [--unit | --bdd | --tdd | --separate | --all] [--module <domain|application|infrastructure|api>] [--coverage] [--rerun]

Options:
  --unit        : Fast isolated unit tests (excludes BDD and TDD)
  --bdd         : Behavior-Driven Development (Cucumber Gherkin scenarios)
  --tdd         : Test-Driven Development (Invariants, memory algorithms, contracts)
  --separate    : Executes Unit, BDD, and TDD in distinct separate phases (default)
  --all         : Executes all tests in a unified Gradle run
  --module, -m  : Target hexagonal module (default: all)
  --coverage    : Generate JaCoCo HTML & XML code coverage reports
  --rerun       : Force fresh execution ignoring Gradle cache
  --help, -h    : Show this help message
EOF
    exit 0
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --unit) TYPE="unit"; shift ;;
        --bdd) TYPE="bdd"; shift ;;
        --tdd) TYPE="tdd"; shift ;;
        --separate) TYPE="separate"; shift ;;
        --all) TYPE="all"; shift ;;
        -m|--module) MODULE="$2"; shift 2 ;;
        --coverage) COVERAGE=true; shift ;;
        --rerun) RERUN=true; shift ;;
        -h|--help) print_help ;;
        *) echo "Unknown option: $1"; print_help ;;
    esac
done

GRADLE_CMD="./gradlew"
if [[ ! -f "$GRADLE_CMD" ]]; then
    echo "Error: Gradle wrapper '$GRADLE_CMD' not found in $SCRIPT_DIR"
    exit 1
fi

echo "=========================================================="
echo "  OENEXA AI TEST RUNNER - [Target: $TYPE | Module: $MODULE]"
echo "=========================================================="

function run_phase() {
    local phase_name="$1"
    local task_name="$2"

    echo ""
    echo ">>> [PHASE: $phase_name] Starting execution..."

    local target_task="$task_name"
    if [[ "$MODULE" != "all" ]]; then
        target_task=":$MODULE:$task_name"
    fi

    local args=("$target_task")
    if [[ "$RERUN" == true ]]; then
        args+=("--rerun")
    fi
    if [[ "$COVERAGE" == true ]]; then
        args+=("jacocoTestReport")
    fi

    echo "Executing: $GRADLE_CMD ${args[*]}"
    start_time=$(date +%s)

    if $GRADLE_CMD "${args[@]}"; then
        end_time=$(date +%s)
        duration=$((end_time - start_time))
        echo ">>> [PHASE: $phase_name] SUCCESS in ${duration}s"
    else
        echo ">>> [PHASE: $phase_name] FAILED"
        exit 1
    fi
}

case "$TYPE" in
    unit)
        run_phase "UNIT TESTS" "testUnit"
        ;;
    bdd)
        run_phase "BDD SCENARIOS (Cucumber)" "testBdd"
        ;;
    tdd)
        run_phase "TDD SPECIFICATIONS (Contracts)" "testTdd"
        ;;
    all)
        run_phase "ALL TESTS (Unified)" "test"
        ;;
    separate)
        echo "Running 3 distinct phases separately: [1/3] UNIT -> [2/3] BDD -> [3/3] TDD"
        run_phase "UNIT TESTS" "testUnit"
        run_phase "BDD SCENARIOS (Cucumber)" "testBdd"
        run_phase "TDD SPECIFICATIONS (Contracts)" "testTdd"
        ;;
esac

echo ""
echo "=========================================================="
echo "  Result: ALL REQUESTED TEST PHASES PASSED SUCCESSFULLY."
echo "=========================================================="
exit 0
