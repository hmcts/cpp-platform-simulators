package cpp.platform.simulators.cjse.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.twiff.transformer.util.GlobalConstants.CJSE_DIR;

import uk.gov.cjse.schemas.endpoint.types.ExecMode;
import uk.gov.cjse.schemas.endpoint.types.ObjectFactory;
import uk.gov.cjse.schemas.endpoint.types.SubmitRequest;
import uk.gov.cjse.schemas.endpoint.wsdl.CJSEPort;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPBinding;

import cpp.platform.simulators.util.FileUtil;

public class CjseSimulatorTestUtil {

    private static String baseUri = "http://localhost:8888";

    private static final QName SERVICE_NAME
            = new QName("http://schemas.cjse.gov.uk/endpoint/wsdl/", "CJSEService");
    private static final QName PORT_NAME
            = new QName("http://schemas.cjse.gov.uk/endpoint/wsdl/", "CJSEPort");

    public CJSEPort getWebServicePort() throws MalformedURLException {

        final URL resource = getClass().getClassLoader().getResource("wsdl/CJSEService.wsdl");

        assertThat(resource, is(notNullValue()));

        Service service = Service.create(resource, SERVICE_NAME);
        String endpointAddress = baseUri + "/CJSE/message";
        service.addPort(PORT_NAME, SOAPBinding.SOAP11HTTP_BINDING, endpointAddress);
        return service.getPort(CJSEPort.class);
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void deleteFiles() throws IOException {
        FileUtil.deleteFiles(CJSE_DIR);
    }

    public SubmitRequest createSoapRequest(String requestId, String message) throws DatatypeConfigurationException {

        SubmitRequest submitRequest = new ObjectFactory().createSubmitRequest();
        submitRequest.setRequestID(requestId);
        submitRequest.setSourceID("22");
        submitRequest.setExecMode(ExecMode.ASYNCH);
        submitRequest.setTimestamp(getTimestamp());
        submitRequest.getDestinationID().add("destId");
        submitRequest.setMessage(message);
        return submitRequest;
    }

    private XMLGregorianCalendar getTimestamp() throws DatatypeConfigurationException {
        final GregorianCalendar now = new GregorianCalendar();
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
    }
}
