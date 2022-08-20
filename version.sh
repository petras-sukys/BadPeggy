#!/bin/bash
set -e
if [ $# -eq 0 ]
then
  echo "usage: $0 version"
  echo "example: $0 1.2.3"
  exit 1
fi

PRODUCT=badpeggy
VERSION=$1

FNAME=pom.xml
perl -i -0 -pe "s/(<artifactId>badpeggy<\/artifactId>\s*<version>).*(<\/version>)/\${1}$VERSION\${2}/g" $FNAME

FNAME=src/main/java/com/coderslagoon/badpeggy/GUI.java
perl -i -0 -pe "s/(final\ static\ String\ VERSION\ = ).*/\${1}\"$VERSION\";/g" $FNAME

FNAME=etc/Info.plist
perl -i -0 -pe "s/(<key>CFBundleShortVersionString<\/key>\s*<string>).*(<\/string>)/\${1}$VERSION\${2}/g" $FNAME
perl -i -0 -pe "s/(<key>CFBundleVersion<\/key>\s*<string>).*(<\/string>)/\${1}$VERSION\${2}/g" $FNAME

FNAME=prepare_jre.sh
perl -i -0 -pe "s/(APP_VERSION=).*/\${1}$VERSION/g" $FNAME
