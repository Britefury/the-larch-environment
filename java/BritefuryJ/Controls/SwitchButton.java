//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Graphics.FilledBorder;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSAbstractBox;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentElement;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.*;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.List;


public class SwitchButton extends ControlPres
{
	public enum Orientation
	{
		HORIZONTAL,
		VERTICAL
	}

	public static interface SwitchButtonListener
	{
		public void onSwitch(SwitchButtonControl switchButton, int previousChoice, int choice);
	}



	public static class SwitchButtonControl extends ControlPres.Control implements IncrementalMonitorListener
	{
		private class SwitchButtonInteractor implements ClickElementInteractor
		{
			private int buttonChoice;

			private SwitchButtonInteractor(int buttonChoice)
			{
				this.buttonChoice = buttonChoice;
			}


			@Override
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				setChoice( buttonChoice );
				return true;
			}
		}



		private LSElement element;
		private LSAbstractBox switchSequenceElement;
		private Pres offOptions[], onOptions[];

		private LiveInterface currentChoice;
		private int prevChoice;
		private SwitchButtonListener listener;


		protected SwitchButtonControl(PresentationContext ctx, StyleValues style, LSElement element, LSAbstractBox switchSequenceElement, Pres offOptions[], Pres onOptions[], LiveInterface currentChoice, SwitchButtonListener listener)
		{
			super( ctx, style );
			this.element = element;
			this.switchSequenceElement = switchSequenceElement;
			this.offOptions = offOptions;
			this.onOptions = onOptions;
			this.currentChoice = currentChoice;
			this.prevChoice = (Integer)currentChoice.getStaticValue();
			this.listener = listener;

			for (int i = 0; i < switchSequenceElement.size(); i++)
			{
				switchSequenceElement.get( i ).addElementInteractor( new SwitchButtonInteractor( i ) );
			}

			currentChoice.addListener( this );
			element.setFixedValue( currentChoice.elementValueFunction() );
		}




		@Override
		public LSElement getElement()
		{
			return element;
		}



		public int getChoice()
		{
			return (Integer)currentChoice.getStaticValue();
		}

		private void setChoice(int choice)
		{
			int currentChoice = getChoice();
			if ( choice != currentChoice )
			{
				listener.onSwitch( this, currentChoice, choice );
			}
		}


		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			// Use getValue() so that @currentChoice reports further value changes
			int value = (Integer)currentChoice.getValue();

			element.setFixedValue( value );

