/* Refer the link below to learn more about the use cases of script.
https://help.sap.com/viewer/368c481cd6954bdfa5d0435479fd4eaf/Cloud/en-US/148851bf8192412cba1f9d2c17f4bd25.html

If you want to know more about the SCRIPT APIs, refer the link below
https://help.sap.com/doc/a56f52e1a58e4e2bac7f7adbf45b2e26/Cloud/en-US/index.html */
import com.sap.gateway.ip.core.customdev.util.Message;
import groovy.xml.*;
import groovy.xml.XmlUtil


import java.text.SimpleDateFormat
def Message processData(Message message) {
    //Body
def body = message.getBody(String);
def sdf = new SimpleDateFormat("yyyyMMdd")
def root= new XmlParser().parseText(body)
def writer = new StringWriter()
def builder = new MarkupBuilder(writer)
builder.root{
   root.record.each{
       rec ->
           record{
               def sorteditems = rec.item.sort{a,b ->
                   a.RecordCategory.toString() <=> b.RecordCategory.toString()?:
                           sdf.parse(a.EffectiveDate.text()) <=> sdf.parse(b.EffectiveDate.text())?:
                                   sdf.parse(a.EffectiveEndDate.text()) <=> sdf.parse(b.EffectiveEndDate.text())
               }


                if(sorteditems.size()==4){
                    def patten=(sdf.parse(sorteditems[0].EffectiveEndDate.text()))<=sdf.parse(sorteditems[2]
                    .EffectiveEndDate.text())?"Patten2":"Patten3"
                    sorteditems[0].appendNode('newEffectiveEndDate',sorteditems[2].EffectiveEndDate.text())
                    sorteditems[0].appendNode('newName',sorteditems[2].BudgetUnitNameKanji.text())
                    sorteditems[1].appendNode('newEffectiveStartDate',sorteditems[3].EffectiveDate.text())
                    sorteditems[1].appendNode('newName',sorteditems[3].BudgetUnitNameKanji.text())
                    sorteditems.remove(sorteditems[3])
                    sorteditems.remove(sorteditems[2])
                    if(patten=="Patten2"){
                        temp=sorteditems[0]
                        sorteditems[0]=sorteditems[1]
                        sorteditems[1]=temp
                    }
                }
                   if(sorteditems.size()==3){


        sorteditems[0].appendNode('newEffectiveEndDate',sorteditems[1].EffectiveEndDate.text())
        sorteditems[0].appendNode('newName',sorteditems[1].BudgetUnitNameKanji.text())
        sorteditems.remove(sorteditems[1])
    }


                sorteditems.each{
                    itm->
                        item{
                            itm.children().each{child->
                                def childxml = XmlUtil.serialize(child).toString()

                                childxml = childxml.replaceAll(/<\?xml.*\?>\s*/, '')
                                mkp.yieldUnescaped(childxml)
                            }
                        }
                }
           }
   }
}



message.setBody(writer.toString())
    return message;
}