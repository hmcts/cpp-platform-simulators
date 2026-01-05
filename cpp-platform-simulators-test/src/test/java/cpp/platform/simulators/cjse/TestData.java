package cpp.platform.simulators.cjse;

public interface TestData {

    String XML_MESSAGE_ESCAPED = "&lt;?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?&gt;&lt;RouteDataResp VersionNumber=\"1.0\" xmlns=\"http://schemas.cjse.gov.uk/common/operations\" xmlns:ns2=\"http://schemas.cjse.gov.uk/common/businessentities\" xmlns:ns3=\"http://schemas.cjse.gov.uk/common/businesstypes\"&gt;&lt;ResponseToSystem VersionNumber=\"1.0\"&gt;&lt;CorrelationID&gt;REQLUEST_AID&lt;/CorrelationID&gt;&lt;SystemID literalvalue=\"CJSE\"&gt;&lt;/SystemID&gt;&lt;OrganizationalUnitID literalvalue=\"CJIT CJSE\"&gt;Z000&lt;/OrganizationalUnitID&gt;&lt;DataController literalvalue=\"String\"&gt;&lt;/DataController&gt;&lt;User literalvalue=\"String\"&gt;lee.flaxington&lt;/User&gt;&lt;SourceID literalvalue=\"String\"&gt;00301PoliceCaseSystem&lt;/SourceID&gt;&lt;DestinationID literalvalue=\"String\"&gt;Z00CJSE&lt;/DestinationID&gt;&lt;TestOperation&gt;true&lt;/TestOperation&gt;&lt;/ResponseToSystem&gt;&lt;OperationStatus&gt;&lt;StatusClass&gt;FatalError&lt;/StatusClass&gt;&lt;Code&gt;301&lt;/Code&gt;&lt;ResponseContext&gt;UnknownSystem&lt;/ResponseContext&gt;&lt;RouteId&gt;001&lt;/RouteId&gt;&lt;/OperationStatus&gt;&lt;OperationStatus&gt;&lt;StatusClass&gt;FatalError&lt;/StatusClass&gt;&lt;Code&gt;307&lt;/Code&gt;&lt;ResponseContext&gt;InvalidDataController&lt;/ResponseContext&gt;&lt;RouteId&gt;001&lt;/RouteId&gt;&lt;/OperationStatus&gt;&lt;OperationStatus&gt;&lt;StatusClass&gt;FatalError&lt;/StatusClass&gt;&lt;Code&gt;308&lt;/Code&gt;&lt;ResponseContext&gt;InvalidOrganizationalUnitID&lt;/ResponseContext&gt;&lt;RouteId&gt;001&lt;/RouteId&gt;&lt;/OperationStatus&gt;&lt;OperationStatus&gt;&lt;StatusClass&gt;FatalError&lt;/StatusClass&gt;&lt;Code&gt;1317&lt;/Code&gt;&lt;ResponseContext&gt;UnknownRouteSourceSystem&lt;/ResponseContext&gt;&lt;RouteId&gt;001&lt;/RouteId&gt;&lt;/OperationStatus&gt;&lt;OperationStatus&gt;&lt;StatusClass&gt;FatalError&lt;/StatusClass&gt;&lt;Code&gt;1318&lt;/Code&gt;&lt;ResponseContext&gt;UnknownRouteDestinationSystem&lt;/ResponseContext&gt;&lt;RouteId&gt;001&lt;/RouteId&gt;&lt;/OperationStatus&gt;&lt;OperationStatus&gt;&lt;StatusClass&gt;FatalError&lt;/StatusClass&gt;&lt;Code&gt;1321&lt;/Code&gt;&lt;ResponseContext&gt;SameSourceAndDestinationSystem&lt;/ResponseContext&gt;&lt;RouteId&gt;001&lt;/RouteId&gt;&lt;/OperationStatus&gt;&lt;OperationStatus&gt;&lt;StatusClass&gt;FatalError&lt;/StatusClass&gt;&lt;Code&gt;1359&lt;/Code&gt;&lt;ResponseContext&gt;DisallowedDataStreamType&lt;/ResponseContext&gt;&lt;RouteId&gt;001&lt;/RouteId&gt;&lt;/OperationStatus&gt;&lt;/RouteDataResp&gt;";

