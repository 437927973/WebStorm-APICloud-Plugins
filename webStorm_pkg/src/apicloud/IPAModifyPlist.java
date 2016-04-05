/**
 * WebStorm APICloud plugin
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package apicloud;

import java.io.File;
import java.io.IOException;

import com.dd.plist.NSDictionary;
import com.dd.plist.NSNumber;
import com.dd.plist.PropertyListParser;

public class IPAModifyPlist {
	private static String CFBundleVersion = "CFBundleVersion";
	private static String CFBundleDisplayName = "CFBundleDisplayName";
	private static String CFBundleIdentifier = "CFBundleIdentifier";
	private static String UIStatusBarHidden = "UIStatusBarHidden";
	NSDictionary rootDict;
	NSDictionary jsnameRootDict;
	String plistFile;

	public IPAModifyPlist() {
		this.rootDict = new NSDictionary();
		this.jsnameRootDict = new NSDictionary();
	}

	public void setPlistFile(String plistFile) {
		this.plistFile = plistFile;

		readPlist();
	}

	private void readPlist() {
		try {
			File file = new File(this.plistFile);
			this.rootDict = ((NSDictionary) PropertyListParser.parse(file));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void savePlistFile() {
		File plist = new File(this.plistFile);
		try {
			PropertyListParser.saveAsXML(this.rootDict, plist);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setCFBundleVersion(String version) {
		this.rootDict.put(CFBundleVersion, version);
	}

	public void setCFBundleDisplayName(String displayName) {
		this.rootDict.put(CFBundleDisplayName, displayName);
	}

	public void setCFBundleIdentifier(String ids) {
		this.rootDict.put(CFBundleIdentifier, ids);
	}

	public void setUIStatusBarHidden(boolean status) {
		NSNumber nsstatus = new NSNumber(status);
		this.rootDict.put(UIStatusBarHidden, nsstatus);
	}


}
