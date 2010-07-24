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
import BritefuryJ.DocPresent.Combinators.ApplyStyleSheet;
import BritefuryJ.DocPresent.Combinators.Pres;

public class StyleSheet2 extends AttributeTable2
{
	public static StyleSheet2 instance = new StyleSheet2();
	
	
	protected StyleSheet2()
	{
		super();
	}
	
	
	protected StyleSheet2 newInstance()
	{
		return new StyleSheet2();
	}
	
	
	public ApplyStyleSheet applyTo(Pres child)
	{
		return new ApplyStyleSheet( this, child );
	}




	public StyleSheet2 withAttr(AttributeBase attribute, Object value)
	{
		return (StyleSheet2)super.withAttr( attribute, value );
	}
	
	public StyleSheet2 withAttrs(HashMap<AttributeBase, Object> valuesMap)
	{
		return (StyleSheet2)super.withAttrs( valuesMap );
	}
		
	public StyleSheet2 withAttrs(AttributeTable2 attribs)
	{
		return (StyleSheet2)super.withAttrs( attribs );
	}
		
	public StyleSheet2 withoutAttr(AttributeBase attribute)
	{
		return (StyleSheet2)super.withoutAttr( attribute );
	}
	
	public StyleSheet2 remapAttr(AttributeBase destAttribute, AttributeBase sourceAttribute)
	{
		return (StyleSheet2)super.remapAttr( destAttribute, sourceAttribute );
	}
}
