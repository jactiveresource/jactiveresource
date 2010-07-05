package org.jactiveresource;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterRegistry;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RailsConverterRegistry implements ConverterRegistry {

	public void registerConverter(final Converter converter, int priority) {

		final DefaultConverterLookup converterLookup = new DefaultConverterLookup();
		converterLookup.registerConverter(new Converter() {
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

			@SuppressWarnings("unchecked")
			public boolean canConvert(Class c) {
				return converter.canConvert(c);
			}
		}, priority);
	}
}
