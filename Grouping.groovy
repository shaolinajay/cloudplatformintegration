/* Refer the link below to learn more about the use cases of script.
https://help.sap.com/viewer/368c481cd6954bdfa5d0435479fd4eaf/Cloud/en-US/148851bf8192412cba1f9d2c17f4bd25.html

If you want to know more about the SCRIPT APIs, refer the link below
https://help.sap.com/doc/a56f52e1a58e4e2bac7f7adbf45b2e26/Cloud/en-US/index.html */
import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import com.sap.it.api.ITApiFactory
import com.sap.it.api.mapping.ValueMappingApi
import groovy.xml.*;
import groovy.util.XmlSlurper
def Message processData(Message message) {
    //Body
    def body = message.getBody(java.lang.String);
    
    def valueMapApi = ITApiFactory.getApi(ValueMappingApi.class, null)
       
/*To set the body, you can use the following method. Refer SCRIPT APIs document for more detail*/
    //message.setBody(body + " Body is modified");
    //Headers
def xml = new XmlSlurper().parseText(body)
 def records = xml.Record.collect {rec->
  def fields = [:]
  rec.children().each { node ->
  fields[node.name()]=node.text()}
  return fields
 }
 //print(records)
 def grouped = records.groupBy {it.BudgetUnitCode}
 
 def writer = new StringWriter()
 def builder = new MarkupBuilder(writer)
 builder.root {
   grouped.each{ bdgunitcode, itms -> record{
     
      
      itms.each{ itm ->
      item{
      itm.each{key,value ->
      "${key}"(value)
      if("${key}"=="LocationCode"){
              def mappedValue = valueMapApi.getMappedValue('sourceag', 'sourceid', value, 'targetag', 'targetid')
          "CompanyCode"(mappedValue)
           
      
      }
      }
}
}
   
}
   }      
 }
 message.setBody(writer.toString())
    return message;
}