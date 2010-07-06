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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterLookup;
import com.thoughtworks.xstream.converters.ConverterRegistry;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.PrioritizedList;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * A hack of <code>com.thoughtworks.xstream.core.DefaultConverterLookup</code>
 * 
 * We wrap all converters passed to <code>registerConverter</code> so that we
 * can check for the rails nil attribute on any field and return null instead of
 * an empty string.
 * 
 * @version $LastChangedRevision: 75 $ <br>
 *          $LastChangedDate: 2010-07-05 19:07:17 -0700 (Mon, 05 Jul 2010) $
 * @author $LastChangedBy: jared $
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RailsConverterLookup implements ConverterLookup, ConverterRegistry {

	private final PrioritizedList converters = new PrioritizedList();
	private transient Map typeToConverterMap = Collections
			.synchronizedMap(new HashMap());

	public RailsConverterLookup() {
	}

	public Converter lookupConverterForType(Class type) {
		Converter cachedConverter = (Converter) typeToConverterMap.get(type);
		if (cachedConverter != null)
			return cachedConverter;
		Iterator iterator = converters.iterator();
		while (iterator.hasNext()) {
			Converter converter = (Converter) iterator.next();
			if (converter.canConvert(type)) {
				typeToConverterMap.put(type, converter);
				return converter;
			}
		}
		throw new ConversionException("No converter specified for " + type);
	}

	public void registerConverter(final Converter converter, int priority) {
		// wrap each converter before registering it
		converters.add(new Converter() {
			public void marshal(Object source, HierarchicalStreamWriter writer,
					MarshallingContext context) {
				converter.marshal(source, writer, context);
			}

			public Object unmarshal(HierarchicalStreamReader reader,
					UnmarshallingContext context) {
				if ("true".equals(reader.getAttribute("nil"))) {
					return null;
				}
				return converter.unmarshal(reader, context);
			}

			public boolean canConvert(Class c) {
				return converter.canConvert(c);
			}

		}, priority);
		for (Iterator iter = this.typeToConverterMap.keySet().iterator(); iter
				.hasNext();) {
			Class type = (Class) iter.next();
			if (converter.canConvert(type)) {
				iter.remove();
			}
		}
	}

}
