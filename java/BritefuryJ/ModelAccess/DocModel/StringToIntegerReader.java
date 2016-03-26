//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
