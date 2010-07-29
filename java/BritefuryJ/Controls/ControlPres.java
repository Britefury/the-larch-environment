//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.util.WeakHashMap;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;

public abstract class ControlPres extends Pres
{
	private WeakHashMap<DPElement, Control> controls = new WeakHashMap<DPElement, Control>();

	
	public static abstract class Control
	{
		protected PresentationContext ctx;
		
		
		public Control(PresentationContext ctx)
		{
			this.ctx = ctx;
		}
		
		public abstract DPElement getElement();
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		Control control = createControl( ctx );
		DPElement element = control.getElement();
		controls.put( element, control );
		return element;
	}
	
	
	public Control getControlForElement(DPElement element)
	{
		return controls.get( element );
	}
	
	
	public abstract Control createControl(PresentationContext ctx);
}
