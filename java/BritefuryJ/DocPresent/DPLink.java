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

public class DPLink extends DPStaticText
{
	protected String targetLocation;
	
	
	public DPLink(String text, String targetLocation)
	{
		this( LinkStyleSheet.defaultStyleSheet, text, targetLocation );
	}
	
	public DPLink(LinkStyleSheet styleSheet, String text, String targetLocation)
	{
		super( styleSheet, text );
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
		if ( isRealised() )
		{
			presentationArea.setCursorArrow( event.getPointer() );
		}
		super.onLeave( event );
	}



	protected boolean onButtonDown(PointerButtonEvent event)
	{
		super.onButtonDown( event );
		
		if ( isRealised() )
		{
			if ( event.button == 1 )
			{
				PageController pageController = presentationArea.getPageController();
				pageController.goToLocation( targetLocation );
				return true;
			}
		}
		
		return false;
	}
}
