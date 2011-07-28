//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Region;
import BritefuryJ.StyleSheet.StyleSheet;

abstract class EdEmbed extends EdNode
{
	protected Object value;
	
	
	protected EdEmbed(Object value)
	{
		this.value = value;
	}
	
	
	public Object getValue()
	{
		return value;
	}
	
	
	
	@Override
	protected void buildTagList(List<Object> tags)
	{
		tags.add( this );
	}
	
	
	
	protected boolean isTextual()
	{
		return false;
	}



	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Region( _paraStyle.applyTo( new Border( value ) ) );
	}

	
	private static final StyleSheet _paraStyle = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 2.0, new Color( 0.0f, 0.5f, 0.0f ), null ) );
}
