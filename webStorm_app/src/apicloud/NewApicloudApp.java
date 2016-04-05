/**
 * WebStorm APICloud plugin
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package apicloud;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
import org.xml.sax.SAXException;

public class NewApicloudApp {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String appName ="";
		String workPath = "";
		String loaderPath = "";
		String type = "";
		String filePath = "";
		if(args.length > 0){
			workPath = args[0]+File.separator;
		}
		if(args.length > 1){
			appName = args[1];
		}

		if(args.length > 2){
			type =  args[2];
		}
		if(args.length > 3){
			filePath = args[3]+File.separator;
		}
		if(isEmpty(appName)){
			System.out.println("appName is null");
			return;
		}
		if(isEmpty(workPath)){
			System.out.println("workPath is null");
			return;
		}
		if(isEmpty(type)){
			System.out.println("type is null");
			return;
		}
		loaderPath = workPath+"webStorm-APICloud"+File.separator+"appLoader"+File.separator;

		if("default".equals(type)){
			NewApicloudDefaultApp(appName,workPath,loaderPath);
		}else if("bottom".equals(type)){
			NewApicloudBottomApp(appName,workPath,loaderPath);
		}else if("home".equals(type)){
			NewApicloudHomeApp(appName,workPath,loaderPath);
		}else if("slide".equals(type)){
			NewApicloudSlideApp(appName,workPath,loaderPath);
		}else if("new".equals(type)){
			try {
				newFile(loaderPath,appName,filePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getMessage());
			}
		}
	}
	private static void NewApicloudDefaultApp(String appName,String workPath,String loaderPath){
		copyFolder(loaderPath+"widget"+File.separator+"default",workPath+appName);
		changeName(workPath+appName+File.separator+"config.xml",appName);
		
	}
	private static void NewApicloudBottomApp(String appName,String workPath,String loaderPath){
		copyFolder(loaderPath+"widget"+File.separator+"bottom",workPath+appName);
		changeName(workPath+appName+File.separator+"config.xml",appName);
		
	}
	private static void NewApicloudHomeApp(String appName,String workPath,String loaderPath){
		copyFolder(loaderPath+"widget"+File.separator+"home",workPath+appName);
		changeName(workPath+appName+File.separator+"config.xml",appName);
		
	}
	private static void NewApicloudSlideApp(String appName,String workPath,String loaderPath){
		copyFolder(loaderPath+"widget"+File.separator+"slide",workPath+appName);
		changeName(workPath+appName+File.separator+"config.xml",appName);
		
	}
	private static void newFile(String loaderPath,String fileName,String filePath) throws IOException{
		
		File oldFile = new File(loaderPath+"widget"+File.separator+"new.html");
		if(!oldFile.exists()){
			System.out.println("File missing");
			return;
		}
		FileInputStream input = new FileInputStream(oldFile);
		FileOutputStream output = new FileOutputStream(filePath+fileName+".html");

		byte[] b = new byte[5120];
		int len;
		while ((len = input.read(b)) != -1) {

			output.write(b, 0, len);
		}
		output.flush();
		output.close();
		input.close();
		
	}
	private static void copyFolder(String oldPath, String newPath) {
		try {
			new File(newPath).mkdirs();
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; ++i) {
				if (oldPath.endsWith(File.separator))
					temp = new File(oldPath + file[i]);
				else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ "/" + temp.getName().toString());

					byte[] b = new byte[5120];
					int len;
					while ((len = input.read(b)) != -1) {

						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory())
					copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static boolean isEmpty(String value) {
		return (("".equals(value)) || (value == null) || "null".equals(value));
	}
	private static void changeName(String config,String name){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		Element wgtName = null;
		Element root = null;
		try {
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.parse(new File(config));
			root = xmldoc.getDocumentElement();
			
			wgtName = (Element) selectSingleNode("/widget/name", root);
			wgtName.setTextContent(name);
			// theBook.setTextContent(replaceValue);
			saveXml(config, xmldoc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static Node selectSingleNode(String express, Object source) {
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
	private static void saveXml(String fileName, Document doc) {
		TransformerFactory transFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transFactory.newTransformer();
			transformer.setOutputProperty("indent", "no");
			doc.setXmlStandalone(true);
			// transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
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
