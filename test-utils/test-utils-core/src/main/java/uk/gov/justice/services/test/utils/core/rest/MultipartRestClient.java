package uk.gov.justice.services.test.utils.core.rest;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA_TYPE;
import static uk.gov.justice.services.test.utils.core.rest.ResteasyClientBuilderFactory.clientBuilder;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;

/**
 * Simple client for performing muti-part rest requests (i.e. for file upload)
 *
 * To Use:
 *
 * <pre>
 *     <blockquote>
 *          final MultipartRestClient multipartRestClient = new MultipartRestClient();
 *          multipartRestClient.create();
 *
 *          final MultipartFormDataOutput multipartFormDataOutput = new MultipartFormDataOutput();
 *          multipartFormDataOutput.addFormData("inputPartName", inputStream, mediaType, fileName);
 *
 *         final Response response = multipartRestClient.postMultipart(
 *                                                          multipartFormDataOutput,
 *                                                          url,
 *                                                          headers);
 *         ...
 *
 *         final Response response = multipartRestClient.getFile(url)
 *         final InputStream inputStream = response.readEntity(InputStream.class);
 *
 *         ...
 *
 *         multipartFormDataOutput.close();
 *
 *     </blockquote>
 * </pre>
 *
 * NB: This class is {@link AutoCloseable} to allow for use in a 'try with resources' statement
 */
public class MultipartRestClient implements AutoCloseable {

    private ResteasyClient resteasyClient;

    /**
     * Creates a MultipartRestClient
     */
    public void create() {
        resteasyClient = clientBuilder().build();
    }

    /**
     * Posts the form data to the specified url
     *
     * @param multipartFormDataOutput   The form data containing the {@link java.io.InputStream}
     *                                  from the file to upload
     * @param url                       The Url to post to
     * @param headers                   Any required headers
     *
     * @return                          The HTTP response for the post
     */
    public Response postMultipart(
            final MultipartFormDataOutput multipartFormDataOutput,
            final URL url,
            final Map<String, Object> headers) {

        if(resteasyClient == null) {
            create();
        }

        final String baseUri = getBaseUri(url);
        final String path = url.getPath();

        final ResteasyWebTarget resteasyWebTarget = resteasyClient
                .target(baseUri)
                .path(path);

        final GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(multipartFormDataOutput) {
        };

        return resteasyWebTarget
                .request()
                .headers(new MultivaluedHashMap<>(headers))
                .post(entity(entity, MULTIPART_FORM_DATA_TYPE));
    }

    /**
     * Gets an {@link java.io.InputStream} from the file at the specified url
     *
     * To Use:
     * <pre>
     *     <blockquote>
     *          final Response response = multipartRestClient.getFile(url)
     *          final InputStream inputStream = response.readEntity(InputStream.class);
     *     </blockquote>
     * </pre>
     *
     *
     * @param url   The resource url
     * @return An HTTP response containing the {@link InputStream )
     */
    public Response getFile(final URL url) {

        if(resteasyClient == null) {
            create();
        }

        final String baseUri = getBaseUri(url);
        final String path = url.getPath();

        return resteasyClient
                .target(baseUri)
                .path(path)
                .request()
                .get();
    }

    /**
     * Closes the Rest Client. Implementation of the {@link AutoCloseable} interface
     */
    @Override
    public void close() {
        if (resteasyClient != null) {
            resteasyClient.close();
        }
    }

    private String getBaseUri(final URL url) {
        return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
    }
}
