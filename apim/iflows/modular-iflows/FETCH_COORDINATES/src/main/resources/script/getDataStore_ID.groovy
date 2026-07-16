import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {
    // Fetch header and properties
    def headers = message.getHeaders();
    def rawAddress = headers.get("address").toString().toLowerCase().replaceAll(/[^\w]/, "")
    
    String entryId = "ADDRESS_" + rawAddress;
    message.setProperty("CacheEntryID", entryId);
    
    // Log request Coordinates
    def messageLog = messageLogFactory?.getMessageLog(message)
    if (messageLog != null) {
        messageLog.addCustomHeaderProperty("Input Address", rawAddress)
        messageLog.addCustomHeaderProperty("CacheEntryID", entryId)
    }
    return message;
}