//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.Modifier;

public abstract class SpinEntry extends Control
{
	protected DPElement element;
	protected TextEntry textEntry;
	protected DPElement upSpinButton, downSpinButton;
	
	
	protected static class SpinEntryTextListener extends TextEntry.TextEntryListener
	{
		private SpinEntry spinEntry = null;
		
		
		@Override
		public void onAccept(TextEntry textEntry, String text)
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
	
	
	
	protected SpinEntry(DPElement element, TextEntry textEntry, DPElement upSpinButton, DPElement downSpinButton, SpinEntryTextListener textListener)
	{
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
