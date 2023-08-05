package se.sandboge.kanjisvg;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    public static void main(String[] args) {
        final String path;
        System.out.println("Kanji SVG");
        if (args.length > 0) {
            path = args[0];
        } else {
            path = "src/main/resources/svg/06426.svg";
        }
        List<String> files;
        File file = new File(path);

        if (file.isDirectory()) {
            String[] array = file.list();
            for (int i = 0; i< Objects.requireNonNull(array).length; i++) {
                array[i] = path + System.getProperty("file.separator") + array[i];
            }
            files = List.of(array);
        } else {
            files = Collections.singletonList(path);
        }
        files.forEach(p -> {
            try {
                File f = (new File(p)).getAbsoluteFile();
                processFile(f);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Skipping file " + p);
            }
        });
    }

    private static void processFile(File file) throws IOException, SAXException, ParserConfigurationException {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        Node rootNode = doc.getElementsByTagName("svg").item(0);
        List<KanjiGroup> groups = new ArrayList<>();
        List<KanjiPart> paths = new ArrayList<>();
        List<KanjiRule> kanjiRules = new ArrayList<>();
        processTree(rootNode, doc, groups, paths, kanjiRules,0);
        String width = getAttrOrNull(rootNode, "width");
        String height = getAttrOrNull(rootNode, "height");
        String viewBox = getAttrOrNull(rootNode, "viewBox");
        SvgElement svgElement = new SvgElement(width, height, viewBox, groups, paths, kanjiRules);
        System.out.println(svgElement);
        toJson(svgElement);
    }

    private static void toJson(SvgElement svgElement) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println(objectMapper.writeValueAsString(svgElement));
    }

    private static void processTree(Node item, Node parent, List<KanjiGroup> groups, List<KanjiPart> paths, List<KanjiRule> kanjiRules, int depth) {
        if (Node.ELEMENT_NODE != item.getNodeType() && Node.TEXT_NODE != item.getNodeType()) {
            System.out.println(item.getNodeType());
            return;
        }
        if (Node.TEXT_NODE == item.getNodeType()) {
            return;
        }
        if ("path".equals(item.getNodeName())) {
            processPath(item, parent, paths, kanjiRules);
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
                processTree(childNodes.item(i), item, groups, paths, kanjiRules,depth +1 );
            }
        }
    }

    private static void processPath(Node item, Node parent, List<KanjiPart> paths, List<KanjiRule> kanjiRules) {
        NamedNodeMap attributes = item.getAttributes();
        String id = attributes.getNamedItem("id").getNodeValue();
        String type = getAttrOrNull(item, "kvg:type");
        String d = attributes.getNamedItem("d").getNodeValue();
        KanjiStroke kanjiStroke = parseD(d);
        KanjiPart kanjiPart = new KanjiPart(id, type, d, parent.getAttributes().getNamedItem("id").getNodeValue());
        paths.add(kanjiPart);


    }

    private static KanjiStroke parseD(String d) {
        List<String> commands = splitToCommands(d);
        if (d == null || !d.startsWith("M")) {
            System.out.println("Error: A stroke must start with M");
            return null;
        }
        float sx = Float.parseFloat(d.substring(1, d.indexOf(',')));

        return null; //new KanjiStroke(new Point(sx, sy), new Point(ex, ey));
    }

    private static List<String> splitToCommands(String d) {
        List<String> commands = new ArrayList<>();
        int start = 1;
        for (int i=1; i<=d.length(); i++) {
            if (i == d.length() || Character.isAlphabetic(d.charAt(i))) {
                System.out.println(d.substring(start -1, i));
                commands.add(d.substring(start -1, i));
                start = i +1;
            }
        }
        return commands;
    }

    private static String getAttrOrNull(Node node, String name) {
        if (!node.hasAttributes()) return null;
        Node attrNode = node.getAttributes().getNamedItem(name);
        return attrNode == null ? null : attrNode.getNodeValue();
    }
}
