#!/bin/bash

set -e

APP_VERSION=2.4.0
APP_NATIVE=cocoa.macosx.x86_64
# APP_NATIVE=win32.win32.x86_64
APP_JAR=target/badpeggy-${APP_NATIVE}-${APP_VERSION}-jar-with-dependencies.jar

JDK_DIR=jre/jdk
JDK_NATIVE=mac_x64
# JDK_NATIVE=win_x64

JDK_VERSION_MAJOR=17
JDK_VERSION_FULL=${JDK_VERSION_MAJOR}.0.4
JDK_VERSION_RELEASE=8
JDK_VERSION_U=${JDK_VERSION_FULL}_${JDK_VERSION_RELEASE}
JDK_VERSION_P=${JDK_VERSION_FULL}+${JDK_VERSION_RELEASE}

JRE_DIR=jre/jlink
[[ "$JDK_NATIVE" == "mac_"* ]] && JRE_ARGS=-XstartOnFirstThread

jdk_acquire() {

    pushd .
    rm -rf   $JDK_DIR
    mkdir -p $JDK_DIR
    cd       $JDK_DIR

    ADP_URL_DOWNLOAD=https://github.com/adoptium/temurin17-binaries/releases/download/jdk-$JDK_VERSION_P

    curl $ADP_URL_DOWNLOAD/OpenJDK17U-jdk_x64_linux_hotspot_${JDK_VERSION_U}.tar.gz   -L -o lnx_x64.tgz
    curl $ADP_URL_DOWNLOAD/OpenJDK17U-jdk_x64_windows_hotspot_${JDK_VERSION_U}.zip    -L -o win_x64.zip
    curl $ADP_URL_DOWNLOAD/OpenJDK17U-jdk_x64_mac_hotspot_${JDK_VERSION_U}.tar.gz     -L -o mac_x64.tgz
    curl $ADP_URL_DOWNLOAD/OpenJDK17U-jdk_aarch64_mac_hotspot_${JDK_VERSION_U}.tar.gz -L -o mac_arm.tgz

    EXTRACT_DIR=jdk-${JDK_VERSION_P}

    unzip -qq win_x64.zip && mv ${EXTRACT_DIR} win_x64
    tar xzf   lnx_x64.tgz && mv ${EXTRACT_DIR} lnx_x64
    tar xzf   mac_x64.tgz && mv ${EXTRACT_DIR}/Contents/Home mac_x64 && rm -rf ${EXTRACT_DIR}
    tar xzf   mac_arm.tgz && mv ${EXTRACT_DIR}/Contents/Home mac_arm && rm -rf ${EXTRACT_DIR}
    rm *.tgz *.zip

    popd
}

do_jlink() {
    echo "determining dependencies ..."
    DEPS=$($JDK_DIR/$JDK_NATIVE/bin/jdeps --list-deps $APP_JAR | awk -vORS=, '{ print $1 }' | sed 's/,$//')
    echo $DEPS

    echo "running jlink ..."
    rm -rf ${JRE_DIR}

    JLINK_OPTS=( "--strip-debug" "--no-header-files" "--no-man-pages" "--add-modules" ${DEPS} )

    ${JDK_DIR}/${JDK_NATIVE}/bin/jlink ${JLINK_OPTS[@]} --module-path ${JDK_DIR}/win_x64/jmods --output ${JRE_DIR}/win_x64
    ${JDK_DIR}/${JDK_NATIVE}/bin/jlink ${JLINK_OPTS[@]} --module-path ${JDK_DIR}/lnx_x64/jmods --output ${JRE_DIR}/lnx_x64
    ${JDK_DIR}/${JDK_NATIVE}/bin/jlink ${JLINK_OPTS[@]} --module-path ${JDK_DIR}/mac_x64/jmods --output ${JRE_DIR}/mac_x64
    ${JDK_DIR}/${JDK_NATIVE}/bin/jlink ${JLINK_OPTS[@]} --module-path ${JDK_DIR}/mac_arm/jmods --output ${JRE_DIR}/mac_arm
}

run_local() {
    echo "running app w/ jlinked (native) JRE ..."
    ${JRE_DIR}/${JDK_NATIVE}/bin/java $JRE_ARGS -cp $APP_JAR com.coderslagoon.badpeggy.GUI
}

jdk_acquire
do_jlink
run_local
