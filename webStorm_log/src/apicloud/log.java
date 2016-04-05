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
import java.util.ArrayList;
import java.util.List;

public class log {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String workPath = "";
		if(args.length > 0){
			workPath = args[0]+File.separator;
		}
		
		String[] parameters;
		String adbPath ;
		if (!OS.isMacOS()) {
			adbPath =workPath  + "tools"+File.separator+"adb.exe";
		} else {
			adbPath =workPath  + "tools"+File.separator+"adb";			
		}
		parameters = new String[] {adbPath,"logcat","-v","time", "-s", "app3c"};
		try {
			if(!getDevices(adbPath)){
				System.out.println("Not found Connected device");
				return;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
			return;
		}
		if(!new File(adbPath).exists()){
			System.out.println("ADB does not exist");
			return;
		}
		runCmd(parameters,true);
	}
	public static Process runCmd(String[] cmd,boolean isLog) {
		Runtime rt = Runtime.getRuntime();
		BufferedReader br = null;
		InputStreamReader isr = null;
		try {
			Process p = rt.exec(cmd);
			if(!isLog){
				return p;
			}
			isr = new InputStreamReader(p.getInputStream());
			br = new BufferedReader(isr);
			String msg = null;
			System.out.println("Log has started");
			while ((msg = br.readLine()) != null) {

			System.out.println(msg);
				
			}
			System.out.println("Not found Connected device");
			
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
		return null;
	}
	private static boolean getDevices(String adbPath) throws IOException {
		List<String> devices = new ArrayList<String>();
		
		String[] parameters = new String[] {adbPath,"devices"};
		
		Process p  =(Process) runCmd(parameters,false);
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
		if(devices.size() > 0){
			return true;
		}else{
			return false;
		}
		
	}

}
