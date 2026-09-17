@echo off
title Amar Hisab - GitHub Save
cd /d "%~dp0"

echo.
echo ========================================
echo        AMAR HISAB - GITHUB SAVE
echo ========================================
echo.

echo [1/3] Changes add করা হচ্ছে...
git add -A

echo.
echo [2/3] Commit করা হচ্ছে...
git diff --cached --quiet
if %errorlevel%==0 (
    echo কোনো নতুন change নেই।
) else (
    git commit -m "Auto save changes"
)

echo.
echo [3/3] GitHub-এ Push করা হচ্ছে...
git push

echo.
echo ========================================
echo              DONE
echo ========================================
echo.
pause