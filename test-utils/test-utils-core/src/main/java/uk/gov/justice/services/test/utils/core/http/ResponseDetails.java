package uk.gov.justice.services.test.utils.core.http;

import java.util.Objects;

import javax.ws.rs.core.Response.Status;

/**
 * The response body and status of an HTTP call
 */
public class ResponseDetails {

    private final Status status;
    private final String responseBody;

    public ResponseDetails(final Status status, final String responseBody) {
        this.status = status;
        this.responseBody = responseBody;
    }

    /**
     * @return the HTTP status of the response
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @return the response body
     */
    public String getResponseBody() {
        return responseBody;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ResponseDetails that = (ResponseDetails) o;
        return getStatus() == that.getStatus() &&
                Objects.equals(getResponseBody(), that.getResponseBody());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getResponseBody());
    }

    @Override
    public String toString() {
        return "ResponseDetails{" +
                "status=" + status +
                ", responseBody='" + responseBody + '\'' +
                '}';
    }
}
