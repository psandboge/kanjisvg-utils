package se.sandboge.kanjisvg;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Process {
    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        String path = "src/main/resources/svg/06426.svg";
        System.out.println("Kanji SVG");
        if (args.length > 1) {
            path = args[1];
        }
        List<String> files;
        File file = new File(path);
        if (file.isDirectory()) {
            files = List.of(file.list());
        } else {
            files = Collections.singletonList(path);
        }
        files.forEach(f -> {
            try {
                processFile(f);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Skipping file " + f);
            }
        });
    }

    private static void processFile(String path) throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new File(path));
        doc.getDocumentElement().normalize();
        Node rootNode = doc.getElementsByTagName("svg").item(0);
        List<KanjiGroup> groups = new ArrayList<>();
        List<KanjiPart> paths = new ArrayList<>();
        List<KanjiRule> kanjiRules = new ArrayList<>();
        processTree(rootNode, doc, groups, paths,0);
        String width = getAttrOrNull(rootNode, "width");
        String height = getAttrOrNull(rootNode, "height");
        String viewBox = getAttrOrNull(rootNode, "viewBox");
        SvgElement svgElement = new SvgElement(width, height, viewBox, groups, paths);
        System.out.println(svgElement);
        toJson(svgElement);
    }

    private static void toJson(SvgElement svgElement) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(svgElement));
    }

    private static void processTree(Node item, Node parent, List<KanjiGroup> groups, List<KanjiPart> paths, int depth) {
        if (Node.ELEMENT_NODE != item.getNodeType() && Node.TEXT_NODE != item.getNodeType()) {
            System.out.println(item.getNodeType());
            return;
        }
        if (Node.TEXT_NODE == item.getNodeType()) {
            return;
        }
        if ("path".equals(item.getNodeName())) {
            processPath(item, parent, paths);
            return;
        }
        System.out.println(depth + item.getNodeName());
        if ("g".equals(item.getNodeName())) {
            String id = getAttrOrNull(item, "id");
            String element = getAttrOrNull(item, "kvg:element");
            String position = getAttrOrNull(item, "kvg:position");
            String parentString = getAttrOrNull(parent, "id");
            KanjiGroup kanjiGroup = new KanjiGroup(id, element, position, parentString);
            groups.add(kanjiGroup);
            System.out.println(kanjiGroup);
        }
        if (item.hasChildNodes()) {
            NodeList childNodes = item.getChildNodes();
            int length = childNodes.getLength();
            for(int i = 0; i<length; i++) {
                processTree(childNodes.item(i), item, groups, paths,depth +1 );
            }
        }
    }

    private static void processPath(Node item, Node parent, List<KanjiPart> paths) {
        NamedNodeMap attributes = item.getAttributes();
        String id = attributes.getNamedItem("id").getNodeValue();
        String type = attributes.getNamedItem("kvg:type").getNodeValue();
        String d = attributes.getNamedItem("d").getNodeValue();
        KanjiPart kanjiPart = new KanjiPart(id, type, d, parent.getAttributes().getNamedItem("id").getNodeValue());
        paths.add(kanjiPart);
        System.out.println(kanjiPart);

    }

    private static String getAttrOrNull(Node node, String name) {
        if (!node.hasAttributes()) return null;
        Node attrNode = node.getAttributes().getNamedItem(name);
        return attrNode == null ? null : attrNode.getNodeValue();
    }
}
