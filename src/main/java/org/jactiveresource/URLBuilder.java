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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class URLBuilder {

	private static final char PATH_SEPARATOR = '/';
	// this separates the entire query string from the rest of the URL
	private static final char QUERY_DELIMITER = '?';
	// the separates the various query parameters from each other
	private static final char QUERY_PARAM_SEPARATOR = '&';
	// join the key and value into a single query parameter
	private static final char QUERY_PARAM_JOINER = '=';

	private static final String UTF8 = "UTF-8";

	URL base;
	ArrayList<String> path;
	ArrayList<QueryParam> query;

	/**
	 * Create a new URL builder
	 */
	public URLBuilder() {
		init();
	}

	/**
	 * Create a URL builder with a path already assigned. In the example below,
	 * the URL's for A and B are the same.
	 * 
	 * <pre>
	 * {@code
	 * URLBuilder a = new URLBuilder();
	 * a.add("people.xml");
	 * URLBuilder b = new URLBuilder("people.xml");
	 * }
	 * @param pathcomponent the path to start the URL with
	 */
	public URLBuilder(String pathcomponent) {
		init();
		add(pathcomponent);
	}

	/**
	 * Create a new URL builder from a given base URL
	 * 
	 * @param base
	 */
	public URLBuilder(URL base) {
		this.base = base;
		init();
	}

	private void init() {
		path = new ArrayList<String>();
		query = new ArrayList<QueryParam>();
	}

	/**
	 * Append a path component to the URL. Adding slashes is taken care of for
	 * you. If you want "/people/1/contacts.xml" then do:
	 * 
	 * <pre>
	 * URLBuilder u = new URLBuilder();
	 * u.add("people").add("1").add("contacts.xml");
	 * </pre>
	 * 
	 * If you want "http://localhost:3000/people/1/contacts.xml" then do:
	 * 
	 * <pre>
	 * URLBuilder u = new URLBuilder("http://localhost:3000");
	 * u.add("people").add("1").add("contacts.xml");
	 * </pre>
	 * 
	 * @param pathcomponent
	 *            any object that can render itself as a string
	 * 
	 * @return self
	 */
	public URLBuilder add(Object pathcomponent) {
		path.add(pathcomponent.toString());
		return this;
	}

	/**
	 * Add a query parameter to the URL. You can do lots of fun stuff with this.
	 * As long as the key and the value both return a useful .toString(), you
	 * are good to go.
	 * <p>
	 * If you pass a value that implements Iterable, then you will get a query
	 * parameter added for each value returned by hasNext(). You can also pass a
	 * value that is an array[] of somethings and get the same behavior.
	 * 
	 * @param key
	 * @param value
	 * @return self
	 */
	public URLBuilder addQuery(Object key, Object value) {
		query.add(new QueryParam(key, value));
		return this;
	}

	/**
	 * Add a bunch of query parameters from a Map. This method provides
	 * compatability with the query parameters provided by
	 * javax.servlet.ServletRequest
	 * 
	 * @param params
	 * @return self
	 */
	public URLBuilder addQuery(Map<Object, Object> params) {
		for (Map.Entry<Object, Object> e : params.entrySet())
			query.add(new QueryParam(e.getKey(), e.getValue()));
		return this;
	}

	/**
	 * Add just the query part (ignore the path) from another URLBuilder and
	 * add it to this URLBuilder.
	 * 
	 * @param params
	 * @return self
	 */
	public URLBuilder addQuery(URLBuilder params) {
		query.addAll(params.query);
		return this;
	}

	/**
	 * turn this URL builder object into a URLEncoded string
	 */
	public String toString() {
		Iterator<String> pi;
		Iterator<URLBuilder.QueryParam> qi;
		StringBuilder out = new StringBuilder();

		// first the base
		if (base != null)
			out.append(base.toString());

		// then the path
		if ((pi = path.iterator()).hasNext()) {
			if (out.length() == 0) {
				// nothing there yet but we have a path; tack on a slash
				out.append(PATH_SEPARATOR);
			} else {
				// we have something already, see if it ends with a slash
				// if not, add one
				if (out.charAt(out.length() - 1) != PATH_SEPARATOR)
					out.append(PATH_SEPARATOR);
			}

			try {
				out.append(URLEncoder.encode(pi.next(), UTF8));
				while (pi.hasNext())
					out.append(PATH_SEPARATOR).append(
							URLEncoder.encode(pi.next(), UTF8));
			} catch (UnsupportedEncodingException e) {
			}
		}

		// and finally the query string
		if ((qi = query.iterator()).hasNext()) {
			out.append(QUERY_DELIMITER);
			out.append(qi.next().toString());
			while (qi.hasNext())
				out.append(QUERY_PARAM_SEPARATOR).append(qi.next().toString());
		}
		return out.toString();
	}

	/**
	 * shamelessly took the LinkTool class in Velocity Tools 1.4 and improved it
	 * http://velocity.apache.org/tools/releases/1.4/view/LinkTool.html
	 * 
	 * @version $LastChangedRevision$ <br>
	 *          $LastChangedDate$
	 * @author $LastChangedBy$
	 */
	public final class QueryParam {

		private final Object key;
		private final Object value;

		/**
		 * Create a new query parameter
		 * 
		 * @param key
		 *            query pair
		 * @param value
		 *            query value
		 */
		public QueryParam(Object key, Object value) {
			this.key = key;
			this.value = value;
		}

		/**
		 * Return the URL-encoded query string.
		 */
		public String toString() {
			StringBuilder out = new StringBuilder();
			try {
				String encodedKey = URLEncoder.encode(key.toString(), UTF8);
				if (value == null) {
					out.append(encodedKey).append(QUERY_PARAM_JOINER);

				} else if (Iterable.class.isInstance(value)) {
					// if it's something iterable, then spin through it
					Iterator<?> i;
					if ((i = ((Iterable<?>) value).iterator()).hasNext()) {
						out.append(encodedKey);
						out.append(QUERY_PARAM_JOINER);
						out.append(i.next().toString());

						while (i.hasNext()) {
							out.append(QUERY_PARAM_SEPARATOR);
							out.append(encodedKey);
							out.append(QUERY_PARAM_JOINER);
							out.append(i.next().toString());
						}
					}

				} else if (value instanceof Object[]) {
					// we'll take array's too
					Object[] array = (Object[]) value;
					for (int i = 0; i < array.length; i++) {
						out.append(encodedKey);
						out.append(QUERY_PARAM_JOINER);
						if (array[i] != null) {
							out.append(URLEncoder.encode(String
									.valueOf(array[i]), UTF8));
						}
						if (i + 1 < array.length) {
							out.append(QUERY_PARAM_SEPARATOR);
						}
					}

				} else {
					out.append(encodedKey);
					out.append(QUERY_PARAM_JOINER);
					out.append(URLEncoder.encode(String.valueOf(value), UTF8));
				}
			} catch (UnsupportedEncodingException e) {
				// make sure we return an empty string
				out = new StringBuilder();
			}

			return out.toString();
		}
	}
}
