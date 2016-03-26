//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.Jython;

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
