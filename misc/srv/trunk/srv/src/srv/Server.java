package srv;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

public class Server {

	public static void main(String[] args) {
		
		int port = 8001;
		
		//Logger logger = Logger.getLogger("");
		
		/*System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tD %1$tH:%1$tM:%1$tS] %4$s: %5$s %n");		
		
		try {
			
			FileHandler fileHandler = new FileHandler("log.txt");
			fileHandler.setFormatter(new SimpleFormatter());			
			logger.addHandler(fileHandler);
		} catch (Exception exception) {
            exception.printStackTrace();
        }*/	
		
		System.setProperty("javax.net.ssl.keyStore", "keystore");
	    System.setProperty("javax.net.ssl.keyStorePassword", "123456");
	    
	    try {			
			
			System.out.println("Locating server socket factory for SSL...");
			SSLServerSocketFactory sslServerSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
			
			System.out.println("Creating a server socket on port " + port);
			SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
			
			
//			String[] suppSuites = sslServerSocket.getSupportedCipherSuites();            
//          System.out.println("Support cipher suites are:");
//          for(int i = 0; i < suppSuites.length; i++) {
//              System.out.println(suppSuites[i]);
//          }            
                        
            String[] enblSuites = sslServerSocket.getEnabledCipherSuites();
            System.out.println("\n\nActive cipher suites are:");
            for(int i = 0; i < enblSuites.length; i++) {
                System.out.println(enblSuites[i]);
            }
            
//          sslServerSocket.setEnabledCipherSuites(suppSuites);
            
//          String[] suppProtocols = sslServerSocket.getSupportedProtocols();
//          System.out.println("Support protocols are:");
//          for (int i = 0; i < suppProtocols.length; i++) {
//              System.out.println(suppProtocols[i]);
//          }
//            
            String[] enblProtocols = sslServerSocket.getEnabledProtocols();
            System.out.println("\n\nActive protocols are: ");
            for (int i = 0; i < enblProtocols.length; i++) {
                System.out.println(enblProtocols[i]);
            }
            
            while(true) {            	
            	
            	System.out.println("Waiting for client...");
            	SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            	System.out.println("Starting handshake...");
            	sslSocket.startHandshake();
            	
            	SSLSession sslSession = sslSocket.getSession();
            	
            	Certificate[] cchain2 = sslSession.getLocalCertificates();
            	
            	for (int i = 0; i < cchain2.length; i++) {
                    System.out.println(((X509Certificate) cchain2[i]).getSubjectDN());
                }
            	
                //Certificate[] cchain3 = sslSession.getPeerCertificates();
            	
            	//for (int i = 0; i < cchain3.length; i++) {
                  //  System.out.println(((X509Certificate) cchain3[i]).getSubjectDN());
                //}
            	
            	System.out.println("Peer host: " + sslSession.getPeerHost());
                System.out.println("Cipher:    " + sslSession.getCipherSuite());
                System.out.println("Protocol:  " + sslSession.getProtocol());
                System.out.println("ID:        " + new BigInteger(sslSession.getId()));
                System.out.println("Created:   " + sslSession.getCreationTime());
                System.out.println("Accessed:  " + sslSession.getLastAccessedTime());
                
                System.out.println("Just connected to " + sslSocket.getRemoteSocketAddress());

            	InputStream inputStream = sslSocket.getInputStream();
            	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            	String string = null;
            	
            	while((string = bufferedReader.readLine()) != null) {
            		System.out.println(string);
            		System.out.flush();
            	}

            	bufferedReader.close();
            	sslSocket.close();           	
            }
            
                      
        } catch (Exception exception) {
            exception.printStackTrace();
        }     

	}

}