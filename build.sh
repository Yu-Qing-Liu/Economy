#!/bin/bash

./gradlew clean build
cp ./build/libs/* ../../MinecraftTestServer/plugins/Economy.jar
