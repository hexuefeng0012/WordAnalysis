package com.hxf.lda;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.hxf.lda.config.PathConfig;
import com.hxf.lda.util.FileUtil;

/**
 * LDA 模型实现类
 * @author HXF
 *
 */
public class LdaModel {
	
	int [][] doc;//word index array
	int V, K, M;//vocabulary size, topic number, document number
	int [][] z;//topic label array
	float alpha; //doc-topic dirichlet prior parameter 
	float beta; //topic-word dirichlet prior parameter
	int [][] nmk;//given document m, count times of topic k. M*K
	int [][] nkt;//given topic k, count times of term t. K*V
	int [] nmkSum;//Sum for each row in nmk
	int [] nktSum;//Sum for each row in nkt
	double [][] phi;//Parameters for topic-word distribution K*V
	double [][] theta;//Parameters for doc-topic distribution M*K
	int iterations;//Times of iterations
	int saveStep;//The number of iterations between two saving
	int beginSaveIters;//Begin save model at this iteration
	int topNum; //Find the top topic words in each topic
	
	public LdaModel(LdaGibbsSampling.modelparameters modelparam) {

		alpha = modelparam.alpha;
		beta = modelparam.beta;
		iterations = modelparam.iteration;
		K = modelparam.topicNum;
		saveStep = modelparam.saveStep;
		beginSaveIters = modelparam.beginSaveIters;
		topNum=modelparam.topNum;
	}

	/**
	 * 初始化需要的索引数组，K，M，V等基本变量，并随机分配给每个文档的每个单词以初始化topic标签
	 * @param docSet
	 */
	public void initializeModel(Documents docSet) {

		M = docSet.docs.size();
		V = docSet.termToIndexMap.size();
		nmk = new int [M][K];
		nkt = new int[K][V];
		nmkSum = new int[M];
		nktSum = new int[K];
		phi = new double[K][V];
		theta = new double[M][K];
		
		//initialize documents index array
		doc = new int[M][];
		for(int m = 0; m < M; m++){
			//Notice the limit of memory
			int N = docSet.docs.get(m).docWords.length;
			doc[m] = new int[N];
			for(int n = 0; n < N; n++){
				doc[m][n] = docSet.docs.get(m).docWords[n];
			}
		}
		
		//initialize topic lable z for each word
		z = new int[M][];
		for(int m = 0; m < M; m++){
			int N = docSet.docs.get(m).docWords.length;
			z[m] = new int[N];
			for(int n = 0; n < N; n++){
				int initTopic = (int)(Math.random() * K);// From 0 to K - 1
				z[m][n] = initTopic;
				//number of words in doc m assigned to topic initTopic add 1
				nmk[m][initTopic]++;
				//number of terms doc[m][n] assigned to topic initTopic add 1
				nkt[initTopic][doc[m][n]]++;
				// total number of words assigned to topic initTopic add 1
				nktSum[initTopic]++;
			}
			 // total number of words in document m is N
			nmkSum[m] = N;
		}
	}

	/**
	 * 模型的推论
	 * @param docSet
	 * @throws IOException
	 */
	public void inferenceModel(Documents docSet) throws IOException {

		if(iterations < saveStep + beginSaveIters){
			System.err.println("Error: the number of iterations should be larger than " + (saveStep + beginSaveIters));
			System.exit(0);
		}
		
		for(int i = 0; i < iterations; i++){
			System.out.println("Iteration " + i);
			if((i >= beginSaveIters) && (((i - beginSaveIters) % saveStep) == 0)){
				//Saving the model
				System.out.println("Saving model at iteration " + i +" ... ");
				//Firstly update parameters
				updateEstimatedParameters();
				//Secondly print model variables
				saveIteratedModel(i, docSet);
			}
			
			//Use Gibbs Sampling to update z[][]
			for(int m = 0; m < M; m++){
				int N = docSet.docs.get(m).docWords.length;
				for(int n = 0; n < N; n++){
					// Sample from p(z_i|z_-i, w)
					int newTopic = sampleTopicZ(m, n);
					z[m][n] = newTopic;
				}
			}
		}
	}
	
