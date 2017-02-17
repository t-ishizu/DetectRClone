package jp.ac.osaka_u.ist.t_ishizu.DetectRClone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class myFileUtils {
	private static void delete(File f){
		if(f.exists()==false) return;
		if(f.isFile()) f.delete();
		else if(f.isDirectory()){
			File[] files = f.listFiles();
			for(int i=0;i<files.length;i++){
				delete(files[i]);
			}
			f.delete();
		}
	}

	public static void mkdir(File f){
		if(f.exists()){
			if(f.isDirectory()){
				delete(f);
			}
		}
		f.mkdir();
	}

	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try{
			inChannel.transferTo(0, inChannel.size(), outChannel);
		}catch(IOException e){
			throw e;
		}
		finally{
			if(inChannel != null) inChannel.close();
			if(outChannel != null) outChannel.close();
		}
	}

	public static void copyCppFile(String in, String out){
		File inDir = new File(in);
		File outDir = new File(out);
		mkdir(outDir);
		if(inDir.exists()==false)return;
		if(inDir.isDirectory()){
			File[] files = inDir.listFiles();
			for(File f : files){
				detectCppFile(f,outDir);
			}
		}
	}

	public static void detectCppFile(File f, File out){
		if(f.isFile()){
			if(getSuffix(f.getName()).equals("c") || getSuffix(f.getName()).equals("h")){
				try {
					copyFile(f, new File(out.getPath() + "\\" + f.getName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}else if(f.isDirectory()){
			File[] files = f.listFiles();
			for(File file:files){
				detectCppFile(file, out);
			}
		}
	}

	public static String getSuffix(String fileName){
		if(fileName == null){
			return null;
		}
		int point = fileName.lastIndexOf(".");
		if(point!=-1){
			return fileName.substring(point+1);
		}
		return fileName;
	}

}
