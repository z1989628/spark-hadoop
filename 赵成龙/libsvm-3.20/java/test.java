import java.io.IOException;


public class test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		/*String[] arg = { "D:\\study\\svm\\libsvm-test\\train.txt", // ���SVMѵ��ģ���õ����ݵ�·��
				"D:\\study\\svm\\libsvm-test\\model_r.txt" }; // ���SVMͨ��ѵ������ѵ/ //��������ģ�͵�·��

		String[] parg = { "D:\\study\\svm\\libsvm-test\\test.txt", // ����Ǵ�Ų�������
				"D:\\study\\svm\\libsvm-test\\model_r.txt", // ���õ���ѵ���Ժ��ģ��
				"D:\\study\\svm\\libsvm-test\\out_r.txt" }; // ���ɵĽ�����ļ���·��*/	
		String[] arg = { "E:\\java\\workspace\\JSvmLib\\JSvmLib\\bin\\lib\\SimplifiedSMO\\heart_scale", // ���SVMѵ��ģ���õ����ݵ�·��
		"D:\\study\\svm\\libsvm-test\\model_r_new.txt" }; // ���SVMͨ��ѵ������ѵ/ //��������ģ�͵�·��

		String[] parg = { "E:\\java\\workspace\\JSvmLib\\JSvmLib\\bin\\lib\\SimplifiedSMO\\heart_scale", // ����Ǵ�Ų�������
		"D:\\study\\svm\\libsvm-test\\model_r_new.txt", // ���õ���ѵ���Ժ��ģ��
		"D:\\study\\svm\\libsvm-test\\out_r_new.txt" }; // ���ɵĽ�����ļ���·��
		System.out.println("........SVM���п�ʼ..........");
		// ����һ��ѵ������
		svm_train t = new svm_train();
		// ����һ��Ԥ����߷���Ķ���
		svm_predict p = new svm_predict();
		t.main(arg); // ����
		p.main(parg); // ����
	}

}
