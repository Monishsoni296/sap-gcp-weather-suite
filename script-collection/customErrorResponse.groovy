import com.sap.gateway.ip.core.customdev.util.Message;

// Script to handle exceptions in the message processing flow and return a custom error response in JSON format
def Message processData(Message message) {
    
    String errorMsg = "An internal server error occurred while processing your request. Please contact integration support."
    String customErrorJson = """{
        "error": {
            "status": "Service Unavailable",
            "details": "${errorMsg.replaceAll('"', '\\"')}",
            "timestamp": "${java.time.ZonedDateTime.now().toString()}"
        }
    }"""
    message.setBody(customErrorJson)
    message.setHeader("Content-Type", "application/json")
    message.setHeader("CamelHttpResponseCode", 500)
    message.setHeader("X-Data-Source", "None-Offline")
    return message;
}