# Zak Nizam | CSD 420 | Module 10.2 | November 14, 2026
# Run from this folder. Use -Test to run the database and JavaFX checks.
param(
    [switch]$Test,
    [string]$JavaFxHome = $env:JAVAFX_HOME,
    [string]$MySqlDriver = (Join-Path $PSScriptRoot '..\..\..\..\.course-tools\mysql-connector-j-8.0.33.jar')
)
$ErrorActionPreference = 'Stop'
try {
    if (-not $JavaFxHome -or -not (Test-Path -LiteralPath (Join-Path $JavaFxHome 'lib\javafx.controls.jar'))) {
        throw 'JavaFX was not found. Set JAVAFX_HOME or supply -JavaFxHome with your SDK folder.'
    }
    if (-not (Test-Path -LiteralPath $MySqlDriver -PathType Leaf)) {
        throw 'MySQL Connector/J was not found. Supply -MySqlDriver with the full path to its JAR file.'
    }
    foreach ($command in @('java', 'javac')) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
            throw "The $command command was not found. Install a JDK and add its bin folder to PATH."
        }
    }
    $sources = @('FanRepository.java', 'NizamFanManager.java', 'NizamFanManagerTest.java')
    $sourcePaths = foreach ($source in $sources) {
        $path = Join-Path $PSScriptRoot $source
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            throw "Required source file not found: $source"
        }
        $path
    }
    $build = Join-Path $PSScriptRoot 'build'
    New-Item -ItemType Directory -Force -Path $build | Out-Null
    $modulePath = Join-Path $JavaFxHome 'lib'
    & javac -Xlint:all --module-path $modulePath --add-modules javafx.controls -d $build @sourcePaths
    if ($LASTEXITCODE -ne 0) { throw 'Compilation failed. Review the compiler messages above.' }
    $mainClass = if ($Test) { 'NizamFanManagerTest' } else { 'NizamFanManager' }
    & java --module-path $modulePath --add-modules javafx.controls --enable-native-access=javafx.graphics -cp "$build;$MySqlDriver" $mainClass
    if ($LASTEXITCODE -ne 0) { throw 'The application or tests did not complete successfully.' }
} catch {
    Write-Error $_ -ErrorAction Continue
    exit 1
}
