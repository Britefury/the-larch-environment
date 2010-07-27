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
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;

public class LinkHeaderBar extends SequentialPres
{
	public LinkHeaderBar(Object children[])
	{
		super( children );
	}
	
	public LinkHeaderBar(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		Pres xs[] = mapPresentAsCombinators( ctx.withStyle( RichText.useBodyAttrs( ctx.getStyle() ) ), children );
		return higherOrderPresent( ctx, RichText.linkHeaderStyle( ctx.getStyle() ),
				new Border( new HBox( xs ).alignHRight() ).alignHExpand() );
	}
}
