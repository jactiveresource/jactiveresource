/*

Copyright (c) 2010, Jared Crapo All rights reserved. 

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
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.jactiveresource.annotation.CollectionName;

import com.thoughtworks.xstream.XStream;

/**
 * A resource factory provides methods that construct url's, retrieves the
 * contents of those url's, and deserializes the contents into java objects.
 * 
 * <h3>Creating a Factory</h3>
 * 
 * <h3>Testing a Factory</h3>
 * 
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class ResourceFactory<T extends Resource> {

	private ResourceConnection connection;
	private Class<T> clazz;
	private XStream xstream;
	private Log log;

	/**
	 * Create a new resource factory.
	 * 
	 * You have to pass in the class for this resource in addition to using the
	 * concrete parameterized type because of type erasure. See <a href=
	 * "http://www.angelikalanger.com/GenericsFAQ/FAQSections/ParameterizedTypes.html#FAQ106"
	 * >Angelika Langer's Java Generics FAQ</a> for details.
	 * 
	 * @param c
	 * @param clazz
	 */
	public ResourceFactory(ResourceConnection c, Class<T> clazz) {
		log = LogFactory.getLog(ResourceFactory.class);
		this.setConnection(c);
		this.setResourceClass(clazz);
		makeXStream();
		registerClass(clazz);
		log.debug("new ResourceFactory created");
	}

	/**
	 * 
	 * @param c
	 */
	public void registerClass(Class<?> c) {
		log.trace("registering class " + c.getName());
		log.trace("processing XStream annotations");
		getXStream().processAnnotations(c);
	}

	/**
	 * create an XStream object suitable for use in parsing Rails flavored XML
	 * 
	 * @return
	 */
	protected void makeXStream() {
		log.trace("creating new XStream() object");
		setXStream(new XStream());
	}

	/**
	 * Return true if a resource exists. Say I have a person service at
	 * <code>http://localhost:3000/</code>.
	 * 
	 * <code>
	 * <pre>
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * boolean fred = rf.exists("5");
	 * </pre>
	 * </code>
	 * 
	 * If <code>http://localhost:3000/people/5.xml</code> is a valid URL which
	 * returns data , then <code>fred</code> is true.
	 * 
	 * @param id
	 *            the id you want to check
	 * @return true if the resource exists, false if it does not
	 */
	public boolean exists(String id) {
		log.trace("exists(String id) id=" + id);
		URLBuilder url = URLForOne(id);
		try {
			getConnection().get(url);
			log.trace(url + " exists");
			return true;
		} catch (ResourceNotFound e) {
			log.trace(url + " does not exist");
			return false;
		} catch (HttpException e) {
			log.info(url + " generated an HttpException", e);
			return false;
		} catch (IOException e) {
			log.info(url + " generated an IOException", e);
			return false;
		} catch (InterruptedException e) {
			log.info(url + " generated an InterruptedException", e);
			return false;
		} catch (URISyntaxException e) {
			log.info(url + " generated an URISyntaxException", e);
			return false;
		}
	}

	/**
	 * Create a new instance of a resource. This method calls the default
	 * constructor of your resource class, and also attaches the factory to the
	 * resource class. Say we had a Person class like this:
	 * 
	 * <code>
	 * <pre>
	 * public class Person extends ActiveResource {
	 *   private String id;
	 *   public String getId() {
	 *     return id;
	 *   }
	 *   public void setId(String id) {
	 *     this.id = id;
	 *   }
	 * }
	 * </pre>
	 * </code>
	 * 
	 * And:
	 * 
	 * <code>
	 * <pre>
	 * ResourceFactory pf = new ResourceFactory(c, Person.class);
	 * Person a = new Person();
	 * a.setFactory(pf);
	 * Person b = pf.instantiate();
	 * </pre>
	 * </code>
	 * 
	 * Person a and Person b are now equivalent.
	 * 
	 * @param <T>
	 * @return a new instance of a resource
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public T instantiate() throws InstantiationException,
			IllegalAccessException {
		T obj = (T) getResourceClass().newInstance();
		setFactory(obj);
		log.trace("instantiated resource class=" + clazz.toString());
		return obj;
	}

	/**
	 * create a new resource on the server from a local object
	 * 
	 * @param r
	 * @throws ClientProtocolException
	 * @throws ClientError
	 * @throws ServerError
	 * @throws IOException
	 */
	public boolean create(T r) throws ClientProtocolException, ClientError,
			ServerError, IOException {
		log.trace("trying to create resource of class="
				+ r.getClass().toString());
		URLBuilder url = URLForCollection();
		String xml = getXStream().toXML(r);
		HttpResponse response = getConnection().post(url, xml,
				getResourceFormat().contentType());
		String entity = EntityUtils.toString(response.getEntity());
		try {
			getConnection().checkHttpStatus(response);
			getXStream().fromXML(entity, r);
			setFactory(r);
			log.trace("resource created from " + r.toString());
			return true;
		} catch (ResourceInvalid e) {
			return false;
		}
	}

	/**
	 * update the server resource associated with an object
	 * 
	 * @param r
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean update(T r) throws URISyntaxException, HttpException,
			IOException, InterruptedException {
		log.trace("update class=" + r.getClass().toString());
		URLBuilder url = URLForOne(r.getId());
		String xml = getXStream().toXML(r);
		HttpResponse response = getConnection().put(url, xml,
				getResourceFormat().contentType());
		// String entity = EntityUtils.toString(response.getEntity());
		try {
			connection.checkHttpStatus(response);
			return true;
		} catch (ResourceInvalid e) {
			return false;
		}
	}

	/**
	 * create the resource if it is new, otherwise update it
	 * 
	 * @param r
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws InterruptedException
	 */
	public boolean save(T r) throws ClientProtocolException, IOException,
			URISyntaxException, HttpException, InterruptedException {
		if (r.isNew())
			return create(r);
		else
			return update(r);
	}

	/**
	 * repopulate a local object with data from the service
	 * 
	 * @param r
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public void reload(T r) throws HttpException, IOException,
			InterruptedException, URISyntaxException {
		log.trace("reloading class=" + r.getClass().toString());
		URLBuilder url = URLForOne(r.getId());
		fetchOne(url, r);
	}

	/**
	 * delete a resource
	 * 
	 * @param r
	 * @throws ClientError
	 * @throws ServerError
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void delete(T r) throws ClientError, ServerError,
			ClientProtocolException, IOException {
		URLBuilder url = URLForOne(r.getId());
		log.trace("deleting class=" + r.getClass().toString() + " id="
				+ r.getId());
		getConnection().delete(url);
	}

	/**
	 * Create one object from the response of a given url. If your subclass
	 * wants to create a bunch of cool find methods that each generate a proper
	 * URL, they can then use this method to get the data and create the objects
	 * from the response.
	 * 
	 * @param <T>
	 * @param url
	 * @return a new object representing the data returned by the url
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public T fetchOne(Object url) throws HttpException, IOException,
			InterruptedException, URISyntaxException {
		return deserializeOne(getConnection().get(url));
	}

	/**
	 * Inflate (or unmarshall) an object from serialized data.
	 * 
	 * If the inflated class inherits from {@link ActiveResource} then also call
	 * the setFactory() method so that the convenience methods of ActiveResource
	 * work.
	 * 
	 * @param data
	 *            a string of serialized data
	 * @return a new object
	 * @throws IOException
	 */
	public T deserializeOne(String data) throws IOException {
		@SuppressWarnings("unchecked")
		T obj = (T) getXStream().fromXML(data);
		log.trace("create new object of class=" + obj.getClass().toString());
		setFactory(obj);
		return obj;
	}

	/**
	 * Update an existing object with data from the given url
	 * 
	 * @param <T>
	 * @param url
	 * @param resource
	 *            the object to update
	 * @return the updated resource object you passed in
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public T fetchOne(Object url, T resource) throws HttpException,
			IOException, InterruptedException, URISyntaxException {
		return deserializeAndUpdateOne(getConnection().get(url), resource);
	}

	/**
	 * Weasel method to update an existing object with serialized data.
	 * 
	 * @param <T>
	 * @param data
	 *            serialized data
	 * @param resource
	 *            the object to update
	 * @return the updated resource object you passed in
	 * @throws IOException
	 */
	public T deserializeAndUpdateOne(String data, T resource)
			throws IOException {
		getXStream().fromXML(data, resource);
		log.trace("updating object of class=" + resource.getClass().toString()
				+ " id=" + resource.getId());
		setFactory(resource);
		return resource;
	}

	/**
	 * Create an array of objects from the response of a given url.
	 * 
	 * @param <T>
	 * @param url
	 * @return an array of objects
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedExceptionInflate
	 *             (or unmarshall) a list of objects from a stream
	 * @throws URISyntaxException
	 */
	public ArrayList<T> fetchMany(Object url) throws HttpException,
			IOException, InterruptedException, URISyntaxException {
		return deserializeMany(getConnection().getStream(url));
	}

	/**
	 * Inflate (or unmarshall) a list of objects from a stream using XStream.
	 * This method exhausts and closes the stream.
	 * 
	 * @param <T>
	 * @param stream
	 *            an open input stream
	 * @return a list of objects
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<T> deserializeMany(BufferedReader stream)
			throws IOException {
		ObjectInputStream ostream = getXStream()
				.createObjectInputStream(stream);
		ArrayList<T> list = new ArrayList<T>();
		T obj;
		while (true) {
			try {
				obj = (T) ostream.readObject();
				setFactory(obj);
				list.add(obj);
			} catch (EOFException e) {
				break;
			} catch (ClassNotFoundException e) {
				// do nothing
			}
		}
		ostream.close();
		log.trace("deserialized " + list.size() + " objects");
		return list;
	}

	/**
	 * by default, we use XML as the format for data exchange
	 * 
	 * You can override this in your subclasses to change it
	 * 
	 * @return a resource format
	 */
	protected ResourceFormat getResourceFormat() {
		return ResourceFormat.XML;
	}

	/**
	 * return the url that accesses the resource identified by id, ie
	 * <code>/people/1.xml</code>
	 * 
	 * If you pass null, you'll get null.
	 * 
	 * @param id
	 *            the identifier of the resource you want the URL to
	 * @return
	 */
	protected URLBuilder URLForOne(String id) {
		if (id == null) {
			return null;
		} else {
			return new URLBuilder(getCollectionName()).add(id
					+ getResourceFormat().extension());
		}
	}

	/**
	 * return the url that accesses the entire collection of resources, ie
	 * <code>/people.xml</code>
	 * 
	 * @return
	 */
	protected URLBuilder URLForCollection() {
		return new URLBuilder(getCollectionName()
				+ getResourceFormat().extension());
	}

	/**
	 * figure out the name of the collection of resources generated by the main
	 * resource of this factory.
	 * 
	 * This method first looks for a CollectionName annotation on the class it
	 * knows how to create. If there is no annotation, then the name of the
	 * class is used.
	 * 
	 * @return the name of the collection
	 */
	protected String getCollectionName() {
		String name;
		CollectionName cn = getResourceClass().getAnnotation(CollectionName.class);
		if (cn != null) {
			name = cn.value();
		} else {
			name = getResourceClass().getSimpleName();
		}
		return name;
	}

	/**
	 * If the resource is a subclass of ActiveResource, then attach the factory
	 * to it
	 * 
	 * @param resource
	 */
	private void setFactory(T resource) {
		if (ActiveResource.class.isInstance(resource)) {
			ActiveResource res = (ActiveResource) resource;
			res.setFactory(this);
		}
	}

	public ResourceConnection getConnection() {
		return connection;
	}

	public void setConnection(ResourceConnection connection) {
		this.connection = connection;
	}

	protected Class<T> getResourceClass() {
		return clazz;
	}

	protected void setResourceClass(Class<T> clazz) {
		this.clazz = clazz;
	}

	protected XStream getXStream() {
		return xstream;
	}

	protected void setXStream(XStream xstream) {
		this.xstream = xstream;
	}

}
