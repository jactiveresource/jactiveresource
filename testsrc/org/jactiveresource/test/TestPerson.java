/*
 * Copyright (c) 2008, Jared Crapo All rights reserved. Redistribution and use
 * in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: - Redistributions of source
 * code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. - Redistributions in binary form must reproduce the
 * above copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution. -
 * Neither the name of jactiveresource.org nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jactiveresource.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;

import org.jactiveresource.ResourceConnection;
import org.jactiveresource.ResourceFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */

public class TestPerson {

	private ResourceConnection c;
	// private PersonFactory pf;
	private ResourceFactory pf;
	private Person p;

	@Before
	public void setUp() throws Exception {
		c = new ResourceConnection("http://localhost:3000");
		c.setUsername("Ace");
		c.setPassword("newenglandclamchowder");
		pf = new ResourceFactory(c, Person.class);
	}

	@Test
	public void testBasicOperations() throws Exception {
		p = pf.instantiate();
		assertNull(p.getId());
		p.setName("King Tut");
		Date old = new Date(new Long("-99999999999999"));
		p.setBirthdate(old);
		p.save();

		String id = p.getId();
		assertEquals(p.getName(), "King Tut");
		assertNotNull("No id present", p.getId());

		p = pf.find(id);
		assertEquals(p.getName(), "King Tut");
		p.setName("Alexander the Great");
		p.save();

		p = pf.find(id);
		assertEquals(p.getName(), "Alexander the Great");

		assertTrue(pf.exists(id));
		p.delete();
		assertFalse(pf.exists(id));
	}

	@Test
	public void testReload() throws Exception {
		p = pf.instantiate();
		p.setName("George Burns");
		p.setBirthdate(new Date());
		p.save();

		p.setName("Fred Flintstone");
		p.reload();
		assertEquals("George Burns", p.getName());

		p.delete();
	}

	@Test
	public void testFindAll() throws Exception {
		ArrayList<Person> people, otherpeople;
		people = pf.findAll();

		p = pf.instantiate();
		p.setName("George Burns");
		p.setBirthdate(new Date());
		p.save();

		otherpeople = pf.findAll();
		assertEquals(otherpeople.size(), people.size() + 1);
		p.delete();

		otherpeople = pf.findAll();
		assertEquals(otherpeople.size(), people.size());
	}

	@Test
	public void testValidation() throws Exception {
		p = pf.instantiate();
		p.setBirthdate(new Date());
		assertFalse(p.save());
		assertNull(p.getId());
		p.setName("Shoeless Joe");
		assertTrue(p.save());
		assertNotNull(p.getId());
		p.delete();
	}
	/*
	 * @Test public void testFind() throws Exception { Person p =
	 * Person.find(c,"1"); assertEquals("King Tut",p.getName()); }
	 * 
	 * @Test public void testExists() throws Exception {
	 * assertEquals(true,Person.exists(c,"1")); }
	 * 
	 * @Test public void testUpdate() throws Exception { Person p =
	 * Person.find(c,"1"); p.setName("Alexander the Great"); p.update(c); }
	 */
}
