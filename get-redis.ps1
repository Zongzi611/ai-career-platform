$release = Invoke-RestMethod -Uri 'https://api.github.com/repos/tporadowski/redis/releases/latest' -UseBasicParsing
$zip = $release.assets | Where-Object { $_.name -like '*zip*' -and $_.name -like '*x64*' } | Select-Object -First 1
Write-Host "Version: $($release.tag_name)"
Write-Host "File: $($zip.name)"
Write-Host "Size: $([math]::Round($zip.size/1MB,1)) MB"
Write-Host "Downloading to E:\Redis-$($zip.name)..."
Invoke-WebRequest -Uri $zip.browser_download_url -OutFile "E:\$($zip.name)" -UseBasicParsing
Write-Host "Done! Downloaded to E:\$($zip.name)"
