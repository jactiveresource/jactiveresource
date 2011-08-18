/*

Copyright (c) 2011, Jared Crapo All rights reserved. 

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

import java.net.URL;

import org.apache.http.client.HttpClient;

/**
 * An interface that defines how to implement a http client factory, which
 * is used by the ResourceConnection class to create and destroy HttpClient
 * objects.
 *
 * The main method of this interface allows an implementation to return a
 * HttpClient object.  The interface also defines a method to shutter the
 * factory, ie a graceful way to clean up any factory resources once the
 * factory is no longer needed.  There are also convenience methods to pass
 * through user authentication information from ResourceConnection objects.
 *
 * @version $LastChangedRevision$ <br>
 *          $LastChangedDate$
 * @author $LastChangedBy$
 */
public interface AbstractHttpClientFactory {

	public HttpClient getHttpClient(URL site);
	public void shutter();

	public String getUsername();
	public void setUsername(String username);

	public String getPassword();
	public void setPassword(String password);
}
