//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2014.
//##************************
package BritefuryJ.Controls;

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
import BritefuryJ.Pres.Primitive.*;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

import java.awt.*;
import java.awt.geom.Arc2D;

public class RadioButton extends ControlPres
{
	public static interface RadioButtonListener
	{
		public void onRadioButton(RadioButtonControl radioButton, Object value);
	}


	protected static class RadioButtonRadioPainter implements ElementPainter
	{
		private Paint outerPaint, innerPaint;
		private RadioButtonControl radio;
		private double circleSpacing;
		private static final Stroke stroke = new BasicStroke( 1.0f );


		public RadioButtonRadioPainter(Paint outerPaint, Paint innerPaint, double circleSpacing, RadioButtonControl radio)
		{
			this.outerPaint = outerPaint;
			this.innerPaint = innerPaint;
			this.circleSpacing = circleSpacing;
			this.radio = radio;
		}

		@Override
		public void drawBackground(LSElement element, Graphics2D graphics)
		{
		}

		@Override
		public void draw(LSElement element, Graphics2D graphics)
		{
			double w = element.getActualWidth();
			double h = element.getActualHeight();
			Arc2D.Double outerCircle = new Arc2D.Double(0.5, 0.5, w-1.0, h-1.0, 0.0, 360.0, Arc2D.CHORD);

			Paint savedPaint = graphics.getPaint();
			Stroke savedStroke = graphics.getStroke();

			graphics.setPaint( outerPaint );
			graphics.setStroke( stroke );
			graphics.draw(outerCircle);
			graphics.setStroke( savedStroke );

			if ( radio.isOn() )
			{
				Arc2D.Double innerCircle = new Arc2D.Double(circleSpacing+0.5, circleSpacing+0.5,
						w - circleSpacing * 2.0 - 1.0, h - circleSpacing * 2.0 - 1.0, 0.0, 360.0, Arc2D.CHORD);

				graphics.setPaint( innerPaint );
				graphics.fill( innerCircle );
			}

			graphics.setPaint( savedPaint );
		}
	}


	protected static class RadioButtonInteractor implements ClickElementInteractor
	{
		private RadioButtonControl radio;


		public RadioButtonInteractor(RadioButtonControl radioButton)
		{
			this.radio = radioButton;
		}



		@Override
		public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
		{
			return event.getButton() == 1;
		}

		@Override
		public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
		{
			radio.choose();
			return true;
		}
	}



	public static class RadioButtonControl extends ControlPres.Control implements IncrementalMonitorListener
	{
		private LSElement element, box, radio;
		private LiveInterface state;
		private Object choiceValue;
		private RadioButtonListener listener;


		protected RadioButtonControl(PresentationContext ctx, StyleValues style, LSElement element, LSElement box, LSElement radio, LiveInterface state, Object choiceValue, RadioButtonListener listener,
					     Paint outerPaint, Paint innerPaint, double circleSpacing)
		{
			super( ctx, style );

			this.element = element;
			this.box = box;
			this.box.addElementInteractor( new RadioButtonInteractor( this ) );
			this.radio = radio;
			radio.addPainter(new RadioButtonRadioPainter(outerPaint, innerPaint, circleSpacing, this ));
			this.state = state;
			this.state.addListener( this );
			this.choiceValue = choiceValue;
			this.listener = listener;
			element.setFixedValue(state.elementValueFunction());
		}



		@Override
		public LSElement getElement()
		{
			return element;
		}


		public Object getValue()
		{
			return state.getStaticValue();
		}



		protected void choose()
		{
			if ( listener != null )
			{
				listener.onRadioButton(this, choiceValue);
			}
		}

		protected boolean isOn()
		{
			return getValue().equals(choiceValue);
		}



		@Override
		public void onIncrementalMonitorChanged(IncrementalMonitor inc)
		{
			// Use getValue() so that @state reports further value changes
			Object value = state.getValue();

			element.setFixedValue( value );

			radio.queueFullRedraw();
		}
	}



	private static class CommitListener implements RadioButtonListener
	{
		private LiveValue value;
		private RadioButtonListener listener;

