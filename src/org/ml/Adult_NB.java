package org.ml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * 
 * 朴素贝叶斯算法实现
 * 属性都转换成连续值处理
 * @author Administrator
 *
 */
public class Adult_NB {
	int n = 0;
	//测试次数
	private static final int REPEAT_NUMBER = 10;
	//属性个数
	private final int CHARACTER_NUMBER = 14;
	//总的训练样本数
	private final int TOTAL_TRAIN_NUMBER = 32561;
	//训练样本数
	private final int TRAIN_NUMBER = 32561;
	//测试样本数
	private final int TEST_NUMBER = 16281;
	//平均错误数
	private float average_fault_number = 0;
	//均值
	private float[][] u = new float[CHARACTER_NUMBER][2];
	//方差
	private float[][] v = new float[CHARACTER_NUMBER][2];
	private float[] y_number = new float[2];

	public static void main(String[] args) {
		Adult_NB a = new Adult_NB();
		try {
			for (int i = 0; i < REPEAT_NUMBER; i++) {
				a.train();
				a.test();
			}
			a.result();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void train() throws Exception {
		float[][] x_train = new float[TRAIN_NUMBER][CHARACTER_NUMBER];
		int[] y_train = new int[TRAIN_NUMBER];

		BufferedReader br = new BufferedReader(new FileReader(
				"files/adult_train.txt"));
		String line = null;
		line = br.readLine();
		List<String> strs = new ArrayList<String>();
		while(line!=null){
			strs.add(line);
			line = br.readLine();
		}
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < TRAIN_NUMBER; i++) {
//			int index = (int)(Math.random()*TOTAL_TRAIN_NUMBER);
//			while(set.contains(index)){
//				index = (int)(Math.random()*TOTAL_TRAIN_NUMBER);
//			}
//			set.add(index);
			String[] lines = strs.get(i).split(",");
			for (int j = 0; j < CHARACTER_NUMBER; j++) {
				x_train[i][j] = ELFHash(lines[j], 10000);
			}
			if (lines[CHARACTER_NUMBER].equalsIgnoreCase(" <=50K")) {
				y_train[i] = 0;
				y_number[0]++;
			} else {
				y_train[i] = 1;
				y_number[1]++;
			}
		}
		for (int i = 0; i < CHARACTER_NUMBER; i++) {
			float temp0 = 0;
			float temp1 = 0;
			for (int j = 0; j < TRAIN_NUMBER; j++) {
				temp0 += x_train[j][i] * delta(y_train[j], 0);
				temp1 += x_train[j][i] * delta(y_train[j], 1);
			}
			u[i][0] = temp0 / y_number[0];
			u[i][1] = temp1 / y_number[1];
		}

		for (int i = 0; i < CHARACTER_NUMBER; i++) {
			float temp0 = 0;
			float temp1 = 0;
			for (int j = 0; j < TRAIN_NUMBER; j++) {
				temp0 += (x_train[j][i] - u[i][0]) * (x_train[j][i] - u[i][0])
						* delta(y_train[j], 0);
				temp1 += (x_train[j][i] - u[i][1]) * (x_train[j][i] - u[i][0])
						* delta(y_train[j], 1);
			}
			v[i][0] = temp0 / y_number[0];
			v[i][1] = temp1 / y_number[1];
		}

	}

	public void test() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(
				"files/adult_test.txt"));
		String line = null;
		float fault_number = 0;
		int test_state = 0;
		int true_state = 0;
		line = br.readLine();
		while (line != null) {
			String[] lines = line.split(",");
			float[] a = new float[CHARACTER_NUMBER];
			for (int i = 0; i < lines.length - 1; i++) {
				a[i] = ELFHash(lines[i], 10000);
			}
			float p0 = gauss(a, 0) * y_number[0];
			float p1 = gauss(a, 1) * y_number[1];
			if (p0 > p1)
				test_state = 0;
			else
				test_state = 1;
			if (lines[lines.length - 1].equalsIgnoreCase(" <=50K."))
				true_state = 0;
			else
				true_state = 1;
			if (test_state != true_state)
				fault_number++;
			line = br.readLine();

		}
		System.out.println("第" + (++n) + "次测试");
		System.out.println("the fault number is: " + fault_number);
		System.out.println("the total number is: " + TEST_NUMBER);
		System.out.println("the test error is:" + fault_number / TEST_NUMBER);
		System.out.println();
		average_fault_number += fault_number;
		y_number[0] = 0;
		y_number[1] = 0;
	}

	public float ELFHash(String str, int prime) {
		long hash = 0;
		long x = 0;

		for (int i = 0; i < str.length(); i++) {
			hash = (hash << 4) + str.charAt(i);

			if ((x = hash & 0xF0000000L) != 0) {
				hash ^= (x >> 24);
			}
			hash &= ~x;
		}
		return (float) Math.abs(hash) % prime / 10000;
	}

	public int delta(int a, int b) {
		if (a == b)
			return 1;
		else
			return 0;
	}

	public float gauss(float[] a, int b) {
		float temp = 1;
		for (int i = 0; i < a.length; i++) {
			temp *= Math.exp(-(a[i] - u[i][b]) * (a[i] - u[i][b])
					/ (2 * v[i][b]))
					/ Math.sqrt(2 * 3.14 * v[i][b]);
		}
		return temp;
	}

	public void result() {
		System.out.println("The Average Test Error is:"
				+ (average_fault_number / (TEST_NUMBER * REPEAT_NUMBER)));
	}

}
