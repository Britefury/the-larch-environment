//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Browser.PaneManager;

import java.awt.Point;
import java.awt.Window;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

public class RootPresentationComponent extends PresentationComponent
{
	private static final long serialVersionUID = 1L;

	
	private PresentationEventErrorLog eventErrorLog = new PresentationEventErrorLog();
	private ArrayList<PopupChain> popupChains = new ArrayList<PopupChain>();
	private PaneManager paneManager;


	public RootPresentationComponent()
	{
		super();
	}
	
	
	//
	//
	// PANE MANAGEMENT
	//
	//

	@Override
	public PaneManager getPaneManager()
	{
		return paneManager;
	}

	public void setPaneManager(PaneManager m)
	{
		this.paneManager = m;
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
