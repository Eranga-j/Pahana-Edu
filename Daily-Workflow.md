# Daily Workflow (Task D)

## Run from Command Prompt (no admin needed)
```bat
daily-update.bat "D:\code\Pahana-Edu" "feat(auth): add login"
```
> If the file is not in PATH, run it with its full path or copy it into the repo root and call:  
> `.\daily-update.bat . "chore(daily): update"`

## Run from PowerShell
> If you see "running scripts is disabled", run once:
> `Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned -Force`
>
> Or call with bypass (no policy change):
> `powershell -ExecutionPolicy Bypass -File .\daily-update.ps1 -Path . -Message "feat: ..." -NewBranch`

```pwsh
.\daily-update.ps1 -Path "D:\code\Pahana-Edu" -Message "feat(report): monthly revenue" -NewBranch
```

## What the scripts do
1. Verify Git and that you're inside a repository.
2. Pull with rebase.
3. Stage all changes.
4. Commit only if there are staged changes (auto message if not supplied).
5. Push (sets upstream automatically if missing).

## Troubleshooting
- **'git' is not recognized** → Install Git for Windows and reopen terminal.
- **Not a git repository** → Run the script in the repo folder or pass `-Path` / folder argument.
- **Push failed (auth)** → Run `git remote -v` and ensure you are logged in (`git config user.name` / `git config user.email`; use HTTPS with a token or set up SSH).
- **Conflicts after pull** → Resolve files, `git add -A`, re-run the script.
