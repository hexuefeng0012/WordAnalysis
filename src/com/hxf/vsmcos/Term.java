package com.hxf.vsmcos;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HXF
 *
 */
public class Term {
	
//	该term共出现的总次数
	int totalCount; 
//	该term所对应的文档及在每个文档中出现的次数
	Map<String, Integer> inDocInfo; 

	public Term() {
		totalCount = 0;
		inDocInfo = new HashMap<String, Integer>();
	}
}