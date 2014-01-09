//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace;

import java.awt.*;

import javax.swing.JWindow;

import BritefuryJ.LSpace.Layout.HAlignment;
import BritefuryJ.LSpace.Layout.VAlignment;
import BritefuryJ.LSpace.Util.WindowTransparency;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public class PresentationPopupWindow
{
	protected JWindow popupWindow;
	protected PopupPresentationComponent popupComponent;
	protected PopupChain chain;
	private boolean chainStart;
	protected boolean closeAutomatically;
	private Point2 screenAnchorPoint;
	private Anchor popupAnchor;
	
	protected PresentationPopupWindow(PopupChain popupChain, Window ownerWindow, PresentationComponent parentComponent, LSElement popupContents, int screenX, int screenY,
			Anchor popupAnchor, boolean closeAutomatically, boolean requestFocus, boolean chainStart)
	{
		chain = popupChain;
		chain.addPopup( this, requestFocus );
		this.chainStart = chainStart;
		this.closeAutomatically = closeAutomatically;
		this.popupAnchor = popupAnchor;

		// Create the popup window
		popupWindow = new JWindow( ownerWindow );
		if ( requestFocus )
		{
			popupWindow.setAlwaysOnTop( true );
			popupWindow.setFocusable( true );
		}

		popupWindow.getContentPane().setBackground( Color.WHITE );
		
		// Create a presentation component for the popup contents, and add them
		popupComponent = new PopupPresentationComponent( this );
		popupComponent.setPageController( parentComponent.getPageController() );
		popupComponent.getRootElement().setChild( popupContents.layoutWrap( HAlignment.EXPAND, VAlignment.EXPAND ) );
		
		popupWindow.add( popupComponent );
		
		
		popupWindow.pack();
		Dimension sz = popupWindow.getSize();
		screenAnchorPoint = new Point2(screenX, screenY);

		reposition(sz);

		popupWindow.setVisible( true );
		if ( requestFocus )
		{
			popupWindow.requestFocus();
		}
		
		
		WindowTransparency.setWindowOpaque( popupWindow, false );
	}
	
	
	public void closePopup()
	{
		popupWindow.setVisible( false );
		popupWindow.dispose();
		chain.notifyPopupClosed( this );
	}
	
	
	public void closeContainingPopupChain()
	{
		chain.closeChain();
	}
	
	
	public PopupPresentationComponent getPresentationComponent()
	{
		return popupComponent;
	}
	
	
	public Point2 getPositionOnScreen()
	{
		Point loc = popupWindow.getLocation();
		return new Point2(loc.x, loc.y);
	}

	public Vector2 getSizeOnScreen()
	{
		Dimension sz = popupWindow.getSize();
		return new Vector2(sz.width, sz.height);
	}
	
	
	public boolean isChainStart()
	{
		return chainStart;
	}



	protected void autoClosePopupChildren()
	{
		chain.autoCloseChildrenOf( this );
	}

	protected void reposition(Dimension size)
	{
		Point2 windowAnchor = new Point2( size.getWidth() * popupAnchor.getPropX(), size.getHeight() * popupAnchor.getPropY() );

		double posX = screenAnchorPoint.x - windowAnchor.x;
		double posY = screenAnchorPoint.y - windowAnchor.y;

		// Get the bounds in which the popup can be displayed
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		double boundsRight = bounds.x + bounds.width;
		double boundsBottom = bounds.y + bounds.height;

		// Ensure to right of and below the top left corner
		posX = Math.max( posX, bounds.x );
		posY = Math.max( posY, bounds.y );

		// Ensure that it does not extend beyond the right and bottom bounds
		posX = Math.min( posX,  boundsRight - size.getWidth() );
		posY = Math.min( posY,  boundsBottom - size.getHeight() );

		popupWindow.setLocation( (int)(posX + 0.5), (int)(posY + 0.5) );
	}
}
