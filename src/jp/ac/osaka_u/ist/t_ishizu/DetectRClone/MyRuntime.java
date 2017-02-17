package jp.ac.osaka_u.ist.t_ishizu.DetectRClone;

import java.io.IOException;

public class MyRuntime {
	public static void myexec(String command){
		Runtime runtime = Runtime.getRuntime();
		try{
			Process p = runtime.exec(command);
			p.waitFor();
		}catch(IOException e){
			System.err.print(e.getMessage());
		} catch (InterruptedException e) {
			System.err.print(e.getMessage());
		}
	}


}
