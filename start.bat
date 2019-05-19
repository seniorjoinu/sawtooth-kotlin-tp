@echo off

echo Stopping old containers...
@echo on
docker-compose down
@echo off

if "%1" == "-r" (
    echo Removing old containers...
    @echo on
    docker-compose rm -f
    @echo off
)

echo Building sources...
call gradlew.bat clean build

echo Building images...
docker-compose build

echo Starting endorser container...
docker-compose up -d endorser

echo Waiting 10 seconds...
timeout 10 > NUL

echo Starting root validator containers...
docker-compose up -d validator-0 rest-api-0 settings-tp-0 dummy-tp-0 pbft-0

echo Waiting 10 seconds...
timeout 10 > NUL

echo Starting 1st validator containers...
docker-compose up -d validator-1 rest-api-1 settings-tp-1 dummy-tp-1 pbft-1

echo Waiting 10 seconds...
timeout 10 > NUL

echo Starting 2nd validator containers...
docker-compose up -d validator-2 rest-api-2 settings-tp-2 dummy-tp-2 pbft-2

echo Waiting 10 seconds...
timeout 10 > NUL

echo Starting 3rd validator containers...
docker-compose up -d validator-3 rest-api-3 settings-tp-3 dummy-tp-3 pbft-3

echo Waiting 10 seconds...
timeout 10 > NUL

echo Starting other containers...
docker-compose up -d shell dummy-transaction-submitter

echo Started containers:
docker ps

echo Waiting 10 seconds...
timeout 10 > NUL

echo Starting 3rd validator containers...
docker-compose up -d validator-adhoc rest-api-adhoc settings-tp-adhoc dummy-tp-adhoc pbft-adhoc

echo Started containers:
docker ps

echo Waiting 20 seconds for validator-adhoc to catch up
timeout 20 > NUL

echo "Current state is:"
docker exec sawtooth-validator-0 /bin/bash -c "sawtooth state list --url \"http://rest-api-0:8008\""

echo Proposing adhoc validator into pbft-members
docker exec sawtooth-validator-0 /bin/bash create-proposal.sh

echo Waiting 10 seconds for proposal to apply
timeout 10 > NUL

echo "Current state is:"
docker exec sawtooth-validator-0 /bin/bash -c "sawtooth state list --url \"http://rest-api-0:8008\""