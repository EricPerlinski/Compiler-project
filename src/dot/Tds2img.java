package dot;

import java.io.*; 

public class Tds2img{

    public static void dotWrite(String src, String name){
    FileWriter fw =null;
    BufferedWriter outputFile=null;
    try{
        String name_output=null;
        if(name==null){
            name_output="tds/input/output.dot";
        }else{
            name_output="tds/input/"+name+".dot";
        }

        fw = new FileWriter(name_output, false);         
        outputFile = new BufferedWriter(fw);
        outputFile.write(src);
        outputFile.flush();
        outputFile.close();
        System.out.println("fichier TDS "+name_output+" créé");
        fw.close();
        outputFile.close();
    }
    catch(Exception ioe){
        System.out.print("Erreur : ");
        ioe.printStackTrace();
    }
}
}