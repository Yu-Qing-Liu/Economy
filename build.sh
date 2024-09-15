#!/bin/bash

./gradlew clean shadowJar
cp ./build/libs/* ../../MinecraftTestServer/plugins/Economy.jar
