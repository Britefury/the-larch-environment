//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPHBox;
import BritefuryJ.DocPresent.Combinators.PresentationContext;

public class HBox extends AbstractBox
{
	public HBox(Object children[])
	{
		super( children );
	}
	
	public HBox(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPHBox element = new DPHBox( Primitive.hboxParams.get( ctx.getStyle() ) );
		element.setChildren( mapPresent( ctx.withStyle( Primitive.useHBoxParams( ctx.getStyle() ) ), children ) );
		return element;
	}
}
