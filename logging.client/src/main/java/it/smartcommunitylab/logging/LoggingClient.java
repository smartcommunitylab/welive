/**
 *    Copyright 2015 Fondazione Bruno Kessler
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package it.smartcommunitylab.logging;

import it.smartcommunitylab.logging.model.Counter;
import it.smartcommunitylab.logging.model.LogMsg;
import it.smartcommunitylab.logging.model.Pagination;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response.Status;

public class LoggingClient {

	private final String endpoint;

	public LoggingClient(final String endpoint) {
		this.endpoint = endpoint;
	}

	private WebTarget createEndpoint() {
		Client client = ClientBuilder.newClient();
		return client.target(endpoint);
	}

	/**
	 * 
	 * @param appId
	 *            required, application identifier
	 * @param payload
	 *            required, object {@link LogMsg}
	 * @return true if operation has gone fine, false otherwise
	 */
	public boolean log(String appId, LogMsg payload) {
		if (appId != null) {
			return createEndpoint().path("log").path(appId).request()
					.post(Entity.json(payload)).getStatus() == Status.OK
					.getStatusCode();
		} else {
			throw new IllegalArgumentException("appId cannot be null");
		}
	}

	/**
	 * 
	 * @param appId
	 *            required, application identifier
	 * @param from
	 *            optional, Timerange start. Express it in millis
	 * @param to
	 *            optional, Timerange end. Express it in millis
	 * @param type
	 *            optional, log type to search
	 * @param pattern
	 *            optional, search criteria on custom fields using Lucene
	 *            syntax. Put in logical AND clause with msgPattern if present
	 * @param msgPattern
	 *            optional, search the pattern in log text. Put in logical AND
	 *            clause with pattern if present.
	 * @param limit
	 *            optional, maximum number of messages to return. Default value
	 *            is 150
	 * @param offset
	 *            optional, index of first message to return. Default value is 0
	 * @return
	 * 
	 * 
	 */
	public Pagination readLogs(String appId, Long from, Long to, String type,
			String pattern, String msgPattern, Integer limit, Integer offset) {
		if (appId != null) {
			WebTarget endpoint = createEndpoint().path("log").path(appId);
			endpoint = pushRequestParams(endpoint, from, to, type, pattern,
					msgPattern, limit, offset);
			return endpoint.request().get(Pagination.class);
		} else {
			throw new IllegalArgumentException("appId cannot be null");
		}
	}

	/**
	 * 
	 * @param appId
	 *            required, application identifier
	 * @param from
	 *            optional, Timerange start. Express it in millis
	 * @param to
	 *            optional, Timerange end. Express it in millis
	 * @param type
	 *            optional, log type to search
	 * @param pattern
	 *            optional, search criteria on custom fields using Lucene
	 *            syntax. Put in logical AND clause with msgPattern if present
	 * @param msgPattern
	 *            optional, search the pattern in log text. Put in logical AND
	 *            clause with pattern if present.
	 * @param limit
	 *            optional, maximum number of messages to return. Default value
	 *            is 150
	 * @param offset
	 *            optional, index of first message to return. Default value is 0
	 * @return
	 * 
	 * 
	 */

	public Counter count(String appId, Long from, Long to, String type,
			String pattern, String msgPattern, Integer limit, Integer offset) {
		if (appId != null) {
			WebTarget endpoint = createEndpoint().path("log").path(appId);
			endpoint = pushRequestParams(endpoint, from, to, type, pattern,
					msgPattern, limit, offset);
			return endpoint.request().get(Counter.class);
		} else {
			throw new IllegalArgumentException("appId cannot be null");
		}
	}

	private WebTarget pushRequestParams(final WebTarget endpoint, Long from,
			Long to, String type, String pattern, String msgPattern,
			Integer limit, Integer offset) {
		// null value removed automatically from method
		return endpoint.queryParam("from", from).queryParam("to", to)
				.queryParam("type", type).queryParam("pattern", pattern)
				.queryParam("msgPattern", msgPattern)
				.queryParam("limit", limit).queryParam("offset", offset);

	}
}
