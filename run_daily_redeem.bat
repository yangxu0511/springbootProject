@echo off
chcp 65001 >nul

cd /d D:\workspace\springbootProject

REM 先编译项目（现在应该能成功了）
call mvn compile -q -DskipTests

if %ERRORLEVEL% neq 0 (
    echo 编译失败，请检查代码
    exit /b 1
)

REM 执行兑奖任务
call mvn exec:java -Dexec.mainClass="com.study.common.redeem.DailyRedeemTask" -Dexec.cleanupDaemonThreads=false -q

exit /b 0
