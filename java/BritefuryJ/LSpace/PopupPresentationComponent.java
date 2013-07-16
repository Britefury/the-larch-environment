//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Browser.PaneManager;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;

import javax.swing.SwingUtilities;


public class PopupPresentationComponent extends PresentationComponent
{
	private static final long serialVersionUID = 1L;

	private PresentationPopupWindow containingPopup = null;

	

	public PopupPresentationComponent(PresentationPopupWindow containingPopup)
	{
		super();
		this.containingPopup = containingPopup;
	}



	@Override
	public PaneManager getPaneManager()
	{
		return containingPopup.chain.owner.getPaneManager();
	}





	void notifyQueueReallocation()
	{
		Runnable r = new Runnable()
		{
			@Override
			public void run()
			{
				Dimension s = clampSize( rootElement.allocateAndGetPreferredSize() );
				containingPopup.popupWindow.setSize( s );
				containingPopup.reposition( s );
			}
		};
		
		SwingUtilities.invokeLater( r );
	}

	
	//
	// Popup methods
	//
	
	public boolean isPopup()
	{
		return true;
	}
	
	public void closeContainingPopupChain()
	{
		containingPopup.chain.closeChain();
	}
	

	@Override
	protected PresentationPopupWindow createPopupPresentation(LSElement popupContents, int targetX, int targetY, Anchor popupAnchor,
			boolean closeAutomatically, boolean requestFocus, boolean chainStart)
	{
		// Offset the popup position by the location of this presentation component on the screen
		Point locOnScreen = getLocationOnScreen();
		targetX += locOnScreen.x;
		targetY += locOnScreen.y;
		
		// Get the popup chain that this presentation component is a member of
		PopupChain chain = containingPopup.chain;
		// Close any child popups
		chain.closeAllChildrenOf( containingPopup );

		// Get the root presentation component
		RootPresentationComponent root = chain.owner;
		
		// Get the owning window of the root presentation component
		Window ownerWindow = SwingUtilities.getWindowAncestor( root );

		return new PresentationPopupWindow( chain, ownerWindow, this, popupContents, targetX, targetY, popupAnchor, closeAutomatically, requestFocus, chainStart );
	}



	public PresentationEventErrorLog getEventErrorLog()
	{
		return containingPopup.chain.owner.getEventErrorLog();
	}



	@Override
	protected void onPopupClosingEvent()
	{
		containingPopup.autoClosePopupChildren();
	}
}
