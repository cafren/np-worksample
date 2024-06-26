/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import se.unlogic.standardutils.collections.CollectionUtils;
import se.unlogic.standardutils.io.CloseUtils;
import se.unlogic.standardutils.string.StringUtils;

public class XMLUtils {
	
	private static final String CONTENT_TYPE = "text/xml";

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
	private static final DocumentBuilderFactory NAMESPACE_AWARE_DOCUMENT_BUILDER_FACTORY;
	private static final DocumentBuilderFactory VALIDATING_DOCUMENT_BUILDER_FACTORY;
	private static final DocumentBuilderFactory NAMESPACE_AWARE_VALIDATING_DOCUMENT_BUILDER_FACTORY;

	private static final DocumentBuilder DOCUMENT_BUILDER;
	private static final DocumentBuilder NAMESPACE_AWARE_DOCUMENT_BUILDER;

	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

	static {
		try {
			DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
			setFactoryParam(DOCUMENT_BUILDER_FACTORY, false, false);

			NAMESPACE_AWARE_DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
			NAMESPACE_AWARE_DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
			setFactoryParam(NAMESPACE_AWARE_DOCUMENT_BUILDER_FACTORY, false, true);

			VALIDATING_DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
			VALIDATING_DOCUMENT_BUILDER_FACTORY.setValidating(true);
			setFactoryParam(VALIDATING_DOCUMENT_BUILDER_FACTORY, true, false);

			NAMESPACE_AWARE_VALIDATING_DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
			NAMESPACE_AWARE_VALIDATING_DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
			NAMESPACE_AWARE_VALIDATING_DOCUMENT_BUILDER_FACTORY.setValidating(true);
			setFactoryParam(NAMESPACE_AWARE_VALIDATING_DOCUMENT_BUILDER_FACTORY, true, true);

			DOCUMENT_BUILDER = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
			NAMESPACE_AWARE_DOCUMENT_BUILDER = NAMESPACE_AWARE_DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();

		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Document createDomDocument() {

		return DOCUMENT_BUILDER.newDocument();
	}

	public static Document createNamespaceAwareDomDocument(String namespaceURI, String qualifiedName) {

		return NAMESPACE_AWARE_DOCUMENT_BUILDER.getDOMImplementation().createDocument(namespaceURI, qualifiedName, null);
	}

	public static String toString(Node node, String encoding, boolean indent) throws TransformerFactoryConfigurationError, TransformerException {

		Source source = new DOMSource(node);
		StringWriter sw = new StringWriter();
		Result result = new StreamResult(sw);

		Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);

		if (indent) {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		}

		transformer.transform(source, result);

		return sw.getBuffer().toString();
	}

	public static void toString(Node node, String encoding, Writer w, boolean indent) throws TransformerFactoryConfigurationError, TransformerException {

		Source source = new DOMSource(node);
		Result result = new StreamResult(w);

		Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);

		if (indent) {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		}

		transformer.transform(source, result);
	}

	public static Document parseXMLFile(String filename, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		return parseXMLFile(new File(filename), validating, namespaceAware);
	}

	public static Document parseXMLFile(File file, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = getDocumentBuilderFactory(validating, namespaceAware);

		Document doc = factory.newDocumentBuilder().parse(file);

		return doc;
	}

	public static Document parseXMLFile(Path file, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		InputStream inputStream = Files.newInputStream(file);

		try {
			DocumentBuilderFactory factory = getDocumentBuilderFactory(validating, namespaceAware);

			Document doc = factory.newDocumentBuilder().parse(inputStream);

			return doc;

		} finally {

			CloseUtils.close(inputStream);
		}
	}

	public static Document parseXMLFile(String file, String encoding, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		return parseXMLFile(new File(file), encoding, validating, namespaceAware);
	}

	public static Document parseXMLFile(File file, String encoding, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		InputStream inputStream = null;

		try {
			inputStream = new FileInputStream(file);

			return XMLUtils.parseXML(inputStream, false, false, encoding);

		} finally {

			CloseUtils.close(inputStream);
		}
	}

	public static Document parseXML(URI uri, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = getDocumentBuilderFactory(validating, namespaceAware);

		Document doc = factory.newDocumentBuilder().parse(uri.toString());

		return doc;
	}

	public static Document parseXML(InputSource inputSource, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = getDocumentBuilderFactory(validating, namespaceAware);

		Document doc = factory.newDocumentBuilder().parse(inputSource);

		return doc;
	}

	public static Document parseXML(InputStream stream, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory factory = getDocumentBuilderFactory(validating, namespaceAware);

		Document doc = factory.newDocumentBuilder().parse(stream);

		return doc;
	}

