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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.http.HttpException;

/**
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public abstract class ActiveResource {

    @SuppressWarnings("unchecked")
    protected static <T extends ActiveResource> T find( Class<T> clazz,
        Connection connection, String id ) throws SecurityException,
        NoSuchMethodException, IllegalArgumentException,
        IllegalAccessException, InvocationTargetException, HttpException,
        IOException, InterruptedException, UnauthorizedException,
        ResourceNotFoundException, ResourceConflictException, ClientError,
        ServerError {

        Method method = clazz.getDeclaredMethod( "getCollectionName",
            new Class[] {} );
        String resource = (String) method.invoke( null, new Object[] {} );
        // TODO fix format
        String xml = connection.get( "/" + resource + "/" + id + ".xml" );
        return (T) connection.getXStream().fromXML( xml );
    }

    @SuppressWarnings("unchecked")
    protected static <T extends ActiveResource> ArrayList<T> findAll(
        Class<T> clazz, Connection connection)
        throws SecurityException, NoSuchMethodException,
        IllegalArgumentException, IllegalAccessException,
        InvocationTargetException, HttpException, IOException,
        InterruptedException, ClassNotFoundException, UnauthorizedException,
        ResourceNotFoundException, ResourceConflictException, ClientError,
        ServerError {

        Method method = clazz.getDeclaredMethod( "getCollectionName",
            new Class[] {} );
        String resource = (String) method.invoke( null, new Object[] {} );
        // TODO fix format
        BufferedReader xml = connection.getStream( "/" + resource + ".xml" );
        ObjectInputStream in = connection.getXStream().createObjectInputStream(
            xml );

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

    public ActiveResource() {
    }

}
