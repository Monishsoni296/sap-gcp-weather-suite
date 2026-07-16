import com.sap.gateway.ip.core.customdev.util.Message;
import java.math.RoundingMode

// This script is intended to set Data Store Entry ID in the message properties for later use in the integration flow.
def Message processData(Message message) {
    
    def header = message.getHeaders()
    def rawlat = header.get("Latitude")
    def rawlon = header.get("Longitude")

    def latStr = rawlat.toString().trim()
    def lonStr = rawlon.toString().trim()

    BigDecimal lat = parseCoordinate(latStr, "Latitude")
    BigDecimal lon = parseCoordinate(lonStr, "Longitude")

    lat = lat.setScale(2, RoundingMode.DOWN)
    lon = lon.setScale(2, RoundingMode.DOWN)

    def entryId = "WEATHER_ALERT_${lat.toPlainString().replace('.', '_')}_${lon.toPlainString().replace('.', '_')}"
    message.setProperty("CacheEntryID", entryId)
    
    // Log request Coordinates
    def messageLog = messageLogFactory?.getMessageLog(message)
    if (messageLog != null) {
        messageLog.addCustomHeaderProperty("Latitude", rawlat)
        messageLog.addCustomHeaderProperty("Longitude", rawlon)
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