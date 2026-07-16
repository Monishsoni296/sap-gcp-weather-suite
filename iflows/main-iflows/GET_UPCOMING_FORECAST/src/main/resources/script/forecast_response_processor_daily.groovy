import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*;
import java.time.*

// Script to set daily forecast response in a structured format, including location, coordinates, unit system, and forecast intervals.
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
    def latitude = headers.get('latitude') ?: headers.get('latitude') ?: 0
    def longitude = headers.get('longitude') ?: headers.get('longitude') ?: 0
    def unitSystem = headers.get('unitsSystem') ?: message.getProperty('unitsSystem') ?: 'METRIC'
    
    def timeZoneId = 'UTC'
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
        def sunrise = setTimeZone(item.sunEvents.sunriseTime, timeZoneId)
        def sunset = setTimeZone(item.sunEvents.sunsetTime, timeZoneId)
        def moonPhase = item.moonEvents?.moonPhase ?: ""
        def day = [
            "condition": item.daytimeForecast.weatherCondition?.description?.text ?: "",
            "iconUrl": item.daytimeForecast.weatherCondition?.iconBaseUri ?: "",
            "humidity": item.daytimeForecast.relativeHumidity ?: "",
            "uvIndex": item.daytimeForecast.uvIndex ?: "",
            "precipitationProbability": item.daytimeForecast.precipitation?.probability?.percent ?: "",
            "precipitationType": item.daytimeForecast.precipitation?.probability?.type ?: "",
            "windSpeed": "${item.daytimeForecast.wind?.speed?.value ?: ""} ${item.daytimeForecast.wind?.speed?.unit ?: ""}",
            "windDirection": "${item.daytimeForecast.wind?.direction?.degrees ?: ""} ${item.daytimeForecast.wind?.direction?.cardinal ?: ""}",
            "cloudCover": item.daytimeForecast.cloudCover ?: "",
        ]
        def night = [
            "condition": item.nighttimeForecast?.weatherCondition?.description?.text ?: "",
            "iconUrl": item.nighttimeForecast?.weatherCondition?.iconBaseUri ?: "",
            "humidity": item.nighttimeForecast?.relativeHumidity ?: "",
            "uvIndex": item.nighttimeForecast?.uvIndex ?: "",
            "precipitationProbability": item.nighttimeForecast?.precipitation?.probability?.percent ?: "",
            "precipitationType": item.nighttimeForecast?.precipitation?.probability?.type ?: "",
            "windSpeed": "${item.nighttimeForecast.wind?.speed?.value ?: ""} ${item.nighttimeForecast.wind?.speed?.unit ?: ""}",
            "windDirection": "${item.nighttimeForecast.wind?.direction?.degrees ?: ""} ${item.nighttimeForecast.wind?.direction?.cardinal ?: ""}",
            "cloudCover": item.nighttimeForecast.cloudCover ?: "",
        ]

        [
            date: date,
            tempMax: tempMax,
            tempMin: tempMin,
            sunrise: sunrise,
            sunset: sunset,
            moonPhase: moonPhase,
            day: day,
            night: night
        ]
    }

    def response = [
        location: location,
        coordinates: [
            latitude: latitude,
            longitude: longitude
        ],
        unitSystem: unitSystem,
        forecast: forecast
    ]

    message.setHeader('Content-Type', 'application/json')
    message.setBody(JsonOutput.toJson(response))
    return message
}
def setTimeZone(timestamp, timeZoneId) {
    if (!timestamp) return ""
    def instant = Instant.parse(timestamp)
    def zone = ZoneId.of(timeZoneId)
    def zonedDateTime = instant.atZone(zone)
    return zonedDateTime.toString()
}