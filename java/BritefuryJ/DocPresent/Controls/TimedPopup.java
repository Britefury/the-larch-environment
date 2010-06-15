//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import BritefuryJ.DocPresent.DPElement;

public class TimedPopup
{
	private DPElement popupElement;
	private double timeout;
	private boolean bRequestFocus;
	
	
	public TimedPopup(DPElement popupElement, double timeout, boolean bRequestFocus)
	{
		this.popupElement = popupElement;
		this.timeout = timeout;
		this.bRequestFocus = bRequestFocus;
	}



	public void popupToRightOf(DPElement element)
	{
		popupElement.popupToRightOf( element, true, bRequestFocus );
		initialiseTimeout();
	}
	
	public void popupBelow(DPElement element)
	{
		popupElement.popupBelow( element, true, bRequestFocus );
		initialiseTimeout();
	}
	
	public void popupAtMousePosition(DPElement element)
	{
		element.getRootElement().createPopupAtMousePosition( popupElement, true, bRequestFocus );
		initialiseTimeout();
	}
	
	
	
	private void initialiseTimeout()
	{
		ActionListener timeoutListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				popupElement.closeContainingPopupChain();
			}
		};
		
		final Timer timer = new Timer( (int)( timeout * 1000.0 ), timeoutListener );
		timer.setRepeats( false );
		timer.start();
	}
}
