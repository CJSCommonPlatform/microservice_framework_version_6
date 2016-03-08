package uk.gov.justice.services.adapters.rest.util.builder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.fail;

public class FileBuilder {

    String relativePath;

    private FileBuilder(String relativePath) {
        this.relativePath = relativePath;
    }

    public static File aDirectory(String relativePath) {
        return aFile(relativePath);
    }

    public static File aFile(String relativePath) {
        URL resource = FileBuilder.class.getClassLoader().getResource(relativePath);
        return new File(resource.getFile());
    }

    public static String aStringFromFile(String relativePath) {
        try {
            return FileUtils.readFileToString(aFile(relativePath));
        } catch (IOException e) {
            fail(e.getMessage());
            return null;
        }
    }

}
