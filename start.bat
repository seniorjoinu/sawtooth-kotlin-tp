@echo off
if "%1" == "" (
    echo You have to specify dummy-tp docker image version to build e.g. 0.1
    exit /b 1
)

set version=%1

echo Stopping old containers...
@echo on
docker-compose down
@echo off

if "%2" == "-r" (
    docker system prune -f
)

echo Building sources...
call gradlew.bat clean build

echo Building new containers...
@echo on
docker build -t dummy-tp:%version% -f Dockerfile-dummy-tp .
docker build -t dummy-transaction-submitter:%version% -f Dockerfile-transaction-submitter .
docker build -t sawtooth-pbft-engine-local:0.1 -f Dockerfile-pbft-engine .
@echo off

echo Starting containers...
@echo on
docker-compose up -d shell validator-0 validator-1 validator-2 validator-3 rest-api-0 rest-api-1 rest-api-2 rest-api-3 settings-tp-0 settings-tp-1 settings-tp-2 settings-tp-3 dummy-tp-0 dummy-tp-1 dummy-tp-2 dummy-tp-3 dummy-transaction-submitter pbft-0 pbft-1 pbft-2 pbft-3
@echo off

echo Started containers:
@echo on
docker ps