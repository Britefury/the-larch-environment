//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Dispatch;

import java.util.List;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;

import BritefuryJ.DocModel.DMObject;

public class DMDispatchPyMethodInvoker
{
	private PyObject function;
	private int indices[];
	
	
	
	public DMDispatchPyMethodInvoker(PyObject function, List<Integer> indices)
	{
		this.function = function;
		this.indices = new int[indices.size()];
		for (int i = 0; i < indices.size(); i++)
		{
			this.indices[i] = indices.get( i );
		}
	}
	
	public PyObject invoke(DMObject node, PyObject dispatchSelf, PyObject args[])
	{
		int numCallARgs = 1  +  args.length  +  1  +  indices.length;
		PyObject callArgs[] = new PyObject[numCallARgs];
		callArgs[0] = dispatchSelf;
		System.arraycopy( args, 0, callArgs, 1, args.length );
		callArgs[args.length+1] = Py.java2py( node );
		for (int i = 0; i < indices.length; i++)
		{
			callArgs[args.length+2+i] = Py.java2py( node.get( indices[i] ) );
		}
		return function.__call__( callArgs );
	}
	
	
	private static final PyString __name__ = Py.newString( "__name__" );
	public String getName()
	{
		return __builtin__.getattr( function, __name__ ).asString(); 
	}
}
