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

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.HttpException;
import org.apache.http.client.ClientProtocolException;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * an abstract class which you can subclass to easily connect to RESTful
 * resources provided by Ruby on Rails
 * 
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public abstract class ActiveResource {

	// omit this field from serialization or you get tons of errors
	@XStreamOmitField
	private ResourceFactory factory;

	void setFactory(ResourceFactory factory) {
		this.factory = factory;
	}

	/**
	 * create a new resource
	 * 
	 * @throws ClientProtocolException
	 * @throws ClientError
	 * @throws ServerError
	 * @throws IOException
	 */
	public void create() throws ClientProtocolException, ClientError,
			ServerError, IOException {
		factory.create(this);
	}

	/**
	 * save the resource
	 * 
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void update() throws URISyntaxException, HttpException, IOException,
			InterruptedException {
		factory.update(this);
	}

	/**
	 * create the record if it doesn't exist on the server, or update it if it
	 * already exists
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws HttpException
	 * @throws InterruptedException
	 */
	public void save() throws ClientProtocolException, IOException,
			URISyntaxException, HttpException, InterruptedException {
		factory.save(this);
	}

	/**
	 * repopulate this object with data retrieved from the remote service
	 * 
	 * @throws HttpException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 */
	public void reload() throws HttpException, IOException,
			InterruptedException, URISyntaxException {
		factory.reload(this);
	}

	/**
	 * delete this resource from the server
	 * 
	 * @throws ClientError
	 * @throws ServerError
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void delete() throws ClientError, ServerError,
			ClientProtocolException, IOException {
		factory.delete(this);
	}

	/**
	 * synonym for delete
	 * 
	 * @throws ClientError
	 * @throws ServerError
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public void destroy() throws ClientError, ServerError,
			ClientProtocolException, IOException {
		delete();
	}

	/**
	 * returns true if all of the following are true:
	 * <ul>
	 * <li>the resource was not created from the server</li>
	 * <li>the resource has not yet been saved to the server</li>
	 * </ul>
	 * 
	 * @return true if the record hasn't been saved yet
	 */
	public boolean isNew() {
		return getId() == null;
	}

	public abstract String getId();

	public abstract void setId(String id);
}
