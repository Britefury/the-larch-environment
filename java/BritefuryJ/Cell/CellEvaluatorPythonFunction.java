//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.Cell;


import org.python.core.PyObject;


public class CellEvaluatorPythonFunction extends CellEvaluator
{
	private PyObject func;
	
		
	
	public CellEvaluatorPythonFunction(PyObject func)
	{
		super();
		this.func = func;
	}


	public Object evaluate()
	{
		return func.__call__();
	}
}
