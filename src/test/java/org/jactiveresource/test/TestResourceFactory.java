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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import org.jactiveresource.ResourceFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class TestResourceFactory {

	private ResourceFactory f;
	private StringBuilder sb;
	private Person p;
	private Calendar cal;

	@Before
	public void setUp() throws Exception {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		cal = Calendar.getInstance(tz);
		f = new ResourceFactory(null, Person.class);
	}

	@Test
	public void testDeserializeOne1() throws IOException {
		p = f.deserializeOne(serializedPerson1());
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
	public void testDeserializeOne2() throws IOException {
		p = f.deserializeOne(serializedPerson2());
		assertEquals("2", p.getId());
		assertNull(p.getName());
		assertNull(p.getBirthdate());
		assertNull(p.getCreatedAt());
		assertNull(p.getUpdatedAt());
	}

	@Test
	public void testDeserializeOne3() throws IOException {
		p = f.deserializeOne(serializedPerson3());
		assertEquals("3", p.getId());
	}

	/*
	 * a person with all fields present and valued
	 */
	private String serializedPerson1() {
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
	private String serializedPerson2() {
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
	private String serializedPerson3() {
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

}
