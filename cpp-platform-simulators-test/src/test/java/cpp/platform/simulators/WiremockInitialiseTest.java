package cpp.platform.simulators;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.github.tomakehurst.wiremock.client.WireMock;

public class WiremockInitialiseTest {

    public void setupTest() {

        String TWIF_DATE_FORMAT = "dd_MM_yyyy";
        String DATE_TODAY = new SimpleDateFormat(TWIF_DATE_FORMAT).format(new Date());

        WireMock wireMock = new WireMock(8080);
        reset();

        wireMock.stubFor(post(urlPathEqualTo("/TWIF/C2IOutbound.asmx"))
                .willReturn(aResponse()
                        .withTransformers("message-received-response-transformer")
                ));

        wireMock.stubFor(get(urlEqualTo("/message-list?date=today"))
                .willReturn(aResponse()
                        .withTransformers("todays-message-list-response-transformer")
                ));

        wireMock.stubFor(get(urlPathMatching("/message-file/" + DATE_TODAY + "\\d{2}_\\d{2}_.*"))
                .willReturn(aResponse()
                        .withTransformers("get-file-response-transformer")
                ));
    }

}

