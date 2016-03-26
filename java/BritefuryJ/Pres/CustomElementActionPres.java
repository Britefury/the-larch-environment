//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Pres;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class CustomElementActionPres extends Pres
{
	public static interface CustomElementAction
	{
		public void action(LSElement element, PresentationContext ctx, StyleValues style);
	}
	
	
	private CustomElementAction action;
	private Pres child;
	
	
	public CustomElementActionPres(CustomElementAction action, Object child)
	{
		this.action = action;
		this.child = coerce( child );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		action.action( element, ctx, style );
		return element;
	}
}
