package com.hxf.lda;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.hxf.lda.config.ConstantConfig;
import com.hxf.lda.config.PathConfig;
import com.hxf.lda.util.FileUtil;

/**
 * LDA Gibbs Sampling
 * @author HXF
 * 文本预处理完毕后我们就可以实现LDA Gibbs Sampling。 首先我们要定义需要的参数，我的实现中在程序中给出了参数默认值，
 * 同时也支持配置文件覆盖，程序默认优先选用配置文件的参数设置。
 * 整个算法流程包括模型初始化，迭代Inference，不断更新主题和待估计参数，最后输出收敛时的参数估计结果。
 */
public class LdaGibbsSampling {
	
	public static class modelparameters {

//		alpha是每个文档下Topic的多项分布的Dirichlet先验参数，beta是每个Topic下词的多项分布的Dirichlet先验参数
//		两个参数的设定是经验值，貌似效果比较好，收敛比较快一点。
//		alpha 是 选择为 50/ k, 其中k是你选择的topic数，beta一般选为0.01吧，
//		topicNum = 10   主题的数目是10，主题数目的选取依据结果的perplexity，越低越好，选取趋近于平缓时的点 
//		iteration = 100   迭代次数是100次 ，经验值是迭代1000次
//		saveStep = 10   每10次保存一次  
//		beginSaveIters = 50  从第50次开始保存
//		topNum = 20  Find the top 20 topic words in each topic
		float alpha = 0.5f; 
		float beta = 0.01f;
		int topicNum = 10;
		int iteration = 100;
		int saveStep = 10;
		int beginSaveIters = 90;
		int topNum = 20;
	}
	
	/**
	 * Get parameters from configuring file. If the configuring file has value in it, use the value.
	 * Else the default value in program will be used
	 * @param ldaparameters
	 * @param parameterFile
	 * @return void
	 */
	private static void getParametersFromFile(modelparameters ldaparameters, String parameterFile) {

		ArrayList<String> paramLines = new ArrayList<String>();
		FileUtil.readLines(parameterFile, paramLines);
		
		for(String line : paramLines){
			String[] lineParts = line.split("\t");
			switch(parameters.valueOf(lineParts[0])){
			case alpha:
				ldaparameters.alpha = Float.valueOf(lineParts[1]);
				break;
			case beta:
				ldaparameters.beta = Float.valueOf(lineParts[1]);
				break;
			case topicNum:
				ldaparameters.topicNum = Integer.valueOf(lineParts[1]);
				break;
			case iteration:
				ldaparameters.iteration = Integer.valueOf(lineParts[1]);
				break;
			case saveStep:
				ldaparameters.saveStep = Integer.valueOf(lineParts[1]);
				break;
			case beginSaveIters:
				ldaparameters.beginSaveIters = Integer.valueOf(lineParts[1]);
				break;
			case topNum:
				ldaparameters.topNum = Integer.valueOf(lineParts[1]);
				break;
			}
		}
	}
	
	/**
	 * 枚举需要提前设定的参数
	 * @author HXF
	 *
	 */
	public enum parameters{
		alpha, beta, topicNum, iteration, saveStep, beginSaveIters, topNum;
	}
	
	/**
	 * lda模型运算
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

//		读取目标路径资源
		String originalDocsPath = PathConfig.ldaDocsPath;
		String resultPath = PathConfig.LdaResultsPath;
		String parameterFile= ConstantConfig.LDAPARAMETERFILE;
		
//		读取指定文件的参数
		modelparameters ldaparameters = new modelparameters();
		getParametersFromFile(ldaparameters, parameterFile);
		
//		将语料路径下所有的文档读取，并进行去噪，去停用词，生成的termToIndexMapHashMap<K,V> 
//		termCountMap	HashMap<K,V> 对每个词进行编号，并统计其出现频数
		long startTime=System.currentTimeMillis();		
		Documents docSet = new Documents();
		docSet.readDocs(originalDocsPath);
		System.out.println("wordMap size " + docSet.termToIndexMap.size());
		
//		结果目标路径是否存在
		FileUtil.mkdir(new File(resultPath));

//		初始化模型
		LdaModel model = new LdaModel(ldaparameters);
		System.out.println("1 Initialize the model ...");
		model.initializeModel(docSet);
		
//		模型推断与学习
		System.out.println("2 Learning and Saving the model ...");
		model.inferenceModel(docSet);
		
//		模型输出，输出迭代次数最后的模型，lda.params lda.phi lda.theta lda.tassign lda.twords
		System.out.println("3 Output the final model ...");
		model.saveIteratedModel(ldaparameters.iteration, docSet);
		
//		采样结束，得到计算耗时
		long endTime=System.currentTimeMillis();
		System.out.println("Done! Time spends  "+(endTime-startTime)+"ms");
		
	}
}
