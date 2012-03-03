//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.Controls;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import BritefuryJ.LSpace.Corner;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.FragmentContext;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class TimedPopup
{
	private Pres child;
	private double timeout;
	private boolean bRequestFocus;
	
	
	public TimedPopup(Object child, double timeout, boolean bRequestFocus)
	{
		this.child = Pres.coerce( child );
		this.timeout = timeout;
		this.bRequestFocus = bRequestFocus;
	}



	public void popup(LSElement element, Corner targetAnchor, Corner popupAnchor, PresentationContext ctx, StyleValues style)
	{
		LSElement childElement = child.present( ctx, style );
		childElement.popup( element, targetAnchor, popupAnchor, true, bRequestFocus );
		initialiseTimeout( childElement );
	}
	
	public void popupAtMousePosition(LSElement element, Corner popupAnchor, PresentationContext ctx, StyleValues style)
	{
		LSElement childElement = child.present( ctx, style );
		element.getRootElement().createPopupAtMousePosition( childElement, popupAnchor, true, bRequestFocus );
		initialiseTimeout( childElement );
	}
	
	
	
	public void popup(LSElement element, Corner targetAnchor, Corner popupAnchor)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popup( element, targetAnchor, popupAnchor, ctx.createPresentationContext(), ctx.getStyleValues() );
		}
		else
		{
			popup( element, targetAnchor, popupAnchor, PresentationContext.defaultCtx, StyleValues.instance );
		}
	}
	
	public void popupAtMousePosition(LSElement element, Corner popupAnchor)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupAtMousePosition( element, popupAnchor, ctx.createPresentationContext(), ctx.getStyleValues() );
		}
		else
		{
			popupAtMousePosition( element, popupAnchor, PresentationContext.defaultCtx, StyleValues.instance );
		}
	}
	
	
	
	private void initialiseTimeout(final LSElement childElement)
	{
		ActionListener timeoutListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				childElement.closeContainingPopupChain();
			}
		};
		
		final Timer timer = new Timer( (int)( timeout * 1000.0 ), timeoutListener );
		timer.setRepeats( false );
		timer.start();
	}
}
