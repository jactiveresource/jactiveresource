/*

Copyright (c) 2008, Jared Crapo All rights reserved. 

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * <h3>Overview</h3>
 * 
 * A resource connection is the channel by which to receive data from the
 * outside world. It provides HTTP transport and that's it. You give it a URL,
 * tell it get, put, post, or delete, and it gives you back the response.
 * 
 * <h3>Authentication</h3>
 * 
 * If your service requires HTTP based authentication, you can give the user
 * name and password in two ways. The preferred approach is to use the
 * {@link #setUsername(String)} and {@link #setPassword(String)} methods. <code>
 * <pre>
 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
 * c.setUsername("Ace");
 * c.setPassword("newenglandclamchowder");
 * </pre>
 * </code>
 * 
 * The alternative is to embed them into the URL, like so: <code>
 * <pre>
 * ResourceConnection c = new ResourceConnection("http://Ace:newenglandclamchowder@localhost:3000");
 * </pre>
 * </code>
 * 
 * <h3>HTTP Parameters</h3>
 * 
 * You can custom http parameters, like connection or socket timeouts, by
 * changing the httpParams property. There are static methods in
 * <code>org.apache.http.params.HttpProtocolParams</code> that set the various
 * parameters.
 * 
 * <code>
 * <pre>
 * {@literal
 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
 * HttpParams params = c.getHttpParams();
 * HttpProtocolParams.setConnectionTimeout(httpParams, 5000);
 * c.setHttpParams(params);
 * ArrayList<String> a = new ArrayList<String>();
 * }</pre>
 * </code>
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class ResourceConnection {

	private URL site;
	private String username;
	private String password;

	private ClientConnectionManager connectionManager;
	private DefaultHttpClient httpclient;

	private static final String CONTENT_TYPE = "Content-type";

	private Log log = LogFactory.getLog(ResourceConnection.class);

	/**
	 * Connect a resource located at a site represented in a URL
	 * 
	 * @param site
	 */
	public ResourceConnection(URL site) {
		this.site = site;
		init();
	}

	/**
	 * Connect a resource located at a site represented in a string
	 * 
	 * @param site
	 * @throws MalformedURLException
	 */
	public ResourceConnection(String site) throws MalformedURLException {
		this.site = new URL(site);
		init();
	}

	public ResourceConnection(URL site, ClientConnectionManager ccm) {
		this.site = site;

	}

	/**
	 * @return the URL object for the site this connection is attached to
	 */
	public URL getSite() {
		return this.site;
	}

	/**
	 * @return the username used for authentication
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to use for authentication
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password used for authentication
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to use for authentication
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * append url to the site this Connection was created with, issue a HTTP GET
	 * request, and return the body of the HTTP response
	 * 
	 * @param url
	 *            generates a URL when toString() is called
	 * @return a string containing the body of the response
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public String get(Object url) throws HttpException, IOException,
			InterruptedException, URISyntaxException {

		StringBuffer sb = new StringBuffer();
		BufferedReader reader = getStream(url);
		if (reader != null) {
			try {
				int c;
				while ((c = reader.read()) != -1)
					sb.append((char) c);
			} finally {
				reader.close();
			}
		}
		return sb.toString();
	}

	/**
	 * append url to the site this Connection was created with, issue a HTTP GET
	 * request, and return a buffered input stream of the body of the HTTP
	 * response. You have to call reader.close() when you are done with it in
	 * order to clean up resources cleanly.
	 * 
	 * if there is no response body, return null
	 * 
	 * @param url
	 * @return a buffered stream of the response
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public BufferedReader getStream(Object url) throws HttpException,
			IOException, InterruptedException, URISyntaxException {

		HttpClient client = createHttpClient(this.getSite());
		String uri = this.getSite().toString() + url.toString();
		HttpGet request = new HttpGet(uri);
		HttpEntity entity = null;
		log.trace("HttpGet uri=" + uri);
		HttpResponse response = client.execute(request);
		checkHttpStatus(response);
		entity = response.getEntity();

		BufferedReader reader = null;
		if (entity != null) {
			reader = new BufferedReader(new InputStreamReader(
					entity.getContent()));
		}
		return reader;
	}

	/**
	 * send an http put request to the server. This is a bit unique because
	 * there is no response returned from the server.
	 * 
	 * @param url
	 * @param body
	 * @param contentType
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public HttpResponse put(Object url, String body, String contentType)
			throws URISyntaxException, HttpException, IOException,
			InterruptedException {
		HttpClient client = createHttpClient(this.getSite());
		String uri = this.getSite().toString() + url.toString();
		HttpPut request = new HttpPut(uri);
		log.trace("HttpPut uri=" + uri);
		request.setHeader(CONTENT_TYPE, contentType);
		StringEntity entity = new StringEntity(body);
		request.setEntity(entity);
		HttpResponse response = client.execute(request);
		return response;
	}

	/**
	 * post body to url using the supplied content type
	 * 
	 * @param url
	 * @param body
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws ClientError
	 * @throws ServerError
	 */
	public HttpResponse post(Object url, String body, String contentType)
			throws ClientProtocolException, IOException, ClientError,
			ServerError {
		HttpClient client = createHttpClient(this.getSite());
		String uri = this.getSite().toString() + url.toString();
		HttpPost request = new HttpPost(uri);
		log.trace("HttpGet uri=" + uri);
		request.setHeader(CONTENT_TYPE, contentType);
		StringEntity entity = new StringEntity(body);
		request.setEntity(entity);
		HttpResponse response = client.execute(request);
		return response;
	}

	/**
	 * delete a resource on the server
	 * 
	 * @param url
	 * @throws ClientError
	 * @throws ServerError
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void delete(Object url) throws ClientError, ServerError,
			ClientProtocolException, IOException {
		HttpClient client = createHttpClient(this.getSite());
		String uri = this.getSite().toString() + url.toString();
		HttpDelete request = new HttpDelete(uri);
		log.trace("HttpDelete uri=" + uri);
		HttpResponse response = client.execute(request);
		checkHttpStatus(response);
	}

	/**
	 * check the status in the HTTP response and throw an appropriate exception
	 * 
	 * @param response
	 * @throws ClientError
	 * @throws ServerError
	 */
	public final void checkHttpStatus(HttpResponse response)
			throws ClientError, ServerError {

		int status = response.getStatusLine().getStatusCode();
		if (status == 400)
			throw new BadRequest();
		else if (status == 401)
			throw new UnauthorizedAccess();
		else if (status == 403)
			throw new ForbiddenAccess();
		else if (status == 404)
			throw new ResourceNotFound();
		else if (status == 405)
			throw new MethodNotAllowed();
		else if (status == 409)
			throw new ResourceConflict();
		else if (status == 422)
			throw new ResourceInvalid();
		else if (status >= 401 && status <= 499)
			throw new ClientError();
		else if (status >= 500 && status <= 599)
			throw new ServerError();
	}

	/*
	 * create or retrieve a cached HttpClient object
	 */
	private HttpClient createHttpClient(URL site) {

		// we recreate the connection manager every time because it looks at
		// http parameters to get created.
		// TODO we should have an alternative way to get http params so the
		// connection manager can persist
		this.connectionManager = new ThreadSafeClientConnManager(
				getHttpParams(), supportedSchemes);

		this.httpclient = new DefaultHttpClient(this.connectionManager,
				getHttpParams());

		// check for authentication credentials
		String u = null, p = null;
		if (this.username != null) {
			// we have explicit username and password
			u = this.username;
			p = this.password;
		} else {
			// check the URI
			String userinfo = site.getUserInfo();
			if (userinfo != null) {
				int pos = userinfo.indexOf(":");
				if (pos > 0) {
					u = userinfo.substring(0, pos);
					p = userinfo.substring(pos + 1);

				}
			}
		}
		// use the credentials if we have them
		if (u != null) {
			this.httpclient.getCredentialsProvider().setCredentials(
					new AuthScope(site.getHost(), site.getPort()),
					new UsernamePasswordCredentials(u, p));
		}
		return this.httpclient;
	}

	private HttpParams httpParams;

	public HttpParams getHttpParams() {
		return httpParams;
	}

	public void setHttpParams(HttpParams params) {
		this.httpParams = params;
	}

	/**
	 * The scheme registry. Instantiated in {@link #init setup}.
	 */
	private static SchemeRegistry supportedSchemes;

	/**
	 * initialize http client settings
	 */
	private void init() {
		supportedSchemes = new SchemeRegistry();

		// Register the "http" protocol scheme, it is required
		// by the default operator to look up socket factories.
		SocketFactory sf = PlainSocketFactory.getSocketFactory();
		supportedSchemes.register(new Scheme("http", sf, 80));

		sf = SSLSocketFactory.getSocketFactory();
		supportedSchemes.register(new Scheme("https", sf, 443));

		// set parameters
		httpParams = new BasicHttpParams();
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, "UTF-8");
		ConnManagerParams.setMaxTotalConnections(httpParams, 400);

		log.trace("ResourceConnection initialized");
	}

	public String toString() {
		return site.toString();
	}
}