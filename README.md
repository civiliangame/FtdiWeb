#FtdiWeb

Interfacing an Android tablet with the OpenMSP430 chip.

#Getting Started

1. Download Android Studio: https://developer.android.com/sdk/index.html
2. Download Github GUI: Google "Download Github GUI" and follow the instructions for your respective platform
3. Click "Clone in Desktop" to the right of this page, and remember where the project is located. If you forget, right-click the project's item in the left column of the Github GUI and an option will appear to help you find it
4. Open Android Studio, click "Import Project (Eclipse ADT, Gradle, etc.)" and double-click on the top level directory (the one that contains the "app", "build" and "gradle" folders)
5. Check to make sure you're using the correct build tools version. Right click "app", choose "Open Module Settings" and change "Build Tools Version" to 22.0.1.
6. There may be some issues after this, but I'm not sure. Many are solved by choosing "Build > Clean Project", including (usually) when the "R" shows up red.
7. A final error might occur if you don't have the FTDI library imported. Right click "app", then go to "Dependencies" and click the "+" button. Select "File dependency" and add all three ".jar" files in the "libs" directory. I don't think this should be an issue, but it might be.

#Resources

- Documentation for the OpenMSP430: http://opencores.org/project,openmsp430,overview (in particular, see "Serial Debug Interface")
- FTDI's Android support page: http://www.ftdichip.com/Android.htm
- Download FTDI drivers: http://www.ftdichip.com/Drivers/D2XX.htm
