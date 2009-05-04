//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Transformation;

import org.python.core.Py;
import org.python.core.PyObject;

public abstract class TransformationFunction
{
	protected static class PyTransformationFunction extends TransformationFunction
	{
		private PyObject callable;
		
		
		public PyTransformationFunction(PyObject callable)
		{
			this.callable = callable;
		}
		
		
		public Object apply(Object x, TransformationFunction innerNodeXform)
		{
			return Py.tojava( callable.__call__( Py.java2py( x ), Py.java2py( innerNodeXform ) ), Object.class );
		}
	}
	
	

	
	public static class CannotApplyTransformationValue
	{
	}
	
	public static CannotApplyTransformationValue cannotApplyTransformationValue = new CannotApplyTransformationValue();
	

	
	
	
	
	public abstract Object apply(Object x, TransformationFunction innerNodeXform);
	
	
	
	public Object __call__(Object x, TransformationFunction innerNodeXform)
	{
		return apply( x, innerNodeXform );
	}

	public Object __call__(Object x, PyObject innerNodeXform)
	{
		return apply( x, new PyTransformationFunction( innerNodeXform ) );
	}
}
