import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import java.io.StringWriter

// Script to determine if a weather alert should be triggered based on the JSON response
def Message processData(Message msg) {
    def headers = msg.getHeaders()
    def body = msg.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    // Renamed local variables to avoid node-name conflicts in MarkupBuilder
    def loc = headers.get('formattedAdr') ?: headers.get('address') ?: message.getProperty('address') ?: 'Unknown'
    def lat = headers.get('latitude') ?: 0
    def lon = headers.get('longitude') ?: 0
    
    def maxTemp = json.temperature.degrees
    def rainProbability = json.precipitation.probability.percent
    def thunderstormProbability = json.thunderstormProbability
    def windGust = json.wind.gust.value

    def alertData = [
        type: "No Alert",
        messageText: "Current conditions are normal.",
        recommendationText: "No special precautions needed."
    ]
    
    if (maxTemp > 30) {
        alertData.type = "High Temperature Alert"
        alertData.messageText = "Temperature is expected to exceed 30°C."
        alertData.recommendationText = "Stay hydrated and avoid prolonged exposure to the sun."
    }
    if (rainProbability > 80) {
        alertData.type = "Heavy Rain Alert"
        alertData.messageText = "High chance of heavy rain."
        alertData.recommendationText = "Carry an umbrella and be cautious of potential flooding."
    }
    if (thunderstormProbability > 50) {
        alertData.type = "Thunderstorm Alert"
        alertData.messageText = "High chance of thunderstorms."
        alertData.recommendationText = "Seek shelter indoors and avoid using electrical appliances."
    }
    if (windGust > 50) {
        alertData.type = "High Wind Alert"
        alertData.messageText = "Wind gusts are expected to exceed 50 km/h."
        alertData.recommendationText = "Secure loose objects outdoors and avoid unnecessary travel."
    }

    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)

    // Safely generate the XML tags using the map properties
    xml.Results {
        result {
            location(loc)
            status("SUCCESS")
            coordinates {
                latitude(lat)
                longitude(lon)
            }
            alert(alertData.type)
            message(alertData.messageText)
            recommendation(alertData.recommendationText)
        }
    }
    
    msg.setBody(writer.toString())
    return msg
}