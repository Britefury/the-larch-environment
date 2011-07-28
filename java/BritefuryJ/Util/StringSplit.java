//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Util;

import java.util.ArrayList;
import java.util.List;

public class StringSplit
{
	public static List<String> split(String x, String pattern)
	{
		ArrayList<String> result = new ArrayList<String>();
		int patLen = pattern.length();
		
		int pos = 0;
		int next = x.indexOf( pattern, pos );
		while ( next != -1 )
		{
			result.add( x.substring( pos, next ) );
			
			pos = next + patLen;
			next = x.indexOf( pattern, pos );
		}
		
		result.add( x.substring( pos ) );
		
		return result;
	}
}
