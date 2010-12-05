//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.ModelAccess.DocModel;

import BritefuryJ.DocModel.DMObject;

public class ObjectFieldReader extends DMModelReader
{
	private String fieldName;
	
	
	public ObjectFieldReader(String fieldName)
	{
		this.fieldName = fieldName;
	}
	
	
	@Override
	public Object readFromModel(Object model)
	{
		if ( model instanceof DMObject )
		{
			DMObject o = (DMObject)model;
			return o.get( fieldName );
		}
		else
		{
			throw new RuntimeException( "ObjectFieldReader cannot read from objects that are not instances of DMObject" );
		}
	}
}
