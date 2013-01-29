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
	// List of popup windows. The popup at the tip is at index 0, the last is the one closest to the root
	private ArrayList<PresentationPopupWindow> popups = new ArrayList<PresentationPopupWindow>();
	protected RootPresentationComponent owner;
	private boolean closed = false, ignoreCloseNotifications = false, focusRequested = false;
	
	
	public PopupChain(RootPresentationComponent owner)
	{
		this.owner = owner;
	}


	protected boolean isEmpty()
	{
		return popups.isEmpty();
	}

	protected boolean wasFocusRequested()
	{
		return focusRequested;
	}
	
	
	protected void addPopup(PresentationPopupWindow popup, boolean focusRequested)
	{
		if ( popups.size() == 0 )
		{
			this.focusRequested = focusRequested;
		}
		popups.add( 0, popup );
	}
	
	
	protected boolean popupHasChild(PresentationPopupWindow popup)
	{
		return getChildOf( popup ) != null;
	}

	protected PresentationPopupWindow getChildOf(PresentationPopupWindow popup)
	{
		int index = popups.indexOf( popup );
		if ( index == -1 )
		{
			throw new RuntimeException( "Could not find popup in chain" );
		}
		return index > 0  ?  popups.get( index - 1 )  :  null;
	}


	protected void autoCloseChildrenOf(PresentationPopupWindow popup)
	{
		PresentationPopupWindow child = getChildOf( popup );
		if ( child != null  &&  child.closeAutomatically )
		{
			closeAllChildrenOf( popup );
		}
	}

	protected void autoCloseChain()
	{
		if ( !isEmpty() )
		{
			PresentationPopupWindow child = popups.get( popups.size() - 1 );
			if ( child.closeAutomatically )
			{
				closeChain();
			}
		}
	}

	protected void closeAllChildrenOf(PresentationPopupWindow popup)
	{
		ignoreCloseNotifications = true;
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
		ignoreCloseNotifications = false;
		notifyOwnerIfDead();
	}
	
	protected void closeChainNotContainingPointers()
	{
		ignoreCloseNotifications = true;
		int index = 0;
		for (PresentationPopupWindow p: popups)
		{
			LSRootElement rootElement = p.popupComponent.getRootElement();
			if ( rootElement.getInputTable().arePointersWithinBoundsOfElement( rootElement ) )
			{
				ArrayList<PresentationPopupWindow> ps = new ArrayList<PresentationPopupWindow>();
				ps.addAll( popups.subList( index, popups.size() ) );
				popups = ps;
				ignoreCloseNotifications = false;
				notifyOwnerIfDead();
				return;
			}
			else
			{
				p.closePopup();
			}
			
			index++;
		}
		popups.clear();
		ignoreCloseNotifications = false;
		notifyOwnerIfDead();
	}


	public void closeChain()
	{
		ignoreCloseNotifications = true;
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
		ignoreCloseNotifications = false;
		notifyOwnerIfDead();
	}


	protected void notifyPopupClosed(PresentationPopupWindow popup)
	{
		if ( !ignoreCloseNotifications )
		{
			popups.remove( popup );
			notifyOwnerIfDead();
		}
	}

	protected void notifyOwnerIfDead()
	{
		if ( popups.isEmpty() )
		{
			owner.notifyChainClosed( this );
		}
	}
}
