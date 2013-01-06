//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.util.ArrayList;

public class PopupChain
{
	private ArrayList<PresentationPopupWindow> popups = new ArrayList<PresentationPopupWindow>();
	protected RootPresentationComponent owner;
	
	
	public PopupChain(RootPresentationComponent owner)
	{
		this.owner = owner;
	}
	
	
	protected void addPopup(PresentationPopupWindow popup)
	{
		popups.add( 0, popup );
	}
	
	
	protected boolean popupHasChild(PresentationPopupWindow popup)
	{
		int index = popups.indexOf( popup );
		if ( index == -1 )
		{
			throw new RuntimeException( "Could not find popup in chain" );
		}
		return index  > 0;
	}


	protected void closeAllChildrenOf(PresentationPopupWindow popup)
	{
		int index = 0;
		for (PresentationPopupWindow p: popups)
		{
			if ( p != popup )
			{
				p.closePopup();
			}
			else
			{
				ArrayList<PresentationPopupWindow> ps = new ArrayList<PresentationPopupWindow>();
				ps.addAll( popups.subList( index, popups.size() ) );
				popups = ps;
				break;
			}
			index++;
		}
	}
	
	protected void closeChainNotContainingPointers()
	{
		int index = 0;
		for (PresentationPopupWindow p: popups)
		{
			LSRootElement rootElement = p.popupComponent.getRootElement();
			if ( rootElement.getInputTable().arePointersWithinBoundsOfElement( rootElement ) )
			{
				ArrayList<PresentationPopupWindow> ps = new ArrayList<PresentationPopupWindow>();
				ps.addAll( popups.subList( index, popups.size() ) );
				popups = ps;
				return;
			}
			else
			{
				p.closePopup();
			}
			
			index++;
		}
		popups.clear();
	}


	public void closeChain()
	{
		int index = 0;
		for (PresentationPopupWindow p: popups)
		{
			if ( p.isChainStart()  &&  index != 0 )
			{
				// Request focus for remaining popup window
				p.popupWindow.requestFocus();
				break;
			}
			
			p.closePopup();
			
			index++;
		}

		ArrayList<PresentationPopupWindow> ps = new ArrayList<PresentationPopupWindow>();
		ps.addAll( popups.subList( index, popups.size() ) );
		popups = ps;
	}
}
