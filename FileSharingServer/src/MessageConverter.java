import org.json.JSONException;
import org.json.JSONObject;


public class MessageConverter {
	/**
	 * Message Converter helps to convert the messages easily for UDP communication
	 */
	
	public static JSONObject bytesToJsonObject(byte[] bytes) throws JSONException{
		/**
		 * Convert bytes to JSON object
		 */
		return new JSONObject((new String(bytes)));
	}
	
	public static byte[] JsonObjectToBytes(JSONObject obj) throws JSONException{
		/**
		 * Convert JSON object to byes
		 */
		return obj.toString().getBytes();
	}
	
}
