@echo off
echo ========================================================
echo   Pushing Smart Campus Placement System to GitHub
echo   Target: https://github.com/ujjawalsaffar-ship-it/smart-campus-placement-system
echo ========================================================

git push -u origin main

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Code successfully pushed to GitHub!
    echo Visit: https://github.com/ujjawalsaffar-ship-it/smart-campus-placement-system
) else (
    echo.
    echo [NOTE] If this is your first time pushing:
    echo 1. Ensure you created the repository 'smart-campus-placement-system' at https://github.com/new
    echo 2. Complete the GitHub Sign-in prompt in your browser if requested.
)
echo.
pause
