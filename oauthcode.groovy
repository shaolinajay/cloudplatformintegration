import com.sap.gateway.ip.core.customdev.util.Message;
import com.sap.it.api.securestore.SecureStoreService;
import com.sap.it.api.securestore.AccessTokenAndUser;
import com.sap.it.api.securestore.exception.SecureStoreException;
import com.sap.it.api.ITApiFactory;
def Message processData(Message message) {

     SecureStoreService secureStoreService = ITApiFactory.getService(SecureStoreService.class, null);
     AccessTokenAndUser accessTokenAndUser = secureStoreService.getAccesTokenForOauth2AuthorizationCodeCredential("mailTest");
     String token = accessTokenAndUser.getAccessToken();

    message.setHeader("Authorization", "Bearer "+token);

    return message;

}