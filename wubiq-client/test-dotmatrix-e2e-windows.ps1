# Prints wubiq-server's test page through a real client into the dot-matrix witness, through a
# Windows printer driver. The rows are the ones of the macOS script: every JDK of -Jdks prints
# with the 2.7.4 jar (legacy-java-VERSION), the JDK 25 of -CompileHome prints with the client
# built from source (source-java-VERSION), and -PackagedJava with -PackagedApp print with an
# installed package (packaged): C:\Program Files\Wubiq\runtime\bin\java.exe and
# C:\Program Files\Wubiq\app. Java 6 prints first, and every other job is compared with its job.
# Each client then prints the page through the fast path (the client writes ESC/P itself, to a
# file); those files are compared the same way, and the witness checks that each one is a well
# formed ESC/P stream. The source and packaged driver jobs also get a ROW-extents row, as in the
# macOS script: the witness measures the width of the job's text lines against
# tests-src\net\sf\wubiq\dotmatrix\golden\test-page.extents (EXTENTS OK, EXTENTS DIFFERENT, or
# NOT CHECKED when there is no extents file).
#
#   .\test-dotmatrix-e2e-windows.ps1 -Jdks JDK6_HOME,JDK8_HOME,... [-CompileHome JDK25_HOME] [-PackagedJava JAVA -PackagedApp DIR] [-Server URL] [-Printer NAME] [-Tolerance IN]
#
# Needs:
# - wubiq-server running (default http://localhost:8080);
# - a printer (default "Wubiq witness 9-pin") with an ESC/P driver on a Standard TCP/IP port
#   to 127.0.0.1:9100 (../wubiq-dotmatrix-witness/README.md);
# - port 9100 free: the script starts and stops its own listener;
# - ant on the PATH.
#
# -Jdks must include a Java 6 home; it prints first and its job is the reference.
# -CompileHome is a JDK 25 with javac; it defaults to the first Java 25 in -Jdks.
# -PackagedJava and -PackagedApp go together: the package's own java.exe and its app directory.
# -Tolerance is the largest accepted shift or size change, in inches (default 0.025, 3 dots at
# 120 dpi).
# The summary has a row per client for the driver job, a row "ROW-fast" for the fast path job and,
# for the source and packaged rows, a row "ROW-extents" for the text extents.
# Exit codes: 0 every row prints like Java 6 and no extents row is EXTENTS DIFFERENT, 1 a row prints
# differently, doesn't print, or its text extents differ, 2 setup error.
#
# Compatible with Windows PowerShell 5.1.

param(
	[string[]]$Jdks = @(),
	[string]$CompileHome = "",
	[string]$PackagedJava = "",
	[string]$PackagedApp = "",
	[string]$Server = "http://localhost:8080",
	[string]$Printer = "Wubiq witness 9-pin",
	[string]$Tolerance = "0.025"
)

$ErrorActionPreference = "Stop"
$Base = $PSScriptRoot
$Witness = Join-Path $Base "..\wubiq-dotmatrix-witness"
$Report = Join-Path $Base "build-tests\dotmatrix-e2e-windows"
$Launcher = Join-Path $Base "build-tests\wubiq-client-launcher.jar"
$LegacyJar = Join-Path $Base "..\wubiq-server\WebContent\wubiq-client.jar"
$Extents = Join-Path $Base "tests-src\net\sf\wubiq\dotmatrix\golden\test-page.extents"

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
$script:FastFailure = $null
$script:FastStray = $null

# The fast path takes the PC850 character table, as the dot-matrix witness tests do.
$ClientProperties = "-Dwubiq.dotmatrix.charset=IBM850 -Dwubiq.dotmatrix.table=3"

