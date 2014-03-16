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
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;
import BritefuryJ.Graphics.SolidBorder;
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
	private RichTextAttributes paraAttrs = new RichTextAttributes();
	private boolean isNewlineSuppressed = false;
	private Object model;
	
	
	protected EdParagraph(Object model, List<Object> contents, RichTextAttributes paraAttrs)
	{
		super( contents );
		if ( paraAttrs != null )
		{
			this.paraAttrs.replaceContentsWith(paraAttrs);
		}
		this.model = model;
	}
	
	
	protected void suppressNewline()
	{
		isNewlineSuppressed = true;
	}
	
	
	public RichTextAttributes getParaAttrs()
	{
		return paraAttrs;
	}
	
	public void setParaAttrs(RichTextAttributes paraAttrs)
	{
		this.paraAttrs.replaceContentsWith(paraAttrs);
	}
	
	



	@Override
	protected Tag prefixTag()
	{
		return new TagPStart(paraAttrs);
	}


	@Override
	protected void buildTagList(List<Object> tags)
	{
		tags.add( new TagPStart(paraAttrs) );
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
		if ( !isNewlineSuppressed )
		{
			tags.add( "\n" );
		}
	}
	
	
	

	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		return new EdParagraph( null, copyContents( memo ), paraAttrs);
	}


	@Override
	protected Object buildModel(RichTextController controller)
	{
		if (model != null)
		{
			return model;
		}
		else
		{
			return controller.buildParagraph( controller.editorModelListToModelList( contents ), paraAttrs);
		}
	}


	@Override
	protected boolean isParagraph()
	{
		return true;
	}

	
	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres attrs = new Paragraph( new Object[] { Pres.coercePresentingNull(this.paraAttrs) } );
		return paraStyle.applyTo( new Border( new Column( new Object[] { presentContents(), new Box( 1.0, 1.0 ).pad( 1.0, 1.0 ).alignHExpand(), attrs } ) ) );
	}


	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof EdParagraph )
		{
			EdParagraph e = (EdParagraph)x;
			boolean s = paraAttrs == e.paraAttrs ||  ( paraAttrs != null  &&  e.paraAttrs != null  &&  paraAttrs.equals( e.paraAttrs) );
			return contents.equals( e.contents )  &&  s;
		}
		else
		{
			return false;
		}
	}


	@Override
	public String toString()
	{
		return "<P " + paraAttrs + ">" + contents + "</P>";
	}


	private static final StyleSheet paraStyle = StyleSheet.style( Primitive.border.as( new SolidBorder( 2.0, 2.0, new Color( 0.0f, 0.25f, 0.5f ), null ) ) );
}
