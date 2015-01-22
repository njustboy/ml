package org.ml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 朴素贝叶斯分类器实现（处理二值分类）
 * 
 * @author Administrator
 *
 */
public class NB {
	// 记录属性值是否连续，连续属性取值true,否则取值false
	private Map<Integer, Boolean> featureMap = new HashMap<Integer, Boolean>();

	private String separate;

	// 属性索引--输出值--属性取值--个数
	private Map<Integer, Map<String, Map<String, Integer>>> disFeatureMap = new HashMap<Integer, Map<String, Map<String, Integer>>>();

	private Map<Integer, Map<String, Map<Double, Integer>>> continFeatureMap = new HashMap<Integer, Map<String, Map<Double, Integer>>>();
	// 记录不同输出的个数
	private Map<String, Integer> outMap = new HashMap<String, Integer>();

	private Map<Integer, Map<String, Map<String, Double>>> disFeatureProMap = new HashMap<Integer, Map<String, Map<String, Double>>>();

	private Map<Integer, Map<String, Double>> averageMap = new HashMap<Integer, Map<String, Double>>();

	private Map<Integer, Map<String, Double>> varianceMap = new HashMap<Integer, Map<String, Double>>();

	/**
	 * 初始化操作
	 */
	public void init(Map<Integer, Boolean> featureMap, String separate) {
		this.featureMap = featureMap;
		this.separate = separate;
	}

	public void train(File trainFile) {
		BufferedReader br = null;
		FileReader fr = null;
		try {
			fr = new FileReader(trainFile);
			br = new BufferedReader(fr);
			String line = br.readLine();
			while (line != null) {
				String[] segments = line.split(separate);
				if (segments.length != featureMap.size() + 1) {
					line = br.readLine();
					continue;
				}
				String out = segments[segments.length - 1];
				// 统计不同输出值的个数
				if (outMap.containsKey(out)) {
					outMap.put(out, outMap.get(out) + 1);
				} else {
					outMap.put(out, 1);
				}
				for (int i = 0; i < featureMap.size(); i++) {
					String feature = segments[i];
					if (featureMap.get(i)) {
						Map<String, Map<Double, Integer>> outFeatureMap = continFeatureMap
								.get(i);
						if (outFeatureMap == null) {
							outFeatureMap = new HashMap<String, Map<Double, Integer>>();
							continFeatureMap.put(i, outFeatureMap);
						}
						Map<Double, Integer> featureMap = outFeatureMap
								.get(out);
						if (featureMap == null) {
							featureMap = new HashMap<Double, Integer>();
							outFeatureMap.put(out, featureMap);
						}
						double dFeature = 0;
						try {
							dFeature = Double.parseDouble(feature);
						} catch (Exception e) {

						}
						if (featureMap.containsKey(dFeature)) {
							featureMap.put(dFeature,
									featureMap.get(dFeature) + 1);
						} else {
							featureMap.put(dFeature, 1);
						}
					} else {
						Map<String, Map<String, Integer>> outFeatureMap = disFeatureMap
								.get(i);
						if (outFeatureMap == null) {
							outFeatureMap = new HashMap<String, Map<String, Integer>>();
							disFeatureMap.put(i, outFeatureMap);
						}
						Map<String, Integer> featureMap = outFeatureMap
								.get(out);
						if (featureMap == null) {
							featureMap = new HashMap<String, Integer>();
							outFeatureMap.put(out, featureMap);
						}
						if (featureMap.containsKey(feature)) {
							featureMap
									.put(feature, featureMap.get(feature) + 1);
						} else {
							featureMap.put(feature, 1);
						}
					}
				}
				line = br.readLine();
			}

			// 根据统计的个数计算概率
			Set<Entry<Integer, Map<String, Map<String, Integer>>>> entrySet = disFeatureMap
					.entrySet();
			for (Entry<Integer, Map<String, Map<String, Integer>>> entry : entrySet) {
				Map<String, Map<String, Double>> result = new HashMap<String, Map<String, Double>>();
				Map<String, Map<String, Integer>> value = entry.getValue();
				Set<Entry<String, Map<String, Integer>>> entrySet2 = value
						.entrySet();
				for (Entry<String, Map<String, Integer>> entry2 : entrySet2) {
					Map<String, Double> result2 = new HashMap<String, Double>();
					Map<String, Integer> value2 = entry2.getValue();
					Set<Entry<String, Integer>> entrySet3 = value2.entrySet();
					for (Entry<String, Integer> entry3 : entrySet3) {
						double result3 = entry3.getValue()
								/ (double)outMap.get(entry2.getKey());
						result2.put(entry3.getKey(), result3);
					}
					result.put(entry2.getKey(), result2);
				}
				disFeatureProMap.put(entry.getKey(), result);
			}

			// 根据统计的个数计算均值
			Set<Entry<Integer, Map<String, Map<Double, Integer>>>> entrySet2 = continFeatureMap
					.entrySet();
			for (Entry<Integer, Map<String, Map<Double, Integer>>> entry2 : entrySet2) {
				Map<String, Double> result = new HashMap<String, Double>();
				Map<String, Map<Double, Integer>> value = entry2.getValue();
				Set<Entry<String, Map<Double, Integer>>> entrySet3 = value
						.entrySet();
				for (Entry<String, Map<Double, Integer>> entry3 : entrySet3) {
					Map<Double, Integer> value2 = entry3.getValue();
					double result2 = 0;
					Set<Entry<Double, Integer>> entrySet4 = value2.entrySet();
					for (Entry<Double, Integer> entry4 : entrySet4) {
						result2 += entry4.getKey() * entry4.getValue();
					}
					result2 = result2 / outMap.get(entry3.getKey());
					result.put(entry3.getKey(), result2);
				}
				averageMap.put(entry2.getKey(), result);
			}

			// 计算方差
			entrySet2 = continFeatureMap.entrySet();
			for (Entry<Integer, Map<String, Map<Double, Integer>>> entry2 : entrySet2) {
				Map<String, Double> result = new HashMap<String, Double>();
				Map<String, Map<Double, Integer>> value = entry2.getValue();
				Set<Entry<String, Map<Double, Integer>>> entrySet3 = value
						.entrySet();
				for (Entry<String, Map<Double, Integer>> entry3 : entrySet3) {
					Map<Double, Integer> value2 = entry3.getValue();
					double result2 = 0;
					Set<Entry<Double, Integer>> entrySet4 = value2.entrySet();
					for (Entry<Double, Integer> entry4 : entrySet4) {
						result2 += entry4.getValue()
								* (entry4.getKey() - averageMap.get(
										entry2.getKey()).get(entry3.getKey()))
								* (entry4.getKey() - averageMap.get(
										entry2.getKey()).get(entry3.getKey()));
					}
					result.put(entry3.getKey(), result2);
				}
				varianceMap.put(entry2.getKey(), result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception exc) {

				}
			}

			if (br != null) {
				try {
					br.close();
				} catch (Exception exc) {

				}
			}
		}
	}

