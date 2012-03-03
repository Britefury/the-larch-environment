//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.StyleSheet.StyleValues;

public class CustomExpander extends Expander
{
	protected static class CustomExpanderInteractor implements ClickElementInteractor
	{
		protected CustomExpanderInteractor()
		{
		}
		
		
		@Override
		public boolean testClickEvent(PointerInputElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(PointerInputElement element, PointerButtonClickedEvent event)
		{
			((LSElement)element).postTreeEvent( new ExpandEvent() );
			return true;
		}
	}

	
	private static class ExpandEvent
	{
	}
	
	private static class ExpandEventHandler implements TreeEventListener
	{
		private ExpanderControl control;
		
		@Override
		public boolean onTreeEvent(LSElement element, LSElement sourceElement, Object event)
		{
			if ( event instanceof ExpandEvent )
			{
				control.toggle();
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	
	private Pres contracted;
	private Pres expanded;

	
	public CustomExpander(Object contracted, Object expanded, boolean initialState, ExpanderListener listener)
	{
		super( initialState, listener );
		
		this.contracted = coerce( contracted );
		this.expanded = coerce( expanded );
	}
	
	public CustomExpander(Object contracted, Object expanded, boolean initialState)
	{
		this( contracted, expanded, initialState, null );
	}
	
	public CustomExpander(Object contracted, Object expanded, ExpanderListener listener)
	{
		this( contracted, expanded, false, listener );
	}
	
	public CustomExpander(Object contracted, Object expanded)
	{
		this( contracted, expanded, false, null );
	}
	
	
	
	public static Pres expanderButton(Object button)
	{
		return Pres.coerce( button ).withElementInteractor( new CustomExpanderInteractor() );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		ExpandEventHandler eventHandler = new ExpandEventHandler();
		
		Pres expander = new Bin( initialState  ?  expanded  :  contracted ).withTreeEventListener( eventHandler );
		LSBin expanderElement = (LSBin)expander.present( ctx, style );
		
		ExpanderControl control = new ExpanderControl( ctx, style, expanderElement, expanded, contracted, initialState, listener );
		eventHandler.control = control;
		return control;
	}
}
