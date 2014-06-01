package getpins;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Main {

	public static void main(String[] args) {
	
		String[] urls = new String[] { 
			"https://www.google.com",
			"https://www.twitter.com", 
			"https://www.blogger.com", 
			"https://www.youtube.com", 
			"https://www.facebook.com",
			"https://www.amazon.com",
			"https://www.linkedin.com", 
			"https://www.blogspot.com", 
			"https://www.wordpress.com",
			"https://www.ebay.com",
			"https://www.instagram.com",
			"https://www.tumblr.com",
			"https://www.imgur.com"  };
		
		
		HashMap<String, String> pins = new HashMap<String, String>();

		for (String strurl : urls) {
           			
			URL url = null;
			try {
				
				url = new URL("https://127.0.0.1:8001");
				
				//if(!url.getProtocol().equals("https")) {	
				//	return;
				//}
				
			} catch (MalformedURLException e) {						
				return;
			}
			
			String hexpin;
			
			try {
				
				HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
				
				httpsURLConnection.setConnectTimeout(4000);
				httpsURLConnection.connect();
				
				//httpsURLConnection.getInputStream();
				
				X509Certificate [] certs = (X509Certificate[]) httpsURLConnection.getServerCertificates();
				if(certs == null) {
					return;
				}
							
				//!!!!!!WUT 0 - perviy
				//X509Certificate rootcert = certs[certs.length - 1];
				X509Certificate rootcert = certs[0];					
				
				final MessageDigest digest = MessageDigest.getInstance("SHA1");	            
	            final byte[] spki = rootcert.getPublicKey().getEncoded();		
				final byte[] pin = digest.digest(spki);
				
				final char[] hexArray = { '0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f' };
				
				char[] hexChars = new char[pin.length * 2];
				
				for(int j = 0; j < pin.length; j++) {
			        
			    	int v = pin[j] & 0xFF;
			    	hexChars[j * 2] = hexArray[v >>> 4];
			        hexChars[j * 2 + 1] = hexArray[v & 0x0F];;	        
			    }    
		    
			    hexpin = new String(hexChars); 
			    
			    pins.put(strurl, hexpin);
			    
			} catch (IOException e) {
				
				e.printStackTrace();
				return;
			} catch (NoSuchAlgorithmException e) {
				
				e.printStackTrace();
				return;
			}
			
		}
		
		for(String key : pins.keySet()) {
			
			System.out.println(key);
			System.out.println(pins.get(key) + "\n");
		}
		
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement("pins");
			doc.appendChild(rootElement);
			
			for(String key : pins.keySet()) {
				
				Element pin = doc.createElement("pin");
				rootElement.appendChild(pin);
				
				Element url = doc.createElement("url");
				url.appendChild(doc.createTextNode(key));
				pin.appendChild(url);
				
				Element hexpin = doc.createElement("pin");
				hexpin.appendChild(doc.createTextNode(pins.get(key)));	
				pin.appendChild(hexpin);
			}
			
			// write the content into xml file
			//TransformerFactory transformerFactory = TransformerFactory.newInstance();
			//transformerFactory.setAttribute("indent-number", 2);
			//Transformer transformer = transformerFactory.newTransformer();
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			
			//DOMSource source = new DOMSource(doc);
			//StreamResult fresult = new StreamResult(new File("file.xml"));
			
			// Output to console for testing
			//StreamResult result = new StreamResult(System.out);
	 
			//transformer.transform(source, result);
			//transformer.transform(source, fresult);
			
		} catch (ParserConfigurationException e) {
			//e.printStackTrace();
		//} catch (TransformerConfigurationException e) {
			//e.printStackTrace();
		//} catch (TransformerException e) {
			//e.printStackTrace();
		}
		
		try {
			
			FileOutputStream fileOutputStream = new FileOutputStream("pins");
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
			
			objectOutputStream.writeObject(pins);
            objectOutputStream.close();
            fileOutputStream.close();
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
		
	}

}
