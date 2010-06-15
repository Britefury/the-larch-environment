//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Border.Border;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheet.PrimitiveStyleSheet;

public class OptionMenu extends Control
{
	public static interface OptionMenuListener
	{
		public void onOptionMenuChoice(OptionMenu optionMenu, int previousChoice, int choice);
	}
	
	
	
	private class OptionMenuInteractor extends ElementInteractor
	{
		private OptionMenuInteractor()
		{
		}
		
		
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
		{
			return true;
		}

		public boolean onButtonUp(DPElement element, PointerButtonEvent event)
		{
			if ( element.isRealised() )
			{
				displayDropdown();
				return true;
			}
			
			return false;
		}
		
		
		public void onEnter(DPElement element, PointerMotionEvent event)
		{
			((DPBorder)element).setBorder( optionMenuHoverBorder );
		}

		public void onLeave(DPElement element, PointerMotionEvent event)
		{
			((DPBorder)element).setBorder( optionMenuBorder );
		}
	}
	
	private class MenuItemListener implements MenuItem.MenuItemListener
	{
		private int choiceIndex;
		
		
		private MenuItemListener(int choiceIndex)
		{
			this.choiceIndex = choiceIndex;
		}
		
		
		@Override
		public void onMenuItemClicked(MenuItem menuItem)
		{
			setChoice( choiceIndex );
		}
	}
	
	

	private DPBorder element;
	private Border optionMenuBorder, optionMenuHoverBorder;
	private DPBin choiceContainer;
	private ArrayList<DPElement> optionChoices = new ArrayList<DPElement>();
	private ArrayList<DPElement> menuChoices = new ArrayList<DPElement>();
	private int currentChoice;
	private OptionMenuListener listener;
	private ControlsStyleSheet styleSheet;
	
	
	protected OptionMenu(DPBorder element, DPBin choiceContainer, List<DPElement> optionChoices, List<DPElement> menuChoices, int initialChoice, OptionMenuListener listener,
			Border optionMenuBorder, Border optionMenuHoverBorder, ControlsStyleSheet styleSheet)
	{
		this.element = element;
		this.optionMenuBorder = optionMenuBorder;
		this.optionMenuHoverBorder = optionMenuHoverBorder;
		this.choiceContainer = choiceContainer;
		this.optionChoices.addAll( optionChoices );
		this.menuChoices.addAll( menuChoices );
		currentChoice = initialChoice;
		this.listener = listener;
		this.styleSheet = styleSheet;
		
		element.addInteractor( new OptionMenuInteractor() );
	}
	
	
	
	
	@Override
	public DPElement getElement()
	{
		return element;
	}
	
	
	
	private void displayDropdown()
	{
		ControlsStyleSheet menuStyle = styleSheet.withClosePopupOnActivate();
		
		ArrayList<DPElement> menuItems = new ArrayList<DPElement>();
		int index = 0;
		for (DPElement choice: menuChoices)
		{
			menuItems.add( menuStyle.menuItem( choice, new MenuItemListener( index ) ).getElement() );
			index++;
		}
		PopupMenu menu = styleSheet.withClosePopupOnActivate().vpopupMenu( menuItems );
		menu.popupBelow( element );
	}
	
	
	public int getChoice()
	{
		return currentChoice;
	}
	
	public void setChoice(int choice)
	{
		if ( choice != currentChoice )
		{
			int oldChoice = currentChoice;
			currentChoice = choice;
			
			DPBin newChoiceContainer = PrimitiveStyleSheet.instance.bin( optionChoices.get( currentChoice ) );
			choiceContainer.replaceWith( newChoiceContainer );
			choiceContainer = newChoiceContainer;
			
			listener.onOptionMenuChoice( this, oldChoice, currentChoice );
		}
	}
}
