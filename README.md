![alt text](https://maxprograms.com/images/Red_squares.png "TMXValidator Icon")

## TMXValidator

Check the validity of your TMX documents in any platform with TMXValidator.

Most CAT (Computer Aided Translation) tools rely on TMX (Translation Memory eXchange) standard to exchange translation memory data. Unfortunately, some tools produce files that are not valid and others do not accept TMX documents that are correctly formatted.

TMXValidator checks your documents against TMX DTD and also verifies if they follow the requirements described in TMX specifications.

TMXValidator supports TMX versions 1.1 , 1.2 , 1.3 and 1.4

Source code of TMXValidator was published originally on SourceForge at http://sourceforge.net/p/tmxvalidator/code/ 

The original version of TMXValidator loaded the TMX file in memory for validation. Validation of very big TMX files was limited by the amount of available memory.

This version of TMXValidator does not need to load the complete file in memory and does not have size limitations.

### Releases

Version | Comment | Release Date
--------|---------|-------------
2.3.0 | Updated code and libraries | February 17, 2022
2.2.0 | Updated libraries and TypeScript code | January 2, 2021
2.1.0 | Added UI written in TypeScript and improved validation | February 5, 2020
2.0.2 | Switched to ant for building and updated OpenXLIFF| August 8, 2019
2.0.1 | Fixed date validation and updated libraries | June 24, 2019
2.0.0 | New version that supports validation of very large files | November 28, 2018

Ready to use installers are available at https://www.maxprograms.com/products/tmxvalidator.html 

## Requirements

- JDK 17 or newer is required for compiling and building. Get it from [Adoptium](https://adoptium.net/).
- Apache Ant 1.10.12 or newer. Get it from [https://ant.apache.org/](https://ant.apache.org/)
- Node.js 16.14.0 LTS or newer. Get it from [https://nodejs.org/](https://nodejs.org/)
- TypeScript 4.5.5 or newer. Get it from [https://www.typescriptlang.org/](https://www.typescriptlang.org/)

## Building

- Checkout this repository.
- Point your `JAVA_HOME` environment variable to JDK 17
- Run `ant` to compile the Java code
- Run `npm install` to download and install NodeJS dependencies
- Run `npm start` to launch TMXValidator

``` bash
  git clone https://github.com/rmraya/TMXValidator.git
  cd TMXValidator
  ant
  npm install
  npm start
```
Compile once and then simply run `npm start` to start TMXValidator
