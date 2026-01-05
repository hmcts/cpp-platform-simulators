package cpp.platform.simulators.cps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static cpp.platform.simulators.util.FileUtil.deleteFiles;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createReader;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.cps.DefenseMaterialNotificationSentToCpsResponseTransformer.DEFENSE_MATERIAL_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CPS_DIR;

import uk.gov.justice.cps.DefenseMaterialNotificationSentToCpsResponseTransformer;
import uk.gov.justice.services.test.utils.core.rest.ResteasyClientBuilderFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DefenseMaterialNotificationSentToCpsResponseTransformerTest {

    private static final String CPS_NOTIFICATION_PATH = "/CP/v1/notification/material-notification";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8888).extensions(DefenseMaterialNotificationSentToCpsResponseTransformer.class));

    @Before
    public void setupTest() throws IOException {

        deleteFiles(CPS_DIR);
        wireMockRule.stubFor(post(urlPathEqualTo(CPS_NOTIFICATION_PATH))
                .willReturn(aResponse()
                        .withTransformer(DEFENSE_MATERIAL_NOTIFICATION_SENT_TO_CPS_RESPONSE_TRANSFORMER, null, null)
                ));
    }

    @After
    public void tearDown() throws IOException {
        deleteFiles(CPS_DIR);
    }

    @Test
    public void shouldReturnValidResponseForMultipartRequest() throws IOException {
        String endpointAddress = "http://localhost:8888" + CPS_NOTIFICATION_PATH;

        MultipartFormDataOutput mdo = new MultipartFormDataOutput();
        mdo.addFormData("Material", FileUtils.openInputStream(new File("src/test/resources/sample/test.txt")),
                MediaType.APPLICATION_OCTET_STREAM_TYPE, "test.txt");
        final String materialId = UUID.randomUUID().toString();
        mdo.addFormData("Notification", getPayload(materialId).toString(), MediaType.APPLICATION_JSON_TYPE);
        GenericEntity<MultipartFormDataOutput> entity = new GenericEntity<MultipartFormDataOutput>(mdo) {
        };

        final Response response = ResteasyClientBuilderFactory.clientBuilder().build().target(endpointAddress).request().post(Entity.entity(entity, MediaType.MULTIPART_FORM_DATA_TYPE));

        assertThat(response.getStatus(), is(200));
        final JsonObject responseObject = createReader(new StringReader(response.readEntity(String.class))).readObject();
        assertThat(responseObject.getString("notificationId"), notNullValue());
    }

    private JsonObject getPayload(final String materialId) {
        final JsonObject materialNotification = createObjectBuilder().add("materialId", materialId).add("fileName", "test.txt").build();
        return createObjectBuilder().add("materialNotification", materialNotification).build();
    }

}