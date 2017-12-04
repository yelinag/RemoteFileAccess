import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;

public class FileController {
	/**
	 * File Controller class helps to acesss the file in the disks easier.
	 * It has read function, write function, append function, list directory, etc.
	 * @param filename
	 * @param offset
	 * @param noofbytes
	 * @return
	 * @throws IllegalArgumentException
	 * @throws FileNotFoundException
	 * @throws IndexOutOfFileException
	 * @throws IOException
	 */
	
	public static String readFile(String filename, int offset, int noofbytes) throws IllegalArgumentException,
		FileNotFoundException, IndexOutOfFileException, IOException{
		BufferedReader br = null;
		String outString = null;
		try {
			//create new buffer for reading the file
			br = new BufferedReader(new FileReader(filename));
			
			//skip the mentioned character in the file
			br.skip(offset);
			offset = 0;
			
			//read the mentioned number of bytes in the parameters
			char[] cbuf = new char[noofbytes];
			int result = br.read(cbuf, offset, noofbytes);
			
			//if the result is -1, the buffer reader failed and it is returning the error
			if(result == -1){
				throw new IndexOutOfFileException("Index input larger than file size");	
			}else{
				//if not the result has been saved
				outString = new String(cbuf, 0, result);
			}
			
		} finally {
			//close the file
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		//return the output read data
		return outString;
	}
	
	public static String readWholeFile(String filename) throws FileNotFoundException, IndexOutOfFileException, 
		IOException{
		/**
		 * Reads the whole file
		 */
		
		//reading the file
		File file = new File(filename);
		FileInputStream fis = new FileInputStream(file);
		byte[] data = new byte[(int) file.length()];
		int result = fis.read(data);
		fis.close();
		
		//if reading failed returns error message
		if(result == -1){
			throw new IndexOutOfFileException("Index input larger than file size");
		}
		
		//if reading success, return the result file data
		return new String(data);
	}
	
	protected static void writeFile(String filename, int offset, String data) throws 
		IllegalArgumentException, FileNotFoundException, IOException{
		/**
		 * Write the file with offset.
		 */
		
		//search for the specified file
		File f = new File(filename);
		//if not found or is directory return error
		if(!f.exists() || f.isDirectory()) { 
		    throw new FileNotFoundException("File not found");
		}
		
		//wrtiing the content into the file
		RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
		RandomAccessFile rtemp = new RandomAccessFile(new File(filename + "~"), "rw");
		long fileSize = r.length();
		FileChannel sourceChannel = r.getChannel();
		FileChannel targetChannel = rtemp.getChannel();
		sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
		sourceChannel.truncate(offset);
		r.seek(offset);
		r.write(data.getBytes());
		long newOffset = r.getFilePointer();
		targetChannel.position(0L);
		sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
		sourceChannel.close();
		targetChannel.close();
		rtemp.close();
		r.close();
	    
	}
	
	public static File[] listDirectory(String directory) throws FileNotFoundException{
		/**
		 * List the directory and its files
		 */
		
		
		File folder = new File(directory);
		//if folder is not folder or doesn't exist, return error
		if(!folder.exists() || !folder.isDirectory()){
			throw new FileNotFoundException("Folder not found");
		}
		
		//retrieve all the list of the files in a folder
		//reject those hidden files in the directory
		File[] listOfFiles = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
		    	if(name.startsWith("."))
		    		return false;
		    	return true;
		    }
		});
		
		return listOfFiles;
		
	}
	
	public static String[] getFilenameList(File[] listOfFiles){
		/**
		 * Convert the list of files into filename strings
		 */
		String filenameList[] = new String[listOfFiles.length];
	    for (int i = 0; i < listOfFiles.length; i++) {
	      if (listOfFiles[i].isFile()) {
	    	if(!listOfFiles[i].isHidden())
	    		filenameList[i] = listOfFiles[i].getName();
	    		//System.out.println("File " + listOfFiles[i].getName());
	      } else if (listOfFiles[i].isDirectory()) {
	        //System.out.println("Directory " + listOfFiles[i].getName());
	    	  filenameList[i] = listOfFiles[i].getName();
	      }
	    }
	    
	    return filenameList;
	}
	
	public static boolean createNewFile(String filename) throws IOException{
		/**
		 * Create new file specified
		 */
		File file = new File(filename);
	    
		if (file.createNewFile()){
			return true;
			//System.out.println("File is created!");
		}else{
			return false;
			//System.out.println("File already exists.");
		}
	}
	
	public static void appendText(String filename, String data) throws FileNotFoundException, IOException{
    	/**
    	 * Append the text at the end of the file
    	 */
		File file =new File(filename);
		
		if(!file.exists() || !file.isFile()){
			throw new FileNotFoundException("File not found");
		}
		
	    Files.write(Paths.get(filename), 
	    		data.getBytes(), StandardOpenOption.APPEND);
	    
	}
	
	public static long getLatestModifiedDate(String filename) throws FileNotFoundException{
		/**
		 * Retrieved the last modified date of the file in the disk
		 */
		File file =new File(filename);
		
		//if file doesnt exists, then return file not found error
		if(!file.exists() || !file.isFile()){
			throw new FileNotFoundException("File not found");
		}
		//if found, return the last modified date and time
		return file.lastModified();
	}
	
}
