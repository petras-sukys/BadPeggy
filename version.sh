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

FNAME=src/main/java/com/coderslagoon/badpeggy/GUI.java
perl -i -0 -pe "s/(final\ static\ String\ VERSION\ = ).*/\${1}\"$VERSION\";/g" $FNAME

FNAME=MANIFEST.MF
perl -i -0 -pe "s/(Specification-Version: *).*/\${1}$VERSION/g" $FNAME
perl -i -0 -pe "s/(Implementation-Version: *).*/\${1}$VERSION/g" $FNAME

FNAME=etc/Info.plist
perl -i -0 -pe "s/(<key>CFBundleShortVersionString<\/key>\s*<string>).*(<\/string>)/\${1}$VERSION\${2}/g" $FNAME
perl -i -0 -pe "s/(<key>CFBundleVersion<\/key>\s*<string>).*(<\/string>)/\${1}$VERSION\${2}/g" $FNAME

FNAME=pom.xml
perl -i -0 -pe "s/($PRODUCT\.\\$\{swt\.os\}<\/artifactId>\s*<version>).*(<\/version>)/\${1}$VERSION\${2}/g" $FNAME
