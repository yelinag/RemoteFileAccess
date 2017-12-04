import org.json.JSONException;
import org.json.JSONObject;

public class MessageConverter {
	/**
	 * This class is for marshalling and unmarshalling of data to send or to receiving across remote network
	 * JSON object is used
	 */
	public static JSONObject bytesToJsonObject(byte[] bytes) throws JSONException{
	//method to convert data in bytes to string and in turns, to JSONObject 
		return new JSONObject((new String(bytes)));
	}
	
	public static byte[] JsonObjectToBytes(JSONObject obj) throws JSONException{
	//method to convert JSONObject to string and in turns, to  bytes 
		return obj.toString().getBytes();
	}
}
