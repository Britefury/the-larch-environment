//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
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
