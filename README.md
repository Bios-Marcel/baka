<img src="icon.ico" alt="stonks man" style="float: right">

A simple visualisation tool for personal finances.
Merge different bank accounts histories together, classify payments and do predictions.

## Concept

This application DOES NOT connect to your bank or your bank account in any way. No data is sent anywhere.
It uses Export-Formats offered by your bank. Currently, there are a couple of hard-coded ones that I am using for test
purposes. The plan is to have a configurable CSV importer, as most banks seem to offer CSV exporting.

Some banks, for example Revolut however, only offer a detailed export using PDF.
These can't be parsed and probably won't ever be supported. Just switch to a better bank ;)

## Requirements

* Windows 10+ (For now)
* Maven
* OpenJDK 22
* https://github.com/wixtoolset/wix3/releases

## Data Storage

For storage, we are using `Eclipse Store` (previously `MicroStream`). The data is stored in `$env.APPDATA`.

In order to delete the data, simply remove `$env.APPDATA/baka`.

## Installation

An installer can be built via maven:

```shell
./mvnw dependency:get -Dartifact="org.antlr:antlr4-runtime:4.13.1:jar"
./mvnw -f antlr.xml generate-resources
./mvnw compile package javafx:jlink jpackage:jpackage
```

On Windows, the installer is a little buggy, as automatic reinstallation does not work.
To update, a manual deinstallation is required before installation.
