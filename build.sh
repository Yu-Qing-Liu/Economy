#!/bin/sh

./gradlew clean shadowJar
cp ./build/libs/* ../Server/plugins/Economy.jar
