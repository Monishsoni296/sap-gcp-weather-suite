import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

// Script to set current weather forecast response to message body
def Message processData(Message message) {
    def body = message.getBody(java.io.Reader.class)
    def properties = message.getProperties()
    def json = new JsonSlurper().parse(body)
    
    // 1. Retrieve the Gemini response property as a String
    def summaryRaw = properties.get("summary") as String
    
    // 2. Parse that string to extract the actual text value safely
    def parsedSummary = new JsonSlurper().parseText(summaryRaw)
    def cleanSummaryText = parsedSummary.summary
    
    String response = """{
            "summary": "${cleanSummaryText}",
            "temperature": "${json.temperature.degrees} ${json.temperature.unit}",
            "maxTemperature": "${json.currentConditionsHistory.maxTemperature.degrees} ${json.currentConditionsHistory.maxTemperature.unit}",
            "minTemperature": "${json.currentConditionsHistory.minTemperature.degrees} ${json.currentConditionsHistory.minTemperature.unit}",
            "isDaytime": "${json.isDaytime}",
            "description": {
                "iconUri": "${json.weatherCondition.iconBaseUri}",
                "description": "${json.weatherCondition.description.text}"
            },
            "windSpeed": {
                "speed": "${json.wind.speed.value} ${json.wind.speed.unit}",
                "direction": "${json.wind.direction.degrees} ${json.wind.direction.cardinal}",
                "gust": "${json.wind.gust.value} ${json.wind.gust.unit}"
            },
            "windDirection": "${json.wind.direction.degrees} ${json.wind.direction.cardinal}",
            "precipitation": {
                "type": "${json.precipitation.probability.type}",
                "percent": "${json.precipitation.probability.percent}"
            },
            "visibility": "${json.visibility.distance} ${json.visibility.unit}",
            "uvIndex": "${json.uvIndex}",
            "cloudCover": "${json.cloudCover}",
            "humidity": "${json.relativeHumidity}"
    }"""

    // Set the current weather forecast response to the message body
    message.setBody(response)
    return message
}