package cpp.platform.simulators.util;

import static java.nio.file.Files.deleteIfExists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {

    private FileUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static void deleteFiles(final String directory) throws IOException {
        Files.createDirectories(Paths.get(directory));
        Files.list(Paths.get(directory))
                .filter(Files::isRegularFile)
                .forEach(FileUtil::deleteFile);
    }

    private static void deleteFile(Path path) {
        try {
            deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error("Unable to delete file at '{}'", path);
        }
    }
}
