@echo off
setlocal
REM === 精简版的签名脚本 ===
REM 脚本改自于Android SDK 35

REM === 杨耿雷 ===
REM 2025-05-08 11:23

REM ==== 查找 Java ====
REM 如果设置了 JAVA_HOME，则使用其下的 java.exe；否则尝试直接使用系统中的 java.exe
if defined JAVA_HOME (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
) else (
    set JAVA_EXE=java.exe
)

REM 验证 java 命令是否可用
%JAVA_EXE% -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Please set JAVA_HOME or ensure java is in PATH.
    exit /b 1
)

REM ==== 定位 apksigner.jar ====
REM %~dp0 表示当前 bat 文件所在目录；jar 文件应与 bat 同目录
set "jarpath=%~dp0apksigner.jar"
if not exist "%jarpath%" (
    echo ERROR: Cannot find apksigner.jar in %~dp0
    exit /b 1
)

REM ==== 设置默认 JVM 启动参数 ====
REM 最大堆内存 1024MB，栈大小 1MB；可通过 -JXmx 等参数覆盖
set javaOpts=-Xmx1024M -Xss1m

REM ==== 解析命令行参数 ====
REM -J 前缀的参数会传给 JVM，其余参数留给 apksigner 本体处理
set params=
:parseArgs
if "%~1"=="" goto run
if /i "%~1:~0,2%"=="-J" (
    REM 去掉 -J 前缀，追加到 javaOpts
    set "opt=%~1"
    set "javaOpts=%javaOpts% -%opt:~2%"
) else (
    REM 普通参数追加到 params
    set "params=%params% %~1"
)
shift
goto parseArgs

REM ==== 执行 apksigner ====
:run
call "%JAVA_EXE%" %javaOpts% -jar "%jarpath%" %params%
