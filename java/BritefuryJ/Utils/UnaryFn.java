//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Utils;

import org.python.core.Py;
import org.python.core.PyType;

public interface UnaryFn
{
	Object invoke(Object x);

	
	
	
	public static class PyTypeTextToValue implements UnaryFn
	{
		private PyType type;
		
		public PyTypeTextToValue(PyType type)
		{
			this.type = type;
		}
		
		
		@Override
		public Object invoke(Object x)
		{
			return Py.tojava( type.__call__( Py.java2py( x ) ), Object.class );
		}
	}

	
	public static final UnaryFn identity = new UnaryFn()
	{
		@Override
		public Object invoke(Object x)
		{
			return x;
		}
	};
}