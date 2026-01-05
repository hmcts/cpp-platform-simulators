package uk.gov.justice.twiff.transformer.util;

public interface ResponseMessage {

     String RESPONSE_SUCCESS = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <typ:SubmitResponseMes>\n" +
            "         <typ:RequestID>" + "DUMMY_REQUEST_ID" + "</typ:RequestID>\n" +
            "         <typ:ResponseCode>1</typ:ResponseCode>\n" +
            "         <typ:ResponseText>SUCCESS</typ:ResponseText>\n" +
            "      </typ:SubmitResponseMes>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

     String RESPONSE_FAILURE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <typ:SubmitResponseMes>\n" +
            "         <typ:RequestID> </typ:RequestID>\n" +
            "         <typ:ResponseCode>0</typ:ResponseCode>\n" +
            "         <typ:ResponseText>" + "DUMMY_FAILURE_MESSAGE" + "</typ:ResponseText>\n" +
            "      </typ:SubmitResponseMes>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";


     String RESPONSE_200_ERROR = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>200</typ:ResponseCode>\n" +
             "         <typ:ResponseText>ERROR</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";

     String RESPONSE_201_TEMPORARY_PROBLEM = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>201</typ:ResponseCode>\n" +
             "         <typ:ResponseText>TEMPORARY PROBLEM</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";

     String RESPONSE_202_SERVER_FAILURE = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>202</typ:ResponseCode>\n" +
             "         <typ:ResponseText>SERVER FAILURE</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";

     String RESPONSE_300_FATAL_ERROR= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>300</typ:ResponseCode>\n" +
             "         <typ:ResponseText>FATAL ERROR</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";

     String RESPONSE_304_WRONG_SOURCE= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>304</typ:ResponseCode>\n" +
             "         <typ:ResponseText>WRONG SOURCE</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";

     String RESPONSE_305_WRONG_DESTINATION= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>305</typ:ResponseCode>\n" +
             "         <typ:ResponseText>WRONG DESTINATION</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";

     String RESPONSE_306_INVALID_REQUESTID= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>306</typ:ResponseCode>\n" +
             "         <typ:ResponseText>INVALID REQUEST ID</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";

     String RESPONSE_307_MODE_ERROR= "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:typ=\"http://schemas.cjse.gov.uk/endpoint/types/\">\n" +
             "   <soapenv:Header/>\n" +
             "   <soapenv:Body>\n" +
             "      <typ:SubmitResponseMes>\n" +
             "         <typ:RequestID> </typ:RequestID>\n" +
             "         <typ:ResponseCode>307</typ:ResponseCode>\n" +
             "         <typ:ResponseText>MODE ERROR</typ:ResponseText>\n" +
             "      </typ:SubmitResponseMes>\n" +
             "   </soapenv:Body>\n" +
             "</soapenv:Envelope>";
}
