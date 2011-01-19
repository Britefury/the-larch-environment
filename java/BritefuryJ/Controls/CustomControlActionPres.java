//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
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