# Prints the test page with one client: its java.exe and the arguments that start it. Leaves the
# job in $out\jobs; returns $false when none arrived. Then prints it through the fast path into
# $out\fast.escp; $script:FastFailure says why it didn't.
function Invoke-Print($javaExe, $javaArguments, $name, $out) {
	$uuid = "witness-windows-$name"
	$script:FastFailure = $null
	$script:FastStray = $null
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
		$script:Client = Start-Process -FilePath $javaExe -PassThru -NoNewWindow -WorkingDirectory $out `
			-ArgumentList "$ClientProperties $javaArguments -h $Server -u $uuid -v" `
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
		# Fast path: the link of the test page with TP=true (send it as a Pageable) and TPT, the
		# destination the client writes the ESC/P bytes to, a file path on this computer. The server
		# makes a URI of it, which can't have backslashes: the path goes with forward slashes.
		$fast = Join-Path $out "fast.escp"
		$fastUri = $fast -replace "\\", "/"
		$fastLink = "$link&TP=true&TPT=$([System.Uri]::EscapeDataString($fastUri))"
		$jobsBefore = @(Get-ChildItem -Directory (Join-Path $out "jobs") -Filter "job-*").Count
		Invoke-WebRequest -UseBasicParsing -Uri "$Server/wubiq-server/$fastLink" -OutFile (Join-Path $out "server-response-fast.html")
		if (Wait-For 120 { (Test-Path $fast) -and ((Get-Item $fast).Length -gt 0) }) {
			Start-Sleep -Seconds 6
			# The fast path doesn't use the printer driver: no job may reach the printer.
			$strays = @(Get-ChildItem -Directory (Join-Path $out "jobs") -Filter "job-*").Count - $jobsBefore
			if ($strays -gt 0) {
				$script:FastStray = "the printer received $strays job(s) besides the file"
			}
		} else {
			$script:FastFailure = "no file was written; $(Get-ClientFailure $out)"
		}
		return $true
	} finally {
		Stop-Started $script:Client
		$script:Client = $null
		Stop-Started $script:Listener
		$script:Listener = $null
	}
}

# Checks the fast path file of one JDK and adds its row to the summary: the witness must read it
# without warnings, and it must match the Java 6 one. Returns $true when it passed.
function Test-FastPath($run, $out, $failure) {
	$name = "$($run.Name)-fast"
	$fast = Join-Path $out "fast.escp"
	if ($failure) {
		Add-Content $Summary "$name`tNOT PRINTED`t$failure"
		return $false
	}
	if ($script:FastStray) {
		Add-Content $Summary "$name`tDRIVER JOB`t$($script:FastStray)"
		return $false
	}
	$render = Join-Path $out "fast-render.txt"
	cmd /c "`"$WitnessJava`" -Djava.awt.headless=true -cp `"$Witness\bin`" net.sf.wubiq.dotmatrix.witness.Main render --strict --out `"$out\fast-report`" `"$fast`" > `"$render`" 2>&1"
	if ($LASTEXITCODE -ne 0) {
		$warning = Select-String -Path $render -Pattern "^\s+@" | Select-Object -First 1
		$reason = "see $render"
		if ($warning) {
			$reason = $warning.Line.Trim()
		}
		Add-Content $Summary "$name`tWARNINGS`t$reason"
		return $false
	}
	if ($run -eq $Reference) {
		$script:ReferenceFast = $fast
		Add-Content $Summary "$name`tREFERENCE`t$out\fast-report\index.html"
		return $true
	}
	if (-not $script:ReferenceFast) {
		Add-Content $Summary "$name`tNOT COMPARED`tJava 6 didn't print through the fast path"
		return $false
	}
	$compare = Join-Path $out "fast-compare.txt"
	cmd /c "`"$WitnessJava`" -Djava.awt.headless=true -cp `"$Witness\bin`" net.sf.wubiq.dotmatrix.witness.Main compare --tolerance $Tolerance `"$($script:ReferenceFast)`" `"$fast`" > `"$compare`" 2>&1"
	if ($LASTEXITCODE -eq 0) {
		Add-Content $Summary "$name`tSAME`t$out\fast-report\index.html"
		return $true
	}
	$first = Select-String -Path $compare -Pattern "FAIL" | Select-Object -First 1
	$reason = "see $compare"
	if ($first) {
		$reason = $first.Line -replace "^\s*FAIL\s+", ""
	}
	Add-Content $Summary "$name`tDIFFERENT`t$reason"
	return $false
}

# Checks the text extents of the driver job $escp of one run and adds its row to the summary.
# Returns $false when the extents differ.
function Test-Extents($run, $out, $escp) {
	$name = "$($run.Name)-extents"
	if (-not (Test-Path $Extents -PathType Leaf)) {
		Add-Content $Summary "$name`tNOT CHECKED`tno $Extents"
		return $true
	}
	$extentsOut = Join-Path $out "extents.txt"
	cmd /c "`"$WitnessJava`" -Djava.awt.headless=true -cp `"$Witness\bin`" net.sf.wubiq.dotmatrix.witness.Main extents --expect `"$Extents`" `"$escp`" > `"$extentsOut`" 2>&1"
	if ($LASTEXITCODE -eq 0) {
		Add-Content $Summary "$name`tEXTENTS OK`t$extentsOut"
		return $true
	}
	$first = Select-String -Path $extentsOut -Pattern "FAIL" | Select-Object -First 1
	$reason = "see $extentsOut"
	if ($first) {
		$reason = $first.Line -replace "^\s*FAIL\s+", ""
	}
	Add-Content $Summary "$name`tEXTENTS DIFFERENT`t$reason"
	return $false
}

