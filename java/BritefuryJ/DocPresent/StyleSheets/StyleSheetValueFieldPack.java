//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

public class StyleSheetValueFieldPack extends StyleSheetValueField
{
	private ElementStyleSheetField elementField;
	private StyleSheetValueField childPackField;
	
	
	public StyleSheetValueFieldPack(String name, Class<?> valueClass, Object defaultValue, ElementStyleSheetField elementField, StyleSheetValueField childPackField)
	{
		super( name, valueClass, defaultValue );
		
		if ( !name.equals( elementField.getName() ) )
		{
			throw new RuntimeException( "Field mis-match; attempting to map an element field named \"" + elementField.getName() + "\" to a value field named \"" + name + "\"" );
		}
		
		this.elementField = elementField;
		this.childPackField = childPackField;
	}

	
	public static StyleSheetValueFieldPack newField(String name, Class<?> valueClass, Object defaultValue, ElementStyleSheetField elementField, StyleSheetValueField childPackField)
	{
		return (StyleSheetValueFieldPack)StyleSheetValues.layout.newField( new StyleSheetValueFieldPack( name, valueClass, defaultValue, elementField, childPackField ) );
	}

	
	public Object cascadeValue(StyleSheetValues parentValues, ElementStyleSheet elementSheet, boolean bUsed)
	{
		if ( elementSheet.containsKey( elementField ) )
		{
			return elementSheet.get( elementField );
		}
		else
		{
			return parentValues.get( childPackField );
		}
	}
}
