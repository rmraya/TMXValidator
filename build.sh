#!/bin/bash
rm -rf tmxvalidator
jlink --module-path "lib:$JAVA_HOME/jmods" --add-modules tmxvalidator --output tmxvalidator
rm tmxvalidator/lib/jrt-fs.jar

cp tmxvalidator.sh tmxvalidator/
cp LICENSE tmxvalidator/

