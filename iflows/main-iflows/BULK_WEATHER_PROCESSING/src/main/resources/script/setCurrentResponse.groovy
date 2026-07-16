import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import java.time.*

// Script to set current weather forecast response to message body
def Message processData(Message message) {
    def headers = message.getHeaders()
    def properties = message.getProperties()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    // Set timestamp to provided timezone
    def ts = json.currentTime
    def tzId = json.timeZone.id
    def instant = Instant.parse(ts)
    def zone = ZoneId.of(tzId)
    def zoned = instant.atZone(zone)
    
    def xmlResponse = """<Results>"""
    xmlResponse += """<result>
        <locationId>${properties.get("locationId")}</locationId>
        <status>SUCCESS</status>
        <location>
            <latitude>${headers.get("latitude")}</latitude>
            <longitude>${headers.get("longitude")}</longitude>
        </location>
        <timestamp>${zoned.toString()}</timestamp>
        <currentWeather>
            <temperature>${json.temperature.degrees} ${json.temperature.unit}</temperature>
            <humidity>${json.relativeHumidity}</humidity>
            <description>
                <iconUri>${json.weatherCondition.iconBaseUri}</iconUri>
                <description>${json.weatherCondition.description.text}</description>
            </description>
            <windSpeed>${json.wind.speed.value}</windSpeed>
            <windDirection>${json.wind.direction.degrees} ${json.wind.direction.cardinal}</windDirection>
            <precipitation>
                <type>${json.precipitation.probability.type}</type>
                <percent>${json.precipitation.probability.percent}</percent>
            </precipitation>
            <visibility>${json.visibility.distance} ${json.visibility.unit}</visibility>
            <uvIndex>${json.uvIndex}</uvIndex>
            <cloudCover>${json.cloudCover}</cloudCover>
            <isDaytime>${json.isDaytime}</isDaytime>
        </currentWeather>
    </result>"""
    xmlResponse += """</Results>"""
    

    // Set the current weather forecast response to the message body
    message.setBody(xmlResponse)
    return message
}