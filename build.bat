rmdir /S /Q .\tmxvalidator\
jlink --module-path "lib;%JAVA_HOME%\jmods" --add-modules tmxvalidator --output tmxvalidator
del .\tmxvalidator\lib\jrt-fs.jar

xcopy tmxvalidator.bat tmxvalidator\
xcopy LICENSE tmxvalidator\


