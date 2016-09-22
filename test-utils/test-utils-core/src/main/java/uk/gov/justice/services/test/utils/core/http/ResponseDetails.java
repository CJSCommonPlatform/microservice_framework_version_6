package uk.gov.justice.services.test.utils.core.http;

import java.util.Objects;

public class ResponseDetails {

    private final int status;
    private final String responseBody;

    public ResponseDetails(final int status, final String responseBody) {
        this.status = status;
        this.responseBody = responseBody;
    }

    public int getStatus() {
        return status;
    }

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
