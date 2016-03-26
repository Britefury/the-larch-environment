//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.ClipboardFilter;

import java.util.IdentityHashMap;

import org.python.core.Py;
import org.python.core.PyObject;

public class ClipboardCopierMemo
{
	private IdentityHashMap<Object, Object> memo;
	private ClipboardCopier copier;
	
	
	protected ClipboardCopierMemo(ClipboardCopier copier)
	{
		this.memo = new IdentityHashMap<Object, Object>();
		this.copier = copier;
	}
	
	
	public PyObject copy(PyObject x)
	{
		if ( x == null  ||  x == Py.None )
		{
			return x;
		}
		
		if ( memo.containsKey( x ) )
		{
			return (PyObject)memo.get( x );
		}
		else
		{
			PyObject copy = copier.py_createCopy( x, this );
			memo.put( x, copy );
			return copy;
		}
	}

	public Object copy(Object x)
	{
		if ( x == null )
		{
			return null;
		}
		
		if ( memo.containsKey( x ) )
		{
			return memo.get( x );
		}
		else
		{
			Object copy = copier.createCopy( x, this );
			memo.put( x, copy );
			return copy;
		}
	}
}
