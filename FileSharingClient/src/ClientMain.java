import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.*;

public class ClientMain {
	/**
	 * This class is the main class for client-side interface
	 * This class allows users to perform Services below
	 * 	1. Read a file.
		2. Write to a file.
		3. Register file monitoring service.
		4. List directory.
		5. Append a file.
	 * This class uses UDPClientSocket, CacheFile and MessageConverter classes
	 */
	static List<CacheFile> cacheFileList;
	static UDPClientSocket requestSocket;

	public static void main(String args[]) {
	
		Scanner sc = new Scanner(System.in);
		int option;
		int suboption;
	
		try {
			//server address and port number are specified as arguments
			String serverIPAddress = args[0]; 
			int portNo = Integer.parseInt(args[1]);

			requestSocket = new UDPClientSocket(serverIPAddress, portNo); //create a client socket for communication with server
			
			cacheFileList = new ArrayList<CacheFile>(); //instantiate cache array
			
			do {
				
				printWelcomeScreen();
				option = Integer.parseInt((sc.next()));
				
				//display sub options based on the option user chose in main menu
				switch (option) {
				case 1:
					do {
						printOption1Screen();
						suboption = Integer.parseInt((sc.next()));
						
						if (suboption == 1) {
							//get require parameter values for File Read Service
							System.out.print("Enter file path: ");
							sc.nextLine();
							String path = sc.nextLine();
							System.out.print("Enter file offset: ");
							int offset = sc.nextInt();
							System.out.print("Enter number of bytes to read: ");
							int readBytes = sc.nextInt();
							System.out.println("==========================================");
							System.out.println("");
							
							//operations for File Read Service start here
							JSONObject sentObj = new JSONObject(); //instantiate a JSON object to store the user inputs
							sentObj.put("option", new Integer(1));
							sentObj.put("path",path);
							sentObj.put("offset",  new Integer(offset));
							sentObj.put("readBytes",  new Integer(readBytes));
							 
							 int fileIndex = getCacheFileIndex(path); //search whether the file is read before
							 String cacheContent = getStringFromClientCache(fileIndex, offset, readBytes); //search the cached data in the cache
							 if (cacheContent != null){ 
								 System.out.println("Local cache found: " + cacheContent); 
							 }else{ //the data is not in cache, program proceeds to send request to server
								JSONObject replyObj = requestSocket.sendRequest(sentObj); //request is formatted as JSON object and sent to server
								String data;
								if(replyObj.has("Success")){ //if the server reply a successful message with data. (use JSON's method to find the data with name "Success")
									data = replyObj.getString("Success");
									System.out.println("Reply from server: " + data);
									
									//Retrieve the file last modified date time from server 
									JSONObject timerequestObj = new JSONObject();
									timerequestObj.put("option",6);
									timerequestObj.put("path", path);
									
									JSONObject timeObj = requestSocket.sendRequest(timerequestObj);
									Long lastModifiedDate = null;
									
									if(timeObj.has("Success")) //able to retrieve last modified datetime
										lastModifiedDate = timeObj.getLong("Success");
									else{ 
										System.out.println(timeObj.toString());
										System.out.println("Unable to get latest modified date from server");
									}
									
									CacheFile tempCacheFile = new CacheFile(path); //create new cacheFile object 
									tempCacheFile.addNewCacheData(offset, data);
									if(lastModifiedDate != null)
										tempCacheFile.setFileLastUpdatedTime(lastModifiedDate); //update file last updated date time (Tmclient) 
									cacheFileList.add(tempCacheFile);  //add into cache
									
								}else{  //if the server does not reply a successful message
									data = replyObj.getString("Error");
									System.out.println("Reply From Server: Error: " + data);
								}
							}
						}
					} while (suboption != 2);
					break;

				case 2:
					do {
						printOption2Screen();
						suboption = Integer.parseInt((sc.next()));
						
						if (suboption == 1) {
							//get required parameter values for File Write Service
							System.out.print("Enter file path: ");
							sc.nextLine();
							String path = sc.nextLine();
							System.out.print("Enter file offset: ");
							int offset = sc.nextInt();
							sc.nextLine();
							System.out.print("Enter data to write: ");							
							String writeLine = sc.nextLine();
							System.out.println("==========================================");
							System.out.println("");
							
							//operations for File Write Service start here
							JSONObject sentObj = new JSONObject(); //instantiate a JSON object to store the user inputs
							sentObj.put("option", 2);
							sentObj.put("path",path);
							sentObj.put("offset",  new Integer(offset));
							sentObj.put("writeLine", writeLine);

							JSONObject replyObj = requestSocket.sendRequest(sentObj);
							String data;
							if(replyObj.has("Success")){ //if the server reply a successful message with data.
								data = replyObj.getString("Success");
								System.out.println("Reply from server: " + data);
								removeCacheFileFromList(path);
							}else{  //if the server does not reply a successful message
								data = replyObj.getString("Error");
								System.out.println("Reply from server: Error: " + data);
							}
						}
					} while (suboption != 2);
					break;

				case 3:
					do {
						printOption3Screen();
						suboption = Integer.parseInt((sc.next()));
						
						if (suboption == 1) {
							//get require parameter values
							System.out.print("Enter file path: ");
							sc.nextLine();
							String path = sc.nextLine();
							System.out.print("Enter monitor time interval(second): ");
							int interval = sc.nextInt();
							System.out.println("==========================================");
							System.out.println("");
							
							//operations for Register Monitor Changes Service start here
							JSONObject sentObj = new JSONObject(); //instantiate a JSON object to store the user inputs
							sentObj.put("option",3);
							sentObj.put("path",path);
							sentObj.put("intervalLength", new Integer(interval * 1000));
							
							JSONObject replyObj = requestSocket.sendRequest(sentObj);
							String data;
							if(replyObj.has("Success")){ //if the server reply a successful message with data.
								data = replyObj.getString("Success");
								System.out.println("Reply from server: " + data);
								CallBackImpl callback = new CallBackImpl();
								requestSocket.monitorUpdateFromServer(sentObj, callback);
							}else{  //if the server does not reply a successful message
								data = replyObj.getString("Error");
								System.out.println("Reply from server: Error: " + data);
							}
						}
					} while (suboption != 2);
					break;
				
				case 4:
					do {
						printOption4Screen();
						suboption = Integer.parseInt((sc.next()));
						
						if (suboption == 1) {
							//get require parameter values
							System.out.print("Enter directory to list: ");
							sc.nextLine();
							String path = sc.nextLine();
							System.out.println("==========================================");
							System.out.println("");
							
							//operations for List Directory Service start here
							JSONObject sentObj = new JSONObject(); //instantiate a JSON object to store the user inputs
							sentObj.put("option",4); 
							sentObj.put("path",path);
							
							JSONObject replyObj = requestSocket.sendRequest(sentObj);
							String data;
							if(replyObj.has("Success")){ //if the server reply a successful message with data.
								System.out.println("Reply from server: \n");
								JSONArray listJson = replyObj.getJSONArray("Success");
								for(int i=0; i<listJson.length(); i++){
									data = listJson.getString(i);
									System.out.println(data);
								}
								if(listJson.length() == 0)
									System.out.println("No item in the directory");
							}else{  //if the server does not reply a successful message
								data = replyObj.getString("Error");
								System.out.println("Reply from server: Error: " + data);
							}
						}
					} while (suboption != 2);
					break;
					
				case 5:
					do {
						printOption5Screen();
						suboption = Integer.parseInt((sc.next()));
						
						if (suboption == 1) {
							//get require parameter values for Append File Service
							System.out.print("Enter file path: ");
							sc.nextLine();
							String path = sc.nextLine();
							System.out.print("Enter data to append: ");
							String writeLine = sc.nextLine();
							System.out.println("==========================================");
							System.out.println("");
							
							//operations for Append File Service start here
							JSONObject sentObj = new JSONObject(); //instantiate a JSON object to store the user inputs
							sentObj.put("option",5);
							sentObj.put("path",path);
							sentObj.put("writeLine", writeLine);
							
							JSONObject replyObj = requestSocket.sendRequest(sentObj);
							String data;
							if(replyObj.has("Success")){ //if the server reply a successful message with data.
								data = replyObj.getString("Success");
								System.out.println("Reply from server: " + data);
								removeCacheFileFromList(path);
							}else{ //if the server does not reply a successful message
								data = replyObj.getString("Error");
								System.out.println("Reply from server: Error: " + data);
							}
						}
					} while (suboption != 2);
					break;
				
				}
				
			} while (option != 6);
			System.out.println("Thank you for using Remote File Access System. See you again!");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static String getStringFromClientCache(int fileIndex, int offset, int length){
		if(fileIndex == -1)
			return null;
		if(!isClientCacheValidate(fileIndex))
			return null;
		return cacheFileList.get(fileIndex).getCacheString(offset, length);
	}
	
	public static boolean isClientCacheValidate(int fileIndex){
		try{
			if(cacheFileList.get(fileIndex).isCacheFileStillValidate() )
				return true;
			else{
				JSONObject sentObj = new JSONObject();
				sentObj.put("option",6);
				sentObj.put("path",cacheFileList.get(fileIndex).getFilename());
				
				JSONObject replyObj = requestSocket.sendRequest(sentObj);
				Long serverLastUpdatedTime;
				if(replyObj.has("Success")){ //if the server reply a successful message with data
					serverLastUpdatedTime = replyObj.getLong("Success");
					if(cacheFileList.get(fileIndex).getFileLastUpdatedTime().getTime() < 
							serverLastUpdatedTime ){
						removeCacheFileFromList(fileIndex);
						return false;
					}
					return true;
				}else{ //if the server does not reply a successful message
					System.out.println("Unable to fetch time from server");
					return false;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("File path and name error");
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public static int getCacheFileIndex(String filename){
	//method to get index number of specified file in the cacheFileList
		for(int i=0; i<cacheFileList.size(); i++){
			if(cacheFileList.get(i).isSameFile(filename))
				return i;
		}
		return -1;
	}
	
	public static void removeCacheFileFromList(String filename){
	//method to remove the specified file from cacheFileList using file name
		for(int i=0; i<cacheFileList.size(); i++){
			if(cacheFileList.get(i).isSameFile(filename))
				cacheFileList.remove(i);
		}
	}
	
	public static void removeCacheFileFromList(int fileIndex){
	//method to remove the specified file from cacheFileList using file index
		cacheFileList.remove(fileIndex);
	}
 
	public static void printWelcomeScreen(){
		System.out.println("==========================================");
		System.out.println("Welcome to Remote File Access System.");
		System.out.println("==========================================");
		System.out.println("Choose an option to start:");
		System.out.println("1. Read a file.");
		System.out.println("2. Write to a file.");
		System.out.println("3. Register file monitoring service.");
		System.out.println("4. List directory.");
		System.out.println("5. Append a file");
		System.out.println("6. Quit.");
		System.out.println("==========================================");
	}
	
	public static void printOption1Screen(){
		System.out.println("==========================================");
		System.out.println("1. Enter file information.");
		System.out.println("2. Back to menu.");
		System.out.println("==========================================");
	}
	
	public static void printOption2Screen(){
		System.out.println("==========================================");
		System.out.println("1. Enter data to write.");
		System.out.println("2. Back to menu.");
		System.out.println("==========================================");
	}
	
	public static void printOption3Screen(){
		System.out.println("==========================================");
		System.out.println("1. Enter file information.");
		System.out.println("2. Back to menu.");
		System.out.println("==========================================");
	}
	
	public static void printOption4Screen(){
		System.out.println("==========================================");
		System.out.println("1. Enter directory information.");
		System.out.println("2. Back to menu.");
		System.out.println("==========================================");
	}
	
	public static void printOption5Screen(){
		System.out.println("==========================================");
		System.out.println("1. Enter data to append.");
		System.out.println("2. Back to menu.");
		System.out.println("==========================================");
	}
}

interface CallBack {
//an interface to monitor service 
    void methodToPrint(String msg);
}

class CallBackImpl implements CallBack {
//a class to monitor service 
    public void methodToPrint(String msg) {
        try {
        	JSONObject tempObj = new JSONObject(msg);
        	System.out.println("==========================================");
			System.out.println(tempObj.get("Notification"));
		} catch (JSONException e) {
			System.out.println("Invalid notification parameters");
		}
    }
}


