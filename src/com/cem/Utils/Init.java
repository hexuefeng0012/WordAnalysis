package com.cem.Utils;

import java.io.UnsupportedEncodingException;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class Init {

	/**
	 * 定义接口CLibrary，继承自com.sun.jna.Library
	 * @author HXF
	 *
	 */
	public interface CLibrary extends Library {
//		 定义并初始化接口的静态变量位置，来加载 dll 的
//		dll文件的路径可以是绝对路径也可以是相对路径，只需要填写 dll 的文件名，不能加后缀
		CLibrary Instance = (CLibrary) Native.loadLibrary("lib/NLPIR", CLibrary.class);
//		 初始化函数声明 
//		sDataPath :Initial Directory Path, where file Configure.xml and Data directory stored. the default value is 0, it indicates the initial directory is current working directory path
//		encoding :default is GBK_CODE (GBK encoding), and it can be set with UTF8_CODE (UTF8 encoding) and BIG5_CODE (BIG5 encoding).
//		sLicenceCode :license code, special use for some commercial users. Other users ignore the argument
		public int NLPIR_Init(String sDataPath, int encoding,String sLicenceCode);
//		执行分词函数声明，sSrc是要分析的语句，bPOSTagged是分词方式，带语义（1）与不带语义（0）		
		public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);
//		提取关键词函数声明
//		nMaxKeyLimit, the maximum number of  key words.
//		whether the keyword weight output or no 增加关键词的词性等分析
		public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,	boolean bWeightOut);
//		从文件中读取语料，并获取其关键字
		public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit,	boolean bWeightOut);
//		添加用户词典声明
		public int NLPIR_AddUserWord(String sWord);
//		删除用户词典声明
		public int NLPIR_DelUsrWord(String sWord);
//		获得错误信息
		public String NLPIR_GetLastErrorMsg();
//		退出函数声明
		public void NLPIR_Exit();
//		文件分词声明
		public void NLPIR_FileProcess(String utf8File, String utf8FileResult, int i);
//		从自定义Text filename for user dictionary
		public int NLPIR_ImportUserDict(String Filename);
	}

	/**
	 * 公共方法，转变字符编码
	 * @param aidString
	 * @param ori_encoding
	 * @param new_encoding
	 * @return
	 */
	public static String transString(String aidString, String ori_encoding,	String new_encoding) {
		try {
			return new String(aidString.getBytes(ori_encoding), new_encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

}