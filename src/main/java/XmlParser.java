import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class XmlParser {

    private static String date;
    private static List<String> objectIds;

    static {
        try(BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/data.txt"))) {
            date = reader.readLine();
            String objectIdsString = reader.readLine();
            objectIds = Arrays.asList(objectIdsString.split(", "));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        task1();
        task2();
    }

    private static void task2() {
        NodeList objectNodes = getAllNodes("OBJECT", "src/main/resources/AS_ADDR_OBJ.XML");
        NodeList parentNodes = getAllNodes("ITEM", "src/main/resources/AS_ADM_HIERARCHY.XML");
        List<String> addresses = new ArrayList<>();
        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element element = (Element) objectNodes.item(i);
            String objectId = element.getAttribute("OBJECTID");
            String typeName = element.getAttribute("TYPENAME");
            String name = element.getAttribute("NAME");

            if (typeName.equals("проезд")) {
                List<String> fullAddress = new ArrayList<>();
                fullAddress.add(typeName + " " + name);
                while (true) {
                    String parentId = findParentIdById(parentNodes, objectId);

                    if (parentId.equals("0")) {
                        Collections.reverse(fullAddress);
                        addresses.add(String.join(", ", fullAddress));
                        break;
                    }
                    String parentObjectNameAndType = findObjectNameAndTypeById(objectNodes, parentId);
                    fullAddress.add(parentObjectNameAndType);
                    objectId = parentId;
                }
            }
        }

        addresses.forEach(address -> System.out.println(address));
    }

    private static String findObjectNameAndTypeById(NodeList nodes, String objectId) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String objectIdToFind = element.getAttribute("OBJECTID");
            if (objectIdToFind.equals(objectId)) {
                String name = element.getAttribute("NAME");
                String type = element.getAttribute("TYPENAME");
                return type + " " + name;
            }
        }

        return "";
    }

    private static String findParentIdById(NodeList nodes, String objectId) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String objectIdToFind = element.getAttribute("OBJECTID");
            String activeconnection = element.getAttribute("ISACTIVE");
            Boolean isActiveConnection = activeconnection.equals("1");
            if (objectIdToFind.equals(objectId) && isActiveConnection) {
                return element.getAttribute("PARENTOBJID");
            }
        }

        return "";
    }

    private static void task1() {
        NodeList nodes = getAllNodes("OBJECT", "src/main/resources/AS_ADDR_OBJ.XML");
        Map<String, String> addresses = new HashMap<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String objectId = element.getAttribute("OBJECTID");
            String actual = element.getAttribute("ISACTUAL");
            LocalDate startDate = toLocalDate(element.getAttribute("STARTDATE"));
            LocalDate endDate = toLocalDate(element.getAttribute("ENDDATE"));
            LocalDate dateToCheck = toLocalDate(date);
            boolean isInDatesRange = dateToCheck.isAfter(startDate) && dateToCheck.isBefore(endDate);
            boolean isActual = actual.equals("1");

            if (objectIds.contains(objectId) && isInDatesRange && isActual) {
                addresses.put(objectId, element.getAttribute("TYPENAME") + " " + element.getAttribute("NAME"));
            }
        }

        addresses.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    private static NodeList getAllNodes(String rootTag, String fileName) {
        Document document;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(new File(fileName));
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return null;
        }
        return document.getElementsByTagName(rootTag);
    }


//    private static boolean toBoolean(String value) {
//        return !value.equals("0");
//    }

    private static LocalDate toLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }



}
