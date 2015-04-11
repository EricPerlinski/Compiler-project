package dot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import org.antlr.runtime.tree.CommonTree;



public class Tree2img{


	private CommonTree tree;
	private String output;
	private BufferedWriter outputFile;

	public Tree2img(CommonTree t, String o){
		tree=t;
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
			System.out.println("fichier TREE "+output+" créé");
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
		Stack<Integer> stackId = new Stack<Integer>();
		stackId.addElement(0);
		StringBuffer content = new StringBuffer();
		parseTreeRec(tree,stackId,stack,0,content);
		outputFile.write(content.toString());
	}

	private int parseTreeRec(CommonTree t, Stack<Integer> sid, Stack<String> s, int index, StringBuffer content) {
		content.append("\t"+index+" [label=\""+  (t.getToken().getText()).replaceAll("('|\")", "\\\\$1")   +"\"]");
		if(index>0){
			content.append("\n\t"+sid.peek()+" -> "+index+" ;\n");
		}
		sid.push(index);
		s.push(t.getToken().getText());
		for(int i=0;i<t.getChildCount();i++){
			index = parseTreeRec((CommonTree)(t.getChild(i)),sid,s,index+1,content);
		}
		s.pop();
		sid.pop();
		return index;
	}





	public static void run(CommonTree treeInput,String name_output) {
		
		if(name_output==null){
			name_output="tree/input/output.dot";
		}else{
			name_output="tree/input/"+name_output+".dot";
		}
			
		Tree2img tree2img = new Tree2img(treeInput,name_output);
		try {
			tree2img.parse();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}