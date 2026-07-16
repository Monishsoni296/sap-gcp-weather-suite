import com.sap.gateway.ip.core.customdev.util.Message
import java.math.RoundingMode

// Check request coordinates and generate Cache ID
def Message processData(Message message) {
    // Fetch message header
    def headers = message.getHeaders()
    def rawLat = headers.get("Latitude")
    def rawLon = headers.get("Longitude")
    def unitsSystem = headers.get("unitsSystem")

    def latStr = rawLat.toString().trim()
    def lonStr = rawLon.toString().trim()

    BigDecimal lat = parseCoordinate(latStr, "Latitude")
    BigDecimal lon = parseCoordinate(lonStr, "Longitude")

    lat = lat.setScale(2, RoundingMode.DOWN)
    lon = lon.setScale(2, RoundingMode.DOWN)

    def entryId = "WEATHER_${unitsSystem}_${lat.toPlainString().replace('.', '_')}_${lon.toPlainString().replace('.', '_')}"
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

private BigDecimal parseCoordinate(String value, String name) {
    try {
        return new BigDecimal(value)
    } catch (NumberFormatException e) {
        throw new java.lang.RuntimeException("Validation Failed: Coordinates must be valid numeric decimals. Found ${name}: ${value}")
    }
}