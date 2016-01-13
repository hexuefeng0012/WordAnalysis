package com.hxf.tfidf;

import java.io.*;
import java.util.*;

import com.hxf.nlpir.Nlpir;

/**
 * tf-idf算法，计算文料中的词的权重，可以用来热词发现
 * @author HXF
 *
 */
public class tfidf {

	// the list of file
    private static ArrayList<String> FileList = new ArrayList<String>(); 

    /**
     * Get all file term frequency
     * @param dirc
     * @return
     * @throws IOException
     */
    public static HashMap<String,HashMap<String, Float>> tfAllFiles(String dirc ,String txtPath) throws IOException{
        
    	HashMap<String, HashMap<String, Float>> allTF = new HashMap<String, HashMap<String, Float>>();
        List<String> filelist = tfidf.readDirs(dirc);
        
        for(String file : filelist){
            HashMap<String, Float> dict = new HashMap<String, Float>();
//          get cut words for one file
            ArrayList<String> cutwords = tfidf.cutWords(file ,txtPath); 
            
            dict = tfidf.tf(cutwords);
            allTF.put(file, dict);
        }
        return allTF;
    }
    
    /**
     * get list of file for the directory, including sub-directory of it
     * Get the fileList<filepath> 
     * @param filepath
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static List<String> readDirs(String filepath) throws FileNotFoundException, IOException
    {
        try
        {
            File file = new File(filepath);
            if(!file.isDirectory())
            {
                System.out.println("目标文件不存在，路径为:" + file.getAbsolutePath());
            }
            
            else
            {
                String[] flist = file.list();
                for(int i = 0; i < flist.length; i++)
                {
                    File newfile = new File(filepath + "\\" + flist[i]);
                    if(!newfile.isDirectory())
                    {
                        FileList.add(newfile.getAbsolutePath());
                    }
                    //if file is a directory, call ReadDirs
                    else if(newfile.isDirectory()) 
                    {
                        readDirs(filepath + "\\" + flist[i]);
                    }                    
                }
            }
        }catch(FileNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
        return FileList;
    }
    
    /**
     * word segmentation ,I change use nlpir
     * @param file
     * @return [吴, 超, 上海, 实验室, 实验, 室]  ArrayList<String>
     * @throws IOException
     */
    public static ArrayList<String> cutWords(String file ,String txtPath) throws IOException{
        
        ArrayList<String> words = new ArrayList<String>();
        String argu = "";
		int charset_type = 1;
		int type=0;
		String userDict="test/userDic.txt";
		
		HashSet<String> stopwordSet=setStopwordSet(txtPath);
        String text = tfidf.readFile(file);
        text=text.replaceAll("\r\n", "");
		String result=Nlpir.diveWord(argu, charset_type, text, type,userDict);
        String[] termArray =result.split(" ");
        
        for (int i = 0; i < termArray.length; i++) {
			words.add(termArray[i]);
		}
        
        for (int j = 0; j < words.size(); j++) {
			if (stopwordSet.contains(words.get(j))) {
				words.remove(j);
			}
		}
        
        return words;
    }
    
	/**
	 * StopWordSet 建立
	 * @param txtPath
	 */
	private static HashSet<String> setStopwordSet(String txtPath) {
		
		String str = "";
		HashSet<String> stopwordSet = new HashSet<String>();
		
		try {
			@SuppressWarnings("resource")
			BufferedReader bufferedIn = new BufferedReader(new FileReader(txtPath));
			while ((str = bufferedIn.readLine()) != null) {
				stopwordSet.add(str);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return stopwordSet;
	}
	
    /**
     * Read file content
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static String readFile(String file) throws FileNotFoundException, IOException
    {
    	 
    	//String is constant， StringBuffer can be changed.
    	StringBuffer strSb = new StringBuffer(); 
        InputStreamReader inStrR = new InputStreamReader(new FileInputStream(file), "gbk"); //byte streams to character streams
        @SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(inStrR); 
        String line = br.readLine();
        while(line != null){
            strSb.append(line).append("\r\n");
            line = br.readLine();    
        }
        
        return strSb.toString();
    }
    
    /**
     * Count term frequency in a file, frequency of each word
     * Result {上海=0.16666667, 实验=0.16666667, 吴=0.16666667, 室=0.16666667, 超=0.16666667, 实验室=0.16666667}
     * Relative Term frequency, get tf
     * @param cutwords
     * @return
     */
    public static HashMap<String, Float> tf(ArrayList<String> cutwords){
        
    	HashMap<String, Float> resTF = new HashMap<String, Float>();
        
        int wordLen = cutwords.size();
        HashMap<String, Integer> intTF = tfidf.normalTF(cutwords); 
        //iterator for that get from TF
        Iterator iter = intTF.entrySet().iterator(); 
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            resTF.put(entry.getKey().toString(), Float.parseFloat(entry.getValue().toString()) / wordLen);
            System.out.println(entry.getKey().toString() + " = "+  Float.parseFloat(entry.getValue().toString()) / wordLen);
        }
        return resTF;
    } 
    
    /**
     * term frequency in a file, times for each word
     * Normal term frequence
     * @param cutwords
     * @return
     */
    public static HashMap<String, Integer> normalTF(ArrayList<String> cutwords){
        
    	HashMap<String, Integer> resTF = new HashMap<String, Integer>();
        
        for(String word : cutwords){
            if(resTF.get(word) == null){
                resTF.put(word, 1);
                System.out.println(word);
            }
            else{
                resTF.put(word, resTF.get(word) + 1);
                System.out.println(word.toString());
            }
        }
        return resTF;
    }
    
    /**
     * Get every term inverse document frequency
     * @param all_tf
     * @return
     */
    public static HashMap<String, Float> idf(HashMap<String,HashMap<String, Float>> all_tf){
        
    	HashMap<String, Float> resIdf = new HashMap<String, Float>();
        HashMap<String, Integer> dict = new HashMap<String, Integer>();
        int docNum = FileList.size();
        
        for(int i = 0; i < docNum; i++){
            HashMap<String, Float> temp = all_tf.get(FileList.get(i));
            Iterator iter = temp.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry = (Map.Entry)iter.next();
                String word = entry.getKey().toString();
                if(dict.get(word) == null){
                    dict.put(word, 1);
                }else {
                    dict.put(word, dict.get(word) + 1);
                }
            }
        }
        System.out.println("IDF for every word is:");
        Iterator iter_dict = dict.entrySet().iterator();
        while(iter_dict.hasNext()){
            Map.Entry entry = (Map.Entry)iter_dict.next();
//          Count innveel term frequency
            float value = (float)Math.log(docNum / Float.parseFloat(entry.getValue().toString()));
            resIdf.put(entry.getKey().toString(), value);
            System.out.println(entry.getKey().toString() + " = " + value);
        }
        return resIdf;
    }        
    
