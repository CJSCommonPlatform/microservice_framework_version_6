package uk.gov.justice.services.test.utils.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class RestClientTest {

    private static final int PORT = 8089;
    private static final String URL = "http://localhost:" + PORT + "/test";
    private static final String CONTENT_TYPE_VALUE = "text/xml";
    private static final String REQUEST_BODY = "<request>body</request>";
    private static final String RESOURCE_PATH = "/test";
    private static final int OK_STATUS_CODE = OK.getStatusCode();
    private static final String HEADER = "Header";
    private static final String HEADER_VALUE = "HeaderValue";
    private static final String BODY_RESPONSE = "<response>Some content</response>";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    private RestClient restClient;

    @Before
    public void setup() {
        restClient = new RestClient();

        stubFor(get(urlEqualTo(RESOURCE_PATH))
                .withHeader(HEADER, equalTo(HEADER_VALUE))
                .willReturn(aResponse()
                        .withStatus(OK.getStatusCode())
                        .withHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
                        .withBody(BODY_RESPONSE)));

        stubFor(get(urlEqualTo(RESOURCE_PATH))
                .withHeader(ACCEPT, equalTo(CONTENT_TYPE_VALUE))
                .willReturn(aResponse()
                        .withStatus(OK.getStatusCode())
                        .withHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
                        .withBody(BODY_RESPONSE)));

        stubFor(post(urlEqualTo(RESOURCE_PATH))
                .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_VALUE))
                .withRequestBody(matching(REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(OK_STATUS_CODE)
                        .withHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
                        .withBody(BODY_RESPONSE)));

        stubFor(post(urlEqualTo(RESOURCE_PATH))
                .withHeader(CONTENT_TYPE, equalTo(CONTENT_TYPE_VALUE))
                .withHeader(HEADER, equalTo(HEADER_VALUE))
                .withRequestBody(matching(REQUEST_BODY))
                .willReturn(aResponse()
                        .withStatus(OK_STATUS_CODE)
                        .withHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE)
                        .withBody(BODY_RESPONSE)));
    }

    @Test
    public void shouldSendCommand() {
        Response response = restClient.postCommand(URL, CONTENT_TYPE_VALUE, REQUEST_BODY);

        assertThat(response.getStatus(), org.hamcrest.CoreMatchers.equalTo(OK_STATUS_CODE));
    }

    @Test
    public void shouldSendCommandWithHeaders() {
        Response response = restClient.postCommand(URL, CONTENT_TYPE_VALUE, REQUEST_BODY, getHeaders());

        assertThat(response.getStatus(), org.hamcrest.CoreMatchers.equalTo(OK_STATUS_CODE));
    }

    @Test
    public void shouldSendQuery() {
        Response response = restClient.query(URL, CONTENT_TYPE_VALUE);

        assertThat(response.getStatus(), org.hamcrest.CoreMatchers.equalTo(OK_STATUS_CODE));
    }

    @Test
    public void shouldSendQueryWithHeaders() {
        Response response = restClient.query(URL, CONTENT_TYPE_VALUE, getHeaders());

        assertThat(response.getStatus(), org.hamcrest.CoreMatchers.equalTo(OK_STATUS_CODE));
    }

    private MultivaluedMap<String, Object> getHeaders() {
        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.putSingle(HEADER, HEADER_VALUE);
        return headers;
    }
}