package com.hxf.nlpir.config;

import java.util.HashMap;

public class WordMean {
	public static void Analysis() {
		HashMap<String, Object> map=new HashMap<String, Object>();
		map.put("/t", "时间词");
		map.put("/v", "动词");
		map.put("/nz", "专有名词");
		map.put("/nr", "人名");
		map.put("/wd", "逗号");
		map.put("/wj", "句号");
		map.put("/y", "语气词");
		map.put("/ng", "名词性语素");
		map.put("/ude1", "助词");
		map.put("/d", "副词");
		map.put("/wyz", "前引号");
		map.put("/wyy", "后引号");
		map.put("/n_newword", "新词");
		map.put("/m", "数词");
		map.put("/n", "名词");
		map.put("/a", "形容词");
		map.put("/c", "连词");
		map.put("/rz", "代词");
		map.put("/ryv", "语气代词");
		map.put("/p", "介词");
		map.put("/vl", "习惯用语");
	}
}
