package uk.gov.justice.darts.transformer.util;

public interface ResponseMessage {

    String RESPONSE_200 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://com.synapps.mojdarts.service.com\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <typ:addDocumentResponse>\n" +
            "      <return>\n" +
            "         <code>200</code>\n" +
            "         <message>OK</message>\n" +
            "      </return>\n" +
            "      </typ:addDocumentResponse>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    String REGISTER_RESPONSE_200 = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://services.rt.fs.documentum.emc.com/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <typ:registerResponse>\n" +
            "      <return>\n" +
            "         <code>200</code>\n" +
            "         <message>OK</message>\n" +
            "      </return>\n" +
            "      </typ:registerResponse>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";
}
