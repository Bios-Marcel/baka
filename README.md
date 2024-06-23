A simple visualisation tool for personal finances.
Merge different bank accounts histories together, classify payments and do predictions.

## Requirements

* Maven
* OpenJDK 22
* https://github.com/wixtoolset/wix3/releases

## Data Storage

Currently this is hardcoded for Windows only and uses `$env.APPDATA`.

Simply delete the folder to reset data.

## Installation

An installer can be built via maven:

```shell
mvnw compile javafx:jlink jpackage:jpackage
```

On windows, the installer is a little buggy, as automatic reinstallation does not work.
To update, a manual uninstall is required before installation.
