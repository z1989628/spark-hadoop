package lib.SimplifiedSMO;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;




public class SimplifiedSmo {
	private HashSet<Integer> boundAlpha = new HashSet<Integer>();
	private Random random = new Random();
	/*
	 * �����������ճ���
	 */
	private double x[][];
	
	/*
	 * �˺���
	 */
	double kernel[][];
	double a[];
	int y[];
	double b = 0.0;
	
	private SVMModel train(double x[][], int y[]) {
		this.x = x;
		this.y = y;
		kernel = new double[x.length][x.length];
		//initiateKernel(x.length);
		initiateKernel_RBF(x.length);
		
		/*
		 * Ĭ���������ֵ
		 * C: regularization parameter
		 * tol: numerical tolerance
		 * max passes
		 */
		double C = 1; //�Բ��ڽ��ڵĳͷ�����
		double tol = 0.01;//���̼���ֵ
		int maxPasses = 5; //��ʾû�иı��������ճ��ӵ�����������
		
		/*
		 * ��ʼ��a[], b, passes 
		 */
		
		double a[] = new double[x.length];//�������ճ���
		this.a = a;
		
		//�����ӳ�ʼ��Ϊ0
		for (int i = 0; i < x.length; i++) {
			a[i] = 0;
		}
		int passes = 0;
		

		
		while (passes < maxPasses) {
			//��ʾ�ı���ӵĴ������������ǳɶԸı�ģ�
			int num_changed_alphas = 0;
			for (int i = 0; i < x.length; i++) {
				//��ʾ�ض��׶���a��b���������������ʵyi�����
				//���չ�ʽ(7)
				double Ei = getE(i);
				/*
				 * ��Υ��KKT������ai��Ϊ��һ��
				 * ����KKT����������ǣ�
				 * yi*f(i) >= 1 and alpha == 0 (��ȷ����)
				 * yi*f(i) == 1 and 0<alpha < C (�ڱ߽��ϵ�֧������)
				 * yi*f(i) <= 1 and alpha == C (�ڱ߽�֮��)
				 * 
				 * 
				 * 
				 * ri = y[i] * Ei = y[i] * f(i) - y[i]^2 >= 0
				 * ���ri < 0����alpha < C ��Υ����KKT����
				 * ��Ϊԭ��ri < 0 Ӧ�ö�Ӧ����alpha = C
				 * ͬ��ri > 0����alpha > 0��Υ����KKT����
				 * ��Ϊԭ��ri > 0��Ӧ��Ӧ����alpha =0
				 */
				if ((y[i] * Ei < -tol && a[i] < C) ||
					(y[i] * Ei > tol && a[i] > 0)) 
				{
					/*
					 * ui*yi=1�߽��ϵĵ� 0 < a[i] < C
					 * ��MAX|E1 - E2|
					 */
					int j;
					/*
					 * boundAlpha��ʾx�㴦�ڱ߽�������Ӧ��
					 * �������ճ���a�ļ���
					 */
					if (this.boundAlpha.size() > 0) {
						j = findMax(Ei, this.boundAlpha);
					} else 
						//����߽���û�У������ѡһ��j != i��aj
						j = RandomSelect(i);
					
					double Ej = getE(j);
					
					//���浱ǰ��ai��aj
					double oldAi = a[i];
					double oldAj = a[j];
					
					/*
					 * ������ӵķ�ΧU, V
					 */
					double L, H;
					if (y[i] != y[j]) {
						L = Math.max(0, a[j] - a[i]);
						H = Math.min(C, C - a[i] + a[j]);
					} else {
						L = Math.max(0, a[i] + a[j] - C);
						H = Math.min(0, a[i] + a[j]);
					}
					
					
					/*
					 * ���eta����0���ߴ���0 �����a����ֵӦ����L����U��
					 */
					//double eta = 2 * k(i, j) - k(i, i) - k(j, j);//��ʽ(3)
					double eta = 2 * Rbf_k(i, j) - Rbf_k(i, i) - Rbf_k(j, j);//��ʽ(3)
					
					if (eta >= 0)
						continue;
					
					a[j] = a[j] - y[j] * (Ei - Ej)/ eta;//��ʽ(2)
					if (0 < a[j] && a[j] < C)
						this.boundAlpha.add(j);
					
					if (a[j] < L) 
						a[j] = L;
					else if (a[j] > H) 
						a[j] = H;
					
					if (Math.abs(a[j] - oldAj) < 1e-5)
						continue;
					a[i] = a[i] + y[i] * y[j] * (oldAj - a[j]);
					if (0 < a[i] && a[i] < C)
						this.boundAlpha.add(i);
					
					/*
					 * ����b1�� b2
					 */
					//double b1 = b - Ei - y[i] * (a[i] - oldAi) * k(i, i) - y[j] * (a[j] - oldAj) * k(i, j);
					//double b2 = b - Ej - y[i] * (a[i] - oldAi) * k(i, j) - y[j] * (a[j] - oldAj) * k(j, j);
					
					double b1 = b - Ei - y[i] * (a[i] - oldAi) * Rbf_k(i, i) - y[j] * (a[j] - oldAj) * Rbf_k(i, j);
					double b2 = b - Ej - y[i] * (a[i] - oldAi) * Rbf_k(i, j) - y[j] * (a[j] - oldAj) * Rbf_k(j, j);
					
					if (0 < a[i] && a[i] < C)
						b = b1;
					else if (0 < a[j] && a[j] < C)
						b = b2;
					else 
						b = (b1 + b2) / 2;
					
					num_changed_alphas = num_changed_alphas + 1;
				}
			}
			if (num_changed_alphas == 0) {
				passes++;
			} else 
				passes = 0;
		}
		
		return new SVMModel(a, y, b);
	}
	
