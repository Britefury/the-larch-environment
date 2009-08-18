//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.StyleSheets;

public class ElementStyleSheetField extends StyleSheetField
{
	protected ElementStyleSheetField(String name, Class<?> valueClass)
	{
		super( name, valueClass );
	}


	public boolean equals(Object x)
	{
		if ( x == this )
		{
			return true;
		}
		
		if ( x instanceof ElementStyleSheetField )
		{
			return super.equals( x );
		}
		else
		{
			return false;
		}
	}



	public static ElementStyleSheetField newField(String name, Class<?> valueClass)
	{
		return (ElementStyleSheetField)ElementStyleSheet.layout.newField( new ElementStyleSheetField( name, valueClass ) );
	}
}
