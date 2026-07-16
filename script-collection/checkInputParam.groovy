import com.sap.gateway.ip.core.customdev.util.Message

// Script to check input address and coordinates parameters. If any of the parameters are invalid, it sets the appropriate HTTP response code and message in the message body.
def Message processData(Message message) {

    def rawAddress = message.getProperty("loc")
    def latProp = message.getProperty("lat")
    def lngProp = message.getProperty("lng")
    def unitsSystem = message.getProperty("unitsSystem")
    def messageLog = messageLogFactory.getMessageLog(message)
    def validCoordinates = true
    def validAddress = true

    if (latProp == null || lngProp == null) {
        validCoordinates = false
    }
    else if (latProp != null && lngProp != null) {
        BigDecimal lat = new BigDecimal(latProp.toString())
        BigDecimal lon = new BigDecimal(lngProp.toString())

        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            validCoordinates = false
        }
    }
    
    if (rawAddress == null || rawAddress.toString().trim().isEmpty()) {
        validAddress = false
    }

    message.setProperty("validCoordinates", validCoordinates)
    message.setProperty("validAddress", validAddress)

    if (validCoordinates == false && validAddress == false) {
        message.setHeader("CamelHttpResponseCode", "400")
        message.setHeader("Content-Type", "text/plain")
        message.setBody("Invalid coordinates or address provided")
        return message
    }

    if (validCoordinates == true) {
        message.setHeader("Latitude", latProp.toString().trim())
        message.setHeader("Longitude", lngProp.toString().trim())
    }

    if (validAddress == true) {
        message.setHeader("address", rawAddress.toString().trim());
    }
    
    message.setHeader("unitsSystem", unitsSystem ? unitsSystem : "METRIC")
    return message
}