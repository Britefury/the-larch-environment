//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.AttributeVisitor;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMNode;

public class PyAttributeEvaluationFunction implements AttributeEvaluationFunction
{
	private PyObject callable;
	
	
	public PyAttributeEvaluationFunction(PyObject callable)
	{
		this.callable = callable;
	}


	public Object evaluateAttribute(DMNode node)
	{
		return Py.tojava( callable.__call__( Py.java2py( node ) ), Object.class );
	}
}
