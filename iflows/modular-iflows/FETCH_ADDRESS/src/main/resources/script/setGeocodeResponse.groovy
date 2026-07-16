import com.sap.gateway.ip.core.customdev.util.Message;

// Script to set custom response for GCP Geocode API in case of status other than OK (200) or Bad Request (400) 
def Message processData(Message message) {
    def headers = message.getHeaders();
    def properties = message.getProperties();
    def messageLog = messageLogFactory?.getMessageLog(message)
    
    def status = headers.get("geocodeStatus")
    def errorMessage = properties.get("ErrorMessage")
    def responseMessage = ""
    def httpStatusCode = 500 // Default to Internal Server Error for unexpected statuses
    def lat = headers.get("Latitude")
    def lng = headers.get("Longitude")

    switch(status) {
        case "ZERO_RESULTS":
            responseMessage = "No results found for the given address."
            httpStatusCode = 404 // Not Found
            break
        case "OVER_QUERY_LIMIT":
            responseMessage = "Query limit exceeded. Please try again later."
            httpStatusCode = 429 // Too Many Requests
            break
        case "REQUEST_DENIED":
            responseMessage = "Request denied. Please check your API key and permissions."
            httpStatusCode = 403 // Forbidden
            break
        case "OVER_DAILY_LIMIT":
            responseMessage = "Daily request limit exceeded. Please try again tomorrow."
            httpStatusCode = 429 // Too Many Requests
            break
        case "INVALID_REQUEST":
            responseMessage = "Invalid request. Please check the address parameter."
            httpStatusCode = 400 // Bad Request
            break
        case "UNKNOWN_ERROR":
            responseMessage = "An unknown error occurred. Please try again."
            httpStatusCode = 500 // Internal Server Error
            break
        default:
            responseMessage = "An unexpected error occurred: No additional details available."
            httpStatusCode = 500 // Internal Server Error   
    }

    // Set the custom JSON response body and appropriate HTTP status code
    message.setHeader("Content-Type", "application/json")
    message.setHeader("CamelHttpStatusCode", httpStatusCode)
    String customJsonResponse = """{
        "error": {
            "status": "${status}",
            "lat": "${lat}",
            "lng": "${lng}",
            "details": "${responseMessage.replaceAll('"', '\\"')}"
        }
    }"""
    message.setBody(customJsonResponse)
    
    // Log error details for debugging purposes
    if (messageLog != null) {
        messageLog.addCustomHeaderProperty("Error_Status", status)
        messageLog.addAttachmentAsString("Error_Details", errorMessage, "text/plain")
    }
    return message;
}