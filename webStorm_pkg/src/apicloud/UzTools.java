/**
 * WebStorm APICloud plugin
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package apicloud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;

public class UzTools {
	// uz 打包工具的缓存目录
	public static String UZ_CACHE_PATH = System.getProperties().getProperty(
			"user.home")
			+ File.separator + "uztools" + File.separator;

	public static void main(String[] args) throws Exception {
		String packagePath = ""
				+ File.separator;
		// String outPath = pkgPath;
		String widgetPath = "";
		String outPath = widgetPath;
		String platform = null;
		if (args.length > 0) {
			packagePath = args[0] + File.separator;
		}
		if (args.length > 1) {
			widgetPath = args[1] + File.separator;
		}
		if (args.length > 2) {
			outPath = args[2] + File.separator;
		}
		if (args.length > 3) {
			platform = args[3];
		}
		if (isEmpty(packagePath)) {
			System.out.println("packagePath is null");
			return;
		}
		if (isEmpty(widgetPath)) {
			System.out.println("widgetPath is null");
			return;
		}
		String configPath = widgetPath + "config.xml";
		if (isEmpty(configPath)) {
			System.out.println("configPath is null");
			return;
		}

		Map<String, String> map = Xml.getWidgetInfo(configPath);
		String appId = null;
		String appName = null;
		if (map.containsKey("appId")) {
			appId = map.get("appId");
		}
		if (map.containsKey("appName")) {
			appName = map.get("appName");
		}
		if (isEmpty(appId)) {
			System.out.println("appId is null");
			return;
		}
		if (isEmpty(appName)) {
			System.out.println("appName is null");
			return;
		}

		int type = 1;
		if ("android".equals(platform)) {
			type = 1;
		} else if ("ios".equals(platform)) {
			type = 2;
		}
		makePackage(type, packagePath, outPath, widgetPath, appId, appName);
	}

	private static boolean isEmpty(String value) {
		return (("".equals(value)) || (value == null) || "null".equals(value));
	}

	public static void makePackage(int type, String installPath,
			String outPath, String widgetPath, String appId, String appName)
			throws Exception {
		String toolsPath = installPath + "tools" + File.separator;
		if (OS.isMacOS()) {
			String cmd = "cd " + toolsPath + "; chmod -R +x ./";
			runCmd(cmd);
		}
		String baseApkPath = installPath + "appLoader" + File.separator
				+ "apicloud-loader" + File.separator + "load.apk";
		String keystorePath = toolsPath + "uzmap.keystore";
		String keystorePathPass = "123456";
		String baseIpaPath = installPath + File.separator + "appLoader"
				+ File.separator + "apicloud-loader" + File.separator
				+ "load.ipa";
		File outPathFile = new File(outPath);
		if (!outPathFile.exists()) {
			outPathFile.mkdirs();
		}
		if (type == 1) {
			File outFile = new File(outPath + appName + ".apk");
			if (outFile.exists()) {
				outFile.delete();
			}
			SplitApk splitApk = new SplitApk(baseApkPath, keystorePath,
					keystorePathPass, toolsPath, appId, outPath, widgetPath,
					appName);
			splitApk.mySplit();
		} else if (type == 2) {
			File outFile = new File(outPath + appName + ".ipa");
			if (outFile.exists()) {
				outFile.delete();
			}
			SplitIpa splitIpa = new SplitIpa(toolsPath, baseIpaPath, appId,
					widgetPath, outPath, appName);
			splitIpa.mySplit();
		} else if (type == 3) {
			File outApkFile = new File(outPath + appName + ".apk");
			if (outApkFile.exists()) {
				outApkFile.delete();
			}
			SplitApk splitApk = new SplitApk(baseApkPath, keystorePath,
					keystorePathPass, toolsPath, appId, outPath, widgetPath,
					appName);
			splitApk.mySplit();
			File outIpaFile = new File(outPath + appName + ".ipa");
			if (outIpaFile.exists()) {
				outIpaFile.delete();
			}
			SplitIpa splitIpa = new SplitIpa(toolsPath, baseIpaPath, appId,
					widgetPath, outPath, appName);
			splitIpa.mySplit();
		}
		if (!OS.isMacOS()) {
			FileUtil.delAllFiles(UZ_CACHE_PATH);
		} else {
			String cmd = "rm -rf " + UZ_CACHE_PATH;
			runCmd(cmd);
		}

	}

	/**
	 * 执行指令
	 * 
	 * @param cmd
	 * @throws IOException
	 */
	public static void runCmd(String cmd) throws Exception {

		// System.out.println("cmd = " + cmd);
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		InputStreamReader esr = null;
		Process p = rt.exec(cmd);
		isr = new InputStreamReader(p.getInputStream(), "gbk");
		br = new BufferedReader(isr);
		String msg = null;
		while ((msg = br.readLine()) != null) {
			// System.out.println(msg);
		}

		esr = new InputStreamReader(p.getErrorStream(), "gbk");
		br = new BufferedReader(esr);
		while ((msg = br.readLine()) != null) {
			// System.out.println(msg);
		}
		if (esr != null) {
			esr.close();
		}
		closeResource(br, isr);
	}

	public static void runCmd(String[] cmd) throws Exception {
		// System.out.println("cmd = " + cmd[2]);
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		Process p = rt.exec(cmd);
		isr = new InputStreamReader(p.getInputStream(), "gbk");
		br = new BufferedReader(isr);
		String msg = null;
		while ((msg = br.readLine()) != null) {
			// System.out.println(msg);
		}
		closeResource(br, isr);
	}

	private static void closeResource(BufferedReader br, InputStreamReader isr)
			throws IOException {
		if (isr != null) {
			isr.close();
		}
		if (br != null) {
			br.close();
		}
	}

	public static StringBuffer readFile(String file) {
		StringBuffer buffer = new StringBuffer();
		String encoding = "UTF-8";

		File destf = new File(file);
		try {
			if ((destf.isFile()) && (destf.exists())) {
				InputStreamReader read = new InputStreamReader(
						new FileInputStream(destf), encoding);

				BufferedReader bufferedReader = new BufferedReader(read);
				String content;
				while ((content = bufferedReader.readLine()) != null) {

					buffer.append(content);
					buffer.append("\n");
				}
				bufferedReader.close();
				read.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return buffer;
	}

	public static void writeFile(String file, StringBuffer buffer) {
		String encoding = "UTF-8";
		try {
			OutputStreamWriter fw = new OutputStreamWriter(
					new FileOutputStream(file), encoding);

			BufferedWriter writer = new BufferedWriter(fw);
			writer.write(buffer.toString());
			writer.close();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
