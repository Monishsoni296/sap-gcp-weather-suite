import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def properties = message.getProperties();
    def enableLogging = properties.get("enableLogPayload")
    
    // Log the payload if logging is enabled
    if (enableLogging.toString().equalsIgnoreCase("true")) {
        def messageLog = messageLogFactory.getMessageLog(message);
        if (messageLog != null) {
            messageLog.addAttachmentAsString("Payload Log", message.getBody(String.class), "text/json");
        }
    }

    return message;
}