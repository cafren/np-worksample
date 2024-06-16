package se.nordicpeak.accesscard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import se.unlogic.standardutils.validation.ValidationError;
import se.unlogic.standardutils.validation.ValidationErrorType;
import se.unlogic.standardutils.xml.XMLParser;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javafx.beans.binding.Bindings.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NordicPeakEmployeeXMLValidatorTest {

    private NordicPeakEmployeeXMLValidator validator;


    @BeforeEach
    public void setUp() {
        validator = new NordicPeakEmployeeXMLValidator();
    }

    private XMLParser createParserFromString(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
        return new XMLParser(document);
    }
    @Test
    void validateUniqueEntry_NoDuplicate() throws Exception {
        List<XMLParser> nodes = new ArrayList<>();
        XMLParser node1 = createParserFromString("<Employee><employeeID>1</employeeID></Employee>");
        XMLParser node2 = createParserFromString("<Employee><employeeID>2</employeeID></Employee>");

        nodes.add(node1);
        nodes.add(node2);

        validator.validateUniqueEntry("employeeID", nodes, node1);

        assertTrue(validator.getErrors().isEmpty());
    }

    @Test
    void validateUniqueEntry_Duplicate() throws Exception {
        List<XMLParser> nodes = new ArrayList<>();
        XMLParser node1 = createParserFromString("<Employee><employeeID>1</employeeID></Employee>");
        XMLParser node2 = createParserFromString("<Employee><employeeID>1</employeeID></Employee>");

        nodes.add(node1);
        nodes.add(node2);

        validator.validateUniqueEntry("employeeID", nodes, node1);
        assertFalse(validator.validateUniqueEntry("employeeID", nodes, node1));

        ValidationError expectedError = new ValidationError("employeeID", ValidationErrorType.Other, "1 is not unique.");


        assertTrue(validator.getErrors().stream().anyMatch(error ->
                error.getFieldName().equals(expectedError.getFieldName()) &&
                        error.getMessageKey().equals(expectedError.getMessageKey()) &&
                        error.getValidationErrorType() == expectedError.getValidationErrorType()
        ));
    }
}