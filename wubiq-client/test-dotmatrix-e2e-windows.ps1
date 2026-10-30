# Prints wubiq-server's test page through a real wubiq-client on several JDK builds into the
# dot-matrix witness, through a Windows printer driver, and compares each job with the job
# printed by Java 6 in the same run.
#
#   .\test-dotmatrix-e2e-windows.ps1 -Jdks HOME1,HOME2,... [-CompileHome JDK8] [-Server URL] [-Printer NAME]
#
# Needs:
# - wubiq-server running (default http://localhost:8080);
# - a printer (default "Wubiq witness 9-pin") with an ESC/P driver on a Standard TCP/IP port
#   to 127.0.0.1:9100 (../wubiq-dotmatrix-witness/README.md);
# - port 9100 free: the script starts and stops its own listener;
# - ant on the PATH.
#
# -Jdks must include a Java 6 home; it prints first and its job is the reference.
# -CompileHome defaults to the first Java 8 in -Jdks.
# Exit codes: 0 every JDK prints like Java 6, 1 a JDK prints differently or not at all, 2 setup error.
#
# Compatible with Windows PowerShell 5.1.

param(
	[string[]]$Jdks = @(),
	[string]$CompileHome = "",
	[string]$Server = "http://localhost:8080",
	[string]$Printer = "Wubiq witness 9-pin"
)

$ErrorActionPreference = "Stop"
$Base = $PSScriptRoot
$Witness = Join-Path $Base "..\wubiq-dotmatrix-witness"
$Report = Join-Path $Base "build-tests\dotmatrix-e2e-windows"
$Launcher = Join-Path $Base "build-tests\wubiq-client-launcher.jar"

if ($Jdks.Count -eq 0 -and $env:WUBIQ_JDKS) {
	$Jdks = $env:WUBIQ_JDKS -split ";" | Where-Object { $_ }
}

function Fail-Setup($message) {
	[Console]::Error.WriteLine($message)
	exit 2
}

# "1.6.0_119", "17.0.21", ... from java -version.
function Get-JavaVersion($jdkHome) {
	$output = cmd /c "`"$jdkHome\bin\java.exe`" -version 2>&1"
	foreach ($line in $output) {
		if ($line -match 'version "([^"]+)"') {
			return $Matches[1]
		}
	}
	return $null
}

# 6 for "1.6.0_119", 17 for "17.0.21".
function Get-Major($version) {
	$parts = $version -split "[.\-+_]"
	if ($parts[0] -eq "1") {
		return [int]$parts[1]
	}
	return [int]$parts[0]
}

# Quotes one command line argument.
function Quote($argument) {
	return "`"$argument`""
}

# Waits up to $seconds for $condition to return true.
function Wait-For($seconds, [scriptblock]$condition) {
	$deadline = (Get-Date).AddSeconds($seconds)
	while ((Get-Date) -lt $deadline) {
		if (& $condition) {
			return $true
		}
		Start-Sleep -Seconds 1
	}
	return $false
}

function Test-Listening {
	return $null -ne (Get-NetTCPConnection -LocalPort 9100 -State Listen -ErrorAction SilentlyContinue)
}

function Get-Console {
	try {
		return (Invoke-WebRequest -UseBasicParsing -Uri "$Server/wubiq-server/").Content
	} catch {
		return ""
	}
}

