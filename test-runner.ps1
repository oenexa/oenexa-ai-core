# =====================================================================
# OENEXA AI ENGINEER - Comprehensive Test Execution Suite
# Runs Unit Tests, BDD Cucumber Scenarios, and TDD Specifications Separately
# =====================================================================
param(
    [Alias("T", "Category")]
    [ValidateSet("all", "unit", "bdd", "tdd", "separate")]
    [string]$Type = "separate",

    [Alias("M")]
    [ValidateSet("all", "domain", "application", "infrastructure", "api")]
    [string]$Module = "all",

    [switch]$Coverage,
    [switch]$Rerun,
    [switch]$Help
)

if ($Help) {
    Write-Host @"
OENEXA AI Test Runner:
  Usage: .\test-runner.ps1 [-Type <unit|bdd|tdd|separate|all>] [-Module <all|domain|application|infrastructure|api>] [-Coverage] [-Rerun]

Options:
  -Type, -T       Test category to execute:
                    'unit'     : Fast isolated unit tests (excludes BDD and TDD)
                    'bdd'      : Behavior-Driven Development (Cucumber Gherkin scenarios)
                    'tdd'      : Test-Driven Development (Invariants, memory algorithms, contracts)
                    'separate' : Executes Unit, BDD, and TDD in distinct separate phases (default)
                    'all'      : Executes all tests in a unified Gradle run
  -Module, -M     Target module (default: 'all')
  -Coverage       Generate JaCoCo HTML & XML code coverage reports
  -Rerun          Force fresh execution ignoring Gradle up-to-date cache
"@
    exit 0
}

$ErrorActionPreference = "Stop"
$ScriptDir = $PSScriptRoot
Set-Location $ScriptDir

$GradleCmd = if ($IsWindows -or $env:OS -like "*Windows*") { ".\gradlew.bat" } else { "./gradlew" }
if (-not (Test-Path (Join-Path $ScriptDir $GradleCmd.TrimStart(".\")))) {
    Write-Error "Gradle wrapper '$GradleCmd' not found in $ScriptDir"
    exit 1
}

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  OENEXA AI TEST RUNNER - [Target: $Type | Module: $Module]" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

function Run-Phase {
    param(
        [string]$PhaseName,
        [string]$TaskName,
        [ConsoleColor]$Color = [ConsoleColor]::Yellow
    )

    Write-Host "`n>>> [PHASE: $PhaseName] Starting execution..." -ForegroundColor $Color
    $targetTask = if ($Module -eq "all") {
        $TaskName
    } else {
        ":$Module`:$TaskName"
    }

    $arguments = @($targetTask)
    if ($Rerun) { $arguments += "--rerun" }
    if ($Coverage) { $arguments += "jacocoTestReport" }

    Write-Host "Executing: $GradleCmd $($arguments -join ' ')" -ForegroundColor DarkGray
    $startTime = [System.Diagnostics.Stopwatch]::StartNew()
    
    & $GradleCmd $arguments
    $exitCode = $LASTEXITCODE
    $startTime.Stop()

    $duration = [math]::Round($startTime.Elapsed.TotalSeconds, 2)
    if ($exitCode -eq 0) {
        Write-Host ">>> [PHASE: $PhaseName] SUCCESS in ${duration}s" -ForegroundColor Green
        return [PSCustomObject]@{
            Phase    = $PhaseName
            Status   = "PASSED"
            Duration = "${duration}s"
            Task     = $targetTask
        }
    } else {
        Write-Host ">>> [PHASE: $PhaseName] FAILED (exit code: $exitCode) in ${duration}s" -ForegroundColor Red
        return [PSCustomObject]@{
            Phase    = $PhaseName
            Status   = "FAILED"
            Duration = "${duration}s"
            Task     = $targetTask
        }
    }
}

$Summary = @()
$OverallFailed = $false

switch ($Type) {
    "unit" {
        $res = Run-Phase -PhaseName "UNIT TESTS" -TaskName "testUnit" -Color Yellow
        $Summary += $res
        if ($res.Status -eq "FAILED") { $OverallFailed = $true }
    }
    "bdd" {
        $res = Run-Phase -PhaseName "BDD SCENARIOS (Cucumber)" -TaskName "testBdd" -Color Magenta
        $Summary += $res
        if ($res.Status -eq "FAILED") { $OverallFailed = $true }
    }
    "tdd" {
        $res = Run-Phase -PhaseName "TDD SPECIFICATIONS (Contracts)" -TaskName "testTdd" -Color Blue
        $Summary += $res
        if ($res.Status -eq "FAILED") { $OverallFailed = $true }
    }
    "all" {
        $res = Run-Phase -PhaseName "ALL TESTS (Unified)" -TaskName "test" -Color Cyan
        $Summary += $res
        if ($res.Status -eq "FAILED") { $OverallFailed = $true }
    }
    "separate" {
        Write-Host "Running 3 distinct phases separately: [1/3] UNIT -> [2/3] BDD -> [3/3] TDD" -ForegroundColor Yellow

        $resUnit = Run-Phase -PhaseName "UNIT TESTS" -TaskName "testUnit" -Color Yellow
        $Summary += $resUnit
        if ($resUnit.Status -eq "FAILED") { $OverallFailed = $true }

        $resBdd = Run-Phase -PhaseName "BDD SCENARIOS (Cucumber)" -TaskName "testBdd" -Color Magenta
        $Summary += $resBdd
        if ($resBdd.Status -eq "FAILED") { $OverallFailed = $true }

        $resTdd = Run-Phase -PhaseName "TDD SPECIFICATIONS (Contracts)" -TaskName "testTdd" -Color Blue
        $Summary += $resTdd
        if ($resTdd.Status -eq "FAILED") { $OverallFailed = $true }
    }
}

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host "  EXECUTION SUMMARY" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
$Summary | Format-Table -AutoSize

if ($Coverage) {
    Write-Host "`n--- Code Coverage Reports ---" -ForegroundColor Cyan
    Get-ChildItem -Path $ScriptDir -Filter "index.html" -Recurse |
        Where-Object { $_.FullName -match "\\build\\reports\\jacoco\\test\\html\\index\.html$" } |
        ForEach-Object {
            Write-Host "Coverage Report: file:///$($_.FullName.Replace('\', '/'))" -ForegroundColor Gray
        }
}

if ($OverallFailed) {
    Write-Host "`nResult: ONE OR MORE TEST PHASES FAILED." -ForegroundColor Red
    exit 1
} else {
    Write-Host "`nResult: ALL REQUESTED TEST PHASES PASSED SUCCESSFULLY." -ForegroundColor Green
    exit 0
}
