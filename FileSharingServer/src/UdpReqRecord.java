import java.util.Date;


public class UdpReqRecord {
	/**
	 * This class is to store all the information of previous 
	 * UDP requests.
	 */
	
	protected int requestId;
	protected String ipAddress;
	protected int portNo;
	protected Date requestedTime;
	protected String returnResult;
	
	public UdpReqRecord(int requestId, String ipAddress, int portNo){
		this.requestId = requestId;
		this.ipAddress = ipAddress;
		this.portNo = portNo;
		this.requestedTime = new Date();
		this.returnResult = "";
	}
	
	public UdpReqRecord(int requestId, String ipAddress, int portNo, String result){
		this.requestId = requestId;
		this.ipAddress = ipAddress;
		this.portNo = portNo;
		this.requestedTime = new Date();
		this.returnResult = result;
	}
	
	public boolean equals(UdpReqRecord anotherRecord){
		//check if the request is duplicate to achieve at-most-once semantics
		return requestId == anotherRecord.requestId &&
				ipAddress.equals(anotherRecord.ipAddress) &&
				portNo == anotherRecord.portNo;
	}
	
	public boolean isSameProcess(UdpReqRecord anotherRecord){
		//check two requests are from the same client
		return ipAddress.equals(anotherRecord.ipAddress) &&
				portNo == anotherRecord.portNo;
	}
	
	public String getResult(){
		//retrieve saved result
		return returnResult;
	}
}
