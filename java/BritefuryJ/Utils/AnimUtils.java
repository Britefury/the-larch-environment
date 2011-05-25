//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

public class AnimUtils
{
	public static double scurve(double x)
	{
		return x * x * ( 3.0 - 2.0 * x );
	}
	
	
	public static double seesaw(double x, double waveLength)
	{
		double ramp = Math.IEEEremainder( x, waveLength )  /  waveLength;
		return Math.abs( ramp ) * 2.0;
	}

	public static double scurveSeesaw(double x, double waveLength)
	{
		return scurve( seesaw( x, waveLength ) );
	}
}
