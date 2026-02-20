@echo off
echo ========================================
echo Pushing Awesome System Design Projects
echo ========================================
echo.

cd /d "%~dp0"

echo Step 1: Initializing Git repository...
git init

echo.
echo Step 2: Adding all files...
git add .

echo.
echo Step 3: Creating initial commit...
git commit -m "Initial commit: Add 15 system design projects with documentation"

echo.
echo Step 4: Renaming branch to main...
git branch -M main

echo.
echo Step 5: Adding remote origin...
git remote add origin https://github.com/Codes-tutorials/awesome-system-design-projects.git

echo.
echo Step 6: Pushing to GitHub...
git push -u origin main

echo.
echo ========================================
echo Done! Check your repository at:
echo https://github.com/Codes-tutorials/awesome-system-design-projects
echo ========================================
pause
