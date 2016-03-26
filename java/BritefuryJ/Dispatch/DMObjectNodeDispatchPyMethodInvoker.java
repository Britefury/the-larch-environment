//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Dispatch;

import java.util.List;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.DocModel.DMObject;

public class DMObjectNodeDispatchPyMethodInvoker extends DispatchPyMethodInvoker
{
	private int indices[];
	
	
	
	public DMObjectNodeDispatchPyMethodInvoker(PyObject function, List<Integer> indices)
	{
		super( function );
		this.indices = new int[indices.size()];
		for (int i = 0; i < indices.size(); i++)
		{
			this.indices[i] = indices.get( i );
		}
	}
	
	
	@Override
	public PyObject invoke(Object node, PyObject dispatchSelf, PyObject args[])
	{
		DMObject dmNode = (DMObject)node;
		
		int numCallARgs = 1  +  args.length  +  1  +  indices.length;
		PyObject callArgs[] = new PyObject[numCallARgs];
		callArgs[0] = dispatchSelf;
		System.arraycopy( args, 0, callArgs, 1, args.length );
		callArgs[args.length+1] = Py.java2py( node );
		for (int i = 0; i < indices.length; i++)
		{
			callArgs[args.length+2+i] = Py.java2py( dmNode.get( indices[i] ) );
		}
		return function.__call__( callArgs );
	}
}
