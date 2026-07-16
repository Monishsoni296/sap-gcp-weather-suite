import com.sap.gateway.ip.core.customdev.util.Message
import groovy.json.*

// Script to set error response for weather and geocode API call
def Message processData(Message message) {
    def headers = message.getHeaders()
    def properties = message.getProperties()
    def body = message.getBody(java.io.Reader.class)
    def json = new JsonSlurper().parse(body)
    
    def xmlResponse = """<Results>"""
    xmlResponse += """<result>
        <locationId>${properties.get("locationId")}</locationId>
        <status>FAILED</status>
        <inputAddress>${headers.get("Address")}</inputAddress>
        <error>
            <status>${json.error.status}</status>
            <message>${json.error.details}</message>
        </error>
    </result>"""
    xmlResponse += """</Results>"""
    
    message.setBody(xmlResponse)
    return message
}