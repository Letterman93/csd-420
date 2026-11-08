# Zak Nizam | CSD 420 | Module 9.2 | November 8, 2026
# Start the local MySQL installation used for this assignment if it is stopped.

$ErrorActionPreference = 'Stop'
$toolsDir = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..\..\.course-tools'))
$serverExe = Join-Path $toolsDir 'mysql-5.7.44-winx64\bin\mysqld.exe'
$configuration = Join-Path $toolsDir 'mysql-assignment.ini'
if (-not (Test-Path -LiteralPath $serverExe) -or -not (Test-Path -LiteralPath $configuration)) {
    throw 'The local MySQL installation or configuration was not found.'
}
$server = Get-Process mysqld -ErrorAction SilentlyContinue | Where-Object Path -eq $serverExe
if ($server) {
    Write-Output 'The assignment MySQL server is already running.'
    return
}
if (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue) {
    throw 'Port 3306 is in use by another server.'
}
Start-Process -FilePath $serverExe -ArgumentList ('"--defaults-file=' + $configuration + '"') -WindowStyle Hidden | Out-Null
for ($attempt = 0; $attempt -lt 30; $attempt++) {
    if (Get-NetTCPConnection -LocalPort 3306 -State Listen -ErrorAction SilentlyContinue) {
        Write-Output 'The assignment MySQL server is ready on localhost:3306.'
        return
    }
    Start-Sleep -Milliseconds 500
}
throw 'MySQL did not finish starting. Check the local MySQL error log.'
