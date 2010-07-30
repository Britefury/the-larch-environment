//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.util.Set;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.StyleSheet.StyleValues;

public class ElementRef extends Pres
{
	private Pres child;
	private WeakHashMap<DPElement, PresentationContext> contextByElement = new WeakHashMap<DPElement, PresentationContext>();
	private WeakHashMap<DPElement, StyleValues> styleByElement = new WeakHashMap<DPElement, StyleValues>();
	
	
	public ElementRef(Pres child)
	{
		this.child = child;
	}
	
	
	public Set<DPElement> getElements()
	{
		return contextByElement.keySet();
	}
	
	public PresentationContext getContextForElement(DPElement element)
	{
		return contextByElement.get( element );
	}
	
	public StyleValues getStyleForElement(DPElement element)
	{
		return styleByElement.get( element );
	}
	
	
	public ElementRef elementRef()
	{
		return this;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		contextByElement.put( element, ctx );
		styleByElement.put( element, style );
		return element;
	}
}
