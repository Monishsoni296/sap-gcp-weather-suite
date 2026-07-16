import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*;
import java.time.*

// Script to set daily forecast response in a structured format, including location, coordinates, unit system, and forecast intervals.
def Message processData(Message message) {
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
        def date = "${item.displayDate.year}-${item.displayDate.month}-${item.displayDate.day}"    
        def tempMax = "${item.maxTemperature?.degrees ?: ""} ${item.maxTemperature?.unit ?: ""}"
        def tempMin = "${item.minTemperature?.degrees ?: ""} ${item.minTemperature?.unit ?: ""}"
        def day = [
            "condition": item.daytimeForecast.weatherCondition?.description?.text ?: "",
            "iconUrl": item.daytimeForecast.weatherCondition?.iconBaseUri ?: "",
            "humidity": item.daytimeForecast.relativeHumidity ?: "",
            "uvIndex": item.daytimeForecast.uvIndex ?: "",
            "precipitationProbability": item.daytimeForecast.precipitation?.probability?.percent ?: "",
            "precipitationType": item.daytimeForecast.precipitation?.probability?.type ?: "",
            "windSpeed": "${item.daytimeForecast.wind?.speed?.value ?: ""} ${item.daytimeForecast.wind?.speed?.unit ?: ""}",
            "windDirection": "${item.daytimeForecast.wind?.direction?.degrees ?: ""} ${item.daytimeForecast.wind?.direction?.cardinal ?: ""}"
        ]
        def night = [
            "condition": item.nighttimeForecast?.weatherCondition?.description?.text ?: "",
            "iconUrl": item.nighttimeForecast?.weatherCondition?.iconBaseUri ?: "",
            "humidity": item.nighttimeForecast?.relativeHumidity ?: "",
            "precipitationProbability": item.nighttimeForecast?.precipitation?.probability?.percent ?: "",
            "precipitationType": item.nighttimeForecast?.precipitation?.probability?.type ?: "",
            "windSpeed": "${item.nighttimeForecast.wind?.speed?.value ?: ""} ${item.nighttimeForecast.wind?.speed?.unit ?: ""}",
            "windDirection": "${item.nighttimeForecast.wind?.direction?.degrees ?: ""} ${item.nighttimeForecast.wind?.direction?.cardinal ?: ""}"
        ]

        [
            date: date,
            tempMax: tempMax,
            tempMin: tempMin,
            day: day,
            night: night
        ]
    }

    def response = [
        forecast: forecast
    ]

    message.setHeader('Content-Type', 'application/json')
    message.setBody(JsonOutput.toJson(response))
    return message
}