@ECHO OFF
SETLOCAL
SET DIR=%~dp0
SET JAVA_EXE=%JAVA_HOME%\bin\java.exe
IF NOT EXIST "%JAVA_EXE%" SET JAVA_EXE=java
SET WRAPPER_JAR=%DIR%gradle\wrapper\gradle-wrapper.jar
SET PROPS_FILE=%DIR%gradle\wrapper\gradle-wrapper.properties

IF NOT EXIST "%WRAPPER_JAR%" (
  IF NOT EXIST "%PROPS_FILE%" (
    ECHO Gradle wrapper properties not found at %PROPS_FILE%
    EXIT /B 1
  )

  FOR /F "tokens=1,* delims==" %%A IN ('findstr "^distributionUrl=" "%PROPS_FILE%"') DO SET DIST_URL=%%B
  IF "%DIST_URL%"=="" (
    ECHO distributionUrl is not defined in %PROPS_FILE%
    EXIT /B 1
  )

  ECHO Downloading Gradle wrapper JAR from %DIST_URL% ...
  powershell -NoLogo -NoProfile -Command "`$temp = New-Item -ItemType Directory -Path ([System.IO.Path]::GetTempPath()) -Name ('gradle-' + [System.Guid]::NewGuid()); `$zip = Join-Path `$temp 'gradle-dist.zip'; Invoke-WebRequest '%DIST_URL%' -OutFile `$zip; Add-Type -AssemblyName System.IO.Compression.FileSystem; `$zipFile = [System.IO.Compression.ZipFile]::OpenRead(`$zip); `$jarEntry = `$zipFile.Entries | Where-Object { `$_.FullName -like '*/lib/gradle-wrapper-*.jar' } | Select-Object -First 1; if (-not `$jarEntry) { Write-Error 'Wrapper JAR not found in distribution'; exit 1 }; [System.IO.Compression.ZipFileExtensions]::ExtractToFile(`$jarEntry, '%WRAPPER_JAR%', $true); `$zipFile.Dispose(); Remove-Item `$temp -Recurse -Force" || EXIT /B 1
)

"%JAVA_EXE%" -Dfile.encoding=UTF-8 -jar "%WRAPPER_JAR%" %*
ENDLOCAL
