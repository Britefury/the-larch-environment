//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.ModelAccess.DocModel;

import BritefuryJ.ModelAccess.ModelReader;

public class StringToIntegerReader extends DMModelReader
{
	private ModelReader innerReader;
	private int defaultValue;
	
	
	public StringToIntegerReader(ModelReader innerReader, int defaultValue)
	{
		this.innerReader = innerReader;
		this.defaultValue = defaultValue;
	}
	
	public StringToIntegerReader(ModelReader innerReader)
	{
		this( innerReader, 0 );
	}
	
	
	
	public Object readFromModel(Object model)
	{
		Object value = innerReader.readFromModel( model );
		int v = defaultValue;
		if ( value != null  &&  value instanceof String )
		{
			try
			{
				v = Integer.valueOf( (String)value );
			}
			catch (NumberFormatException e)
			{
			}
		}
		return v;
	}
}
