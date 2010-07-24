//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import java.lang.ref.WeakReference;

import BritefuryJ.DocPresent.DPElement;

public class ElementRef extends Pres
{
	private Pres child;
	private WeakReference<DPElement> lastElement = null;
	
	
	public ElementRef(Pres child)
	{
		this.child = child;
	}
	
	
	public DPElement getLastElement()
	{
		if ( lastElement != null )
		{
			return lastElement.get();
		}
		else
		{
			return null;
		}
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPElement element = child.present( ctx );
		lastElement = new WeakReference<DPElement>( element );
		return element;
	}
}
