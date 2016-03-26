//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
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
		public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
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
	
	
	public CustomExpander(Object contracted, Object expanded, LiveInterface state, ExpanderListener listener)
	{
		super( state, listener );
		
		this.contracted = coerce( contracted );
		this.expanded = coerce( expanded );
	}
	
	
	public CustomExpander(Object contracted, Object expanded, LiveValue state)
	{
		super( state );
		
		this.contracted = coerce( contracted );
		this.expanded = coerce( expanded );
	}
	
	
	
	public static Pres expanderButton(Object button)
	{
		return Pres.coerce( button ).withElementInteractor( new CustomExpanderInteractor() );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		ExpandEventHandler eventHandler = new ExpandEventHandler();
		
		LiveInterface state = stateSource.getLive();
		LiveFunction contentsLive = createContentsFn( state, expanded, contracted );
		
		Pres expander = new Bin( contentsLive ).withTreeEventListener( eventHandler );
		LSBin expanderElement = (LSBin)expander.present( ctx, style );
		
		ExpanderControl control = new ExpanderControl( ctx, style, expanderElement, state, listener );
		eventHandler.control = control;
		return control;
	}
}
