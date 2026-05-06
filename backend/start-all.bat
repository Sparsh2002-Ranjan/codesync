@echo off
title CodeSync - Service Launcher
color 0B

echo.
echo  ╔══════════════════════════════════════════════════╗
echo  ║     CodeSync Microservices - Starting All        ║
echo  ║     9 Services + API Gateway                     ║
echo  ╚══════════════════════════════════════════════════╝
echo.

:: ── 1. Auth Service (port 8081) ────────────────────────
echo [1/9] Starting Auth Service (port 8081)...
start "CodeSync - Auth Service [8081]" cmd /c "cd /d %~dp0auth-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 5 /nobreak >nul

:: ── 2. Project Service (port 8082) ─────────────────────
echo [2/9] Starting Project Service (port 8082)...
start "CodeSync - Project Service [8082]" cmd /c "cd /d %~dp0project-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 3 /nobreak >nul

:: ── 3. File Service (port 8083) ────────────────────────
echo [3/9] Starting File Service (port 8083)...
start "CodeSync - File Service [8083]" cmd /c "cd /d %~dp0file-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 3 /nobreak >nul

:: ── 4. Collaboration Service (port 8084) ───────────────
echo [4/9] Starting Collaboration Service (port 8084)...
start "CodeSync - Collaboration Service [8084]" cmd /c "cd /d %~dp0collaboration-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 3 /nobreak >nul

:: ── 5. Version Service (port 8085) ─────────────────────
echo [5/9] Starting Version Service (port 8085)...
start "CodeSync - Version Service [8085]" cmd /c "cd /d %~dp0version-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 3 /nobreak >nul

:: ── 6. Execution Service (port 8086) ───────────────────
echo [6/9] Starting Execution Service (port 8086)...
start "CodeSync - Execution Service [8086]" cmd /c "cd /d %~dp0execution-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 3 /nobreak >nul

:: ── 7. Comment Service (port 8087) ─────────────────────
echo [7/9] Starting Comment Service (port 8087)...
start "CodeSync - Comment Service [8087]" cmd /c "cd /d %~dp0comment-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 3 /nobreak >nul

:: ── 8. Notification Service (port 8088) ────────────────
echo [8/9] Starting Notification Service (port 8088)...
start "CodeSync - Notification Service [8088]" cmd /c "cd /d %~dp0notification-service && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"
timeout /t 5 /nobreak >nul

:: ── 9. API Gateway (port 8080) ─────────────────────────
echo [9/9] Starting API Gateway (port 8080)...
start "CodeSync - API Gateway [8080]" cmd /c "cd /d %~dp0api-gateway && mvnw.cmd spring-boot:run 2>&1 || mvn spring-boot:run 2>&1 || (echo FAILED & pause)"

echo.
echo  ╔══════════════════════════════════════════════════╗
echo  ║  All services launched!                          ║
echo  ║                                                  ║
echo  ║  API Gateway:    http://localhost:8080            ║
echo  ║  Auth Service:   http://localhost:8081            ║
echo  ║  Project:        http://localhost:8082            ║
echo  ║  File:           http://localhost:8083            ║
echo  ║  Collaboration:  http://localhost:8084            ║
echo  ║  Version:        http://localhost:8085            ║
echo  ║  Execution:      http://localhost:8086            ║
echo  ║  Comment:        http://localhost:8087            ║
echo  ║  Notification:   http://localhost:8088            ║
echo  ║                                                  ║
echo  ║  Health Check:   /actuator/health on each port   ║
echo  ╚══════════════════════════════════════════════════╝
echo.
echo Press any key to exit this launcher (services keep running)...
pause >nul
