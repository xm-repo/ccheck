//Based on HTTPProxySnifferEngine.java from The Grinder distribution.
// The Grinder distribution is available at http://grinder.sourceforge.net/
/*
Copyright 2007 Srinivas Inguva

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * Neither the name of Stanford University nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package mitm;

import iaik.asn1.ObjectID;
import iaik.asn1.structures.Name;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.SSLSocket;

public class HTTPSProxyEngine extends ProxyEngine {

	public static final String ACCEPT_TIMEOUT_MESSAGE = "Listen time out";

	private String m_tempRemoteHost;
	private int m_tempRemotePort;

	private final Pattern m_httpsConnectPattern;

	private final ProxySSLEngine m_proxySSLEngine;

	private final HashMap<String, MITMSSLSocketFactory> cnMap = new HashMap<String, MITMSSLSocketFactory>();

	public HTTPSProxyEngine(MITMPlainSocketFactory plainSocketFactory, MITMSSLSocketFactory sslSocketFactory,
			ProxyDataFilter requestFilter, ProxyDataFilter responseFilter, String localHost, int localPort, 
			int timeout) throws IOException, PatternSyntaxException {
		
		super(plainSocketFactory, requestFilter, responseFilter, new ConnectionDetails(localHost, localPort, "", -1, false), timeout);

		m_httpsConnectPattern = Pattern.compile("^CONNECT[ \\t]+([^:]+):(\\d+).*\r\n\r\n", Pattern.DOTALL);

		assert sslSocketFactory != null;
		
		m_proxySSLEngine = new ProxySSLEngine(sslSocketFactory, requestFilter, responseFilter);
	}

	public void run() {
		
		final byte[] buffer = new byte[40960];

		while(true) {
			
			try {
				
				final Socket localSocket = getServerSocket().accept();

				final BufferedInputStream in = new BufferedInputStream(localSocket.getInputStream(), buffer.length);

				in.mark(buffer.length);

				final int bytesRead = in.read(buffer);

				final String line = (bytesRead > 0) ? new String(buffer, 0, bytesRead, "US-ASCII") : "";

				final Matcher httpsConnectMatcher = m_httpsConnectPattern.matcher(line);

				if(httpsConnectMatcher.find()) {
												
					while(in.read(buffer, 0, in.available()) > 0);

					final String remoteHost = httpsConnectMatcher.group(1);

					final int remotePort = Integer.parseInt(httpsConnectMatcher.group(2));

					final String target = remoteHost + ":" + remotePort;

					if(MITMProxyServer.debugFlag) {
						System.out .println("[HTTPSProxyEngine] Establishing a new HTTPS proxy connection to " + target);
					}

					m_tempRemoteHost = remoteHost;
					m_tempRemotePort = remotePort;

					X509Certificate java_cert = null;
					SSLSocket remoteSocket = null;
					
					try {
						
						remoteSocket = (SSLSocket) m_proxySSLEngine.getSocketFactory().createClientSocket(remoteHost, remotePort);						
						java_cert = (X509Certificate) remoteSocket.getSession().getPeerCertificates()[0];
					} catch (IOException ioe) {
						
						ioe.printStackTrace();
						sendClientResponse(localSocket.getOutputStream(), "504 Gateway Timeout", remoteHost, remotePort);
						continue;
					}

					iaik.x509.X509Certificate certificate = new iaik.x509.X509Certificate(java_cert.getEncoded());
					Name name = (Name) certificate.getSubjectDN();
					String serverCN = name.getRDN(ObjectID.commonName);

					if(MITMProxyServer.debugFlag) {
						System.out.println("[HTTPSProxyEngine] Remote Server Cert CN = " + serverCN);
					}

					m_proxySSLEngine.setRemoteSocket(remoteSocket);

					ServerSocket localProxy = m_proxySSLEngine.createServerSocket(serverCN, certificate);

					new Thread(m_proxySSLEngine, "HTTPS proxy SSL engine").start();

					try {						
						Thread.sleep(10);
					} catch (Exception ignore) {						
					}

					final Socket sslProxySocket = getSocketFactory().createClientSocket(getConnectionDetails().getLocalHost(), localProxy.getLocalPort());

					new Thread(new CopyStreamRunnable(in, sslProxySocket.getOutputStream()),
							"Copy to proxy engine for " + target).start();

					final OutputStream out = localSocket.getOutputStream();

					new Thread(new CopyStreamRunnable(sslProxySocket.getInputStream(), out),
							"Copy from proxy engine for " + target).start();

					sendClientResponse(out, "200 OK", remoteHost, remotePort);
				} else { 
					
					System.err.println("Failed to determine proxy destination from message:");
					System.err.println(line);
					sendClientResponse(localSocket.getOutputStream(),"501 Not Implemented", "localhost", getConnectionDetails().getLocalPort());
				}
			} catch (InterruptedIOException e) {
				
				System.err.println(ACCEPT_TIMEOUT_MESSAGE);
				break;
			} catch (Exception e) {
				
				e.printStackTrace(System.err);
			}
		}
	}

	private void sendClientResponse(OutputStream out, String msg, String remoteHost, int remotePort) throws IOException {
		
		final StringBuffer response = new StringBuffer();
		
		response.append("HTTP/1.0 ").append(msg).append("\r\n");
		response.append("Host: " + remoteHost + ":" + remotePort + "\r\n");
		response.append("Proxy-agent: CS255-MITMProxy/1.0\r\n");
		response.append("\r\n");
		
		out.write(response.toString().getBytes());
		out.flush();
	}

	private class ProxySSLEngine extends ProxyEngine {
		
		Socket remoteSocket = null;
		int timeout = 0;

		ProxySSLEngine(MITMSSLSocketFactory socketFactory, ProxyDataFilter requestFilter, ProxyDataFilter responseFilter)
				throws IOException {
			
			super(socketFactory, requestFilter, responseFilter,
					new ConnectionDetails(HTTPSProxyEngine.this.getConnectionDetails().getLocalHost(), 0, "", -1, true), 0);
		}

		public final void setRemoteSocket(Socket socket) {
			
			this.remoteSocket = socket;
		}

		public final ServerSocket createServerSocket(String remoteServerCN, iaik.x509.X509Certificate remoteServerCert) throws Exception {

			assert remoteServerCN != null;

			MITMSSLSocketFactory ssf = null;

			if(cnMap.get(remoteServerCN) == null) {
				
				System.out.println("[HTTPSProxyEngine] Creating a new certificate for " + remoteServerCN);
				
				ssf = new MITMSSLSocketFactory(remoteServerCN, remoteServerCert);
				cnMap.put(remoteServerCN, ssf);
			} else {
				if(MITMProxyServer.debugFlag) {
					System.out.println("[HTTPSProxyEngine] Found cached certificate for " + remoteServerCN);
				}
				
				ssf = (MITMSSLSocketFactory) cnMap.get(remoteServerCN);
			}
			
			m_serverSocket = ssf.createServerSocket(getConnectionDetails().getLocalHost(), 0, timeout);
			
			return m_serverSocket;
		}

		public void run() {
			
			try {
				
				final Socket localSocket = this.getServerSocket().accept();

				if(MITMProxyServer.debugFlag) {
					System.out .println("[HTTPSProxyEngine] New proxy proxy connection to " + m_tempRemoteHost + ":" + m_tempRemotePort);
				}

				this.launchThreadPair(localSocket, remoteSocket, localSocket.getInputStream(),
						localSocket.getOutputStream(), m_tempRemoteHost, m_tempRemotePort);
				
			} catch (IOException e) {
				
				e.printStackTrace(System.err);
			}
		}
	}

}
