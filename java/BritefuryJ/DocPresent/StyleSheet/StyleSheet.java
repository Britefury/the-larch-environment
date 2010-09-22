//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.StyleSheet;

import java.util.HashMap;

import BritefuryJ.AttributeTable.AttributeBase;
import BritefuryJ.AttributeTable.AttributeTable;
import BritefuryJ.DocPresent.Combinators.ApplyStyleSheet;
import BritefuryJ.DocPresent.Combinators.Pres;

public class StyleSheet extends AttributeTable
{
	public static StyleSheet instance = new StyleSheet();
	
	
	protected StyleSheet()
	{
		super();
	}
	
	
	protected StyleSheet newInstance()
	{
		return new StyleSheet();
	}
	
	
	public ApplyStyleSheet applyTo(Pres child)
	{
		return new ApplyStyleSheet( this, child );
	}

	public ApplyStyleSheet __call__(Pres child)
	{
		return new ApplyStyleSheet( this, child );
	}




	public StyleSheet withAttr(AttributeBase attribute, Object value)
	{
		return (StyleSheet)super.withAttr( attribute, value );
	}
	
	public StyleSheet withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return (StyleSheet)super.withAttrs( valuesMap );
	}
		
	public StyleSheet withAttrs(AttributeTable attribs)
	{
		return (StyleSheet)super.withAttrs( attribs );
	}
		
	public StyleSheet withAttrFrom(AttributeBase destAttr, AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleSheet)super.withAttrFrom( destAttr, srcTable, srcAttr );
	}
	
	public StyleSheet withAttrsFrom(AttributeTable srcTable, AttributeBase srcAttr)
	{
		return (StyleSheet)super.withAttrsFrom( srcTable, srcAttr );
	}
	
	public StyleSheet withoutAttr(AttributeBase attribute)
	{
		return (StyleSheet)super.withoutAttr( attribute );
	}
	
	public StyleSheet remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		return (StyleSheet)super.remapAttr( destAttribute, sourceAttribute );
	}
	
	
	
	public static StyleSheet fromAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return instance.withAttrs( valuesMap );
	}
}
