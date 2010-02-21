//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet;
import BritefuryJ.GSym.IncrementalContext.GSymIncrementalNodeContext;

public class PyGSymViewFragmentFunction implements GSymViewFragmentFunction
{
	private PyObject callable;
	
	
	public PyGSymViewFragmentFunction(PyObject callable)
	{
		this.callable = callable;
	}
	
	
	public int hashCode()
	{
		return callable.hashCode();
	}


	public DPWidget createViewFragment(DMNode x, GSymIncrementalNodeContext ctx, StyleSheet styleSheet, Object state)
	{
		return Py.tojava( callable.__call__( Py.java2py( x ), Py.java2py( ctx ), Py.java2py( styleSheet ), Py.java2py( state ) ), DPWidget.class );
	}
}