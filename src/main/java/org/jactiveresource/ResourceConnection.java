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
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;

/**
 * <h3>Overview</h3>
 * 
 * A resource connection defines and implements a channel by which to exchange data 
 * with a resource. It provides HTTP transport and that's it. You give it a URL,
 * tell it get, put, post, or delete, and it gives you back the response.  This
 * class uses <a href="http://hc.apache.org/httpcomponents-client-ga/">Apache HTTP Client</a>
 * to do all of the real work.
 * 
 * <h3>Usage</h3>
 * You create a resource connection by indicating the base URL from which one or more
 * resources may be interacted with.
 * 
 * Say there is a person resource available at
 * <code>http://localhost:3000/people.xml</code>, and a <code>Person</code>
 * class which models the data elements provided by that resource.
 * <code>
 * <pre>
 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
 * ResourceFactory f = new ResourceFactory<Person>(c, Person.class);
 * </pre>
 * </code>
 * 
 * The resource connection provides http transport, and the resource factory takes the
 * serialized data stream sent over that transport and turns it into Person objects.
 * 
 * <h3>HttpClient Factories</h3>
 * 
 * A resource connection uses an HttpClient object to do all of the dirty work.  You may
 * find that you want to control the creation and parameters associated with these HttpClient
 * objects used by the resource connection.  For example, you might have a cookie based
 * authentication scheme, and you need to get your cookies on the HttpClient objects
 * used by the resource connection.  By default, a resource connection uses
 * {@link DefaultHttpClientFactory} to create HttpClient objects for it's use.  You can
 * create your own factory, as long as it implements the {@link AbstractHttpClientFactory}
 * interface, giving you full control over TCP timeouts, cookies, authentication,
 * and concurrency of the HttpClient objects used by the resource connection.
 * 
 * <h3>Authentication</h3>
 * 
 * If your service requires HTTP based authentication, you can use the
 * {@link #setUsername(String)} and {@link #setPassword(String)} methods. <code>
 * <pre>
 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
 * c.setUsername("Ace");
 * c.setPassword("newenglandclamchowder");
 * </pre>
 * </code>
 * These credentials will be passed through to the HttpClientFactory object,
 * which is responsible for using these credentials on the HTTP request.  The
 * default HttpClientFactory object will use these credentials as basic
 * authentication.  Authentication credentials embedded in the URL will be ignored.
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class ResourceConnection {

	private URL site;

	private AbstractHttpClientFactory clientFactory;

	private static final String CONTENT_TYPE = "Content-type";

	private Log log = LogFactory.getLog(ResourceConnection.class);

	/**
	 * Connect a resource located at a site represented in a URL
	 * 
	 * @param site
	 */
	public ResourceConnection(URL site) {
		this.site = site;
		this.clientFactory = new DefaultHttpClientFactory();
	}

	/**
	 * Connect a resource located at a site represented in a string
	 * 
	 * @param site
	 * @throws MalformedURLException
	 */
	public ResourceConnection(String site) throws MalformedURLException {
		this.site = new URL(site);
		this.clientFactory = new DefaultHttpClientFactory();
	}

	/**
	 * Connect a resource located at a site represented in a URL
	 * 
	 * @param site
	 */
	public ResourceConnection(URL site, AbstractHttpClientFactory clientFactory) {
		this.site = site;
		this.clientFactory = clientFactory;
	}

	/**
	 * Connect a resource located at a site represented in a string, using a
	 * specific HttpClientFactory
	 * 
	 * @param site
	 * @param factory
	 * @throws MalformedURLException
	 */
	public ResourceConnection(String site, AbstractHttpClientFactory factory)
			throws MalformedURLException {
		this.site = new URL(site);
		this.clientFactory = factory;
	}

	/**
	 * @return the URL object for the site this connection points to
	 */
	public URL getSite() {
		return this.site;
	}

	/**
	 * set the factory used to create HttpClient objects. If you don't
	 * set your own factory, {@link DefaultHttpClientFactory} will be used.
	 * 
	 * @param factory
	 */
	public void setHttpClientFactory(AbstractHttpClientFactory factory) {
		this.clientFactory = factory;
	}

	/**
	 * 
	 * @return the factory used to create HttpClient objects
	 */
	public AbstractHttpClientFactory getHttpClientFactory() {
		return this.clientFactory;
	}

	/**
	 * @return the username used for authentication
	 */
	public String getUsername() {
		return this.clientFactory.getUsername();
	}

	/**
	 * @param username
	 *            the username to use for authentication
	 */
	public void setUsername(String username) {
		this.clientFactory.setUsername(username);
	}

	/**
	 * @return the password used for authentication
	 */
	public String getPassword() {
		return this.clientFactory.getPassword();
	}

	/**
	 * @param password
	 *            the password to use for authentication
	 */
	public void setPassword(String password) {
		this.clientFactory.setPassword(password);
	}

	/**
	 * Close this resource connection
	 */
	public void close() {
		clientFactory.shutter();
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

		HttpClient client = clientFactory.getHttpClient(this.getSite());
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
		HttpClient client = clientFactory.getHttpClient(this.getSite());
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
		HttpClient client = clientFactory.getHttpClient(this.getSite());
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
		HttpClient client = clientFactory.getHttpClient(this.getSite());
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

	public String toString() {
		return site.toString();
	}
}