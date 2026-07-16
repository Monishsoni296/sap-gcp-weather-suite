import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def properties = message.getProperties();
    def token = properties.get("nextPageToken");
    
    // If token exists and is not empty, build the query parameter string
    String pageQueryParam = (token != null && !token.trim().isEmpty()) ? "&pageToken=" + token.trim() : "";
    
    message.setProperty("pageQueryParam", pageQueryParam);
    return message;
}