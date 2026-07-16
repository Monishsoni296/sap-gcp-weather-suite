import com.sap.gateway.ip.core.customdev.util.Message

// Script to check input parameters. If any of the parameters are invalid, it sets the appropriate HTTP response code and message in the message body.
def Message processData(Message message) {

    def rawAddress = message.getProperty("loc")
    def latProp = message.getProperty("lat")
    def lngProp = message.getProperty("lng")
    def units = message.getProperty("units")
    def unitsToForecast = message.getProperty("unitsToForecast")
    
    // Safe navigation operator used to handle case where unitsSystem might be null
    def unitsSystem = message.getProperty("unitsSystem")?.toString()?.trim()
    
    def messageLog = messageLogFactory.getMessageLog(message)
    def validCoordinates = true
    def validAddress = true
    def validUnits = true

    if (messageLog != null) {
        messageLog.addCustomHeaderProperty("Latitude", latProp ? latProp : "null")
        messageLog.addCustomHeaderProperty("Longitude", lngProp ? lngProp : "null")
        messageLog.addCustomHeaderProperty("Address", rawAddress ? rawAddress : "null")
        messageLog.addCustomHeaderProperty("Units", units ? units : "null")
        messageLog.addCustomHeaderProperty("Units To Forecast", unitsToForecast ? unitsToForecast : "null")
    }

    // 1. Check if lat & lng are valid decimals and within specified range
    if (latProp == null || lngProp == null) {
        validCoordinates = false
    }
    else {
        try {
            BigDecimal lat = new BigDecimal(latProp.toString().trim())
            BigDecimal lon = new BigDecimal(lngProp.toString().trim())

            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                validCoordinates = false
            }
        } catch (NumberFormatException e) {
            // Catches invalid decimals (e.g. string text or malformed numbers)
            validCoordinates = false
        }
    }
    
    if (rawAddress == null || rawAddress.toString().trim().isEmpty()) {
        validAddress = false
    }
    
    // 2. Check if unitsToForecast is an integer within [1 - 10] or [1 - 240]
    if (units == null || unitsToForecast == null) {
        validUnits = false
    }
    else if (units != "hours" && units != "days") {
        validUnits = false
    }
    else {
        try {
            def utfStr = unitsToForecast.toString().trim()
            
            // Regex to ensure the string represents a valid whole integer
            if (utfStr ==~ /^-?\d+$/) {
                int utf = utfStr.toInteger()
                
                if (units == "hours" && (utf < 1 || utf > 240)) {
                    validUnits = false
                }
                else if (units == "days" && (utf < 1 || utf > 10)) {
                    validUnits = false
                }
            } else {
                // Fails if it contains decimals (e.g., 12.5) or non-numeric characters
                validUnits = false
            }
        } catch (Exception e) {
            validUnits = false
        }
    }

    message.setProperty("validCoordinates", validCoordinates)
    message.setProperty("validAddress", validAddress)
    message.setProperty("validUnits", validUnits)

    if ((validCoordinates == false && validAddress == false) || validUnits == false) {
        message.setHeader("CamelHttpResponseCode", "400")
        message.setHeader("Content-Type", "text/plain")
        message.setBody("Null or Invalid coordinates, address, units provided")
        return message
    }

    if (validCoordinates == true) {
        message.setHeader("Latitude", latProp.toString().trim())
        message.setHeader("Longitude", lngProp.toString().trim())
    }

    if (validAddress == true) {
        message.setHeader("address", rawAddress.toString().trim())
    }
    
    message.setHeader("units", units)
    message.setHeader("unitsToForecast", unitsToForecast)
    message.setHeader("unitsSystem", unitsSystem ? unitsSystem : "METRIC")
    return message
}