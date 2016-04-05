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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileUtil {
	
	public static File buildFile(String fileName, boolean isDirectory) {
        File target = new File(fileName);
        if (isDirectory) {
            target.mkdirs();
        } else {
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
                target = new File(target.getAbsolutePath());
            }
        }
        return target;
    }
	
	public static int compare(String s1, String s2){
        if( s1 == null && s2 == null )
            return 0;
        else if( s1 == null )
            return -1;
        else if( s2 == null )
            return 1;

        String[]
            arr1 = s1.split("[^a-zA-Z0-9]+"),
            arr2 = s2.split("[^a-zA-Z0-9]+")
        ;

        int i1, i2, i3;

        for(int ii = 0, max = Math.min(arr1.length, arr2.length); ii <= max; ii++){
            if( ii == arr1.length )
                return ii == arr2.length ? 0 : -1;
            else if( ii == arr2.length )
                return 1;

            try{
                i1 = Integer.parseInt(arr1[ii]);
            }
            catch (Exception x){
                i1 = Integer.MAX_VALUE;
            }

            try{
                i2 = Integer.parseInt(arr2[ii]);
            }
            catch (Exception x){
                i2 = Integer.MAX_VALUE;
            }

            if( i1 != i2 ){
                return i1 - i2;
            }

            i3 = arr1[ii].compareTo(arr2[ii]);

            if( i3 != 0 )
                return i3;
        }

        return 0;
    }
	
	public static String readVersion(String filePath) {
		String version = "";
        try {
            File file=new File(filePath);
            if(file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(
                new FileInputStream(file), "UTF-8"); 
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while((lineTxt = bufferedReader.readLine()) != null) {
                    version += lineTxt;
                }
                read.close();
        	}
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
        return version;
    }
     
	public static boolean deleteDirectory(String dir) {
		if (!dir.endsWith(File.separator)) {
			dir = dir + File.separator;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		File[] files = dirFile.listFiles();
		for(File f:files) {
			if (f.isFile()) {
				flag = deleteFile(f.getAbsolutePath());
				if (!flag) {
					break;
				}
			} else {
				flag = deleteDirectory(f.getAbsolutePath());
				if (!flag) {
					break;
				}
			}
		}

		if (!flag) {
			return false;
		}

		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
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

	public static boolean deleteFile(String fileName) {
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			file.delete();
			return true;
		} else {
			return false;
		}
	}
	public static void copyFile(String sourcefile, String targetFile)
			throws IOException {
		File sourceFile = new File(sourcefile);
		if(sourceFile.exists()) {
			copyFile(sourceFile, new File(targetFile));
		}
	}
	
	public static void copyFile(File sourcefile, File targetFile)
			throws IOException {
		OutputStream out;
		InputStream in;
		
		in = new FileInputStream(sourcefile);
		out = new FileOutputStream(targetFile, true); //append
		
		byte[] b = new byte[1024]; //block read to improve performance
		int temp = 0;
		while ((temp = in.read()) != -1) {
			out.write(b, 0, temp);
		}
		in.close();
		out.close();
	}
	public static void copyFolder(String oldPath, String newPath) {
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

}