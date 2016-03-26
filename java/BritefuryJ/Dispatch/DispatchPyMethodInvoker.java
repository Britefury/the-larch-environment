//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Dispatch;

import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.__builtin__;

public abstract class DispatchPyMethodInvoker
{
	protected PyObject function;
	
	
	
	public DispatchPyMethodInvoker(PyObject function)
	{
		this.function = function;
	}
	
	public abstract PyObject invoke(Object node, PyObject dispatchSelf, PyObject args[]);
	
	
	private static final PyString __name__ = Py.newString( "__name__" );
	public String getName()
	{
		return __builtin__.getattr( function, __name__ ).asString(); 
	}
}
