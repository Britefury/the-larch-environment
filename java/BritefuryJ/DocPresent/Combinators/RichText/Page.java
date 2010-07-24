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
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;

public class Page extends Pres
{
	private Pres children[];
	
	
	public Page(Object children[])
	{
		this.children = mapCoerce( children );
	}
	
	public Page(List<Object> children)
	{
		this.children = mapCoerce( children );
	}

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPElement childElems[] = mapPresent( ctx.withStyle( RichText.usePageAttrs( ctx.getStyle() ) ), children );
		return new VBox( childElems ).present( ctx.withStyle( RichText.pageStyle( ctx.getStyle() ) ) );
	}
}
