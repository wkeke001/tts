param(
    [string]$ProjectPath = "D:\project\keling-tts",
    [string]$ServerUser = "ubuntu",
    [string]$ServerHost = "175.178.44.121",
    [string]$RemoteDir = "/home/www/keling-tts/app",
    [string]$JarName = "keling-tts.jar",
    [string]$Profile = "prod"
)

$Server = "$ServerUser@$ServerHost"

Write-Host "====================================="
Write-Host "Keling TTS Deploy Script"
Write-Host "====================================="
Write-Host "Project: $ProjectPath"
Write-Host "Server: $Server"
Write-Host "Remote: $RemoteDir"
Write-Host "Jar: $JarName"
Write-Host "Profile: $Profile"
Write-Host "====================================="

if (!(Test-Path $ProjectPath)) {
    Write-Host "Project path not found: $ProjectPath" -ForegroundColor Red
    exit 1
}

Set-Location $ProjectPath

if (!(Test-Path "$ProjectPath\pom.xml")) {
    Write-Host "pom.xml not found" -ForegroundColor Red
    exit 1
}

Write-Host "`n[1/5] Maven package (profile: $Profile)..." -ForegroundColor Cyan

mvn clean package --define skipTests --define "spring.profiles.active=$Profile"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven build failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n[2/5] Finding jar..." -ForegroundColor Cyan

$JarFile = Get-ChildItem -Path "$ProjectPath\target" -Filter "*.jar" |
    Where-Object {
        $_.Name -notlike "*sources*" `
        -and $_.Name -notlike "*javadoc*" `
        -and $_.Name -notlike "*.original"
    } |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if ($null -eq $JarFile) {
    Write-Host "No jar found in target" -ForegroundColor Red
    exit 1
}

Write-Host "Found: $($JarFile.FullName)" -ForegroundColor Green

Write-Host "`n[3/5] Creating remote dir & stopping service..." -ForegroundColor Cyan

ssh $Server "mkdir -p $RemoteDir && sudo systemctl stop keling-tts 2>/dev/null; sudo fuser -k 8087/tcp 2>/dev/null; sleep 2"

if ($LASTEXITCODE -ne 0) {
    Write-Host "SSH connection failed" -ForegroundColor Red
    exit 1
}

Write-Host "`n[4/5] Uploading jar & start.sh..." -ForegroundColor Cyan

scp $JarFile.FullName "${Server}:$RemoteDir/$JarName"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Upload jar failed" -ForegroundColor Red
    exit 1
}

scp "$ProjectPath\start.sh" "${Server}:$RemoteDir/start.sh"
ssh $Server "chmod +x $RemoteDir/start.sh"

Write-Host "`n[5/5] Starting service..." -ForegroundColor Cyan

ssh $Server "sudo systemctl start keling-tts"

if ($LASTEXITCODE -ne 0) {
    Write-Host "Start failed, check manually" -ForegroundColor Yellow
} else {
    Write-Host "Service started" -ForegroundColor Green
}

Write-Host "`n====================================="
Write-Host "Deploy done!" -ForegroundColor Green
Write-Host "Remote: $RemoteDir/$JarName"
Write-Host "Profile: $Profile"
Write-Host "====================================="
