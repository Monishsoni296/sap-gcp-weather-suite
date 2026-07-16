import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.util.TimeZone

def Message processData(Message message) {
    def headers = message.getHeaders()
    def body = message.getBody(java.io.Reader.class)
    
    // Parse incoming payload safely
    def json = new JsonSlurper().parse(body)
    def resultsList = json?.Results?.result ?: []

    // 1. Clean Refactor: Use Groovy's built-in collection methods instead of manual loops
    int success = resultsList.count { it.status == "SUCCESS" }
    int fail = resultsList.size() - success
    
    // Determine overall processing status
    def status = fail == 0 ? "SUCCESS" : success == 0 ? "FAILED" : "PARTIAL SUCCESS"
    
    // 2. Added 'generatedAt' using standard ISO 8601 UTC timestamp format
    def timestamp = new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"))
    
    // 3. Clean Refactor: Construct a structured Groovy map 
    def responseMap = [
        status             : status,
        requestedLocations : headers.get("requestedLocations") ?: "",
        successfulLocations: success,
        failedLocations    : fail,
        generatedAt        : timestamp,
        results            : resultsList
    ]
    
    // 4. Secure Serialization: Safely convert map to a valid JSON string 
    message.setBody(JsonOutput.toJson(responseMap))
    
    return message
}