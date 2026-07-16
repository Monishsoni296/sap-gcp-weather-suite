import com.sap.gateway.ip.core.customdev.util.Message

// Script to check and validate data store response
def Message processData(Message message) {
    
    def headers = message.getHeaders()
    def messageLog = messageLogFactory?.getMessageLog(message)

    def cacheHit = headers.get("SAP_DataStoreEntryFound")
    def enableLogging = properties.get("enableLogPayload")
    def cachePerformance = "MISS"

    // Cache HIT
    if (cacheHit != null && cacheHit.toString().equalsIgnoreCase("true")) {
        cachePerformance = "HIT"

        if (enableLogging.toString().equalsIgnoreCase("true")) {
            if (messageLog != null) {
                messageLog.addAttachmentAsString("Payload Log", message.getBody(String.class), "text/json");
            }
        }
    }

    if (messageLog != null) {
        messageLog.addCustomHeaderProperty("Cache_Performance", cachePerformance)
    }
    return message
}