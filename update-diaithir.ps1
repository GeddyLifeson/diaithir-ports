# Update Diaithir: pulls the latest test pack into the Prism instance.
# Run it by double-clicking the "Update Diaithir" shortcut. It needs no typing.
# The pack address lives in pack_url.txt next to this script on GitHub, so it can be
# retargeted without handing you a new file.

$ErrorActionPreference = 'Stop'
$base = 'https://raw.githubusercontent.com/GeddyLifeson/diaithir-ports/refs/heads/claude/pc-updater'
$prism = 'C:\Prism'
$log = Join-Path ([Environment]::GetFolderPath('Desktop')) 'update-diaithir.log'

try { Start-Transcript -Path $log -Force | Out-Null } catch { }

function Finish($code) {
    try { Stop-Transcript | Out-Null } catch { }
    Write-Host ''
    Read-Host 'Press Enter to close'
    exit $code
}

try {
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    Write-Host 'Diaithir updater'
    Write-Host '----------------'

    # 1. Which pack?
    $stamp = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    $packUrl = ([string](Invoke-RestMethod -Uri "$base/pack_url.txt?t=$stamp" -UseBasicParsing)).Trim()
    if ($packUrl -notmatch '^https://raw\.githubusercontent\.com/GeddyLifeson/diaithir-ports/.+/pack\.toml$') {
        throw "Unexpected pack address: $packUrl"
    }
    Write-Host "Pack: $packUrl"
    $zipUrl = $packUrl -replace 'pack\.toml$', 'Diaithir.zip'

    # 2. Find the Prism instance called Diaithir.
    $instRoot = Join-Path $prism 'instances'
    if (-not (Test-Path $instRoot)) { throw "No Prism instances folder at $instRoot" }
    $inst = $null
    $cands = @(Get-ChildItem -Path $instRoot -Directory -ErrorAction SilentlyContinue | Where-Object {
        $cfg = Join-Path $_.FullName 'instance.cfg'
        (Test-Path $cfg) -and ((Get-Content $cfg -ErrorAction SilentlyContinue) -match '^name=Diaithir')
    })
    if ($cands.Count -eq 0) {
        $cands = @(Get-ChildItem -Path $instRoot -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -like '*Diaithir*' })
    }
    if ($cands.Count -gt 0) {
        $inst = ($cands | Sort-Object LastWriteTime -Descending | Select-Object -First 1)
    }

    $zipPath = Join-Path $env:TEMP 'Diaithir-import.zip'
    $zipDownloaded = $false
    function Get-Zip {
        if (-not $script:zipDownloaded) {
            Write-Host 'Downloading the instance template...'
            Invoke-WebRequest -Uri $zipUrl -OutFile $zipPath -UseBasicParsing
            $script:zipDownloaded = $true
        }
    }

    if ($null -eq $inst) {
        Write-Host 'No Diaithir instance found, creating one.'
        Get-Zip
        $tmp = Join-Path $env:TEMP 'Diaithir-import'
        if (Test-Path $tmp) { Remove-Item $tmp -Recurse -Force }
        Expand-Archive -Path $zipPath -DestinationPath $tmp -Force
        $dest = Join-Path $instRoot 'Diaithir'
        if (Test-Path $dest) { $dest = Join-Path $instRoot ('Diaithir-' + $stamp) }
        Copy-Item -Path (Join-Path $tmp 'Diaithir') -Destination $dest -Recurse -Force
        $inst = Get-Item $dest
    }
    Write-Host "Instance: $($inst.FullName)"

    # 3. Minecraft folder and the packwiz installer.
    $mc = Join-Path $inst.FullName '.minecraft'
    if (-not (Test-Path $mc)) { $mc = Join-Path $inst.FullName 'minecraft' }
    if (-not (Test-Path $mc)) { New-Item -ItemType Directory -Path $mc | Out-Null }
    $boot = Join-Path $mc 'packwiz-installer-bootstrap.jar'
    if (-not (Test-Path $boot)) {
        Get-Zip
        $tmp2 = Join-Path $env:TEMP 'Diaithir-boot'
        if (Test-Path $tmp2) { Remove-Item $tmp2 -Recurse -Force }
        Expand-Archive -Path $zipPath -DestinationPath $tmp2 -Force
        Copy-Item -Path (Join-Path $tmp2 'Diaithir\.minecraft\packwiz-installer-bootstrap.jar') -Destination $boot -Force
    }

    # 4. Java: Prism's own, otherwise whatever is installed.
    $java = $null
    $jdirs = @(Get-ChildItem -Path (Join-Path $prism 'java') -Recurse -Filter 'java.exe' -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match '\\bin\\java\.exe$' } | Sort-Object FullName -Descending)
    if ($jdirs.Count -gt 0) { $java = $jdirs[0].FullName }
    if (-not $java) {
        $cmd = Get-Command java.exe -ErrorAction SilentlyContinue
        if ($cmd) { $java = $cmd.Source }
    }
    if (-not $java) { throw 'Could not find Java. Start Prism once so it downloads Java, then run this again.' }
    Write-Host "Java: $java"

    # 5. Point the instance at this pack so every launch keeps updating, and keep 12 GB.
    $running = @(Get-Process -Name 'prismlauncher*' -ErrorAction SilentlyContinue)
    $cfgPath = Join-Path $inst.FullName 'instance.cfg'
    if ($running.Count -gt 0) {
        Write-Host 'Prism is open, so its settings file is left alone. The mods are still updated below.'
    } elseif (Test-Path $cfgPath) {
        $lines = @(Get-Content $cfgPath)
        $pre = 'PreLaunchCommand="$INST_JAVA" -jar packwiz-installer-bootstrap.jar ' + $packUrl
        $out = @()
        $havePre = $false; $haveOv = $false; $haveMax = $false
        foreach ($l in $lines) {
            if ($l -match '^PreLaunchCommand=') { $out += $pre; $havePre = $true }
            elseif ($l -match '^OverrideCommands=') { $out += 'OverrideCommands=true'; $haveOv = $true }
            elseif ($l -match '^MaxMemAlloc=') { $out += 'MaxMemAlloc=12288'; $haveMax = $true }
            else { $out += $l }
        }
        if (-not $haveOv) { $out += 'OverrideCommands=true' }
        if (-not $havePre) { $out += $pre }
        if (-not $haveMax) { $out += 'OverrideMemory=true'; $out += 'MaxMemAlloc=12288' }
        [IO.File]::WriteAllLines($cfgPath, $out, (New-Object Text.UTF8Encoding($false)))
        Write-Host 'Instance now updates from this pack on every launch (12 GB memory).'
    }

    # 6. Pull the pack.
    Write-Host ''
    Write-Host 'Updating mods and files (the first run downloads a lot)...'
    Push-Location $mc
    try {
        & $java -jar $boot -g $packUrl
        $code = $LASTEXITCODE
    } finally {
        Pop-Location
    }
    if ($code -ne 0) { throw "The pack installer stopped with code $code. See the lines above." }

    $mods = @(Get-ChildItem -Path (Join-Path $mc 'mods') -Filter '*.jar' -ErrorAction SilentlyContinue).Count
    Write-Host ''
    Write-Host "Done. $mods mods installed. Start Diaithir from Prism."
    Finish 0
}
catch {
    Write-Host ''
    Write-Host "Update failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "A log is on your Desktop: $log"
    Finish 1
}
