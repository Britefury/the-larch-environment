//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import org.python.core.Py;
import org.python.core.PyFunction;
import org.python.core.PyObject;

public class StyleSheetDerivedPyAttrFn
{
	public static class Helper
	{
		private StyleSheet self;
		private StyleSheetDerivedPyAttrFn attr;
		
		
		public Helper(StyleSheet self, StyleSheetDerivedPyAttrFn attr)
		{
			this.self = self;
			this.attr = attr;
		}


		public PyObject __call__()
		{
			if ( self.derivedAttributes.containsKey( attr ) )
			{
				return self.derivedAttributes.get( attr );
			}
			else
			{
				PyObject result = attr.fun.__call__( Py.java2py( self ) );
				self.derivedAttributes.put( attr, result );
				return result;
			}
		}
	}
	
	private PyFunction fun;
	
	
	public StyleSheetDerivedPyAttrFn(PyFunction fun)
	{
		this.fun = fun;
	}
	
	
	public Object __get__(StyleSheet instance, PyObject owner)
	{
		if ( instance != null )
		{
			return new Helper( instance, this );
		}
		else
		{
			return this;
		}
	}
}
