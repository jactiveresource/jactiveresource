/*
 * 
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

public abstract class ActiveResource {

    protected static ActiveResource find( Class<?> clazz,
        Connection connection, String id ) throws SecurityException,
        NoSuchMethodException, IllegalArgumentException,
        IllegalAccessException, InvocationTargetException, HttpException,
        IOException, InterruptedException {

        Method method = clazz.getDeclaredMethod( "getCollectionName",
            new Class[] {} );
        String resource = (String) method.invoke( null, new Object[] {} );
        // TODO fix format
        String xml = connection.get( "/" + resource + "/" + id + ".xml" );
        return (ActiveResource) connection.getXStream().fromXML( xml );
    }

    protected static ArrayList<? extends ActiveResource> findAll(
        Class<? extends ActiveResource> clazz, Connection connection )
        throws SecurityException, NoSuchMethodException,
        IllegalArgumentException, IllegalAccessException,
        InvocationTargetException, HttpException, IOException, InterruptedException, ClassNotFoundException {
        Method method = clazz.getDeclaredMethod( "getCollectionName",
            new Class[] {} );
        String resource = (String) method.invoke( null, new Object[] {} );
        // TODO fix format
        BufferedReader xml = connection.getStream( "/" + resource + ".xml" );
//        String xml = connection.get( "/" + resource + ".xml" );
        
        ObjectInputStream in = connection.getXStream().createObjectInputStream(xml);
        
        ArrayList<ActiveResource> list = new ArrayList<ActiveResource>();
        while (true) {
            try {
                list.add( (ActiveResource) in.readObject() );
            } catch (EOFException e) {
              break;  
            }
        }
//        ArrayList list = new ArrayList();
        return list;
    }

    public ActiveResource() {
    }

}
