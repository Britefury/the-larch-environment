//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSBin;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.*;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.StyleSheet.StyleValues;

public class ToggleButton extends ControlPres{
	public static interface ToggleButtonListener
	{
		public void onToggle(ToggleButtonControl checkbox, boolean state);
	}


	public static class ToggleButtonControl extends Control implements IncrementalMonitorListener
	{
		protected class ToggleBoxInteractor implements ClickElementInteractor
		{
			@Override
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				toggle();
				return true;
			}
		}


		private LSBin element;
		Pres inactive, active;
		private LiveInterface state;
		private boolean currentState;
		private ToggleButtonListener listener;


		protected ToggleButtonControl(PresentationContext ctx, StyleValues style, LSBin element, Pres inactive, Pres active, LiveInterface state, ToggleButtonListener listener)
		{
			super( ctx, style );

			this.element = element;
			this.element.addElementInteractor( new ToggleBoxInteractor() );
			this.inactive = inactive;
			this.active = active;
			this.state = state;
			this.state.addListener( this );
			this.listener = listener;
			this.currentState = (Boolean)state.getStaticValue();
			element.setFixedValue( state.elementValueFunction() );
		}



		@Override
		public LSElement getElement()
		{
			return element;
		}


		public boolean getState()
		{
			return (Boolean)state.getStaticValue();
		}



		protected void toggle()
		{
			boolean value = (Boolean)state.getStaticValue();
			if ( listener != null )
			{
				listener.onToggle(this, !value);
			}
		}



		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			// Use getValue() so that @state reports further value changes
			boolean value = (Boolean)state.getValue();

			if (value != currentState) {
				currentState = value;
				element.setFixedValue( value );

				Pres c = value ?  active  :  inactive;
				LSElement contents = c.present(ctx, style);
				element.setChild(contents);
			}
		}


	}



	private static class CommitListener implements ToggleButtonListener
	{
		private LiveValue value;
		private ToggleButtonListener listener;

		public CommitListener(LiveValue value, ToggleButtonListener listener)
		{
			this.value = value;
			this.listener = listener;
		}

		@Override
		public void onToggle(ToggleButtonControl checkbox, boolean state)
		{
			if ( listener != null )
			{
				listener.onToggle(checkbox, state);
			}
			value.setLiteralValue( state );
		}
	}



	private Pres inactiveChild, activeChild;
	private LiveSource valueSource;
	private ToggleButtonListener listener;


	private ToggleButton(Object inactiveChild, Object activeChild, LiveSource valueSource, ToggleButtonListener listener)
	{
		this.inactiveChild = coerce( inactiveChild );
		this.activeChild = coerce( activeChild );
		this.valueSource = valueSource;
		this.listener = listener;
	}


	public ToggleButton(Object inactiveChild, Object activeChild, boolean initialState, ToggleButtonListener listener)
	{
		LiveValue value = new LiveValue( initialState );

		this.inactiveChild = coerce( inactiveChild );
		this.activeChild = coerce( activeChild );
		this.valueSource = new LiveSourceRef( value );
		this.listener = new CommitListener( value, listener );
	}

	public ToggleButton(Object inactiveChild, Object activeChild, LiveInterface value, ToggleButtonListener listener)
	{
		this( inactiveChild, activeChild, new LiveSourceRef( value ), listener );
	}

	public ToggleButton(Object inactiveChild, Object activeChild, LiveValue value)
	{
		this( inactiveChild, activeChild, new LiveSourceRef( value ), new CommitListener( value, null ) );
	}


	public static ToggleButton toggleButtonWithLabel(String labelText, boolean state, ToggleButtonListener listener)
	{
		Label label = new Label(labelText);
		return new ToggleButton( label, label, state, listener );
	}

	public static ToggleButton toggleButtonWithLabel(String labelText, LiveInterface state, ToggleButtonListener listener)
	{
		Label label = new Label(labelText);
		return new ToggleButton( label, label, state, listener );
	}

	public static ToggleButton toggleButtonWithLabel(String labelText, LiveValue state)
	{
		Label label = new Label(labelText);
		return new ToggleButton( label, label, state );
	}



	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		final LiveInterface value = valueSource.getLive();

		final AbstractBorder inactiveBorder = style.get(Controls.toggleButtonInactiveBorder, AbstractBorder.class);
		final AbstractBorder activeBorder = style.get(Controls.toggleButtonActiveBorder, AbstractBorder.class);

		boolean v = (Boolean)value.getStaticValue();

		Pres inactive = inactiveBorder.surround(inactiveChild);
		Pres active = activeBorder.surround(activeChild);

		Pres bin = new Bin(v  ?  active : inactive);

		LSBin element = (LSBin)bin.present( ctx, style );
		element.setFixedValue( v );

		return new ToggleButtonControl( ctx, style, element, inactive, active, value, listener );
	}
}
