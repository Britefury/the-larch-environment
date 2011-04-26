//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.RichText;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.SequentialPres;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleValues;

public class Page extends SequentialPres
{
	public Page(Object children[])
	{
		super( children );
	}
	
	public Page(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement xs[] = mapPresent( ctx, RichText.usePageAttrs( style ).withAttr( Primitive.hAlign, HAlignment.EXPAND ).withAttr( Primitive.vAlign, VAlignment.REFY ), children );
		return RichText.pageStyle( style ).applyTo( new Column( xs ).alignHExpand().alignVRefY() ).present( ctx, style );
	}
}
