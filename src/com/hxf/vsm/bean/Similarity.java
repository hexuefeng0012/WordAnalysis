package com.hxf.vsm.bean;
import java.io.*;
import java.util.*;

	/**
	 * 基于tf-idf加权技术改进的空间向量模型(VSM)
	 * @author HXF
	 *
	 */
	public class Similarity extends Doc {
		
//		文档key的长度  
		private static final int ARTICLE_KEY_LENGTH = 19;
//		文档长度归一化阀值
		private static final double THRESHOLD = 0.01;
//		文档平均长度
		private static double averageArticleLen;       
//		停用词集合,set<string> 表示集合set里面只能存储string类型的数据
		private static Set<String> stopwordSet; 
//		文档key与内容的映射集
		private static TreeMap<String, String> articleMap;
//		词项与其相关统计信息的映射集
		private static TreeMap<String, Term> TermMap;
//		文档key与其相关统计信息（长度、词项集）的映射集  
		private static TreeMap<String, Doc> DocMap;
//		文档词项交集
		private static TreeSet<String> termIntersection; 

		/**
		 * 读取一个文档，把每行内容存储起来，作为词库语料
		 * @return 文档行数和读取花费的时间
		 */
		public static void setStopwordSet(String txtPath) {
			
			long sReadTime = System.currentTimeMillis();
			BufferedReader bufferedIn = null;
			String str = "";
			stopwordSet = new HashSet<String>();
			
			try {
				bufferedIn = new BufferedReader(new FileReader(txtPath));
				while ((str = bufferedIn.readLine()) != null) {
					stopwordSet.add(str);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			long eReadTime = System.currentTimeMillis();
			System.out.println("停用词共有 " + stopwordSet.size());
			System.out.println("停用词集合建立成功！ 共耗时"	+ (eReadTime - sReadTime) + "ms" + '\n');
		}

		/**
		 * 将分词好的文章存储到map结构中，articleKey 该txt前面的时间，也就是看成一篇文章的关键字  articleContent 文章内容
		 * {19980101-01-001-001=iphone5s 16G, 19980101-01-001-002=iphone5s 16G 电信版} 
		 * @return articles
		 */
		@SuppressWarnings("resource")
		public TreeMap<String, String> setArticleMap(String txtPath) {
			
			long sReadTime = System.currentTimeMillis();
			BufferedReader bufferedIn = null;
			String str = "";
			int lineCounter=0;
			TreeMap<String, String> articles = new TreeMap<String, String>();
			
			try {
				bufferedIn = new BufferedReader(new FileReader(txtPath));
				while ((str = bufferedIn.readLine()) != null) {
					if (lineCounter<100 && str.length() > 0) {
						String articleKey = str.substring(0, ARTICLE_KEY_LENGTH);
						String articleContent = str.substring(ARTICLE_KEY_LENGTH + 2);
						if (articles.isEmpty() || !articles.containsKey(articleKey))
							articles.put(articleKey, articleContent);
						else {
							String tempStr = articles.get(articleKey) + articleContent;
							articles.put(articleKey, tempStr);
						}
					}
					lineCounter++;
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			long eReadTime = System.currentTimeMillis();
			System.out.println("文章的篇数是 " + articles.size());
			System.out.println("建立关键词-文章的映射集成功！ 共耗时" + (eReadTime - sReadTime) + "ms" + '\n');
			return articles;
		}

		/**
		 * 获取所有词的情况的TreeMap,在文章中出现的所有分词
		 * @return terms<String, Term> 分词 Term totalCount 该term共出现的总次数  inDocInfo 在某文档中出现次数
		 * Object Value
		 * {16G=com.cem.vsm.Term@7730661d, iphone5s=com.cem.vsm.Term@a80370d, 巧克力=com.cem.vsm.Term@679e3bdd, 电信版=com.cem.vsm.Term@456c5f50}
		 * Value 该词在每篇文章中的出现频率{19980101-01-001-001=1, 19980101-01-001-002=1} totalCount 2 该词在所有文章中出现频率
		 * map keySet() 有一个Map对象，这时候使用keySet()方法获取所有的key值
		 */
		public TreeMap<String, Term> setTermSet() {
			
			long sTime = System.currentTimeMillis();
			TreeMap<String, Term> terms = new TreeMap<String, Term>();
			String currentArticleKey = articleMap.firstKey();
			
			while (currentArticleKey != null) {
				String currentArticleContent = articleMap.get(currentArticleKey);
				String[] termArray = currentArticleContent.split(" ");
				
				for(String t:termArray)
				{
					if(stopwordSet.contains(t))
					{
						continue;
					}else{
						if(!terms.keySet().contains(t))
						{
							Term newTerm =  new Term();
							newTerm.totalCount = 1;
							newTerm.inDocInfo.put(currentArticleKey, 1);
							terms.put(t, newTerm);
						}else{
							Term oldTerm=terms.get(t);
							oldTerm.totalCount++;
							if(oldTerm.inDocInfo.get(currentArticleKey)==null)
								oldTerm.inDocInfo.put(currentArticleKey, 1);
							else{
								Integer tempInDocCount = oldTerm.inDocInfo.get(currentArticleKey)+1;
								oldTerm.inDocInfo.put(currentArticleKey, tempInDocCount);
							}
							terms.put(t, oldTerm);
						}
					}
				}				
				
				currentArticleKey = articleMap.higherKey(currentArticleKey);
			}
			long eTime = System.currentTimeMillis();
			System.out.println("所有词汇的总数是" + terms.size());
			System.out.println("建立词汇-（总频数，每篇文章的频数）的映射集成功 共耗时 "	+ (eTime - sTime) + "ms" + '\n');
			return terms;
		}

		/**
		 * 文档key与其相关统计信息（长度、词项集）的映射集 
		 * @return 结构 int length 该文档的单词的总个数 termVec 该文档的单词集合
		 */
		public TreeMap<String, Doc> setDocMap() {
			
			long sTime = System.currentTimeMillis();
			TreeMap<String, Doc> Docs = new TreeMap<String, Doc>();
			String currentArticleKey = articleMap.firstKey();
			int allArticleLength = 0;
			int len = 0;
			
			while (currentArticleKey != null) {
				String currentArticleContent = articleMap.get(currentArticleKey);
				if (Docs.get(currentArticleKey) == null) {
					len = 0;
				} else
					len = Docs.get(currentArticleKey).length;
				
				String[] termArray = currentArticleContent.trim().split(" ");

				for(String t:termArray)
				{
					if(t.equals(""))
					{
						continue;
					}
					len=t.length();
					if(stopwordSet.contains(t))
					{
						continue;
					}else{
						if(!Docs.keySet().contains(currentArticleKey))
						{
							
							Doc newDoc =  new Doc();
							newDoc.length = len;
							newDoc.termVec.add(t);
							Docs.put(currentArticleKey, newDoc);
							
						}else{
							Doc oldDoc=Docs.get(currentArticleKey);
							oldDoc.length = len;
							oldDoc.termVec.add(t);
							Docs.put(currentArticleKey, oldDoc);
						}
					}
				}

				allArticleLength += Docs.get(currentArticleKey).length;
				currentArticleKey = articleMap.higherKey(currentArticleKey);
			}
			
			long eTime = System.currentTimeMillis();
			averageArticleLen = allArticleLength / articleMap.size();
			System.out.println("每篇文档平均词汇数是 " + averageArticleLen);
			System.out.println("建立文档关键词-（长度，词汇）的映射集成功！ 共耗时"	+ (eTime - sTime) + "ms" + '\n');
			return Docs;
		}

		/**
		 * 计算特定两篇文章的相似性
		 * qKey 第一篇文章的名字 dKey 第二篇文章的名字
		 * @param qKey
		 * @param dKey
		 * @return
		 */
		public double computeSimilarity(String qKey, String dKey) {
			
			double sim = 0.0;		
			intersectTerm(qKey, dKey);
			Iterator<String> it = termIntersection.iterator();

			while (it.hasNext()) {
				String commonTerm = it.next();
				int cwd = TermMap.get(commonTerm).inDocInfo.get(dKey);
				int cwq = TermMap.get(commonTerm).inDocInfo.get(qKey);
				int dfw = TermMap.get(commonTerm).inDocInfo.size();
				
				double result=(1 + Math.log(1 + Math.log(cwd)))/ (1 - THRESHOLD + THRESHOLD	* DocMap.get(dKey).length/ averageArticleLen) * cwq	* Math.log((articleMap.size() + 1) / dfw);
				sim += result;
			}
			
			return sim;
		}

		/**
		 * 求词项集合交集 
		 * termIntersection 文档q 文档d 的词汇交集
		 * @param q
		 * @param d
		 */
		public static void intersectTerm(String q, String d) {
			
			termIntersection = new TreeSet<String>();
			TreeSet<String> tempSet = new TreeSet<String>();
			
			if (DocMap.get(q) == null || DocMap.get(d) == null) {
				System.exit(0);
			}
			for (int i = 0; i != DocMap.get(q).termVec.size(); i++)
				tempSet.add(DocMap.get(q).termVec.get(i));
			for (int i = 0; i != DocMap.get(d).termVec.size(); i++) {
				boolean flag = tempSet.contains(DocMap.get(d).termVec.get(i));
				if (flag == true) {
					termIntersection.add(DocMap.get(d).termVec.get(i));
				}
			}
		}
		
		public static void main(String args[]) {
			
			Similarity.setStopwordSet("test/ChineseStopWords.txt");
			Similarity s = new Similarity();
			articleMap = s.setArticleMap("test/edited2014.txt");			
			TermMap = s.setTermSet();			
			DocMap = s.setDocMap();
			double similarity = s.computeSimilarity("19980101-01-001-001","19980101-01-001-002");
			System.out.println("两篇文章相似度是"+similarity);
		}
	}
