package cpp.platform.simulators.cps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.google.common.base.Splitter.on;
import static cpp.platform.simulators.util.FileUtil.deleteFiles;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static uk.gov.justice.cps.CpsTodaysMessageListResponseTransformer.CPS_TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CPS_DIR;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.TWIF_DATE_FORMAT;
import static uk.gov.justice.util.FileUtil.createFile;

import uk.gov.justice.cps.CpsTodaysMessageListResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.justice.util.FileUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CpsTodaysMessageListResponseTransformerTest {

    private static final String CPS_GET_FILE_LIST_FOR_TODAY = "/cps/message-list";

    private static final String ENDPOINT = "http://localhost:8888" + CPS_GET_FILE_LIST_FOR_TODAY;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(CpsTodaysMessageListResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {

        deleteFiles(CPS_DIR);
        wireMockRule.stubFor(get(urlPathEqualTo(CPS_GET_FILE_LIST_FOR_TODAY))
                .willReturn(aResponse()
                        .withTransformer(CPS_TODAYS_MESSAGE_LIST_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @After
    public void tearDown() throws IOException {
        deleteFiles(CPS_DIR);
    }

    @Test
    public void shouldRetrieveFilesDateStampedForToday() {
        final String dateToday = new SimpleDateFormat(TWIF_DATE_FORMAT).format(new Date());
        final String dateYesterday = new SimpleDateFormat(TWIF_DATE_FORMAT).format(Date.from(ZonedDateTime.now().minusDays(1).toInstant()));
        final String dateTomorrow = new SimpleDateFormat(TWIF_DATE_FORMAT).format(Date.from(ZonedDateTime.now().plusDays(1).toInstant()));

        createFilesFor(dateToday, 2);
        createFilesFor(dateYesterday, 3);
        createFilesFor(dateTomorrow, 1);

        final Response response = new RestClient().query(ENDPOINT, "text/xml", new MultivaluedHashMap<>());
        assertThat(response.getStatus(), CoreMatchers.is(200));
        final String entity = response.readEntity(String.class);
        final List<String> split = on(" ").trimResults().omitEmptyStrings().splitToList(entity);
        assertThat(split, hasSize(2));
    }

    private void createFilesFor(final String datePrefix, final int count) {
        for(int i = 0; i < count; i++) {
            final String filename = datePrefix + "_" + randomAlphanumeric(10) + ".txt";
            final String samplePayload = randomAlphanumeric(100);
            createFile(CPS_DIR, filename, samplePayload);
        }

    }
}
