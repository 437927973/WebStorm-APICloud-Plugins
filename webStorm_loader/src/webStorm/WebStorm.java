/**
 * WebStorm APICloud plugin
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package webStorm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


public class WebStorm {
	private static String OS = System.getProperty("os.name").toLowerCase();
	private static String pkgName;
	private static String loaderName;
	private static String pendingVersion;
	private static String workPath;
	private static String adbPath;
	private static String cmdLogType;
	private static String wigetPath;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		workPath = "";
		
		wigetPath = "";
		if (args.length > 0 && args[0] != null) {
			workPath = args[0];
		}
		if (args.length > 1 && args[1] != null) {
			wigetPath = args[1];
		}
		if(args.length > 2 && args[2] != null && "ios".equalsIgnoreCase(args[2])){
			IosLoader.runMain(workPath, wigetPath);
			return;
		}
		System.out.println("start...");
		getWidgetPath(wigetPath);
		
		wigetPath +=File.separator;
		String configPath = wigetPath + "config.xml";
		
		File configFile = new File(configPath);
		if (!configFile.exists()) {
			System.out.println("Not found config.xml");
			return;
		}
		
		try {
			System.out.println("checkBasicInfo...");
			checkBasicInfo(workPath);
			List<String> devices = getDevices();
			System.out.println("get devices..."+devices.size());
			if (devices.size() == 0) {
				System.out.println("Not found Connected device");
				return;
			}
			System.out.println("getAppId...");
			String appId = getAppId(configPath);
			
			if (appId == null || "".equals(appId)) {
				System.out.println("Please make sure the directory is correct");
				return;
			}
			System.out.println("getLoaderType...");
			getLoaderType(appId);
			for (String device : devices) {
				System.out.println("pushDirOrFile... device = "+device);
				boolean isOk = pushDirOrFileCmd(device, wigetPath, appId);
				if(!isOk){
					System.out.println("Failed to copy the file to the mobile phone, please check the connection device");
					return;
				}
				if(pkgName.equals("com.apicloud.apploader")){
					System.out.println("pushStartInfo...");
					isOk = pushStartInfo(device,appId);
					if(!isOk){
						System.out.println("Failed to copy the file to the mobile phone, please check the connection device");
						return;
					}
				}
				System.out.println("getApploaderVersion...");
				String currentVersion=getApploaderVersionCmd(device);
				boolean isNeedInstall=true;
				if(currentVersion != null){
					System.out.println("compareAppLoaderVer...");
					isNeedInstall = compareAppLoaderVer(currentVersion,pendingVersion);
				}
				System.out.println("isNeedInstall is "+isNeedInstall);
				if(isNeedInstall){
					if(currentVersion != null){
						System.out.println("uninstallApploader...");
						isOk = uninstallApploaderCmd(device);
						if(!isOk){
							System.out.println("Failed to uninstall appLoader");
							continue;
						}
					}
					System.out.println("installAppLoader...");
					isOk =installAppLoaderCmd(device);
					if(!isOk){
						System.out.println("Install appLoader failed");
						continue;
					}
					
				}else{
					System.out.println("stopApploader...");
					stopApploaderCmd(device);
				}
				System.out.println("startApploader...");
				isOk =startApploaderCmd(device);
				if(!isOk){
					System.out.println("startApploader failed");
					continue;
				}
				System.out.println("end...device = "+device);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private static void getWidgetPath(String filePath){
		if(filePath == null || "".equals(filePath)){
			return;
		}
		File file = new File(filePath);
		String[] fileList = file.list();
		boolean isContinue = true;
		for(String fileS : fileList){
			if("config.xml".equals(fileS)){
				String content =  getWidgetContent(filePath+File.separator+"config.xml");
				if(isContent(fileList,content)){
					wigetPath = filePath;
					isContinue = false;
					break;
				}
			}
		}
		
		if(isContinue)getWidgetPath(file.getParent());
		
		
	}
	private static boolean isContent(String[] listFile,String content){
		for(String file:listFile){
			if(file.equals(content)){
				return true;
			}
		}
		return false;
	}
	private static int checkBasicInfo(String workParh) throws IOException{
		File toolsFile = new File(workParh+"tools");
		if(!toolsFile.exists() || !toolsFile.isDirectory()){
			return -1;
		}
		File load_conf_file = new File(workParh+"appLoader"+File.separator+"apicloud-loader"+File.separator+"load.conf");
		File load_apk_file = new File(workParh+"appLoader"+File.separator+"apicloud-loader"+File.separator+"load.apk");
		if(!load_conf_file.exists() || !load_apk_file.exists()){
			return -1;
		}
		if(isMacOS()){
			adbPath = workPath + "tools"+File.separator+"adb";
			String chx = "chmod +x " + adbPath;
			runCmd(chx,false);
		}else{
			adbPath = workPath + "tools"+File.separator+"adb.exe";
		}
		InputStreamReader read = new InputStreamReader(new FileInputStream(
				workParh+"appLoader"+File.separator+"apicloud-loader"+File.separator+"load.conf"));// 考虑到编码格式
		BufferedReader bufferedReader = new BufferedReader(read);
		String lineTxt = null;
		StringBuffer conf = new StringBuffer();
		while ((lineTxt = bufferedReader.readLine()) != null) {
			conf.append(lineTxt);
		}
		read.close();
		//System.out.println(conf);
		JSONObject json = JSONObject.fromObject(conf.toString());
		if(json.has("version")){
			pendingVersion = json.getString("version");
		}
		if(json.has("cmdLogType")){
			cmdLogType = json.getString("cmdLogType");
		}
		return 0;
	}
	public static boolean isMacOS(){
		return OS.indexOf("mac")>=0&&OS.indexOf("os")>0;
	}
	private static List<String> getDevices() throws IOException {
		List<String> devices = new ArrayList<String>();
		String cmd = adbPath + " devices ";
		
		
		Process p  =(Process) runCmd(cmd,true);
		//System.out.println(p);
		BufferedReader ok_buffer =new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		BufferedReader err_buffer = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));
		if(ok_buffer != null){
			
			String out = ok_buffer.readLine();
			while((out = ok_buffer.readLine()) != null){
				if(!out.contains("List of devices attached") && out.contains("device")){
					devices.add(out.split("\tdevice")[0]);
					//System.out.println("out = " + out.split("\tdevice")[0]);
				}
			}
		}
		if(err_buffer != null){
			String out = null;
			while((out = ok_buffer.readLine()) != null){
				//System.out.println("err out = " + out);
			}
		}
		if(p != null){
			p.destroy();
			p = null;
		}
		return devices;
	}

	private static boolean pushDirOrFileCmd( String serialNumber,
			String srcPath, String appId) throws IOException {
		
		
		String desPath = "/sdcard/UZMap/wgt/" + appId;
		String cachePath = System.getProperties().getProperty("user.home")
				+ File.separator + "uztools" + File.separator + appId+File.separator+"wgt"  ;
		if(!isMacOS()) {
			delAllFiles(cachePath);
		}else{
			String cmd = "rm -rf "+cachePath;
			runCmd(cmd,false);
		}
		copyFolder(srcPath,cachePath);
		String pushCmd =adbPath + " -s " + serialNumber + " push " + cachePath + " "
				+ desPath;
		if(!isMacOS()){
			pushCmd = "cmd.exe /C start "+pushCmd;
		}
		

		
		String out =  (String) runCmd(pushCmd,false);
//		System.out.println(out);
		if(!isMacOS()) {
			delAllFiles(cachePath);
		}else{
			String cmd = "rm -rf "+cachePath;
			runCmd(cmd,false);
		}
		if(out.contains("error: device not found")){
			return false;
		}
		return true;
	}
	public static boolean delAllFiles(String path){
		boolean flag = false;
	    File file = new File(path);
	    if (!file.exists()) {
	        return flag;
	    }
	    if (!file.isDirectory()) {
	        return flag;
	    }
	    String[] tempList = file.list();
	    File temp = null;
	    for (int i = 0; i < tempList.length; i++) {
	        if (path.endsWith(File.separator)) {
	            temp = new File(path + tempList[i]);
	        } else {
	            temp = new File(path + File.separator + tempList[i]);
	        }
	        if (temp.isFile()) {
	            temp.delete();
	        }
	        if (temp.isDirectory()) {
	        	delAllFiles(path + "/" + tempList[i]); 
	            flag = true;
	        }
	    }
	    return flag;
	}
	public static void copyFolder(String oldPath, String newPath) {
		try {
			new File(newPath).mkdirs();
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; ++i) {
				if(file[i].contains(".svn")){
					continue;
				}
				if (oldPath.endsWith(File.separator))
					temp = new File(oldPath + file[i]);
				else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ File.separator + temp.getName().toString());

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
					copyFolder(oldPath + File.separator + file[i], newPath + File.separator + file[i]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static void getLoaderType( String appId)
			throws IOException {
		String custom_loader_path = workPath + "appLoader"+File.separator+"custom-loader"+File.separator+appId+File.separator;
		String custom_loader_conf = custom_loader_path + "load.conf";
		String custom_loader_ipa = custom_loader_path + "load.apk";
		File custom_loader_conf_file = new File(custom_loader_conf);
		if (custom_loader_conf_file.exists()
				&& new File(custom_loader_ipa).exists()) {
			InputStreamReader read = new InputStreamReader(new FileInputStream(
					custom_loader_conf_file));// 考虑到编码格式
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			StringBuffer conf = new StringBuffer();
			while ((lineTxt = bufferedReader.readLine()) != null) {
				conf.append(lineTxt);
			}
			read.close();
			if(conf.length() == 0){
				pkgName = "com.apicloud.apploader";
				loaderName = "apicloud-loader";
				return;
			}
			String version = "";
			JSONObject json = JSONObject.fromObject(conf.toString());
			if(json.has("version")){
				version = json.getString("version");
			}
			if(json.has("packageName")){
				pkgName = json.getString("packageName");
			}
			if(!version.equals("") && !pkgName.equals("")){
				pendingVersion = version;
				loaderName="custom-loader"+File.separator+appId;
			}
		} else {
			pkgName = "com.apicloud.apploader";
			loaderName = "apicloud-loader";
		}
	}
	private static String getWidgetContent(String configPath){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element content = null;
		Element root = null;
		try {
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.parse(new File(configPath));
			root = xmldoc.getDocumentElement();
			content = (Element) selectSingleNode("/widget/content", root);
			String src = content.getAttribute("src");
			//System.out.println(appId);
			return src;
			// theBook.setTextContent(replaceValue);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private static String getAppId(String configPath) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element widget = null;
		Element root = null;
		try {
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder db = factory.newDocumentBuilder();
			Document xmldoc = db.parse(new File(configPath));
			root = xmldoc.getDocumentElement();
			widget = (Element) selectSingleNode("/widget", root);
			String appId = widget.getAttribute("id");
			//System.out.println(appId);
			return appId;
			// theBook.setTextContent(replaceValue);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	private static boolean pushStartInfo(String serialNumber,String appId) throws IOException{
		String  desPath="/sdcard/UZMap/A6965066952332/";
		String srcPath=workPath +"appLoader"+File.separator+"startInfo.txt";
		FileWriter fw = new FileWriter(srcPath, false);
		PrintWriter pw = new PrintWriter(fw);
		pw.print(appId);
		pw.flush();
		fw.flush();
		pw.close();
		fw.close();
		String pushCmd=adbPath+" -s "+serialNumber+" push "+srcPath+" "+desPath;
//		System.out.println("pushCmd = "+pushCmd);
		String out  =  (String) runCmd(pushCmd,false);
		
		if(out.contains("error: device not found")){
			return false;
		}
		return true;
	}
	private static boolean compareAppLoaderVer(String deviceVersion,String appLoaderVersion){
		String[] deviceVersionArray=deviceVersion.split("\\.");
		String[] appLoaderVersionArray=appLoaderVersion.split("\\.");
		
		for(int i = 0;i< 3;i++){
			if(Integer.valueOf(appLoaderVersionArray[i]) > Integer.valueOf(deviceVersionArray[i])){
				return true;
			}
		}
		return false;
	}
	private static String getApploaderVersionCmd(String serialNumber) throws IOException{
		String version=null;
		String cmd=adbPath+" -s "+serialNumber+" shell dumpsys package "+pkgName;
		//System.out.println(cmd);
		String out =  (String) runCmd(cmd,false);
		
		Pattern pattern = Pattern.compile("versionName=([0-9]{1,}.[0-9]{1,}.[0-9]{1,})");  
		Matcher matcher = pattern.matcher(out);
//		System.out.println(matcher.);
		boolean result = matcher.find(); 
		
		if(result){
			version = matcher.group(0).split("=")[1];
		}
		//System.out.println(version);
		return version;
	}
	private static boolean uninstallApploaderCmd(String serialNumber) throws IOException{
		String uninstallCmd=adbPath+" -s "+serialNumber+" uninstall "+pkgName;
		
		String out =  (String) runCmd(uninstallCmd,false);
		
		if(!out.contains("Success")){
			return false;
		}
		return true;
	}
	private static boolean installAppLoaderCmd(String serialNumber) throws IOException{
		String appLoader=workPath+"appLoader"+File.separator+loaderName+File.separator+"load.apk";
		String installCmd=adbPath+" -s "+serialNumber+" install "+appLoader;
		
		String out =  (String) runCmd(installCmd,false);
		System.out.println("out  == "+out);
		if(!out.contains("Success")){
			return false;
		}
		return true;
	}
	private static void stopApploaderCmd(String serialNumber){
		String stopCmd=adbPath +" -s "+serialNumber+" shell am force-stop "+pkgName;
		runCmd(stopCmd, false);
	}
	private static boolean startApploaderCmd(String serialNumber) throws IOException{
		String appLoaderPkg=pkgName+"/com.uzmap.pkg.EntranceActivity";
		String startCmd=adbPath +" -s "+serialNumber+" shell am start -W -n "+appLoaderPkg;
		
		String out =  (String) runCmd(startCmd,false);
		
		if(out.contains("error")){
			return false;
		}
		
		return true;
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

	private static Object runCmd(String cmd,boolean isReturnP) {
//		Map<String,BufferedReader> map = new HashMap<String, BufferedReader>();
//		System.out.println("cmd  == "+cmd);
		
		Runtime r = Runtime.getRuntime();

		Process p = null;
		StringBuffer okString = new StringBuffer();
		
		try {
			p = r.exec(cmd);
			if(isReturnP){
				return p;
			}
			
			
			BufferedReader buf = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader errBuf = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			
			String out =null;
			
			while ((out=buf.readLine()) != null) {
//				System.out.println("out  == "+out);
				okString.append(out);
				
			}
		
			while ((out = errBuf.readLine()) != null) {
//				System.out.println(out);
				okString.append(out);
			}
			buf.close();
			errBuf.close();
			buf = null;
			errBuf = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (p != null && !isReturnP){
				p.destroy();
				p = null;
			}
			
				
		}
		return okString.toString();
	}
}
