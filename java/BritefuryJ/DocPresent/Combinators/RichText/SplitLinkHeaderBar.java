//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.RichText;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;

public class SplitLinkHeaderBar extends Pres
{
	protected Pres leftChildren[], rightChildren[];
	
	
	public SplitLinkHeaderBar(Object leftChildren[], Object rightChildren[])
	{
		this.leftChildren = mapCoerce( leftChildren );
		this.rightChildren = mapCoerce( rightChildren );
	}
	
	public SplitLinkHeaderBar(List<Object> leftChildren, List<Object> rightChildren)
	{
		this.leftChildren = mapCoerce( leftChildren );
		this.rightChildren = mapCoerce( rightChildren );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		Pres left[] = mapPresentAsCombinators( ctx.withStyle( RichText.useBodyAttrs( ctx.getStyle() ) ), leftChildren );
		Pres right[] = mapPresentAsCombinators( ctx.withStyle( RichText.useBodyAttrs( ctx.getStyle() ) ), rightChildren );
		return higherOrderPresent( ctx, RichText.linkHeaderStyle( ctx.getStyle() ),
				new Border( new HBox( new Pres[] { new HBox( left ).alignHLeft(), new HBox( right ).alignHRight() } ).alignHExpand()).alignHExpand() );
	}
}
