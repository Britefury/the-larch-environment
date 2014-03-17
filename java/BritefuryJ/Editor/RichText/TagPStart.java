//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
