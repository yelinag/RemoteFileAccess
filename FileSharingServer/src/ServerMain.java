import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerMain {
	
	static final int SOCKET_BUFFER_SIZE = 1000;
	static final int SOCKET_PORT = 6800;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		//to save all the registered client info with dynamic data structure
		List<RegisteredClient> registerClientArr = new ArrayList<RegisteredClient>(); 
		
		//Main UDP Socket 
		UDPServerSocket udpSocket = null;
		try {
			
			System.out.println("Server Loading");
			
			udpSocket = new UDPServerSocket(SOCKET_PORT, SOCKET_BUFFER_SIZE);
			
			//create new history keeper for the requests made by clients
			ReqHistoryKeeper historyKeeper = new ReqHistoryKeeper();
			while(true){
				try{
					//waiting for new requests, if no request comes in,
					//the server state will be stuck here until it terminates
					DatagramPacket request = udpSocket.waitForRequest();
					
					//get request data and covert the data type to json object type for easier access
					JSONObject requestJsonObj = MessageConverter.bytesToJsonObject(request.getData());
					
					//it will first check the request history kept by server
					//based on the information given by incoming request
					String successMsg;
					successMsg = historyKeeper.getRequestResults(requestJsonObj.getInt("requestId"), 
							request.getAddress().toString(), request.getPort());
					
					JSONObject returnJsonObj = new JSONObject();
					if(successMsg != null){
						//if the same request was found, return the saved result to
						//client for faster access and to achieve at-most-one invocation semantics
						System.out.println("Record Found");
						returnJsonObj = new JSONObject(successMsg);
					}else{
						//if no same request was found, compute the request accordingly
						if(requestJsonObj.get("option").equals(new Integer(1))){
							//read service based on the filename, offset and number of bytes
							//decoding all the information necessary
							String filename = requestJsonObj.getString("path");
							int offset = requestJsonObj.getInt("offset");
							int noofbytes = requestJsonObj.getInt("readBytes");
							
							//call the file read function to perform the read operation and
							//retrieve the result
							successMsg = FileController.readFile(filename, offset, noofbytes);
							
							//if success, it will use 'Success' word in the packet and
							//if error, it will use 'Error'
							returnJsonObj.put("Success", successMsg);
						}else if(requestJsonObj.get("option").equals(new Integer(2))){
							//write service based on filename, offset and write data
							String filename = requestJsonObj.getString("path");
							int offset = requestJsonObj.getInt("offset");
							String data = requestJsonObj.getString("writeLine");
							
							//call the write function to perform write operation
							FileController.writeFile(filename, offset, data);
							//if success, success message will be returned
							//else exception will be thrown and return error message
							successMsg = "Successfully inserted";
							returnJsonObj.put("Success", successMsg);
							
							//after performing write operation, the disk data has been altered,
							//therefore, it needs to send the notification to all the registered
							//clients for that filename
							sendNotificationToAllClients(registerClientArr, filename);
						}else if(requestJsonObj.get("option").equals(new Integer(3))){
							//Monitoring service, it will save the register client info
							//into an array
							String filename = requestJsonObj.getString("path");
							long interval = requestJsonObj.getLong("intervalLength");
							
							//it retrieves the requested data packet for client information
							DatagramPacket tempPacket = udpSocket.getRequestPacket();
							//add the client to the array for registered clients
							registerClientArr.add(new RegisteredClient(filename, interval, tempPacket.getAddress(), 
									tempPacket.getPort()));
							//it will return current content of the whole file to client
							//this return information depends on the purpose of the client application
							String filecontent = FileController.readWholeFile(filename);
							returnJsonObj.put("Success", filecontent);
						}else if(requestJsonObj.get("option").equals(new Integer(4))){
							/**
							 * List service returns the names of all the files and folders
							 * in the mentioned directory
							 */
							
							//retrieving all necessary information
							String filename = requestJsonObj.getString("path");
							File[] listofFiles = FileController.listDirectory(filename);
							
							//get the list of the names
							String[] filenameList = FileController.getFilenameList(listofFiles);
							
							//send the list back to client
							JSONArray jsonArray = new JSONArray(filenameList);
							returnJsonObj.put("Success", jsonArray);
						}else if(requestJsonObj.get("option").equals(new Integer(5))){
							/**
							 * Append service add the text at the end of the requested file
							 */
							String filename = requestJsonObj.getString("path");
							String appendData = requestJsonObj.getString("writeLine");
							
							//perform the append operation
							FileController.appendText(filename, appendData);
							
							//return success message if no error exception
							successMsg = "Successfully Appended";
							returnJsonObj.put("Success", successMsg);
						}else if(requestJsonObj.get("option").equals(new Integer(6))){
							/**
							 * Get the last modified update date/time of a file in a disk
							 */
							String filename = requestJsonObj.getString("path");
							
							//get the last modified date and time as long data type 
							long modifiedDate = FileController.getLatestModifiedDate(filename);
							returnJsonObj.put("Success", modifiedDate);
						}else{
							returnJsonObj.put("Error", "Invalid Option");
						}
						
						//if success, add the current result to the history stack for
						//future references
						historyKeeper.addHistory(requestJsonObj.getInt("requestId"), 
								request.getAddress().toString(), request.getPort(), 
								returnJsonObj.toString());
					}
					
					//reply the result back to client using udp socket
					udpSocket.sendReplyToRequest(returnJsonObj);
					
					
				
				//handle exceptions
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					udpSocket.sendErrorMsg("Invalid Parameter Values");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					udpSocket.sendErrorMsg("Unsupported Encoding texts");
				} catch(IllegalArgumentException e){
					//e.printStackTrace();	
					udpSocket.sendErrorMsg("Invalid offset value");
				} catch(FileNotFoundException e){
					//e.printStackTrace();
					if(e.getMessage() == null)
						udpSocket.sendErrorMsg("Invalid file input");
					else
						udpSocket.sendErrorMsg(e.getMessage());
				} catch(IndexOutOfFileException e){
					//e.printStackTrace();
					udpSocket.sendErrorMsg("Index input larger than file size");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					udpSocket.sendErrorMsg("I/O Exception Error");
				} 
				
			}
			
			
		} catch (SocketException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} finally{ udpSocket.closeSocket();}
		
	}
	
	
	
	static void sendNotificationToAllClients(List<RegisteredClient> registerClientArr, String filename) 
			throws FileNotFoundException, IndexOutOfFileException, IOException, JSONException{
		/**
		 * This function will find all the clients that has been registered to
		 * the mentioned file path/name. And then send the notification to the client
		 * when called
		 */
		
		//get the whole file content
		//again, this content depends on the client application purposes
		//in this case, assume that it will just return all content values
		String filecontent = FileController.readWholeFile(filename);
		
		//create new JSON object for notification message
		JSONObject notiObj = new JSONObject();
		notiObj.put("Notification", filecontent);
		System.out.println("Noti Size: " + registerClientArr.size());
		for(int i=0; i<registerClientArr.size(); i++){
			//check if the client is still monitoring based on the start time
			//,current time and interval
			if(registerClientArr.get(i).isClientMonitoring()){
				if(registerClientArr.get(i).isSameFile(filename)){
					//check if the filename is the same, if yes,
					//it will send the notification
					//else do nothing
					UDPServerSocket notiSocket = new UDPServerSocket(SOCKET_BUFFER_SIZE);
					notiSocket.sendToMornitoringClient(notiObj, registerClientArr.get(i).getIpAddress(),
							registerClientArr.get(i).getPort());
				}
			}else{
				//if client is not monitoring, it will be removed from the array
				registerClientArr.remove(i);
				i--;
			}
		}
		
	}

}
