//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.List;

import org.python.core.Py;
import org.python.core.PyJavaType;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;

public class DMNode
{
	public Object coerce(String x)
	{
		// Create a clone of the string to ensure that all String objects in the document are
		// distinct, even if their contents are the same
		return new String( x );
	}
	
	public Object coerce(PyString x)
	{
		return coerce( x.toString() );
	}
	
	public Object coerce(PyUnicode x)
	{
		return coerce( x.toString() );
	}
	
	public Object coerce(List<Object> x)
	{
		return new DMList( x );
	}
	
	@SuppressWarnings("unchecked")
	public Object coerce(Object x)
	{
		if ( x instanceof DMNode )
		{
			return x;
		}
		else if ( x instanceof PyString )
		{
			return coerce( (PyString)x );
		}
		else if ( x instanceof PyUnicode )
		{
			return coerce( (PyUnicode)x );
		}
		else if ( x instanceof String )
		{
			return coerce( (String)x );
		}
		else if ( x instanceof List )
		{
			return coerce( (List<Object>)x );
		}
		else if ( x instanceof PyJavaType )
		{
			return coerce( Py.tojava( (PyObject)x, Object.class ) );
		}
		else
		{
			System.out.println( "DMNode.coerce(): attempted to coerce " + x.getClass().getName() + " (" + x.toString() + ")" );
			//throw new RuntimeException();
			return x;
		}
	}
	
	
	
	public static boolean isNull(Object x)
	{
		return "<null>".equals( x );
	}
	
	public static String newNull()
	{
		return new String( "<null>" );
	}
}
