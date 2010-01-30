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

package org.jactiveresource.test;

import static org.junit.Assert.*;

import java.util.Date;

import org.jactiveresource.Connection;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */

public class TestPerson {

    private Connection c;

    @Before
    public void setUp() throws Exception {
        c = new Connection("http://localhost:3000");
        c.registerResource( Person.class );
    }

    @Test
    public void testBasicOperations() throws Exception {
        Person p = new Person();
        p.setName("King Tut");
        p.setBirthdate(new Date());
        p = p.create(c);
        String id = p.getId();
        assertEquals(p.getName(),"King Tut");
        assertNotNull("No id present",p.getId());
        
        p = Person.find(c, id);
        assertEquals(p.getName(),"King Tut");
        p.setName("Alexander the Great");
        p.update(c);
        
        p = Person.find(c, id);
        assertEquals(p.getName(),"Alexander the Great");
        
        assertTrue(Person.exists(c, id));
        p.delete(c);
        assertFalse(Person.exists(c, id));
    }
    
    /*@Test
    public void testFind() throws Exception {
    	Person p = Person.find(c,"1");
    	assertEquals("King Tut",p.getName());
    }
    
    @Test
    public void testExists() throws Exception {
    	assertEquals(true,Person.exists(c,"1"));
    }
    
    @Test
    public void testUpdate() throws Exception {
    	Person p = Person.find(c,"1");
    	p.setName("Alexander the Great");
    	p.update(c);
    }*/
}
