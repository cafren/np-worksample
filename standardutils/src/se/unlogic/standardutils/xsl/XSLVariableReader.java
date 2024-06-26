/*******************************************************************************
 * Copyright (c) 2010 Robert "Unlogic" Olofsson (unlogic@unlogic.se).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0-standalone.html
 ******************************************************************************/
package se.unlogic.standardutils.xsl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import se.unlogic.standardutils.collections.CollectionUtils;
import se.unlogic.standardutils.string.StringUtils;
import se.unlogic.standardutils.xml.ClassPathURIResolver;
import se.unlogic.standardutils.xml.PooledXPathFactory;
import se.unlogic.standardutils.xml.XMLUtils;

/**
 * This class is used to read the values of xsl:variable tags in XSL stylesheets. It recursively parses through all xsl:includes and xsl:imports and also
 * handles 'classpath://' style URI's.
 * 
 * @author Robert "Unlogic" Olofsson
 * 
 */
public class XSLVariableReader {

	private final Document doc;
	private final List<Document> subDocuments;

	private final XPath xpath = PooledXPathFactory.newXPath();

	public XSLVariableReader(Document doc) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, URISyntaxException {

		this.doc = doc;
		subDocuments = this.getSubDocuments(doc, null);
	}

	public XSLVariableReader(URI uri) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, URISyntaxException {

		this.doc = XMLUtils.parseXML(uri, false, false);
		subDocuments = this.getSubDocuments(doc, null);
	}

	public XSLVariableReader(String filePath) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, URISyntaxException {

		this.doc = XMLUtils.parseXMLFile(filePath, false, false);
		subDocuments = this.getSubDocuments(doc, null);
	}

	public XSLVariableReader(File file) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException, URISyntaxException {

		this.doc = XMLUtils.parseXMLFile(file, false, false);
		subDocuments = this.getSubDocuments(doc, null);
	}

	protected List<Document> getSubDocuments(Document doc, List<Document> subDocuments) throws SAXException, IOException, ParserConfigurationException, URISyntaxException, XPathExpressionException {

		URI uri = new URI(doc.getBaseURI());

		NodeList includeNodeList = (NodeList) xpath.evaluate("/stylesheet/include/@href", doc, XPathConstants.NODESET);
		NodeList importNodeList = (NodeList) xpath.evaluate("/stylesheet/import/@href", doc, XPathConstants.NODESET);
		
		List<Node> nodeList = null;
		
		nodeList = CollectionUtils.addAndInstantiateIfNeeded(nodeList, XMLUtils.getList(includeNodeList));
		nodeList = CollectionUtils.addAndInstantiateIfNeeded(nodeList, XMLUtils.getList(importNodeList));
		
		if (nodeList != null) {

			for(Node node : nodeList){
			
				URI subURI = new URI(node.getTextContent());
				
				if (!subURI.isAbsolute()) {

					subURI = new URL(uri.toURL(),node.getTextContent()).toURI();
				}

				if (subURI.toString().startsWith(ClassPathURIResolver.PREFIX) && subURI.toString().length() > ClassPathURIResolver.PREFIX.length()) {

					subURI = ClassPathURIResolver.getURL(subURI.toString()).toURI();
				}

				Document subDoc = XMLUtils.parseXML(subURI, false, false);

				if (subDocuments == null) {

					subDocuments = new ArrayList<Document>();
				}

				subDocuments.add(subDoc);

				this.getSubDocuments(subDoc, subDocuments);				
			}
		}

		return subDocuments;
	}

	public String getValue(String name) {

		try {
			String response = this.xpath.evaluate("/stylesheet/variable[@name='" + name + "']/text()", this.doc.getDocumentElement());

			if (subDocuments != null && StringUtils.isEmpty(response)) {

				for (Document document : subDocuments) {

					response = this.xpath.evaluate("/stylesheet/variable[@name='" + name + "']/text()", document.getDocumentElement());

					if (!StringUtils.isEmpty(response)) {

						return response;
					}
				}
			}

			return response;

		} catch (XPathExpressionException e) {
			return null;
		}
	}
}
