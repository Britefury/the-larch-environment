//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Label;
import BritefuryJ.Pres.Primitive.Proxy;
import BritefuryJ.StyleSheet.StyleValues;

import java.util.HashMap;
import java.util.regex.Pattern;

public abstract class NumericLabel extends ControlPres
{
	public abstract static class NumericLabelControl extends Control
	{
		private class NumericLabelInteractor implements DragElementInteractor
		{
			private HashMap<PointerInterface,Object> pointerToDragStartValue = new HashMap<PointerInterface,Object>();
			
			
			@Override
			public boolean dragBegin(LSElement element, PointerButtonEvent event)
			{
				if ( event.getButton() == 1 )
				{
					pointerToDragStartValue.put( event.getPointer().concretePointer(), storeValue() );
					
					return true;
				}
				
				return false;
			}


			@Override
			public void dragEnd(LSElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
			{
				pointerToDragStartValue.remove( event.getPointer().concretePointer() );
				
				if ( event.getPointer().getLocalPos().x  ==  dragStartPos.x )
				{
					showTextEntry();
				}
			}


			@Override
			public void dragMotion(LSElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
			{
				double delta = event.getPointer().getLocalPos().x  -  dragStartPos.x;
				
				Object startValue = pointerToDragStartValue.get( event.getPointer().concretePointer() );
				if ( startValue != null )
				{
					onDrag( startValue, delta );
				}
			}
		}
		
		private NumericLabelInteractor labelInteractor = new NumericLabelInteractor();
		
		
		
		private TextEntry.TextEntryListener entryListener = new TextEntry.TextEntryListener()
		{
			@Override
			public void onAccept(TextEntry.TextEntryControl textEntry, String text)
			{
				NumericLabelControl.this.onTextChanged( text );
				showLabel();
			}
		};

		
		
		
		protected LiveInterface value;
		protected LiveInterface text;
		protected LiveValue displayEntry;
		protected LSElement element;
		
		
		protected NumericLabelControl(PresentationContext ctx, StyleValues style, LiveInterface value, LiveInterface text, LiveFunction display, LSElement element)
		{
			super( ctx, style );
			
			this.value = value;
			this.text = text;
			this.displayEntry = new LiveValue( false );
			initDisplayFunction( display );
			
			this.element = element;
		}
		
		
		private void initDisplayFunction(LiveFunction display)
		{
			LiveFunction.Function fn = new LiveFunction.Function()
			{
				@Override
				public Object evaluate()
				{
					boolean showEntry = (Boolean)displayEntry.getValue();
					
					if ( showEntry )
					{
						TextEntry entry = new TextEntry( text, entryListener ).regexValidated( getValidationPattern(), getValidationFailMessage() );
						entry.grabCaretOnRealise();
						return entry;
					}
					else
					{
						String textVal = (String)text.getValue(); 
						return new Label( textVal ).withStyleSheetFromAttr( Controls.numericLabelTextAttrs ).withElementInteractor( labelInteractor ); 
					}
				}
			};
			display.setFunction( fn );
		}
		
		
		private void showLabel()
		{
			displayEntry.setLiteralValue( false );
		}
		
		private void showTextEntry()
		{
			displayEntry.setLiteralValue( true );
		}
		
		
		protected abstract void onTextChanged(String text);
		protected abstract void onDrag(Object startValue, double delta);
		protected abstract Object storeValue();
		
		
		protected abstract Pattern getValidationPattern();
		protected abstract String getValidationFailMessage();
		
		
	
		@Override
		public LSElement getElement()
		{
			return element;
		}
	}
	
	
	private static class TextLiveFn implements LiveFunction.Function
	{
		private LiveInterface value;
		
		
		public TextLiveFn(LiveInterface value)
		{
			this.value = value;
		}
		
		
		@Override
		public Object evaluate()
		{
			return String.valueOf( value.getValue() );
		}
	}
	
	
	
	private LiveSource valueSource;
	
	
	protected NumericLabel(LiveSource valueSource)
	{
		this.valueSource = valueSource;
	}
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		LiveInterface value = valueSource.getLive();
		LiveFunction text = new LiveFunction( new TextLiveFn( value ) );
		LiveFunction display = LiveFunction.value( new Blank() );
		
		Proxy proxy = new Proxy( display );
		LSElement element = proxy.present( ctx, style );
		
		return createNumericLabelControl( ctx, style, value, text, display, element );
	}
	
	
	protected abstract NumericLabelControl createNumericLabelControl(PresentationContext ctx, StyleValues style, LiveInterface value, LiveInterface text, LiveFunction display, LSElement element);
}
