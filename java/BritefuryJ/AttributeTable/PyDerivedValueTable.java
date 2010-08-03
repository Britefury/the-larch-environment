//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.AttributeTable;

import org.python.core.Py;
import org.python.core.PyObject;

public class PyDerivedValueTable
{
	public static class PyDerviedValueTableImpl extends DerivedValueTable<Object>
	{
		private PyObject callable;
		
		
		public PyDerviedValueTableImpl(AttributeNamespace namespace, PyObject callable)
		{
			super( namespace );
			
			this.callable = callable;
		}


		@Override
		protected Object evaluate(AttributeTable attribs)
		{
			PyObject result = callable.__call__( Py.java2py( attribs ) );
			return Py.tojava( result, Object.class );
		}
	}
	
	
	private AttributeNamespace namespace;
	
	public PyDerivedValueTable(AttributeNamespace namespace)
	{
		this.namespace = namespace;
	}
	
	
	public PyDerviedValueTableImpl __call__(PyObject callable)
	{
		return new PyDerviedValueTableImpl( namespace, callable );
	}
}
