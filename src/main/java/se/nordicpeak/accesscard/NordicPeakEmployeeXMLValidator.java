package se.nordicpeak.accesscard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import se.unlogic.standardutils.populators.IntegerPopulator;
import se.unlogic.standardutils.populators.StringPopulator;
import se.unlogic.standardutils.validation.Birthdate8DigitValidator;
import se.unlogic.standardutils.validation.ValidationError;
import se.unlogic.standardutils.validation.ValidationErrorType;
import se.unlogic.standardutils.xml.XMLParser;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static se.unlogic.standardutils.xml.XMLValidationUtils.validateNotEmptyParameter;
import static se.unlogic.standardutils.xml.XMLValidationUtils.validateParameter;

public class NordicPeakEmployeeXMLValidator {
    private static final Logger logger = LogManager.getLogger(NordicPeakEmployeeXMLValidator.class);
    private final List<ValidationError> errors;
    private List<XMLParser> departmentNodes;
    private List<XMLParser> employeeNodes;
    private final Set<XMLParser> employeeNodesToRemove;
    private final Set<XMLParser> departmentNodesToRemove;

    public NordicPeakEmployeeXMLValidator() {
        this.errors = new ArrayList<>();
        this.departmentNodes = new ArrayList<>();
        this.employeeNodes = new ArrayList<>();
        this.employeeNodesToRemove = new HashSet<>();
        this.departmentNodesToRemove = new HashSet<>();
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public List<XMLParser> getDepartmentNodes() {
        return departmentNodes;
    }

    public List<XMLParser> getEmployeeNodes() {
        return employeeNodes;
    }

    public Set<XMLParser> getEmployeeNodesToRemove() {
        return employeeNodesToRemove;
    }

    public Set<XMLParser> getDepartmentNodesToRemove() {
        return departmentNodesToRemove;
    }


    public ValidationResult validateNodes(String XMLPath) {
        try {
            initializeAndParseXML(XMLPath);
            validateAllEmployeeNodes();
            validateAllDepartmentNodes();
            validateManagerAndDepartmentRelationships();
            logErrors(errors);
            cleanupInvalidNodes();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return new ValidationResult(employeeNodes, departmentNodes);
    }

    private void initializeAndParseXML(String XMLPath) throws IOException, ParserConfigurationException, SAXException {
        XMLParser xmlParser = new XMLParser(XMLPath);
        employeeNodes = xmlParser.getNodes("//Employee", false);
        departmentNodes = xmlParser.getNodes("//Department", false);
    }

    private void validateAllEmployeeNodes() {
        for (XMLParser employeeNode : employeeNodes) {
            validateEmployeeNode(employeeNode);
        }
        employeeNodes.removeAll(getEmployeeNodesToRemove());
    }

    private void validateAllDepartmentNodes() {
        for (XMLParser departmentNode : departmentNodes) {
            validateDepartmentNode(departmentNode);
        }
        departmentNodes.removeAll(getDepartmentNodesToRemove());
    }

    private void validateManagerAndDepartmentRelationships() {
        for (XMLParser departmentNode : departmentNodes) {
            validateManagerIsValidated(departmentNode, employeeNodes);
        }
        departmentNodes.removeAll(getDepartmentNodesToRemove());

        for (XMLParser employeeNode : employeeNodes) {
            validateDepartmentIsValidated(employeeNode, departmentNodes);
        }
        employeeNodes.removeAll(getEmployeeNodesToRemove());
    }

    private void cleanupInvalidNodes() {
        employeeNodes.removeAll(getEmployeeNodesToRemove());
        departmentNodes.removeAll(getDepartmentNodesToRemove());
    }


    public void validateDepartmentNode(XMLParser departmentNode) {
        Integer value1 = validateParameter("departmentID", departmentNode, true, 1, 9999, new IntegerPopulator(), errors);
        String value2 = validateParameter("name", departmentNode, true, 1, 20, new StringPopulator(), errors);
        Integer value3 = validateParameter("managerID", departmentNode, true, 1, 9999, new IntegerPopulator(), errors);
        String value4 = validateParameter("location", departmentNode, true, 1, 20, new StringPopulator(), errors);
        Boolean value5 = validateUniqueEntry("departmentID", departmentNodes, departmentNode);

        if (value1 == null || value2 == null || value3 == null || value4 == null || value5 == null) {
            departmentNodesToRemove.add(departmentNode);
        }

    }

    public void validateEmployeeNode(XMLParser employeeNode) {
        Integer value1 = validateParameter("employeeID", employeeNode, true, 1, 9999, new IntegerPopulator(), errors);
        String value2 = validateParameter("firstname", employeeNode, true, 1, 20, new StringPopulator(), errors);
        String value3 = validateParameter("lastname", employeeNode, true, 1, 20, new StringPopulator(), errors);
        String value4 = validateCitizenIdentifier("citizenIdentifier", employeeNode, true);
        Date value5 = validateEmploymentDate("employmentDate", employeeNode, true);
        Integer value6 = validateParameter("departmentID", employeeNode, true, 1, 9999, new IntegerPopulator(), errors);
        BufferedImage value7 = validateProfileImage("profileImage", employeeNode, true);
        Boolean value8 = validateUniqueEntry("employeeID", employeeNodes, employeeNode);
        Boolean value9 = validateUniqueEntry("citizenIdentifier", employeeNodes, employeeNode);

        if (value1 == null || value2 == null || value3 == null || value4 == null || value5 == null || value6 == null || value7 == null || value8 == null || value9 == null) {
            employeeNodesToRemove.add(employeeNode);
        }

    }

    public String validateCitizenIdentifier(String fieldName, XMLParser xmlParser, boolean required) {
        String value = xmlParser.getString(fieldName);

        if(required && validateNotEmptyParameter(fieldName, xmlParser, errors) == null) {
            return null;

        }
        value = value.trim();

        if (!value.matches("\\d{8}-\\d{4}")) {
            ValidationError error = new ValidationError(fieldName, ValidationErrorType.InvalidFormat, value + " is not a valid citizen identifier.");
            errors.add(error);
            return null;
        }

        String birthdatePart = value.substring(0, 8);
        Birthdate8DigitValidator birthdateValidator = new Birthdate8DigitValidator();
        if (!birthdateValidator.validateFormat(birthdatePart)) {
            ValidationError error = new ValidationError(fieldName, ValidationErrorType.InvalidFormat, value + " is not a valid birthdate.");
            errors.add(error);
            return null;
        }

        return value;
    }

    public Date validateEmploymentDate(String fieldName, XMLParser xmlParser, boolean required) {
        String value = xmlParser.getString(fieldName);

        if (required && validateNotEmptyParameter(fieldName, xmlParser, errors) == null) {
            return null;
        }

        value = value.trim();
        Date date = parseDate(value, fieldName);

        if (date == null) {
            return null;
        }

        if (!validateDateRange(date, fieldName, value)) {
            return null;
        }

        return date;
    }

    private SimpleDateFormat getSimpleDateFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        sdf.setLenient(false);
        return sdf;
    }

    private Date parseDate(String value, String fieldName) {
        SimpleDateFormat sdf = getSimpleDateFormat();

        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            ValidationError error = new ValidationError(fieldName, ValidationErrorType.InvalidFormat, value + " is not a valid date");
            errors.add(error);
            return null;
        }
    }

