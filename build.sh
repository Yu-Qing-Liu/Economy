#!/bin/sh

./gradlew clean shadowJar
cp ./build/libs/* ../DevServer/plugins/Economy.jar
