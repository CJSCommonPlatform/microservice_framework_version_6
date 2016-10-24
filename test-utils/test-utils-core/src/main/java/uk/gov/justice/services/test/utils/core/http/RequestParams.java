package uk.gov.justice.services.test.utils.core.http;

import java.util.Objects;

import javax.ws.rs.core.MultivaluedMap;

public class RequestParams {

    private final String url;
    private final String mediaType;
    private final MultivaluedMap<String, Object> headers;

    public RequestParams(
            final String url,
            final String mediaType,
            final MultivaluedMap<String, Object> headers) {
        this.url = url;
        this.mediaType = mediaType;
        this.headers = headers;
    }

    /**
     * @return The url of the rest end point
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the media/content type of the rest call
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * @return a map of header names/values for adding to the request
     */
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestParams that = (RequestParams) o;
        return Objects.equals(getUrl(), that.getUrl()) &&
                Objects.equals(getMediaType(), that.getMediaType()) &&
                Objects.equals(headers, that.headers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl(), getMediaType(), headers);
    }

    @Override
    public String toString() {
        return "RequestParams{" +
                "url='" + url + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", headers=" + headers +
                '}';
    }
}
