
package org.jactiveresource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.PlainSocketFactory;
import org.apache.http.conn.Scheme;
import org.apache.http.conn.SchemeRegistry;
import org.apache.http.conn.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;

public class Connection {

    private URL site;
    private String prefix;
    private Format format;
    private XStream xstream;

    enum Format {
        XML
    };

    public Connection( String site, Format format )
        throws MalformedURLException {
        this.site = new URL( site );
        this.prefix = this.site.getPath();
        this.format = format;
        // set up xstream
        xstream = new XStream();
        xstream.registerConverter( new ISO8601DateConverter() );
        init();
    }

    public String getSite() {
        return this.site.toString();
    }

    public Format getFormat() {
        return this.format;
    }

    public XStream getXStream() {
        return xstream;
    }

    public void registerResource( Class<?> clazz ) {
        Method method;
        try {
            method = clazz.getDeclaredMethod( "setConnection",
                new Class[] { Connection.class } );
            method.invoke( null, new Object[] { this } );
            // add the item to our stream parser
            xstream.processAnnotations( clazz );
        } catch ( SecurityException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( NoSuchMethodException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IllegalArgumentException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InvocationTargetException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // "#{prefix(prefix_options)}#{collection_name}/#{id}.#{format.extension}#{
    // query_string(query_options)}"

    public String get( String URL ) throws HttpException, IOException,
        InterruptedException {

        HttpHost host = new HttpHost( site.getHost(), site.getPort(), site
            .getProtocol() );
        HttpClient client = createHttpClient();
        HttpRequest request = createRequest( this.prefix + URL );
        HttpEntity entity = null;
        try {
            HttpResponse response = client.execute( host, request, null );
            entity = response.getEntity();
            StringBuffer sb = new StringBuffer();
            BufferedReader reader = new BufferedReader( new InputStreamReader(
                entity.getContent() ) );

            int c;
            while ( ( c = reader.read() ) != -1 )
                sb.append( (char) c );

            return sb.toString();
        } finally {
            // if there is no entity, the connection is already released
            if ( entity != null )
                entity.consumeContent(); // release connection gracefully
        }
    }

    public BufferedReader getStream( String URL ) throws HttpException,
        IOException, InterruptedException {
        HttpHost host = new HttpHost( site.getHost(), site.getPort(), site
            .getProtocol() );
        HttpClient client = createHttpClient();
        HttpRequest request = createRequest( this.prefix + URL );
        HttpEntity entity = null;
//        try {
            HttpResponse response = client.execute( host, request, null );
            entity = response.getEntity();
            StatusLine status = response.getStatusLine();
            status.
            
            BufferedReader reader = new BufferedReader( new InputStreamReader(
                entity.getContent() ) );

            return reader;
//        } finally {
//            // if there is no entity, the connection is already released
//            if ( entity != null )
//                entity.consumeContent(); // release connection gracefully
//        }

    }

    private final HttpRequest createRequest( String path ) {

        HttpRequest req = new BasicHttpRequest( "GET", path,
            HttpVersion.HTTP_1_1 );
        // ("OPTIONS", "*", HttpVersion.HTTP_1_1);

        return req;
    }

    private HttpClient createHttpClient() {

        ClientConnectionManager ccm = new ThreadSafeClientConnManager(
            getParams(), supportedSchemes );
        // new SingleClientConnManager(getParams(), supportedSchemes);

        DefaultHttpClient dhc = new DefaultHttpClient( ccm, getParams() );

        return dhc;
    }

    private final HttpParams getParams() {
        return defaultParameters;
    }

    /**
     * The default parameters. Instantiated in {@link #setup setup}.
     */
    private static HttpParams defaultParameters = null;

    /**
     * The scheme registry. Instantiated in {@link #setup setup}.
     */
    private static SchemeRegistry supportedSchemes;

    private void init() {
        supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register( new Scheme( "http", sf, 80 ) );

        // prepare parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );
        HttpProtocolParams.setContentCharset( params, "UTF-8" );
        HttpProtocolParams.setUseExpectContinue( params, true );
        defaultParameters = params;
    }
}
