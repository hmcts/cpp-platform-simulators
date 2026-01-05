package uk.gov.justice.util;

import static uk.gov.justice.twiff.transformer.util.GlobalConstants.TWIF_DATE_FORMAT_WITH_HHMM;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    private DateUtil() {
    }

    public static String getFormattedDate() {
        return new SimpleDateFormat(TWIF_DATE_FORMAT_WITH_HHMM).format(new Date());
    }
}
