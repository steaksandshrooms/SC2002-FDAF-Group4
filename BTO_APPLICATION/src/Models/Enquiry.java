package Models;

public class Enquiry {
    private String enquiryId;
    private String userId;
    private String projectName;
    private String message;
    private String response; // ✅ new field

    public Enquiry(String enquiryId, String userId, String projectName, String message) {
        this.enquiryId = enquiryId;
        this.userId = userId;
        this.projectName = projectName;
        this.message = message;
        this.response = ""; // default empty response
    }

    public String getEnquiryId() { return enquiryId; }
    public String getUserId() { return userId; }
    public String getProjectName() { return projectName; }
    public String getMessage() { return message; }
    public String getResponse() { return response; } // ✅ getter

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResponse(String response) { // ✅ setter
        this.response = response;
    }

    public String toCSV() {
        return enquiryId + "," + userId + "," + projectName + "," +
               message.replace(",", ";") + "," + response.replace(",", ";");
    }
}
