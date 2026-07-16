import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import java.time.Instant
import java.time.ZoneId

// Script to set current weather forecast response to message body
def Message processData(Message message) {
    def headers = message.getHeaders()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    // Set timestamp to provided timezone
    def ts = json.currentTime
    def tzId = json.timeZone.id
    def instant = Instant.parse(ts)
    def zone = ZoneId.of(tzId)
    def zonedString = instant.atZone(zone).toString()

    // Use JsonBuilder to construct the response safely
    def builder = new JsonBuilder()
    builder {
        address headers.get("formattedAdr")
        coordinates {
            lat headers.get("latitude")
            lng headers.get("longitude")
        }
        unitsSystem headers.get("unitsSystem")
        timestamp zonedString
        weather {
            temperature "${json?.temperature?.degrees} ${json?.temperature?.unit}"
            humidity json?.relativeHumidity
            description {
                iconUri json?.weatherCondition?.iconBaseUri
                description json?.weatherCondition?.description?.text
            }
            windSpeed json?.wind?.speed?.value
            windDirection "${json?.wind?.direction?.degrees} ${json?.wind?.direction?.cardinal}"
            precipitation {
                type json?.precipitation?.probability?.type
                percent json?.precipitation?.probability?.percent
            }
            visibility "${json?.visibility?.distance} ${json?.visibility?.unit}"
            uvIndex json?.uvIndex
            cloudCover json?.cloudCover
            isDaytime json?.isDaytime
        }
    }

    // Set the current weather forecast response to the message body
    message.setBody(builder.toString())
    return message
}