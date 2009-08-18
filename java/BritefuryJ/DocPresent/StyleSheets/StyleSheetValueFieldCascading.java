//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

public class StyleSheetValueFieldCascading extends StyleSheetValueField
{
	private ElementStyleSheetField elementField;
	
	
	public StyleSheetValueFieldCascading(String name, Class<?> valueClass, Object defaultValue, ElementStyleSheetField elementField)
	{
		super( name, valueClass, defaultValue );
		
		if ( !name.equals( elementField.getName() ) )
		{
			throw new RuntimeException( "Field mis-match; attempting to map an element field named \"" + elementField.getName() + "\" to a value field named \"" + name + "\"" );
		}
		
		this.elementField = elementField;
	}

	
	public static StyleSheetValueFieldCascading newField(String name, Class<?> valueClass, Object defaultValue, ElementStyleSheetField elementField)
	{
		return (StyleSheetValueFieldCascading)StyleSheetValues.layout.newField( new StyleSheetValueFieldCascading( name, valueClass, defaultValue, elementField ) );
	}

	
	public Object cascadeValue(StyleSheetValues parentValues, ElementStyleSheet elementSheet, boolean bUsed)
	{
		if ( elementSheet.containsKey( elementField ) )
		{
			return elementSheet.get( elementField );
		}
		else
		{
			return parentValues.get( this );
		}
	}
}
