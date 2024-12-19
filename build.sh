#!/bin/bash

./gradlew clean shadowJar
cp ./build/libs/* ../../Minecraft/plugins/Economy.jar
