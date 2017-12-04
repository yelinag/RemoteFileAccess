import java.net.*;
import java.util.Date;
import java.io.*;
import org.json.*;

public class UDPClientSocket 
{
	/**
	 * This class helps to make the UDP connection
	 * with the server and send data accordingly
	 */
	//Longest message size allowed
	public static final int BUFFER_SIZE = 1000; 
	
	//timeout interval can be different based on the network
	//latency and traffic.
	public static final int REQUEST_TIMEOUT_INTERVAL = 2000; 
	
	private static DatagramSocket socket = null;
	private DatagramPacket packet = null;
	private InetAddress serverIPAddress =  null;// translate user-specified hostname to Internet address
	private int serverPortNo;
	private byte[] send_data;
	private int requestId;
	
	public UDPClientSocket(String serverIpAddress, int serverPort) throws SocketException, UnknownHostException{
		socket = new DatagramSocket();//create new socket with available port number
		send_data = new byte[BUFFER_SIZE];//create new buffer for data transfer
		this.serverIPAddress = InetAddress.getByName(serverIpAddress); //convert IP Adddress in string to internet address
		this.serverPortNo = serverPort;
		this.requestId = 0; 
	}
	
	public JSONObject sendRequest(JSONObject msg) throws IOException, JSONException {
	//methodd to send request message from client to server
	//and to receive reply message from server to client
		
		requestId++;
		msg.put("requestId", this.requestId);
		
		//perform marshalling for request message
		//to get a buffer for sending
		send_data = MessageConverter.JsonObjectToBytes(msg); 
		//create new socket with message in bytes, with available serverIPAddress and port number
        packet = new DatagramPacket(send_data, send_data.length, serverIPAddress, serverPortNo);
        
        boolean receivedReply = false;
        JSONObject resultObj = null;
		while(!receivedReply){ //loop until a correct reply is received
			try{
				socket.send(packet); //send the datagram packet to server 
				socket.setSoTimeout(REQUEST_TIMEOUT_INTERVAL);
				byte[] buffer = new byte[BUFFER_SIZE]; //a buffer for receive 
				DatagramPacket reply = new DatagramPacket(buffer, buffer.length); //a datagram packet for receive
				socket.receive(reply); //receive reply from server
				
				//perform the unmarshalling for reply message 
				resultObj =  MessageConverter.bytesToJsonObject(reply.getData());
				if(resultObj.has("Success") || resultObj.has("Error")){
					receivedReply = true;
				}
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Timeout occurs, resending request...");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
       return resultObj;
       
	}
	
	public void closeSocket(){
	//method to close/terminate the socket
		socket.close();
	}
	
	public void monitorUpdateFromServer(JSONObject msgObj, CallBack callback)
			throws JSONException, IOException{
	//method to receive updates from server using CallBack interface
		
		int mInterval = msgObj.getInt("intervalLength");//time interval to monitor the file
		Date startTime = new Date();
		while(new Date().getTime() - startTime.getTime() < mInterval){ //time is not up yet
			try{
				byte[] buffer = new byte[BUFFER_SIZE]; //a buffer for receive 
				DatagramPacket noti = new DatagramPacket(buffer, buffer.length); //a datagram packet for receive
				int timeLeft = (int)(mInterval - new Date().getTime() + startTime.getTime()); //calculate time left
				socket.setSoTimeout(timeLeft); //socket time out after the interval is over (if it is not over yet, the method will proceed)
				socket.receive(noti); //receive message from server after time out occurs
				
				//send reply when client has received an update
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Received", "Received updated text");
				byte[] temp_send_data = MessageConverter.JsonObjectToBytes(jsonObject); // a buffer for sending
				DatagramPacket reply = new DatagramPacket(temp_send_data, temp_send_data.length, noti.getAddress(), noti.getPort());//a datagram packet for reply
				socket.send(reply);  
				
				callback.methodToPrint(new String(noti.getData()));
			}catch (SocketTimeoutException e) {
				System.out.println("Interval time's up. Monitoring ending.");
			}
			
		}
	}
}
