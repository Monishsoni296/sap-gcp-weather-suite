import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import groovy.xml.MarkupBuilder
import java.io.StringWriter

def Message processData(Message message) {
    def properties = message.getProperties()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    // Safely get bounds and record count
    def max_loc = properties.get("maxLocations")?.toInteger() ?: 10
    def min_loc = properties.get("minLocations")?.toInteger() ?: 5
    def recordCount = json.locations?.size() ?: 0

    // 1. Validate Location Count Bounds
    if (recordCount > max_loc || recordCount < min_loc) {
        def errorJson = """{
            "error": "Invalid number of locations. Must be between ${min_loc} and ${max_loc}.",
            "receivedCount": ${recordCount}
        }"""
        return sendError(message, 400, errorJson)
    }
    
    // 2. Validate Request Type
    def requestType = json.requestType?.trim()?.toLowerCase() ?: ""
    if (!(requestType in ["current", "forecast", "alert"])) { 
        def errorJson = """{
            "error": "Invalid request type: ${requestType}. Must be current, forecast or alert."
        }"""
        return sendError(message, 400, errorJson)
    }
    message.setProperty("requestType", requestType)
    
    // 3. Validate Forecast Constraints
    if (requestType == "forecast") {
        def forecastType = json.forecastType?.trim()?.toLowerCase()
        // Safely check if it's a valid integer before converting
        def forecastUnits = json.forecastUnits?.toString()?.isInteger() ? json.forecastUnits.toInteger() : 0

        if (!(forecastType in ["hours", "days"]) || forecastUnits <= 0 || 
           (forecastType == "days" && forecastUnits > 10) || 
           (forecastType == "hours" && forecastUnits > 240)) {
            
            def errorJson = """{
                "error": "Invalid forecast request. Hours must be at most 240 and days must be at most 10."
            }"""
            return sendError(message, 400, errorJson)
        }
        
        message.setHeader("unitsToForecast", forecastUnits)
        message.setHeader("units", forecastType)
    }
    
    // 4. Safely Extract Locations and Build XML with MarkupBuilder
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)
    
    // This creates proper, escaped XML automatically
    xml.Locations {
        json.locations.each { loc ->
            Location {
                locationId(loc.locationId?.toString()?.trim() ?: "")
                address(loc.address?.toString()?.trim() ?: "")
            }
        }
    }

    message.setHeader("unitsSystem", json.unitsSystem ?: "METRIC")
    message.setHeader("requestedLocations", recordCount)
    message.setProperty("validRequest", true)
    message.setBody(writer.toString()) // Set the beautifully built XML as the body
    
    return message
}

// Reusable helper method to standardize error outputs
def Message sendError(Message message, int statusCode, String payload) {
    message.setProperty("validRequest", false)
    message.setHeader("Content-Type", "application/json")
    message.setHeader("CamelHttpResponseCode", statusCode.toString())
    message.setBody(payload)
    return message
}