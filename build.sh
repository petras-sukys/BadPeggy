#!/bin/bash

set -e

rm -rf target

swtoss=( \
    cocoa.macosx.aarch64 \
    cocoa.macosx.x86_64 \
    gtk.linux.x86_64 \
    win32.win32.x86_64 \
)

for swtos in "${swtoss[@]}"
do
    mvn package -Dmaven.test.skip=true -Dswt.os=$swtos
    mvn install -Dmaven.test.skip=true -Dswt.os=$swtos
done