    String XML_MESSAGE_UNESCAPED = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<RouteDataResp xmlns=\"http://schemas.cjse.gov.uk/common/operations\" xmlns:ns2=\"http://schemas.cjse.gov.uk/common/businessentities\" xmlns:ns3=\"http://schemas.cjse.gov.uk/common/businesstypes\" VersionNumber=\"1.0\">\n" +
            "   <ResponseToSystem VersionNumber=\"1.0\">\n" +
            "      <CorrelationID>REQLUEST_AID</CorrelationID>\n" +
            "      <SystemID literalvalue=\"CJSE\" />\n" +
            "      <OrganizationalUnitID literalvalue=\"CJIT CJSE\">Z000</OrganizationalUnitID>\n" +
            "      <DataController literalvalue=\"String\" />\n" +
            "      <User literalvalue=\"String\">lee.flaxington</User>\n" +
            "      <SourceID literalvalue=\"String\">00301PoliceCaseSystem</SourceID>\n" +
            "      <DestinationID literalvalue=\"String\">Z00CJSE</DestinationID>\n" +
            "      <TestOperation>true</TestOperation>\n" +
            "   </ResponseToSystem>\n" +
            "   <OperationStatus>\n" +
            "      <StatusClass>FatalError</StatusClass>\n" +
            "      <Code>301</Code>\n" +
            "      <ResponseContext>UnknownSystem</ResponseContext>\n" +
            "      <RouteId>001</RouteId>\n" +
            "   </OperationStatus>\n" +
            "   <OperationStatus>\n" +
            "      <StatusClass>FatalError</StatusClass>\n" +
            "      <Code>307</Code>\n" +
            "      <ResponseContext>InvalidDataController</ResponseContext>\n" +
            "      <RouteId>001</RouteId>\n" +
            "   </OperationStatus>\n" +
            "   <OperationStatus>\n" +
            "      <StatusClass>FatalError</StatusClass>\n" +
            "      <Code>308</Code>\n" +
            "      <ResponseContext>InvalidOrganizationalUnitID</ResponseContext>\n" +
            "      <RouteId>001</RouteId>\n" +
            "   </OperationStatus>\n" +
            "   <OperationStatus>\n" +
            "      <StatusClass>FatalError</StatusClass>\n" +
            "      <Code>1317</Code>\n" +
            "      <ResponseContext>UnknownRouteSourceSystem</ResponseContext>\n" +
            "      <RouteId>001</RouteId>\n" +
            "   </OperationStatus>\n" +
            "   <OperationStatus>\n" +
            "      <StatusClass>FatalError</StatusClass>\n" +
            "      <Code>1318</Code>\n" +
            "      <ResponseContext>UnknownRouteDestinationSystem</ResponseContext>\n" +
            "      <RouteId>001</RouteId>\n" +
            "   </OperationStatus>\n" +
            "   <OperationStatus>\n" +
            "      <StatusClass>FatalError</StatusClass>\n" +
            "      <Code>1321</Code>\n" +
            "      <ResponseContext>SameSourceAndDestinationSystem</ResponseContext>\n" +
            "      <RouteId>001</RouteId>\n" +
            "   </OperationStatus>\n" +
            "   <OperationStatus>\n" +
            "      <StatusClass>FatalError</StatusClass>\n" +
            "      <Code>1359</Code>\n" +
            "      <ResponseContext>DisallowedDataStreamType</ResponseContext>\n" +
            "      <RouteId>001</RouteId>\n" +
            "   </OperationStatus>\n" +
            "</RouteDataResp>";