	public void test(File testFile) {
		FileReader fr = null;
		BufferedReader br = null;
		
		try{
			fr = new FileReader(testFile);
			br = new BufferedReader(fr);
			String line = br.readLine();
			int index = 0;
			int rightNum = 0;
			Set<String> keySet = outMap.keySet();
			while(line!=null){
				String[] segments = line.split(separate);
				if(segments.length!=featureMap.size()+1){
					line = br.readLine();
					continue;
				}
				Map<String,Double> probMap = new HashMap<String, Double>();
				for(String out:keySet){
					double prob = 1;
					for(int i=0;i<segments.length-1;i++){
						// 连续属性
						if(featureMap.get(i)){
							double dFeature = 0;
							try{
								dFeature = Double.parseDouble(segments[i]);
							}catch(Exception e){
								
							}
							double average = averageMap.get(i).get(out);
							double variance = varianceMap.get(i).get(out);
							prob *= Math.exp(-(dFeature-average)*(dFeature-average)/(2*variance))/(Math.sqrt(2*3.14*variance));
						}else{
							if(null==disFeatureProMap.get(i)){
								System.out.println("the "+i+" in disFeatureProMap is null");
								prob = 0;
							}else if(disFeatureProMap.get(i).get(out)==null){
								System.out.println("the "+i+" in disFeatureProMap with "+out+" is null");
								prob = 0;
							}else if(disFeatureProMap.get(i).get(out).get(segments[i])==null){
								prob = 0;
							}else{
							prob *= disFeatureProMap.get(i).get(out).get(segments[i]);
							}
						}
					}
					probMap.put(out, prob);
				}
				double maxProb = 0;
				String probOut = "";
				Set<Entry<String,Double>> entrySet = probMap.entrySet();
				for(Entry<String,Double> entry:entrySet){
					if(entry.getValue()>maxProb){
						maxProb = entry.getValue();
						probOut = entry.getKey();
					}
				}
				if(segments[segments.length-1].contains(probOut)){
					rightNum++;
				}
				index++;
				line = br.readLine();
			}
			
			System.out.println("测试样本共有"+index+"条数据");
			System.out.println("共有"+rightNum+"条数据被成功分类");
			System.out.println("分类器正确率为"+rightNum/(double)index);
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if (fr != null) {
				try {
					fr.close();
				} catch (Exception exc) {

				}
			}

			if (br != null) {
				try {
					br.close();
				} catch (Exception exc) {

				}
			}
		}
	}

	public static void main(String[] args) {
		NB nb = new NB();
		Map<Integer,Boolean> featureMap = new HashMap<Integer,Boolean>();
		for(int i=0;i<64;i++){
			featureMap.put(i, false);
		}
		String separate = ",";
		File trainFile = new File("files/HandWritten_train.txt");
		File testFile = new File("files/HandWritten_test.txt");
		nb.init(featureMap, separate);
		nb.train(trainFile);
		nb.test(testFile);
	}
}
