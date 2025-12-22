/* Refer the link below to learn more about the use cases of script.
https://help.sap.com/viewer/368c481cd6954bdfa5d0435479fd4eaf/Cloud/en-US/148851bf8192412cba1f9d2c17f4bd25.html

If you want to know more about the SCRIPT APIs, refer the link below
https://help.sap.com/doc/a56f52e1a58e4e2bac7f7adbf45b2e26/Cloud/en-US/index.html */
import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.json.*;
def Message processData(Message message) {

    message.setHeader("Content-Type", "multipart/mixed;boundary=batch");
    message.setHeader("Accept", "multipart/mixed");
    def headers = message.getHeaders();
    def cookies = headers.get("set-cookie");
    def properties = message.getProperties();
    
    message.setHeader("cookie",String.join(";",cookies));
    def Entity = properties.get("Entity");
    def body = message.getBody(java.io.Reader)
    def sb = new StringBuilder(10_000_000)
    sb.append("--batch\r\n")
    sb.append("Content-Type: multipart/mixed; boundary=changeset_001\r\n\r\n")
  
    
    def json = new JsonSlurper().parse(body);
    def rows = json.Root.row  // List<Map>
    def i=1
    rows.each { r ->
    sb.append("--changeset_001\r\n")
    sb.append("Content-Type: application/http\r\n")
    sb.append("Content-Transfer-Encoding: binary\r\n")
    sb.append("Content-ID: ").append(i++).append("\r\n\r\n")
    sb.append("POST ").append(Entity).append(" HTTP/1.1\r\n")
    sb.append("Content-Type: application/json;odata.metadata=minimal\r\n\r\n")
    sb.append(JsonOutput.toJson(r)).append("\r\n\r\n")
    }
    sb.append("--changeset_001--\r\n\r\n")
    sb.append("--batch--")
    message.setBody(sb.toString())
  

    return message;
}


