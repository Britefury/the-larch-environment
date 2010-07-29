//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.Paint;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Label;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.Spacer;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class Checkbox extends ControlPres
{
	public static interface CheckboxListener
	{
		public void onCheckboxToggled(CheckboxControl checkbox, boolean state);
	}
	
	
	
	public static class CheckboxControl extends Control
	{
		private DPElement element, box, check;
		private CheckboxListener listener;
		private boolean state;

		
		protected CheckboxControl(PresentationContext ctx, DPElement element, DPElement box, DPElement check, boolean state, CheckboxListener listener, Paint checkForeground)
		{
			super( ctx );
			
			this.element = element;
			this.box = box;
			this.box.addInteractor( new CheckboxHelper.CheckboxCheckInteractor( this ) );
			this.check = check;
			check.addInteractor( new CheckboxHelper.CheckboxCheckPainterInteractor( checkForeground, this ) );
			this.listener = listener;
			this.state = state;
			element.setFixedValue( state );
		}
		
		
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		
		public boolean getState()
		{
			return state;
		}
		
		public void setState(boolean state)
		{
			if ( state != this.state )
			{
				this.state = state;
				
				check.queueFullRedraw();
				
				element.setFixedValue( state );
				
				listener.onCheckboxToggled( this, state );
			}
		}

		
		
		public void toggle()
		{
			setState( !state );
		}
	}

	
	
	private Pres child;
	private CheckboxListener listener;
	private boolean initialState;
	private WeakHashMap<DPElement, Object> elements = new WeakHashMap<DPElement, Object>();

	
	public Checkbox(Object child, boolean initialState, CheckboxListener listener)
	{
		this.child = coerce( child );
		this.listener = listener;
		this.initialState = initialState;
	}
	
	
	public static Checkbox checkboxWithLabel(String labelText, boolean initialState, CheckboxListener listener)
	{
		return new Checkbox( new Label( labelText ), initialState, listener );
	}
	
	
	
	@Override
	public Control createControl(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		StyleSheet2 checkStyle = StyleSheet2.instance.withAttr( Primitive.border, style.get( Controls.checkboxCheckBorder, BritefuryJ.DocPresent.Border.Border.class ) );
		StyleSheet2 checkboxStyle = Controls.checkboxStyle.get( style );
		
		double checkSize = style.get( Controls.checkboxCheckSize, Double.class );
		Paint checkForeground = style.get( Controls.checkboxCheckForeground, Paint.class );
		
		Pres check = new Spacer( checkSize, checkSize );
		DPElement checkElement = check.present( ctx );
		Pres checkBorder = checkStyle.applyTo( new Border( checkElement ) );
		
		Pres childElement = presentAsCombinator( Controls.useCheckboxAttrs( ctx ), child );
		Pres hbox = checkboxStyle.applyTo( new HBox( new Pres[] { checkBorder.alignVCentre(), childElement.alignVCentre() } ) );
		DPElement hboxElement = hbox.present( ctx );
		
		Pres bin = new Bin( hboxElement );
		DPElement element = bin.present( ctx );
		elements.put( element, null );
		element.setFixedValue( initialState );
		return new CheckboxControl( ctx, element, hboxElement, checkElement, initialState, listener, checkForeground );
	}
}
