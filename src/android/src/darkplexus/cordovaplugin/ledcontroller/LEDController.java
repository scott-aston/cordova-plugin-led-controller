package darkplexus.cordovaplugin.ledcontroller;
// this needs to be compliant with JDK 1.8, meaning Java 8. it cannot use AndroidX.
// the reasoning behind this is because of the Cordova app this is being used on requires Java 8.
// otherwise you could switch it to a higher JDK version as needed and update any function call return-types as needed, too.

// see: https://developer.android.com/reference
// used SDK 30 for Android 11.
import android.util.Log;

// see: https://docs.oracle.com/javase/8/docs/api/overview-summary.html
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// see: https://github.com/apache/cordova-android/tree/master/framework/src/org/apache/cordova
// you need the 8.1.x branch, because 9+ started to use AndroidX
// ln -s /path/to/cordova-android/framework/src/org/apache/cordova /path/to/<project>/src/android/org/apache/
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

// see: https://github.com/stleary/JSON-java/tree/master/src/main/java/org/json
// ln -s /path/to/JSON-java/src/main/java/org/json /path/to/<project>/src/android/org/
import org.json.JSONArray;
import org.json.JSONException;
// ===== //

public class LEDController extends CordovaPlugin {
	private static final String SET_COLOR = "setColor";
	private static final String LIST_COLORS = "listColors";
	private static final String INIT = "init";
	private static final String ZIGBEE_RESET = "/sys/devices/platform/led_con_h/zigbee_reset";
	private static final String TAG = "LEDController";
	private static final Map<String, String> COLOR_MAP;


	static {
		COLOR_MAP = new HashMap<>(); // TODO: create better names, group up the colors names by color channel, then organize them by color shade from dark to light.
		// 0x00 was the "brighter" button, which seems to have no effect.
		// 0x01 was the "darker", which seems to have no effect.
		// 0x02 was the "off" button, which did turn off the LED bar.
		// 0x03 was the "on" button, which did not not restore any color at all or have any effect.
		COLOR_MAP.put( "off", "0x02" );
		COLOR_MAP.put( "red", "0x04" );
		COLOR_MAP.put( "green", "0x05" );
		COLOR_MAP.put( "blue", "0x06" );
		COLOR_MAP.put( "white", "0x07" );
		COLOR_MAP.put( "orange", "0x08" );
		COLOR_MAP.put( "aquamarine", "0x09" );
		COLOR_MAP.put( "purple", "0x0A" );
		COLOR_MAP.put( "cycle_fast", "0x0B" );
		COLOR_MAP.put( "gold", "0x0C" );
		COLOR_MAP.put( "turquoise", "0x0D" );
		COLOR_MAP.put( "neon_purple", "0x0E" );
		COLOR_MAP.put( "white_pulse", "0x0F" );
		COLOR_MAP.put( "yellow", "0x10" );
		COLOR_MAP.put( "neon_blue", "0x11" );
		COLOR_MAP.put( "violet", "0x12" );
		COLOR_MAP.put( "cycle_fade", "0x13" );
		COLOR_MAP.put( "lemon", "0x14" );
		COLOR_MAP.put( "arctic_blue", "0x15" );
		COLOR_MAP.put( "magenta", "0x16" );
		COLOR_MAP.put( "cycle_rgb", "0x17" );
		// 0x18 through 0xFF seem to have no effect.
	}

	private String writeToLED( String value ) {
		try ( FileWriter writer = new FileWriter( ZIGBEE_RESET ) ) { // requires the file-mode to be in 666: rw-rw-rw
			// trying to `echo "w 0x05" > /sys/devices/platform/led_con_h/zigbee_reset` results in an error, stating not allowed to create the file. (because it was file-mode 664)
			writer.write( "w " + value );
			writer.flush();
			return null;
		} catch ( IOException e ) {
			return e.getMessage();
		}
	}

