//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Isolation;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyTuple;

public class IslandPickleTag implements Cloneable
{
	public IslandPickleTag()
	{
	}
	
	
	public PyObject __reduce__()
	{
		return new PyTuple( Py.java2py( getClass() ), new PyTuple(), __getstate__() );
	}

	public PyObject __getstate__()
	{
		return Py.newInteger( 1 );
	}
	
	public void __setstate__(PyObject state)
	{
	}
}
