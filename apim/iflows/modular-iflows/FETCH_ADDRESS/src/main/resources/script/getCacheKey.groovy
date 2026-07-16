import com.sap.gateway.ip.core.customdev.util.Message

// Generate Cache ID
def Message processData(Message message) {
    // Fetch message header
    def headers = message.getHeaders()
    def rawLat = headers.get("Latitude")
    def rawLon = headers.get("Longitude")

    def latStr = rawLat.toString().trim()
    def lonStr = rawLon.toString().trim()

    def entryId = "COORDINATES_${latStr.replace('.', '_')}_${lonStr.replace('.', '_')}"
    message.setProperty("CacheEntryID", entryId)
    
    // Log request Coordinates
    def messageLog = messageLogFactory?.getMessageLog(message)
    if (messageLog != null) {
        messageLog.addCustomHeaderProperty("Latitude", rawLat)
        messageLog.addCustomHeaderProperty("Longitude", rawLon)
        messageLog.addCustomHeaderProperty("CacheEntryID", entryId)
    }
    return message
}