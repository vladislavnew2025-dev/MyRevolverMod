@echo off
setlocal EnableExtensions EnableDelayedExpansion

title MyRevolverMod - Build Script
cd /d "%~dp0"

echo ============================================================
echo MyRevolverMod build helper
echo ============================================================
echo.

set "LOCAL_GRADLE_DIR=%CD%\.tools\gradle-8.7"
set "LOCAL_GRADLE_BIN=%LOCAL_GRADLE_DIR%\bin\gradle.bat"
set "BUILD_CMD="
set "BOOTSTRAP_CMD="
set "SYSTEM_GRADLE="

if exist "%CD%\gradlew.bat" (
    set "BUILD_CMD=%CD%\gradlew.bat"
    echo Found gradle wrapper.
) else (
    for %%G in (gradle.bat gradle.cmd gradle.exe gradle) do (
        if not defined SYSTEM_GRADLE (
            for /f "delims=" %%P in ('where %%G 2^>nul') do (
                if not defined SYSTEM_GRADLE set "SYSTEM_GRADLE=%%P"
            )
        )
    )

    if defined SYSTEM_GRADLE (
        set "BOOTSTRAP_CMD=%SYSTEM_GRADLE%"
        echo Found system gradle:
        echo   %SYSTEM_GRADLE%
    ) else (
        echo Gradle was not found. Downloading local Gradle 8.7...
        if not exist "%CD%\.tools" mkdir "%CD%\.tools"

        powershell -NoProfile -ExecutionPolicy Bypass -Command ^
            "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12;" ^
            "$zip='%CD%\\.tools\\gradle-8.7-bin.zip';" ^
            "$url='https://services.gradle.org/distributions/gradle-8.7-bin.zip';" ^
            "Invoke-WebRequest -Uri $url -OutFile $zip;" ^
            "Expand-Archive -Path $zip -DestinationPath '%CD%\\.tools' -Force;" ^
            "Remove-Item $zip -Force"

        if not exist "%LOCAL_GRADLE_BIN%" (
            echo ERROR: Could not prepare local Gradle.
            goto :fail
        )

        set "BOOTSTRAP_CMD=%LOCAL_GRADLE_BIN%"
        echo Local Gradle is ready.
    )
)

if not defined BUILD_CMD (
    echo.
    echo Creating Gradle wrapper files...
    call "%BOOTSTRAP_CMD%" wrapper --gradle-version 8.7 --distribution-type bin --warning-mode all --stacktrace

    if ERRORLEVEL 1 (
        echo ERROR: Wrapper generation failed.
        goto :fail
    )

    set "BUILD_CMD=%CD%\gradlew.bat"
)

echo.
echo Using Gradle command:
echo   %BUILD_CMD%
echo.

echo Running full clean build...
call "%BUILD_CMD%" --no-daemon clean build --warning-mode all --stacktrace --info
if ERRORLEVEL 1 (
    echo.
    echo Build failed.
    goto :fail
)

echo.
echo ============================================================
echo Build completed successfully.
echo Output JAR files:
dir /b "%CD%\build\libs\*.jar"
echo ============================================================
echo.
goto :end

:fail
echo.
echo The build script ended with errors.

:end
echo.
echo Press any key to close...
pause >nul
endlocal
