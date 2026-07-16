import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import groovy.xml.MarkupBuilder
import java.io.StringWriter
import java.time.*

// Script to set Alert forecast response to message body
def Message processData(Message message) {
    def headers = message.getHeaders()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    // Renamed local variables to avoid node-name conflicts in MarkupBuilder
    def loc = headers.get('formattedAdr') ?: headers.get('address') ?: message.getProperty('address') ?: 'Unknown'
    def lat = headers.get('latitude') ?: 0
    def lon = headers.get('longitude') ?: 0
    
    def timeZoneId = 'UTC'

    // Use StringWriter and MarkupBuilder to construct XML
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)

    xml.Results {
        result {
            location(loc)
            status("SUCCESS")
            coordinates {
                latitude(lat)
                longitude(lon)
            }
            json.weatherAlerts == "" ? alert{title("No alert")} : alert {
                alertID(json.weatherAlerts.alertId)
                alertTitle(json.weatherAlerts.alertTitle.text)
                eventType(json.weatherAlerts.eventType)
                areaName(json.weatherAlerts.areaName)
                description(json.weatherAlerts.description)
                severity(json.weatherAlerts.severity)
                certainty(json.weatherAlerts.certainty)
                startTime(json.weatherAlerts.startTime)
                expirationTime(json.weatherAlerts.expirationTime)
                dataSource{
                    publisher(json.weatherAlerts.dataSource.publisher)
                    name(json.weatherAlerts.dataSource.name)
                    authorityUri(json.weatherAlerts.dataSource.authorityUri)
                }
            }
        }
    }

    message.setBody(writer.toString())
    return message
}

def setTimeZone(timestamp, timeZoneId) {
    if (!timestamp) return ""
    def instant = Instant.parse(timestamp)
    def zone = ZoneId.of(timeZoneId)
    def zonedDateTime = instant.atZone(zone)
    return zonedDateTime.toString()
}