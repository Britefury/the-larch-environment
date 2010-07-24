//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.util.Iterator;
import java.util.WeakHashMap;

import BritefuryJ.DocPresent.DPElement;

public class ElementRef extends Pres
{
	public static class RefSet implements Iterable<DPElement>
	{
		private WeakHashMap<DPElement,Object> elements = new WeakHashMap<DPElement,Object>();
		
		private RefSet()
		{
		}
		
		
		private void add(DPElement element)
		{
			elements.put( element, null );
		}
		
		
		@Override
		public Iterator<DPElement> iterator()
		{
			return elements.keySet().iterator();
		}
		
	}
	
	
	private Pres child;
	private RefSet refs = new RefSet();
	
	
	public ElementRef(Pres child)
	{
		this.child = child;
	}
	
	
	public RefSet getRefs()
	{
		return refs;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPElement element = child.present( ctx );
		refs.add( element );
		return element;
	}
}
