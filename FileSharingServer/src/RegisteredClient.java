import java.net.InetAddress;
import java.util.Date;


public class RegisteredClient {
	/**
	 * This class is to store the necessary information for registered client for
	 * monitoring services. Like ipaddress, port number, etc
	 */
	private String filename;
	private long mInterval;
	private InetAddress ipAddress;
	private int port;
	private Date startTime;
	
	//Constructor
	public RegisteredClient(String filename, long mInterval, InetAddress ipAddress, int port){
		this.filename = filename;
		this.mInterval = mInterval;
		this.ipAddress = ipAddress;
		this.port = port;
		this.startTime = new Date();
	}
	
	public String getFilename(){
		return filename;
	}
	
	public long getInterval(){
		return mInterval;
	}
	
	public long getMonitorDuration(){
		Date curTime = new Date();
		return curTime.getTime()-startTime.getTime();
	}
	
	public boolean isClientMonitoring(){
		/**
		 * if the duration between current time and start monitoring time is 
		 * larger than the interval specified, client is no longer monitoring
		 */
		if(getMonitorDuration() > mInterval)
			return false;
		return true;
	}
	
	public boolean isSameFile(String filename){
		return filename.equals(this.filename);
	}
	
	public InetAddress getIpAddress(){
		return ipAddress;
	}
	
	public int getPort(){
		return port;
	}
}
