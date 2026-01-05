package uk.gov.tomakehurst.wiremock.scheduler;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteFileTask extends TimerTask {

    final static Logger logger = LoggerFactory.getLogger(DeleteFileTask.class);

    @Override
    public void run() {
        deleteOldFiles();
    }

    private void deleteOldFiles() {
        final String dir = "/tmp/TWIFMessages";
        final String TWIF_DATE_FORMAT = "dd_MM_yyyy";
        final String DATE_TODAY = new SimpleDateFormat(TWIF_DATE_FORMAT).format(new Date());
        try {
            Files.list(Paths.get(dir))
                    .filter(Files::isRegularFile)
                    .filter((val) -> !val.getFileName().toString().startsWith(DATE_TODAY))
                    .forEach(this::deleteFiles);
        } catch (IOException e) {
            logger.error("IO exception");
        }
    }

    private void deleteFiles(Path filePath) {
        try {
            Files.delete(filePath);
        } catch (NoSuchFileException x) {
            logger.error("{}: no such  file or directory", filePath);
        } catch (DirectoryNotEmptyException x) {
            logger.error("{} not empty", filePath);
        } catch (IOException x) {
            logger.error("IO error : {}", x);
        }
    }
}
