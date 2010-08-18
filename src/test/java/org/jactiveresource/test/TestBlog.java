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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.TimeZone;

import org.jactiveresource.ResourceConnection;
import org.jactiveresource.rails.RailsResourceFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class TestBlog {

	private ResourceConnection c;
	private RailsResourceFactory<Post> pf;
	private Post p;
	private StringBuffer sb;
	private Calendar cal;

	@Before
	public void setUp() throws Exception {
		c = new ResourceConnection("http://localhost:3000");
		c.setUsername("Ace");
		c.setPassword("newenglandclamchowder");
		pf = new RailsResourceFactory<Post>(c, Post.class);
		pf.registerClass(Comment.class);
		TimeZone tz = TimeZone.getTimeZone("GMT");
		cal = Calendar.getInstance(tz);
	}

	@Test
	public void getPostAndComments() throws Exception {
		p = (Post) pf.find("1");
		assertEquals("first post", p.getTitle());
	}

	@Test
	public void deserialize1() throws Exception {
		p = pf.deserializeOne(serializedPost1());
		assertEquals("1", p.getId());
		assertEquals("first post", p.getTitle());
		assertNull(p.getPublishedAt());
		assertEquals(2,p.getComments().size());
		// check a comment date
		
		cal.setTime(p.getComments().get(1).getUpdatedAt());
		assertEquals(2010, cal.get(Calendar.YEAR));
		assertEquals(Calendar.JULY, cal.get(Calendar.MONTH));
		assertEquals(8, cal.get(Calendar.DAY_OF_MONTH));
		assertEquals(5, cal.get(Calendar.HOUR_OF_DAY));
		assertEquals(7, cal.get(Calendar.MINUTE));
		assertEquals(25, cal.get(Calendar.SECOND));
	}

	private String serializedPost1() {
		sb = new StringBuffer();
		sb.append("<post>");
		sb.append("<body>my very first post</body>");
		sb.append("<created-at type=\"datetime\">2010-07-08T05:05:27Z</created-at>");
		sb.append("<id type=\"integer\">1</id>");
		sb.append("<published-at type=\"datetime\" nil=\"true\"/>");
		sb.append("<title>first post</title>");
		sb.append("<updated-at type=\"datetime\">2010-07-08T05:05:27Z</updated-at>");
		sb.append("<comments type=\"array\">");
		sb.append("  <comment>");
		sb.append("  <body>the first comment on the first post</body>");
		sb.append("  <created-at type=\"datetime\">2010-07-08T05:05:48Z</created-at>");
		sb.append("  <id type=\"integer\">1</id>");
		sb.append("  <post-id type=\"integer\">1</post-id>");
		sb.append("  <published-at type=\"datetime\" nil=\"true\"/>");
		sb.append("  <updated-at type=\"datetime\">2010-07-08T05:07:08Z</updated-at>");
		sb.append("  </comment>");
		sb.append("  <comment>");
		sb.append("  <body>the second comment to the first post</body>");
		sb.append("  <created-at type=\"datetime\">2010-07-08T05:07:25Z</created-at>");
		sb.append("  <id type=\"integer\">2</id>");
		sb.append("  <post-id type=\"integer\">1</post-id>");
		sb.append("  <published-at type=\"datetime\" nil=\"true\"/>");
		sb.append("  <updated-at type=\"datetime\">2010-07-08T05:07:25Z</updated-at>");
		sb.append("  </comment>");
		sb.append("</comments>");
		sb.append("</post>");
		return sb.toString();
	}
}