#!/bin/bash

set -e

if [[ ! ("$#" -eq 1) ]]; then
    echo 'need version'
    exit 1
fi

BUILD=BadPeggy-$1
BUILDIR=tmp/$BUILD

echo "shipping Linux version ..."

BADPEGGYJAR=$(ls -1 target/badpeggy-gtk.linux.x86_64-*-jar-with-dependencies.jar)

mkdir -p ship
rm -f ship/badpeggy$1_linux.zip
rm -rf   $BUILDIR
mkdir -p $BUILDIR

cp -a $BADPEGGYJAR             $BUILDIR/badpeggy.jar
cp -a etc/badpeggy.desktop     $BUILDIR/
cp -a etc/scripts/badpeggy     $BUILDIR/
cp -a etc/scripts/install.sh   $BUILDIR/
cp -a etc/scripts/uninstall.sh $BUILDIR/
cp -a etc/images/badpeggy.png  $BUILDIR/

tr -d "\r" < LICENSE.txt      | cat > $BUILDIR/LICENSE
tr -d "\r" < etc/README.txt   | cat > $BUILDIR/README
tr -d "\r" < etc/LIESMICH.txt | cat > $BUILDIR/LIESMICH

chmod 755 $BUILDIR/badpeggy
chmod 755 $BUILDIR/install.sh
chmod 755 $BUILDIR/uninstall.sh

cp -a -R jre/jlink/lnx_x64 $BUILDIR/jre

mkdir -p ship
cd tmp
zip -9 -q -r -X ../ship/badpeggy-$1_linux.zip $BUILD
cd ..

echo "shipping Windows version ..."

BADPEGGYJAR=$(ls -1 target/badpeggy-win32.win32.x86_64-*-jar-with-dependencies.jar)

rm -f ship/badpeggy$1_windows.zip
rm -rf   $BUILDIR
mkdir -p $BUILDIR

cp -a $BADPEGGYJAR             $BUILDIR/badpeggy.jar
cp LICENSE.txt                 $BUILDIR/LICENSE.txt
cp etc/README.txt              $BUILDIR/
cp etc/LIESMICH.txt            $BUILDIR/
cp etc/scripts/badpeggy*.cmd   $BUILDIR/
cp etc/scripts/install.vbs     $BUILDIR/
cp etc/images/badpeggy.ico     $BUILDIR/

cp -a -R jre/jlink/win_x64 $BUILDIR/jre

cd tmp
zip -9 -q -r -X ../ship/badpeggy-$1_windows.zip $BUILD
cd ..

ls -lah ./ship/badpeggy-$1_linux.zip
ls -lah ./ship/badpeggy-$1_windows.zip