	/**
	 * 当迭代指定次数时，更新phi和theta
	 */
	private void updateEstimatedParameters() {

		for(int k = 0; k < K; k++){
			for(int t = 0; t < V; t++){
				phi[k][t] = (nkt[k][t] + beta) / (nktSum[k] + V * beta);
			}
		}
		
		for(int m = 0; m < M; m++){
			for(int k = 0; k < K; k++){
				theta[m][k] = (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
			}
		}
	}

	/**
	 * 保存某个迭代次数下的lda模型 lda.params lda.phi lda.theta lda.tassign lda.twords
	 * @param iters
	 * @param docSet
	 * @throws IOException
	 */
	public void saveIteratedModel(int iters, Documents docSet) throws IOException {

		//lda.params
		String resPath = PathConfig.LdaResultsPath;
		String modelName = "lda_" + iters;
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("alpha = " + alpha);
		lines.add("beta = " + beta);
		lines.add("topicNum = " + K);
		lines.add("docNum = " + M);
		lines.add("termNum = " + V);
		lines.add("iterations = " + iterations);
		lines.add("saveStep = " + saveStep);
		lines.add("beginSaveIters = " + beginSaveIters);
		FileUtil.writeLines(resPath + modelName + ".params", lines);
		
		//lda.phi K*V
		BufferedWriter writer = new BufferedWriter(new FileWriter(resPath + modelName + ".phi"));		
		for (int i = 0; i < K; i++){
			for (int j = 0; j < V; j++){
				writer.write(phi[i][j] + "\t");
			}
			writer.write("\n");
		}
		writer.close();
		
		//lda.theta M*K
		writer = new BufferedWriter(new FileWriter(resPath + modelName + ".theta"));
		for(int i = 0; i < M; i++){
			for(int j = 0; j < K; j++){
				writer.write(theta[i][j] + "\t");
			}
			writer.write("\n");
		}
		writer.close();
		
		//lda.tassign
		writer = new BufferedWriter(new FileWriter(resPath + modelName + ".tassign"));
		for(int m = 0; m < M; m++){
			for(int n = 0; n < doc[m].length; n++){
				writer.write(doc[m][n] + ":" + z[m][n] + "\t");
			}
			writer.write("\n");
		}
		writer.close();
		
		//lda.twords phi[][] K*V
		writer = new BufferedWriter(new FileWriter(resPath + modelName + ".twords"));		
		for(int i = 0; i < K; i++){
			List<Integer> tWordsIndexArray = new ArrayList<Integer>(); 
			for(int j = 0; j < V; j++){
				tWordsIndexArray.add(new Integer(j));
			}
			Collections.sort(tWordsIndexArray, new LdaModel.TwordsComparable(phi[i]));
			writer.write("topic " + i + "\t:\t");
			for(int t = 0; t < topNum; t++){
				writer.write(docSet.indexToTermMap.get(tWordsIndexArray.get(t)) + " " + phi[i][tWordsIndexArray.get(t)] + "\t");
			}
			writer.write("\n");
		}
		writer.close();
	}
	
	/**
	 * 实现Comparator接口，继承compare()，比较，提出概论较大的
	 * @author HXF
	 *
	 */
	public class TwordsComparable implements Comparator<Integer> {
		
		public double [] sortProb; // Store probability of each word in topic k
		
		public TwordsComparable (double[] sortProb){
			this.sortProb = sortProb;
		}

		@Override
		public int compare(Integer o1, Integer o2) {

			//Sort topic word index according to the probability of each word in topic k
			if(sortProb[o1] > sortProb[o2]) return -1;
			else if(sortProb[o1] < sortProb[o2]) return 1;
			else return 0;
		}
	}
	
	/**
	 * 用Gibbs Sampling学习LDA
	 * z[][],phi[][],theta[][],都是未知的隐含变量，也是我们需要根据观察到的文档集合中的词来学习估计的
	 * 每次调用sampleTopicZ时，就是一次迭代
	 * 直到发现每个文档下Topic分布和每个Topic下词的分布收敛，算法停止，输出待估计的参数phi,theta,tassign
	 * 实际应用中会设置最大迭代次数，迭代完成后输出参数结果
	 * Sample from p(z_i|z_-i, w) using Gibbs updating rule
	 * @param m
	 * @param n
	 * @return
	 */
	private int sampleTopicZ(int m, int n) {

		//Remove topic label for w_{m,n}
		int oldTopic = z[m][n];
		nmk[m][oldTopic]--;
		nkt[oldTopic][doc[m][n]]--;
		nmkSum[m]--;
		nktSum[oldTopic]--;
		
//		排除当前词的主题分配，根据其他所有词的主题分配估计当前词分配各个主题的概率
		
		//Compute p(z_i = k|z_-i, w)
		double [] p = new double[K];
		for(int k = 0; k < K; k++){
			p[k] = (nkt[k][doc[m][n]] + beta) / (nktSum[k] + V * beta) * (nmk[m][k] + alpha) / (nmkSum[m] + K * alpha);
		}
		
//		p[k]是当前次属于所有主题z的概率分布后，根据这个概率分布为该词sample一个新的主题

		//Compute cumulated probability for p
		for(int k = 1; k < K; k++){
			p[k] += p[k - 1];
		}
		//p[] is unnormalised
		double u = Math.random() * p[K - 1]; 
		int newTopic;
		for(newTopic = 0; newTopic < K; newTopic++){
			if(u < p[newTopic]){
				break;
			}
		}
		
		//Add new topic label for w_{m, n}
		nmk[m][newTopic]++;
		nkt[newTopic][doc[m][n]]++;
		nmkSum[m]++;
		nktSum[newTopic]++;
		return newTopic;
	}

}
