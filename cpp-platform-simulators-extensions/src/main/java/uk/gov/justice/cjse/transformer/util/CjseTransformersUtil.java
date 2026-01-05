package uk.gov.justice.cjse.transformer.util;

import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CJSE_DIR;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.TWIF_DATE_FORMAT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CjseTransformersUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(CjseTransformersUtil.class);
    private static final Pattern PATTERN_TO_EXTRACT_CORRELATION_ID_FROM_XML = Pattern.compile("<(.*)CorrelationID>(.*?)<(.*)CorrelationID>");
    private static final Pattern PATTERN_TO_EXTRACT_URN_FROM_XML = Pattern.compile("<(.*)PTIURN>(.*?)<(.*)PTIURN>");

    public static String getFileContentAsString(final String fileName) {

        String fileContentWithSOAPEnvelope = "";
        try {
            fileContentWithSOAPEnvelope = new String(Files.readAllBytes(Paths.get(new StringBuilder(CJSE_DIR).append("/").append(fileName).toString())));
        } catch (IOException ioe) {
            LOGGER.debug("FILE IO Exception while reading a file {}", fileName);
            throw new RuntimeException("FILE IO Exception while reading a file " + fileName, ioe);
        }
        return fileContentWithSOAPEnvelope;
    }

    public static Path getLatestMatchingFile(List<Path> matchingFiles) {

        if (matchingFiles == null || matchingFiles.isEmpty()) {
            throw new RuntimeException("No matching files");
        }
        matchingFiles.sort((Path path1, Path path2) -> getFileCreationTime(path1).compareTo(getFileCreationTime(path2)));
        return matchingFiles.get(matchingFiles.size() - 1);
    }


    public static String getLatestFile(final List<Path> paths) {
        final Path latestMatchingFile = getLatestMatchingFile(paths);
        return getFileContentAsString(latestMatchingFile.getFileName().toString());
    }

    public static List<Path> getNewCaseFilesByPredicate(final Predicate<Path> predicate) {

        final String dateToday = new SimpleDateFormat(TWIF_DATE_FORMAT).format(new Date());
        List<Path> matchingFiles;
        try {
            matchingFiles = Files.list(Paths.get(CJSE_DIR))
                    .filter(Files::isRegularFile)
                    .filter((val) -> val.getFileName().toString().startsWith(dateToday))
                    .filter(predicate)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOGGER.debug("Problem getting file with given criteria: {}",  e.getStackTrace());
            throw new RuntimeException("Problem getting file with given criteria : " , e);
        }
        return matchingFiles;
    }

    public static String getLatestFileMatchingPredicate(final Predicate<Path> predicate) {

        List<Path> testPredicate = getNewCaseFilesByPredicate(predicate);
        return getLatestFile(testPredicate);
    }

    private static FileTime getFileCreationTime(Path file) {
        BasicFileAttributes attributes;
        try {
            attributes = Files.getFileAttributeView(file, BasicFileAttributeView.class).readAttributes();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading attributes of file " + file);
        }
        return attributes.creationTime();
    }

    public static String getUrn(final String xmlBodyAsString) {
        return extractValueFromXML(xmlBodyAsString, PATTERN_TO_EXTRACT_URN_FROM_XML, 2);
    }

    public static  String getCorrelationId(final String xmlBodyAsString) {
        return extractValueFromXML(xmlBodyAsString, PATTERN_TO_EXTRACT_CORRELATION_ID_FROM_XML, 2);
    }

    public static String extractValueFromXML(final String xmlBodyAsString, final Pattern pattern, int groupNo) {
        String unescapeXml = StringEscapeUtils.unescapeXml(xmlBodyAsString);
        unescapeXml= StringEscapeUtils.unescapeXml(unescapeXml);
        return extractValue(unescapeXml, pattern, groupNo);
    }

    public static String extractValue(final String data, final Pattern pattern, final int groupNo) {
        Matcher matcher = pattern.matcher(data);
        String value = "";
        if (matcher.find()) {
            value = matcher.group(groupNo);
        }
        return value;
    }
}
