//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util;

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
