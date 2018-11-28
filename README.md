![alt text](https://maxprograms.com/images/Red_squares.png "TMXValidator Icon")

## TMXValidator

Check the validity of your TMX documents in any platform with TMXValidator.

Source code of TMXValidator is available under [Eclipse Public License 1.0](https://www.eclipse.org/org/documents/epl-v10.html).

Most CAT (Computer Aided Translation) tools rely on TMX (Translation Memory eXchange) standard to exchange translation memory data. Unfortunately, some tools produce files that are not valid and others do not accept TMX documents that are correctly formatted.

TMXValidator checks your documents against TMX DTD and also verifies if they follow the requirements described in TMX specifications.

TMXValidator supports TMX versions 1.1 , 1.2 , 1.3 and 1.4

Source code of TMXValidator was published originally on SourceForge at http://sourceforge.net/p/tmxvalidator/code/ 

The original version of TMXValidator loaded the TMX file in memory for validation. Validation of very big TMX files was limited by the amount of available memory.

This version of TMXValidator does not need to load the complete file in memory and does not have size limitations.

### Build Requirements

- JDK 11 or newer.

### Building

- Checkout this repository.
- Point your JAVA_HOME variable to JDK 11
- Use `buid.bat` or `build.sh` to generate a binary distribution in `./tmxvalidator`

### Validating TMX files

You can use the library in your own Java code. Validation of TMX files is handled by the class `com.maxprograms.tmxvalidation.TMXValidator`.

If you use binaries from the command line, running `.\tmxvalidator.bat` or `./tmxvalidator.sh` without parameters displays help for TMX validation. 

```
Usage:

   tmxvalidator.bat [-help] [-version] -tmx tmxFile

Where:

   -help:       (optional) Display this help information and exit
   -version:    (optional) Display version & build information and exit
   -tmx:        TMX file to validate
```
### Graphical User Interface (GUI)

Installers that provide a graphical user interface (GUI) for TMXValidator are available at https://www.maxprograms.com/products/tmxvalidator.html 

![alt text](https://maxprograms.com/images/TMXValidatorUI.png "TMXValidator GUI")
