//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.GSym.View.ListView;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.ElementContext;

public class PySeparatorElementFactory implements SeparatorElementFactory
{
	private PyObject callable;
	
	public PySeparatorElementFactory(PyObject callable)
	{
		this.callable = callable;
	}
	
	
	public DPWidget createElement(ElementContext ctx, int index, DPWidget child)
	{
		return (DPWidget)Py.tojava( callable.__call__( Py.java2py( ctx ), Py.java2py( index ), Py.java2py( child ) ), DPWidget.class );
	}
	
	
	public static SeparatorElementFactory pyToSeparatorElementFactory(PyObject x)
	{
		if ( x == Py.None )
		{
			return null;
		}
		else
		{
			return new PySeparatorElementFactory( x );
		}
	}
}
