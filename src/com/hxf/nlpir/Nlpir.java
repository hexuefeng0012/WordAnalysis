package com.hxf.nlpir;

import com.hxf.nlpir.config.Init.CLibrary;

public class Nlpir {

	private static void init(String argu,int charset_type) {
		
		int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");
		if (0 == init_flag) {
			String errorMessage = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is "+errorMessage);
			return;
		}
	}
	
	/**
	 * @param argu
	 * @param charset_type
	 * @param sInput
	 * @param type 参数0表示不带词性，参数1表示带有词性
	 * @param userDict 
	 * @return
	 */
	public static String diveWord(String argu,int charset_type,String sInput,int type, String userDict) {
//		init seting
		init(argu, charset_type);
//		add user dic
		CLibrary.Instance.NLPIR_ImportUserDict(userDict);
//		get dive result
		String diveResult = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, type);
		CLibrary.Instance.NLPIR_Exit();
		return diveResult;
		
	}
	
	/**
	 * Get KeyWord
	 * @param argu
	 * @param charset_type
	 * @param sInput
	 * @param num 返回的关键字数量
	 * @param userDict 
	 * @return
	 */
	public static String getKeyWord(String argu,int charset_type,String sInput,int num, String userDict) {
//		init seting
		init(argu, charset_type);
//		add user dic
		CLibrary.Instance.NLPIR_ImportUserDict("userDict");
		String keyWordResult = CLibrary.Instance.NLPIR_GetKeyWords(sInput, num,false);
		CLibrary.Instance.NLPIR_Exit();
		return keyWordResult;
	}
	
	/**
	 * Get a filePath KeyWord
	 * @param argu
	 * @param charset_type
	 * @param sInput
	 * @param num
	 * @param orifilePath
	 * @param userDict 
	 * @return
	 */
	public static String getFileKeyWord(String argu,int charset_type,String sInput,int num,String orifilePath, String userDict) {

//		init seting
		init(argu, charset_type);
//		add user dic
		CLibrary.Instance.NLPIR_ImportUserDict(userDict);
		String keyWordResult = CLibrary.Instance.NLPIR_GetFileKeyWords(orifilePath, num,false);
		CLibrary.Instance.NLPIR_Exit();
		return keyWordResult;
	}
	
	/**
	 * Read a file word, output spilted word
	 * @param argu
	 * @param charset_type
	 * @param sInput
	 * @param num
	 * @param orifilePath
	 * @param utf8File
	 * @param utf8FileResult
	 * @param userDict 
	 */
	public static void getFileDiveWord(String argu,int charset_type,String sInput,String orifilePath,String utf8File,String utf8FileResult, String userDict) {
//		init seting
		init(argu, charset_type);
//		add user dic
		CLibrary.Instance.NLPIR_ImportUserDict(userDict);
		CLibrary.Instance.NLPIR_FileProcess(utf8File, utf8FileResult,0);
		CLibrary.Instance.NLPIR_Exit();
	}
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		String argu = "";
		int charset_type = 1;
		String sInput = "去年开始，打开百度李毅吧，满屏的帖子大多含有“屌丝”二字，一般网友不仅不懂这词什么意思，更难理解这个词为什么会这么火。然而从下半年开始，“屌丝”已经覆盖网络各个角落，人人争说屌丝，人人争当屌丝。从遭遇恶搞到群体自嘲，“屌丝”名号横空出世";
		String orifilePath="test/nlpir/屌丝，一个字头的诞生.txt";
		String utf8File = "test/nlpir/屌丝，一个字头的诞生.txt";
		String utf8FileResult = "test/nlpir/屌丝，一个字头的诞生_result.txt";
		String userDict = "test/userDic.txt";
		
//		不带词性分词结果
		System.out.println("不带词性分词结果为： " + diveWord(argu, charset_type, sInput, 0 ,userDict));
//		带有词性分词结果
		System.out.println("带词性分词结果为： " + diveWord(argu, charset_type, sInput, 1 ,userDict));
//		关键词的提取
		System.out.println("关键词提取结果是：" + getKeyWord(argu, charset_type, sInput, 3 ,userDict));
//		对某个语料关键词的提取
		System.out.println("关键词提取结果是：" + getFileKeyWord(argu, charset_type, sInput, 3, orifilePath ,userDict));
//		对文件的读取与分词
		getFileDiveWord(argu, charset_type, sInput, orifilePath, utf8File, utf8FileResult ,userDict);

	}
}
