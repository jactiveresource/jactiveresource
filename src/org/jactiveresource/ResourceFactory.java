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

import static org.jactiveresource.Inflector.dasherize;
import static org.jactiveresource.Inflector.singularize;
import static org.jactiveresource.Inflector.underscore;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;
import org.jactiveresource.annotation.CollectionName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;

/**
 * <h3>Finding Resources</h3>
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class ResourceFactory {

	private ResourceConnection connection;
	private Class<? extends ActiveResource> clazz;
	private XStream xstream;

	public ResourceFactory(ResourceConnection c,
			Class<? extends ActiveResource> clazz) {
		this.connection = c;
		this.clazz = clazz;

		xstream = new XStream();
		xstream.registerConverter(new ISO8601DateConverter());

		// "#{prefix(prefix_options)}#{collection_name}/#{id}.#{format.extension}#{
		// query_string(query_options)}"

		String xmlname = singularize(dasherize(getCollectionName()));
		xstream.alias(xmlname, clazz);

		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			xmlname = dasherize(underscore(field.getName()));
			xstream.aliasField(xmlname, clazz, field.getName());
		}

		xstream.processAnnotations(clazz);
	}

	/**
	 * Retrieve the resource identified by <code>id</code>, and return a new
	 * instance of the appropriate object
	 * 
	 * @param <T>
	 * @param id
	 *            the primary identifier
	 * @return a new instance of a subclass of @{link ActiveResource}
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws HttpException
	 */
	public <T extends ActiveResource> T find(String id) throws HttpException,
			IOException, InterruptedException, URISyntaxException {
		String url = "/" + getCollectionName() + "/" + id
				+ getResourceFormat().extension();
		return getOne(url);
	}

	/**
	 * Fetch a single resource using a custom method. Say I have a collection of
	 * people at <code>http://localhost:3000/people.xml</code>. Say there is a
	 * custom method managers at
	 * <code>http://localhost:3000/people/geeks.xml</code> which returns only
	 * the people who are geeks. If I want the first geek, I would do:
	 * 
	 * <pre>
	 * {@code
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * Person geek = rf.find(QueryScope.FIRST,"geeks");
	 * }</pre>
	 * 
	 * For the last geek you can use:
	 * 
	 * <pre>
	 * {@code
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * Person geek = rf.find(QueryScope.LAST,"geeks");
	 * }</pre>
	 * <p>
	 * If I had a method which I knew only returned a single object, like
	 * <code>http://localhost:3000/people/alphageek.xml</code>, then you could
	 * use:
	 * 
	 * <pre>
	 * {@code
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * Person geek = rf.find(QueryScope.ONE,"alphageek");
	 * }</pre>
	 * This last one is not the safest, because we really don't have a way of
	 * knowing that the alphageek method will only return a single object. It
	 * would be safer to use the {@link #findAll(String)} method instead.
	 * 
	 * @param <T>
	 * @param scope
	 * @param from
	 *            the name of the custom method
	 * @return an object if found, null otherwise
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws HttpException
	 */
	public <T extends ActiveResource> T find(QueryScope scope, String from)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {
		ArrayList<T> list;
		String url = "/" + getCollectionName() + "/" + from
				+ getResourceFormat().extension();
		if (scope == QueryScope.ONE) {
			return getOne(url);
		} else if (scope == QueryScope.FIRST) {
			list = getMany(url);
			try {
				return list.get(0);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} else if (scope == QueryScope.LAST) {
			list = getMany(url);
			try {
				return list.get(list.size() - 1);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Fetch a list of resources using a custom method. Say I have a collection
	 * of people at <code>http://localhost:3000/people.xml</code>. Say there is
	 * a custom method managers at
	 * <code>http://localhost:3000/people/geeks.xml</code> which returns only
	 * the people who are geeks. To get the list of geeks I would use:
	 * 
	 * <pre>
	 * {@code
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * ArrayList<Person> geeks = rf.findAll("geeks");
	 * }</pre>
	 * 
	 * @param <T>
	 * @param from
	 *            the name of the custom method
	 * @return a list of objects
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public <T extends ActiveResource> ArrayList<T> findAll(String from)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {
		String url = "/" + getCollectionName() + "/" + from
				+ getResourceFormat().extension();
		return getMany(url);
	}

	/**
	 * Fetch all the resources. Say I have a person service at
	 * <code>http://localhost:3000/</code>. The following would return the list
	 * of people returned by <code>http://localhost:3000/people.xml</code>.
	 * 
	 * <pre>
	 * {@code
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * ArrayList<Person> people = rf.findAll();
	 * }</pre>
	 * 
	 * @param <T>
	 * @return a list of objects
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public <T extends ActiveResource> ArrayList<T> findAll()
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {
		String url = "/" + getCollectionName()
				+ getResourceFormat().extension();
		return getMany(url);
	}

	/**
	 * Return true if a resource exists. Say I have a person service at
	 * <code>http://localhost:3000/</code>. If the following is a valid URL
	 * which returns data <code>http://localhost:3000/people/5.xml</code>, then
	 * <code>fred</code> is true.
	 * 
	 * <pre>
	 * {@code
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * boolean fred = rf.exists("5");
	 * }</pre>
	 * 
	 * @param id
	 *            the id you want to check
	 * @return true if the resource exists, false if it does not
	 */
	public boolean exists(String id) {
		String url = "/" + getCollectionName() + "/" + id
				+ getResourceFormat().extension();
		try {
			connection.get(url);
			return true;
		} catch (HttpException e) {
			return false;
		} catch (IOException e) {
			return false;
		} catch (InterruptedException e) {
			return false;
		} catch (URISyntaxException e) {
			return false;
		}
	}

	/**
	 * create a new instance of a resource
	 * 
	 * @param <T>
	 * @return a new instance of a resource
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T extends ActiveResource> T instantiate()
			throws InstantiationException, IllegalAccessException {
		T obj = (T) clazz.newInstance();
		obj.setFactory(this);
		return obj;
	}

	/**
	 * 
	 * @param r
	 * @throws ClientProtocolException
	 * @throws ClientError
	 * @throws ServerError
	 * @throws IOException
	 */
	public void create(ActiveResource r) throws ClientProtocolException,
			ClientError, ServerError, IOException {
		String url = "/" + getCollectionName()
				+ getResourceFormat().extension();
		String xml = xstream.toXML(r);
		String response = connection.post(url, xml, ResourceFormat.XML
				.contentType());

		xstream.fromXML(response, r);
		r.setFactory(this);
	}

	/**
	 * 
	 * @param r
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void update(ActiveResource r) throws URISyntaxException,
			HttpException, IOException, InterruptedException {
		String url = "/" + getCollectionName() + "/" + r.getId()
				+ getResourceFormat().extension();
		String xml = xstream.toXML(r);
		connection.put(url, xml, getResourceFormat().contentType());
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
	public void save(ActiveResource r) throws ClientProtocolException,
			IOException, URISyntaxException, HttpException,
			InterruptedException {
		if (r.isNew())
			create(r);
		else
			update(r);
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
	public void reload(ActiveResource r) throws HttpException, IOException,
			InterruptedException, URISyntaxException {
		String url = "/" + getCollectionName() + "/" + r.getId()
				+ getResourceFormat().extension();
		getOne(url, r);
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
	public void delete(ActiveResource r) throws ClientError, ServerError,
			ClientProtocolException, IOException {
		String url = "/" + getCollectionName() + "/" + r.getId()
				+ getResourceFormat().extension();
		connection.delete(url);
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
	protected <T extends ActiveResource> T getOne(String url)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {

		String data = connection.get(url);
		T obj = (T) xstream.fromXML(data);
		obj.setFactory(this);
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
	protected <T extends ActiveResource> T getOne(String url, T resource)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {

		String data = connection.get(url);
		xstream.fromXML(data, resource);
		resource.setFactory(this);
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
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	protected <T extends ActiveResource> ArrayList<T> getMany(String url)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {
		BufferedReader xml = connection.getStream(url);
		ObjectInputStream in = xstream.createObjectInputStream(xml);

		ArrayList<T> list = new ArrayList<T>();
		while (true) {
			try {
				list.add((T) in.readObject());
			} catch (EOFException e) {
				break;
			} catch (ClassNotFoundException e) {
				// do nothing
			}
		}
		in.close();
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
	 * figure out the name of the collection of resources generated by this
	 * factory.
	 * 
	 * This method first looks for a CollectionName annotation on the class it
	 * knows how to create. If there is no annotation, then it guesses based on
	 * the name of the class.
	 * 
	 * @return the name of the collection
	 */
	protected String getCollectionName() {
		String name;
		CollectionName cn = clazz.getAnnotation(CollectionName.class);
		if (cn != null) {
			name = cn.value();
		} else {
			name = Inflector.underscore(clazz.getSimpleName());
			name = Inflector.pluralize(name);
		}
		return name;
	}
}
