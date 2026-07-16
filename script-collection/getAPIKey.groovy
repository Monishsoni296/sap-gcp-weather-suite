import com.sap.gateway.ip.core.customdev.util.Message;
import com.sap.it.api.ITApiFactory;
import com.sap.it.api.securestore.*
import com.sap.it.api.securestore.exception.SecureStoreException

def Message processData(Message message) {
    def apikey_alias = message.getProperty("secureParameterName")
    def secureStorageService =  ITApiFactory.getService(SecureStoreService.class, null)
    try{
        def secureParameter = secureStorageService.getUserCredential(apikey_alias)
        def apikey = secureParameter.getPassword().toString()
        message.setProperty("Authorization", apikey)
    } catch(Exception e){
        throw new SecureStoreException("Secure Parameter not available")
    }
    return message;
}

