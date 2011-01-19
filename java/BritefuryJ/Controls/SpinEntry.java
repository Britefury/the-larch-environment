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
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.DocPresent.Interactor.DragElementInteractor;
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
		protected DPElement element;
		protected TextEntry.TextEntryControl textEntry;
		protected DPElement upSpinButton, downSpinButton;
		
		
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
			public boolean dragBegin(PointerInputElement element, PointerButtonEvent event)
			{
				if ( event.getButton() == 1  ||  event.getButton() == 2 )
				{
					pointerToDragStartValue.put( event.getPointer().concretePointer(), storeValue() );
					
					
					if ( event.getButton() == 1 )
					{
						if ( ( event.getPointer().getModifiers() & Modifier.CTRL ) != 0 )
						{
							onPage( bUp );
						}
						else
						{
							onStep( bUp );
						}
						return true;
					}
					else if ( event.getButton() == 2 )
					{
						onPage( bUp );
						return true;
					}
					
					return false;
				}
				
				return false;
			}


			@Override
			public void dragEnd(PointerInputElement element, PointerButtonEvent event, Point2 dragStartPos, int dragButton)
			{
				pointerToDragStartValue.remove( event.getPointer().concretePointer() );
			}


			@Override
			public void dragMotion(PointerInputElement element, PointerMotionEvent event, Point2 dragStartPos, int dragButton)
			{
				double delta = event.getPointer().getLocalPos().y - dragStartPos.y;
				
				Object startValue = pointerToDragStartValue.get( event.getPointer().concretePointer() );
				if ( startValue != null )
				{
					onDrag( startValue, delta );
				}
			}
		}
		
		
		
		protected SpinEntryControl(PresentationContext ctx, StyleValues style, DPElement element, TextEntry.TextEntryControl textEntry,
				DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener)
		{
			super( ctx, style );
			
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
		protected abstract void onPage(boolean bUp);
		protected abstract void onDrag(Object startValue, double delta);
		protected abstract Object storeValue();
		
		
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
	}
	
	
	public SpinEntry()
	{
	}
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleSheet arrowStyleSheet = style.get( Controls.spinEntryArrowAttrs, StyleSheet.class );
		StyleValues arrowStyle = style.withAttrs( arrowStyleSheet );
		double arrowSize = style.get( Controls.spinEntryArrowSize, Double.class );
		double hspacing = style.get( Controls.spinEntryHSpacing, Double.class );
		
		Pres upArrow = new Arrow( Arrow.Direction.UP, arrowSize );
		DPElement upArrowElement = arrowStyle.applyTo( upArrow ).present( ctx, style );
		Pres downArrow = new Arrow( Arrow.Direction.DOWN, arrowSize );
		DPElement downArrowElement = arrowStyle.applyTo( downArrow ).present( ctx, style );
		Pres arrowsBox = arrowStyle.applyTo( new Column( new Object[] { upArrowElement, downArrowElement } ) );
		
		SpinEntryControl.SpinEntryTextListener textListener = new SpinEntryControl.SpinEntryTextListener();
		
		TextEntry entry = new TextEntry( getInitialValueString(), textListener, getValidationPattern(), getValidationFailMessage() );
		TextEntry.TextEntryControl entryControl = (TextEntryControl)entry.createControl( ctx, style );
		
		Pres row = StyleSheet.instance.withAttr( Primitive.rowSpacing, hspacing ).applyTo( new Row( new Object[] { entryControl.getElement().alignHExpand().alignVRefYExpand(), arrowsBox.alignVCentre() } ) );
		DPElement element = row.present( ctx, style );
		
		return createSpinEntryControl( ctx, style, element, entryControl, upArrowElement, downArrowElement, textListener );
	}
	
	
	protected abstract String getInitialValueString();
	protected abstract Pattern getValidationPattern();
	protected abstract String getValidationFailMessage();
	
	
	protected abstract SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, DPElement element, TextEntry.TextEntryControl entryControl, DPElement upArrow,
			DPElement downArrow, SpinEntryControl.SpinEntryTextListener textListener);
}
