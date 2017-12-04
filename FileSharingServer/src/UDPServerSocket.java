import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.json.JSONException;
import org.json.JSONObject;


public class UDPServerSocket {
	/**
	 * This class helps to make the UDP connection
	 * with the client and send data accordingly
	 */
	
	//timeout interval can be different based on the network
	//latency and traffic.
	public static int TIMEOUT_INTERVAL = 2000;
	
	byte[] buffer = null;
	DatagramSocket aSocket = null;
	DatagramPacket request = null;
	
	public UDPServerSocket(int bufferSize) throws SocketException{
		aSocket = new DatagramSocket();//create new socket with available port number
		buffer = new byte[bufferSize];//create new buffer for data transfer
		request = new DatagramPacket(buffer, buffer.length);//create new request based on buffer and length
	}
	
	public UDPServerSocket(int socketPort, int bufferSize) throws SocketException{
		aSocket = new DatagramSocket(socketPort);//create new socket with identified port number
		buffer = new byte[bufferSize];
		request = new DatagramPacket(buffer, buffer.length);
	}
	
	public void closeSocket(){
		//close/terminate the socket
		if (aSocket != null) aSocket.close(); 
	}
	
	public DatagramPacket getRequestPacket(){
		//get the request packet and its info if not null
		if(request != null)
			return request;
		return null;
	}
	
	public DatagramPacket waitForRequest() throws IOException, JSONException{
		/**
		 * Wait for the client request
		 */
		System.out.println("Waiting for requests");
		aSocket.receive(request); //blocked if no input
		
		//perform the unmarshalling for request message in this step / bytes --> JSON in this case
		JSONObject requestJsonObj = MessageConverter.bytesToJsonObject(request.getData());
		System.out.println("Request: " + requestJsonObj.toString());
		return request;
	}

	public void sendReplyToRequest(JSONObject returnJsonObj) throws JSONException, IOException{
		//perform the marshalling in this step / JSON --> bytes in this case
		byte[] returnBytes = MessageConverter.JsonObjectToBytes(returnJsonObj);
		
		//send the reply message to requested client
		DatagramPacket reply = new DatagramPacket( returnBytes, returnBytes.length, request.getAddress(), request.getPort());
		//to reply, send back to the same ip address and port number
		aSocket.send(reply);
	}
	
	
	public void sendErrorMsg(String errorMsg){
		/**
		 * Send back the error message to requested client
		 */
		try {
			//data name differs if error message
			JSONObject errorObj = new JSONObject();
			errorObj.put("Error", errorMsg);
			
			//marshalling
			byte[] errorMessage = MessageConverter.JsonObjectToBytes(errorObj);
			
			DatagramPacket reply = new DatagramPacket( errorMessage, errorMessage.length, request.getAddress(), request.getPort());
			//to reply, send back to the same ip address and port
			aSocket.send(reply);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void sendToMornitoringClient(JSONObject jsonMessage, InetAddress clientAddress, int clientPort){
		/**
		 * Send the data content to monitoring client
		 */
		try {
			DatagramSocket aSocket = new DatagramSocket(); //use a free local port
			aSocket.setSoTimeout(TIMEOUT_INTERVAL);
			
			//marshalling
			byte[] message = MessageConverter.JsonObjectToBytes(jsonMessage);
			
			DatagramPacket packet = new DatagramPacket( message, message.length, clientAddress, clientPort);
			
			boolean receivedReply = false;
			int retryCount = 0;
			
			//it will keep sending if reply is not received within
			//timeout interval. Retry count is to avoid infinite try.
			//For example, client can just terminate the program,
			//hence no reply will be sent. Retry count is to avoid sending
			//infinitely to the client.
			while(!receivedReply && retryCount < 3){
				try{
					//send the packet
					aSocket.send(packet);
					byte[] buffer = new byte[ServerMain.SOCKET_BUFFER_SIZE]; //a buffer for receive 
					DatagramPacket reply = new DatagramPacket(buffer, buffer.length); //a different constructor
					aSocket.receive(reply);
					//unmarshalling
					JSONObject replyJsonObj = MessageConverter.bytesToJsonObject(reply.getData());
					
					//if received, set recievedReply to true to stop sending
					if(replyJsonObj.has("Received"))
						receivedReply = true;
					retryCount++;
				}catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			aSocket.close();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	 
	
	
}
