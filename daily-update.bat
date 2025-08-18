@echo off
setlocal enabledelayedexpansion
REM ------------------------------------------------------------
REM  daily-update.bat  (robust version)
REM  Usage:
REM     daily-update.bat [repoPath] ["commit message"]
REM     daily-update.bat . "feat(x): details"
REM ------------------------------------------------------------

REM Default repo is current folder
set "REPO=%~1"
if "%REPO%"=="" set "REPO=."

REM Optional message
set "MSG=%~2"

pushd "%REPO%" 2>nul || (
  echo [ERROR] Folder not found: %REPO%
  exit /b 1
)

REM Check Git availability
git --version >nul 2>&1 || (
  echo [ERROR] Git is not installed or not in PATH.
  popd & exit /b 1
)

REM Check inside a git repo
git rev-parse --is-inside-work-tree >nul 2>&1 || (
  echo [ERROR] Not a git repository: %CD%
  popd & exit /b 1
)

for /f "delims=" %%b in ('git branch --show-current') do set "BRANCH=%%b"

REM Default commit message (no arg given)
if "%MSG%"=="" (
  for /f "usebackq delims=" %%t in (`powershell -NoProfile -Command "(Get-Date).ToString('yyyy-MM-dd HH:mm')"` ) do set "MSG=feat(new jsp): jsp creat %%t"
)

echo.
echo === Daily update ===
echo Repo: %CD%
echo Branch: %BRANCH%
echo Message: %MSG%
echo.

git pull --rebase || echo [WARN] git pull failed. Resolve if needed.

git add -A

REM Commit only if there are changes staged
git diff --cached --quiet
if errorlevel 1 (
  git commit -m "%MSG%" || (echo [ERROR] Commit failed. & popd & exit /b 1)
) else (
  echo [INFO] Nothing to commit.
)

REM Push and set upstream if needed
git rev-parse --abbrev-ref --symbolic-full-name @{u} >nul 2>&1
if errorlevel 1 (
  git push -u origin %BRANCH% || (echo [ERROR] Push failed. Check credentials/remote. & popd & exit /b 1)
) else (
  git push || (echo [ERROR] Push failed. Check credentials/remote. & popd & exit /b 1)
)

echo [OK] Daily update complete.
popd
exit /b 0
