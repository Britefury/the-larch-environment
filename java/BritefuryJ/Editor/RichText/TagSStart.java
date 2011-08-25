//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;

class TagSStart extends TagStart
{
	private HashMap<Object, Object> styleAttrs = new HashMap<Object, Object>();
	
	
	public TagSStart(HashMap<Object, Object> styleAttrs)
	{
		this.styleAttrs.putAll( styleAttrs );
	}
	
	
	public Map<Object, Object> getStyleAttrs()
	{
		return styleAttrs;
	}
	
	
	@Override
	protected Pres presentTagContents()
	{
		ArrayList<Object> xs = new ArrayList<Object>();
		for (Map.Entry<Object, Object> entry: styleAttrs.entrySet())
		{
			xs.add( new Label( " " ) );
			xs.add( Pres.coerce( entry.getKey() ) );
			xs.add( new Label( "=" ) );
			xs.add( Pres.coerceNonNull( entry.getValue() ) );
		}
		return new Row( xs );
	}
	


	@Override
	protected String getTagName()
	{
		return "start";
	}
}