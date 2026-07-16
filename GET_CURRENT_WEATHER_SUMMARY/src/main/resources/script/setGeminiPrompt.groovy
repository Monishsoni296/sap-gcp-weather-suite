import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.time.Instant
import java.time.ZoneId

// Script to set Gemini prompt to message body
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
    message.setProperty('timestamp', zonedString)

    def prompt = """You are a concise, helpful weather assistant. Analyze this data and provide a weather summary:
                    Address: ${headers.get("formattedAdr")}
                    Temperature: ${json?.temperature?.degrees} ${json?.temperature?.unit}
                    Humidity: ${json?.relativeHumidity}
                    Weather Condition: ${json?.weatherCondition?.description?.text}
                    Wind Speed: ${json?.wind?.speed?.value} ${json?.wind?.speed?.unit}
                    Wind Direction: ${json?.wind?.direction?.degrees} ${json?.wind?.direction?.cardinal}
                    Precipitation Type: ${json?.precipitation?.probability?.type}
                    Precipitation Probability: ${json?.precipitation?.probability?.percent}
                    Visibility: ${json?.visibility?.distance} ${json?.visibility?.unit}
                    UV Index: ${json?.uvIndex}
                    Cloud Cover: ${json?.cloudCover}
                    Is Daytime: ${json?.isDaytime}
                    Please provide the summary in a single paragraph."""

    def responseMap = [
        "contents": [
            [
                "parts": [
                    [
                        "text": prompt
                    ]
                ]
            ]
        ],
        "generationConfig": [
            "responseMimeType": "text/plain"
        ]
    ]
    
    // Serialize the Groovy Map structure into a valid JSON string
    def jsonString = JsonOutput.toJson(responseMap)
    message.setBody(jsonString)
    return message
}