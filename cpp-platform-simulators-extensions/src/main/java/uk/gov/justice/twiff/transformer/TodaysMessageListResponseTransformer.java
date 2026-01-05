package uk.gov.justice.twiff.transformer;

import static uk.gov.justice.twiff.transformer.util.GlobalConstants.TWIF_DIR;

import uk.gov.justice.common.AbstractTodaysMessageListResponseTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TodaysMessageListResponseTransformer extends AbstractTodaysMessageListResponseTransformer {

    public static final String TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER = "todays-message-list-response-transformer";
    final static Logger LOGGER = LoggerFactory.getLogger(TodaysMessageListResponseTransformer.class);

    @Override
    public String getName() {
        return TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER;
    }


    @Override
    public String getDirectory() {
        return TWIF_DIR;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
