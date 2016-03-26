//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Editor.RichText;

import BritefuryJ.Editor.RichText.Attrs.RichTextAttributes;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;

class TagPStart extends TagStart
{
	private RichTextAttributes paraAttrs;
	
	
	public TagPStart(RichTextAttributes paraAttrs)
	{
		this.paraAttrs = paraAttrs;
	}
	
	
	public RichTextAttributes getAttrs()
	{
		return paraAttrs;
	}
	
	
	@Override
	protected Pres presentTagContents()
	{
		return new Row( new Object[] { new Label( " " ), Pres.coercePresentingNull(paraAttrs) } );
	}


	@Override
	public boolean equals(Object x)
	{
		if ( x instanceof TagPStart )
		{
			TagPStart t = (TagPStart)x;
			return paraAttrs == t.paraAttrs ||  ( paraAttrs != null  &&  t.paraAttrs != null  &&  paraAttrs.equals( t.paraAttrs) );
		}
		else
		{
			return false;
		}
	}

	
	@Override
	public String toString()
	{
		return "<p>";
	}


	@Override
	protected String getTagName()
	{
		return "p";
	}
}
