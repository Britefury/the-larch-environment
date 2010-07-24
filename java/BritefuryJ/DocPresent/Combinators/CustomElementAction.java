//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.DocPresent.DPElement;

public class CustomElementAction extends Pres
{
	private Pres child;
	private CustomAction handler;
	
	
	public CustomElementAction(Pres child, CustomAction handler)
	{
		this.handler = handler;
		this.child = child;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPElement element = child.present( ctx );
		handler.apply( element, ctx );
		return element;
	}
}
