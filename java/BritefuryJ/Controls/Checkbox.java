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

public class Checkbox extends Pres
{
	public static interface CheckboxListener
	{
		public void onCheckboxToggled(Checkbox checkbox, boolean state);
	}
	
	
	private Pres child;
	private CheckboxListener listener;
	private boolean state;
	private WeakHashMap<DPElement, Object> elements = new WeakHashMap<DPElement, Object>();

	
	public Checkbox(Object child, boolean state, CheckboxListener listener)
	{
		this.child = coerce( child );
		this.listener = listener;
		this.state = state;
	}
	
	
	public static Checkbox checkboxWithLabel(String labelText, boolean state, CheckboxListener listener)
	{
		return new Checkbox( new Label( labelText ), state, listener );
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
			
			for (DPElement element: elements.keySet())
			{
				element.setFixedValue( state );
				element.queueFullRedraw();
			}
			
			listener.onCheckboxToggled( this, state );
		}
	}

	
	
	public void toggle()
	{
		setState( !state );
	}



	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		StyleSheet2 checkStyle = StyleSheet2.instance.withAttr( Primitive.border, style.get( Controls.checkboxCheckBorder, BritefuryJ.DocPresent.Border.Border.class ) );
		StyleSheet2 checkboxStyle = Controls.checkboxStyle.get( style );
		
		double checkSize = style.get( Controls.checkboxCheckSize, Double.class );
		Paint foreground = style.get( Controls.checkboxCheckForeground, Paint.class );
		
		Pres check = new Spacer( checkSize, checkSize ).addInteractor( new CheckboxHelper.CheckboxCheckPainterInteractor( foreground, this ) );
		Pres checkBorder = checkStyle.applyTo( new Border( check ) );
		
		Pres childElement = presentAsCombinator( Controls.useCheckboxAttrs( ctx ), child );
		Pres hbox = checkboxStyle.applyTo( new HBox( new Pres[] { checkBorder.alignVCentre(), childElement.alignVCentre() } ) ).addInteractor( new CheckboxHelper.CheckboxCheckInteractor( this ) );
		Pres bin = new Bin( hbox );
		DPElement element = bin.present( ctx );
		elements.put( element, null );
		element.setFixedValue( state );
		return element;
	}
}
