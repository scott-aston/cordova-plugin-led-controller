const C = window.cordova ? window.cordova : {
	exec: (...args) => { console.log( "Cordova is missing.", args ); }
}
// cordova.exec( onSuccess, onFail, "Java Class by Name", "arbitrary string", [ arg1, arg2, arg3, ... ] );
const setColor = (namedColor, resolve, reject) => { // use listColors to get a named color.
	C.exec( resolve, reject, "LEDController", "setColor", [ namedColor ] );
};
const getValue = (color, resolve, reject) => {
	// cannot acquire the color. cannot read the zigbee_reset file. it's always empty. maybe it's found in a different file.
}
const setValue = (toWhat, resolve, reject) => { // low level function to write arbitrary data, for tehLuLz.
	C.exec( resolve, reject, "LEDController", "setValue", [ toWhat ] );
};
const listColors = (resolve, reject) => { // the list of color commands are located in `COLOR_MAP` in the .java file.
	C.exec( resolve, reject, "LEDController", "listColors", [] );
};
const init = (resolve, reject) => { // makes the `/sys/devices/platform/lef_con_h/zigbee_reset` file writable using file-mode 666.
	C.exec( resolve, reject, "LEDController", "init", [] );
};

const LEDController = {
	setColor: setColor,
	setValue: setValue,
	listColors: listColors,
	init: init
};

module.exports = LEDController;

window.LEDController = LEDController;

document.addEventListener( "deviceready", () => {
	LEDController.init( () => {
		console.log( "LED Controller", "Ready to use." );
	}, (fail) => {
		console.log( "LED Controller", fail ); // couldn't chmod 666 the zigbee_reset file
	} );
} );
