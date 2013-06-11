//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.HashMap;
import java.util.regex.Pattern;

import BritefuryJ.Controls.TextEntry.TextEntryControl;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.PointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Input.Modifier;
import BritefuryJ.LSpace.Input.PointerInterface;
import BritefuryJ.LSpace.Interactor.DragElementInteractor;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public abstract class SpinEntry extends ControlPres
{
	public abstract static class SpinEntryControl extends Control
	{
		protected static class SpinEntryTextListener extends TextEntry.TextEntryListener
		{
			private SpinEntryControl spinEntry = null;
			
			
			@Override
			public void onAccept(TextEntry.TextEntryControl textEntry, String text)
			{
				spinEntry.onTextChanged( text );
			}
		}
		
		
		
		private class SpinButtonInteractor implements DragElementInteractor
		{
			private boolean bUp;
			private HashMap<PointerInterface,Object> pointerToDragStartValue = new HashMap<PointerInterface,Object>();
			
			private SpinButtonInteractor(boolean bUp)
			{
				this.bUp = bUp;
			}
			
			
			@Override
			public boolean dragBegin(LSElement element, PointerButtonEvent event)
			{
				if ( event.getButton() == 1  ||  event.getButton() == 2 )
				{
					pointerToDragStartValue.put( event.getPointer().concretePointer(), storeValue() );
					
					
					if ( event.getButton() == 1 )
					{
						if ( ( event.getPointer().getModifiers() & Modifier.CTRL ) != 0 )
						{
							onJump(bUp);
						}
						else
						{
							onStep( bUp );
						}
						return true;
					}
					else if ( event.getButton() == 2 )
					{
						onJump(bUp);
						return true;
					}
					
					return false;
				}
				
				return false;
			}


			@Override
			public void dragEnd(LSElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
			{
				pointerToDragStartValue.remove( event.getPointer().concretePointer() );
			}


			@Override
			public void dragMotion(LSElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
			{
				double delta = dragStartPos.y - event.getPointer().getLocalPos().y;
				
				Object startValue = pointerToDragStartValue.get( event.getPointer().concretePointer() );
				if ( startValue != null )
				{
					onDrag( startValue, delta );
				}
			}
		}
		
		
		
		protected LiveInterface value;
		protected LSElement element;
		protected TextEntry.TextEntryControl textEntry;
		protected LSElement upSpinButton, downSpinButton;
		
		
		protected SpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, TextEntry.TextEntryControl textEntry,
				LSElement upSpinButton, LSElement downSpinButton, SpinEntryTextListener textListener)
		{
			super( ctx, style );
			
			this.value = value;
			this.element = element;
			this.textEntry = textEntry;
			this.upSpinButton = upSpinButton;
			this.downSpinButton = downSpinButton;
			this.upSpinButton.addElementInteractor( new SpinButtonInteractor( true ) );
			this.downSpinButton.addElementInteractor( new SpinButtonInteractor( false ) );
			textListener.spinEntry = this;
		}
		
		
		protected abstract void onTextChanged(String text);
		protected abstract void onStep(boolean bUp);
		protected abstract void onJump(boolean bUp);
		protected abstract void onDrag(Object startValue, double delta);
		protected abstract Object storeValue();
		
		
		
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
	
	
	protected SpinEntry(LiveSource valueSource)
	{
		this.valueSource = valueSource;
	}
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		LiveInterface value = valueSource.getLive();
		LiveFunction text = new LiveFunction( new TextLiveFn( value ) );
		
		
		
		StyleSheet arrowStyleSheet = style.get( Controls.spinEntryArrowAttrs, StyleSheet.class );
		StyleValues arrowStyle = style.withAttrs( arrowStyleSheet );
		double arrowSize = style.get( Controls.spinEntryArrowSize, Double.class );
		double hspacing = style.get( Controls.spinEntryHSpacing, Double.class );
		
		Pres upArrow = new Arrow( Arrow.Direction.UP, arrowSize ).alignVBottom();
		LSElement upArrowElement = arrowStyle.applyTo( upArrow ).present( ctx, style );
		Pres downArrow = new Arrow( Arrow.Direction.DOWN, arrowSize ).alignVTop();
		LSElement downArrowElement = arrowStyle.applyTo( downArrow ).present( ctx, style );
		Pres arrowsBox = arrowStyle.applyTo( new Column( new Object[] { upArrowElement, downArrowElement } ).alignHPack().alignVCentre() );
		
		SpinEntryControl.SpinEntryTextListener textListener = new SpinEntryControl.SpinEntryTextListener();
		
		TextEntry entry = new TextEntry( text, textListener ).regexValidated( getValidationPattern(), getValidationFailMessage() );
		TextEntry.TextEntryControl entryControl = (TextEntryControl)entry.createControl( ctx, style.alignHExpand().alignVRefY() );
		
		Pres row = StyleSheet.style( Primitive.rowSpacing.as( hspacing ) ).applyTo( new Row( new Object[] { entryControl.getElement(), arrowsBox } ) );
		LSElement element = row.present( ctx, style );
		
		return createSpinEntryControl( ctx, style, value, element, entryControl, upArrowElement, downArrowElement, textListener );
	}
	
	
	protected abstract Pattern getValidationPattern();
	protected abstract String getValidationFailMessage();
	
	
	protected abstract SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, LiveInterface value, LSElement element, TextEntry.TextEntryControl entryControl, LSElement upArrow,
			LSElement downArrow, SpinEntryControl.SpinEntryTextListener textListener);
}