		public CommitListener(LiveValue value, RadioButtonListener listener)
		{
			this.value = value;
			this.listener = listener;
		}

		@Override
		public void onRadioButton(RadioButtonControl radioButton, Object value)
		{
			if ( listener != null )
			{
				listener.onRadioButton(radioButton, value);
			}
			this.value.setLiteralValue( value );
		}
	}



	private Pres child;
	private Object choiceValue;
	private ControlPres.LiveSource valueSource;
	private RadioButtonListener listener;


	private RadioButton(Object child, Object choiceValue, ControlPres.LiveSource valueSource, RadioButtonListener listener)
	{
		this.child = coerce( child );
		this.choiceValue = choiceValue;
		this.valueSource = valueSource;
		this.listener = listener;
	}


	public RadioButton(Object child, Object choiceValue, LiveInterface value, RadioButtonListener listener)
	{
		this( child, choiceValue, new ControlPres.LiveSourceRef( value ), listener );
	}

	public RadioButton(Object child, Object choiceValue, LiveValue value)
	{
		this( child, choiceValue, new ControlPres.LiveSourceRef( value ), new CommitListener( value, null ) );
	}


	public static RadioButton radioButtonWithLabel(String labelText, Object choiceValue, LiveInterface state, RadioButtonListener listener)
	{
		return new RadioButton( new BritefuryJ.Pres.Primitive.Label( labelText ), choiceValue, state, listener );
	}

	public static RadioButton radioButtonWithLabel(String labelText, Object choiceValue, LiveValue state)
	{
		return new RadioButton( new BritefuryJ.Pres.Primitive.Label( labelText ), choiceValue, state );
	}


	public static RadioButton[] radioButtonGroup(Object children[], Object choiceValues[], Object initialValue, RadioButtonListener listener)
	{
		LiveValue value = new LiveValue( initialValue );

		if (children.length != choiceValues.length)
		{
			throw new RuntimeException("Number of children must equal the number of choices");
		}

		RadioButton radios[] = new RadioButton[children.length];
		for (int i = 0; i < children.length; i++)
		{
			radios[i] = new RadioButton(children[i], choiceValues[i], value, listener);
		}

		return radios;
	}

	public static RadioButton[] radioButtonGroupWithLabels(String labelTexts[], Object choiceValues[], Object initialValue, RadioButtonListener listener)
	{
		Object children[] = new Object[labelTexts.length];
		for (int i = 0; i < labelTexts.length; i++)
		{
			children[i] = new BritefuryJ.Pres.Primitive.Label( labelTexts[i] );
		}
		return radioButtonGroup(children, choiceValues, initialValue, listener);
	}



	@Override
	public ControlPres.Control createControl(PresentationContext ctx, StyleValues style)
	{
		final LiveInterface value = valueSource.getLive();

		double radioButtonPadding = style.get( Controls.radioButtonPadding, Double.class );
		StyleSheet radioButtonStyle = Controls.radioButtonStyle.get( style );

		double radioSize = style.get( Controls.radioButtonRadioSize, Double.class );
		Paint outerCirclePaint = style.get( Controls.radioButtonOuterCirclePaint, Paint.class );
		Paint innerCirclePaint = style.get( Controls.radioButtonInnerCirclePaint, Paint.class );
		double circleSpacing = style.get( Controls.radioButtonCircleSpacing, Double.class );

		Pres radio = new Spacer( radioSize, radioSize );
		LSElement radioElement = radio.present( ctx, style );
		Pres radioBin = new Bin(radioElement);

		Pres childElement = presentAsCombinator( ctx, Controls.useRadioButtonAttrs(style), child );
		Pres row = radioButtonStyle.applyTo( new Row( new Pres[] { radioBin.pad( radioButtonPadding, radioButtonPadding ).alignHPack(),
				childElement } ) ).alignVCentre();
		LSElement rowElement = row.present( ctx, style);

		Pres bin = new Bin( rowElement );
		LSElement element = bin.present( ctx, style );
		element.setFixedValue( value.getStaticValue() );
		return new RadioButtonControl( ctx, style, element, rowElement, radioElement, value, choiceValue, listener, outerCirclePaint, innerCirclePaint, circleSpacing );
	}}
