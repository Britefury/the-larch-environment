//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

public abstract class StyleSheetValueField extends StyleSheetField
{
	protected Object defaultValue;
	
	
	public StyleSheetValueField(String name, Class<?> valueClass, Object defaultValue)
	{
		super( name, valueClass );
		
		if ( defaultValue != null  &&  !valueClass.isInstance( defaultValue ) )
		{
			throw new ValueTypeException( "StyleSheetValueField#<init>(): default value is of incorrect type" );
		}
		
		this.defaultValue = defaultValue;
	}
	
	
	public Object getDefaultValue()
	{
		return defaultValue;
	}
	
	
	public abstract Object cascadeValue(StyleSheetValues parentValues, ElementStyleSheet elementSheet, boolean bUsed);
	
	public Object packingContainerCascadeValue(StyleSheetValues parentValues, ElementStyleSheet elementSheet, boolean bUsed)
	{
		return cascadeValue( parentValues, elementSheet, bUsed );
	}
}
