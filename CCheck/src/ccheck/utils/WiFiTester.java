package ccheck.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class WiFiTester {
	
	public static boolean isWiFiUp(Context context) {
		
		boolean isWiFiUp = true;		    

	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
	    
	    for(NetworkInfo ni : networkInfo) {
	    	
	        if(ni.getTypeName().equalsIgnoreCase("WIFI") && ni.isConnected()) {
	        	isWiFiUp = true;
	            break;
	        }				        
	    }
	    
	    return isWiFiUp;
	}

}
