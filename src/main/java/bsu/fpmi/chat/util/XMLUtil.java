package bsu.fpmi.chat.util;

import bsu.fpmi.chat.model.Message;
import bsu.fpmi.chat.model.MessageStorage;
import org.apache.log4j.Logger;
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
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;


public class XMLUtil {
    private static final XMLUtil instance = new XMLUtil();

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm ");

    private XMLUtil() {
        super();
    }

    public static XMLUtil getInstance() {
        return instance;
    }

    public synchronized void startWritingToXML(String filepath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("messages");
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

    public synchronized void addMessageToXML(Message message, Date currentDate, String filepath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            Element msgElement = doc.createElement("message");
            msgElement.setAttribute("id", message.getId());
            Node root = doc.getFirstChild();
            root.appendChild(msgElement);

            Element author = doc.createElement("author");
            author.appendChild(doc.createTextNode(message.getUserName()));
            msgElement.appendChild(author);

            Element text = doc.createElement("text");
            text.appendChild(doc.createTextNode(message.getMessage()));
            msgElement.appendChild(text);

            Element date = doc.createElement("date");
            date.appendChild(doc.createTextNode(dateFormat.format(currentDate)));
            msgElement.appendChild(date);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

        }  catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public synchronized void deleteMessageFromXML(String id, String filepath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            NodeList messages = doc.getElementsByTagName("message");

            for(int i = 0; i < messages.getLength(); i++) {
                Node message = messages.item(i);
                if(message.getNodeType() == Node.ELEMENT_NODE) {
                    if(((Element)message).getAttribute("id").equals(id)) {
                        message.getChildNodes().item(0).setTextContent("");
                        message.getChildNodes().item(1).setTextContent("");
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
    }

    public static void editMessageInXML(String id, String text, String filepath) {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            NodeList messages = doc.getElementsByTagName("message");

            for(int i = 0; i < messages.getLength(); i++) {
                Node message = messages.item(i);
                if(message.getNodeType() == Node.ELEMENT_NODE) {
                    if(((Element)message).getAttribute("id").equals(id)) {
                        message.getChildNodes().item(1).setTextContent(text);
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
    }

    public static void historyParser(Logger logger, String filepath) {
        List<Message> messageList = new ArrayList<Message>();
        try {
            File xmlFile = new File(filepath);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            NodeList messages = doc.getElementsByTagName("message");

            if (messages.getLength() == 0) {
                logger.info("Message history is empty");
                return;
            }

            logger.info("Loading message history...");

            for (int i = 0; i < messages.getLength(); i++) {
                Node message = messages.item(i);
                if (message.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) message;

                    String mDate = ((Element) message).getElementsByTagName("date").item(0).getTextContent();
                    String mText = ((Element) message).getElementsByTagName("text").item(0).getTextContent();
                    String mAuthor = ((Element) message).getElementsByTagName("author").item(0).getTextContent();
                    String mId = ((Element) message).getAttribute("id");

                    MessageStorage.addMessage(new Message(mId, mAuthor, mText, "standard"));

                    logger.info(mDate + " " + mAuthor + " : " + mText);
                }
            }

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
