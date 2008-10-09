//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.JythonInterface;

import org.python.core.Py;

public class JythonIndex
{
	public static int pyIndexToJava(int index, int size, String message)
	{
		if ( index < 0 )
		{
			index = size + index;
		}
		
		if ( index < 0  ||  index >= size )
		{
			throw Py.IndexError( message );
		}
		
		return index;
	}
}
