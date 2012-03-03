//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.util.WeakHashMap;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class ControlPres extends Pres
{
	private WeakHashMap<LSElement, Control> controls = new WeakHashMap<LSElement, Control>();

	
	public static abstract class Control
	{
		protected PresentationContext ctx;
		protected StyleValues style;
		
		
		public Control(PresentationContext ctx, StyleValues style)
		{
			this.ctx = ctx;
			this.style = style;
		}
		
		public abstract LSElement getElement();
	}
	
	
	
	public static interface LiveSource
	{
		public LiveInterface getLive();
	}
	
	public static class LiveSourceRef implements LiveSource
	{
		private LiveInterface live;
		
		public LiveSourceRef(LiveInterface live)
		{
			this.live = live;
		}

		@Override
		public LiveInterface getLive()
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
		public LiveInterface getLive()
		{
			return new LiveValue( value );
		}
	}
	
	
	
	
	public CustomControlActionPres withCustomControlAction(CustomControlActionPres.CustomControlAction action)
	{
		return new CustomControlActionPres( action, this );
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		Control control = createControl( ctx, style );
		registerControl( control );
		return control.getElement();
	}
	
	
	public Control getControlForElement(LSElement element)
	{
		return controls.get( element );
	}
	
	
	protected void registerControl(Control control)
	{
		LSElement element = control.getElement();
		controls.put( element, control );
	}
	
	
	public abstract Control createControl(PresentationContext ctx, StyleValues style);
}
