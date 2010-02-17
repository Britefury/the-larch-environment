//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocPresent.StyleSheet.StyleSheet;

public class PyElementFactory implements ElementFactory
{
	private PyObject callable;
	
	public PyElementFactory(PyObject callable)
	{
		this.callable = callable;
	}
	
	
	public DPWidget createElement(StyleSheet styleSheet)
	{
		return (DPWidget)Py.tojava( callable.__call__( Py.java2py( styleSheet ) ), DPWidget.class );
	}
	
	
	public static ElementFactory pyToElementFactory(PyObject x)
	{
		if ( x == Py.None )
		{
			return null;
		}
		else
		{
			return new PyElementFactory( x );
		}
	}
}
