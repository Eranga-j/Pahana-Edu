# quick environment check for Git repo issues
Write-Host "=== Git Daily Diagnose ==="
try { git --version } catch { Write-Error "Git missing from PATH"; exit 1 }
git rev-parse --is-inside-work-tree 2>$null
if ($LASTEXITCODE -ne 0) { Write-Error "Not inside a git repo"; exit 1 }
Write-Host ("Repo: " + (Get-Location).Path)
Write-Host ("Branch: " + (git rev-parse --abbrev-ref HEAD).Trim())
Write-Host "Remotes:"
git remote -v | Select-Object -First 4
Write-Host "Status (porcelain):"
git status --porcelain