    private boolean validateDateRange(Date date, String fieldName, String value) {
        SimpleDateFormat sdf = getSimpleDateFormat();

        try {
            Date earliestDate = sdf.parse("20100721");
            Date currentDate = new Date();

            if (date.before(earliestDate) || date.after(currentDate)) {
                ValidationError error = new ValidationError(fieldName, ValidationErrorType.Other, value + " is before " + sdf.format(earliestDate) + " or after " + sdf.format(currentDate));
                errors.add(error);
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    public BufferedImage validateProfileImage(String fieldName, XMLParser xmlParser, boolean required) {
        String base64String = xmlParser.getString(fieldName);
        if(required && validateNotEmptyParameter(fieldName, xmlParser, errors) == null) {
            return null;
        }
        try {
            return GraphicsUtil.decodeBase64ToImage(base64String);
        } catch (IllegalArgumentException e) {
            ValidationError error = new ValidationError("profileImage", ValidationErrorType.Other, "Invalid base64 string");
            errors.add(error);
            return null;
        } catch (IOException e) {
            ValidationError error = new ValidationError("profileImage", ValidationErrorType.Other, "Error reading image data");
            errors.add(error);
            return null;
        }
    }

    public Boolean validateUniqueEntry(String fieldName, List<XMLParser> nodes, XMLParser node) {
        String value = node.getString(fieldName);
        boolean duplicate = nodes.stream()
                .filter(currentNode -> !currentNode.equals(node))
                .anyMatch(currentNode -> value.equals(currentNode.getString(fieldName)));

        if (duplicate) {
            ValidationError error = new ValidationError(fieldName, ValidationErrorType.Other, value + " is not unique.");
            errors.add(error);
            return false;
        }
        return true;
    }

    public void validateManagerIsValidated(XMLParser departmentNode, List<XMLParser> employeeNodes) {
        String managerID = departmentNode.getString("managerID");
        boolean hasValidManager = employeeNodes.stream()
                .anyMatch(employeeNode -> managerID.equals(employeeNode.getString("employeeID")));

        if (!hasValidManager) {
            ValidationError error = new ValidationError("managerID", ValidationErrorType.Other, managerID + " is not a valid manager.");
            errors.add(error);
            departmentNodesToRemove.add(departmentNode);
        }
    }

    public void validateDepartmentIsValidated(XMLParser employeeNode, List<XMLParser> departmentNodes) {
        String departmentID = employeeNode.getString("departmentID");
        boolean hasValidDepartment = departmentNodes.stream()
                .anyMatch(departmentNode -> departmentID.equals(departmentNode.getString("departmentID")));

        if (!hasValidDepartment) {
            ValidationError error = new ValidationError("departmentID", ValidationErrorType.Other, departmentID + " is not a valid department.");
            errors.add(error);
            employeeNodesToRemove.add(employeeNode);
        }
    }

    public static void logErrors(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            logger.error("Logging: " + error);
        }
    }

}
