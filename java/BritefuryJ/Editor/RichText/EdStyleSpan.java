//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.Editor.RichText.SpanAttrs.AttrValue;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.StyleSheet.StyleSheet;

public class EdStyleSpan extends EdAbstractText
{
	private SpanAttributes styleAttrs;

	
	protected EdStyleSpan(List<Object> contents, SpanAttributes styleAttrs)
	{
		super( contents );
		this.styleAttrs = styleAttrs.copy();
	}
	
	protected EdStyleSpan(SpanAttributes styleAttrs)
	{
		super();
		this.styleAttrs = styleAttrs.copy();
	}
	
	
	public SpanAttributes getStyleAttrs()
	{
		return styleAttrs;
	}
	
	public void setStyleAttrs(SpanAttributes styleAttrs)
	{
		this.styleAttrs.replaceContentsWith(styleAttrs);
	}
	
	



	@Override
	protected Tag containingPrefixTag()
	{
		return new TagSStart( styleAttrs );
	}

	@Override
	protected Tag containingSuffixTag()
	{
		return new TagSEnd();
	}


	@Override
	protected void buildTagList(List<Object> tags)
	{
		tags.add( new TagSStart( styleAttrs ) );
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
		tags.add( new TagSEnd() );
	}

	
	
	@Override
	public Object clipboardCopy(ClipboardCopierMemo memo)
	{
		return new EdStyleSpan( copyContents( memo ), styleAttrs );
	}


	@Override
	protected Object buildModel(RichTextController controller)
	{
		return controller.buildSpan( controller.editorModelListToModelList( contents ), styleAttrs );
	}

	
	protected static Pres presentStyleAttrs(SpanAttributes styleAttrs)
	{
		ArrayList<Pres[]> tableContents = new ArrayList<Pres[]>();
		for (Map.Entry<Object, AttrValue> e: styleAttrs.entrySet())
		{
			tableContents.add( new Pres[] { Pres.coerce( e.getKey() ), Pres.coerce( e.getValue() ) } );
		}
		Pres[][] tabCells = tableContents.toArray( new Pres[tableContents.size()][] );
		return attrStyle.applyTo( new Table( tabCells ) );
	}

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		return new Border( new Column( new Object[] { presentContents(), presentStyleAttrs( styleAttrs ) } ) );
	}


	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof EdStyleSpan )
		{
			EdStyleSpan e = (EdStyleSpan)x;
			boolean s = styleAttrs == e.styleAttrs  ||  ( styleAttrs != null  &&  e.styleAttrs != null  &&  styleAttrs.equals( e.styleAttrs ) );
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
		return "<SPAN " + styleAttrs + ">" + contents + "</SPAN>";
	}


	private static final StyleSheet attrStyle = StyleSheet.style( Primitive.background.as( new FillPainter( new Color( 0.85f, 0.95f, 1.0f ) ) ) );
}
