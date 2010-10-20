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

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

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



	public void popupToRightOf(DPElement element, PresentationContext ctx, StyleValues style)
	{
		DPElement childElement = child.present( ctx, style );
		childElement.popupToRightOf( element, true, bRequestFocus );
		initialiseTimeout( childElement );
	}
	
	public void popupBelow(DPElement element, PresentationContext ctx, StyleValues style)
	{
		DPElement childElement = child.present( ctx, style );
		childElement.popupBelow( element, true, bRequestFocus );
		initialiseTimeout( childElement );
	}
	
	public void popupAtMousePosition(DPElement element, PresentationContext ctx, StyleValues style)
	{
		DPElement childElement = child.present( ctx, style );
		element.getRootElement().createPopupAtMousePosition( childElement, true, bRequestFocus );
		initialiseTimeout( childElement );
	}
	
	
	
	public void popupToRightOf(DPElement element)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupToRightOf( element, ctx.createPresentationContext(), ctx.getStyleValues() );
		}
		else
		{
			popupToRightOf( element, new PresentationContext(), StyleValues.instance );
		}
	}
	
	public void popupBelow(DPElement element)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupBelow( element, ctx.createPresentationContext(), ctx.getStyleValues() );
		}
		else
		{
			popupBelow( element, new PresentationContext(), StyleValues.instance );
		}
	}
	
	public void popupAtMousePosition(DPElement element)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupAtMousePosition( element, ctx.createPresentationContext(), ctx.getStyleValues() );
		}
		else
		{
			popupAtMousePosition( element, new PresentationContext(), StyleValues.instance );
		}
	}
	
	
	
	private void initialiseTimeout(final DPElement childElement)
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
