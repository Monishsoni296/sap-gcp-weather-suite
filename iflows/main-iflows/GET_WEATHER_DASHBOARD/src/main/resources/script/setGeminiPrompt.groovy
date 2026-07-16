import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

// Script to set Gemini prompt to message body
def Message processData(Message message) {
    def headers = message.getHeaders()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    def prompt = "You are a concise, helpful weather assistant. Analyze this data and provide a weather summary:\n" +
                 "Address: ${headers.get("formattedAdr")}, " +
                 "temperature: ${json.temperature.degrees} ${json.temperature.unit}, " +
                 "humidity: ${json.relativeHumidity}, " +
                 "weather condition: ${json.weatherCondition.description.text}, " +
                 "wind speed: ${json.wind.speed.value} ${json.wind.speed.unit}, " +
                 "wind direction: ${json.wind.direction.degrees} ${json.wind.direction.cardinal}, " +
                 "precipitation type: ${json.precipitation.probability.type}, " +
                 "precipitation probability: ${json.precipitation.probability.percent}, " +
                 "visibility: ${json.visibility.distance} ${json.visibility.unit}, " +
                 "UV index: ${json.uvIndex}, cloud cover: ${json.cloudCover}, is daytime: ${json.isDaytime}. Please provide the summary in a single paragraph."

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
            "responseMimeType": "application/json"
        ]
    ]
    
    // Serialize the Groovy Map structure into a valid JSON string
    def jsonString = JsonOutput.toJson(responseMap)
    message.setBody(jsonString)
    return message
}