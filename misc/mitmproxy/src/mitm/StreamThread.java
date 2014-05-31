
package mitm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.SocketException;

/*
 * Copies bytes from an InputStream to an OutputStream.  
 * Uses a ProxyDataFilter to log the contents appropriately.
 */

public class StreamThread implements Runnable {
	
	private final static int BUFFER_SIZE = 65536;

	private final ConnectionDetails m_connectionDetails;
	private final InputStream m_in;
	private final OutputStream m_out;
	private final ProxyDataFilter m_filter;
	private final PrintWriter m_outputWriter;

	public StreamThread(ConnectionDetails connectionDetails, InputStream in, 
			OutputStream out, ProxyDataFilter filter, PrintWriter outputWriter) {
		
		m_connectionDetails = connectionDetails;
		m_in = in;
		m_out = out;
		m_filter = filter;
		m_outputWriter = outputWriter;

		final Thread thread = new Thread(this, "Filter thread for " + m_connectionDetails.getDescription());

		try {
			
			m_filter.connectionOpened(m_connectionDetails);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		thread.start();
	}

	public void run() {
		
		try {
			
			byte[] buffer = new byte[BUFFER_SIZE];

			while (true) {
				
				final int bytesRead = m_in.read(buffer, 0, BUFFER_SIZE);

				if(bytesRead == -1) {
					break;
				}

				final byte[] newBytes = m_filter.handle(m_connectionDetails, buffer, bytesRead);

				m_outputWriter.flush();

				if(newBytes != null) {
					
					m_out.write(newBytes);
				} else {
					
					m_out.write(buffer, 0, bytesRead);
				}
			}
		} catch (SocketException e) {

		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		try {
			
			m_filter.connectionClosed(m_connectionDetails);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}

		m_outputWriter.flush();

		try {
			
			m_out.close();
		} catch (Exception e) {
			
		}

		try {
			
			m_in.close();
		} catch (Exception e) {
			
		}
	}
}
