import java.util.ArrayList;
import java.util.List;


public class ReqHistoryKeeper {
	
	//REQUEST_AFTER_TIME is the maximum number of milliseconds 
	//to keep the request history. Those requests after this interval, is no longer stored
	public static int REQUEST_AFTER_TIME = 60000;
	
	//keeping all the previous request history within limited time
	List<UdpReqRecord> reqHistory;
	
	public ReqHistoryKeeper(){
		reqHistory = new ArrayList<UdpReqRecord>();
	}
	
	public void addHistory(int requestId, String ipAddress, int portNo, String result){
		/**
		 * Add the new request history to the history list stack
		 */
		UdpReqRecord newRecord = new UdpReqRecord(requestId, ipAddress, portNo, result);
		
		//if there is a previous request from the same client
		//, it will replace the previous history to
		//save memory. If not create the new record and add it.
		int prevIndex = getIndexByDevice(newRecord);
		if(prevIndex == -1)
			reqHistory.add(newRecord);
		else
			reqHistory.set(prevIndex, newRecord);
		
	}
	
	public int getIndexByDevice(UdpReqRecord record){
		/**
		 * Search for the index number of client in the history list
		 */
		for(int i=0; i<reqHistory.size(); i++){
			if(record.isSameProcess(reqHistory.get(i)))
				return i;
		}
		return -1;
	}
	
	public boolean isSameRequestExists(int requestId, String ipAddress, int portNo){
		/**
		 * Check for the duplicated requests in the history
		 */
		if(getRequestResults(requestId, ipAddress, portNo) == null)
			return false;
		return true;
	}
	
	public String getRequestResults(int requestId, String ipAddress, int portNo){
		/**
		 * get the previous saved result for the request
		 */
		UdpReqRecord newRecord = new UdpReqRecord(requestId, ipAddress, portNo);
		for(int i=0; i<reqHistory.size(); i++){
			if(newRecord.equals(reqHistory.get(i)))
				return reqHistory.get(i).getResult();
		}
		return null;
	}
	
}
