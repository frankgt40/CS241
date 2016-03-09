package edu.uci.ccai6.cs241.RA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * for clustering phi instruction
 * @author norrathep
 *
 */
public class Clusters {

	Map<Integer, Integer> clusterIndex = new HashMap<Integer, Integer>();
	List<HashSet<Integer>> clusters = new ArrayList<HashSet<Integer>>();
	
	
	public void addCluster(Set<Integer> elts) {
		int index = -1;
		// first check if any element in elts is alrdy in any cluster
		//TODO: is it a good idea?
		for(Integer e : elts) {
			if(clusterIndex.containsKey(e) && index == -1 && clusterIndex.get(e) != index) {
				index = clusterIndex.get(e); // already existed 
			} else if(clusterIndex.containsKey(e) && index != -1 && clusterIndex.get(e) != index) {
				System.err.println("Multiple clusters for "+elts+" "+index+" vs "+clusterIndex.get(e));
				printAll();
				System.exit(-1);
			}
		}
		if(index == -1) { 
			HashSet<Integer> cluster = new HashSet<Integer>();
			index = clusters.size();
			cluster.addAll(elts);
			clusters.add(cluster);
		} else {
			HashSet<Integer> existCluster = clusters.get(index);
			existCluster.addAll(elts);
			clusters.set(index, existCluster);
		}
		for(Integer e : elts) {
			clusterIndex.put(e, index);
		}
	}
	
	public void printAll() {
		for(Set<Integer> cluster : clusters) {
			System.out.println("cluster: "+cluster);
		}
	}
	
	public Set<Integer> getCluster(Integer query) {
		Set<Integer> cluster = new HashSet<Integer>();
		cluster.add(query);
		if(!clusterIndex.containsKey(query)) return cluster;
		int index = clusterIndex.get(query);
		return clusters.get(index);
	}
}
