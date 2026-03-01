@echo off
cd /d "%~dp0"
echo Building DeepFace API image (this may take several minutes)...
docker build -f Dockerfile.deepface -t deepface-api .
if errorlevel 1 (
    echo Build failed. Is Docker Desktop running?
    pause
    exit /b 1
)
docker rm -f deepface-api 2>nul
echo Starting DeepFace API on http://localhost:5000 ...
docker run -p 5000:5000 --name deepface-api deepface-api
pause
