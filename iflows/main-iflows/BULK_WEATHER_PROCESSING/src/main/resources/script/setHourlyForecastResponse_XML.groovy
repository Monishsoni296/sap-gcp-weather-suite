import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*
import groovy.xml.MarkupBuilder
import java.time.*
import java.io.StringWriter

// Script to set interval forecast response in a structured XML format, wrapped in <Results><result>...
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

    // Renamed variables to avoid root node-name conflicts
    def loc = headers.get('formattedAdr') ?: headers.get('address') ?: message.getProperty('address') ?: 'Unknown'
    def lat = headers.get('latitude') ?: 0
    def lon = headers.get('longitude') ?: 0
    def unit = headers.get('unitsSystem') ?: message.getProperty('unitsSystem') ?: 'METRIC'
    
    def timeZoneId = 'UTC'
    def actualIntervals = []

    if (forecastItems) {
        def lastItem = forecastItems[-1]
        
        if (lastItem instanceof Map && lastItem.timeZone?.id) {
            timeZoneId = lastItem.timeZone.id
            actualIntervals = forecastItems.size() > 1 ? forecastItems[0..-2] : []
        } else {
            actualIntervals = forecastItems
        }
    }

    def writer = new StringWriter()
    def xml = new MarkupBuilder(writer)

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
                    // Prefixed with 'json' to stop conflicts with MarkupBuilder tags like speed() or condition()
                    def jsonInterval = item.interval ?: [:]
                    def jsonCondition = item.weatherCondition ?: [:]
                    def jsonDesc = jsonCondition.description ?: [:]
                    def jsonWind = item.wind ?: [:]
                    def jsonDir = jsonWind.direction ?: [:]
                    def jsonSpeed = jsonWind.speed ?: [:]
                    def jsonGust = jsonWind.gust ?: [:]

                    forecast {
                        startTime(setTimeZone(jsonInterval.startTime, timeZoneId))
                        endTime(setTimeZone(jsonInterval.endTime, timeZoneId))
                        condition(jsonDesc.text ?: '')
                        iconUrl(jsonCondition.iconBaseUri ?: '')
                        temperature(item.temperature?.degrees ?: 0)
                        feelsLike(item.feelsLikeTemperature?.degrees ?: 0)
                        humidity(item.relativeHumidity ?: 0)
                        uvIndex(item.uvIndex ?: 0)
                        
                        wind {
                            speed(jsonSpeed.value ?: 0)
                            directionDegrees(jsonDir.degrees ?: 0)
                            gust(jsonGust.value ?: 0)
                        }
                        
                        precipitationProbability(item.precipitation?.probability?.percent ?: 0)
                        cloudCover(item.cloudCover ?: 0)
                        isDaytime(item.isDaytime ?: false)
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