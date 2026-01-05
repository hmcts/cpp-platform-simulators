package uk.gov.justice.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {

    public static String getFileNameFromUrl(final String url, final String patternPrefix) {
        String patternAsString = patternPrefix + "(.*?)$";
        Pattern pattern = Pattern.compile(patternAsString);
        Matcher matcher = pattern.matcher(url);
        String messageName = "";
        if (matcher.find()) {
            messageName = matcher.group(1);
        }
        return messageName;
    }
}
