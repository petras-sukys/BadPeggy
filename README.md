# Bad Peggy

Bad Peggy scans JPEG and other image formats for damage and other blemishes and shows
the results and images instantly. It allows you to find such broken files quickly,
inspect and then either delete or move them to a different location.

Implemented in Java 17 and SWT. Runs on Windows, MacOS and Linux.

## Development

Everything is managed by Maven, to build things in one step simply type
```
./build.sh
```
Under Windows you may need to install MSYS to run such shell commands.

The preferred development environment is Visual Studio Code - it automatically does
the Maven build in the background on every code change. There also are launchers to
either just run/debug the code or the fully packaged JAR files, as well as tasks to
do the building processes.

To run the tests (this can take a while):
```
mvn test
```

Since SWT drives the UI you need to adjust the _.mvn/local-settings.xml_ file
to declare the platform you're working on.

Sometimes thing might get stuck. In such a case try to
* remove the _./target_ and _./ship_ folders
* delete the _~/.swt_ folder in your home directory
* under _Java Projects_ (same place than the folder view, on the bottom)
  click _[...]_ and then _Clean Workspace_

## Shipping

Set the latest version in a variety of places and rebuild:
```
./version.sh 1.2.3
./build.sh
```

Acquire and prepare the JRE binaries:
```
./prepare_jre.sh
```
This downloads all JREs for all the supported platforms and also optimizes them
for shipping.

Then run the shell scripts with that version number of the release:
```
./ship.sh 1.2.3
./ship_macos.sh 1.2.3
```
The final packaged material can then be found in the _./ship_ folder.
