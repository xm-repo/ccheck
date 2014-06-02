
package mitm;

import java.io.FileWriter;
import java.io.PrintWriter;

/*
 * Main class for the Man In The Middle SSL proxy.  Delegates the real work to HTTPSProxyEngine.
 */

public class MITMProxyServer {
	
	public static boolean debugFlag = false;

	public static void main(String[] args) {
		
		final MITMProxyServer proxy = new MITMProxyServer(args);
		proxy.run();
	}

	private Error printUsage() {
		
		System.err.println("\n"
						+ "Usage: "
						+ "\n java mitm.MITMProxyServer <options>"
						+ "\n"
						+ "\n Where options can include:"
						+ "\n"
						+ "\n   [-localHost <host name/ip>]  Default is localhost"
						+ "\n   [-localPort <port>]          Default is 8001"
						+ "\n   [-keyStore <file>]           Key store details for"
						+ "\n   [-keyStorePassword <pass>]   certificates. Equivalent to"
						+ "\n   [-keyStoreType <type>]       javax.net.ssl.XXX properties"
						+ "\n   [-keyStoreAlias <alias>]     Default is keytool default of 'mykey'"
						+ "\n   [-outputFile <filename>]     Default is stdout"
						+ "\n   [-v ]                        Verbose proxy output"
						+ "\n   [-h ]                        Print this message"
						+ "\n"
						+ "\n -outputFile specifies where the output from ProxyDataFilter will go."
						+ "\n By default, it is sent to stdout" + "\n");

		System.exit(1);
		
		return null;
	}

	private Error printUsage(String error) {
		
		System.err.println("\n" + "Error: " + error);
		throw printUsage();
	}

	private ProxyEngine m_engine = null;

	private MITMProxyServer(String[] args) {
		
		// Default values.
		ProxyDataFilter requestFilter = new ProxyDataFilter();
		ProxyDataFilter responseFilter = new ProxyDataFilter();

		int localPort = 8001;
		String localHost = "localhost";
		
		System.setProperty(JSSEConstants.KEYSTORE_PROPERTY, "E:\\FakeCAStore");
		System.setProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, "fakekeystorepassword");
		System.setProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, "JKS");
		System.setProperty(JSSEConstants.KEYSTORE_ALIAS_PROPERTY, "mykey");

		int timeout = 0;
		debugFlag = true;

		try {
			
			for(int i = 0; i < args.length; i++) {
				
				if(args[i].equals("-localHost")) {
					
					localHost = args[++i];
				} else if (args[i].equals("-localPort")) {
					
					localPort = Integer.parseInt(args[++i]);
				} else if (args[i].equals("-keyStore")) {
					
					System.setProperty(JSSEConstants.KEYSTORE_PROPERTY, args[++i]);
				} else if (args[i].equals("-keyStorePassword")) {
					
					System.setProperty(JSSEConstants.KEYSTORE_PASSWORD_PROPERTY, args[++i]);
				} else if (args[i].equals("-keyStoreType")) {
					
					System.setProperty(JSSEConstants.KEYSTORE_TYPE_PROPERTY, args[++i]);
				} else if (args[i].equals("-keyStoreAlias")) {
					
					System.setProperty(JSSEConstants.KEYSTORE_ALIAS_PROPERTY, args[++i]);
				} else if (args[i].equals("-timeout")) {
					
					timeout = Integer.parseInt(args[++i]) * 1000;
				} else if (args[i].equals("-v")) {
					
					debugFlag = true;
				} else if (args[i].equals("-outputFile")) {
					
					PrintWriter pw = new PrintWriter(new FileWriter(args[++i]), true);
					requestFilter.setOutputPrintWriter(pw);
					responseFilter.setOutputPrintWriter(pw);
				} else {
					
					throw printUsage();
				}
				
			}
			
		} catch(Exception e) {
			throw printUsage();
		}

		if(timeout < 0) {
			throw printUsage("Timeout must be non-negative");
		}

		final StringBuffer startMessage = new StringBuffer();

		startMessage.append("Initializing SSL proxy with the parameters:"
				+ "\n   Local host:       " + localHost
				+ "\n   Local port:       " + localPort);

		System.err.println(startMessage);

		try {
			
			m_engine = new HTTPSProxyEngine(new MITMPlainSocketFactory(), new MITMSSLSocketFactory(), requestFilter, responseFilter,
					localHost, localPort, timeout);

			System.err.println("Proxy initialized, listening on port " + localPort);
		} catch (Exception e) {
			
			System.err.println("Could not initialize proxy:");
			e.printStackTrace();
			System.exit(2);
		}
	}

	public void run() {
		
		m_engine.run();
		
		System.err.println("Engine exited");
		System.exit(0);
	}
}