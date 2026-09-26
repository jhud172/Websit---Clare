[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [string]$OutputDirectory,
    [string]$Label = "clare"
)

$ErrorActionPreference = "Stop"

foreach ($name in "PGHOST", "PGDATABASE", "PGUSER") {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
        throw "Set $name before running a database backup. Credentials must be supplied through PostgreSQL environment variables or a pgpass file."
    }
}

$pgDump = Get-Command pg_dump -ErrorAction Stop
$destination = [System.IO.Path]::GetFullPath($OutputDirectory)
[System.IO.Directory]::CreateDirectory($destination) | Out-Null
$safeLabel = $Label -replace '[^a-zA-Z0-9_-]', '-'
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backup = Join-Path $destination "$safeLabel-$stamp.dump"
$manifest = "$backup.sha256"

if (Test-Path -LiteralPath $backup) {
    throw "Refusing to overwrite existing backup: $backup"
}

Write-Host "Creating a compressed PostgreSQL backup in $destination ..."
& $pgDump.Source --format=custom --compress=9 --no-owner --no-privileges --file=$backup
if ($LASTEXITCODE -ne 0) {
    throw "pg_dump failed with exit code $LASTEXITCODE. No successful backup was recorded."
}

$hash = (Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash.ToLowerInvariant()
Set-Content -LiteralPath $manifest -Value "$hash  $([System.IO.Path]::GetFileName($backup))" -Encoding ascii
Write-Host "Backup complete: $backup"
Write-Host "Integrity manifest: $manifest"
