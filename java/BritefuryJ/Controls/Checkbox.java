//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.Paint;

import BritefuryJ.Graphics.AbstractBorder;
import BritefuryJ.Incremental.IncrementalMonitor;
import BritefuryJ.Incremental.IncrementalMonitorListener;
import BritefuryJ.LSpace.LSElement;
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
	public static class CheckboxControl extends Control implements IncrementalMonitorListener
	{
		private LSElement element, box, check;
		private LiveValue state;

		
		protected CheckboxControl(PresentationContext ctx, StyleValues style, LSElement element, LSElement box, LSElement check, LiveValue state, Paint checkForeground)
		{
			super( ctx, style );
			
			this.element = element;
			this.box = box;
			this.box.addElementInteractor( new CheckboxHelper.CheckboxCheckInteractor( this ) );
			this.check = check;
			check.addPainter( new CheckboxHelper.CheckboxCheckPainter( checkForeground, this ) );
			this.state = state;
			this.state.addListener( this );
			element.setFixedValue( state );
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
		
		public void setState(boolean state)
		{
			this.state.setLiteralValue( state );
		}

		
		
		public void toggle()
		{
			boolean value = (Boolean)state.getStaticValue();
			state.setLiteralValue( !value );
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

	
	
	private Pres child;
	private LiveValue state;

	
	public Checkbox(Object child, LiveValue state)
	{
		this.child = coerce( child );
		this.state = state;
	}
	
	public Checkbox(Object child, boolean state)
	{
		this( child, new LiveValue( state ) );
	}
	
	
	public static Checkbox checkboxWithLabel(String labelText, LiveValue state)
	{
		return new Checkbox( new Label( labelText ), state );
	}
	
	public static Checkbox checkboxWithLabel(String labelText, boolean state)
	{
		return new Checkbox( new Label( labelText ), state );
	}
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleSheet checkStyle = StyleSheet.style( Primitive.border.as( style.get( Controls.checkboxCheckBorder, AbstractBorder.class ) ) );
		StyleSheet checkboxStyle = Controls.checkboxStyle.get( style );
		
		double checkSize = style.get( Controls.checkboxCheckSize, Double.class );
		Paint checkForeground = style.get( Controls.checkboxCheckForeground, Paint.class );
		
		Pres check = new Spacer( checkSize, checkSize );
		LSElement checkElement = check.present( ctx, style );
		Pres checkBorder = checkStyle.applyTo( new Border( checkElement ) );
		
		Pres childElement = presentAsCombinator( ctx, Controls.useCheckboxAttrs( style ), child );
		Pres row = checkboxStyle.applyTo( new Row( new Pres[] { checkBorder.alignHPack().alignVCentre(), childElement.alignVCentre() } ) );
		LSElement rowElement = row.present( ctx, style);
		
		Pres bin = new Bin( rowElement );
		LSElement element = bin.present( ctx, style );
		element.setFixedValue( state.getStaticValue() );
		return new CheckboxControl( ctx, style, element, rowElement, checkElement, state, checkForeground );
	}
}
