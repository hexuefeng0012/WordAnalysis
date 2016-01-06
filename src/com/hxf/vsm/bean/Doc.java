package com.hxf.vsm.bean;

import java.util.*;

public class Doc {
	int length;  //该文章term个数
	Vector<String> termVec; //该文章term集合
	
	public Doc() { 
		length = 0;
		termVec = new Vector<String>();
	}
	
//	返回的Doc格式 19980101-01-001-001=The length is :3 iphone5s 16G
	public String toString()
	{
		String s="\nThe length is :"+this.length+"\n";
		for(String t:termVec)
		{
			s+=t+"\n";
		}
		return s;
	}
}