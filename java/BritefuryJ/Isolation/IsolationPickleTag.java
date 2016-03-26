//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Isolation;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyTuple;

public class IsolationPickleTag implements Cloneable
{
	public IsolationPickleTag()
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
