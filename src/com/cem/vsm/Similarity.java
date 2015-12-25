package com.cem.vsm;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

	public class Similarity extends Doc {
		
		/**
		 * TF-IDF模型所需数据
		 * */
		private static final int ARTICLE_KEY_LENGTH = 19; 
		private static final double THRESHOLD = 0.01;  //阀值
		private static double averageArticleLen;       // 文档平均长度
		
		
		/**
		 * 两种算法共同需要数据
		 * */
//		set<string> 表示集合set里面只能存储string类型的数据
		private static Set<String> stopwordSet; 
		private static TreeMap<String, String> articleMap; 
		private static TreeMap<String, Term> TermMap; 
		private static TreeMap<String, Doc> DocMap; 
		private static TreeSet<String> termIntersection; 
		private static TreeSet<String> termUnion;



		/**
		 * 读取一个文档，把每行内容存储起来
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
			System.out.println("stopword count is " + stopwordSet.size());
			System.out.println("Reading the Chinese stopwords into the Set container used "	+ (eReadTime - sReadTime) + "ms" + '\n');
		}

		/**
		 * 将文章存储到map结构中，articleKey 该txt前面的时间，也就是看成一篇文章的关键字  articleContent 文章内容
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
			System.out.println("Article count is " + articles.size());
			System.out.println("Building the articleMap used " + (eReadTime - sReadTime) + "ms" + '\n');
			return articles;
		}

		/**
		 * 获取所有词的情况的TreeMap
		 * @return terms<String, Term> 分词 Term totalCount 该term共出现的总次数  inDocInfo 在某文档中出现次数
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
			System.out.println("Term total count is " + terms.size());
			System.out.println("Building the TermMap used "	+ (eTime - sTime) + "ms" + '\n');
			return terms;
		}

		/**
		 * 将所有文档存储到Doc结构中
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
			System.out.println("Building the DocMap used "	+ (eTime - sTime) + "ms" + '\n');
			return Docs;
		}

		/**
		 * 计算特定两篇文章的相似性
		 * @param qKey
		 * @param dKey
		 * @return
		 */
		public double computeSimilarity(String qKey, String dKey) {
			
			long sTime = System.currentTimeMillis();
			double sim = 0;
			
			intersectTerm(qKey, dKey);
			Iterator<String> it = termIntersection.iterator();

			while (it.hasNext()) {
				String commonTerm = it.next();
				int cwd = TermMap.get(commonTerm).inDocInfo.get(dKey);
				int cwq = TermMap.get(commonTerm).inDocInfo.get(qKey);
				int dfw = TermMap.get(commonTerm).inDocInfo.size();
				
				System.out.println("cwd:"+cwd+" cwq:"+cwq+" dfw:"+dfw);
				double result=(1 + Math.log(1 + Math.log(cwd)))/ (1 - THRESHOLD + THRESHOLD	* DocMap.get(dKey).length/ averageArticleLen) * cwq	* Math.log((articleMap.size() + 1) / dfw);
				long eTime = System.currentTimeMillis();
				System.out.println("The result is "+result+ "The time cost is\t"+(eTime-sTime)+"ms");
				sim += result;
			}
			
			return sim;
		}

		/**
		 * @param q
		 * @param d
		 */
		public static void intersectTerm(String q, String d) {
			
			termIntersection = new TreeSet<String>();
			TreeSet<String> tempSet = new TreeSet<String>();
			
			if (DocMap.get(q) == null || DocMap.get(d) == null) {
				System.out.println("Invalid article key.q is "+q+" and d is "+d);
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
		
		/**
		 * 计算文档的相似性
		 * @param articleKey
		 */
		public static void getSimArray(String articleKey) {
			
//			DocSimilarity TreeMap<String, Double> dKeySim
			TreeMap<String, Vector<DocSimilarity>> DocSimilarityArray = new TreeMap<String, Vector<DocSimilarity>>();
			Vector<DocSimilarity> anDocSimilarityVec = new Vector<DocSimilarity>();
			double sim = 0;
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

			int index = 0;
			dKey = articleMap.firstKey();
			for (; dKey != null;) {
				if(DocSimilarityArray.get(qKey).get(index).dKeySim.get(dKey)!=0)
				System.out.println(qKey	+ "->"+ dKey+ " = "	+ DocSimilarityArray.get(qKey).get(index).dKeySim.get(dKey));
				dKey = articleMap.higherKey(dKey);
				index++;
			}
		}
		
		/**
		 * @param qKey
		 * @param dKey
		 * @return
		 */
		public static double computeSimilarityCos(String qKey,String dKey)
		{
			double sim=0;
			unionTerm(qKey,dKey);
			Iterator<String> it=termUnion.iterator();
			Vector<Integer> vq = new Vector<Integer>();
			Vector<Integer> vd = new Vector<Integer>();
			
			System.out.println("qkey:"+qKey+" dKey:"+dKey);
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
		 * @param q
		 * @param d
		 */
		public static void unionTerm(String q,String d)
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
		
		public static void main(String args[]) {
			
			Similarity.setStopwordSet("test/ChineseStopWords.txt");
			Similarity s = new Similarity();
			articleMap = s.setArticleMap("test/edited1988.txt");			
			TermMap = s.setTermSet();			
			DocMap = s.setDocMap();
			long sTime = System.currentTimeMillis();
			String qKey = null;
			qKey = articleMap.firstKey();
			for (int i = 0; i < 100; i++) {
				if(qKey!=null){
					Similarity.getSimArray(qKey);
					qKey = articleMap.higherKey(qKey);
				}
			}

//			Computing first 100 Articles' Similarity used 76ms 计算文档的相似性的花费时间
			long eTime = System.currentTimeMillis();
			System.out.println("Computing first 100 Articles' Similarity used " + (eTime - sTime) + "ms" + '\n');
			
//			entrySet() entrySet实现了Set接口，里面存放的是键值对 通过getKey（）得到K，getValue得到V 而keySet，里面存的是Map的K。
			for(Entry<String,Doc> entry:DocMap.entrySet())
				{
					System.out.println("The title is "+entry.getKey()+" and the content is "+entry.getValue());
				}
				System.out.println("The size of termSet is "+TermMap.size()+"\n");

			 double similarity = s.computeSimilarity("19980101-01-001-001","19980101-01-001-002");
			 System.out.println(similarity);
		}
	}
