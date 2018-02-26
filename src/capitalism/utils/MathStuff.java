/*
 *  Copyright (C) Alan Freeman 2017-2019
 *  
 *  This file is part of the Capitalism Simulation, abbreviated to CapSim
 *  in the remainder of this project
 *
 *  Capsim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either project 3 of the License, or
 *  (at your option) any later project.
*
*   Capsim is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with Capsim.  If not, see <http://www.gnu.org/licenses/>.
*/
package capitalism.utils;

import org.apache.commons.math3.util.Precision;

/**
 * This class handles all operations in which rounding errors can create problems
 *
 */
public class MathStuff {
	// the precision for decimal calculations with large amounts (that is, anything except coefficients, the melt, rate of profit, etc)

	public static int roundingPrecision = 4;
	public static double epsilon = 10 ^ (1 / roundingPrecision);

	public static boolean equals(double x, double y) {
		return (y<x+epsilon && y>x-epsilon);
	}
	
	public static double round(double x) {
		return Precision.round(x, roundingPrecision);
	}
}
