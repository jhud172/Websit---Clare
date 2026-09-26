[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$mavenWrapper = Join-Path $projectRoot "mvnw.cmd"
if (-not (Test-Path -LiteralPath $mavenWrapper)) {
    throw "Maven wrapper not found at $mavenWrapper"
}

Push-Location $projectRoot
try {
    & $mavenWrapper -q -DskipTests compile exec:java "-Dexec.mainClass=co.uk.clarebrunton.ceremonies.tool.AdminPasswordHashGenerator"
    if ($LASTEXITCODE -ne 0) {
        throw "Password hash generation failed with exit code $LASTEXITCODE."
    }
}
finally {
    Pop-Location
}
