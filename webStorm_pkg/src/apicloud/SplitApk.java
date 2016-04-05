/**
 * WebStorm APICloud plugin
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package apicloud;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplitApk {
	String toolsPath;
	String sourceApk;
	String keyFile;
	String keyPasswd;
	String appId;
	String cachePath;
	String outPath;
	String widgetPath;
	String name;

	public SplitApk(String sourceApk, String keyFile, String keyPasswd,
			String toolsPath, String appId, String outPath, String widgetPath,
			String appName) {
		this.sourceApk = sourceApk;
		this.keyFile = keyFile;
		this.keyPasswd = keyPasswd;
		this.toolsPath = toolsPath;
		this.appId = appId;
		this.outPath = outPath;
		this.widgetPath = widgetPath;
		this.name = appName;
		this.cachePath = System.getProperties().getProperty("user.home")
				+ File.separator + "uztools" + File.separator + appId
				+ File.separator + "android" + File.separator;
		File file = new File(cachePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public void mySplit() throws Exception {
		modifyXudao();// 解包 - 打包 - 签名
	}

	/**
	 * apktool解压apk
	 * 
	 * @throws IOException
	 * 
	 * @throws Exception
	 */
	private void modifyXudao() throws Exception {

		System.out.println("Start......");
		String cmdUnpack = "java -jar " + toolsPath + "apktool.jar d -f -s "
				+ sourceApk + " " + cachePath + "/";
		UzTools.runCmd(cmdUnpack);

		String f_mani = cachePath + "AndroidManifest.xml";

		Xml.updateAndroidManifestXML(f_mani, appId, f_mani);
		Map<String, String> map = new HashMap<String, String>();
		Xml.getFeatureApiKey(widgetPath + "/config.xml", map);
		List<String> metadataList = new ArrayList<String>();
		if (map.containsKey("baiduMap")) {
			String metadata = "<meta-data android:name=\"com.baidu.lbsapi.API_KEY\" android:value=\""
					+ map.get("baiduMap") + "\" />";
			metadataList.add(metadata);
		}
		StringBuffer destContent = new StringBuffer();
		StringBuffer newContent = new StringBuffer();
		String label = "</application>";
		int index_s = 0;
		for (String metadata : metadataList) {
			// System.out.println("metadata = "+metadata);
			newContent.append("\n" + metadata);
		}
		destContent = UzTools.readFile(f_mani);

		if (newContent.length() > 0) {

			index_s = destContent.indexOf(label);
			if (index_s != -1) {
				destContent.insert(index_s - 1, newContent);
			}

		}
		UzTools.writeFile(f_mani, destContent);

		String strings = cachePath + File.separator + "res" + File.separator
				+ "values" + File.separator + "strings.xml";

		String cache_widgetPath = cachePath + File.separator + "assets"
				+ File.separator + "widget" + File.separator;

		Xml.updateStringXML(strings, "/resources/string[@name='app_name']",
				name, strings);

		FileUtil.deleteDirectory(cachePath + "assets/widget/");

		FileUtil.deleteDirectory(cachePath + "assets/widget/");

		FileUtil.copyFolder(widgetPath, cache_widgetPath);

		System.out.println("Ready to pack......");
		System.out.println("Please wait......");

		if (!OS.isMacOS()) {
			String unsignApk = cachePath + "_un.apk";
			String signApk = "\"" + cachePath + "signApk.apk\"";
			String cmdPack = String.format(toolsPath
					+ "packageApk.bat  %s %s %s %s", toolsPath + "apktool.jar",
					toolsPath + "aapt.exe", cachePath, unsignApk);

			String cmd = "&&java -classpath \"" + toolsPath
					+ "tools.jar\" sun.security.tools.JarSigner -keystore \""
					+ keyFile + "\" -storepass " + keyPasswd + " -signedjar "
					+ signApk + " " + unsignApk + " uzmap.keystore";
			String cmd_android = "\"" + toolsPath + "zipalign.exe\"  -v 4 "
					+ signApk + " \"" + outPath + name + ".apk\"";
			UzTools.runCmd(cmdPack + cmd + "&&" + cmd_android);

			System.out.println("Complete!");
		} else {
			String unsignApk = cachePath + "_un.apk";
			String signApk = cachePath + "signApk.apk";
			String cmdPack = String.format("cd " + toolsPath
					+ ";sh apktool b --aapt %s %s %s", toolsPath + "aapt",
					cachePath + "/", unsignApk);
			String cmd = ";java -classpath \"" + toolsPath
					+ "tools.jar\" sun.security.tools.JarSigner -keystore \""
					+ keyFile + "\" -storepass " + keyPasswd + " -signedjar "
					+ signApk + " " + unsignApk + " uzmap.keystore";

			String cmd_android = ";" + toolsPath + "zipalign  -v 4 " + signApk
					+ " " + outPath + name + ".apk";
			String[] cmds = { "/bin/sh", "-c", cmdPack + cmd + cmd_android };
			UzTools.runCmd(cmds);

			System.out.println("Complete!");
		}
	}

	public void updateFile(String file, String file_back, String target,
			String replacement) throws Exception {

		FileUtil.copyFile(file, file_back);

		BufferedReader br = null;
		OutputStreamWriter osw = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(
					file_back), "UTF-8"));
			String line = null;
			StringBuffer sb = new StringBuffer();
			while ((line = br.readLine()) != null) {
				if (line.contains(target)) {
					line = line.replace(target, replacement);
				}
				sb.append(line + "\n");
			}

			osw = new OutputStreamWriter(new FileOutputStream(file, false),
					"UTF-8");
			osw.write(sb.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (osw != null) {
					osw.close();
				}
				File back_file = new File(file_back);
				if (back_file.exists()) {
					back_file.delete();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}