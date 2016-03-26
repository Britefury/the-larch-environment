//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.ClipboardFilter.ClipboardCopierMemo;
import BritefuryJ.Editor.RichText.Attrs.AttrValue;
import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;
import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Table;
import BritefuryJ.StyleSheet.StyleSheet;

public class EdSpan extends EdAbstractText
{
	private RichTextAttributes spanAttrs;

	
	protected EdSpan(List<Object> contents, RichTextAttributes spanAttrs)
	{
		super( contents );
		this.spanAttrs = spanAttrs.copy();
	}
	
	protected EdSpan(RichTextAttributes spanAttrs)
	{
		super();
		this.spanAttrs = spanAttrs.copy();
	}
	
	
	public RichTextAttributes getSpanAttrs()
	{
		return spanAttrs;
	}
	
	public void setSpanAttrs(RichTextAttributes spanAttrs)
	{
		this.spanAttrs.replaceContentsWith(spanAttrs);
	}
	
	



	@Override
	protected Tag containingPrefixTag()
	{
		return new TagSStart(spanAttrs);
	}

	@Override
	protected Tag containingSuffixTag()
	{
		return new TagSEnd();
	}


	@Override
	protected void buildTagList(List<Object> tags)
	{
		tags.add( new TagSStart(spanAttrs) );
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
		return new EdSpan( copyContents( memo ), spanAttrs);
	}


	@Override
	protected Object buildModel(RichTextController controller)
	{
		return controller.buildSpan( controller.editorModelListToModelList( contents ), spanAttrs);
	}

	
	protected static Pres presentStyleAttrs(RichTextAttributes styleAttrs)
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
		return new Border( new Column( new Object[] { presentContents(), presentStyleAttrs(spanAttrs) } ) );
	}


	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof EdSpan)
		{
			EdSpan e = (EdSpan)x;
			boolean s = spanAttrs == e.spanAttrs ||  ( spanAttrs != null  &&  e.spanAttrs != null  &&  spanAttrs.equals( e.spanAttrs) );
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
		return "<SPAN " + spanAttrs + ">" + contents + "</SPAN>";
	}


	private static final StyleSheet attrStyle = StyleSheet.style( Primitive.background.as( new FillPainter( new Color( 0.85f, 0.95f, 1.0f ) ) ) );
}
