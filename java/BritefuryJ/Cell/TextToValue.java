//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Cell;

import org.python.core.Py;
import org.python.core.PyType;

public interface TextToValue
{
	Object textToValue(String textValue);

	
	
	
	public static class PyTypeTextToValue implements TextToValue
	{
		private PyType type;
		
		public PyTypeTextToValue(PyType type)
		{
			this.type = type;
		}
		
		
		@Override
		public Object textToValue(String textValue)
		{
			return Py.tojava( type.__call__( Py.newString( textValue ) ), Object.class );
		}
	}

	
	public static final TextToValue identity = new TextToValue()
	{
		@Override
		public Object textToValue(String textValue)
		{
			return textValue;
		}
	};
}