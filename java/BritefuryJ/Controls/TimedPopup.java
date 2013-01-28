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

import BritefuryJ.LSpace.Anchor;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.FragmentContext;
import BritefuryJ.Pres.CustomElementActionPres;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class TimedPopup
{
	private Pres child;
	private double timeout;
	private boolean requestFocus;
	
	
	public TimedPopup(Object child, double timeout, boolean requestFocus)
	{
		this.child = Pres.coerce( child ).withCustomElementAction( action );
		this.timeout = timeout;
		this.requestFocus = requestFocus;
	}



	public void popup(LSElement element, Anchor targetAnchor, Anchor popupAnchor, PresentationContext ctx, StyleValues style)
	{
		child.popup( element, targetAnchor, popupAnchor, true, requestFocus);
	}
	
	public void popupAtMousePosition(LSElement element, Anchor popupAnchor, PresentationContext ctx, StyleValues style)
	{
		child.popupAtMousePosition( element, popupAnchor, true, requestFocus);
	}
	
	
	
	public void popup(LSElement element, Anchor targetAnchor, Anchor popupAnchor)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popup( element, targetAnchor, popupAnchor, ctx.createPresentationContext(), ctx.getStyleValues() );
		}
		else
		{
			popup( element, targetAnchor, popupAnchor, PresentationContext.defaultCtx, StyleValues.getRootStyle() );
		}
	}
	
	public void popupAtMousePosition(LSElement element, Anchor popupAnchor)
	{
		FragmentContext ctx = element.getFragmentContext();
		if ( ctx != null )
		{
			popupAtMousePosition( element, popupAnchor, ctx.createPresentationContext(), ctx.getStyleValues() );
		}
		else
		{
			popupAtMousePosition( element, popupAnchor, PresentationContext.defaultCtx, StyleValues.getRootStyle() );
		}
	}
	
	
	
	private CustomElementActionPres.CustomElementAction action = new CustomElementActionPres.CustomElementAction()
	{
		
		@Override
		public void action(final LSElement element, PresentationContext ctx, StyleValues style)
		{
			ActionListener timeoutListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					element.closeContainingPopupChain();
				}
			};
			
			final Timer timer = new Timer( (int)( timeout * 1000.0 ), timeoutListener );
			timer.setRepeats( false );
			timer.start();
		}
	};
}
