package se.nordicpeak.accesscard;

public class AccessCardInfo {
    private final String employeeID;
    private final String firstName;
    private final String lastName;
    private final String profileImage;
    private final String employmentDate;
    private final String departmentName;
    private final String location;


    public AccessCardInfo(String employeeID, String firstName, String lastName, String profileImage, String employmentDate, String departmentName, String location) {
        this.employeeID = employeeID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileImage = profileImage;
        this.employmentDate = employmentDate;
        this.departmentName = departmentName;
        this.location = location;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getEmploymentDate() {
        return employmentDate;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getLocation() {
        return location;
    }
}
