import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*;
import java.time.*

def Message processData(Message message) {
    def body = message.getBody(java.io.Reader.class)
    def properties = message.getProperties()
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
    
    // 1. Retrieve the Gemini response property as a String
    def summaryRaw = properties.get("summary") as String
    
    // 2. Parse that string to extract the actual text value safely
    def parsedSummary = new JsonSlurper().parseText(summaryRaw)
    def cleanSummaryText = parsedSummary.summary
    
    def forecast = actualIntervals.collect { item ->
        def interval = item.interval ?: [:]
        def weatherCondition = item.weatherCondition ?: [:]
        def description = weatherCondition.description ?: [:]
        def wind = item.wind ?: [:]
        def direction = wind.direction ?: [:]
        def speed = wind.speed ?: [:]
        def gust = wind.gust ?: [:]

        [
            startTime: setTimeZone(interval.startTime, timeZoneId),
            endTime: setTimeZone(interval.endTime, timeZoneId),
            condition: description.text ?: '',
            iconUrl: weatherCondition.iconBaseUri ?: '',
            temperature: item.temperature?.degrees ?: 0,
            humidity: item.relativeHumidity ?: 0,
            uvIndex: item.uvIndex ?: 0,
            wind: [
                speed: speed.value ?: 0,
                directionDegrees: direction.degrees ?: 0,
                gust: gust.value ?: 0
            ],
            precipitationProbability: item.precipitation?.probability?.percent ?: 0,
            cloudCover: item.cloudCover ?: 0,
            isDaytime: item.isDaytime ?: false
        ]
    }

    def response = [
        summary: "${cleanSummaryText}",
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