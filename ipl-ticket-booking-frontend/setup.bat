@echo off
echo ========================================
echo IPL Ticket Booking Frontend Setup
echo ========================================
echo.

echo Checking Node.js installation...
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js version:
node --version

echo.
echo Checking npm installation...
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: npm is not installed or not in PATH
    pause
    exit /b 1
)

echo npm version:
npm --version

echo.
echo Installing Angular CLI globally...
npm install -g @angular/cli

echo.
echo Installing project dependencies...
npm install

echo.
echo Setup completed successfully!
echo.
echo To start the development server, run:
echo   npm start
echo   or
echo   ng serve
echo.
echo The application will be available at http://localhost:4200
echo.
pause