# Test page link of the witness printer registered by client $uuid, from the server console.
function Get-TestPageLink($uuid) {
	foreach ($match in [regex]::Matches((Get-Console), "wubiq-print-test\.do\?[^`"']*")) {
		$link = [System.Net.WebUtility]::HtmlDecode($match.Value)
		if ($link -match "uuid=$([regex]::Escape($uuid))(&|$)" -and $link -match "(?i)printServiceName=[^&]*witness") {
			return $link
		}
	}
	return $null
}

# First exception the client logged, or a generic reason.
function Get-ClientFailure($out) {
	foreach ($log in @("client.log", "client-err.log")) {
		$path = Join-Path $out $log
		if (Test-Path $path) {
			$line = Select-String -Path $path -Pattern "Exception|Error:" | Select-Object -First 1
			if ($line) {
				return ($line.Line -replace '^Exception in thread "[^"]*" ', "")
			}
		}
	}
	return "see $out\client.log"
}

function Stop-Started($process) {
	if ($process -and -not $process.HasExited) {
		Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
		$process.WaitForExit(10000) | Out-Null
	}
}

$script:Listener = $null
$script:Client = $null

# Prints the test page with one JDK. Leaves the job in $out\jobs; returns $false when none arrived.
function Invoke-Print($jdkHome, $name, $out) {
	$uuid = "witness-windows-$name"
	if (Test-Path $out) {
		Remove-Item -Recurse -Force $out
	}
	New-Item -ItemType Directory -Force (Join-Path $out "jobs") | Out-Null
	$failure = Join-Path $out "failure.txt"
	try {
		$script:Listener = Start-Process -FilePath $WitnessJava -PassThru -NoNewWindow `
			-ArgumentList "-Djava.awt.headless=true -cp $(Quote "$Witness\bin") net.sf.wubiq.dotmatrix.witness.Main listen --out $(Quote "$out\jobs")" `
			-RedirectStandardOutput (Join-Path $out "witness.log") -RedirectStandardError (Join-Path $out "witness-err.log")
		if (-not (Wait-For 15 { Test-Listening })) {
			Set-Content $failure "the witness listener didn't start"
			return $false
		}
		$script:Client = Start-Process -FilePath "$jdkHome\bin\java.exe" -PassThru -NoNewWindow -WorkingDirectory $out `
			-ArgumentList "-jar $(Quote $Launcher) -h $Server -u $uuid -v" `
			-RedirectStandardOutput (Join-Path $out "client.log") -RedirectStandardError (Join-Path $out "client-err.log")
		Wait-For 120 { (Get-TestPageLink $uuid) -or $script:Client.HasExited } | Out-Null
		$link = Get-TestPageLink $uuid
		if (-not $link) {
			if ($script:Client.HasExited) {
				Set-Content $failure "the client stopped: $(Get-ClientFailure $out)"
			} else {
				Set-Content $failure "the client didn't register the witness printer; $(Get-ClientFailure $out)"
			}
			return $false
		}
		Invoke-WebRequest -UseBasicParsing -Uri "$Server/wubiq-server/$link" -OutFile (Join-Path $out "server-response.html")
		if (-not (Wait-For 240 { Test-Path (Join-Path $out "jobs\job-*\index.html") })) {
			Set-Content $failure "no job reached the witness; $(Get-ClientFailure $out)"
			return $false
		}
		Start-Sleep -Seconds 2
		return $true
	} finally {
		Stop-Started $script:Client
		$script:Client = $null
		Stop-Started $script:Listener
		$script:Listener = $null
	}
}

# Setup checks.
$Runs = @()
foreach ($jdkHome in $Jdks) {
	if (-not (Test-Path "$jdkHome\bin\java.exe")) {
		Fail-Setup "No java.exe in $jdkHome."
	}
	$version = Get-JavaVersion $jdkHome
	if (-not $version) {
		Fail-Setup "Can't read the Java version of $jdkHome."
	}
	$Runs += New-Object PSObject -Property @{
		Home = $jdkHome; Version = $version; Major = (Get-Major $version)
		Name = "java-" + ($version -replace "[._+]", "-")
	}
}
$Reference = $Runs | Where-Object { $_.Major -eq 6 } | Select-Object -First 1
if (-not $Reference) {
	Fail-Setup "-Jdks needs a Java 6 home: its job is the reference."
}
if (-not $CompileHome) {
	$java8 = $Runs | Where-Object { $_.Major -eq 8 } | Select-Object -First 1
	if ($java8) {
		$CompileHome = $java8.Home
	}
}
if (-not $CompileHome -or -not (Test-Path "$CompileHome\bin\javac.exe")) {
	Fail-Setup "A JDK 8 (with javac) is needed to compile for Java 6; pass -CompileHome."
}
if (-not (Get-Command ant -ErrorAction SilentlyContinue)) {
	Fail-Setup "ant is not on the PATH."
}
try {
	Invoke-WebRequest -UseBasicParsing -Uri "$Server/wubiq-server/" | Out-Null
} catch {
	Fail-Setup "wubiq-server doesn't answer at $Server/wubiq-server/: $($_.Exception.Message)"
}
$printerInfo = Get-Printer -Name $Printer -ErrorAction SilentlyContinue
if (-not $printerInfo) {
	Fail-Setup "Printer $Printer not found."
}
$port = Get-PrinterPort -Name $printerInfo.PortName -ErrorAction SilentlyContinue
if (-not $port -or $port.PrinterHostAddress -ne "127.0.0.1" -or $port.PortNumber -ne 9100) {
	Fail-Setup "Printer $Printer doesn't print to a Standard TCP/IP port at 127.0.0.1:9100."
}
if (Test-Listening) {
	Fail-Setup "Port 9100 is in use; stop the witness listener running there."
}

Write-Host "Compiling with $CompileHome"
$env:JAVA_HOME = $CompileHome
cmd /c "ant -q -f `"$Base\build.xml`" compile-tests create-launcher-jar > `"$env:TEMP\wubiq-e2e-build.log`" 2>&1"
if ($LASTEXITCODE -ne 0) {
	Get-Content "$env:TEMP\wubiq-e2e-build.log"
	Fail-Setup "The build failed."
}
$WitnessJava = "$CompileHome\bin\java.exe"

if (Test-Path $Report) {
	Remove-Item -Recurse -Force $Report
}
New-Item -ItemType Directory -Force $Report | Out-Null
$Summary = Join-Path $Report "summary.txt"
Set-Content $Summary "" -NoNewline
$Failed = 0
$ReferenceJob = $null

# Java 6 first: the other jobs are compared with its job.
$Ordered = @($Reference) + @($Runs | Where-Object { $_ -ne $Reference })
foreach ($run in $Ordered) {
	$out = Join-Path $Report $run.Name
	Write-Host "Java $($run.Version) ($($run.Home))"
	$attempts = 1
	if ($run.Major -eq 6) {
		$attempts = 3
	}
	$printed = $false
	for ($attempt = 1; $attempt -le $attempts -and -not $printed; $attempt++) {
		$printed = Invoke-Print $run.Home $run.Name $out
	}
	if (-not $printed) {
		$reason = Get-Content (Join-Path $out "failure.txt") -ErrorAction SilentlyContinue
		Write-Host "  $reason"
		Add-Content $Summary "$($run.Name)`tNOT PRINTED`t$reason"
		$Failed++
		continue
	}
	$job = Get-ChildItem -Directory (Join-Path $out "jobs") -Filter "job-*" | Select-Object -First 1
	$escp = Join-Path $job.FullName "job.escp"
	if ($run -eq $Reference) {
		$ReferenceJob = $escp
		Write-Host "  reference job: $escp"
		Add-Content $Summary "$($run.Name)`tREFERENCE`t$($job.FullName)\index.html"
		continue
	}
	if (-not $ReferenceJob) {
		Add-Content $Summary "$($run.Name)`tNOT COMPARED`tJava 6 didn't print"
		$Failed++
		continue
	}
	$compare = Join-Path $out "compare.txt"
	cmd /c "`"$WitnessJava`" -Djava.awt.headless=true -cp `"$Witness\bin`" net.sf.wubiq.dotmatrix.witness.Main compare `"$ReferenceJob`" `"$escp`" > `"$compare`" 2>&1"
	if ($LASTEXITCODE -eq 0) {
		Add-Content $Summary "$($run.Name)`tSAME`t$($job.FullName)\index.html"
	} else {
		$first = Select-String -Path $compare -Pattern "FAIL" | Select-Object -First 1
		$reason = "see $compare"
		if ($first) {
			$reason = $first.Line -replace "^\s*FAIL\s+", ""
		}
		Add-Content $Summary "$($run.Name)`tDIFFERENT`t$reason"
		$Failed++
	}
	Get-Content $compare | ForEach-Object { Write-Host "  $_" }
}

Write-Host ""
Write-Host "Against the Java $($Reference.Version) job:"
Get-Content $Summary | ForEach-Object {
	$columns = $_ -split "`t"
	Write-Host ("{0,-22} {1,-13} {2}" -f $columns[0], $columns[1], $columns[2])
}
if ($Failed -eq 0) {
	exit 0
}
exit 1
