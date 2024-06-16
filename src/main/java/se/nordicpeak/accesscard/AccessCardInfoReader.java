package se.nordicpeak.accesscard;

import se.unlogic.standardutils.xml.XMLParser;

import java.util.ArrayList;
import java.util.List;

public class AccessCardInfoReader {
    private String filePath;
    private NordicPeakEmployeeXMLValidator validator;

    public AccessCardInfoReader(String filePath, NordicPeakEmployeeXMLValidator validator) {
        this.filePath = filePath;
        this.validator = validator;
    }
    public AccessCardInfoReader(String filePath) {
        this.filePath = filePath;
        this.validator = new NordicPeakEmployeeXMLValidator();
    }

    public List<AccessCardInfo> getAccessCardInfo() {
        ValidationResult validationResult = validator.validateNodes(filePath);

        List<AccessCardInfo> accessCardInfos = new ArrayList<>();

        for (XMLParser employeeNode : validationResult.getValidatedEmployeeNodes()) {
            XMLParser matchedDepartmentNode = null;

            for (XMLParser departmentNode : validationResult.getValidatedDepartmentNodes()) {
                if (departmentNode.getString("departmentID").equals(employeeNode.getString("departmentID"))) {
                    matchedDepartmentNode = departmentNode;
                    break;
                }
            }

            if (matchedDepartmentNode != null) {
                AccessCardInfo accessCardInfo = new AccessCardInfo(
                        employeeNode.getString("employeeID"),
                        employeeNode.getString("firstName"),
                        employeeNode.getString("lastName"),
                        employeeNode.getString("profileImage"),
                        employeeNode.getString("employmentDate"),
                        matchedDepartmentNode.getString("name"),
                        matchedDepartmentNode.getString("location")
                );
                accessCardInfos.add(accessCardInfo);
            }
        }

        return accessCardInfos;
    }

}
