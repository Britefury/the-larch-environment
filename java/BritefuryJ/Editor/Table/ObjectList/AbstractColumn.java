//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table.ObjectList;

import org.python.core.Py;

import BritefuryJ.Utils.UnaryFn;

public abstract class AbstractColumn
{
	public abstract Object get(Object modelRow);
	public abstract void set(Object modelRow, Object value);
	
	
	public abstract Object defaultValue();
	public abstract Object convertValue(Object x);
	
	
	public abstract Object presentHeader();
	
	
	protected static AbstractColumn coerce(Object x)
	{
		if ( x instanceof AbstractColumn )
		{
			return (AbstractColumn)x;
		}
		else if ( x instanceof String )
		{
			String sx = (String)x;
			return new AttributeColumn( sx, Py.newString( sx ) );
		}
		else
		{
			throw new RuntimeException( "Could not coerce type " + x.getClass().getName() + " to create an AbstractColumn" );
		}
	}
	
	
	protected UnaryFn createConversionFn()
	{
		UnaryFn fn = new UnaryFn()
		{
			@Override
			public Object invoke(Object x)
			{
				return AbstractColumn.this.convertValue( x );
			}
			
		};
		return fn;
	}
}
