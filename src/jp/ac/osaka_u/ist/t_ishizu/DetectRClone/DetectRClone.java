package jp.ac.osaka_u.ist.t_ishizu.DetectRClone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class DetectRClone {
	public static int commitId_1;
	public static int commitId_2;
	private static String hashPath = "C:\\Users\\t-ishizu\\Documents\\inoue\\Git\\git_sha1.txt";
	private static String targetRepositoryPath = "C:\\Users\\t-ishizu\\Documents\\inoue\\Git\\git";
	private static String dataPath = "C:\\Users\\t-ishizu\\Documents\\inoue\\Git\\git_data";
	private static String ccfinderPath = "C:\\CCFinderX\\ccfx-win32-en\\bin";
	private static ArrayList<String> hashList;
	private static Repository repository;
	public static ArrayList<String> fileList_1;
	public static ArrayList<String> fileList_2;
	public static void main(String[] args)throws Exception{
		if(args.length != 2){
			System.err.print("Augument Error: DetectRClone comitid_1 comitid_2");
			System.exit(-1);
		}
		commitId_1 = Integer.parseInt(args[0]);
		commitId_2 = Integer.parseInt(args[1]);

		createHashList();
		buildRepository(targetRepositoryPath + "\\.git");
		checkout(hashList.get(commitId_1));
		runCCFinderX("\\data1");
		fileList_1 = createFileList(dataPath+"\\data1");
		checkout(hashList.get(commitId_2));
		runCCFinderX("\\data2");
		fileList_2 = createFileList(dataPath+"\\data2");
		execute(hashList.get(commitId_2), hashList.get(commitId_1));

	}

	public static void createHashList(){
		hashList = new ArrayList<String>();
		BufferedReader br = getBufferedReader(hashPath);
		try {
			String str = br.readLine();
			while(str!=null){
				hashList.add(str);
				str = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runCCFinderX(String in){
		myFileUtils.copyCppFile(targetRepositoryPath, dataPath + in);
		MyRuntime.myexec(ccfinderPath + "\\ccfx.exe d cpp -dn " + dataPath + in);
		MyRuntime.myexec(ccfinderPath + "\\ccfx.exe p " + ccfinderPath + "\\a.ccfxd -o clonepair.tsv");
		try {
			myFileUtils.copyFile(new File(ccfinderPath + "\\a.ccfxd"), new File(dataPath + in + "\\a.ccfxd"));
			myFileUtils.copyFile(new File(ccfinderPath + "\\clonepair.tsv"), new File(dataPath + in + "\\clonepair.tsv"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void createCloneMap(String in){
		BufferedReader br = getBufferedReader(in);
		HashMap<Integer,ArrayList<CodeClone>> cloneMap = new HashMap<Integer,ArrayList<CodeClone>>();
		try{
			String str = br.readLine();
			boolean find = false;
			while(str != null){
				if(str.equals("clone_pairs {")){
					find = true;
				}else if(str.equals("}")){
					find = false;
				}else if(find){
					String[] str_split = str.split("[.,\\-\t]",0);
					CodeClone clone1 = new CodeClone();
					clone1.id = Integer.parseInt(str_split[0]);
					clone1.fid = Integer.parseInt(str_split[1]);
					clone1.sid = Integer.parseInt(str_split[2]);
					clone1.eid = Integer.parseInt(str_split[3]);
					CodeClone clone2 = new CodeClone();
					clone2.id = Integer.parseInt(str_split[0]);
					clone2.fid = Integer.parseInt(str_split[4]);
					clone2.sid = Integer.parseInt(str_split[5]);
					clone2.eid = Integer.parseInt(str_split[6]);

					int id1 = -1;
					int id2 = -1;

					if(cloneMap.containsKey(clone1.fid)){
						id1 = getIndex(cloneMap.get(clone1.fid), clone1);
					}else{
						cloneMap.put(clone1.fid, new ArrayList<CodeClone>());
					}

					if(cloneMap.containsKey(clone2.fid)){
						id1 = getIndex(cloneMap.get(clone2.fid), clone2);
					}else{
						cloneMap.put(clone2.fid, new ArrayList<CodeClone>());
					}

					if(id1 == -1 && id2 == -1){
						cloneMap.get(clone1.fid).add(clone1);
						cloneMap.get(clone2.fid).add(clone2);
						CloneSet cloneset = new CloneSet();
						clone1.cloneset = cloneset;
						clone2.cloneset = cloneset;
						cloneset.id = clone1.id;
						cloneset.cloneList.add(clone1);
						cloneset.cloneList.add(clone2);
					}else if(id1 != -1 && id2 == -1){
						cloneMap.get(clone2.fid).add(clone2);
						CloneSet cloneset = cloneMap.get(clone1.fid).get(id1).cloneset;
						clone2.cloneset = cloneset;
						cloneset.cloneList.add(clone2);
					}else if(id1 == -1 && id2 != -1){
						cloneMap.get(clone1.fid).add(clone1);
						CloneSet cloneset = cloneMap.get(clone2.fid).get(id2).cloneset;
						clone1.cloneset = cloneset;
						cloneset.cloneList.add(clone1);
					}
				}
				str=br.readLine();
			}
		}catch(IOException e){
			System.err.println(e.getMessage());
		}
	}

	public static int getIndex(ArrayList<CodeClone>list, CodeClone clone){
		for(int i=0;i<list.size();i++){
			CodeClone c = list.get(i);
			if(clone.sid == c.sid && clone.eid == c.eid){
				return i;
			}
		}
		return -1;
	}

	public static void buildRepository(String path) throws IOException{
		repository = new FileRepositoryBuilder().setGitDir(new File(path))
				.readEnvironment().findGitDir().build();
	}

	public static void checkout(String name){
		Git git = new Git(repository);
		try {
			git.checkout().setName(name).call();
		} catch (RefAlreadyExistsException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (RefNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InvalidRefNameException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (CheckoutConflictException e) {
			//git.reset().call();
			/*
			 * http://qiita.com/konweb/items/061475d6376db957b3c4
			 * git status
			 * Change not staged for commit: git checkout .(file name)
			 * Untracked files: git clean -f
			 *
			 */
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	public static final ArrayList<String> createFileList(String path){
		BufferedReader br = getBufferedReader(new File(path + "\\clonepair.tsv"));
		ArrayList<String> fileList = new ArrayList<String>();
		try{
			String str = br.readLine();
			boolean canRead = false;
			while(str!=null){
				if(str.equals("source_files {")){
					canRead = true;
				}else if(str.equals("}")){
					canRead = false;
				}else if(canRead){
					String[] str_split = str.split("[\t]+",0);
					fileList.add(str_split[1]);
				}
				str=br.readLine();
			}
		}catch(IOException e){
			System.out.println(e.getMessage());
			System.exit(0);
		}
		return fileList;
	}

	private static RawText readText(AbbreviatedObjectId blobId,
	        ObjectReader reader) throws IOException {
	    	ObjectLoader oldLoader = reader.open(blobId.toObjectId(),
	            Constants.OBJ_BLOB);
	    return new RawText(oldLoader.getCachedBytes());
	}
	public static void execute(String From, String To)throws Exception{
		DiffFormatter diffFormatter = new DiffFormatter(System.out);
		diffFormatter.setRepository(repository);
		RevWalk walk = new RevWalk(repository);
		RevCommit fromCommit = walk.parseCommit(repository.resolve(From));
		RevCommit toCommit = walk.parseCommit(repository.resolve(To));
		RevTree fromTree = fromCommit.getTree();
		RevTree toTree = toCommit.getTree();
		List<DiffEntry> list = diffFormatter.scan(fromTree, toTree);
		ObjectReader reader = repository.newObjectReader();
		DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(repository
                .getConfig().getEnum(ConfigConstants.CONFIG_DIFF_SECTION,
                        null, ConfigConstants.CONFIG_KEY_ALGORITHM,
                        SupportedAlgorithm.HISTOGRAM));
		list.forEach((diffEntry) -> {
			try {
				RawText oldText = readText(diffEntry.getOldId(), reader);
				RawText newText = readText(diffEntry.getNewId(), reader);
                EditList editList = diffAlgorithm.diff(
                        RawTextComparator.DEFAULT, oldText, newText);
                for (Edit edit : editList) {
                    System.out.println(diffEntry.getNewPath()
                            +" " + diffEntry.getChangeType()
                            + "\n old -" + edit.getBeginA() +","+ edit.getLengthA()+ "\n"
                            + oldText.getString(edit.getBeginA(),
                                    edit.getEndA(), false)
                            + "\n new +" + edit.getBeginB() +","+ edit.getLengthB()+ "\n"
                            + newText.getString(edit.getBeginB(),
                                    edit.getEndB(), false));
                }
			} catch (Exception e) {

				e.printStackTrace();
			}
		});
		walk.dispose();
	}

	public static final BufferedReader getBufferedReader(String fileName){
		File file = new File(fileName);
		if(!file.exists()){
			System.err.println("!Error:There are no file " + file.getPath());
			System.exit(0);
		}
		try {
			return new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final BufferedReader getBufferedReader(File file){
		if(!file.exists()){
			System.err.println("!Error:There are no file " + file.getPath());
			System.exit(0);
		}
		try {
			return new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