	private int findMax(double Ei, HashSet<Integer> boundAlpha2) {
		double max = 0;
		int maxIndex = -1;
		for (Iterator<Integer> iterator = boundAlpha2.iterator(); iterator.hasNext();) {
			Integer j = (Integer) iterator.next();
			double Ej = getE(j);
			if (Math.abs(Ei - Ej) > max) {
				max = Math.abs(Ei - Ej);
				maxIndex = j;
			}
		}
		return maxIndex;
	}

	private double predict(SVMModel model, double x[][], int y[]) {
		double probability = 0;
		int correctCnt = 0;
		int total = y.length;
		
		for (int i = 0; i < total; i++) {
			//ԭ��ѵ�������ά�������ȣ�
			int len = model.y.length;
			double sum = 0;
			for (int j = 0; j < len; j++) {
				//sum += model.y[j] * model.a[j] * k(j, i);
				sum += model.y[j] * model.a[j] * Rbf_k(j, i);
			}
			sum += model.b;
			if ((sum > 0 && y[i] > 0) || (sum < 0 && y[i] < 0))
				correctCnt++;
		}
		probability = (double)correctCnt / (double)total;
		return probability;
	} 
	
	
	
	private void initiateKernel(int length) {
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				kernel[i][j] = k(i, j);
			}
		}
	}
	
	private void initiateKernel_RBF(int length) {
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				kernel[i][j] = Rbf_k(i, j);
			}
		}
	}


	/*
	 * simple
	 * kernel(i, j) = xTx
	 */
	private double k(int i, int j) {
		double sum = 0.0;
		//System.out.println("x.length:" + x.length + "x[i].length" + x[i].length);
		for (int t = 0; t < x[i].length; t++) {
			sum += x[i][t] * x[j][t];
		}
		return sum;
	}
	
	/*
	 * simple ��˹�˺��� Ĭ��r=0.5
	 * kernel(i, j) = exp(-r||xi-xj||2)
	 */
	private double Rbf_k(int i, int j) {
		double sum = 0.0;
		//System.out.println("x.length:" + x.length + "x[i].length" + x[i].length);
		for (int t = 0; t < x[i].length; t++) {
			sum += (x[i][t] - x[j][t])*(x[i][t] - x[j][t]);
		}
		return Math.exp(-0.5*sum);
	}



	/*
	 * select j which is not equal with i
	 */
	private int RandomSelect(int i) {
		int j;
		do {
			j = random.nextInt(x.length);
		} while(i == j);
		return j;
	}



	private double f(int j) {
		double sum = 0;
		for (int i = 0; i < x.length; i++) {
			sum += a[i] * y[i] * kernel[i][j]; 
		}
		
		return sum + this.b;
	}

	private double getE(int i) {
		return f(i) - y[i];
	}

	public static void main(String[] args) {
		SimplifiedSmo simplifiedSMO = new SimplifiedSmo();
		SVMFileReader reader = new SVMFileReader("E:\\java\\workspace\\JSvmLib\\JSvmLib\\bin\\lib\\SimplifiedSMO\\heart_scale");
		SVMData svmData = reader.getSVMData(50);
		
		System.out.println("��ʼѵ��...");
		SVMModel model = simplifiedSMO.train(svmData.getX(), svmData.getY());
		System.out.println("ѵ������");
		//��ʼԤ��
		//������ԭ������ѵ��������
		System.out.println("��ʼԤ��...");
		double probability = simplifiedSMO.predict(model, svmData.getX(), svmData.getY());
		System.out.println("Ԥ����ȷ��Ϊ��" + probability);
	}
}
