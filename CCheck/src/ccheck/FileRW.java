package ccheck;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;

import android.content.Context;

public class FileRW {
	
	Context context;
	    
    private HashMap<String, String> pins;
    
    
    public FileRW(Context context) {
    	
    	this.context = context;
   
    	this.pins = new HashMap<String, String>();
    	load();
    }
    
    public HashMap<String, String> getPins() {
        return this.pins;
    }
    
    @SuppressWarnings("unchecked")
	public void load() { 
    	
		pins.clear();
		
		String fileName = "pins";
		InputStream inputStream = null;
    	
    	File file = new File(context.getFilesDir(), fileName);
    	
    	if(file.exists()) {
    		try {
    			inputStream = context.openFileInput(fileName);
    		} catch (FileNotFoundException fnfException) {			
    			//fnfException.printStackTrace();
    			return;
    		} 
    	} else {
    		try {
    			inputStream = context.getAssets().open(fileName);
    		} catch (IOException ioException) {				
				//ioException.printStackTrace();
    			return;
			}
    	}
    	
        ObjectInputStream objectInputStream;
        
		try {
			
			objectInputStream = new ObjectInputStream(inputStream);
			pins = (HashMap<String, String>) objectInputStream.readObject();
			
			objectInputStream.close();
			inputStream.close();
			
		} catch (StreamCorruptedException scException) {
			//scException.printStackTrace();
			return;
		} catch (IOException ioException1) {
			//ioException1.printStackTrace();
			return;
		} catch (ClassNotFoundException cnfException) {
			//cnfException.printStackTrace();
			return;
		}
		
	}
	
    public String removePin(String pin) {
    	return pins.remove(pin);    	
    }
    
    public boolean containsUrl(String url) {
    	return pins.containsKey(url);
    }
    
    public boolean insertPin(String url, String pin) {
    	
    	boolean contains = false;
    	
    	if(!(contains = pins.containsKey(url))) {
    		pins.put(url, pin);
    	}
    	
    	return contains;
    }    
  
    public void save() {
    	
    	OutputStream outputStream = null; 
    	
    	String fileName = "pins";
    	
    	context.deleteFile(fileName);
    	
    	try {
    		outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {			
			//e.printStackTrace();
			return;
		}
        
		try {
			
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(pins);
			
	        objectOutputStream.close();
	        outputStream.close(); 
	        
		} catch (IOException ioException) {
			//ioException.printStackTrace();
			return;
		}
        
    	
    }    

}
