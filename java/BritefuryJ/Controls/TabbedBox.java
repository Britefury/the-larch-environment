//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Controls;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPRow;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.PointerInputElement;
import BritefuryJ.DocPresent.Interactor.PushElementInteractor;
import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.VAlignment;
import BritefuryJ.Math.Point2;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Blank;
import BritefuryJ.Pres.Primitive.Column;
import BritefuryJ.Pres.Primitive.Primitive;
import BritefuryJ.Pres.Primitive.Row;
import BritefuryJ.StyleSheet.StyleValues;

public class TabbedBox extends ControlPres
{
	public static interface TabbedBoxListener
	{
		public void onTab(TabbedBoxControl expander, int tab);
	}


	public static class TabbedBoxControl extends Control
	{
		private DPElement element;
		private DPBin contentsElement;
		
		private Pres tabContents[];
		private int currentTab;
		private TabbedBoxListener listener;
		
		
		protected TabbedBoxControl(PresentationContext ctx, StyleValues style, DPElement element, DPBin contentsElement, Pres tabContents[], int initialTab, TabbedBoxListener listener)
		{
			super( ctx, style );
			this.element = element;
			this.contentsElement = contentsElement;

			this.tabContents = tabContents;
			currentTab = -1;
			setTab( initialTab );
			this.listener = listener;
		}
		
		
		
		
		@Override
		public DPElement getElement()
		{
			return element;
		}
		
		
		public int getTab()
		{
			return currentTab;
		}
		
		public void setTab(int tab)
		{
			if ( tab != currentTab )
			{
				currentTab = tab;
				
				Pres contents = tabContents[currentTab];
				contentsElement.setChild( contents.present( ctx, style ).layoutWrap( style.get( Primitive.hAlign, HAlignment.class ), style.get( Primitive.vAlign, VAlignment.class ) ) );
				if ( listener != null )
				{
					listener.onTab( this, currentTab );
				}
			}
		}
	}

	
	
	protected static class TabbedBoxHeaderInteractor implements PushElementInteractor
	{
		TabbedBoxControl control;
		
		protected TabbedBoxHeaderInteractor()
		{
		}
		
		
		@Override
		public boolean buttonPress(PointerInputElement element, PointerButtonEvent event)
		{
			return event.getButton() == 1;
		}


		@Override
		public void buttonRelease(PointerInputElement element, PointerButtonEvent event)
		{
			DPRow header = (DPRow)element;
			Point2 clickPos = event.getLocalPointerPos();
			int i = 0;
			for (DPElement child: header.getChildren())
			{
				if ( child.containsParentSpacePoint( clickPos ) )
				{
					control.setTab( i );
					break;
				}
				i++;
			}
		}
	}




	
	private Pres tabs[][];
	private int initialTab;
	private TabbedBoxListener listener;

	
	public TabbedBox(Pres tabs[][], int initialTab, TabbedBoxListener listener)
	{
		super();

		this.tabs = tabs;
		this.initialTab = initialTab;
		this.listener = listener;
	}




	@Override
	public Control createControl(PresentationContext ctx, StyleValues style)
	{
		StyleValues usedStyle = Controls.useTabsAttrs( style );

		
		Pres headers[] = new Pres[tabs.length];
		Pres contents[] = new Pres[tabs.length];
		for (int i = 0; i < tabs.length; i++)
		{
			headers[i] = new Bin( tabs[i][0] );
			contents[i] = tabs[i][1];
		}

		
		// Build the header
		TabbedBoxHeaderInteractor headerInteractor = new TabbedBoxHeaderInteractor();
		Pres header = new Row( headers ).withElementInteractor( headerInteractor ).alignHExpand();
		
		
		
		// Handle contents
		Pres contentsBox = new Bin( new Blank() ).alignHExpand().alignVExpand();
		DPBin contentsElement = (DPBin)contentsBox.present( ctx, usedStyle );
		
		
		// Tabs
		Pres tabs = new Column( new Object[] { header, contentsElement } );
		DPElement element = tabs.present( ctx, usedStyle );
		
		
		// Control
		TabbedBoxControl control = new TabbedBoxControl( ctx, usedStyle, element, contentsElement, contents, initialTab, listener );
		headerInteractor.control = control;
		return control;
	}
}
