package uk.gov.justice.services.test.utils.core.http;

import java.util.Objects;

import javax.ws.rs.core.Response.Status;

/**
 * The response body and status of an HTTP call
 */
public class ResponseData {

    private final Status status;
    private final String payload;

    public ResponseData(final Status status, final String payload) {
        this.status = status;
        this.payload = payload;
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
    public String getPayload() {
        return payload;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ResponseData that = (ResponseData) o;
        return getStatus() == that.getStatus() &&
                Objects.equals(getPayload(), that.getPayload());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStatus(), getPayload());
    }

    /**
     * Used by matchers to report on failure
     *
     * @return string representation of the http response
     */
    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", payload='" + payload + '\'' +
                '}';
    }
}
