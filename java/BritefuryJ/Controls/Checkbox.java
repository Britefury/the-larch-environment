//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.*;
import java.awt.geom.Line2D;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.ElementPainter;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.Pres.Primitive.Spacer;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class Checkbox extends ControlPres
{
	public static interface CheckboxListener
	{
		public void onCheckbox(CheckboxControl checkbox, boolean state);
	}


	protected static class CheckboxCheckPainter implements ElementPainter
	{
		private Paint paint;
		private Checkbox.CheckboxControl checkbox;
		private static final Stroke stroke = new BasicStroke( 2.0f );


		public CheckboxCheckPainter(Paint paint, Checkbox.CheckboxControl checkbox)
		{
			this.paint = paint;
			this.checkbox = checkbox;
		}

		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
			if ( checkbox.getState() )
			{
				double w = element.getActualWidth();
				double h = element.getActualHeight();
				Line2D.Double a = new Line2D.Double( 0.0, 0.0, w, h );
				Line2D.Double b = new Line2D.Double( w, 0.0, 0.0, h );

				Paint savedPaint = graphics.getPaint();
				Stroke savedStroke = graphics.getStroke();
				graphics.setPaint( paint );
				graphics.setStroke( stroke );
				graphics.draw( a );
				graphics.draw( b );
				graphics.setStroke( savedStroke );
				graphics.setPaint( savedPaint );
			}
		}
	}


	protected static class CheckboxCheckInteractor implements ClickElementInteractor
	{
		private Checkbox.CheckboxControl checkbox;


		public CheckboxCheckInteractor(Checkbox.CheckboxControl checkbox)
		{
			this.checkbox = checkbox;
		}



		@Override
		public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
		{
			checkbox.toggle();
			return true;
		}
	}



	public static class CheckboxControl extends Control implements IncrementalMonitorListener
	{
		private LSElement element, box, check;
		private LiveInterface state;
		private CheckboxListener listener;

		
		protected CheckboxControl(PresentationContext ctx, StyleValues style, LSElement element, LSElement box, LSElement check, LiveInterface state, CheckboxListener listener, Paint checkForeground)
		{
			super( ctx, style );
			
			this.element = element;
			this.box = box;
			this.box.addElementInteractor( new CheckboxCheckInteractor( this ) );
			this.check = check;
			check.addPainter( new CheckboxCheckPainter( checkForeground, this ) );
			this.state = state;
			this.state.addListener( this );
			this.listener = listener;
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
				listener.onCheckbox( this, !value );
			}
		}



		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			// Use getValue() so that @state reports further value changes
			boolean value = (Boolean)state.getValue();
			
			element.setFixedValue( value );

			check.queueFullRedraw();
		}
	}

	
	
	private static class CommitListener implements CheckboxListener
	{
		private LiveValue value;
		private CheckboxListener listener;
		
		public CommitListener(LiveValue value, CheckboxListener listener)
		{
			this.value = value;
			this.listener = listener;
		}
		
		@Override
		public void onCheckbox(CheckboxControl checkbox, boolean state)
		{
			if ( listener != null )
			{
				listener.onCheckbox( checkbox, state );
			}
			value.setLiteralValue( state );
		}
	}
	

	
	private Pres child;
	private LiveSource valueSource;
	private CheckboxListener listener;
	
	
	private Checkbox(Object child, LiveSource valueSource, CheckboxListener listener)
	{
		this.child = coerce( child );
		this.valueSource = valueSource;
		this.listener = listener;
	}

	
	public Checkbox(Object child, boolean initialState, CheckboxListener listener)
	{
		LiveValue value = new LiveValue( initialState );
		
		this.child = coerce( child );
		this.valueSource = new LiveSourceRef( value );
		this.listener = new CommitListener( value, listener );
	}
	
	public Checkbox(Object child, LiveInterface value, CheckboxListener listener)
	{
		this( child, new LiveSourceRef( value ), listener );
	}
	
	public Checkbox(Object child, LiveValue value)
	{
		this( child, new LiveSourceRef( value ), new CommitListener( value, null ) );
	}
	
	
	public static Checkbox checkboxWithLabel(String labelText, boolean state, CheckboxListener listener)
	{
		return new Checkbox( new Label( labelText ), state, listener );
	}
	
	public static Checkbox checkboxWithLabel(String labelText, LiveInterface state, CheckboxListener listener)
	{
		return new Checkbox( new Label( labelText ), state, listener );
	}
	
	public static Checkbox checkboxWithLabel(String labelText, LiveValue state)
	{
		return new Checkbox( new Label( labelText ), state );
	}
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		final LiveInterface value = valueSource.getLive();

		double checkboxPadding = style.get( Controls.checkboxPadding, Double.class );
		StyleSheet checkStyle = StyleSheet.style( Primitive.border.as( style.get( Controls.checkboxCheckBorder, AbstractBorder.class ) ) );
		StyleSheet checkboxStyle = Controls.checkboxStyle.get( style );
		
		double checkSize = style.get( Controls.checkboxCheckSize, Double.class );
		Paint checkForeground = style.get( Controls.checkboxCheckForeground, Paint.class );
		
		Pres check = new Spacer( checkSize, checkSize );
		LSElement checkElement = check.present( ctx, style );
		Pres checkBorder = checkStyle.applyTo( new Border( checkElement ) );
		
		Pres childElement = presentAsCombinator( ctx, Controls.useCheckboxAttrs( style ), child );
		Pres row = checkboxStyle.applyTo( new Row( new Pres[] { checkBorder.pad( checkboxPadding, checkboxPadding ).alignHPack(),
				childElement } ) ).alignVCentre();
		LSElement rowElement = row.present( ctx, style);
		
		Pres bin = new Bin( rowElement );
		LSElement element = bin.present( ctx, style );
		element.setFixedValue( value.getStaticValue() );
		return new CheckboxControl( ctx, style, element, rowElement, checkElement, value, listener, checkForeground );
	}
}
