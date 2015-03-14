import java.io.FileInputStream;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;

public class Test {
public static void main(String[] args) throws Exception {
		FileInputStream stream=new FileInputStream("test/correct/test-sujet.plic");
        ANTLRInputStream input = new ANTLRInputStream(stream);
        PlicLexer lexer = new PlicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PlicParser parser = new PlicParser(tokens);
        PlicParser.root_return r = parser.root();
        CommonTree t = (CommonTree)r.getTree();
        System.out.println(t.toStringTree());
        for(int i = 0; i< t.getChildCount();i++){
        	System.out.println(t.getChild(i).toString());
        	for (int j = 0; j<t.getChild(i).getChildCount();j++){
        		System.out.println(t.getChild(i).getChild(j).toString());
        	}
        }
    }
}