# Setup checks.
if (($PackagedJava -and -not $PackagedApp) -or (-not $PackagedJava -and $PackagedApp)) {
	Fail-Setup "-PackagedJava and -PackagedApp go together."
}
if ($PackagedJava -and (-not (Test-Path $PackagedJava -PathType Leaf) -or -not (Test-Path $PackagedApp -PathType Container))) {
	Fail-Setup "No packaged client at $PackagedJava and $PackagedApp."
}
if (-not (Test-Path $LegacyJar -PathType Leaf)) {
	Fail-Setup "No 2.7.4 jar at $LegacyJar."
}
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
		Name = "legacy-java-" + ($version -replace "[._+]", "-")
		JavaExe = "$jdkHome\bin\java.exe"; JavaArguments = "-jar " + (Quote $LegacyJar); Extents = $false
	}
}
$Reference = $Runs | Where-Object { $_.Major -eq 6 } | Select-Object -First 1
if (-not $Reference) {
	Fail-Setup "-Jdks needs a Java 6 home: its job is the reference."
}
if (-not $CompileHome) {
	$java25 = $Runs | Where-Object { $_.Major -eq 25 } | Select-Object -First 1
	if ($java25) {
		$CompileHome = $java25.Home
	}
}
if (-not $CompileHome -or -not (Test-Path "$CompileHome\bin\javac.exe")) {
	Fail-Setup "A JDK 25 (with javac) is needed to compile the client; pass -CompileHome."
}
$compileVersion = Get-JavaVersion $CompileHome
$Runs += New-Object PSObject -Property @{
	Home = $CompileHome; Version = $compileVersion; Major = (Get-Major $compileVersion)
	Name = "source-java-" + ($compileVersion -replace "[._+]", "-")
	JavaExe = "$CompileHome\bin\java.exe"; JavaArguments = "-jar " + (Quote $Launcher); Extents = $true
}
if ($PackagedJava) {
	# The package's runtime home: java.exe is in its bin directory.
	$packagedHome = Split-Path (Split-Path $PackagedJava)
	$packagedVersion = Get-JavaVersion $packagedHome
	$Runs += New-Object PSObject -Property @{
		Home = $packagedHome; Version = $packagedVersion; Major = (Get-Major $packagedVersion)
		Name = "packaged"; JavaExe = $PackagedJava
		JavaArguments = "--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.desktop/javax.print=ALL-UNNAMED --add-opens=java.desktop/sun.print=ALL-UNNAMED -cp " + (Quote "$PackagedApp\*") + " net.sf.wubiq.clients.LocalPrintManager"
		Extents = $true
	}
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
$script:ReferenceFast = $null

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
		$printed = Invoke-Print $run.JavaExe $run.JavaArguments $run.Name $out
	}
	if (-not $printed) {
		$reason = Get-Content (Join-Path $out "failure.txt") -ErrorAction SilentlyContinue
		Write-Host "  $reason"
		Add-Content $Summary "$($run.Name)`tNOT PRINTED`t$reason"
		Add-Content $Summary "$($run.Name)-fast`tNOT RUN`tthe test page didn't print"
		$Failed++
		continue
	}
	if (-not (Test-FastPath $run $out $script:FastFailure)) {
		$Failed++
	}
	$job = Get-ChildItem -Directory (Join-Path $out "jobs") -Filter "job-*" | Select-Object -First 1
	$escp = Join-Path $job.FullName "job.escp"
	if ($run -eq $Reference) {
		$ReferenceJob = $escp
		Write-Host "  reference job: $escp"
		Add-Content $Summary "$($run.Name)`tREFERENCE`t$($job.FullName)\index.html"
		if ($run.Extents -and -not (Test-Extents $run $out $escp)) {
			$Failed++
		}
		continue
	}
	if (-not $ReferenceJob) {
		Add-Content $Summary "$($run.Name)`tNOT COMPARED`tJava 6 didn't print"
		$Failed++
		continue
	}
	$compare = Join-Path $out "compare.txt"
	cmd /c "`"$WitnessJava`" -Djava.awt.headless=true -cp `"$Witness\bin`" net.sf.wubiq.dotmatrix.witness.Main compare --tolerance $Tolerance `"$ReferenceJob`" `"$escp`" > `"$compare`" 2>&1"
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
	if ($run.Extents -and -not (Test-Extents $run $out $escp)) {
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
