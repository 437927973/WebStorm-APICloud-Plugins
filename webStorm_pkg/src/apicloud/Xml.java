/**
 * WebStorm APICloud plugin
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */


package apicloud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Xml {
	public static void updateStringXML(String file,String elementName,String replaceValue,String outFile){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element theBook = null;
		Element root = null;
		try {
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.parse(new File(file));
			root = xmldoc.getDocumentElement();			
			theBook = (Element) selectSingleNode(elementName,
					root);
			theBook.setTextContent(replaceValue);
			saveXml(outFile, xmldoc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void updateAndroidManifestXML(String file,String replaceValue,String outFile) throws Exception{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element root = null;
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.parse(new File(file));
			root = xmldoc.getDocumentElement();			
			root.setAttribute("package","com.apicloud."+ replaceValue);
//			System.out.println(root.getAttribute("package"));
		
			NodeList providers =root.getElementsByTagName("provider");
			
			for(int i = 0;i<providers.getLength();i++){
				Element provider = (Element)providers.item(i);
				provider.setAttribute("android:authorities",root.getAttribute("package")+"."+ provider.getAttribute("android:authorities"));
//				System.out.println(provider.getAttribute("android:authorities"));
			}
			NodeList application =root.getElementsByTagName("application");
			NodeList metaDatas = ((Element)application.item(0)).getElementsByTagName("meta-data");
			for(int i = 0 ;i < metaDatas.getLength();i++){
				Element metaData = (Element)metaDatas.item(i);
//				System.out.println(metaData.getAttribute("android:name"));
				if(metaData.getAttribute("android:name").equals("com.baidu.lbsapi.API_KEY")){
					metaData.getParentNode().removeChild(metaData);
				}
			}
			
			saveXml(outFile, xmldoc);
	}
	public static Map<String,String> getWidgetInfo(String config){
		Map<String,String> map = new HashMap<String, String>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element widget = null;
		Element wgtName = null;
		Element root = null;
		try {
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.parse(new File(config));
			root = xmldoc.getDocumentElement();
			widget = (Element) selectSingleNode("/widget", root);
			map.put("appId", widget.getAttribute("id")) ;
			
			wgtName = (Element) selectSingleNode("/widget/name", root);
			map.put("appName", wgtName.getTextContent()) ;
			
			// theBook.setTextContent(replaceValue);
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	public static void getFeatureApiKey(String config,Map<String,String> map){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		Element root = null;
		try {
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.parse(new File(config));
			root = xmldoc.getDocumentElement();
			NodeList features = selectNodes("/widget/feature/param[@name='android_api_key']", root);
			for(int i = 0; i < features.getLength(); i++){
				Element feature = (Element) features.item(i);
				
//				System.out
//						.println(feature.getAttribute("name"));
				String key = ((Element)feature.getParentNode()).getAttribute("name");
				String value = feature.getAttribute("value");
//				System.out
//				.println(key);
//				System.out
//				.println(value);
				
				map.put(key, value);
				
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static Node selectSingleNode(String express, Object source) {// 查找节点，并返回第一个符合条件节点
		Node result = null;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		try {
			result = (Node) xpath
					.evaluate(express, source, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static NodeList selectNodes(String express, Object source) {// 查找节点，返回符合条件的节点集。
		NodeList result = null;
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		try {
			result = (NodeList) xpath.evaluate(express, source,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void saveXml(String fileName, Document doc) {// 将Document输出到文件
		TransformerFactory transFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty("indent", "yes");

			DOMSource source = new DOMSource();
			source.setNode(doc);
			StreamResult result = new StreamResult();
			result.setOutputStream(new FileOutputStream(fileName));

			transformer.transform(source, result);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
