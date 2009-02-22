
package org.jactiveresource;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.ConverterRegistry;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.DefaultConverterLookup;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class RailsConverter extends DefaultConverterLookup {

    public RailsConverter() {
    }
/*
    public void marshal( Object source, HierarchicalStreamWriter writer,
        MarshallingContext context ) {
        //super.marshal( source, writer, context );
    }

    public Object unmarshal( HierarchicalStreamReader reader,
        UnmarshallingContext context ) {
        if ( "true".equals( reader.getAttribute( "nil" ) ) ) {
            return null;
        }
        //return super.unmarshal( reader, context );
    }

    public boolean canConvert( Class arg0 ) {
        // TODO Auto-generated method stub
        return false;
    }

    public void registerConverter( Converter converter, int priority ) {
        converterLookup.registerConverter( new RailsConverter(), priority );
        
    }
*/
}
