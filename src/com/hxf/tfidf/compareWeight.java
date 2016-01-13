package com.hxf.tfidf;

import java.util.Comparator;

public class compareWeight implements Comparator<Float>{

	@Override
	public int compare(Float o1, Float o2) {
		// TODO Auto-generated method stub
		if(o1 > o2) return -1;
		else if(o1 < o2) return 1;
		else return 0;
	}


}
