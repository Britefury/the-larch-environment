//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.List;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Event.AbstractPointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerButtonClickedEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.ClickElementInteractor;
import BritefuryJ.DocPresent.Interactor.HoverElementInteractor;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Arrow;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Border;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleSheet;
import BritefuryJ.StyleSheet.StyleValues;

public class OptionMenu extends ControlPres
{
	public static interface OptionMenuListener
	{
		public void onOptionMenuChoice(OptionMenuControl optionMenu, int previousChoice, int choice);
	}
	
	
	
	public static class OptionMenuControl extends Control
	{
		private class OptionMenuInteractor implements ClickElementInteractor, HoverElementInteractor
		{
			private OptionMenuInteractor()
			{
			}
			
			
			@Override
			public boolean testClickEvent(PointerInputElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(PointerInputElement element, PointerButtonClickedEvent event)
			{
				displayDropdown();
				return true;
			}
			
			
			@Override
			public void pointerEnter(PointerInputElement element, PointerMotionEvent event)
			{
				((DPBorder)element).setBorder( optionMenuHoverBorder );
			}

			@Override
			public void pointerLeave(PointerInputElement element, PointerMotionEvent event)
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
			public void onMenuItemClicked(MenuItem.MenuItemControl menuItem)
			{
				setChoice( choiceIndex );
			}
		}
		
		

		private DPBorder element;
		private BritefuryJ.DocPresent.Border.AbstractBorder optionMenuBorder, optionMenuHoverBorder;
		private DPBin choiceContainer;
		private PopupMenu choiceMenu;
		private Pres choices[];
		private int currentChoice;
		private OptionMenuListener listener;
		
		
		protected OptionMenuControl(PresentationContext ctx, StyleValues style, DPBorder element, DPBin choiceContainer, Pres choices[], int initialChoice, OptionMenuListener listener,
				BritefuryJ.DocPresent.Border.AbstractBorder optionMenuBorder, BritefuryJ.DocPresent.Border.AbstractBorder optionMenuHoverBorder)
		{
			super( ctx, style );
			this.element = element;
			this.optionMenuBorder = optionMenuBorder;
			this.optionMenuHoverBorder = optionMenuHoverBorder;
			this.choiceContainer = choiceContainer;
			this.choices = choices;
			currentChoice = initialChoice;
			this.listener = listener;
			
			element.addElementInteractor( new OptionMenuInteractor() );

			MenuItem menuItems[] = new MenuItem[choices.length];
			int i = 0;
			for (Pres choice: this.choices)
			{
				menuItems[i] = new MenuItem( choice, new MenuItemListener( i ) );
				i++;
			}
			choiceMenu = new VPopupMenu( menuItems );
		}
		
		
		
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		
		
		private void displayDropdown()
		{
			choiceMenu.popupBelow( element, ctx, style.withAttr( Controls.bClosePopupOnActivate, true ) );
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
				
				Pres newChoiceContainer = new Bin( choices[currentChoice] );
				DPBin newChoiceContainerElement = (DPBin)newChoiceContainer.present( ctx, style );
				choiceContainer.replaceWith( newChoiceContainerElement );
				choiceContainer = newChoiceContainerElement;
				
				element.setFixedValue( currentChoice );
				listener.onOptionMenuChoice( this, oldChoice, currentChoice );
			}
		}
	}

	
	
	

	private Pres choices[];
	private int initialChoice;
	private OptionMenuListener listener;
	
	
	
	
	private OptionMenu(Pres choices[], int initialChoice, OptionMenuListener listener)
	{
		this.choices = choices;
		this.initialChoice = initialChoice;
		this.listener = listener;
	}

	public OptionMenu(List<Object> choices, int initialChoice, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), initialChoice, listener );
	}

	public OptionMenu(Object choices[], int initialChoice, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), initialChoice, listener );
	}
	
	
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useOptionMenuAttrs( style );
		
		StyleSheet arrowStyle = StyleSheet.instance.withAttr( Primitive.shapePainter, style.get( Controls.optionMenuArrowPainter, Painter.class ) );
		double arrowSize = style.get( Controls.optionMenuArrowSize, Double.class );
		Pres arrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.DOWN, arrowSize ) );
		
		Pres choiceBin = new Bin( new Bin( choices[initialChoice] ) );
		DPBin choiceContainer = (DPBin)choiceBin.present( ctx, usedStyle );
		
		BritefuryJ.DocPresent.Border.AbstractBorder border = style.get( Controls.optionMenuBorder, BritefuryJ.DocPresent.Border.AbstractBorder.class );
		BritefuryJ.DocPresent.Border.AbstractBorder hoverBorder = style.get( Controls.optionMenuHoverBorder, BritefuryJ.DocPresent.Border.AbstractBorder.class );
		StyleSheet optionStyle = StyleSheet.instance.withAttr( Primitive.rowSpacing, style.get( Controls.optionMenuContentsSpacing, Double.class ) ).withAttr( Primitive.border, border );
		Pres optionContents = new Row( new Pres[] { coerce( choiceContainer ).alignHExpand(), arrow.alignVCentre() } );
		Pres optionMenu = optionStyle.applyTo( new Border( optionContents.alignHExpand() ) ); 
		DPBorder optionMenuElement = (DPBorder)optionMenu.present( ctx, style );
		
		
		return new OptionMenuControl( ctx, style, optionMenuElement, choiceContainer, choices, initialChoice, listener, border, hoverBorder );
	}
}
