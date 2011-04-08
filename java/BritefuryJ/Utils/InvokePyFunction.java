//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyTuple;

public class InvokePyFunction
{
	// Used to invoke Python functions, where it is desirable to catch *ALL* exceptions, and return the
	// exception objects themselves, so that they can be displayed.
	public static PyTuple invoke(PyObject callable)
	{
		PyObject exc = Py.None;
		PyObject result = Py.None;
		try
		{
			result = callable.__call__();
		}
		catch (Throwable t)
		{
			exc = Py.java2py( t );
		}
		
		return new PyTuple( new PyObject[] { result, exc } );
	}
}
