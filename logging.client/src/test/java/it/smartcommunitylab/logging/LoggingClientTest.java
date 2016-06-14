package it.smartcommunitylab.logging;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import it.smartcommunitylab.logging.model.Counter;
import it.smartcommunitylab.logging.model.LogMsg;
import it.smartcommunitylab.logging.model.Pagination;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class LoggingClientTest {

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8888);

	private LoggingClient loggingClient = LoggingClient.logClient("http://127.0.0.1:8888","");
	private static final String APPID = "testApp";

	@Test
	public void log() {
		stubFor(post(urlMatching("/log/" + APPID)).willReturn(
				aResponse().withStatus(200)));

		LogMsg payload = new LogMsg();
		payload.setMsg("test msg");
		Assert.assertTrue(loggingClient.log(APPID, payload));
	}

	@Test
	public void logFailure() {
		stubFor(post(urlMatching("/log/" + APPID)).willReturn(
				aResponse().withStatus(500)));

		Assert.assertFalse(loggingClient.log(APPID, null));
	}

	@Test
	public void read() throws JsonProcessingException {
		Pagination result = new Pagination();
		result.setTotalResults(5);
		result.setLimit(2);
		result.setOffset(0);

		List<LogMsg> res = new ArrayList<LogMsg>();
		for (int i = 0; i < 2; i++) {
			LogMsg m = new LogMsg();
			m.setAppId(APPID);
			m.setMsg("text_" + System.currentTimeMillis());
			m.setType("MyLogType");
			m.setTimestamp(System.currentTimeMillis());
			res.add(m);
		}
		result.setData(res);
		ObjectMapper mapper = new ObjectMapper();
		stubFor(get(urlMatching("/log/" + APPID)).willReturn(
				aResponse().withBody(mapper.writeValueAsString(result))
						.withStatus(200)
						.withHeader("Content-Type", "application/json")));

		Assert.assertEquals(
				2,
				loggingClient
						.readLogs(APPID, null, null, null, null, null, null,
								null).getData().size());
		Assert.assertEquals(
				5,
				loggingClient
						.readLogs(APPID, null, null, null, null, null, null,
								null).getTotalResults().intValue());

	}

	@Test
	public void count() throws JsonProcessingException {
		Counter c = new Counter();
		c.setTotalResults(15);

		ObjectMapper mapper = new ObjectMapper();
		stubFor(get(urlMatching("/log/" + APPID)).willReturn(
				aResponse().withBody(mapper.writeValueAsString(c))
						.withStatus(200)
						.withHeader("Content-Type", "application/json")));

		Assert.assertEquals(
				15,
				loggingClient
						.count(APPID, null, null, null, null, null, null, null)
						.getTotalResults().intValue());

	}
}
