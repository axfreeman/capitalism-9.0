package rd.dev.simulation.utils;

public class MathStuff {
	public static double precision=0.0000001;
	public static boolean equals(double x, double y) {
		return (y<x+precision&&y>x-precision);
	}
}