    /**
     * Count every document every term tf-idf
     * @param all_tf
     * @param idfs
     * @return 
     */
    public static HashMap<String, HashMap<String, Float>> tf_idf(HashMap<String,HashMap<String, Float>> all_tf,HashMap<String, Float> idfs){
        
    	HashMap<String, HashMap<String, Float>> resTfIdf = new HashMap<String, HashMap<String, Float>>();            
        int docNum = FileList.size();
        for(int i = 0; i < docNum; i++){
            String filepath = FileList.get(i);
            HashMap<String, Float> tfidf = new HashMap<String, Float>();
            HashMap<String, Float> temp = all_tf.get(filepath);
            Iterator iter = temp.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry = (Map.Entry)iter.next();
                String word = entry.getKey().toString();
                Float value = (float)Float.parseFloat(entry.getValue().toString()) * idfs.get(word); 
                tfidf.put(word, value);
            }
            resTfIdf.put(filepath, tfidf);
        }
        return resTfIdf;
    }

    /**
     * Main Method ,get the txt key word
     * @param file
     * @param txtPath
     * @param num
     * @return
     * @throws IOException
     */
    public static HashMap<String, HashMap<String, Float>> getKey(String file, String txtPath, int num) throws IOException {
		
    	HashMap<String,HashMap<String, Float>> all_tf = tfAllFiles(file, txtPath);
        HashMap<String, Float> idfs = idf(all_tf);
        HashMap<String, HashMap<String, Float>> tfidf=tf_idf(all_tf, idfs);
        
        @SuppressWarnings("rawtypes")
		Iterator iter1 = tfidf.entrySet().iterator();
        HashMap<String, HashMap<String, Float>> keyResult = new HashMap<String, HashMap<String, Float>>();
        
        while(iter1.hasNext()){
            @SuppressWarnings("rawtypes")
			Map.Entry entrys = (Map.Entry)iter1.next();
            System.out.println(entrys.getKey());

            @SuppressWarnings("unchecked")
			HashMap<String, Float> tempMap = (HashMap<String, Float>) entrys.getValue();
            @SuppressWarnings("rawtypes")
			Iterator iter2 = tempMap.entrySet().iterator();
            ArrayList<Float> floatList = new ArrayList<Float>();
            
            while(iter2.hasNext()){
                @SuppressWarnings("rawtypes")
				Map.Entry entry = (Map.Entry)iter2.next();
                System.out.print(entry.getKey() + ":"+entry.getValue()+"\t");
                floatList.add((float)entry.getValue());
            }
            System.out.println();
            Collections.sort(floatList,new compareWeight());
            @SuppressWarnings("unchecked")
			HashMap<String, Float> valueMap = (HashMap<String, Float>) entrys.getValue();
        	HashMap<String, Float> fooMap = new HashMap<String, Float>();
        	
            for (int i = 0; i < num; i++) {
            	@SuppressWarnings("rawtypes")
				Iterator iter3 = valueMap.entrySet().iterator();
            	
            	while (iter3.hasNext()) {
            		@SuppressWarnings("rawtypes")
					Map.Entry entry2 = (Map.Entry)iter3.next();
            		if (entry2.getValue().equals(floatList.get(i))) {
						fooMap.put((String) entry2.getKey(),floatList.get(i));
					}
				}
            	
			}
            keyResult.put((String) entrys.getKey(), fooMap);
        }
		return keyResult;
	}
    
    public static void main(String[] args) throws IOException {

        String file = "test/tfidf";        
        String txtPath = "test/stopWord.txt";
        int num=3;
        
        HashMap<String, HashMap<String, Float>> keyResult = getKey(file, txtPath, num);
        System.out.println(keyResult);
    }

}
