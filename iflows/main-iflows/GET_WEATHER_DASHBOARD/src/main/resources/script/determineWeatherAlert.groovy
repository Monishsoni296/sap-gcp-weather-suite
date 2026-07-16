import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

// Script to determine if a weather alert should be triggered based on the JSON response
def Message processData(Message message) {
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    def currentTime = json.currentTime
    def maxTemp = json.temperature.degrees
    def rainProbability = json.precipitation.probability.percent
    def thunderstormProbability = json.thunderstormProbability
    def windGust = json.wind.gust.value

    def alert = "No Alert"
    def alert_message = "Current conditions are normal."
    def recommendation = "No special precautions needed."
    if (maxTemp > 30) {
        alert = "High Temperature Alert"
        alert_message = "Temperature is expected to exceed 30°C."
        recommendation = "Stay hydrated and avoid prolonged exposure to the sun."
    }
    if (rainProbability > 80) {
        alert = "Heavy Rain Alert"
        alert_message = "High chance of heavy rain."
        recommendation = "Carry an umbrella and be cautious of potential flooding."
    }
    if (thunderstormProbability > 50) {
        alert = "Thunderstorm Alert"
        alert_message = "High chance of thunderstorms."
        recommendation = "Seek shelter indoors and avoid using electrical appliances."
    }
    if (windGust > 50) {
        alert = "High Wind Alert"
        alert_message = "Wind gusts are expected to exceed 50 km/h."
        recommendation = "Secure loose objects outdoors and avoid unnecessary travel."
    }

    body = JsonOutput.toJson([alert: alert, message: alert_message, recommendation: recommendation])
    message.setBody(body)
    
    return message
}