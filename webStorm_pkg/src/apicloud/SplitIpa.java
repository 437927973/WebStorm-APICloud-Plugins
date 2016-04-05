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
import java.io.IOException;
import java.io.InputStreamReader;

import com.dd.plist.PropertyListParser;

public class SplitIpa {
	String toolsPath;
	String sourceIpa;
	String appId;
	String widgetPath;
	String cachePath;
	String curPath;
	String outPath;
	String name;

	public SplitIpa(String toolsPath, String sourceIpa, String appId,
			String widgetPath, String outPath, String appName) {
		this.curPath = new File("").getAbsolutePath();
		this.toolsPath = toolsPath;
		this.sourceIpa = sourceIpa;
		this.appId = appId;
		this.widgetPath = widgetPath;
		this.outPath = outPath;

		this.name = appName;

		cachePath = System.getProperties().getProperty("user.home")
				+ File.separator + "uztools" + File.separator + appId
				+ File.separator + "ios";

		File file = new File(cachePath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public void mySplit() throws Exception {
		String cmdunzip = null;
		String cmdzip = null;
		String cmd_copy = null;
		System.out.println("Start......");
		if (!OS.isMacOS()) {
			cmdunzip = "\"" + toolsPath + "7za.exe\" x -y \"" + sourceIpa
					+ "\" -o\"" + cachePath + "\"";
			cmdzip = "\"" + toolsPath + "7za.exe\" a \"" + cachePath + "\\"
					+ name + ".zip\" \"" + cachePath + "\\Payload\\\"";
			cmd_copy = "xcopy \"" + widgetPath + "\" \"" + cachePath
					+ "\\Payload\\UZApp.app\\widget\\\" /e /y";
		} else {
			cmdunzip = "unzip -q -o " + sourceIpa + " -d " + cachePath;
			cmdzip = "zip -q -r " + cachePath + "/" + name + ".zip  "
					+ cachePath + "/Payload/";
			cmd_copy = "cp -rf " + widgetPath + "/  " + cachePath
					+ "/Payload/UZApp.app/widget/";
			runCmd("rm -rf " + cachePath + "/Payload/");
		}

		runCmd(cmdunzip);
		String f_mani = cachePath + "/Info.xml";
		File manifest = new File(f_mani);
		if (manifest.exists()) {
			manifest.delete();
		}
		manifest.createNewFile();
		System.out.println("Ready to pack......");
		System.out.println("Please wait......");
		try {
			PropertyListParser.convertToXml(new File(cachePath
					+ "/Payload/UZApp.app/Info.plist"), manifest);
			IPAModifyPlist plist = new IPAModifyPlist();
			plist.setPlistFile(f_mani);
//			plist.setCFBundleIdentifier("com.apicloud." + appId);
			plist.setCFBundleDisplayName(name);

			plist.savePlistFile();

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!OS.isMacOS()) {
			FileUtil.deleteDirectory(cachePath
					+ "\\Payload\\UZApp.app\\widget\\");
		} else {
			runCmd("rm -rf " + cachePath + "/Payload/UZApp.app/widget/");
		}

		runCmd(cmd_copy);

		File infoFile = new File(cachePath + File.separator + "Payload"
				+ File.separator + "UZApp.app" + File.separator + "Info.plist");
		if (infoFile.exists()) {
			infoFile.delete();
		}
		try {
			PropertyListParser.convertToBinary(new File(f_mani), infoFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		runCmd(cmdzip);

		File zip = new File(cachePath + File.separator + name + ".zip");
		File ipa = new File(outPath + File.separator + name + ".ipa");
		zip.renameTo(ipa);
		System.out.println("Complete!");
	}

	/**
	 * 执行指令
	 * 
	 * @param cmd
	 */
	public static void runCmd(String cmd) {
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		try {
			Process p = rt.exec(cmd);
			isr = new InputStreamReader(p.getInputStream());
			br = new BufferedReader(isr);
			String msg = null;
			while ((msg = br.readLine()) != null) {
				// System.out.println(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (isr != null) {
					isr.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
