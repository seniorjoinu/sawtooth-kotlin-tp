FROM cantara/alpine-openjdk-jdk8

LABEL "install-type"="mounted"

RUN mkdir -p /project/sawtooth-sdk-java/ \
 && mkdir -p /var/log/sawtooth \
 && mkdir -p /var/lib/sawtooth \
 && mkdir -p /etc/sawtooth \
 && mkdir -p /etc/sawtooth/keys

COPY build/libs/dummy-tp-0.1.jar dummy-tp.jar