//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.IsolationSerialize;

import org.python.core.Py;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyTuple;

import BritefuryJ.DocModel.DMPickleHelper;

public class IsolationBarrier
{
	protected static IslandPickler islandPickler;
	protected static IslandUnpickler islandUnpickler;
	
	
	private Object value = null;
	private transient IslandUnpickler unpickler = null;
	private int index = -1;
	
	
	public IsolationBarrier(Object value)
	{
		this.value = value;
	}
	
	
	public PyObject __getstate__()
	{
		if ( islandPickler != null )
		{
			return Py.newInteger( islandPickler.isolatedValue( value ) + 1 );
		}
		else
		{
			throw Py.TypeError( "No island pickler available" );
		}
	}
	
	public void __setstate__(PyObject state)
	{
		if ( islandUnpickler != null )
		{
			if ( state instanceof PyInteger )
			{
				value = null;
				unpickler = islandUnpickler;
				index = state.asInt() - 1;
			}
			else
			{
				throw Py.TypeError( "Pickle state should be a Python integer" );
			}
		}
		else
		{
			throw Py.TypeError( "No island unpickler available" );
		}
	}
	
	public PyObject __reduce__()
	{
		//return new PyTuple( getPyFactory(), new PyTuple(), __getstate__() );
		return null;
	}

	
	
	public Object getValue()
	{
		if ( index != -1 )
		{
			value = unpickler.getIsolatedValue( index );
			unpickler = null;
			index = -1;
		}
		return value;
	}
}
