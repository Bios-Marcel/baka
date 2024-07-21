<img src="icon.ico" alt="stonks man" style="float: right">

A simple visualisation tool for personal finances.
Merge different bank accounts histories together, classify payments and do predictions.

## Concept

This application DOES NOT connect to your bank or your bank account in any way. No data is sent anywhere.
It uses Export-Formats offered by your bank. Currently, there are a couple of hard-coded ones that I am using for test
purposes. The plan is to have a configurable CSV importer, as most banks seem to offer CSV exporting.

## Data Storage

For storage, we are using `Eclipse Store` (previously `MicroStream`). The data is stored in `$env.APPDATA`.

In order to delete the data, simply remove `$env.APPDATA/baka`.

## Installation

### Windows

An installer can be built via maven:

```shell
./mvnw -f antlr.xml dependency:get -Dartifact="org.antlr:antlr4-runtime:4.13.1:jar"
./mvnw -f antlr.xml generate-resources
./mvnw compile package javafx:jlink jpackage:jpackage
```

Updating an existing installation does not work correctly as of now.
To update, a manual uninstallation is required before installation.

Pre-built installers are also available as build artifacts.

### Other systems

TODO

## Development

### Requirements

* Required
    * Maven
    * OpenJDK 22
* Optional
    * Building a windows installer: https://github.com/wixtoolset/wix3/releases

### Setup

In order to get all dependencies and generated sources, run:

```shell
./mvnw -f antlr.xml dependency:get -Dartifact="org.antlr:antlr4-runtime:4.13.1:jar"
./mvnw -f antlr.xml generate-resources
./mvnw compile
```

If you are using an IDE (such as IntelliJ), regenerate required IDE resources and reload the project.

### Debug Mode

The debug mode offers additional (potentially dangerous) features. It can be toggled via `Ctrl + Shift + D`.