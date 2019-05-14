@echo off

if "%1" == "" (
    echo You have to specify dummy-tp docker image version to build e.g. 0.1
    exit /b 1
)

set version=%1

@echo on
docker build -t dummy-tp:%version% .
docker-compose up -d