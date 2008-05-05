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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.HttpException;
import org.jactiveresource.annotation.CollectionName;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public abstract class ActiveResource {

    @SuppressWarnings("unchecked")
    protected static <T extends ActiveResource> T find( Class<T> clazz,
        Connection c, String id ) throws HttpException, IOException,
        InterruptedException, URISyntaxException {

        String collection = getCollectionName( clazz );
        String u = "/" + collection + "/" + id + c.getFormat().extension();
        String xml = c.get( u );
        return deserialize( clazz, c, xml );
    }

    @SuppressWarnings("unchecked")
    protected static <T extends ActiveResource> ArrayList<T> findAll(
        Class<T> clazz, Connection c ) throws HttpException, IOException,
        InterruptedException, ClassNotFoundException, URISyntaxException {

        String collection = getCollectionName( clazz );
        BufferedReader xml = c.getStream( "/" + collection
            + c.getFormat().extension() );
        ObjectInputStream in = c.getXStream().createObjectInputStream( xml );

        ArrayList<T> list = new ArrayList<T>();
        while ( true ) {
            try {
                list.add( (T) in.readObject() );
            } catch ( EOFException e ) {
                break;
            }
        }
        return list;
    }

    /*
     * protected static <T extends ActiveResource> boolean exists( Class<T>
     * clazz, Connection connection, String id ) { return false; }
     */

    /*
     * protected static <T extends ActiveResource> void delete( Class<T> clazz,
     * Connection connection, String id ) { }
     */

    @SuppressWarnings("unchecked")
    protected static <T extends ActiveResource> T deserialize( Class<T> clazz,
        Connection c, String xml ) {
        return (T) c.getXStream().fromXML( xml );
    }

    /**
     * guess the collection name for a particular class. Can be overridden by
     * with a CollectionName annotation.
     * 
     * @param className
     * @return
     */
    protected static String getCollectionName(
        Class<? extends ActiveResource> clazz ) {
        String name;

        CollectionName cn = clazz.getAnnotation( CollectionName.class );
        if ( cn != null ) {
            name = cn.value();
        } else {
            name = Inflector.underscore( clazz.getSimpleName() );
            name = Inflector.pluralize( name );
        }
        return name;
    }

    /**
     * guess the collection name for an instance of a subclass of
     * ActiveResource. Calls the static getCollectionName() method.
     * 
     * @return
     */
    protected String getCollectionName() {
        return ActiveResource.getCollectionName( this.getClass() );
    }

    /**
     * serialize this object
     * 
     * @param c
     * @return
     */
    public String serialize( Connection c ) {
        return c.getXStream().toXML( this );
    }

    public boolean isNew() {
        // TODO ticket:1
        return true;
    }

    /**
     * save this resource to the specified connection
     * 
     * @param c
     */
    public void save( Connection c ) {
        if ( isNew() )
            create( c );
        else
            update( c );
    }

    public void create( Connection c ) {
        // do a post
    }

    public void update( Connection c ) {
        // do a put
    }

    public void delete( Connection c ) {

    }

    public void destroy( Connection c ) {
        delete( c );
    }

}
