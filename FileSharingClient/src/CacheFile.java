import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CacheFile {
	/**
	 * This class is a cache object to store content of a file in memory
	 * The data is stored as <Offset number, character> pair
	 */
	static final int CACHE_VALIDATE_INTERVAL = 5000; //millisecond 
	
	private String filename;
	private Date cacheLastValidatedTime; //cache last validated datetime
	private Date tmClient; //file last modified datetime (updated when a client retrieve data from server and his cache is outdated)
	private Map<Integer, Character> charMapArray = new HashMap<Integer, Character>();

	public CacheFile(String filename){
		this.filename = filename;
		cacheLastValidatedTime = new Date();
	}
	
	public boolean isSameFile(String otherFilename){
	//method to check whether two file (path) are the same
		return this.filename.equals(otherFilename);
	}
	
	public String getFilename(){
		return this.filename;
	}
	
	public void addNewCacheData(int offset, String data){
	//method to add new cache data with LastValidatedTime and Char Array
		cacheLastValidatedTime = new Date();
		for(int i=0; i<data.length(); i++){
			Character newChar = data.charAt(i);
			charMapArray.remove(offset+i);
			charMapArray.put(offset+i, newChar);
		}
	}
	
	public String getCacheString(int offset, int length){
	//method to get existing data (either get whole or subset of the data according to the offset and length)
		if(isCharRangeValid(offset, length)){
			String returnString = "";
			for(int i=0; i<length; i++){
				Character returnChar = charMapArray.get(offset+i);
				if(returnChar == null)
					return null;
				returnString += returnChar;
			}
			return returnString;
		}
		return null;
	}
	
	private boolean isCharRangeValid(int offset, int length){
	//method to validate the offset and byte length
		if(offset >= 0 && length > 0){
			return true;
		}
		return false;
	}
	
	public boolean isCacheFileStillValidate(){
	//method to check whether the local cache is fresh enough 
		return new Date().getTime() - cacheLastValidatedTime.getTime() < CACHE_VALIDATE_INTERVAL;
	}
	
	public Date getFileLastUpdatedTime(){
		return tmClient;
	}
	
	public void setFileLastUpdatedTime(long updateTime){
		tmClient = new Date(updateTime);
	}
	
}
