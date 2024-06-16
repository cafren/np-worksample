package se.nordicpeak.accesscard;

import se.unlogic.standardutils.xml.XMLParser;

import java.util.List;

public class ValidationResult {
    private final List<XMLParser> validatedEmployeeNodes;
    private final List<XMLParser> validatedDepartmentNodes;

    public ValidationResult(List<XMLParser> validatedEmployeeNodes, List<XMLParser> validatedDepartmentNodes) {
        this.validatedEmployeeNodes = validatedEmployeeNodes;
        this.validatedDepartmentNodes = validatedDepartmentNodes;
    }

    public List<XMLParser> getValidatedEmployeeNodes() {
        return validatedEmployeeNodes;
    }

    public List<XMLParser> getValidatedDepartmentNodes() {
        return validatedDepartmentNodes;
    }
}
