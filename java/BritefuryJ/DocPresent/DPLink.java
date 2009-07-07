//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Event.PointerMotionEvent;
import BritefuryJ.DocPresent.StyleSheets.LinkStyleSheet;

public class DPLink extends DPText
{
	protected String targetLocation;
	
	
	public DPLink(String text, String targetLocation)
	{
		this( LinkStyleSheet.defaultStyleSheet, text, text, targetLocation );
	}
	
	public DPLink(String text, String textRepresentation, String targetLocation)
	{
		this( LinkStyleSheet.defaultStyleSheet, text, textRepresentation, targetLocation );
	}
	
	public DPLink(LinkStyleSheet styleSheet, String text, String targetLocation)
	{
		this( styleSheet, text, text, targetLocation );
	}

	public DPLink(LinkStyleSheet styleSheet, String text, String textRepresentation, String targetLocation)
	{
		super( styleSheet, text, textRepresentation );
		this.targetLocation = targetLocation;
	}




	protected void onEnter(PointerMotionEvent event)
	{
		super.onEnter( event );
		if ( isRealised() )
		{
			presentationArea.setCursorHand( event.getPointer() );
		}
	}

	protected void onLeave(PointerMotionEvent event)
	{
		super.onLeave( event );
		if ( isRealised() )
		{
			presentationArea.setCursorArrow( event.getPointer() );
		}
	}



	protected boolean onButtonDown(PointerButtonEvent event)
	{
		super.onButtonDown( event );
		
		PageController pageController = presentationArea.getPageController();
		pageController.goToLocation( targetLocation );
		return true;
	}
}
