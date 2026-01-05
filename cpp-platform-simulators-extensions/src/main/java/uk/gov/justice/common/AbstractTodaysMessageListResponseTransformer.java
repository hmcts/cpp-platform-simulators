package uk.gov.justice.common;

import static uk.gov.justice.twiff.transformer.util.GlobalConstants.FILE_LIST_SEPARATOR;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.TWIF_DATE_FORMAT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.slf4j.Logger;

public abstract class AbstractTodaysMessageListResponseTransformer extends ResponseDefinitionTransformer {

    @Override
    public ResponseDefinition transform(Request request, ResponseDefinition responseDefinition, FileSource files, Parameters parameters) {

        long currentTime = System.currentTimeMillis();
        final String concatenatedStringOfTodaysFiles = getTodaysFileList();
        getLogger().info("Received request to get list of todays file : {}", request.getUrl());
        getLogger().info("Sending response:{} ", concatenatedStringOfTodaysFiles);
        final ResponseDefinition responseDefinition1 = new ResponseDefinitionBuilder()
                .withHeader("Content-Type", "text/xml")
                .withStatus(200)
                .withBody(concatenatedStringOfTodaysFiles)
                .build();
        long timeToExecute = System.currentTimeMillis() - currentTime;
        getLogger().info("TIME TO EXECUTE : {}", timeToExecute);
        return responseDefinition1;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private String getTodaysFileList() {

        final String dateToday = new SimpleDateFormat(TWIF_DATE_FORMAT).format(new Date());
        StringBuilder stringBuilder = new StringBuilder();
        try {
            final List<Path> paths = Files.list(Paths.get(getDirectory()))
                    .filter(Files::isRegularFile)
                    .filter((val) -> val.getFileName().toString().startsWith(dateToday))
                    .collect(Collectors.toList());
            for (Path path : paths) {
                stringBuilder.append(FILE_LIST_SEPARATOR).append(path.getFileName());
            }
        } catch (IOException e) {
            getLogger().debug("Problem forming list of todays file", e);
        }
        return stringBuilder.toString();
    }

    public abstract String getDirectory();

    public abstract Logger getLogger();


}
