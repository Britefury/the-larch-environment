//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.util.WeakHashMap;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.IncrementalUnit.LiteralUnit;
import BritefuryJ.IncrementalUnit.UnitInterface;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class ControlPres extends Pres
{
	private WeakHashMap<DPElement, Control> controls = new WeakHashMap<DPElement, Control>();

	
	public static abstract class Control
	{
		protected PresentationContext ctx;
		protected StyleValues style;
		
		
		public Control(PresentationContext ctx, StyleValues style)
		{
			this.ctx = ctx;
			this.style = style;
		}
		
		public abstract DPElement getElement();
	}
	
	
	
	public static interface LiveSource
	{
		public UnitInterface getLive();
	}
	
	public static class LiveSourceRef implements LiveSource
	{
		private UnitInterface live;
		
		public LiveSourceRef(UnitInterface live)
		{
			this.live = live;
		}

		@Override
		public UnitInterface getLive()
		{
			return live;
		}
	}
	
	public static class LiveSourceValue implements LiveSource
	{
		private Object value;
		
		public LiveSourceValue(Object value)
		{
			this.value = value;
		}

		@Override
		public UnitInterface getLive()
		{
			return new LiteralUnit( value );
		}
	}
	
	
	
	
	public CustomControlActionPres withCustomControlAction(CustomControlActionPres.CustomControlAction action)
	{
		return new CustomControlActionPres( action, this );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		Control control = createControl( ctx, style );
		registerControl( control );
		return control.getElement();
	}
	
	
	public Control getControlForElement(DPElement element)
	{
		return controls.get( element );
	}
	
	
	protected void registerControl(Control control)
	{
		DPElement element = control.getElement();
		controls.put( element, control );
	}
	
	
	public abstract Control createControl(PresentationContext ctx, StyleValues style);
}
