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
        "R21010": new Tuple("HEAD010", head010),
        "R21020": new Tuple("HEAD020", head020),
        "R21030": new Tuple("HEAD030", head030),
        "R21040": new Tuple("HEAD040", head040),
        "R21050": new Tuple("HEAD050", head050),
        "R21060": new Tuple("HEAD060", head060),
        "R21070": new Tuple("HEAD070", head070),
        "R21080": new Tuple("HEAD080", head080),
        "R21090": new Tuple("HEAD090", head090),
        "R21100": new Tuple("HEAD100", head100),
        "R21110": new Tuple("HEAD110", head110),
        "R22010": new Tuple("ITEM010", item010),
        "R22020": new Tuple("ITEM020", item020),
        "R22030": new Tuple("ITEM030", item030),
        "R23010": new Tuple("DETAIL010", detail010),
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
    //Body
    def body = message.getBody(java.lang.String) as String;
    def xml = new XmlSlurper().parseText(body)
    def body1 = ""
    body1 = body1 + getNodeText(xml)
    message.setBody(updCount(body1));
    return message;
}

def boolean isGroupNode(groovy.util.slurpersupport.NodeChild node) {
    def nodeName = node.name();
    return grpNodes.contains(nodeName);
}

def String getNodeText(groovy.util.slurpersupport.NodeChild node) {
    def body1 = "";
    if (isGroupNode(node)) {
        def childNodes = node.'*'
        for (child in childNodes) {
            body1 = body1 + getNodeText(child);
        }
    } else {
        def segCode = node.text().substring(0, 6)
        println(segCode)
        body1 = body1 + buildLine(node.name(), node, segMap[segCode][1]) + "\n"
    }
    return body1;
}

def String buildLine(String TagName, groovy.util.slurpersupport.NodeChild data, int [] lens) {
    def result = new StringBuilder()
    def map = [:]
    data.'*'.each {
        child -> map.put(child.name(), child.text())
    }
    for (i = 0; i < lens.length; i++) {
        def fieldName = "FIELD" + ("" + (i+1)).padLeft(2, "0")
        if (map.containsKey(fieldName)) {
            result.append(format(map[fieldName], lens[i]))
        } else {
            result.append(format("", lens[i]))
        }
    }
    return result
}


def String buildLine1(String TagName, groovy.util.slurpersupport.NodeChild data, int [] lens) {
    def result = new StringBuilder()
    data.'*'.eachWithIndex {
        child, i -> result.append(format(child.text(), lens[i]))
    }
    return result
}


def String format(String str, int len) {
    int spaces
    if (str.length() <= len) {
        spaces = len - str.length()
        return str.concat(" " * spaces)
    }
    else {
        return str.substring(0, len)
    }
}

def String updCount(String body1) {
    def result = ""
    def lines = body1.split("\n");
    def recCnt = Integer.toString(lines.length - 2)
    for (i = 0; i < lines.length; i++)
    {
        def segCode = lines[i].substring(0, 6)
        if (segMap.containsKey(segCode)) {
            def tagName = segMap[segCode][0]
            if (tagName == "TRAILER") {
                def last = new StringBuilder()
                last.append(lines[i].substring(0, 12))
                last.append(recCnt.padLeft(5, '0'))
                last.append(lines[i].substring(17))
                result = result + last + '\n'
            } else {
                result = result + lines[i] + '\n'
            }
        }
    }
    return result
}