	public static Document parseXML(InputStream stream, boolean validating, boolean namespaceAware, String encoding) throws SAXException, IOException, ParserConfigurationException {

		Reader reader = null;

		try {
			reader = new InputStreamReader(stream, encoding);

			InputSource inputSource = new InputSource(reader);
			inputSource.setEncoding(encoding);

			return XMLUtils.parseXML(inputSource, validating, namespaceAware);

		} finally {

			CloseUtils.close(reader);
		}
	}

	private static void setFactoryParam(DocumentBuilderFactory factory, boolean validating, boolean namespaceAware) throws ParserConfigurationException {

		try {
			factory.setFeature("http://xml.org/sax/features/namespaces", namespaceAware);
			factory.setFeature("http://xml.org/sax/features/validation", validating);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (ParserConfigurationException e) {}
	}

	public static Document parseXML(String string, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		return parseXML(StringUtils.getInputStream(string), validating, namespaceAware);
	}

	public static Document parseXML(String string, String encoding, boolean validating, boolean namespaceAware) throws SAXException, IOException, ParserConfigurationException {

		return parseXML(new ByteArrayInputStream(string.getBytes(encoding)), validating, namespaceAware);
	}

	public static Element createElement(String name, Object value, Document doc) {

		Element element = doc.createElement(name);
		element.appendChild(doc.createTextNode(value.toString()));
		return element;
	}

	public static Element createElementNS(String namespace, String name, Object value, Document doc) {

		Element element = doc.createElementNS(namespace, name);
		element.appendChild(doc.createTextNode(value.toString()));
		return element;
	}

	public static Element createCDATAElement(String name, Object value, Document doc) {

		Element element = doc.createElement(name);
		element.appendChild(doc.createCDATASection(value.toString()));
		return element;
	}

	public static void writeXMLFile(Node node, String filename, boolean indent, String encoding) throws TransformerFactoryConfigurationError, TransformerException, FileNotFoundException {

		writeXMLFile(node, filename, indent, encoding, null);
	}

	public static void writeXMLFile(Node node, String filename, boolean indent, String encoding, Integer indentAmount) throws TransformerFactoryConfigurationError, TransformerException, FileNotFoundException {

		// Prepare the output file
		File file = new File(filename);

		writeXMLFile(node, file, indent, encoding, null, indentAmount);
	}

	public static void writeXMLFile(Node node, File file, boolean indent, String encoding) throws TransformerFactoryConfigurationError, TransformerException, FileNotFoundException {

		writeXMLFile(node, file, indent, encoding, null);
	}

	public static void writeXMLFile(Node node, File file, boolean indent, String encoding, String version) throws TransformerFactoryConfigurationError, TransformerException, FileNotFoundException {

		writeXMLFile(node, file, indent, encoding, version, null);
	}

	public static void writeXMLFile(Node node, File file, boolean indent, String encoding, String version, Integer indentAmount) throws TransformerFactoryConfigurationError, TransformerException, FileNotFoundException {

		FileOutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(file);

			writeXML(node, outputStream, indent, encoding, version, indentAmount);

		} finally {

			CloseUtils.close(outputStream);
		}
	}

	public static void writeXML(Node node, OutputStream outputStream, boolean indent, String encoding) throws TransformerFactoryConfigurationError, TransformerException {

		writeXML(node, outputStream, indent, encoding, null);
	}

	public static void writeXML(Node node, OutputStream outputStream, boolean indent, String encoding, String version) throws TransformerFactoryConfigurationError, TransformerException {

		writeXML(node, outputStream, indent, encoding, version, null);
	}

	public static void writeXML(Node node, OutputStream outputStream, boolean indent, String encoding, String version, Integer indentAmount) throws TransformerFactoryConfigurationError, TransformerException {

		// Prepare the DOM document for writing
		Source source = new DOMSource(node);

		// Prepare the output file
		Result result = new StreamResult(outputStream);

		Transformer transformer = TRANSFORMER_FACTORY.newTransformer();

		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);

		if (version != null) {

			transformer.setOutputProperty(OutputKeys.VERSION, version);
		}

