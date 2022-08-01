/*
javac -Xlint VCF_liftOver.java
java VCF_liftOver path_originalFile path_paftools path_pafFile

Author: Hui Chen
Date: Aug.1.2022
Version: 1.3
*/

import java.util.*;
import java.io.*;


public class VCF_liftOver {

	public static void main(String[] args) throws IOException{

		//get file name and path from input
    	String originalFile = args[0];
    	String path_paftools = args[1];
    	String path_pafFile = args[2];

    	//get path of original file
		File fo = new File(originalFile);
		String parentPath = fo.getAbsoluteFile().getParent();

		//delete result files if existed
    	File found = new File(parentPath+"/Found.vcf");
    	if(found.exists()){
    		found.delete();
    	}
    	File notFound = new File(parentPath+"/NotFound.vcf");
    	if(notFound.exists()){
    		notFound.delete();
    	}    	
		
		//create a hashmap to pair the names "NC_000001.11" to "chr1".
		HashMap<String, String> chrName = new HashMap<>();
		chrName.put("NC_000001.11", "chr1");
		chrName.put("NC_000002.12", "chr2");
		chrName.put("NC_000003.12", "chr3");
		chrName.put("NC_000004.12", "chr4");
		chrName.put("NC_000005.10", "chr5");
		chrName.put("NC_000006.12", "chr6");
		chrName.put("NC_000007.14", "chr7");
		chrName.put("NC_000008.11", "chr8");
		chrName.put("NC_000009.12", "chr9");
		chrName.put("NC_000010.11", "chr10");
		chrName.put("NC_000011.10", "chr11");
		chrName.put("NC_000012.12", "chr12");
		chrName.put("NC_000013.11", "chr13");
		chrName.put("NC_000014.9", "chr14");
		chrName.put("NC_000015.10", "chr15");
		chrName.put("NC_000016.10", "chr16");
		chrName.put("NC_000017.11", "chr17");
		chrName.put("NC_000018.10", "chr18");
		chrName.put("NC_000019.10", "chr19");
		chrName.put("NC_000020.11", "chr20");
		chrName.put("NC_000021.9", "chr21");
		chrName.put("NC_000022.11", "chr22");
		chrName.put("NC_000023.11", "chrX");
		chrName.put("NC_000024.10", "chrY");


    	//rewrite the first column to chr, second column, second column+1
    	File ori_file = new File(originalFile);
    	Scanner ori_scan = new Scanner(ori_file);
    	while (ori_scan.hasNextLine()) {
    		String[] data_ori = ori_scan.nextLine().split("\\s+",3);
    		String line_chr = chrName.get(data_ori[0]) +"\t"+data_ori[1]+"\t"+String.valueOf(Integer.parseInt(data_ori[1])+1);
 			writeToFile("tmp_3column.vcf", line_chr);

    	}

    	//liftOver to get .bed
        try {
        	String cmd = path_paftools+" liftover "+path_pafFile+" tmp_3column.vcf";
            InputStream inputStream
                = Runtime.getRuntime().exec(cmd).getInputStream();
 			Scanner s = new Scanner(inputStream);
 			while(s.hasNext()){
 				String line = s.nextLine();
 				writeToFile("tmp.bed", line);			
 			}

        }
        catch (IOException e) {
            e.printStackTrace();
        }    	


    	//create a hashmap to pair locations in originalFile and bedFile.
    	HashMap<String, String> trans = new HashMap<>();

    	File file_bed = new File("tmp.bed");
    	Scanner input_bed = new Scanner(file_bed);    	
    	while (input_bed.hasNextLine()){
    		String[] data_bed = input_bed.nextLine().split("\\s+");
    		trans.put(data_bed[3],data_bed[0]+" "+data_bed[1]);
    	}
    	input_bed.close();


    	//combines from vcf and bed
    	try{
	    	File file_vcf = new File(originalFile);
	    	Scanner input_vcf = new Scanner(file_vcf);
	    	
	    	while (input_vcf.hasNextLine()){
	    		String line_vcf = input_vcf.nextLine();
	    		String[] data_vcf = line_vcf.split("\\s+");	  
	    		String[] data_vcf3 = line_vcf.split("\\s+",3);	
	    		String key_vcf = chrName.get(data_vcf[0])+"_"+data_vcf[1]+"_"+String.valueOf(Integer.parseInt(data_vcf[1]) + 1);

				if (trans.containsKey(key_vcf)){
					String[] toWrite = trans.get(key_vcf).split(" ");
					String newline = toWrite[0]+"\t"+toWrite[1]+"\t"+data_vcf3[2];	
					writeToFile(parentPath+"/Found.vcf", newline);
				}
				else {	
					writeToFile(parentPath+"/NotFound.vcf", line_vcf);
				}
	    	}
			if(input_vcf != null)
				{input_vcf.close();}
	    		    	
    	}
    	catch(IOException e){
    		e.printStackTrace();  
    	}

		File toDelete1 = new File("tmp_3column.vcf");
		File toDelete2 = new File("tmp.bed");
		if (toDelete1.delete()){
			System.out.println("tmp1 Deleted Successfully.");
		}
		if (toDelete2.delete()){
			System.out.println("tmp2 Deleted Successfully.");
		}		

    	System.out.println("Done!");


	}
	

	public static void writeToFile(String fileName, String toWrite)  {
		try{
			FileWriter fw = new FileWriter(fileName,true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(toWrite);
			pw.close();
			fw.close();

		}
		catch (IOException e) {
            e.printStackTrace();
        }  
	}

}