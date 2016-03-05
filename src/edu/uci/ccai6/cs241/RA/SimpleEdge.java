package edu.uci.ccai6.cs241.RA;

import java.util.HashSet;
import java.util.Set;

public class SimpleEdge {

	int smallerNode;
	int biggerNode;

	
	public static Set<SimpleEdge> createEdges(int i, Set<Integer> j) {
		Set<SimpleEdge> sparseEdges = new HashSet<SimpleEdge>();
		for(Integer jj : j) {
			if(i==jj) continue;
			SimpleEdge edgeIndex = new SimpleEdge(i,jj);
			sparseEdges.add(edgeIndex);
		}
		return sparseEdges;
	}
	
	public SimpleEdge(int x, int y) {
		smallerNode = x;
		biggerNode = y;
		if(smallerNode > biggerNode) {
			int tmpNode = biggerNode;
			biggerNode = smallerNode;
			smallerNode = tmpNode;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof SimpleEdge)) return false;
		SimpleEdge two = (SimpleEdge) o;
		return (smallerNode == two.smallerNode) && biggerNode == two.biggerNode;
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + smallerNode;
	    result = prime * result + biggerNode;
	    return result;
		
	}
}
