//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import org.python.core.Py;
import org.python.core.PyObject;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class PyPresCombinatorFn
{
	public static class PyPresCombinatorFnImpl extends Pres
	{
		private PyObject callable;
		private PyObject args[];
		
		
		public PyPresCombinatorFnImpl(PyObject callable, PyObject args[])
		{
			this.callable = callable;
			this.args = args;
		}
		
		
		@Override
		public LSElement present(PresentationContext ctx, StyleValues style)
		{
			PyObject callArgs[] = new PyObject[args.length + 2];
			callArgs[0] = Py.java2py( ctx );
			callArgs[1] = Py.java2py( style );
			System.arraycopy( args, 0, callArgs, 2, args.length );
			PyObject result = callable.__call__( callArgs );
			return Py.tojava( result, LSElement.class );
		}
	}


	private PyObject callable;
	
	
	public PyPresCombinatorFn(PyObject callable)
	{
		this.callable = callable;
	}


	public PyPresCombinatorFnImpl __call__(PyObject values[])
	{
		return new PyPresCombinatorFnImpl( callable, values );
	}
}	

