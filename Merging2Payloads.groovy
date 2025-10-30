/* Refer the link below to learn more about the use cases of script.
https://help.sap.com/viewer/368c481cd6954bdfa5d0435479fd4eaf/Cloud/en-US/148851bf8192412cba1f9d2c17f4bd25.html

If you want to know more about the SCRIPT APIs, refer the link below
https://help.sap.com/doc/a56f52e1a58e4e2bac7f7adbf45b2e26/Cloud/en-US/index.html */
import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.*;
def Message processData(Message message) {
    def body = message.getBody(String);
    def Payload = message.getProperty('Payload');
    def xml1 = new XmlParser().parseText(Payload.getText('UTF-8'))
    def xml2 = new XmlParser().parseText(body);
    
    
    
    

    
        xml1.select_response.row.each { rec ->
            def fcur = rec.currency_code.text()
            def tcur = rec.row.NCurrencyCode.text()
           
            def pnrtocc =xml2.select_response.row.find{ it.FromCurrencyCode.text() == fcur && it.ToCurrencyCode.text() == tcur}
            pnrtocc =(pnrtocc!=null)?pnrtocc.ConversionRate.text():1
            def cctoCom = xml2.select_response.row.find{ it.FromCurrencyCode.text() == tcur && it.ToCurrencyCode.text() == "KWD"}
            cctoCom=(cctoCom!=null)?cctoCom.ConversionRate.text():1
           
            rec.appendNode("PTC",pnrtocc);
            rec.appendNode("CTC",cctoCom)



        

    
}
def writer = new StringWriter()
def printer = new XmlNodePrinter(new PrintWriter(writer))
printer.preserveWhitespace = true
printer.print(xml1)
message.setBody(writer.toString())

    return message;
}