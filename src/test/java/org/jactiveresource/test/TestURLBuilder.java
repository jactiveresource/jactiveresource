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

package org.jactiveresource.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.jactiveresource.URLBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * tests for the URLBuilder class
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class TestURLBuilder {

	URLBuilder u, v;
	String BASE = "http://localhost:3000";
	URI base;

	@Before
	public void setup() throws Exception {
		base = new URI(BASE);
	}

	@Test
	public void constructors() throws Exception {
		u = new URLBuilder();
		assertEquals("", u.toString());

		u = new URLBuilder("people.xml");
		assertEquals("/people.xml", u.toString());

		u = new URLBuilder(base);
		assertEquals(base.toString(), u.toString());
	}

	@Test
	public void add() {

		u = new URLBuilder();
		u.add("people");
		assertEquals("add with no base is broken", "/people", u.toString());

		u.add("managers.xml");
		assertEquals("add after add is broken", "/people/managers.xml",
				u.toString());

		u = new URLBuilder();
		u.add("big people");
		assertEquals("url encoding of path elements is broken", "/big+people",
				u.toString());

		u = new URLBuilder();
		u.add("/people/managers.xml");
		assertEquals("/people/managers.xml", u.toString());

		u = new URLBuilder();
		u.add("people/managers.xml");
		assertEquals("/people/managers.xml", u.toString());
	}

	@Test
	public void setBase() throws Exception {
		base = new URI("http://www.example.com:8080/path/components?parm=value");
		u = new URLBuilder();
		u.setBase(base);
		assertEquals("http://www.example.com:8080/path/components",
				u.toString());

		base = new URI("/path/components?parm=value");
		u = new URLBuilder();
		u.setBase(base);
		assertEquals("/path/components", u.toString());

		base = new URI("/path/components?parm=value#fragment");
		u = new URLBuilder();
		u.setBase(base);
		assertEquals("/path/components", u.toString());

		base = new URI("");
		u = new URLBuilder();
		u.setBase(base);
		assertEquals("", u.toString());
	}

	@Test
	public void addWithBase() throws Exception {
		u = new URLBuilder(base);
		u.add("people.xml");
		assertEquals("add with base is broken",
				base.toString() + "/people.xml", u.toString());

	}

	@Test
	public void queryParam() {
		URLBuilder.QueryParam p;
		u = new URLBuilder();

		p = u.new QueryParam("key", "value");
		assertEquals("key=value", p.toString());

		p = u.new QueryParam("key", "=hi");
		assertEquals("url encoding of query parameters is broken", "key=%3Dhi",
				p.toString());

		ArrayList<String> list = new ArrayList<String>();
		list.add("value1");
		list.add("value2");
		p = u.new QueryParam("key", list);
		assertEquals("list of values is broken", "key=value1&key=value2",
				p.toString());

		String[] array = new String[3];
		array[0] = "value1";
		array[1] = "value2";
		array[2] = "value3";
		p = u.new QueryParam("key", array);
		assertEquals("array of values is broken",
				"key=value1&key=value2&key=value3", p.toString());
	}

	@Test
	public void queryMap() {
		HashMap<Object, Object> p = new HashMap<Object, Object>();
		p.put("position", "manager");
		p.put("salary", "60000");
		u = new URLBuilder();
		u.add("people").add("1").add("promote.xml");
		u.addQuery(p);
		assertEquals("/people/1/promote.xml?position=manager&salary=60000",
				u.toString());

	}

	@Test
	public void onlyQueryParam() {
		u = new URLBuilder();
		u.addQuery("key1", "value1");
		u.addQuery("key2", "value2");
		assertEquals("", "?key1=value1&key2=value2", u.toString());
	}

	@Test
	public void pathAndQuery() {
		u = new URLBuilder();
		u.add("people").add("1");
		u.addQuery("position", "manager");
		u.add("promote.xml");
		assertEquals("/people/1/promote.xml?position=manager", u.toString());

		u.addQuery("salary", "60000");
		assertEquals("/people/1/promote.xml?position=manager&salary=60000",
				u.toString());
	}

	@Test
	public void queryCopy() {
		u = new URLBuilder("people");
		u.add("promote.xml");
		u.addQuery("position", "manager");
		u.addQuery("salary", "60000");

		URLBuilder v = new URLBuilder("otherpeople.xml");
		v.addQuery(u);
		assertEquals("addQuery with another URLBuilder is broken",
				"/otherpeople.xml?position=manager&salary=60000", v.toString());
	}

	@Test
	public void fragment() {
		u = new URLBuilder("people.html");
		u.setFragment("dorks");
		assertEquals("/people.html#dorks", u.toString());
		u.clearFragment();
		assertEquals("/people.html", u.toString());
		try {
			u.setFragment("invalid/");
			fail("invalid fragment did not throw IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
		}
		try {
			u.setFragment("invalid?");
			fail("invalid fragment did not throw IllegalArgumentException.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void toURI() throws Exception {
		u = new URLBuilder("people");
		assertEquals(new URI("/people"), u.toURI());
	}
}
