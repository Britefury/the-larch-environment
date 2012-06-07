//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.List;

import BritefuryJ.Graphics.Painter;
import BritefuryJ.LSpace.LSBorder;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Event.PointerMotionEvent;
import BritefuryJ.LSpace.Interactor.ClickElementInteractor;
import BritefuryJ.LSpace.Interactor.HoverElementInteractor;
import BritefuryJ.Live.LiveFunction;
import BritefuryJ.Live.LiveInterface;
import BritefuryJ.Live.LiveValue;
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
			public boolean testClickEvent(LSElement element, AbstractPointerButtonEvent event)
			{
				return event.getButton() == 1;
			}

			@Override
			public boolean buttonClicked(LSElement element, PointerButtonClickedEvent event)
			{
				displayDropdown();
				return true;
			}
			
			
			@Override
			public void pointerEnter(LSElement element, PointerMotionEvent event)
			{
				((LSBorder)element).setBorder( optionMenuHoverBorder );
			}

			@Override
			public void pointerLeave(LSElement element, PointerMotionEvent event)
			{
				((LSBorder)element).setBorder( optionMenuBorder );
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
		
		

		private LSBorder element;
		private BritefuryJ.Graphics.AbstractBorder optionMenuBorder, optionMenuHoverBorder;
		private PopupMenu choiceMenu;
		private Pres choices[];
		private LiveInterface currentChoice;
		private OptionMenuListener listener;
		
		
		protected OptionMenuControl(PresentationContext ctx, StyleValues style, LSBorder element, Pres choices[], LiveInterface currentChoice, OptionMenuListener listener,
				BritefuryJ.Graphics.AbstractBorder optionMenuBorder, BritefuryJ.Graphics.AbstractBorder optionMenuHoverBorder)
		{
			super( ctx, style );
			this.element = element;
			this.optionMenuBorder = optionMenuBorder;
			this.optionMenuHoverBorder = optionMenuHoverBorder;
			this.choices = choices;
			this.currentChoice = currentChoice;
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
		public LSElement getElement()
		{
			return element;
		}
		
		
		
		private void displayDropdown()
		{
			choiceMenu.popupBelow( element, style.withAttr( Controls.bClosePopupOnActivate, true ) );
		}
		
		
		public int getChoice()
		{
			return (Integer)currentChoice.getStaticValue();
		}
		
		private void setChoice(int choice)
		{
			int currentChoice = getChoice();
			if ( choice != currentChoice )
			{
				listener.onOptionMenuChoice( this, currentChoice, choice );
			}
		}
	}

	
	
	
	private static class CommitListener implements OptionMenuListener
	{
		private LiveValue value;
		private OptionMenuListener listener;
		
		public CommitListener(LiveValue value, OptionMenuListener listener)
		{
			this.value = value;
			this.listener = listener;
		}
		
		@Override
		public void onOptionMenuChoice(OptionMenuControl optionMenu, int previousChoice, int choice)
		{
			if ( listener != null )
			{
				listener.onOptionMenuChoice( optionMenu, previousChoice, choice );
			}
			value.setLiteralValue( choice );
		}
	}
	
	

	private Pres choices[];
	private LiveSource valueSource;
	private OptionMenuListener listener;
	
	
	
	private OptionMenu(Pres choices[], LiveSource valueSource, OptionMenuListener listener)
	{
		this.choices = choices;
		this.valueSource = valueSource;
		this.listener = listener;
	}

	
	
	private OptionMenu(Pres choices[], int initialChoice, OptionMenuListener listener)
	{
		LiveValue value = new LiveValue( initialChoice );
		
		this.choices = choices;
		this.valueSource = new LiveSourceRef( value );
		this.listener = new CommitListener( value, listener );
	}

	public OptionMenu(List<Object> choices, int initialChoice, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), initialChoice, listener );
	}

	public OptionMenu(Object choices[], int initialChoice, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), initialChoice, listener );
	}
	
	
	private OptionMenu(Pres choices[], LiveInterface value, OptionMenuListener listener)
	{
		this( choices, new LiveSourceRef( value ), listener );
	}

	public OptionMenu(List<Object> choices, LiveInterface value, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), value, listener );
	}

	public OptionMenu(Object choices[], LiveInterface value, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), value, listener );
	}
	
	
	private OptionMenu(Pres choices[], LiveValue value)
	{
		this( choices, new LiveSourceRef( value ), new CommitListener( value, null ) );
	}

	public OptionMenu(List<Object> choices, LiveValue value)
	{
		this( mapCoerce( choices ), value );
	}

	public OptionMenu(Object choices[], LiveValue value)
	{
		this( mapCoerce( choices ), value );
	}
	
	
	
	
	
	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		final StyleValues usedStyle = Controls.useOptionMenuAttrs( style );

		StyleSheet arrowStyle = StyleSheet.style( Primitive.shapePainter.as( style.get( Controls.optionMenuArrowPainter, Painter.class ) ) );
		double arrowSize = style.get( Controls.optionMenuArrowSize, Double.class );
		Pres arrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.DOWN, arrowSize ) );
		
		final LiveInterface value = valueSource.getLive();

		LiveFunction.Function optionCurrentFn = new LiveFunction.Function()
		{
			@Override
			public Object evaluate()
			{
				int val = (Integer)value.getValue();
				Pres p = usedStyle.applyTo( new Bin( choices[val] ) );
				p = p.withFixedValue( val );
				return p;
			}
		};
		
		LiveFunction optionCurrentLive = new LiveFunction( optionCurrentFn );
		
		BritefuryJ.Graphics.AbstractBorder border = style.get( Controls.optionMenuBorder, BritefuryJ.Graphics.AbstractBorder.class );
		BritefuryJ.Graphics.AbstractBorder hoverBorder = style.get( Controls.optionMenuHoverBorder, BritefuryJ.Graphics.AbstractBorder.class );
		StyleSheet optionStyle = StyleSheet.style( Primitive.rowSpacing.as( style.get( Controls.optionMenuContentsSpacing, Double.class ) ), Primitive.border.as( border ) );
		Pres optionContents = new Row( new Pres[] { coerce( optionCurrentLive ).alignHExpand(), arrow.alignHPack().alignVCentre() } );
		Pres optionMenu = optionStyle.applyTo( new Border( optionContents ) ); 
		LSBorder optionMenuElement = (LSBorder)optionMenu.present( ctx, style );
		
		
		return new OptionMenuControl( ctx, style, optionMenuElement, choices, value, listener, border, hoverBorder );
	}
}
