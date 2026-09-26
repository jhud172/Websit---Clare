[CmdletBinding(SupportsShouldProcess, ConfirmImpact = "High")]
param(
    [Parameter(Mandatory)]
    [string]$BackupFile,
    [Parameter(Mandatory)]
    [string]$TargetDatabase,
    [Parameter(Mandatory)]
    [string]$ConfirmTargetDatabase,
    [switch]$ReplaceExisting
)

$ErrorActionPreference = "Stop"
$backup = [System.IO.Path]::GetFullPath($BackupFile)
if (-not (Test-Path -LiteralPath $backup -PathType Leaf)) {
    throw "Backup file not found: $backup"
}
if ($TargetDatabase -cne $ConfirmTargetDatabase) {
    throw "Confirmation did not exactly match target database '$TargetDatabase'."
}
foreach ($name in "PGHOST", "PGUSER") {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
        throw "Set $name before restoring. Credentials must not be passed on the command line."
    }
}

$pgRestore = Get-Command pg_restore -ErrorAction Stop
$manifest = "$backup.sha256"
if (Test-Path -LiteralPath $manifest) {
    $expected = ((Get-Content -LiteralPath $manifest -Raw).Trim() -split '\s+')[0]
    $actual = (Get-FileHash -LiteralPath $backup -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($expected -ne $actual) {
        throw "Backup checksum does not match $manifest. Refusing to restore a damaged or substituted file."
    }
}

$operation = if ($ReplaceExisting) { "replace objects in" } else { "restore into" }
if (-not $PSCmdlet.ShouldProcess($TargetDatabase, "$operation database from $backup")) {
    return
}

$arguments = @("--exit-on-error", "--no-owner", "--no-privileges", "--dbname=$TargetDatabase")
if ($ReplaceExisting) {
    $arguments += @("--clean", "--if-exists")
}
$arguments += $backup

& $pgRestore.Source @arguments
if ($LASTEXITCODE -ne 0) {
    throw "pg_restore failed with exit code $LASTEXITCODE. Inspect the target before retrying."
}
Write-Host "Restore completed for database '$TargetDatabase'. Run the documented smoke checks before using it."
