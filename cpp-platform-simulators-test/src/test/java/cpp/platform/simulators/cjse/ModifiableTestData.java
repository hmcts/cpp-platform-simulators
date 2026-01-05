package cpp.platform.simulators.cjse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifiableTestData {

    private static String DATA =
            "<?xml version=\"1.0\" ?>\n" +
                    "<?xml-stylesheet href=\"ensemble/Ensemble_XMLDisplay.xsl\" type=\"text/xsl\" ?>\n" +
                    "<!--type: SOAPS.TWIF.BusOp.Request.submitNewCaseMessageRequest  id: 30098146 -->\n" +
                    "<submitNewCaseMessageRequest xmlns:s = \"http://www.w3.org/2001/XMLSchema\" xmlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "    <NewCaseMessage\n" +
                    "        xmlns:s01 = \"http://www.dca.gov.uk/xmlschemas/libra\"\n" +
                    "        Flow = \"NewCasesFromThePolice\"\n" +
                    "        Interface = \"LibraStandardProsecutorPolice\"\n" +
                    "        SchemaVersion = \"0.6g\">\n" +
                    "        <Case>\n" +
                    "            <PTIURN>1234</PTIURN>\n" +
                    "            <OriginatingOrganisation>042HQ00</OriginatingOrganisation>\n" +
                    "            <CaseInitiation>J</CaseInitiation>\n" +
                    "            <Informant>Stephen Kavanagh</Informant>\n" +
                    "      <CorrelationID>REQLUEST_AID</CorrelationID>\n" +
                    "            <InitialHearing>\n" +
                    "                <CourtHearingLocation>B42CM75</CourtHearingLocation>\n" +
                    "                <DateOfHearing>2019-02-08</DateOfHearing>\n" +
                    "                <TimeOfHearing>10:00:00Z</TimeOfHearing>\n" +
                    "            </InitialHearing>\n" +
                    "            <OtherPartyOfficerInCase>\n" +
                    "                <OfficerName>\n" +
                    "                    <PersonGivenName1>David</PersonGivenName1>\n" +
                    "                    <PersonFamilyName>Rudd</PersonFamilyName>\n" +
                    "                </OfficerName>\n" +
                    "                <PoliceOfficerRank>CIV</PoliceOfficerRank>\n" +
                    "                <PoliceWorkerReferenceNumber>79226</PoliceWorkerReferenceNumber>\n" +
                    "                <PoliceWorkerLocationCode>042HQ00</PoliceWorkerLocationCode>\n" +
                    "                <StructuredAddress xmlns:s02 = \"http://www.govtalk.gov.uk/people/bs7666\">\n" +
                    "                    <s02:PAON>Essex Police Headquarters</s02:PAON>\n" +
                    "                    <s02:StreetDescription>Kingston Crescent</s02:StreetDescription>\n" +
                    "                    <s02:Locality>Springfield</s02:Locality>\n" +
                    "                    <s02:Town>Chelmsford</s02:Town>\n" +
                    "                    <s02:AdministrativeArea>Essex</s02:AdministrativeArea>\n" +
                    "                    <s02:PostCode>CM2 6DN</s02:PostCode>\n" +
                    "                </StructuredAddress>\n" +
                    "                <EmailDetails>\n" +
                    "                    <EmailAddress1>david.rudd@essex.pnn.police.uk</EmailAddress1>\n" +
                    "                </EmailDetails>\n" +
                    "            </OtherPartyOfficerInCase>\n" +
                    "            <Defendant>\n" +
                    "                <ProsecutorReference>0416BH0000000090557N</ProsecutorReference>\n" +
                    "                <PoliceIndividualDefendant>\n" +
                    "                    <PersonDefendant>\n" +
                    "                        <BasePersonDetails>\n" +
                    "                            <PersonName>\n" +
                    "                                <PersonTitle>Mr</PersonTitle>\n" +
                    "                                <PersonGivenName1>Eden</PersonGivenName1>\n" +
                    "                                <PersonGivenName2>Frank</PersonGivenName2>\n" +
                    "                                <PersonFamilyName>Hurley</PersonFamilyName>\n" +
                    "                            </PersonName>\n" +
                    "                            <Birthdate>1994-05-27</Birthdate>\n" +
                    "                            <Gender>1</Gender>\n" +
                    "                        </BasePersonDetails>\n" +
                    "                    </PersonDefendant>\n" +
                    "                    <Address xmlns:s02 = \"http://www.govtalk.gov.uk/people/bs7666\">\n" +
                    "                        <s02:PAON>43A</s02:PAON>\n" +
                    "                        <s02:StreetDescription>Dockfield Avenue</s02:StreetDescription>\n" +
                    "                        <s02:Town>Harwich</s02:Town>\n" +
                    "                        <s02:AdministrativeArea>Essex</s02:AdministrativeArea>\n" +
                    "                        <s02:PostCode>CO12 4LF</s02:PostCode>\n" +
                    "                    </Address>\n" +
                    "                    <LanguageOptions>\n" +
                    "                        <DocumentationLanguage>E</DocumentationLanguage>\n" +
                    "                        <HearingLanguage>E</HearingLanguage>\n" +
                    "                    </LanguageOptions>\n" +
                    "                    <DriverInformation>\n" +
                    "                        <DriverNumber>PAUL9905274EF9LG</DriverNumber>\n" +
                    "                    </DriverInformation>\n" +
                    "                </PoliceIndividualDefendant>\n" +
                    "                <Offence>\n" +
                    "                    <BaseOffenceDetails>\n" +
                    "                        <OffenceSequenceNumber>1</OffenceSequenceNumber>\n" +
                    "                        <OffenceCode>RT88509</OffenceCode>\n" +
                    "                        <OffenceWording>On 22/08/2018 at Clacton in the county of Essex drove a motor vehicle, namely VAUXHALL index FD56BKF, on a road namely A133, whilst not wearing an adult belt in conformity with regulation 5(1)(a) of the Motor Vehicles (Wearing of seat belts) Regulations 1993.</OffenceWording>\n" +
                    "                        <OffenceTiming>\n" +
                    "                            <OffenceDateCode>1</OffenceDateCode>\n" +
                    "                            <OffenceStart>\n" +
                    "                                <OffenceDateStartDate>2018-08-22</OffenceDateStartDate>\n" +
                    "                                <OffenceStartTime>08:02:00Z</OffenceStartTime>\n" +
                    "                            </OffenceStart>\n" +
                    "                        </OffenceTiming>\n" +
                    "                        <ChargeDate>2019-01-02</ChargeDate>\n" +
                    "                        <ArrestDate>2019-01-02</ArrestDate>\n" +
                    "                        <VehicleRelatedOffence>\n" +
                    "                            <VehicleCode>O</VehicleCode>\n" +
                    "                            <VehicleRegistrationMark>FD56BKF</VehicleRegistrationMark>\n" +
                    "                        </VehicleRelatedOffence>\n" +
                    "                    </BaseOffenceDetails>\n" +
                    "                    <ProsecutionFacts>At 08:02 on 22/08/2018 the defendant drove VAUXHALL index FD56BKF on A133 and was not wearing a seat belt which was fitted.</ProsecutionFacts>\n" +
                    "                </Offence>\n" +
                    "            </Defendant>\n" +
                    "        </Case>\n" +
                    "    </NewCaseMessage>\n" +
                    "</submitNewCaseMessageRequest>\n";

    static void setValueForField(final String field, final String newValue) {
        final String oldField = extractValue(Pattern.compile("<(.*)" + field + ">(.*?)<(.*)" + field + ">"));
        final String newField = "<" + field + ">" + newValue + "</" + field + ">";
        DATA = DATA.replace(oldField, newField);
    }

    static String getData() {
        return DATA;
    }

    private static String extractValue(final Pattern pattern) {
        Matcher matcher = pattern.matcher(DATA);
        String value = "";
        if (matcher.find()) {
            value = matcher.group(0);
        }
        return value;
    }


}