	private static String transformerColorCode( String colorText ) {
		return COLOR_MAP.get( colorText.toLowerCase() ); // returns NULL if the key is missing.
		/*
		switch ( colorText.toLowerCase() ) {
			// 0x00 is a Plus button -- seems to have no effect?
			// 0x01 is a Minus button -- has no effect?
			case "off": {
				return "0x02";
			}
			// 0x02 is a power button -- has no effect?
			case "red": {
				return "0x04";
			}
			case "green": {
				return "0x05";
			}
			case "blue": {
				return "0x06";
			}
			case "white": {
				return "0x07";
			}
			case "orange": {
				return "0x08";
			}
			case "aquamarine": { // 0x09 and 0x0D are very similar, but this one is a bit brighter, and green-er.
				return "0x09";
			}
			case "purple": { // a bit darker than 0x0E...
				return "0x0A"; // violet is darker than what im seeing. this is like, neon purple.
			}
			case "cycle_fast": { // cycles [red, orange, yellow, green, aquamarine, blue, purple]
				return "0x0B";
			}
			case "gold": { // darker than 0x10, but lighter than
				return "0x0C";
			}
			case "turquoise": { // very similar to 0x09, but this one is a bit darker, and blue-er
				return "0x0D";
			}
			case "neon_purple": { // brighter than 0x0A,
				return "0x0E";
			}
			case "white_pulse": {
				return "0x0F";
			}
			case "yellow": {
				return "0x10";
			}
			case "neon_blue": { // more plain blue than 0x0D
				return "0x11";
			}
			case "violet": { // a tad darker than 0x0E
				return "0x12";
			}
			case "cycle_fade": { // cycles [red, gold, yellow, green?, cyan?, blue, purple?]
				return "0x13";
			}
			case "lemon": { // brighter than 0x10
				return "0x14";
			}
			case "arctic_blue": { // a bit darker than 0x11
				return "0x15";
			}
			case "magenta": { // a bit brighter than 0x12, with a bit of red.
				return "0x16";
			}
			case "cycle_rgb": { // cycles [red, green, blue]
				return "0x17";
			}
			// values 0x18 through 0xFF seem to have no effect.
		}
		return null;
		*/
	}

	@Override
	public boolean execute( String action, JSONArray rawArgs, CallbackContext cbc ) throws JSONException {
		if ( action.equals( INIT ) ) {
			boolean ok = false;
			logFilePermissions(); // get a "before" snapshot.
			try {
				String command = "su root chmod 666 " + ZIGBEE_RESET + ";";
				Process process = new ProcessBuilder()
					.command( "sh", "-c", command )
					.redirectErrorStream( true ) // try to obtain info when things go wrong. same as: `/path/to/command &2 > 1`
					.start();
				BufferedReader reader = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
				StringBuilder output = new StringBuilder();
				String line;
				while ( (line = reader.readLine()) != null ) {
					output.append( line ).append( "\n" );
				}
				int exitCode = process.waitFor();
				Log.d( TAG, "exit code " + exitCode + " for command: chmod 666 " + ZIGBEE_RESET );
				Log.d( TAG, "su -c chmod 666 output:" + output.toString() );
				ok = true;
			} catch ( IOException | InterruptedException e ) {
				Log.e( TAG, "Could not change the file's permissions", e );
			}
			logFilePermissions(); // get an "after" snapshot.
			if ( ok ) {
				cbc.success();
			} else {
				cbc.error( "Could not change the file's permission to 666 on " + ZIGBEE_RESET );
			}
		} else if ( action.equalsIgnoreCase( SET_COLOR ) ) {
			if ( rawArgs.length() > 0 ) {
				String colorKey = transformerColorCode( rawArgs.getString( 0 ) );
				if ( colorKey != null ) {
					String error = writeToLED( colorKey );
					if ( error == null ) {
						cbc.success();
					} else {
						cbc.error( "Could not set a color. Java exception:" + error );
					}
				} else {
					cbc.error( "Invalid color code. Use listColors to get a list of valid codes." );
				}
			} else {
				cbc.error( "Missing required value. You didn't supply a color code. Use listColors to get a list of valid codes." );
			}
		} else if ( action.equalsIgnoreCase( LIST_COLORS ) ) {
			cbc.success( Arrays.toString( COLOR_MAP.keySet().toArray() ) );
		} else { // else function not found
			return false; // if you return false, it throws a function not found error.
		}
		return true;
	}

	private void logFilePermissions() {
		try {
			File file = new File( ZIGBEE_RESET );
			if ( file.exists() ) {
				String command = "ls -l " + file.getAbsolutePath();
				Process checkProcess = Runtime.getRuntime().exec( command );
				BufferedReader reader = new BufferedReader( new InputStreamReader( checkProcess.getInputStream() ) );
				String line;
				while ( (line = reader.readLine()) != null ) {
					Log.d( TAG, "File permissions: " + line );
				}
				checkProcess.waitFor();
			} else {
				Log.e( TAG, "File does not exist: " + file.getAbsolutePath() );
			}
		} catch ( IOException | InterruptedException e ) {
			Log.e( TAG, "Error reading file permissions", e );
		}
	}
}
