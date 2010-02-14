//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.StyleSheets.ContainerStyleSheet;

public class DPFragment extends DPProxy
{
	protected ElementContext context;


	public DPFragment(ElementContext context)
	{
		super();
		
		this.context = context;
	}

	public DPFragment(ElementContext context, ContainerStyleSheet styleSheet)
	{
		super( styleSheet );
		
		this.context = context;
	}





	//
	// Context
	//
	
	// DPWidget.getContext() is the public method to get the element context, this helper method
	// simply retrieves the context at this element, DPWidget.getContext() will search parent elements
	// for it.
	protected ElementContext getContext_helper()
	{
		return context;
	}
	
	public ElementContext getContext()
	{
		return context;
	}
	
	public void setContext(ElementContext context)
	{
		this.context = context;
	}
}
