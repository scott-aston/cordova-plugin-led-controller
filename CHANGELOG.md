# Changelog

## [1.0.0] - 2024-05-11
- Made a new Cordova Plugin.
- Created the led-controller.js which defines how the JavaScript side ought to run commands to change the LED color.
- Created the LEDController java class, added all the stub functions I want to have it preform. 
- Updated the plugin.xml, package.json, and this CHANGELOG.md file.
- TODO: Discover how to read what color or value is currently in use.

## [1.0.1] - 2024-05-12
- Updated LEDController.java to use `Map<String, String>` for it's list of colors instead of a switch-case.
- Added a lot more logging. Moved some things to a `logFilePermissions` to see a "before and after" for changing the file-mode to 666 on the zigbee_reset file.
- Changed the `INIT` action to collect what standard-error might print out, using `.redirectErrorStream( true )`.
- TODO: Discover how to acquire what was the last value sent through zigbee_reset. I'm starting to think it's not possible.

## [1.0.2] - 2024-05-18
- Changed the names of the colors to closer match HTML basic color names. Yellow and Orange each had two colors mapping to one HTML color name, so I labeled them a "darker" variant.
