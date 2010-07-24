//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable2;

public class StyleSheetValues extends AttributeTable2
{
	public static StyleSheetValues instance = new StyleSheetValues();
	
	
	protected StyleSheetValues()
	{
		super();
	}
	
	
	protected StyleSheetValues newInstance()
	{
		return new StyleSheetValues();
	}




	public StyleSheetValues withAttr(AttributeBase fieldName, Object value)
	{
		return (StyleSheetValues)super.withAttr( fieldName, value );
	}
	
	public StyleSheetValues withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return (StyleSheetValues)super.withAttrs( valuesMap );
	}
		
	public StyleSheetValues withAttrs(AttributeTable2 attribs)
	{
		return (StyleSheetValues)super.withAttrs( attribs );
	}
		
	public StyleSheetValues withoutAttr(AttributeBase fieldName)
	{
		return (StyleSheetValues)super.withoutAttr( fieldName );
	}
	
	public StyleSheetValues useAttr(AttributeBase fieldName)
	{
		return (StyleSheetValues)super.useAttr( fieldName );
	}
	
	public StyleSheetValues remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		return (StyleSheetValues)super.remapAttr( destAttribute, sourceAttribute );
	}
}
