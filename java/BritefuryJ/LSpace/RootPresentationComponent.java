//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class RootPresentationComponent extends PresentationComponent
{
	private static final long serialVersionUID = 1L;

	
	private PresentationEventErrorLog eventErrorLog = new PresentationEventErrorLog();
	private ArrayList<PopupChain> popupChains = new ArrayList<PopupChain>();


	public RootPresentationComponent()
	{
		super();
	}
	
	
	void notifyQueueReallocation()
	{
		if ( !isMinimumSizeSet()  ||  !isPreferredSizeSet()  ||  !isMaximumSizeSet() )
		{
			invalidate();
		}
	}


	@Override
	protected PresentationPopupWindow createPopupPresentation(LSElement popupContents, int targetX, int targetY, Anchor popupAnchor,
			boolean closeAutomatically, boolean requestFocus, boolean chainStart)
	{
		// Offset the popup position by the location of this presentation component on the screen
		Point locOnScreen = getLocationOnScreen();
		targetX += locOnScreen.x;
		targetY += locOnScreen.y;
		
		// Create a popup chain rooted here
		PopupChain chain = new PopupChain( this );
		pruneChains();
		popupChains.add( chain );
		
		// Get the owning window of this presentation component
		Window ownerWindow = SwingUtilities.getWindowAncestor( this );

		return new PresentationPopupWindow( chain, ownerWindow, this, popupContents, targetX, targetY, popupAnchor, closeAutomatically, requestFocus, chainStart );
	}


	public PresentationEventErrorLog getEventErrorLog()
	{
		return eventErrorLog;
	}



	@Override
	protected void onPopupClosingEvent()
	{
		pruneChains();

		ArrayList<PopupChain> chains = new ArrayList<PopupChain>();
		chains.addAll( popupChains );
		for (PopupChain chain: chains)
		{
			chain.autoCloseChain();
		}
	}


	protected void notifyChainClosed(PopupChain chain)
	{
		boolean reacquireFocus = chain.wasFocusRequested();
		popupChains.remove( chain );
		if ( reacquireFocus )
		{
			requestFocus();
		}
	}


	private void pruneChains()
	{
		for (int i = popupChains.size() - 1; i >= 0; i--)
		{
			if ( popupChains.get( i ).isEmpty() )
			{
				popupChains.remove( i );
			}
		}
	}
}
