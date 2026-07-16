import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

// Script to determine if a weather alert should be triggered based on the JSON response
def Message processData(Message message) {
    def headers = message.getHeaders()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)
    
    def address = headers.get("formattedAdr")
    def lat = headers.get("Latitude")
    def lng = headers.get("Longitude")
    def currentTime = json.currentTime
    def maxTemp = json.temperature.degrees
    def rainProbability = json.precipitation.probability.percent
    def thunderstormProbability = json.thunderstormProbability
    def windGust = json.wind.gust.value

    def alert = "No Alert"
    if (maxTemp > 30) {
        alert = "High Temperature Alert"
    }
    if (rainProbability > 80) {
        alert = "Heavy Rain Alert"
    }
    if (thunderstormProbability > 50) {
        alert = "Thunderstorm Alert"
    }
    if (windGust > 50) {
        alert = "High Wind Alert"
    }

    body = JsonOutput.toJson([address: address, coordinates: [lat: lat, lng: lng], currentTime: currentTime, alert: alert])
    message.setBody(body)
    
    return message
}