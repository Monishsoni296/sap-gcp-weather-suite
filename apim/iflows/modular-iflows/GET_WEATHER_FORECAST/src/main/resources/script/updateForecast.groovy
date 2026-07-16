import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*;

// Script to add new record to the existing JSON payload
def Message processData(Message message) {
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)
    def properties = message.getProperties()
    def units = message.getHeaders().get("units")
    def masterForecast = properties.get("masterForecast")
    
    if (masterForecast == null) {
        masterForecast = []
    }
    
    def forecast = units?.toString() == "hours" ? json.forecastHours : json.forecastDays
    def hasMorePages = json.nextPageToken.toString() != "" ? "true" : "false"
    def nextPageToken = json.nextPageToken.toString() != "" ? json.nextPageToken.toString() : ""
    
    if (forecast != null) {
        masterForecast.addAll(forecast)
    }
    
    
    message.setProperty("masterForecast", masterForecast)
    message.setProperty("hasMorePages", hasMorePages)
    message.setProperty("nextPageToken", nextPageToken)
    message.setProperty("timeZone", json.timeZone)
    message.setHeader("timeZone", json.timeZone?.id)
    
    return message
}