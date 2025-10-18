import com.sap.gateway.ip.core.customdev.util.Message; 
import java.util.HashMap; 
import groovy.xml.*
import com.sap.it.api.ITApiFactory; 
import com.sap.gateway.ip.core.customdev.util.Message; 
import java.util.HashMap; 



def Message processData(Message message) 
{
    int [] header = [8, 1, 1, 26, 14, 5, 395];
    int [] head010 = [3, 3, 8, 35, 14, 8, 3, 8, 3, 3, 8, 8, 35, 35, 35, 3, 238];
    int [] head020 = [3, 3, 8, 35, 14, 12, 8, 14, 35, 15, 3, 20, 17, 15, 35, 70, 70, 3, 1, 69];
    int [] head030 = [3, 3, 8, 35, 14, 17, 17, 17, 2, 17, 15, 17, 44, 25, 17, 17, 17, 17, 17, 17, 17, 17, 80];
    int [] head040 = [3, 3, 8, 35, 14, 35, 35, 35, 35, 35, 35, 35, 35, 35, 25, 3, 25, 3, 16];
    int [] head050 = [3, 3, 8, 35, 14, 35, 35, 35, 35, 35, 35, 35, 35, 35, 25, 3, 25, 3, 16];
    int [] head060 = [3, 3, 8, 35, 14, 35, 35, 70, 50, 50, 147];
    int [] head070 = [3, 3, 8, 35, 14, 70, 70, 70, 70, 70, 3, 34];
    int [] head080 = [3, 3, 8, 35, 14, 35, 35, 35, 35, 35, 35, 35, 35, 35, 25, 3, 25, 3, 16];
    int [] head090 = [3, 3, 8, 35, 14, 35, 35, 35, 35, 35, 35, 35, 35, 35, 25, 3, 25, 3, 16];
    int [] head100 = [3, 3, 8, 35, 14, 35, 35, 35, 35, 35, 35, 35, 35, 35, 25, 3, 25, 3, 16];
    int [] head110 = [3, 3, 8, 35, 14, 50, 50, 50, 237];
    int [] item010 = [3, 3, 8, 35, 6, 8, 3, 20, 35, 6, 323];
    int [] item020 = [3, 3, 8, 35, 6, 8, 35, 35, 35, 282];
    int [] item030 = [3, 3, 8, 35, 6, 8, 17, 16, 3, 15, 17, 3, 20, 17, 15, 35, 229];
    int [] detail010 = [3, 3, 8, 35, 6, 8, 3, 12, 17, 3, 12, 340];
    int [] trailer = [8, 4, 5, 433];

    segMap = [
        "A00010": new Tuple("HEADER", header),
        "R31010": new Tuple("HEAD010", head010),
        "R31020": new Tuple("HEAD020", head020),
        "R31030": new Tuple("HEAD030", head030),
        "R31040": new Tuple("HEAD040", head040),
        "R31050": new Tuple("HEAD050", head050),
        "R31060": new Tuple("HEAD060", head060),
        "R31070": new Tuple("HEAD070", head070),
        "R31080": new Tuple("HEAD080", head080),
        "R31090": new Tuple("HEAD090", head090),
        "R31100": new Tuple("HEAD100", head100),
        "R31110": new Tuple("HEAD110", head110),
        "R32010": new Tuple("ITEM010", item010),
        "R32020": new Tuple("ITEM020", item020),
        "R32030": new Tuple("ITEM030", item030),
        "R33010": new Tuple("DETAIL010", detail010),
        "A0Z010": new Tuple("TRAILER", trailer),
    ];
    segGrp = [
        "0": new Tuple(1, "<ROOT>", "</ROOT>"),
        "1": new Tuple(2, "<PO>", "</PO>"),
        "2": new Tuple(3, "<ITEM>", "</ITEM>"),
        "3": new Tuple(4, "<DETAIL>", "</DETAIL>"),
        "Z": new Tuple(1, "<ROOT>", "</ROOT>"),
    ]
    grpNodes = ["ROOT", "PO", "ITEM", "DETAIL"];

    def body = message.getBody(java.lang.String) as String;
    def lines = customSplit(body,450)

    //body.split("\n");
    def body1 = "";
    ind = 0;
    body1 = getNodes(lines)
    message.setBody(body1);
    return message;
}

def String[] customSplit(String text, int slen){
    def body1 = text.replaceAll("[\\n\\r]+","");
    def records = body1.toList().collate(slen)*.join();
    records = records.collect { it.padRight(slen," ")}
    return records
}

def String getNodes(String[] lines) {
    def body1 = "";
    def grpCodeNode = lines[ind].substring(2, 3)
    def grpCodeNodeInd = segGrp[grpCodeNode][0]
    // Node Tag begin
    body1 = body1 + getNodeTag(grpCodeNode, 1) + "\n"
    // process Node
    for (i = ind; i < lines.length; i++)
    {
        def segCode = lines[i].substring(0, 6)
        def grpCode = lines[i].substring(2, 3)
        def grpCodeInd = segGrp[grpCode][0]
        if (grpCodeInd == grpCodeNodeInd) {
            if (segMap.containsKey(segCode)) {
                def tagName = segMap[segCode][0]
                body1 = (
                    body1 + getLastNodeTag(tagName, lines[i], segMap[segCode][1])
 + "\n"
                );
            }
            ind = ind + 1;
        } else if (grpCodeInd > grpCodeNodeInd) {
            body1 = body1 + getNodes(lines)
            if (segGrp[lines[ind].substring(2, 3)][0] - grpCodeNodeInd == 1) {
                i = ind;
            }
            else {
                break ;
            }
        } else if (grpCodeInd < grpCodeNodeInd) {
            break ;
        }
    }
    if (grpCodeNodeInd == 1) {
        def segCode = lines[ind].substring(0, 6)
        if (segMap.containsKey(segCode)) {
            def tagName = segMap[segCode][0]
            body1 = (
                body1 + getLastNodeTag(tagName, lines[ind], segMap[segCode][1])
 + "\n"
            );
        }
    }
    //Node Tag end
    body1 = body1 + getNodeTag(grpCodeNode, 2) + "\n"
    return body1;
}

def String getNodeTag(String grpCode, int tagPos) {
    String tag = ""
    tag = tag + segGrp[grpCode][tagPos];
    return tag
}

def String getLastNodeTag(String TagName, String data, int [] lens) {
    data = data.padRight(450);
    def body1 = "";
    body1 += "<" + TagName + ">";
    def sum = 0;
    for (k = 0; k < lens.length; k++)
    {
        def ind = String.format('%02d', k + 1);
        body1 += "<FIELD" + ind + ">" + data.substring(
            sum, sum + lens[k]
        ).trim() + "</FIELD" + ind + ">";
        sum += lens[k];
    }
    body1 += "</" + TagName + ">";
    return body1;
}