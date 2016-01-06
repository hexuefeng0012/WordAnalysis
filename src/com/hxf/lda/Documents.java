package com.hxf.lda;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hxf.lda.util.FileUtil;
import com.hxf.lda.util.Stopwords;

/**
 * 文档集预处理
 * @author HXF
 * 要用LDA对文本进行topic建模，首先要对文本进行预处理，包括token，去停用词，stem，去noise词，去掉低频词等等。
 * 当语料库比较大时，我们也可以不进行stem。然后将文本转换成term的index表示形式，因为后面实现LDA的过程中
 * 经常需要在term和index之间进行映射。Documents类的实现如下，里面定义了Document内部类，用于描述文本集合中的文档。
 */
public class Documents {
	
	ArrayList<Document> docs; 
	Map<String, Integer> termToIndexMap;
	ArrayList<String> indexToTermMap;
	Map<String,Integer> termCountMap;
	
	public Documents(){
		docs = new ArrayList<Document>();
		termToIndexMap = new HashMap<String, Integer>();
		indexToTermMap = new ArrayList<String>();
		termCountMap = new HashMap<String, Integer>();
	}
	
	/**
	 * 读取目标路径下的文档，该类中的主方法
	 * @param docsPath
	 */
	public void readDocs(String docsPath){
		for(File docFile : new File(docsPath).listFiles()){
			Document doc = new Document(docFile.getAbsolutePath(), termToIndexMap, indexToTermMap, termCountMap);
			docs.add(doc);
		}
	}
	
	public static class Document {	
		
		@SuppressWarnings("unused")
		private String docName;
		int[] docWords;
		
		public Document(String docName, Map<String, Integer> termToIndexMap, ArrayList<String> indexToTermMap, Map<String, Integer> termCountMap){
			this.docName = docName;
			
			//Read file and initialize word index array
			ArrayList<String> docLines = new ArrayList<String>();
			ArrayList<String> words = new ArrayList<String>();
			FileUtil.readLines(docName, docLines);
			for(String line : docLines){
				FileUtil.tokenizeAndLowerCase(line, words);
			}
			
			//Remove stop words and noise words
			for(int i = 0; i < words.size(); i++){
				if(Stopwords.isStopword(words.get(i)) || isNoiseWord(words.get(i))){
					words.remove(i);
					i--;
				}
			}
			//Transfer word to index
			this.docWords = new int[words.size()];
			for(int i = 0; i < words.size(); i++){
				String word = words.get(i);
				if(!termToIndexMap.containsKey(word)){
					int newIndex = termToIndexMap.size();
					termToIndexMap.put(word, newIndex);
					indexToTermMap.add(word);
					termCountMap.put(word, new Integer(1));
					docWords[i] = newIndex;
				} else {
					docWords[i] = termToIndexMap.get(word);
					termCountMap.put(word, termCountMap.get(word) + 1);
				}
			}
			words.clear();
		}
		
		/**
		 * Noise Word 进行判断
		 * @param string
		 * @return
		 */
		public boolean isNoiseWord(String string) {
			string = string.toLowerCase().trim();
			Pattern MY_PATTERN = Pattern.compile(".*[a-zA-Z]+.*");
			Matcher m = MY_PATTERN.matcher(string);
			// filter @xxx and URL
			if(string.matches(".*www\\..*") || string.matches(".*\\.com.*") || 
					string.matches(".*http:.*") )
				return true;
			if (!m.matches()) {
				return true;
			} else
				return false;
		}
		
	}
}
