//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class Expander extends ControlPres
{
	public static interface ExpanderListener
	{
		public void onExpander(ExpanderControl expander, boolean expanded);
	}

	public static class ExpanderControl extends Control
	{
		private LSElement element;
		private LiveInterface state;
		private ExpanderListener listener;
		
		
		protected ExpanderControl(PresentationContext ctx, StyleValues style, LSElement element, LiveInterface state, ExpanderListener listener)
		{
			super( ctx, style );
			this.element = element;
			this.state = state;
			this.listener = listener;
		}
		
		
		
		
		@Override
		public LSElement getElement()
		{
			return element;
		}
		
		
		
		public void toggle()
		{
			setState( !getState() );
		}
		
		
		public boolean getState()
		{
			return (Boolean)state.getStaticValue();
		}
		
		public void setState(boolean state)
		{
			boolean currentState = getState();
			if ( state != currentState )
			{
				if ( listener != null )
				{
					listener.onExpander( this, state );
				}
			}
		}
	}

	
	
	private static class CommitListener implements ExpanderListener
	{
		private LiveValue value;
		private ExpanderListener listener;
		
		public CommitListener(LiveValue value, ExpanderListener listener)
		{
			this.value = value;
			this.listener = listener;
		}
		
		@Override
		public void onExpander(ExpanderControl expander, boolean expanded)
		{
			if ( listener != null )
			{
				listener.onExpander( expander, expanded );
			}
			value.setLiteralValue( expanded );
		}
	}
	
	

	protected LiveSource stateSource;
	protected ExpanderListener listener;

	
	private Expander(LiveSource stateSource, ExpanderListener listener)
	{
		super();

		this.stateSource = stateSource;
		this.listener = listener;
	}


	
	public Expander(boolean initialState, ExpanderListener listener)
	{
		LiveValue value = new LiveValue( initialState );

		this.stateSource = new LiveSourceRef( value );
		this.listener = new CommitListener( value, listener );
	}

	public Expander(LiveInterface state, ExpanderListener listener)
	{
		this( new LiveSourceRef( state ), listener );
	}

	public Expander(LiveValue state)
	{
		this( new LiveSourceRef( state ), new CommitListener( state, null ) );
	}
	
	
	
	protected LiveFunction createContentsFn(final LiveInterface state, final Pres expanded, final Pres contracted)
	{
		LiveFunction.Function contentsLiveFn = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				boolean val = (Boolean)state.getValue();
				return val  ?  expanded  :  contracted;
			}
		};
		
		LiveFunction contentsLive = new LiveFunction( contentsLiveFn );
		
		return contentsLive;
	}
}