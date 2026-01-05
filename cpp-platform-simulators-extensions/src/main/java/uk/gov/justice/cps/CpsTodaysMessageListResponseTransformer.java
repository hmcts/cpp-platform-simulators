package uk.gov.justice.cps;

import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CPS_DIR;

import uk.gov.justice.common.AbstractTodaysMessageListResponseTransformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CpsTodaysMessageListResponseTransformer extends AbstractTodaysMessageListResponseTransformer {

    public static final String CPS_TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER = "cps-todays-message-list-response-transformer";
    final static Logger LOGGER = LoggerFactory.getLogger(CpsTodaysMessageListResponseTransformer.class);

    @Override
    public String getName() {
        return CPS_TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER;
    }


    @Override
    public String getDirectory() {
        return CPS_DIR;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
