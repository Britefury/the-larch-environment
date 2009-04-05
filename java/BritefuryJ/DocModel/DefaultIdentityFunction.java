//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocModel;

import java.util.ArrayList;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyUnicode;

public class DefaultIdentityFunction
{
	private static class PyTransformationFunction implements TransformationFunction
	{
		private PyObject callable;
		
		
		public PyTransformationFunction(PyObject callable)
		{
			this.callable = callable;
		}
		
		
		public Object apply(Object x)
		{
			return Py.tojava( callable.__call__( Py.java2py( x ) ), Object.class );
		}
	}
	
	
	
	public DefaultIdentityFunction()
	{
	}
	
	

	
	public DMObject __call__(DMObject x, TransformationFunction xform)
	{
		Object[] fieldData = x.getFieldValuesImmutable();
		Object[] newData = new Object[fieldData.length];
		for (int i = 0; i < fieldData.length; i++)
		{
			newData[i] = applyToChild( fieldData[i], xform );
		}
		return new DMObject( x.getDMClass(), newData );
	}
	
	public DMObject __call__(DMObject x, PyObject callable)
	{
		return __call__( x, new PyTransformationFunction( callable ) );
	}
	
	
	private Object applyToChild(Object x, TransformationFunction xform)
	{
		if ( x instanceof String  ||  x instanceof PyString  ||  x instanceof PyUnicode )
		{
			return x;
		}
		else if ( x instanceof DMList )
		{
			return applyToChild( (DMList)x, xform );
		}
		else if ( x instanceof DMObject )
		{
			return xform.apply( x );
		}
		else
		{
			throw new RuntimeException( "Could not transform node of type " + x.getClass().getName() );
		}
	}
	
	private Object applyToChild(DMList xs, TransformationFunction xform)
	{
		ArrayList<Object> values = new ArrayList<Object>();
		values.ensureCapacity( xs.size() );
		for (Object x: xs)
		{
			values.add( applyToChild( x, xform ) );
		}
		return new DMList( values );
	}
}
