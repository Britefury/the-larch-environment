//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

public class StringDiff
{
	public static int getCommonPrefixLength(String x, String y)
	{
		for (int i = 0; i < Math.min( x.length(), y.length() ); i++)
		{
			if ( x.charAt( i ) != y.charAt( i ) )
			{
				return i;
			}
		}
		
		return Math.min( x.length(), y.length() );
	}

	public static int getCommonSuffixLength(String x, String y)
	{
		int j = x.length() - 1;
		int k = y.length() - 1;
		for (int i = 0; i < Math.min( x.length(), y.length() ); i++)
		{
			if ( x.charAt( j ) != y.charAt( k ) )
			{
				return i;
			}
			
			j--;
			k--;
		}
		
		return Math.min( x.length(), y.length() );
	}
}
