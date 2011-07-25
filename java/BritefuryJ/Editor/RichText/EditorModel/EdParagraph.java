//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText.EditorModel;

import java.awt.Color;
import java.util.List;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.DocPresent.Border.SolidBorder;
import BritefuryJ.Editor.RichText.Tags.PStart;
import BritefuryJ.Editor.RichText.Tags.Tag;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Paragraph;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;

public class EdParagraph extends EdAbstractText
{
	private Object styleAttrs = null;
	private boolean bSuppressNewline = false;
	
	
	public EdParagraph(List<Object> contents, Object styleAttrs)
	{
		super( contents );
		this.styleAttrs = styleAttrs;
	}
	
	
	protected void suppressNewline()
	{
		bSuppressNewline = true;
	}
	
	
	public Object getStyleAttrs()
	{
		return styleAttrs;
	}
	
	
	
	
	
	
	@Override
	protected Tag prefixTag()
	{
		return new PStart( styleAttrs );
	}


	@Override
	protected void buildTagList(List<Object> tags)
	{
		tags.add( new PStart( styleAttrs ) );
		for (Object x: contents)
		{
			if ( x instanceof EdAbstractText )
			{
				((EdNode)x).buildTagList( tags );
			}
			else
			{
				tags.add( x );
			}
		}
		if ( !bSuppressNewline )
		{
			tags.add( "\n" );
		}
	}
	
	
	

	@Override
	public boolean isParagraph()
	{
		return true;
	}


	@Override
	protected EdNode withContents(List<Object> contents)
	{
		return new EdParagraph( contents, styleAttrs );
	}

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres attrs = new Paragraph( new Object[] { Pres.coerceNonNull( this.styleAttrs ) } );
		return paraStyle.applyTo( new Border( new Column( new Object[] { presentContents(), new Box( 1.0, 1.0 ).pad( 1.0, 1.0 ).alignHExpand(), attrs } ) ) );
	}

	
	private static final StyleSheet paraStyle = StyleSheet.instance.withAttr( Primitive.border, new SolidBorder( 2.0, 2.0, new Color( 0.0f, 0.25f, 0.5f ), null ) );
}
