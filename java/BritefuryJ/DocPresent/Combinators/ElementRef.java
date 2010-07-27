//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.DPElement;

public class ElementRef extends Pres
{
	private Pres child;
	private WeakHashMap<DPElement, PresentationContext> elements = new WeakHashMap<DPElement, PresentationContext>();
	
	
	public ElementRef(Pres child)
	{
		this.child = child;
	}
	
	
	public Set<DPElement> getElements()
	{
		return elements.keySet();
	}
	
	public Set<Map.Entry<DPElement, PresentationContext>> getElementsAndContexts()
	{
		return elements.entrySet();
	}
	
	public PresentationContext getContextForElement(DPElement element)
	{
		return elements.get( element );
	}
	
	
	public ElementRef elementRef()
	{
		return this;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPElement element = child.present( ctx );
		elements.put( element, ctx );
		return element;
	}
}
