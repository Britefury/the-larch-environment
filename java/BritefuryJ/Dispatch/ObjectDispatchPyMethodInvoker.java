//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Dispatch;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;

public class ObjectDispatchPyMethodInvoker
{
	private PyObject function;
	
	
	
	public ObjectDispatchPyMethodInvoker(PyObject function)
	{
		this.function = function;
	}
	
	public PyObject invoke(Object node, PyObject dispatchSelf, PyObject args[])
	{
		int numCallARgs = args.length + 2;
		PyObject callArgs[] = new PyObject[numCallARgs];
		callArgs[0] = dispatchSelf;
		System.arraycopy( args, 0, callArgs, 1, args.length );
		callArgs[args.length+1] = Py.java2py( node );
		return function.__call__( callArgs );
	}
	
	
	private static final PyString __name__ = Py.newString( "__name__" );
	public String getName()
	{
		return __builtin__.getattr( function, __name__ ).asString(); 
	}
}
