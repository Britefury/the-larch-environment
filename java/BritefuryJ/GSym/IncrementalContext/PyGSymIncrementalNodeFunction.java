//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.IncrementalContext;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPWidget;

public class PyGSymIncrementalNodeFunction implements GSymIncrementalNodeFunction
{
	private PyObject callable;
	
	
	public PyGSymIncrementalNodeFunction(PyObject callable)
	{
		this.callable = callable;
	}


	public Object computeNodeResult(DMNode x, GSymIncrementalNodeContext ctx, Object state)
	{
		return (DPWidget)Py.tojava( callable.__call__( Py.java2py( x ), Py.java2py( ctx ), Py.java2py( state ) ), DPWidget.class );
	}
}
