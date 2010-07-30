//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.regex.Pattern;

import BritefuryJ.Controls.TextEntry.TextEntryControl;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Arrow;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Combinators.Primitive.VBox;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.Modifier;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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
		
		private class SpinButtonInteractor extends ElementInteractor
		{
			private boolean bUp;
			
			private SpinButtonInteractor(boolean bUp)
			{
				this.bUp = bUp;
			}
			
			
			public boolean onButtonDown(DPElement element, PointerButtonEvent event)
			{
				return true;
			}
	
			public boolean onButtonUp(DPElement element, PointerButtonEvent event)
			{
				if ( element.isRealised() )
				{
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
				}
				
				return false;
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
			this.upSpinButton.addInteractor( new SpinButtonInteractor( true ) );
			this.downSpinButton.addInteractor( new SpinButtonInteractor( false ) );
			textListener.spinEntry = this;
		}
		
		
		protected abstract void onTextChanged(String text);
		protected abstract void onStep(boolean bUp);
		protected abstract void onPage(boolean bUp);
		
		
		
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
		StyleSheet2 arrowStyleSheet = style.get( Controls.spinEntryArrowAttrs, StyleSheet2.class );
		StyleValues arrowStyle = style.withAttrs( arrowStyleSheet );
		double arrowSize = style.get( Controls.spinEntryArrowSize, Double.class );
		double hspacing = style.get( Controls.spinEntryHSpacing, Double.class );
		
		Pres upArrow = new Arrow( Arrow.Direction.UP, arrowSize );
		DPElement upArrowElement = arrowStyle.applyTo( upArrow ).present( ctx, style );
		Pres downArrow = new Arrow( Arrow.Direction.DOWN, arrowSize );
		DPElement downArrowElement = arrowStyle.applyTo( downArrow ).present( ctx, style );
		Pres arrowsBox = arrowStyle.applyTo( new VBox( new Object[] { upArrowElement, downArrowElement } ) );
		
		SpinEntryControl.SpinEntryTextListener textListener = new SpinEntryControl.SpinEntryTextListener();
		
		TextEntry entry = new TextEntry( getInitialValueString(), textListener, getValidationPattern(), getValidationFailMessage() );
		TextEntry.TextEntryControl entryControl = (TextEntryControl)entry.createControl( ctx, style );
		
		Pres hbox = StyleSheet2.instance.withAttr( Primitive.hboxSpacing, hspacing ).applyTo( new HBox( new Object[] { entryControl.getElement().alignHExpand().alignVCentre(), arrowsBox.alignVCentre() } ) );
		DPElement element = hbox.present( ctx, style );
		
		return createSpinEntryControl( ctx, style, element, entryControl, upArrowElement, downArrowElement, textListener );
	}
	
	
	protected abstract String getInitialValueString();
	protected abstract Pattern getValidationPattern();
	protected abstract String getValidationFailMessage();
	
	
	protected abstract SpinEntryControl createSpinEntryControl(PresentationContext ctx, StyleValues style, DPElement element, TextEntry.TextEntryControl entryControl, DPElement upArrow,
			DPElement downArrow, SpinEntryControl.SpinEntryTextListener textListener);
}
