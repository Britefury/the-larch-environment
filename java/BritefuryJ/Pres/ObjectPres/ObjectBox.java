//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.ObjectPres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.StyleSheet.StyleValues;

public class ObjectBox extends Pres
{
	private String title;
	private Pres contents;
	
	
	public ObjectBox(String title, Object contents)
	{
		this.title = title;
		this.contents = coercePresentingNull(contents);
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		double padding = style.get( ObjectPresStyle.objectContentPadding, Double.class );
		StyleValues childStyle = ObjectPresStyle.useObjectBorderAttrs( ObjectPresStyle.useObjectBoxAttrs( style ) );
		LSElement contentsElement = contents.padX( padding ).present( ctx, childStyle );
		
		Pres titlePres = new ObjectTitle( title );
		
		return new ObjectBorder( new Column( new Object[] { titlePres, contentsElement } ).alignHExpand() ).present( ctx, style );
	}
}
