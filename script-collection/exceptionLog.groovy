import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    def messageLog = messageLogFactory.getMessageLog(message)
    if (messageLog != null) {
        // Capture the standard Camel exception property
        def ex = message.getProperties().get("CamelExceptionCaught")
        if (ex != null) {
            messageLog.addAttachmentAsString("Exception_Stacktrace", ex.toString(), "text/plain")
            messageLog.addAttachmentAsString("Payload Log", message.getBody(String.class), "text/json");
        }
    }
    return message;
}