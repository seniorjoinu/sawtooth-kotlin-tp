#!/bin/bash

# looping over files and getting they content
FILES=/etc/sawtooth/keys/validator-*.pub
last_file=$(ls -t $FILES | tail -1)

keys=""
for f in $FILES
do
    key_content=$(<$f)
    if [[ $f = $last_file ]]
        then keys="$keys\"$key_content\""
        else keys="$keys\"$key_content\","
    fi
done

your_key_file=/etc/sawtooth/keys/validator.pub
your_key=$(<$your_key_file)
keys="[\"$your_key\",$keys]"

# creating proposal TODO: fix hardcoded url
sawset proposal create --key /etc/sawtooth/keys/validator.priv sawtooth.consensus.pbft.members=${keys} --url "http://rest-api-0:8008"