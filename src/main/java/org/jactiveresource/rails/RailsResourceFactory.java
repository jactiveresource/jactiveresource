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
import java.net.URL;
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
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.xml.XppDriver;

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
		String xmlName = singularize(dasherize(underscore(c.getSimpleName())));
		getXStream().alias(xmlName, c);

		Field[] fields = c.getDeclaredFields();
		for (Field field : fields) {
			/*
			 * Rails dasherizes field names in XML, but underscores them in
			 * JSON. This is desired behavior because dash is also used as a
			 * minus sign in Javascript expressions. Here's an example:
			 */
			// {"person":{"birthdate":"-1200-02-26T14:13:20Z",
			// "created_at":"2010-07-09T06:03:57Z",
			// "id":217,
			// "name":"King Tut",
			// "updated_at":"2010-07-09T06:03:57Z"}}
			//
			// <person>
			// <birthdate type="datetime">-1200-02-26T14:13:20Z</birthdate>
			// <created-at type="datetime">2010-07-09T06:03:57Z</created-at>
			// <id type="integer">217</id>
			// <name>King Tut</name>
			// <updated-at type="datetime">2010-07-09T06:03:57Z</updated-at>
			// </person>
			if (getResourceFormat() == ResourceFormat.JSON) {
				xmlName = underscore(field.getName());
				getXStream().aliasField(xmlName, c, field.getName());
			} else {
				xmlName = dasherize(underscore(field.getName()));
				getXStream().aliasField(xmlName, c, field.getName());
			}

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
		XStream x = new XStream(null, getStreamDriver(),
				new ClassLoaderReference(new CompositeClassLoader()), null,
				rcl, null);

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
		URL url = URLForCollection(params);
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
		URL url = URLForCollection(from);
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
		URL url = URLForCollection(from, params);
		log.trace("findAll url=" + url);
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
	public String getCollectionName() {
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

	protected URL URLForCollection(Map<Object, Object> params) {
		URLBuilder url;
		url = new URLBuilder(getCollectionName()
				+ getResourceFormat().extension());
		url.addQuery(params);
		return url.toURL();
	}

	protected URL URLForCollection(String from) {
		URLBuilder url;
		url = new URLBuilder(from + getResourceFormat().extension());
		return url.toURL();
	}

	protected URL URLForCollection(String from, Map<Object, Object> params) {
		URLBuilder url;
		url = new URLBuilder(from + getResourceFormat().extension());
		url.addQuery(params);
		return url.toURL();
	}

	protected HierarchicalStreamDriver getStreamDriver() {
		HierarchicalStreamDriver hsd = null;
		switch (getResourceFormat()) {
		case XML:
			hsd = new XppDriver();
			break;
		case JSON:
			hsd = new JettisonMappedXmlDriver();
			// hsd = new JsonHierarchicalStreamDriver() {
			// public HierarchicalStreamWriter createWriter(Writer writer) {
			// return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
			// }
			// };
			break;
		}
		return hsd;
	}
}
