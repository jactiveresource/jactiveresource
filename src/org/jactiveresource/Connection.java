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

package org.jactiveresource;

import static org.jactiveresource.Inflector.dasherize;
import static org.jactiveresource.Inflector.singularize;
import static org.jactiveresource.Inflector.underscore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.extended.ISO8601DateConverter;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public class Connection {

    private URL site;
    private Format format;
    private XStream xstream;

    private Format defaultFormat = Format.XML;

    private ClientConnectionManager connectionManager;
    private DefaultHttpClient httpclient;

    public Connection( URL site ) {
        this.site = site;
        init( Format.XML );
    }

    public Connection( String site ) throws MalformedURLException {
        this.site = new URL( site );
        init( defaultFormat );
    }

    public Connection( URL site, Format format ) {
        this.site = site;
        init( format );
    }

    public Connection( String site, Format format )
        throws MalformedURLException {
        this.site = new URL( site );
        init( format );
    }

    public URL getSite() {
        return this.site;
    }

    public Format getFormat() {
        return this.format;
    }

    public XStream getXStream() {
        return xstream;
    }

    /**
     * register a resource with this connection
     * 
     * @param <T>
     * @param clazz
     */
    public void registerResource( Class<? extends ActiveResource> clazz ) {

        String xmlname = singularize( dasherize( ActiveResource
            .getCollectionName( clazz ) ) );
        xstream.alias( xmlname, clazz );

        Field[] fields = clazz.getDeclaredFields();
        for ( Field field : fields ) {
            xmlname = dasherize( underscore( field.getName() ) );
            xstream.aliasField( xmlname, clazz, field.getName() );
        }

        // xstream.processAnnotations( clazz );

    }

    // "#{prefix(prefix_options)}#{collection_name}/#{id}.#{format.extension}#{
    // query_string(query_options)}"

    public String get( String url ) throws HttpException, IOException,
        InterruptedException, URISyntaxException {

        HttpClient client = createHttpClient( this.getSite() );
        HttpGet request = new HttpGet( this.getSite().toString() + url );
        HttpEntity entity = null;
        try {
            HttpResponse response = client.execute( request );
            checkStatus( response );
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

    public BufferedReader getStream( String url ) throws HttpException,
        IOException, InterruptedException, URISyntaxException {

        HttpClient client = createHttpClient( this.getSite() );
        HttpGet request = new HttpGet( this.getSite().toString() + url );

        HttpEntity entity = null;
        HttpResponse response = client.execute( request );
        checkStatus( response );
        entity = response.getEntity();

        BufferedReader reader = new BufferedReader( new InputStreamReader(
            entity.getContent() ) );
        return reader;
    }

    public void put( String url, String body ) throws URISyntaxException,
        HttpException, IOException, InterruptedException {

        HttpClient client = createHttpClient( this.getSite() );
        HttpPut request = new HttpPut( this.getSite().toString() + url );
        StringEntity entity = new StringEntity( body );
        request.setEntity( entity );
        HttpResponse response = client.execute( request );
        checkStatus( response );
    }

    private final void checkStatus( HttpResponse response ) throws ClientError,
        ServerError {

        int status = response.getStatusLine().getStatusCode();
        if ( status == 400 )
            throw new BadRequest();
        else if ( status == 401 )
            throw new UnauthorizedAccess();
        else if ( status == 403 )
            throw new ForbiddenAccess();
        else if ( status == 404 )
            throw new ResourceNotFound();
        else if ( status == 405 )
            throw new MethodNotAllowed();
        else if ( status == 409 )
            throw new ResourceConflict();
        else if ( status == 422 )
            throw new ResourceInvalid();
        else if ( status >= 401 && status <= 499 )
            throw new ClientError();
        else if ( status >= 500 && status <= 599 )
            throw new ServerError();
    }

    /*
     * create or retrieve a cached HttpClient object
     */
    private HttpClient createHttpClient( URL site ) {

        //if ( this.connectionManager == null ) {
            this.connectionManager = new ThreadSafeClientConnManager(
                getParams(), supportedSchemes );
        //}

        //if ( this.httpclient == null ) {
            this.httpclient = new DefaultHttpClient( this.connectionManager,
                getParams() );
            String userinfo = site.getUserInfo();
            if ( userinfo != null ) {
                int pos = userinfo.indexOf( ":" );
                if ( pos > 0 ) {
                    this.httpclient.getCredentialsProvider().setCredentials(
                        new AuthScope( site.getHost(), site.getPort() ),
                        new UsernamePasswordCredentials( userinfo.substring( 0,
                            pos ), userinfo.substring( pos + 1 ) ) );
                }
            }
        //}
        return this.httpclient;
    }

    private final HttpParams getParams() {
        return defaultParameters;
    }

    /**
     * The default parameters. Instantiated in {@link #init setup}.
     */
    private static HttpParams defaultParameters = null;

    /**
     * The scheme registry. Instantiated in {@link #init setup}.
     */
    private static SchemeRegistry supportedSchemes;

    private void init( Format format ) {
        this.format = format;
        // set up xstream
        // final RailsConverter rc = new RailsConverter();

        // XStream xstream = new XStream(null, new XppDriver(), new
        // ClassLoaderReference(new
        // CompositeClassLoader()), null, rc, rc );

        xstream = new XStream();
        xstream.registerConverter( new ISO8601DateConverter() );

        supportedSchemes = new SchemeRegistry();

        // Register the "http" protocol scheme, it is required
        // by the default operator to look up socket factories.
        SocketFactory sf = PlainSocketFactory.getSocketFactory();
        supportedSchemes.register( new Scheme( "http", sf, 80 ) );

        // prepare parameters
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion( params, HttpVersion.HTTP_1_1 );
        HttpProtocolParams.setContentCharset( params, "UTF-8" );
        // HttpProtocolParams.setUseExpectContinue( params, true );
        ConnManagerParams.setMaxTotalConnections( params, 400 );
        defaultParameters = params;

    }

}