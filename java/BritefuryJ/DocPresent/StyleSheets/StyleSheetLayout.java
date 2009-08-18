//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

import java.util.ArrayList;
import java.util.HashMap;

class StyleSheetLayout<FieldType extends StyleSheetField>
{
	protected ArrayList<FieldType> fields;
	protected HashMap<String, FieldType> nameToField;
	
	
	protected StyleSheetLayout()
	{
		fields = new ArrayList<FieldType>();
		nameToField = new HashMap<String, FieldType>();
	}
	
	
	protected StyleSheetField newField(FieldType newField)
	{
		FieldType field = nameToField.get( newField.getName() );
		if ( field == null )
		{
			field = newField;
			int index = fields.size();
			fields.add( field );
			nameToField.put( field.getName(), field );
			field.register( index );
			
			return field;
		}
		else
		{
			if ( field.equals( newField ) )
			{
				return field;
			}
			else
			{
				throw new RuntimeException( "A style-sheet field named \"" + field.getName() + "\" has already been registered with a different type (" + field.getValueClass().getName() + ")" );
			}
		}
	}
}
