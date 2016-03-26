//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class CustomControlActionPres extends ControlPres
{
	public static interface CustomControlAction
	{
		public void action(Control control, PresentationContext ctx, StyleValues style);
	}
	
	
	private CustomControlAction action;
	private ControlPres child;
	
	
	public CustomControlActionPres(CustomControlAction action, ControlPres child)
	{
		this.action = action;
		this.child = child;
	}
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		Control control = child.createControl( ctx, style );
		action.action( control, ctx, style );
		registerControl( control );
		return control;
	}
}
