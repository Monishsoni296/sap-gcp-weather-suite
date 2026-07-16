import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*

def Message processData(Message message) {
    
    def properties = message.getProperties();
    def masterForecast = properties.get("masterForecast")
    def timeZone = properties.get("timeZone")
    
    masterForecast.add("timeZone": timeZone)
    
    message.setBody(JsonOutput.toJson(masterForecast))
    return message;
}