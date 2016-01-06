package com.hxf.nlpir;

import com.hxf.nlpir.config.Init.CLibrary;

public class Nlpir {

	public static void main(String[] args) throws Exception {
		
		String argu = "";
		String diveResult = "";
		String keyWordResult ="";
		String errorMessage="";
		int charset_type = 1;
		String sInput = "去年开始，打开百度李毅吧，满屏的帖子大多含有“屌丝”二字，一般网友不仅不懂这词什么意思，更难理解这个词为什么会这么火。然而从下半年开始，“屌丝”已经覆盖网络各个角落，人人争说屌丝，人人争当屌丝。从遭遇恶搞到群体自嘲，“屌丝”名号横空出世";

//		初始化分词工具
		int init_flag = CLibrary.Instance.NLPIR_Init(argu, charset_type, "0");
		if (0 == init_flag) {
			errorMessage = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is "+errorMessage);
			return;
		}
		try {
//			增加用户自定义语料库
			CLibrary.Instance.NLPIR_ImportUserDict("test/Userdic.txt");
//			参数0表示不带词性，参数1表示带有词性
			diveResult = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 0);
			System.out.println("不带词性分词结果为： " + diveResult);
			
			diveResult = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
			System.out.println("带词性分词结果为： " + diveResult+"\n");

//			增加用户词典后与删除用户词典后,增加用户词典时，可以增加该词的词性，比如"更难理解 v"
			CLibrary.Instance.NLPIR_AddUserWord("更难理解 v");
			diveResult = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
			System.out.println("增加用户词典后分词结果为： " + diveResult);
						
			CLibrary.Instance.NLPIR_DelUsrWord("更难理解");
			diveResult = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 1);
			System.out.println("删除用户词典后分词结果为： " + diveResult+"\n");
			
//			对某个语料关键词的提取
			keyWordResult = CLibrary.Instance.NLPIR_GetKeyWords(sInput, 10,false);
			System.out.print("关键词提取结果是：" + keyWordResult+"\r\n");

			keyWordResult = CLibrary.Instance.NLPIR_GetFileKeyWords("test/屌丝，一个字头的诞生.txt", 10,false);
			System.out.print("关键词提取结果是：" + keyWordResult+"\r\n");

//			文件分词的输入和输出
			String utf8File = "test/屌丝，一个字头的诞生.txt";
			String utf8FileResult = "test/屌丝，一个字头的诞生_result.txt";
			CLibrary.Instance.NLPIR_FileProcess(utf8File, utf8FileResult,0);
			
			CLibrary.Instance.NLPIR_Exit();

		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}
}
