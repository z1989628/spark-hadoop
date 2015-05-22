package cn.zcl.lr;
public class ComputeGradient  {
    private final double[] weights;
    ComputeGradient(double[] weights) {
      this.weights = weights;
    }
    
    public static double dot(double[] a, double[] b) {
	    double x = 0;
	    for (int i = 0; i < PARAMENT.D; i++) {
	      x += a[i] * b[i];
	    }
	    return x;
	  }

    public double[] call(DataPoint p) {
      double[] gradient = new double[PARAMENT.D];
      for (int i = 0; i < PARAMENT.D; i++) {
        double dot = dot(weights, p.x);
        gradient[i] = (1 / (1 + Math.exp(-p.y * dot)) - 1) * p.y * p.x[i];
      }
      return gradient;
    }
  }