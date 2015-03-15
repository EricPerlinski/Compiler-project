import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;


public class Tree2img{

	static private final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

	private String treeStr;
	private String output;
	private BufferedWriter outputFile;

	public Tree2img(String t, String o){
		treeStr=t;
		output=o;
	}

	public void parse() throws IOException{
		String start = "digraph unix { \n\n";
		String end = "}";
		FileWriter fw =null;
		try{
			fw = new FileWriter(output, false);			
			this.outputFile = new BufferedWriter(fw);
			outputFile.write(start);
			this.parseTree();
			outputFile.write(end);
			outputFile.flush();
			outputFile.close();
			System.out.println("fichier "+output+" créé");
			fw.close();
			outputFile.close();
		}
		catch(IOException ioe){
			System.out.print("Erreur : ");
			ioe.printStackTrace();
		}finally{
			if(fw!=null){
				fw.close();
			}
			if(this.outputFile!=null){
				this.outputFile.close();
			}
		}
	}


	private void parseTree() throws IOException{
		Stack<String> stack = new Stack<String>();
		stack.addElement("ROOT");
		Stack<String> stackId = new Stack<String>();
		stackId.addElement("ROOT");
		String treeTab[] = treeStr.split(String.format(WITH_DELIMITER, "\\(|\\)|\\s"));
		boolean stackNext=false;
		StringBuffer content = new StringBuffer();
		int rand = 0;
		for(int i=0;i<treeTab.length;i++){
			rand = (int)(Math.random()*1000);
			if(treeTab[i].trim().length()>0){
				//System.out.println(i+" : "+treeTab[i]);
				switch(treeTab[i].charAt(0)){
				case '(':
					stackNext=true;
					break;
				case ')':
					stack.pop();
					stackId.pop();
					break;
				default:
					if(stackNext){
						stackNext=false;
						content.append("\t"+i+" [label=\""+treeTab[i]+"\"]");
						content.append("\n\t"+stackId.peek()+" -> "+i+" ;\n");
						stack.addElement(treeTab[i]);
						stackId.addElement(""+i);
					}else{
						content.append("\t"+i+" [label=\""+treeTab[i]+"\"]");
						content.append("\t"+stackId.peek()+" -> "+i+" ;\n");
					}


				}
			}
		}
		outputFile.write(content.toString());
	}





	public static void main(String[] args) {
		String inputStr = null;
		String outputFile="output/output.dot";
		switch(args.length){
		case 0:
			System.out.println("USAGE : java Tree2img file.tree [outputFile.dot]");
			return;
		case 1:
			inputStr = "input/"+args[0];
			outputFile="output/"+args[0]+".dot";
			break;
		default:
			System.out.println("USAGE : java Tree2img file.tree [outputFile.dot]");
			return;
		}
		
		
		
		FileReader in = null;
		BufferedReader input=null;
		StringBuffer treeInput = new StringBuffer();
		try {
			in = new FileReader(inputStr);
			input = new BufferedReader(in);
			String line;
			while ((line=input.readLine())!=null) {
				treeInput.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		Tree2img tree = new Tree2img(treeInput.toString(),outputFile);
		try {
			tree.parse();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}