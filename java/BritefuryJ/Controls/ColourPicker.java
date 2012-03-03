//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JColorChooser;

import BritefuryJ.Graphics.FillPainter;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Input.PointerInputElement;
import BritefuryJ.LSpace.Interactor.PushElementInteractor;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Box;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class ColourPicker extends ControlPres
{
	public static interface ColourPickerListener
	{
		public void onColourChanged(ColourPickerControl colourPicker, Color colour);
	}
	
	
	
	public static class ColourPickerControl extends Control
	{
		private LSElement element;
		
		
		public ColourPickerControl(PresentationContext ctx, StyleValues style)
		{
			super( ctx, style );
		}



		@Override
		public LSElement getElement()
		{
			return element;
		}
	}

	
	
	private static class CommitListener implements ColourPickerListener
	{
		private LiveInterface value;
		
		public CommitListener(LiveInterface value)
		{
			this.value = value;
		}
		
		@Override
		public void onColourChanged(ColourPickerControl editableLabel, Color colour)
		{
			value.setLiteralValue( colour );
		}
	}
	
	

	private LiveSource valueSource;
	private ColourPickerListener listener;
	
	
	
	
	private ColourPicker(LiveSource valueSource, ColourPickerListener listener)
	{
		this.valueSource = valueSource;
		this.listener = listener;
	}
	
	
	public ColourPicker(Color initialColour, ColourPickerListener listener)
	{
		this( new LiveSourceValue( initialColour ), listener );
	}
	
	public ColourPicker(LiveInterface value, ColourPickerListener listener)
	{
		this( new LiveSourceRef( value ), listener );
	}	
	
	public ColourPicker(LiveValue value)
	{
		this( new LiveSourceRef( value ), new CommitListener( value ) );
	}
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		final LiveInterface value = valueSource.getLive();
		final ColourPickerControl ctl = new ColourPickerControl( ctx, style );

		LiveFunction.Function displayFunction = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				Object v = value.getValue();
				final Color colour = v instanceof Color  ?  (Color)v  :  Color.GRAY;
				
				PushElementInteractor swatchInteractor = new PushElementInteractor()
				{
					@Override
					public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
					{
						LSElement swatchElement = (LSElement)element;
						
						Color newColour = JColorChooser.showDialog( swatchElement.getRootElement().getComponent(), "Choose colour", colour );
						
						if ( newColour != null )
						{
							listener.onColourChanged( ctl, newColour );
						}
					}
					
					@Override
					public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
					{
						return event.getButton() == 1;
					}
				};
				
				
				StyleSheet swatchStyle = StyleSheet.style( Primitive.shapePainter.as( new FillPainter( colour ) ), Primitive.cursor.as( new Cursor( Cursor.HAND_CURSOR ) ) );
				Pres swatch = swatchStyle.applyTo( new Box( 30.0, 20.0 ) );
				return swatch.withElementInteractor( swatchInteractor );
			}
		};
		
		LiveFunction display = new LiveFunction( displayFunction );
		
		ctl.element = display.present( ctx, style );
		
		return ctl;
	}
}
