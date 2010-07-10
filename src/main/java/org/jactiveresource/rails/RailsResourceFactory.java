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

package org.jactiveresource.rails;

import static org.jactiveresource.rails.Inflector.dasherize;
import static org.jactiveresource.rails.Inflector.singularize;
import static org.jactiveresource.rails.Inflector.underscore;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.jactiveresource.Resource;
import org.jactiveresource.ResourceConnection;
import org.jactiveresource.ResourceFactory;
import org.jactiveresource.ResourceFormat;
import org.jactiveresource.URLBuilder;
import org.jactiveresource.annotation.CollectionName;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;
import com.thoughtworks.xstream.core.util.ClassLoaderReference;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;

/**
 * The Rails Resource Factory is specially designed to talk to rest services
 * provided by Ruby on Rails applications.
 * 
 * <h3>Creating a Factory</h3>
 * 
 * <h3>Finding Resources</h3>
 * 
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class RailsResourceFactory<T extends Resource> extends
		ResourceFactory<T> {
	private Log log = LogFactory.getLog(RailsResourceFactory.class);

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
	public RailsResourceFactory(ResourceConnection c, Class<T> clazz) {
		super(c, clazz);
	}

	public RailsResourceFactory(ResourceConnection c, Class<T> clazz,
			ResourceFormat rf) {
		super(c, clazz, rf);
	}

	/**
	 * 
	 * @param c
	 */
	public void registerClass(Class<?> c) {
		// no logging here because it gets called by the constructor
		String xmlname = singularize(dasherize(underscore(c.getSimpleName())));
		getXStream().alias(xmlname, c);

		Field[] fields = c.getDeclaredFields();
		for (Field field : fields) {
			xmlname = dasherize(underscore(field.getName()));
			getXStream().aliasField(xmlname, c, field.getName());
		}

		getXStream().processAnnotations(c);
	}

	/**
	 * Create an XStream object that uses a custom ConverterLookup and register
	 * a converter to handle rails style dates
	 */
	@Override
	public void makeXStream() {
		// no logging here because it gets called by the constructor
		RailsConverterLookup rcl = new RailsConverterLookup();
		XStream x = new XStream(null, getHSD(), new ClassLoaderReference(
				new CompositeClassLoader()), null, rcl, null);

		// register a special converter so we can parse rails dates
		x.registerConverter(new ISO8601DateConverter());
		setXStream(x);
	}

	/**
	 * Fetch resources using query parameters. Say I have a collection of people
	 * at <code>http://localhost:3000/people.xml</code>. I can specify a
	 * parameter to limit the people to those who hold a position of manager by
	 * using <code>http://localhost:3000/people.xml?position=manager</code>.
	 * 
	 * <code>
	 * <pre>
	 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
	 * RailsResourceFactory<Person> rf = new RailsResourceFactory<Person>(c, Person.class);
	 * HashMap<String,String> params = new HashMap<String,String>();
	 * params.put("position", "manager");
	 * ArrayList<Person> rubydevs = rf.findAll(params);
	 * </pre>
	 * </code>
	 * 
	 * @param <T>
	 * @param params
	 * @return a list of objects
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public ArrayList<T> findAll(Map<Object, Object> params)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {
		URLBuilder url = URLForCollection().addQuery(params);
		log.trace("finding all url=" + url);
		return fetchMany(url);
	}

	/**
	 * Fetch resources using query parameters. In this case the query parameters
	 * are taken from a URLBuilder object. To get resources from
	 * <code>http://localhost:3000/people.xml?position=manager</code> do:
	 * 
	 * <code>
	 * <pre>
	 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
	 * RailsResourceFactory<Person> rf = new RailsResourceFactory<Person>(c, Person.class);
	 * URLBuilder params = new URLBuilder();
	 * params.addQuery("position", "manager");
	 * ArrayList<Person> rubydevs = rf.findAll(params);
	 * </pre>
	 * </code>
	 * 
	 * @param <T>
	 * @param params
	 * @return a list of objects
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public ArrayList<T> findAll(URLBuilder params) throws HttpException,
			IOException, InterruptedException, URISyntaxException {
		URLBuilder url = URLForCollection().addQuery(params);
		log.trace("finding all url=" + url);
		return fetchMany(url);
	}

	/**
	 * Fetch a list of resources using a custom method. Say I have a collection
	 * of people at <code>http://localhost:3000/people.xml</code>. Say there is
	 * a custom method managers at
	 * <code>http://localhost:3000/people/geeks.xml</code> which returns only
	 * the people who are geeks. To get the list of geeks I would use:
	 * 
	 * <code>
	 * <pre>
	 * c = new ResourceConnection("http://localhost:3000");
	 * rf = new ResourceFactory(c, Person.class);
	 * ArrayList<Person> geeks = rf.findAll("geeks");
	 * </pre>
	 * </code>
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
	public ArrayList<T> findAll(String from) throws HttpException, IOException,
			InterruptedException, URISyntaxException {
		URLBuilder url = new URLBuilder(from + getResourceFormat().extension());
		log.trace("findAll(String from) from=" + from);
		return fetchMany(url);
	}

	/**
	 * Fetch resources using a custom method and query parameters. Say I have a
	 * collection of people at <code>http://localhost:3000/people.xml</code>.
	 * Say there is a custom method <code>developers</code> at
	 * <code>http://localhost:3000/people/developers.xml</code> which returns
	 * only the people who are developers. Additionally, I can specify a
	 * parameter to limit the developers to those who are skilled in a
	 * particular language by using
	 * <code>http://localhost:3000/people/developers.xml?language=ruby</code>
	 * <p>
	 * To get the ruby developers:
	 * 
	 * <code>
	 * <pre>
	 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
	 * RailsResourceFactory<Person> rf = new RailsResourceFactory<Person>(c, Person.class);
	 * HashMap<String, String> params = new HashMap<String, String>();
	 * params.put("language", "ruby");
	 * ArrayList<Person> rubydevs = rf.findAll("developers", params);
	 * </pre>
	 * </code>
	 * 
	 * @param <T>
	 * @param from
	 * @param params
	 * @return a list of objects
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public ArrayList<T> findAll(String from, Map<Object, Object> params)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {
		URLBuilder url = new URLBuilder(from + getResourceFormat().extension())
				.addQuery(params);
		log.trace("findAll url=" + url);
		return fetchMany(url);
	}

	/**
	 * Fetch resources using a custom method and query parameters, where the
	 * query parameters are taken from a URLBuilder object. To get the resources
	 * from
	 * <code>http://localhost:3000/people/developers.xml?language=ruby</code>
	 * do:
	 * 
	 * <code>
	 * <pre>
	 * ResourceConnection c = new ResourceConnection("http://localhost:3000");
	 * ResourceFactory rf = new ResourceFactory(c, Person.class);
	 * URLBuilder params = new URLBuilder();
	 * params.addQuery("language", "ruby");
	 * ArrayList<Person> rubydevs = rf.findAll(<developers>, params);
	 * </pre>
	 * </code>
	 * 
	 * @param <T>
	 * @param from
	 * @param params
	 * @return a list of objects
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public ArrayList<T> findAll(String from, URLBuilder params)
			throws HttpException, IOException, InterruptedException,
			URISyntaxException {
		URLBuilder url = new URLBuilder(from + getResourceFormat().extension())
				.addQuery(params);
		log.trace("findAll url=" + from);
		return fetchMany(url);
	}

	/**
	 * figure out the name of the collection of resources generated by the main
	 * class of this factory.
	 * 
	 * This method first looks for a CollectionName annotation on the class it
	 * knows how to create. If there is no annotation, then it guesses based on
	 * the name of the class.
	 * 
	 * @return the name of the collection
	 */
	protected String getCollectionName() {
		String name;
		CollectionName cn = getResourceClass().getAnnotation(
				CollectionName.class);
		if (cn != null) {
			name = cn.value();
		} else {
			name = Inflector.underscore(getResourceClass().getSimpleName());
			name = Inflector.pluralize(name);
		}
		return name;
	}
}
