//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocView.DVNode.NodeElementChangeListener;

public class PyGSymViewFactory extends GSymViewFactory
{
	private PyObject changeListenerFactory, viewFunctionFactory;
	
	public PyGSymViewFactory(PyObject viewFunctionFactor, PyObject changeListenerFactory)
	{
		this.viewFunctionFactory = viewFunctionFactor;
		this.changeListenerFactory = changeListenerFactory;
	}
	
	
	public NodeElementChangeListener createChangeListener()
	{
		return (NodeElementChangeListener)Py.tojava( changeListenerFactory.__call__(), NodeElementChangeListener.class );
	}

	public GSymNodeViewFunction createViewFunction()
	{
		PyObject viewFn = viewFunctionFactory.__call__();
		return new GSymNodeViewInstance.PyGSymNodeViewFunction( viewFn );
	}

}
