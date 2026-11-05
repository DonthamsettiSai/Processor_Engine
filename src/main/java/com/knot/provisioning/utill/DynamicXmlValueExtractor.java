
package com.knot.provisioning.utill;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/** 
 * @category Soap based XML Dynamic Replacement Construction
 * @author SAI D   
 */

public class DynamicXmlValueExtractor {

	public static Map<String, String> getValues(String xml, String operationName, String modifier, List<String> keys)
			throws Exception {

		if (xml == null || xml.isBlank()) {
			throw new IllegalArgumentException("XML cannot be null or empty");
		}

		if (operationName == null || operationName.isBlank() || modifier == null || modifier.isBlank() || keys == null
				|| keys.isEmpty()) {
			throw new IllegalArgumentException("Operation name, modifier and keys are required");
		}

		Set<String> remainingKeys = new LinkedHashSet<>(keys);
		Map<String, String> result = new LinkedHashMap<>();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);

		/** Prevent XML external entity attacks */
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");

		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

		NodeList operations = document.getElementsByTagNameNS("*", "Operation");

		for (int i = 0; i < operations.getLength(); i++) {

			Element operation = (Element) operations.item(i);

			if (!operationName.equals(operation.getAttribute("name"))
					|| !modifier.equals(operation.getAttribute("modifier"))) {
				continue;
			}

			NodeList elements = operation.getElementsByTagName("*");

			for (int j = 0; j < elements.getLength(); j++) {

				Element element = (Element) elements.item(j);
				String key = element.getAttribute("name");

				if (!remainingKeys.contains(key)) {
					continue;
				}

				/** Skip parent structures; extract leaf elements only */
				boolean hasElementChild = false;
				NodeList children = element.getChildNodes();

				for (int k = 0; k < children.getLength(); k++) {
					if (children.item(k).getNodeType() == Node.ELEMENT_NODE) {
						hasElementChild = true;
						break;
					}
				}

				if (hasElementChild) {
					continue;
				}

				String value = element.getTextContent();

				if (value == null || value.isBlank()) {
					throw new IllegalStateException("Value is empty for XML key: " + key);
				}

				result.put(key, value.trim());
				remainingKeys.remove(key);

				if (remainingKeys.isEmpty()) {
					return result;
				}
			}
		}

		if (!remainingKeys.isEmpty()) {
			throw new IllegalStateException("Keys not found in operation '" + operationName + "', modifier '" + modifier
					+ "': " + remainingKeys);
		}

		return result;
	}
}
