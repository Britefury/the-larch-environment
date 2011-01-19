//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.StyleSheet.StyleValues;

public class CustomElementActionPres extends Pres
{
	public static interface CustomElementAction
	{
		public void action(DPElement element, PresentationContext ctx, StyleValues style);
	}
	
	
	private CustomElementAction action;
	private Pres child;
	
	
	public CustomElementActionPres(CustomElementAction action, Object child)
	{
		this.action = action;
		this.child = coerce( child );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		action.action( element, ctx, style );
		return element;
	}
}
