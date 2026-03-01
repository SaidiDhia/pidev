# Build and run the DeepFace API in Docker.
# Prerequisite: Start Docker Desktop first.
# Then run: .\run-deepface-api.ps1

Set-Location $PSScriptRoot

Write-Host "Building DeepFace API image (this may take several minutes)..." -ForegroundColor Cyan
docker build -f Dockerfile.deepface -t deepface-api .
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed. Is Docker Desktop running?" -ForegroundColor Red
    exit 1
}

# Remove existing container if present (so run can be repeated)
docker rm -f deepface-api 2>$null

Write-Host "Starting DeepFace API on http://localhost:5000 ..." -ForegroundColor Green
docker run -p 5000:5000 --name deepface-api deepface-api
