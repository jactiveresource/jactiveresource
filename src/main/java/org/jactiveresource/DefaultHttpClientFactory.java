/*

Copyright (c) 2011, Jared Crapo All rights reserved. 

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met: 

- Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer. 

- Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution. 

- Neither the name of jactiveresource.org nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

 */

package org.jactiveresource;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

/**
 * An implementation of {@link AbstractHttpClientFactory} which
 * uses the HTTPClient ThreadSafeClientConnManager connection pool.
 * 
 * The HttpClient objects returned by {@link #getHttpClient(URL)} can
 * connect to http services on port 80, and http services with TLS on
 * port 443.  The client objects use HTTP 1.1 and ask for data in
 * UTF-8 encoding.  It allows a maximum of 40 concurrent connections to
 * a URL; TCP connections time out after 10 seconds, TCP read operations
 * time out after 30 seconds.
 * 
 * If a username and password are given, they will be made available for
 * HTTPBasic authentication.
 *
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class DefaultHttpClientFactory implements AbstractHttpClientFactory {

	private ThreadSafeClientConnManager ccm;
	private String username;
	private String password;
	private Log log = LogFactory.getLog(DefaultHttpClientFactory.class);

	public DefaultHttpClientFactory() {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory
				.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory
				.getSocketFactory()));

		// create a thread-safe pooled tcp connection manager
		ccm = new ThreadSafeClientConnManager(schemeRegistry);

		// bump up the maximum number of connections allowed
		ccm.setMaxTotal(40);
		ccm.setDefaultMaxPerRoute(40);

		log.trace("ThreadSafeClientConnManager initialized");
	}

	@Override
	public HttpClient getHttpClient(URL site) {
		DefaultHttpClient c = new DefaultHttpClient(this.ccm);

		// set basic http parameters
		HttpParams p = c.getParams();
		p.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
				HttpVersion.HTTP_1_1);
		p.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,
				"UTF-8");
		// connection timeout in 10 seconds, socket timeout in 30 seconds
		p.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(10000));
		p.setParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(30000));
		
		// use the credentials if we have them
		if (this.username != null) {
			c.getCredentialsProvider().setCredentials(
					new AuthScope(site.getHost(), site.getPort()),
					new UsernamePasswordCredentials(this.username,
							this.password));
		}

		log.trace("HttpClient created.");
		return c;
	}

	@Override
	public void shutter() {
		ccm.shutdown();
	}
	
	/**
	 * @return the username used for authentication
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to use for authentication
	 */
	@Override
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password used for authentication
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to use for authentication
	 */
	@Override
	public void setPassword(String password) {
		this.password = password;
	}

}
