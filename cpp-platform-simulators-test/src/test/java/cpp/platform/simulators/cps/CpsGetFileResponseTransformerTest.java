package cpp.platform.simulators.cps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.google.common.base.Splitter.on;
import static cpp.platform.simulators.util.FileUtil.deleteFiles;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.cps.CpsGetFileResponseTransformer.CPS_GET_FILE_RESPONSE_TRANSFORMER;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CPS_DIR;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.TWIF_DATE_FORMAT;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.TWIF_DATE_FORMAT_WITH_HHMM;
import static uk.gov.justice.util.FileUtil.createFile;

import uk.gov.justice.cps.CpsGetFileResponseTransformer;
import uk.gov.justice.cps.CpsTodaysMessageListResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.RestClient;
import uk.gov.justice.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jgroups.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class CpsGetFileResponseTransformerTest {

    private static final String CPS_GET_FILE_RESPONSE = "/cps/message-file/%s";

    private static final String ENDPOINT = "http://localhost:8888" + CPS_GET_FILE_RESPONSE;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(CpsGetFileResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {

        deleteFiles(CPS_DIR);
        wireMockRule.stubFor(get(urlPathMatching("/cps/message-file/\\d{2}_\\d{2}_\\d{6}_\\d{2}_.*"))
                .willReturn(aResponse()
                        .withTransformer(CPS_GET_FILE_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @After
    public void tearDown() throws IOException {
        deleteFiles(CPS_DIR);
    }

    @Test
    public void shouldRetrieveTextPayloadFileAsStringResponse() {
        final String dateToday = new SimpleDateFormat(TWIF_DATE_FORMAT_WITH_HHMM).format(new Date());

        final String textPayload = randomAlphanumeric(500);
        final String filename = createFile(dateToday, textPayload);

        final String url = String.format(ENDPOINT, filename);
        final Response response = new RestClient().query(url, "text/xml", new MultivaluedHashMap<>());
        assertThat(response.getStatus(), CoreMatchers.is(200));
        final String responsePayload = response.readEntity(String.class);
        assertThat(responsePayload, is(textPayload));
    }

    @Test
    public void shouldRetrieveBinaryFileAsBase64StringResponse() throws IOException {
        final String dateToday = new SimpleDateFormat(TWIF_DATE_FORMAT_WITH_HHMM).format(new Date());

        final File file = new File("src/test/resources/sample/sample.pdf");
        final String filename = createFile(dateToday, FileUtils.openInputStream(file));

        final String url = String.format(ENDPOINT, filename);
        final Response response = new RestClient().query(url, "text/xml", new MultivaluedHashMap<>());
        assertThat(response.getStatus(), CoreMatchers.is(200));
        final String responsePayload = response.readEntity(String.class);
        assertThat(Base64.decode(responsePayload), is(FileUtils.readFileToByteArray(file)));
    }

    private String createFile(final String datePrefix, final String payload) {
        final String filename = datePrefix + "_" + randomAlphanumeric(10) + "_payload.txt";
        FileUtil.createFile(CPS_DIR, filename, payload);
        return filename;
    }

    private String createFile(final String datePrefix, final InputStream binaryPayload) {
        final String filename = datePrefix + "_" + randomAlphanumeric(10) + "_binary.pdf";
        FileUtil.createFile(CPS_DIR, filename, binaryPayload);
        return filename;
    }
}
