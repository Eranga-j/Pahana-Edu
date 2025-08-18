#requires -Version 5.1
<#
.SYNOPSIS
  Daily Git update helper for PowerShell (robust).
#>
param(
  [string]$Path = ".",
  [string]$Message,
  [switch]$NewBranch
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

try {
  Set-Location -Path $Path
} catch {
  Write-Error "Folder not found: $Path"
}

# Git present?
git --version *> $null
if ($LASTEXITCODE -ne 0) { throw "Git is not installed or not in PATH." }

# Repo?
git rev-parse --is-inside-work-tree *> $null
if ($LASTEXITCODE -ne 0) { throw "Not a git repository at '$((Get-Location).Path)'." }

# Optional new branch
if ($NewBranch) {
  $date = Get-Date -Format "yyyyMMdd"
  $candidate = "feat/daily-$date"
  git checkout -b $candidate
}

$branchName = (git rev-parse --abbrev-ref HEAD).Trim()

if (-not $Message) { $Message = "chore(daily): auto update $(Get-Date -Format 'yyyy-MM-dd HH:mm')" }

Write-Host ""
Write-Host "=== Daily update ==="
Write-Host "Repo: $((Get-Location).Path)"
Write-Host "Branch: $branchName"
Write-Host "Message: $Message"
Write-Host ""

try { git pull --rebase } catch { Write-Warning "git pull failed. Resolve if needed."; }

git add -A

git diff --cached --quiet
if ($LASTEXITCODE -eq 0) {
  Write-Host "[INFO] Nothing to commit."
} else {
  git commit -m "New Customer add"
}

# Push; set upstream if missing
git rev-parse --abbrev-ref --symbolic-full-name @{u} *> $null
if ($LASTEXITCODE -ne 0) {
  git push -u origin $branchName
} else {
  git push
}

Write-Host "[OK] Daily update complete."
