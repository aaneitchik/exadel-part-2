package bsu.fpmi.chat.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.IOException;

public class XMLUtil {
    private static final XMLUtil instance = new XMLUtil();

    private XMLUtil() {
        super();
    }

    public static XMLUtil getInstance() {
        return instance;
    }

    public synchronized void startWritingUsersToXML(String filepath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("users");
            doc.appendChild(rootElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean validateUser(String email, String password, String filepath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            //checking, if this user already exists
            NodeList users = doc.getElementsByTagName("user");
            for(int i = 0; i < users.getLength(); i++) {
                Node user = users.item(i);
                if(user.getNodeType() == Node.ELEMENT_NODE) {
                    if(user.getChildNodes().item(0).getTextContent().equals(email) && user.getChildNodes().item(1).getTextContent().equals(password)) {
                        return true;
                    }
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public synchronized boolean addUserToXML(String email, String password, String filepath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            //checking, if this user already exists
            NodeList users = doc.getElementsByTagName("user");
            for(int i = 0; i < users.getLength(); i++) {
                Node user = users.item(i);
                if(user.getNodeType() == Node.ELEMENT_NODE) {
                    if(user.getChildNodes().item(0).getTextContent().equals(email)) {
                        return false;
                    }
                }
            }

            Element userElement = doc.createElement("user");
            Node root = doc.getFirstChild();
            root.appendChild(userElement);

            Element emailElement = doc.createElement("email");
            emailElement.appendChild(doc.createTextNode(email));
            userElement.appendChild(emailElement);

            Element passwordElement = doc.createElement("password");
            passwordElement.appendChild(doc.createTextNode(password));
            userElement.appendChild(passwordElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return true;
    }
}
