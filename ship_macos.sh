#!/bin/bash

set -e

if [[ ! ("$#" -eq 1) ]]; then
    echo 'need version'
    exit 1
fi

BADPEGGYJAR=$(ls -1 target/badpeggy.cocoa.macosx.x86_64-*-jar-with-dependencies.jar)

BUILD=BadPeggy-$1
BUILDIR=tmp/$BUILD

mkdir -p ship
rm -f ship/badpeggy$1_macos_*.zip
rm -rf   $BUILDIR
mkdir -p $BUILDIR

DEPLOYDIR=tmp/deploy
APPDIR_TMP=$DEPLOYDIR/__tmp__.app
APPDIR=$DEPLOYDIR/Bad\ Peggy.app
CONTDIR_TMP=$APPDIR_TMP/Contents
CONTDIR=$APPDIR/Contents

rm -rf $DEPLOYDIR

mkdir -p $CONTDIR_TMP/MacOS
mkdir    $CONTDIR_TMP/Resources

cp -a $BADPEGGYJAR               $CONTDIR_TMP/MacOS/badpeggy.jar
cp -a etc/scripts/badpeggy_macos $CONTDIR_TMP/MacOS/badpeggy
cp -a etc/Info.plist             $CONTDIR_TMP/
cp -a etc/images/badpeggy.icns   $CONTDIR_TMP/Resources/

tr -d "\r" < LICENSE.txt | cat > $CONTDIR_TMP/MacOS/LICENSE

chmod 755 $CONTDIR_TMP/MacOS/badpeggy

cp -a -R jre/jlink/mac_x64 $CONTDIR_TMP/MacOS/jre

mv $APPDIR_TMP "$APPDIR"
ln -s /Applications $DEPLOYDIR/Applications

DMGFILE=ship/badpeggy-$1_macos-intel.dmg
rm -f $DMGFILE

hdiutil create -volname "Bad Peggy" -srcfolder $DEPLOYDIR $DMGFILE

BADPEGGYJAR=$(ls -1 target/badpeggy-*-cocoa.macosx.aarch64-jar-with-dependencies.jar)
rm -f "$CONTDIR/MacOS/badpeggy.jar"
cp -a $BADPEGGYJAR "$CONTDIR/MacOS//badpeggy.jar"
DMGFILE=ship/badpeggy-$1_macos-arm.dmg
rm -f $DMGFILE

hdiutil create -volname "Bad Peggy" -srcfolder $DEPLOYDIR $DMGFILE

rm -rf $DEPLOYDIR

ls -lah ship/badpeggy-$1*.dmg
