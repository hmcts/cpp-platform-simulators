package uk.gov.justice.util;

import static java.lang.String.format;
import static java.nio.charset.Charset.defaultCharset;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtil {
    final static Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static String getPayloadFromClassPath(final String path) {
        String request = null;
        try {
            request = Resources.toString(Resources.getResource(path), defaultCharset());
        } catch (final Exception e) {
            LOGGER.error("Error consuming file from location {}", path, e);
            throw new RuntimeException(format("Error consuming file from location '%s'", path), e);
        }
        return request;
    }

    public static void createFile(final String directory, final String filename, final String payload) {

        final String fullyQualifiedFileName = directory + "/" + filename;
        LOGGER.info("Message stored in a file : {}", fullyQualifiedFileName);

        try {
            Path path = Paths.get(fullyQualifiedFileName);
            Files.createDirectories(Paths.get(directory));
            Files.write(path, payload.getBytes());
        } catch (IOException e) {
            LOGGER.debug("Problem creating file", e);
        }
    }

    public static void createFile(final String directory, final String filename, final InputStream inputStream) {

        final String fullyQualifiedFileName = directory + "/" + filename;
        LOGGER.info("Message stored in a file : {}", fullyQualifiedFileName);

        try {
            Path path = Paths.get(fullyQualifiedFileName);
            Files.createDirectories(Paths.get(directory));
            Files.copy(inputStream, path);
        } catch (IOException e) {
            LOGGER.debug("Problem creating file", e);
        }
    }
}
