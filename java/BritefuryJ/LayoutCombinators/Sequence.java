//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LayoutCombinators;

import org.python.core.Py;
import org.python.core.PyObject;

abstract class Sequence extends LayoutCombinator
{
	protected Object children[];
	
	
	public Sequence(Object children[])
	{
		this.children = children;
	}
	
	public Sequence(PyObject values[])
	{
		children = new Object[values.length];
		for (int i = 0; i < values.length; i++)
		{
			children[i] = Py.tojava( values[i], Object.class );
		}
	}
}
