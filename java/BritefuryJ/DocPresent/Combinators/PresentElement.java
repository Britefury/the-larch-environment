//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.DocPresent.DPElement;

public class PresentElement extends Pres
{
	private DPElement element;
	
	
	public PresentElement(DPElement element)
	{
		this.element = element;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		return element;
	}
}
