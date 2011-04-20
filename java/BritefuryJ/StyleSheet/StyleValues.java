//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.StyleSheet;

import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.Pres.ApplyStyleSheetValues;
import BritefuryJ.Pres.Pres;

public class StyleValues extends AttributeTable
{
	protected StyleValues()
	{
		super();
	}
	
	
	protected StyleValues newInstance()
	{
		return new StyleValues();
	}
	
	
	public ApplyStyleSheetValues applyTo(Pres child)
	{
		return new ApplyStyleSheetValues( this, child );
	}




	public StyleValues withAttr(AttributeBase fieldName, Object value)
	{
		return (StyleValues)super.withAttr( fieldName, value );
	}
	
	public StyleValues withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return (StyleValues)super.withAttrs( valuesMap );
	}
		
	public StyleValues withAttrs(AttributeTable attribs)
	{
		return (StyleValues)super.withAttrs( attribs );
	}
		
	public StyleValues withAttrFrom(AttributeBase destAttr, AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleValues)super.withAttrFrom( destAttr, srcTable, srcAttr );
	}
	
	public StyleValues withAttrsFrom(AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleValues)super.withAttrsFrom( srcTable, srcAttr );
	}
	
	public StyleValues withoutAttr(AttributeBase fieldName)
	{
		return (StyleValues)super.withoutAttr( fieldName );
	}
	
	public StyleValues useAttr(AttributeBase fieldName)
	{
		return (StyleValues)super.useAttr( fieldName );
	}
	
	public StyleValues remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		return (StyleValues)super.remapAttr( destAttribute, sourceAttribute );
	}



	public static final StyleValues instance = new StyleValues();
}
