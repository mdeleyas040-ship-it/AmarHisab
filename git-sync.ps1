$ErrorActionPreference = "Stop"

git add .

$status = git status --porcelain

if (-not $status) {
    Write-Host "কোনো নতুন পরিবর্তন নেই।"
    Read-Host "Enter চাপুন"
    exit
}

$message = Read-Host "Commit message দিন"

if ([string]::IsNullOrWhiteSpace($message)) {
    $message = "update amar hisab"
}

git commit -m "$message"
git push origin master

Write-Host ""
Write-Host "================================"
Write-Host "Commit + Push সফল হয়েছে!"
Write-Host "================================"
Read-Host "Enter চাপুন"
