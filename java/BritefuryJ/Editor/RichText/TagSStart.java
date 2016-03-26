//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.ArrayList;
import java.util.Map;

import BritefuryJ.Editor.RichText.Attrs.AttrValue;
import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;

class TagSStart extends TagStart
{
	private RichTextAttributes spanAttrs;
	
	
	public TagSStart(RichTextAttributes spanAttrs)
	{
		this.spanAttrs = spanAttrs;
	}
	
	
	public RichTextAttributes getSpanAttrs()
	{
		return spanAttrs;
	}
	
	
	@Override
	protected Pres presentTagContents()
	{
		ArrayList<Object> xs = new ArrayList<Object>();
		for (Map.Entry<Object, AttrValue> entry: spanAttrs.entrySet())
		{
			xs.add( new Label( " " ) );
			xs.add( Pres.coerce( entry.getKey() ) );
			xs.add( new Label( "=" ) );
			xs.add( Pres.coercePresentingNull(entry.getValue()) );
		}
		return new Row( xs );
	}
	
	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof TagSStart )
		{
			return spanAttrs.equals( ((TagSStart)x).spanAttrs);
		}
		else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return "<style " + spanAttrs + ">";
	}
	


	@Override
	protected String getTagName()
	{
		return "start";
	}
}