			if ( value != prevChoice )
			{
				// Change contents
				LSElement prev = offOptions[prevChoice].present( ctx, style );
				prev.addElementInteractor( new SwitchButtonInteractor( prevChoice ) );

				LSElement cur = onOptions[value].present( ctx, style );

				switchSequenceElement.set( prevChoice, prev );
				switchSequenceElement.set( value, cur );

				prevChoice = value;

				switchSequenceElement.queueFullRedraw();
			}
		}
	}



	private static class SwitchButtonPainter implements ElementPainter
	{
		private float separatorThickness;
		private Paint separatorPaint;
		private Orientation orientation;


		private SwitchButtonPainter(float separatorThickness, Paint separatorPaint, Orientation orientation)
		{
			this.separatorThickness = separatorThickness;
			this.separatorPaint = separatorPaint;
			this.orientation = orientation;
		}


		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
			Stroke oldStroke = graphics.getStroke();
			Paint oldPaint = graphics.getPaint();

			graphics.setStroke( new BasicStroke( separatorThickness ) );
			graphics.setPaint( separatorPaint );

			LSAbstractBox seq = (LSAbstractBox)element;
			List<LSElement> children = seq.getChildren();

			if ( orientation == Orientation.HORIZONTAL )
			{
				double y1 = seq.getActualHeight();
				for (int i = 0; i < children.size(); i++)
				{
					double x = children.get( i ).getPositionInParentSpaceX();
					graphics.draw( new Line2D.Double( x, 0.0, x, y1 ) );
				}
			}
			else if ( orientation == Orientation.VERTICAL )
			{
				double x1 = seq.getActualWidth();
				for (int i = 0; i < children.size(); i++)
				{
					double y = children.get( i ).getPositionInParentSpaceY();
					graphics.draw( new Line2D.Double( 0.0, y, x1, y ) );
				}
			}
			else
			{
				throw new RuntimeException( "Invalid orientation" );
			}

			graphics.setStroke( oldStroke );
			graphics.setPaint( oldPaint );
		}
	}




	private static class CommitListener implements SwitchButtonListener
	{
		private LiveValue value;
		private SwitchButtonListener listener;

		public CommitListener(LiveValue value, SwitchButtonListener listener)
		{
			this.value = value;
			this.listener = listener;
		}

		@Override
		public void onSwitch(SwitchButtonControl switchButton, int previousChoice, int choice)
		{
			if ( listener != null )
			{
				listener.onSwitch( switchButton, previousChoice, choice );
			}
			value.setLiteralValue( choice );
		}
	}



	private Pres offOptions[], onOptions[];
	private Orientation orientation;
	private ControlPres.LiveSource valueSource;
	private SwitchButtonListener listener;



	private SwitchButton(Pres offOptions[], Pres onOptions[], Orientation orientation, ControlPres.LiveSource valueSource, SwitchButtonListener listener)
	{
		if ( offOptions.length != onOptions.length )
		{
			throw new RuntimeException( "The lists of off-options and on-options must have the same length" );
		}

		this.offOptions = offOptions;
		this.onOptions = onOptions;
		this.orientation = orientation;
		this.valueSource = valueSource;
		this.listener = listener;
	}



	private SwitchButton(Pres offOptions[], Pres onOptions[], Orientation orientation, int initialChoice, SwitchButtonListener listener)
	{
		if ( offOptions.length != onOptions.length )
		{
			throw new RuntimeException( "The lists of off-options and on-options must have the same length" );
		}

		LiveValue value = new LiveValue( initialChoice );

		this.offOptions = offOptions;
		this.onOptions = onOptions;
		this.orientation = orientation;
		this.valueSource = new ControlPres.LiveSourceRef( value );
		this.listener = new CommitListener( value, listener );
	}

	public SwitchButton(List<Object> offOptions, List<Object> onOptions, Orientation orientation, int initialChoice, SwitchButtonListener listener)
	{
		this( mapCoerce( offOptions ), mapCoerce( onOptions ), orientation, initialChoice, listener );
	}

	public SwitchButton(Object offOptions[], Object onOptions[], Orientation orientation, int initialChoice, SwitchButtonListener listener)
	{
		this( mapCoerce( offOptions ), mapCoerce( onOptions ), orientation, initialChoice, listener );
	}


	private SwitchButton(Pres offOptions[], Pres onOptions[], Orientation orientation, LiveInterface value, SwitchButtonListener listener)
	{
		this( offOptions, onOptions, orientation, new ControlPres.LiveSourceRef( value ), listener );
	}

	public SwitchButton(List<Object> offOptions, List<Object> onOptions, Orientation orientation, LiveInterface value, SwitchButtonListener listener)
	{
		this( mapCoerce( offOptions ), mapCoerce( onOptions ), orientation, value, listener );
	}

	public SwitchButton(Object offOptions[], Object onOptions[], Orientation orientation, LiveInterface value, SwitchButtonListener listener)
	{
		this( mapCoerce( offOptions ), mapCoerce( onOptions ), orientation, value, listener );
	}


	private SwitchButton(Pres offOptions[], Pres onOptions[], Orientation orientation, LiveValue value)
	{
		this( offOptions, onOptions, orientation, new ControlPres.LiveSourceRef( value ), new CommitListener( value, null ) );
	}

	public SwitchButton(List<Object> offOptions, List<Object> onOptions, Orientation orientation, LiveValue value)
	{
		this( mapCoerce( offOptions ), mapCoerce( onOptions ), orientation, value );
	}

	public SwitchButton(Object offOptions[], Object onOptions[], Orientation orientation, LiveValue value)
	{
		this( mapCoerce( offOptions ), mapCoerce( onOptions ), orientation, value );
	}





	@Override
	public ControlPres.Control createControl(PresentationContext ctx, StyleValues style)
	{
		final StyleValues usedStyle = Controls.useSwitchButtonAttrs( style );

		// Get style values
		AbstractBorder border = style.get( Controls.switchButtonBorder, AbstractBorder.class );
		double inset = style.get( Controls.switchButtonInset, Double.class );
		double spacing = style.get( Controls.switchButtonSpacing, Double.class );
		Paint sepPaint = style.get( Controls.switchButtonSeparatorPaint, Paint.class );
		float sepThickness = style.get( Controls.switchButtonSeparatorThickness, Float.class );
		Paint backgSelected = style.get( Controls.switchButtonBackgroundSelected, Paint.class );
		Paint backgHover = style.get( Controls.switchButtonBackgroundHover, Paint.class );
		StyleSheet internalStyleSheet = style.get( Controls.switchButtonInternalStyle, StyleSheet.class );
		StyleValues internalStyle = usedStyle.withAttrs( internalStyleSheet );


		final LiveInterface value = valueSource.getLive();
		int initialChoice = (Integer)value.getStaticValue();

		// Create borders that space the contents appropriately
		FilledBorder startBorderNotSelected, bodyBorderNotSelected, endBorderNotSelected;
		FilledBorder startBorderSelected, bodyBorderSelected, endBorderSelected;

		if ( orientation == Orientation.HORIZONTAL )
		{
			startBorderNotSelected = new FilledBorder( inset, spacing * 0.5, inset, inset, null ).highlight( backgHover );
			bodyBorderNotSelected = new FilledBorder( spacing * 0.5, spacing * 0.5, inset, inset, null ).highlight( backgHover );
			endBorderNotSelected = new FilledBorder( spacing * 0.5, inset, inset, inset, null ).highlight( backgHover );
			startBorderSelected = new FilledBorder( inset, spacing * 0.5, inset, inset, backgSelected );
			bodyBorderSelected = new FilledBorder( spacing * 0.5, spacing * 0.5, inset, inset, backgSelected );
			endBorderSelected = new FilledBorder( spacing * 0.5, inset, inset, inset, backgSelected );
		}
		else if ( orientation == Orientation.VERTICAL )
		{
			startBorderNotSelected = new FilledBorder( inset, inset, inset, spacing * 0.5, null ).highlight( backgHover );
			bodyBorderNotSelected = new FilledBorder( inset, inset, spacing * 0.5, spacing * 0.5, null ).highlight( backgHover );
			endBorderNotSelected = new FilledBorder( inset, inset, spacing * 0.5, inset, null ).highlight( backgHover );
			startBorderSelected = new FilledBorder( inset, inset, inset, spacing * 0.5, backgSelected );
			bodyBorderSelected = new FilledBorder( inset, inset, spacing * 0.5, spacing * 0.5, backgSelected );
			endBorderSelected = new FilledBorder( inset, inset, spacing * 0.5, inset, backgSelected );
		}
		else
		{
			throw new RuntimeException( "Invalid orientation" );
		}

		// Build an array of option containers, with contents
		Pres offPres[] = new Pres[offOptions.length];
		Pres onPres[] = new Pres[onOptions.length];
		Pres switchPres[] = new Pres[offOptions.length];
		for (int i = 0; i < offOptions.length; i++)
		{
			// Pick the border depending on the index of the option
			FilledBorder bOff, bOn;
			if ( i == 0 )
			{
				bOff = startBorderNotSelected;
				bOn = startBorderSelected;
			}
			else if ( i == offOptions.length - 1 )
			{
				bOff = endBorderNotSelected;
				bOn = endBorderSelected;
			}
			else
			{
				bOff = bodyBorderNotSelected;
				bOn = bodyBorderSelected;
			}

			if ( orientation == Orientation.HORIZONTAL )
			{
				offPres[i] = bOff.surround( offOptions[i] ).alignVRefYExpand();
				onPres[i] = bOn.surround( onOptions[i] ).alignVRefYExpand();
			}
			else if ( orientation == Orientation.VERTICAL )
			{
				offPres[i] = bOff.surround( offOptions[i] ).alignHExpand();
				onPres[i] = bOn.surround( onOptions[i] ).alignHExpand();
			}
			else
			{
				throw new RuntimeException( "Invalid orientation" );
			}

			switchPres[i] = i == initialChoice  ?  onPres[i]  :  offPres[i];
		}

		Pres switchSequencePres;
		if ( orientation == Orientation.HORIZONTAL )
		{
			switchSequencePres = new Row( switchPres );
		}
		else if ( orientation == Orientation.VERTICAL )
		{
			switchSequencePres = new Column( switchPres );
		}
		else
		{
			throw new RuntimeException( "Invalid orientation" );
		}

		if (sepPaint != null)
		{
			switchSequencePres = switchSequencePres.withPainter( new SwitchButtonPainter( sepThickness, sepPaint, orientation ) );
		}

		LSAbstractBox switchSequenceElement = (LSAbstractBox)switchSequencePres.present( ctx, internalStyle );

		Pres switchButtonPres = border.surround( new PresentElement( switchSequenceElement ) );

		LSElement switchButtonElement = switchButtonPres.present( ctx, style );

		return new SwitchButtonControl( ctx, internalStyle, switchButtonElement, switchSequenceElement, offPres, onPres, value, listener );
	}
}
