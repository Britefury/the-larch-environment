//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBorder;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementInteractor;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.Primitive.Arrow;
import BritefuryJ.DocPresent.Combinators.Primitive.Bin;
import BritefuryJ.DocPresent.Combinators.Primitive.Border;
import BritefuryJ.DocPresent.Combinators.Primitive.HBox;
import BritefuryJ.DocPresent.Combinators.Primitive.Primitive;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.Painter.Painter;
import BritefuryJ.DocPresent.StyleSheet.StyleSheet2;
import BritefuryJ.DocPresent.StyleSheet.StyleSheetValues;

public class OptionMenu extends Pres
{
	public static interface OptionMenuListener
	{
		public void onOptionMenuChoice(OptionMenu optionMenu, int previousChoice, int choice);
	}
	
	
	
	private class OptionMenuInteractor extends ElementInteractor
	{
		private BritefuryJ.DocPresent.Border.Border border, hoverBorder;
		
		private OptionMenuInteractor(BritefuryJ.DocPresent.Border.Border border, BritefuryJ.DocPresent.Border.Border hoverBorder)
		{
			this.border = border;
			this.hoverBorder = hoverBorder;
		}
		
		
		public boolean onButtonDown(DPElement element, PointerButtonEvent event)
		{
			return true;
		}

		public boolean onButtonUp(DPElement element, PointerButtonEvent event)
		{
			if ( element.isRealised() )
			{
				displayDropdown( element );
				return true;
			}
			
			return false;
		}
		
		
		public void onEnter(DPElement element, PointerMotionEvent event)
		{
			((DPBorder)element).setBorder( hoverBorder );
		}

		public void onLeave(DPElement element, PointerMotionEvent event)
		{
			((DPBorder)element).setBorder( border );
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
		public void onMenuItemClicked(MenuItem menuItem, DPElement element)
		{
			setChoice( choiceIndex );
		}
	}
	
	

	private Pres choices[];
	private int currentChoice;
	private OptionMenuListener listener;
	
	private WeakHashMap<DPElement, PresentationContext> choiceBins = new WeakHashMap<DPElement, PresentationContext>();
	private WeakHashMap<DPElement, PresentationContext> optionMenus = new WeakHashMap<DPElement, PresentationContext>();
	private PopupMenu popupMenu;
	
	
	
	
	private OptionMenu(Pres choices[], int initialChoice, OptionMenuListener listener)
	{
		this.choices = choices;
		currentChoice = initialChoice;
		this.listener = listener;
		
		
		MenuItem menuItems[] = new MenuItem[choices.length];
		int i = 0;
		for (Pres choice: this.choices)
		{
			menuItems[i] = new MenuItem( choice, new MenuItemListener( i ) );
			i++;
		}
		popupMenu = new VPopupMenu( menuItems );
	}

	public OptionMenu(List<Object> choices, int initialChoice, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), initialChoice, listener );
	}

	public OptionMenu(Object choices[], int initialChoice, OptionMenuListener listener)
	{
		this( mapCoerce( choices ), initialChoice, listener );
	}
	
	
	
	
	
	private void displayDropdown(DPElement element)
	{
		PresentationContext ctx = optionMenus.get( element );
		ctx = ctx.withStyle( ctx.getStyle().withAttr( Controls.bClosePopupOnActivate, true ) );
		popupMenu.popupBelow( element, ctx );
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
			
			for (Map.Entry<DPElement, PresentationContext> entry: choiceBins.entrySet())
			{
				Pres newChoiceContainer = new Bin( choices[currentChoice] );
				((DPBin)entry.getKey()).setChild( newChoiceContainer.present( entry.getValue() ) );
			}
			
			for (Map.Entry<DPElement, PresentationContext> entry: optionMenus.entrySet())
			{
				entry.getKey().setFixedValue( currentChoice );
			}
			
			listener.onOptionMenuChoice( this, oldChoice, currentChoice );
		}
	}
	
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		StyleSheetValues style = ctx.getStyle();
		PresentationContext usedStyleCtx = Controls.useOptionMenuAttrs( ctx );
		
		StyleSheet2 arrowStyle = StyleSheet2.instance.withAttr( Primitive.shapePainter, style.get( Controls.optionMenuArrowPainter, Painter.class ) );
		double arrowSize = style.get( Controls.optionMenuArrowSize, Double.class );
		Pres arrow = arrowStyle.applyTo( new Arrow( Arrow.Direction.DOWN, arrowSize ) );
		
		Pres choiceBin = new Bin( new Bin( choices[currentChoice] ) );
		DPElement choiceBinElement = choiceBin.present( usedStyleCtx );
		choiceBins.put( choiceBinElement, usedStyleCtx );
		
		BritefuryJ.DocPresent.Border.Border border = style.get( Controls.optionMenuBorder, BritefuryJ.DocPresent.Border.Border.class );
		BritefuryJ.DocPresent.Border.Border hoverBorder = style.get( Controls.optionMenuHoverBorder, BritefuryJ.DocPresent.Border.Border.class );
		StyleSheet2 optionStyle = StyleSheet2.instance.withAttr( Primitive.hboxSpacing, style.get( Controls.optionMenuContentsSpacing, Double.class ) ).withAttr( Primitive.border, border );
		Pres optionContents = new HBox( new Pres[] { coerce( choiceBinElement ).alignHExpand().alignVCentre(), arrow.alignVCentre() } );
		Pres optionMenu = optionStyle.applyTo( new Border( optionContents.alignHExpand() ) ); 
		DPElement optionMenuElement = optionMenu.present( ctx );
		optionMenuElement.addInteractor( new OptionMenuInteractor( border, hoverBorder ) );
		optionMenus.put( optionMenuElement, ctx );
		return optionMenuElement;
	}
}
