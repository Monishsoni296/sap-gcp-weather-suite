import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*;

// Script to setup prompt for Gemini API response based on the hourly forecast data
def Message processData(Message message) {
    def headers = message.getHeaders()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)

    def forecastItems = []
    if (json instanceof Map) {
        if (json.forecast instanceof List) {
            forecastItems = json.forecast
        } else if (json.intervals instanceof List) {
            forecastItems = json.intervals
        } else {
            forecastItems = [json]
        }
    } else if (json instanceof List) {
        forecastItems = json
    }
    def location = headers.get('formattedAdr') ?: headers.get('address') ?: message.getProperty('address') ?: 'Unknown'
    def actualIntervals = []

    // Since the timezone metadata is guaranteed to be at the very end
    if (forecastItems) {
        def lastItem = forecastItems[-1]
        
        if (lastItem instanceof Map && lastItem.timeZone?.id) {
            timeZoneId = lastItem.timeZone.id
            // [0..-2] extracts everything from index 0 up to the second-to-last item
            actualIntervals = forecastItems.size() > 1 ? forecastItems[0..-2] : []
        } else {
            // Fallback if the payload structure is unexpected
            actualIntervals = forecastItems
        }
    }

    def forecast = actualIntervals.collect { item ->
        def description = item.weatherCondition.description ?: [:]
        def temp = item.temperature.degrees ?: ""
        def unit = item.temperature.unit ?: ""
        def hour = item.displayDateTime?.hour ?: ""

        [
            hour: hour,
            temperature: "${temp} ${unit}",
            condition: description.text ?: ""
        ]
    }

    def next12 = forecast.size() > 12 ? forecast[0..11] : forecast
    def promptText = "Summarize the next 12 hours of weather forecast for ${location} in 1-2 lines. " +
        "Use the following hourly data: " +
        next12.collect { it.hour ? "${it.hour}h ${it.temperature} ${it.condition}" : "${it.temperature} ${it.condition}" }.join('; ')

    def prompt = [
        "contents": [
            [
                "parts": [
                    [
                        "text": promptText
                    ]
                ]
            ]
        ],
        "generationConfig": [
            "responseMimeType": "application/json"
        ]
    ]
    def jsonString = JsonOutput.toJson(prompt)
    message.setBody(jsonString)
    message.setHeader('Content-Type', 'application/json')
    return message
}