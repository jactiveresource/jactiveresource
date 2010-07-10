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
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.jactiveresource.ResourceFormat;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class TestPersonFactory {

	private PersonFactory xf, jf;
	private StringBuilder sb;
	private Person p;
	private Calendar cal;

	@Before
	public void setUp() throws Exception {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		cal = Calendar.getInstance(tz);
		xf = new PersonFactory(null, ResourceFormat.XML);
		jf = new PersonFactory(null, ResourceFormat.JSON);
	}

	@Test
	public void deserialize1XML() throws IOException {
		p = xf.deserializeOne(person1XML());
		assertEquals("1", p.getId());
		assertEquals("Alexander the Great", p.getName());
		// check the birthdate
		cal.setTime(p.getBirthdate());
		assertEquals(2010, cal.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
		assertEquals(29, cal.get(Calendar.DAY_OF_MONTH));
		// check the created-at datetime
		cal.setTime(p.getCreatedAt());
		assertEquals(2010, cal.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
		assertEquals(29, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals(33, cal.get(Calendar.MINUTE));
		assertEquals(47, cal.get(Calendar.SECOND));
	}

	@Test
	public void deserialize2XML() throws IOException {
		p = xf.deserializeOne(person2XML());
		assertEquals("2", p.getId());
		assertNull(p.getName());
		assertNull(p.getBirthdate());
		assertNull(p.getCreatedAt());
		assertNull(p.getUpdatedAt());
	}

	@Test
	public void deserialize3XML() throws IOException {
		p = xf.deserializeOne(person3XML());
		assertEquals("3", p.getId());
	}

	/*
	 * a person with all fields present and valued
	 */
	private String person1XML() {
		sb = new StringBuilder();
		sb.append("<person>");
		sb.append("<birthdate type=\"date\">2010-01-29</birthdate>");
		sb.append("<created-at type=\"datetime\">2010-01-29T18:33:47Z</created-at>");
		sb.append("<id type=\"integer\">1</id>");
		sb.append("<name>Alexander the Great</name>");
		sb.append("<updated-at type=\"datetime\">2010-01-30T05:41:38Z</updated-at>");
		sb.append("</person>");
		return sb.toString();
	}

	/*
	 * a person with a null birthdate and name
	 */
	private String person2XML() {
		sb = new StringBuilder();
		sb.append("<person>");
		sb.append("<birthdate type=\"date\" nil=\"true\" />");
		sb.append("<id type=\"integer\">2</id>");
		sb.append("<name nil=\"true\"/>");
		sb.append("</person>");
		return sb.toString();
	}

	/*
	 * a person with a null birthdate and name
	 */
	private String person3XML() {
		sb = new StringBuilder();
		sb.append("<person>");
		sb.append("<birthdate type=\"date\" nil=\"true\" />");
		sb.append("<created-at type=\"datetime\">2010-02-01T05:23:39Z</created-at>");
		sb.append("<id type=\"integer\">3</id>");
		sb.append("<name nil=\"true\"/>");
		sb.append("<updated-at type=\"datetime\">2010-02-01T05:23:39Z</updated-at>");
		sb.append("</person>");
		return sb.toString();
	}

	@Test
	public void deserialize1JSON() throws IOException {
		p = jf.deserializeOne(alexanderJSON());
		assertEquals("1", p.getId());
		assertEquals("Alexander the Great", p.getName());
		// check the birthdate
		cal.setTime(p.getBirthdate());
		assertEquals(2010, cal.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
		assertEquals(29, cal.get(Calendar.DAY_OF_MONTH));
		// check the created-at datetime
		cal.setTime(p.getCreatedAt());
		assertEquals(2010, cal.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, cal.get(Calendar.MONTH));
		assertEquals(29, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(18, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals(33, cal.get(Calendar.MINUTE));
		assertEquals(47, cal.get(Calendar.SECOND));
	}

	/*
	 * we can't deserialize a list of JSON objects yet.
	 *  
	@Test
	public void deserializePeopleJSON() throws IOException {
		// p = jf.deserializeOne(peopleJSON());

		BufferedReader br = new BufferedReader(new StringReader(peopleJSON()));
		ArrayList<Person> people = jf.deserializeMany(br);
		assertEquals(2, people.size());
	}

	@Test
	public void serializePeopleJSON() throws Exception {
		ArrayList<Person> people = new ArrayList<Person>();
		p = jf.instantiate();
		p.setName("Alexander the Great");
		p.setBirthdate(new Date());
		people.add(p);
		p = jf.instantiate();
		p.setName("Saladin");
		p.setBirthdate(new Date());
		people.add(p);
		String stuff = jf.serializeMany(people);
		assertEquals("hi", stuff);
	}
	 */

	private String alexanderJSON() {
		sb = new StringBuilder();
		sb.append("{\"person\":{");
		sb.append("  \"birthdate\":\"2010-01-29\",");
		sb.append("  \"created_at\":\"2010-01-29T18:33:47Z\",");
		sb.append("  \"id\":1,");
		sb.append("  \"name\":\"Alexander the Great\",");
		sb.append("  \"updated_at\":\"2010-01-30T05:41:38Z\"");
		sb.append("}}");
		return sb.toString();
	}

	private String peopleJSON() {
		sb = new StringBuilder();
		sb.append("{ \"people\": {");
		sb.append("{\"person\":{");
		sb.append("  \"birthdate\":\"2010-01-29\",");
		sb.append("  \"created_at\":\"2010-01-29T18:33:47Z\",");
		sb.append("  \"id\":1,");
		sb.append("  \"name\":\"Alexander the Great\",");
		sb.append("  \"updated_at\":\"2010-01-30T05:41:38Z\"");
		sb.append("}}");
		sb.append("}},");
		sb.append("{\"person\":{");
		sb.append("  \"birthdate\":\"2010-01-29\",");
		sb.append("  \"created_at\":\"2010-01-29T18:33:47Z\",");
		sb.append("  \"id\":1,");
		sb.append("  \"name\":\"Saladin\",");
		sb.append("  \"updated_at\":\"2010-01-30T05:41:38Z\"");
		sb.append("}}");
		sb.append("]");
		return sb.toString();
	}
}
