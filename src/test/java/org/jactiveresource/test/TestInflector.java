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
import static org.jactiveresource.rails.Inflector.*;

import org.junit.Test;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class TestInflector {

	@Test
	public void camelizeTests() {
		assertEquals("ActiveRecord", camelize("active_record"));
		assertEquals("activeRecord", camelize("active_record", false));
		assertEquals("ActiveRecord::Errors", camelize("active_record/errors"));
		assertEquals("activeRecord::Errors",
				camelize("active_record/errors", false));
	}

	@Test
	public void underscoreTests() {
		assertEquals("asset", underscore("Asset"));
		assertEquals("asset_type", underscore("AssetType"));
		assertEquals("active_record/errors", underscore("ActiveRecord::Errors"));
	}

	@Test
	public void pluralizeTests() {
		assertEquals("assets", pluralize("asset"));
		assertEquals("Assets", pluralize("Asset"));
		assertEquals("guts", pluralize("guts"));
		// assertEquals( "GUTs", pluralize( "GUTS" ) );
		assertEquals("axes", pluralize("axis"));
		assertEquals("Axes", pluralize("Axis"));
		assertEquals("calves", pluralize("calf"));
		assertEquals("halves", pluralize("half"));
		assertEquals("Halves", pluralize("Half"));
		assertEquals("AXes", pluralize("AXIS"));
		assertEquals("Hives", pluralize("Hive"));
		assertEquals("QUEries", pluralize("QUEry"));
		assertEquals("rice", pluralize("rice"));
		assertEquals("oxen", pluralize("ox"));
		assertEquals("Oxen", pluralize("Ox"));
		assertEquals("matrices", pluralize("matrix"));
		assertEquals("indices", pluralize("index"));
		assertEquals("indices", pluralize("indix"));
		assertEquals("people", pluralize("person"));
		assertEquals("People", pluralize("Person"));
		assertEquals("Kine", pluralize("Cow"));
		assertEquals("kine", pluralize("cow"));
	}

	@Test
	public void singularizeTests() {
		assertEquals("duck", singularize("ducks"));
		assertEquals("thesis", singularize("theses"));
		assertEquals("diagnosis", singularize("diagnoses"));
		assertEquals("analysis", singularize("analyses"));
		assertEquals("half", singularize("halves"));
		assertEquals("alias", singularize("aliases"));
		assertEquals("person", singularize("people"));
		assertEquals("Person", singularize("People"));
		assertEquals("cow", singularize("kine"));
		assertEquals("Cow", singularize("Kine"));
	}
}
