package uk.gov.tomakehurst.wiremock.servlet;

import static java.util.Collections.emptyMap;

import uk.gov.justice.cjse.transformer.CjseGetMessageByCorrelationIdResponseTransformer;
import uk.gov.justice.cjse.transformer.CjseGetMessageByUrnAndFamilyNameResponseTransformer;
import uk.gov.justice.cjse.transformer.CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer;
import uk.gov.justice.cjse.transformer.CjseMessageReceivedResponseTransformer;
import uk.gov.justice.cps.ApplicationNotificationSentToCpsResponseTransformer;
import uk.gov.justice.cps.ApplicationRequestNotificationSentToCpsResponseTransformer;
import uk.gov.justice.cps.ApplicationResultNotificationSentToCpsResponseTransformer;
import uk.gov.justice.cps.CpsGetFileResponseTransformer;
import uk.gov.justice.cps.CpsTodaysMessageListResponseTransformer;
import uk.gov.justice.cps.DefenseMaterialNotificationSentToCpsResponseTransformer;
import uk.gov.justice.darts.transformer.DartsServiceContextRegistryResponseTransformer;
import uk.gov.justice.darts.transformer.DartsServiceResponseTransformer;
import uk.gov.justice.dcs.tramsformer.DcsDefenceRepresentationResponseTransformer;
import uk.gov.justice.dcs.tramsformer.DcsDefendantUpdateResponseTransformer;
import uk.gov.justice.dcs.tramsformer.DcsLinkCaseAndDefendantResponseTransformer;
import uk.gov.justice.dcs.tramsformer.DcsMaterialUpdateResponseTransformer;
import uk.gov.justice.twiff.transformer.GetMessageByTransactionIdResponseTransformer;
import uk.gov.justice.twiff.transformer.GetMessageResponseTransformer;
import uk.gov.justice.twiff.transformer.MessageReceivedResponseTransformer;
import uk.gov.justice.twiff.transformer.TodaysMessageListResponseTransformer;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.servlet.WarConfiguration;
import javax.servlet.ServletContext;

public class SimulatorsWarConfiguration extends WarConfiguration {

    public SimulatorsWarConfiguration(ServletContext servletContext) {
        super(servletContext);
    }

    public <T extends Extension> Map<String, T> extensionsOfType(Class<T> extensionType) {
        if (extensionType.isAssignableFrom(ResponseDefinitionTransformer.class)) {
            final HashMap extensionMap = new HashMap<>();
            extensionMap.put(GetMessageResponseTransformer.GET_FILE_RESPONSE_TRANSFORMER, new GetMessageResponseTransformer());
            extensionMap.put(MessageReceivedResponseTransformer.MESSAGE_RECEIVED_RESPONSE_TRANSFORMER, new MessageReceivedResponseTransformer());
            extensionMap.put(TodaysMessageListResponseTransformer.TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER, new TodaysMessageListResponseTransformer());
            extensionMap.put(CjseMessageReceivedResponseTransformer.CJSE_MESSAGE_RECEIVED_RESPONSE_TRANSFORMER, new CjseMessageReceivedResponseTransformer());
            extensionMap.put(CjseGetMessageByUrnAndFamilyNameResponseTransformer.CJSE_GET_MESSAGE_BY_URN_RESPONSE_TRANSFORMER, new CjseGetMessageByUrnAndFamilyNameResponseTransformer());
            extensionMap.put(CjseGetMessageByCorrelationIdResponseTransformer.CJSE_GET_MESSAGE_BY_CORRELATION_ID_RESPONSE_TRANSFORMER, new CjseGetMessageByCorrelationIdResponseTransformer());
            extensionMap.put(CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer.CJSE_GET_MESSAGE_BY_URN_AND_PROSECUTOR_REFERENCE_RESPONSE_TRANSFORMER, new CjseGetMessageByUrnAndProsecutorReferenceResponseTransformer());
            extensionMap.put(DartsServiceResponseTransformer.DARTS_SERVICE_RESPONSE_TRANSFORMER, new DartsServiceResponseTransformer());
            extensionMap.put(DartsServiceContextRegistryResponseTransformer.DARTS_SERVICE_CONTEXT_REGISTRY_RESPONSE_TRANSFORMER, new DartsServiceContextRegistryResponseTransformer());
            extensionMap.put(GetMessageByTransactionIdResponseTransformer.GET_MESSAGE_BY_TRANSACTION_ID_RESPONSE_TRANSFORMER, new GetMessageByTransactionIdResponseTransformer());
            extensionMap.put(DefenseMaterialNotificationSentToCpsResponseTransformer.DEFENSE_MATERIAL_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER, new DefenseMaterialNotificationSentToCpsResponseTransformer());
            extensionMap.put(ApplicationNotificationSentToCpsResponseTransformer.APPLICATION_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER, new ApplicationNotificationSentToCpsResponseTransformer());
            extensionMap.put(ApplicationResultNotificationSentToCpsResponseTransformer.APPLICATION_RESULT_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER, new ApplicationResultNotificationSentToCpsResponseTransformer());
            extensionMap.put(ApplicationRequestNotificationSentToCpsResponseTransformer.APPLICATION_REQUEST_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER, new ApplicationRequestNotificationSentToCpsResponseTransformer());
            extensionMap.put(CpsTodaysMessageListResponseTransformer.CPS_TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER, new CpsTodaysMessageListResponseTransformer());
            extensionMap.put(CpsGetFileResponseTransformer.CPS_GET_FILE_RESPONSE_TRANSFORMER, new CpsGetFileResponseTransformer());
            extensionMap.put(DcsLinkCaseAndDefendantResponseTransformer.DCS_LINK_CASE_AND_DEFENDANT_RESPONSE_TRANSFORMER, new DcsLinkCaseAndDefendantResponseTransformer());
            extensionMap.put(DcsDefendantUpdateResponseTransformer.DCS_DEFENDANT_UPDATE_RESPONSE_TRANSFORMER, new DcsDefendantUpdateResponseTransformer());
            extensionMap.put(DcsDefenceRepresentationResponseTransformer.DCS_DEFENCE_REPRESENTATION_RESPONSE_TRANSFORMER, new DcsDefenceRepresentationResponseTransformer());
            extensionMap.put(DcsMaterialUpdateResponseTransformer.DCS_MATERIAL_UPDATE_RESPONSE_TRANSFORMER, new DcsMaterialUpdateResponseTransformer());
            return extensionMap;
        }

        return emptyMap();


    }
}
