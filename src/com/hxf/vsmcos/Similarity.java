package com.hxf.vsmcos;
import java.io.*;
import java.util.*;

	/**
	 * 基于空间向量模型(VSM)的文档相似度计算
	 * @author HXF
	 *
	 */
	public class Similarity extends Doc {
		
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
//		vsm的向量模型，包含所有出现词
		private static TreeSet<String> termUnion;
		
		/**
		 * 初始化文档，建立停用词表，词汇词频，文档词等映射
		 * @param stopWordPath
		 * @param articlePath
		 */
		public static void init(String stopWordPath,String articlePath) {
			Similarity.setStopwordSet(stopWordPath);
			Similarity s = new Similarity();
			articleMap = s.setArticleMap(articlePath);			
			TermMap = s.setTermSet();			
			DocMap = s.setDocMap();
		}
		
		/**
		 * 读取一个停用词文档，把每行内容存储起来，作为词库语料
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
		private TreeMap<String, String> setArticleMap(String txtPath) {
			
			long sReadTime = System.currentTimeMillis();
			BufferedReader bufferedIn = null;
			String str = "";
			int lineCounter=0;
			TreeMap<String, String> articles = new TreeMap<String, String>();
			
			try {
				bufferedIn = new BufferedReader(new FileReader(txtPath));
				while ((str = bufferedIn.readLine()) != null) {
					if (lineCounter<100 && str.length() > 0) {
						String[] oriArticle=str.split("\t");
						String articleKey = oriArticle[0];
						String articleContent = oriArticle[1];
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
		private TreeMap<String, Term> setTermSet() {
			
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
		private TreeMap<String, Doc> setDocMap() {
			
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
		 * 计算两个指定文档的相似度
		 * @param qkey
		 * @param dkey
		 * @return
		 */
		public static double SimTwoDoc(String qkey,String dkey) {
			
			double sim=0.0;
			String fooKey = articleMap.firstKey();
			
			TreeMap<String, Vector<DocSimilarity>> DocSimilarityArray=getSimArray(qkey);
			Vector<DocSimilarity> anDocSimilarityVec=DocSimilarityArray.get(qkey);
			for (int i = 0; i < anDocSimilarityVec.size(); i++) {
				if (fooKey.equals(dkey)) {
					sim=anDocSimilarityVec.get(i).dKeySim.get(dkey);
				}
				fooKey=articleMap.higherKey(fooKey);
			}
			return sim;
		}
		
		/**
		 * 计算某文档与语料集中其他文档的相似度
		 * Result 19980101-01-001-001->19980101-01-001-002 = 0.8164965809277259
		 * @param articleKey
		 */
		private static TreeMap<String, Vector<DocSimilarity>> getSimArray(String articleKey) {
			
			TreeMap<String, Vector<DocSimilarity>> DocSimilarityArray = new TreeMap<String, Vector<DocSimilarity>>();
			Vector<DocSimilarity> anDocSimilarityVec = new Vector<DocSimilarity>();
			double sim = 0.0;
			String qKey = articleKey;
			String dKey = articleMap.firstKey();
			
			while (dKey != null) {
				DocSimilarity anDocSimilarity = new DocSimilarity();
				if (dKey.equals(qKey))
					sim = 0;
				else
					sim = computeSimilarityCos(qKey, dKey);
				anDocSimilarity.dKeySim.put(dKey, sim);
				anDocSimilarityVec.add(anDocSimilarity);
				DocSimilarityArray.put(qKey, anDocSimilarityVec);
				dKey = articleMap.higherKey(dKey);
			}		
			return DocSimilarityArray;
			
		}
		
		/**
		 * 通过余弦值计算文档相似度
		 * @param qKey
		 * @param dKey
		 * @return
		 */
		private static double computeSimilarityCos(String qKey,String dKey)
		{
			
			double sim=0.0;
			unionTerm(qKey,dKey);
			Iterator<String> it=termUnion.iterator();
			Vector<Integer> vq = new Vector<Integer>();
			Vector<Integer> vd = new Vector<Integer>();

			while(it.hasNext())
			{
				String term=it.next();
				if(TermMap.get(term).inDocInfo.get(qKey)== null)
					vq.add(new Integer(0));
				else
					vq.add(new Integer(TermMap.get(term).inDocInfo.get(qKey)));
				if(TermMap.get(term).inDocInfo.get(dKey)== null)
					vd.add(new Integer(0));
				else
					vd.add(new Integer(TermMap.get(term).inDocInfo.get(dKey)));
			}
			
			double mtpResult=0D;
			double ldSqr=0D;
			double lqSqr=0D;
			for(int i=0;i<vq.size();i++)
			{
				mtpResult+=((Integer)vq.get(i)).doubleValue()*((Integer)vd.get(i)).doubleValue();
				lqSqr+=((Integer)vq.get(i)).intValue()*((Integer)vq.get(i)).intValue();
				ldSqr+=((Integer)vd.get(i)).intValue()*((Integer)vd.get(i)).intValue();
			}
			sim=mtpResult/(Math.sqrt(ldSqr)*Math.sqrt(lqSqr));
			
			return sim;
		}
		
		/**
		 * 建立VSM，确定同一个维度向量
		 * @param q
		 * @param d
		 */
		private static void unionTerm(String q,String d)
		{
			
			termUnion =  new TreeSet<String>();
			if(DocMap.get(q)==null || DocMap.get(d)==null)
			{
				System.out.println("Invalid article key.q is "+q+" and d is "+d);
				System.exit(0);
			}
			for (int i = 0; i != DocMap.get(q).termVec.size(); i++)
				termUnion.add(DocMap.get(q).termVec.get(i));
			for (int i = 0; i != DocMap.get(d).termVec.size(); i++)
				termUnion.add(DocMap.get(d).termVec.get(i));
		}
		
		/**
		 * Count the sim between two doc
		 * @param stopWordPath
		 * @param articlePath
		 * @param qkey
		 * @param dkey
		 * @return
		 */
		public static double countSim(String stopWordPath, String articlePath, String qkey, String dkey) {
			init(stopWordPath, articlePath);
			return SimTwoDoc(qkey,dkey);
		}
		
		public static void main(String args[]) {
			
			String stopWordPath="test/stopWord.txt";
			String articlePath="test/vsmcos/edited2014.txt";
			String qkey="19980101-01-001-001";
			String dkey="19980101-01-001-002";
			
			init(stopWordPath, articlePath);
			System.out.println(qkey+"与"+dkey+"的文档相似度是"+SimTwoDoc(qkey,dkey));

		}
	}
   