import com.sap.gateway.ip.core.customdev.util.Message;

def Message processData(Message message) {

    def queryString = message.getHeader("CamelHttpQuery", String.class);
    def messageLog = messageLogFactory?.getMessageLog(message)
    
    if (queryString) {

        def params = queryString.split("&").collectEntries { param ->
            def keyValue = param.split("=")
            
            def key = keyValue[0]
            def value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : ""
            [(key): value]
        }
        
        params.each { key, value ->
            message.setProperty(key, value);
            if (messageLog != null) {
                messageLog.addCustomHeaderProperty(key, value)
            }
        }
    }
    
    return message;
}
