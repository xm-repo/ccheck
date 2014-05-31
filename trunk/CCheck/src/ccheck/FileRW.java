package ccheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
    
    public void load() { 
    	
		pins.clear();
		
		String fileName = "pins";
		InputStream inputStream = null;
    	
    	File file = new File(context.getFilesDir(), fileName);
    	
    	if(file.exists()) {
    		try {
    			inputStream = context.openFileInput(fileName);
    		} catch (FileNotFoundException e) {			
    			//e.printStackTrace();
    			return;
    		} 
    	} else {
    		try {
    			inputStream = context.getAssets().open(fileName);
    		} catch (IOException e) {				
				//e.printStackTrace();
    			return;
			}
    	}
    	
    	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		
		try {
			String url, pin;
			while((url = bufferedReader.readLine()) != null) {
				
				if(url.trim().isEmpty() || url.indexOf('#') == 0) {
					continue;
				}
				
				pin = bufferedReader.readLine();  				
				pins.put(url, pin);    								
			}
		} catch (IOException e) {
			//e.printStackTrace();
			return;
		}
		
		try {
			bufferedReader.close();
		} catch (IOException e) {
			//e.printStackTrace();
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
    	
    	BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));    	
    	
    	for(String url : pins.keySet()) {
    		
    		try {
    			
    			bufferedWriter.write(url);
    			bufferedWriter.newLine();
    			
    			bufferedWriter.write(pins.get(url));
    			bufferedWriter.newLine();
			} catch (IOException e) {
				//e.printStackTrace();
				return;
			}
    		
    	}
    	
    	try {
    		bufferedWriter.close();
		} catch (IOException e) {			
			//e.printStackTrace();
			return;
		}   	
    	
    }    

}
