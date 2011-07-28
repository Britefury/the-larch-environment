//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.RichText;

import java.util.Map;

import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Row;

class TagPStart extends TagStart
{
	private Map<Object, Object> styleAttrs;
	
	
	public TagPStart(Map<Object, Object> styleAttrs)
	{
		this.styleAttrs = styleAttrs;
	}
	
	
	public Map<Object, Object> getAttrs()
	{
		return styleAttrs;
	}
	
	
	@Override
	protected Pres presentTagContents()
	{
		return new Row( new Object[] { new Label( " " ), Pres.coerceNonNull( styleAttrs ) } );
	}
	


	@Override
	protected String getTagName()
	{
		return "p";
	}
}
