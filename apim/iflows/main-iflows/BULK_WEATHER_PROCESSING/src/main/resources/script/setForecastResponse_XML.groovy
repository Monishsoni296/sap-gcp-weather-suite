import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import groovy.xml.MarkupBuilder
import java.time.*
import java.io.StringWriter

// Script to set daily forecast response in a structured XML format, including location, coordinates, unit system, and forecast intervals.
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

    // Renamed local variables to avoid node-name conflicts in MarkupBuilder
    def loc = headers.get('formattedAdr') ?: headers.get('address') ?: message.getProperty('address') ?: 'Unknown'
    def lat = headers.get('latitude') ?: 0
    def lon = headers.get('longitude') ?: 0
    def unit = headers.get('unitsSystem') ?: message.getProperty('unitsSystem') ?: 'METRIC'

    def timeZoneId = 'UTC'
    def actualIntervals = []

    // Since the timezone metadata is guaranteed to be at the very end
    if (forecastItems) {
        def lastItem = forecastItems[-1]
        
        if (lastItem instanceof Map && lastItem.timeZone?.id) {
            timeZoneId = lastItem.timeZone.id
            // extracts everything from index 0 up to the second-to-last item
            actualIntervals = forecastItems.size() > 1 ? forecastItems[0..-2] : []
        } else {
            // Fallback if the payload structure is unexpected
            actualIntervals = forecastItems
        }
    }

    // Use StringWriter and MarkupBuilder to construct XML
    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)

    // Build the XML payload
    xml.Results {
        result {
            location(loc)
            status("SUCCESS")
            coordinates {
                latitude(lat)
                longitude(lon)
            }
            unitSystem(unit)
        
            forecasts {
                actualIntervals.each { item ->
                    forecast {
                        date("${item.displayDate?.year ?: ''}-${item.displayDate?.month ?: ''}-${item.displayDate?.day ?: ''}")
                        tempMax("${item.maxTemperature?.degrees ?: ''} ${item.maxTemperature?.unit ?: ''}".trim())
                        tempMin("${item.minTemperature?.degrees ?: ''} ${item.minTemperature?.unit ?: ''}".trim())
                        sunrise(setTimeZone(item.sunEvents?.sunriseTime, timeZoneId))
                        sunset(setTimeZone(item.sunEvents?.sunsetTime, timeZoneId))
                        moonPhase(item.moonEvents?.moonPhase ?: "")
                    
                        day {
                            condition(item.daytimeForecast?.weatherCondition?.description?.text ?: "")
                            iconUrl(item.daytimeForecast?.weatherCondition?.iconBaseUri ?: "")
                            humidity(item.daytimeForecast?.relativeHumidity ?: "")
                            uvIndex(item.daytimeForecast?.uvIndex ?: "")
                            precipitationProbability(item.daytimeForecast?.precipitation?.probability?.percent ?: "")
                            precipitationType(item.daytimeForecast?.precipitation?.probability?.type ?: "")
                            windSpeed("${item.daytimeForecast?.wind?.speed?.value ?: ''} ${item.daytimeForecast?.wind?.speed?.unit ?: ''}".trim())
                            windDirection("${item.daytimeForecast?.wind?.direction?.degrees ?: ''} ${item.daytimeForecast?.wind?.direction?.cardinal ?: ''}".trim())
                            cloudCover(item.daytimeForecast?.cloudCover ?: "")
                        }
                    
                        night {
                            condition(item.nighttimeForecast?.weatherCondition?.description?.text ?: "")
                            iconUrl(item.nighttimeForecast?.weatherCondition?.iconBaseUri ?: "")
                            humidity(item.nighttimeForecast?.relativeHumidity ?: "")
                            uvIndex(item.nighttimeForecast?.uvIndex ?: "")
                            precipitationProbability(item.nighttimeForecast?.precipitation?.probability?.percent ?: "")
                            precipitationType(item.nighttimeForecast?.precipitation?.probability?.type ?: "")
                            windSpeed("${item.nighttimeForecast?.wind?.speed?.value ?: ''} ${item.nighttimeForecast?.wind?.speed?.unit ?: ''}".trim())
                            windDirection("${item.nighttimeForecast?.wind?.direction?.degrees ?: ''} ${item.nighttimeForecast?.wind?.direction?.cardinal ?: ''}".trim())
                            cloudCover(item.nighttimeForecast?.cloudCover ?: "")
                        }
                    }
                }
            }
        }
    }

    message.setHeader('Content-Type', 'application/xml')
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