		if (indent) {

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			if (indentAmount != null) {
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indentAmount.toString());
			}
		}

		transformer.transform(source, result);
	}

	public static Element append(Document doc, Element targetElement, Elementable elementable) {

		if (elementable != null) {

			return (Element) targetElement.appendChild(elementable.toXML(doc));
		}

		return null;
	}

	public static void append(Document doc, Element targetElement, Collection<? extends XMLable> beans) {

		if (beans != null && !beans.isEmpty()) {

			for (XMLable xmlable : beans) {
				targetElement.appendChild(xmlable.toXML(doc));
			}
		}
	}

	public static <T extends XMLable> void append(Document doc, Element targetElement, T[] beans) {

		if (beans != null) {

			for (XMLable xmlable : beans) {
				targetElement.appendChild(xmlable.toXML(doc));
			}
		}
	}

	public static void append(Document doc, Element targetElement, String elementName, String subElementsName, Object[] values) {

		if (values != null) {

			Element subElement = doc.createElement(elementName);
			targetElement.appendChild(subElement);

			for (Object value : values) {

				appendNewCDATAElement(doc, subElement, subElementsName, value);
			}
		}
	}

	public static void append(Document doc, Element targetElement, String elementName, String subElementsName, Collection<? extends Object> values) {

		if (!CollectionUtils.isEmpty(values)) {

			Element subElement = doc.createElement(elementName);
			targetElement.appendChild(subElement);

			for (Object value : values) {

				appendNewCDATAElement(doc, subElement, subElementsName, value);
			}
		}
	}

	public static Element append(Document doc, Element targetElement, String elementName, Collection<? extends XMLable> beans) {

		if (!CollectionUtils.isEmpty(beans)) {

			Element subElement = doc.createElement(elementName);
			targetElement.appendChild(subElement);

			for (XMLable xmlable : beans) {
				subElement.appendChild(xmlable.toXML(doc));
			}

			return subElement;
		}

		return null;
	}

	public static void appendAsElementName(Document doc, Element targetElement, String elementName, Collection<? extends Object> values) {

		if (!CollectionUtils.isEmpty(values)) {

			Element subElement = doc.createElement(elementName);
			targetElement.appendChild(subElement);

			for (Object value : values) {

				subElement.appendChild(doc.createElement(value.toString()));
			}
		}
	}

	public static void appendNewCDATAElement(Document doc, Element targetElement, String elementName, String value) {

		if (!StringUtils.isEmpty(value)) {
			targetElement.appendChild(createCDATAElement(elementName, value, doc));
		}
	}

	public static void appendNewElement(Document doc, Element targetElement, String elementName, String value) {

		if (!StringUtils.isEmpty(value)) {
			targetElement.appendChild(createElement(elementName, value, doc));
		}
	}

	public static void appendNewElementNS(Document doc, Element targetElement, String namespace, String elementName, String value) {

		if (!StringUtils.isEmpty(value)) {
			targetElement.appendChild(createElementNS(namespace, elementName, value, doc));
		}
	}

	public static Element appendNewElement(Document doc, Element targetElement, String elementName) {

		Element element = doc.createElement(elementName);

		targetElement.appendChild(element);

		return element;
	}

	public static Element appendNewElementNS(Document doc, Element targetElement, String namespace, String elementName) {

		Element element = doc.createElementNS(namespace, elementName);

		targetElement.appendChild(element);

		return element;
	}

	public static void appendNewCDATAElement(Document doc, Element targetElement, String elementName, Object value) {

		if (value != null) {
			appendNewCDATAElement(doc, targetElement, elementName, value.toString());
		}
	}

	public static void appendNewElement(Document doc, Element targetElement, String elementName, Object value) {

		if (value != null) {
			appendNewElement(doc, targetElement, elementName, value.toString());
		}

	}

	public static void appendNewElementNS(Document doc, Element targetElement, String namespace, String elementName, Object value) {

		if (value != null) {
			appendNewElementNS(doc, targetElement, namespace, elementName, value.toString());
		}

	}

	/**
	 * Adds or replaces node in parent.
	 *
	 * @param parent
	 * @param node
	 * @throws Exception - Node cannot exist more than once, i.e. multiple nodes with the same name cannot exist in parent.
	 */
	public static void replaceSingleNode(Element parent, final Node node) throws RuntimeException {

		NodeList nodes = parent.getElementsByTagName(node.getNodeName());

		if (nodes.getLength() > 1) {
			throw new RuntimeException("Parent element contains multiple nodes with the name " + node.getNodeName());
		}
		if (nodes.getLength() == 0) {
			parent.appendChild(node);
		} else {
			parent.replaceChild(node, nodes.item(0));
		}
	}

	public enum TimeUnit {
		HOUR, MINUTE, SECOND;
	}

	//Replace all usage of this method with client side javascript instead
	@Deprecated
	public static Element getTimeUnits(Document doc, TimeUnit timeUnit) {

		switch (timeUnit) {
			case HOUR:
				Element hoursElement = doc.createElement("hours");
				Element hourElement;
				for (int i = 0; i < 10; ++i) {
					hourElement = doc.createElement("hour");
					XMLUtils.appendNewElement(doc, hourElement, "value", "0" + i);
					hoursElement.appendChild(hourElement);
				}
				for (int i = 10; i < 24; ++i) {
					hourElement = doc.createElement("hour");
					XMLUtils.appendNewElement(doc, hourElement, "value", i);
					hoursElement.appendChild(hourElement);
				}
				return hoursElement;
			case MINUTE:
				Element minutesElement = doc.createElement("minutes");
				Element minuteElement;
				for (int i = 0; i < 10; ++i) {
					minuteElement = doc.createElement("minute");
					XMLUtils.appendNewElement(doc, minuteElement, "value", "0" + i);
					minutesElement.appendChild(minuteElement);
				}
				for (int i = 10; i < 60; ++i) {
					minuteElement = doc.createElement("minute");
					XMLUtils.appendNewElement(doc, minuteElement, "value", i);
					minutesElement.appendChild(minuteElement);
				}
				return minutesElement;
			case SECOND:
				Element secondsElement = doc.createElement("seconds");
				Element secondElement;
				for (int i = 0; i < 10; ++i) {
					secondElement = doc.createElement("second");
					XMLUtils.appendNewElement(doc, secondElement, "value", "0" + i);
					secondsElement.appendChild(secondElement);
				}
				for (int i = 10; i < 60; ++i) {
					secondElement = doc.createElement("second");
					XMLUtils.appendNewElement(doc, secondElement, "value", i);
					secondsElement.appendChild(secondElement);
				}
				return secondsElement;
		}
		return null;
	}

	public static String toValidElementName(String string) {

		if (string.length() >= 3 && string.substring(0, 3).equalsIgnoreCase("xml")) {

			string = "___" + string.substring(3);

		} else if (string.substring(0, 1).matches("[0-9]")) {

			string = "_" + string.substring(1);
		}

		string = string.replaceAll("[��]", "a");
		string = string.replaceAll("[��]", "A");
		string = string.replace("�", "o");
		string = string.replace("�", "O");

		return string.replaceAll("[^0-9a-zA-Z-.]", "_");
	}

	public static List<Node> getList(NodeList nodeList) {

		if (nodeList.getLength() == 0) {

			return null;
		}

		ArrayList<Node> list = new ArrayList<Node>(nodeList.getLength());

		int index = 0;

		while (index < nodeList.getLength()) {

			list.add(nodeList.item(index));

			index++;
		}

		return list;
	}

	public static boolean isValidXML(String xml) {

		try {
			parseXML(xml, false, false);
			return true;

		} catch (Exception ignore) {

			return false;
		}
	}

	private static DocumentBuilderFactory getDocumentBuilderFactory(boolean validating, boolean namespaceAware) {

		if (validating && namespaceAware) {

			return NAMESPACE_AWARE_VALIDATING_DOCUMENT_BUILDER_FACTORY;

		} else if (validating) {

			return VALIDATING_DOCUMENT_BUILDER_FACTORY;

		} else if (namespaceAware) {

			return NAMESPACE_AWARE_DOCUMENT_BUILDER_FACTORY;
		}

		return DOCUMENT_BUILDER_FACTORY;
	}

	public static <T extends XMLParserPopulateable> T parseBean(Element element, Class<T> beanClass) throws Exception {

		T bean = beanClass.newInstance();

		bean.populate(new XMLParser(element));

		return bean;
	}

	/**
	 * This method ensures that the output String has only valid XML unicode characters as specified by the XML 1.0 standard. For reference, please see
	 * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the standard</a>. This method will return an empty String if the input is null or empty.
	 *
	 * From: http://blog.mark-mclaren.info/2007/02/invalid-xml-characters-when-valid-utf8_5873.html
	 *
	 * @param xml The String whose non-valid characters we want to remove.
	 * @return The in String, stripped of non-valid characters.
	 */
	public static String stripNonValidXMLCharacters(String xml) {

		StringBuffer out = new StringBuffer(); // Used to hold the output.
		char current; // Used to reference the current character.

		if (xml == null || ("".equals(xml))){
			
			return xml;
		}
		
		for (int i = 0; i < xml.length(); i++) {
			
			current = xml.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
			
			if ((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF)) || ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF))) {
				out.append(current);
			}
		}
		
		return out.toString();
	}
	
	public static String getContentType() {

		return CONTENT_TYPE;
	}
}
