//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.Point;
import java.awt.Window;

import javax.swing.SwingUtilities;

public class RootPresentationComponent extends PresentationComponent
{
	private static final long serialVersionUID = 1L;

	
	private PresentationEventErrorLog eventErrorLog = new PresentationEventErrorLog();


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

	
	protected PresentationPopupWindow createPopupPresentation(LSElement popupContents, int targetX, int targetY, Anchor popupAnchor,
			boolean bCloseOnLoseFocus, boolean bRequestFocus, boolean chainStart)
	{
		// Offset the popup position by the location of this presentation component on the screen
		Point locOnScreen = getLocationOnScreen();
		targetX += locOnScreen.x;
		targetY += locOnScreen.y;
		
		// Create a popup chain rooted here
		PopupChain chain = new PopupChain( this );
		
		// Get the owning window of this presentation component
		Window ownerWindow = SwingUtilities.getWindowAncestor( this );

		return new PresentationPopupWindow( chain, ownerWindow, this, popupContents, targetX, targetY, popupAnchor, bCloseOnLoseFocus, bRequestFocus, chainStart );
	}


	public PresentationEventErrorLog getEventErrorLog()
	{
		return eventErrorLog;
	}
}
