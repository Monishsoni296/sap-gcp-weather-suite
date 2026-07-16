import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

// Script to check if weather alerts are present in the JSON response and set a property accordingly
def Message processData(Message message) {
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    def alert = json.weatherAlerts
    if (alert) {
        message.setProperty("alertPresent", "true")
    } else {
        message.setProperty("alertPresent", "false")
    }

    return message
}