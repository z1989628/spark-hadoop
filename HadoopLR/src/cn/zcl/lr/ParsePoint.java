package cn.zcl.lr;

import java.util.regex.Pattern;


public class ParsePoint {
	private static final Pattern SPACE = Pattern.compile(" ");

    public DataPoint call(String line) {
      String[] tok = SPACE.split(line);
      double y = Double.parseDouble(tok[0]);
      double[] x = new double[PARAMENT.D];
      for (int i = 0; i < PARAMENT.D; i++) {
        x[i] = Double.parseDouble(tok[i + 1]);
      }
      return new DataPoint(x, y);
    }
    
    public double[] dcall(String line) {
        String[] tok = SPACE.split(line);
        double[] x = new double[PARAMENT.D];
        for (int i = 0; i < PARAMENT.D; i++) {
          x[i] = Double.parseDouble(tok[i]);
        }
        return x;
      }
}