    String MESSAGE_WITH_URN_1234_AND_FAMILY_NAME_SMITH = "<?xml version=\"1.0\" ?>\n" +
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
            "                <ProsecutorReference>1900XX0000000000061V</ProsecutorReference>\n" +
            "                <PoliceIndividualDefendant>\n" +
            "                    <PersonDefendant>\n" +
            "                        <BasePersonDetails>\n" +
            "                            <PersonName>\n" +
            "                                <PersonTitle>Mr</PersonTitle>\n" +
            "                                <PersonGivenName1>Eden</PersonGivenName1>\n" +
            "                                <PersonGivenName2>Frank</PersonGivenName2>\n" +
            "                                <PersonFamilyName>SMITH</PersonFamilyName>\n" +
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


    String MESSAGE_WITH_URN_4321_AND_FAMILY_NAME_HURLEY =
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
                    "            <PTIURN>4321</PTIURN>\n" +
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
                    "                <ProsecutorReference>1900XX0000000000061V</ProsecutorReference>\n" +
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


    String MESSAGE_WITH_URN_42TK1000218_AND_FAMILY_NAME_PAUL = "<?xml version=\"1.0\" ?>\n" +
            "<?xml-stylesheet href=\"ensemble/Ensemble_XMLDisplay.xsl\" type=\"text/xsl\" ?>\n" +
            "<!--type: SOAPS.TWIF.Services.TWIFService.Libra.StdProsPoliceResultedCaseStructure  id: 415730 -->\n" +
            "<s01:StdProsPoliceResultedCaseStructure\n" +
            "    xmlns:s = \"http://www.w3.org/2001/XMLSchema\"\n" +
            "    xmlns:s01 = \"http://www.dca.gov.uk/xmlschemas/libra\"\n" +
            "    xmlns:xsi = \"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "    Flow = \"ResultedCasesForThePolice\"\n" +
            "    Interface = \"LibraStandardProsecutorPolice\"\n" +
            "    SchemaVersion = \"0.6g\">\n" +
            "    <Session>\n" +
            "        <CourtHearing>\n" +
            "            <Hearing>\n" +
            "                <CourtHearingLocation>B42CM75</CourtHearingLocation>\n" +
            "                <DateOfHearing>2019-02-08</DateOfHearing>\n" +
            "                <TimeOfHearing>10:00:00Z</TimeOfHearing>\n" +
            "            </Hearing>\n" +
            "            <PSAcode>1970</PSAcode>\n" +
            "        </CourtHearing>\n" +
            "        <Case>\n" +
            "            <PTIURN>42TK1000218</PTIURN>\n" +
            "      <CorrelationID>REQLUEST_AID</CorrelationID>\n" +
            "            <Defendant>\n" +
            "                <ProsecutorReference>1900XX0000000000061V</ProsecutorReference>\n" +
            "                <CourtIndividualDefendant>\n" +
            "                    <PersonDefendant>\n" +
            "                        <BasePersonDetails>\n" +
            "                            <PersonName>\n" +
            "                                <PersonTitle>Mr</PersonTitle>\n" +
            "                                <PersonGivenName1>Eden</PersonGivenName1>\n" +
            "                                <PersonGivenName2>Frank</PersonGivenName2>\n" +
            "                                <PersonFamilyName>PAUL</PersonFamilyName>\n" +
            "                            </PersonName>\n" +
            "                            <Birthdate>1994-05-27</Birthdate>\n" +
            "                            <Gender>1</Gender>\n" +
            "                        </BasePersonDetails>\n" +
            "                    </PersonDefendant>\n" +
            "                    <BailStatus>A</BailStatus>\n" +
            "                    <Address>\n" +
            "                        <SimpleAddress>\n" +
            "                            <AddressLine1>43A</AddressLine1>\n" +
            "                            <AddressLine2>Dockfield Avenue</AddressLine2>\n" +
            "                            <AddressLine3>Harwich</AddressLine3>\n" +
            "                            <AddressLine4>Essex</AddressLine4>\n" +
            "                            <AddressLine5>CO12 4LF</AddressLine5>\n" +
            "                        </SimpleAddress>\n" +
            "                    </Address>\n" +
            "                    <PresentAtHearing>N</PresentAtHearing>\n" +
            "                </CourtIndividualDefendant>\n" +
            "                <Offence>\n" +
            "                    <BaseOffenceDetails>\n" +
            "                        <OffenceSequenceNumber>1</OffenceSequenceNumber>\n" +
            "                        <OffenceCode>RT88509</OffenceCode>\n" +
            "                        <OffenceWording>On 22/08/2018 at Clacton in the county of Essex drove a motor vehicle, namely VAUXHALL index FD56BKF, on a road namely A133, whilst not wearing an adult belt in conformity with regulation 5(1)(a) of the Motor Vehicles (Wearing of seat belts) Regulations 1993.;Contrary to section 14(3) of the Road Traffic Act 1988 and Schedule 2 to the Road Traffic Offenders Act 1988.</OffenceWording>\n" +
            "                        <OffenceTiming>\n" +
            "                            <OffenceDateCode>1</OffenceDateCode>\n" +
            "                            <OffenceStart>\n" +
            "                                <OffenceDateStartDate>2018-08-22</OffenceDateStartDate>\n" +
            "                            </OffenceStart>\n" +
            "                        </OffenceTiming>\n" +
            "                        <ChargeDate>2019-01-02</ChargeDate>\n" +
            "                        <ArrestDate>2019-01-02</ArrestDate>\n" +
            "                        <LocationOfOffence>not applicable</LocationOfOffence>\n" +
            "                        <VehicleRelatedOffence>\n" +
            "                            <VehicleCode>O</VehicleCode>\n" +
            "                            <VehicleRegistrationMark>FD56BKF</VehicleRegistrationMark>\n" +
            "                        </VehicleRelatedOffence>\n" +
            "                    </BaseOffenceDetails>\n" +
            "                    <InitiatedDate>2019-01-02</InitiatedDate>\n" +
            "                    <Plea>3</Plea>\n" +
            "                    <ModeOfTrial>1</ModeOfTrial>\n" +
            "                    <FinalDisposalIndicator>Y</FinalDisposalIndicator>\n" +
            "                    <ConvictionDate>2019-02-08</ConvictionDate>\n" +
            "                    <ConvictingCourt>1970</ConvictingCourt>\n" +
            "                    <Finding>G</Finding>\n" +
            "                    <Result>\n" +
            "                        <ResultCode>3011</ResultCode>\n" +
            "                        <ResultText>To pay costs of £30.00.</ResultText>\n" +
            "                        <Outcome>\n" +
            "                            <ResultAmountSterling>30</ResultAmountSterling>\n" +
            "                        </Outcome>\n" +
            "                    </Result>\n" +
            "                    <Result>\n" +
            "                        <ResultCode>1015</ResultCode>\n" +
            "                        <ResultText>Fined £220.00.</ResultText>\n" +
            "                        <Outcome>\n" +
            "                            <ResultAmountSterling>220</ResultAmountSterling>\n" +
            "                        </Outcome>\n" +
            "                    </Result>\n" +
            "                    <Result>\n" +
            "                        <ResultCode>3117</ResultCode>\n" +
            "                        <ResultText>To pay a surcharge to fund victim services of £30.00.</ResultText>\n" +
            "                        <Outcome>\n" +
            "                            <ResultAmountSterling>30</ResultAmountSterling>\n" +
            "                        </Outcome>\n" +
            "                    </Result>\n" +
            "                    <Result>\n" +
            "                        <ResultText>Collection order made.</ResultText>\n" +
            "                    </Result>\n" +
            "                </Offence>\n" +
            "            </Defendant>\n" +
            "        </Case>\n" +
            "    </Session>\n" +
            "</s01:StdProsPoliceResultedCaseStructure>\n";
}
