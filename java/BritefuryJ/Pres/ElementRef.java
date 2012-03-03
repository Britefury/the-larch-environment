//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres;

import java.util.Set;
import java.util.WeakHashMap;

import BritefuryJ.LSpace.LSElement;
import BritefuryJ.StyleSheet.StyleValues;

public class ElementRef extends Pres
{
	private Pres child;
	private WeakHashMap<LSElement, PresentationContext> contextByElement = new WeakHashMap<LSElement, PresentationContext>();
	private WeakHashMap<LSElement, StyleValues> styleByElement = new WeakHashMap<LSElement, StyleValues>();
	
	
	public ElementRef(Pres child)
	{
		this.child = child;
	}
	
	
	public Set<LSElement> getElements()
	{
		return contextByElement.keySet();
	}
	
	public PresentationContext getContextForElement(LSElement element)
	{
		return contextByElement.get( element );
	}
	
	public StyleValues getStyleForElement(LSElement element)
	{
		return styleByElement.get( element );
	}
	
	
	public void queueFullRedraw()
	{
		for (LSElement element: contextByElement.keySet())
		{
			element.queueFullRedraw();
		}
	}
	
	
	public ElementRef elementRef()
	{
		return this;
	}
	
	
	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		contextByElement.put( element, ctx );
		styleByElement.put( element, style );
		return element;
	}
}
