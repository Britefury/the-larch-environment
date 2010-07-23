//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.FragmentTree.FragmentIterator;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;

public class DPFragment extends DPProxy
{
	protected FragmentContext context;


	public DPFragment(FragmentContext context)
	{
		super();
		
		this.context = context;
	}

	public DPFragment(FragmentContext context, ContainerStyleParams styleParams)
	{
		super(styleParams);
		
		this.context = context;
	}
	
	protected DPFragment(DPFragment element)
	{
		super( element );
		
		context = element.context;
	}
	
	
	//
	//
	// Presentation tree cloning
	//
	//
	
	public DPElement clonePresentationSubtree()
	{
		DPFragment clone = new DPFragment( this );
		clone.clonePostConstuct( this );
		return clone;
	}





	//
	// Context
	//
	
	// DPElement.getContext() is the public method to get the element context, this helper method
	// simply retrieves the context at this element, DPElement.getContext() will search parent elements
	// for it.
	protected FragmentContext getFragmentContext_helper()
	{
		return context;
	}
	
	public FragmentContext getFragmentContext()
	{
		return context;
	}
	
	public void setFragmentContext(FragmentContext context)
	{
		this.context = context;
	}
	
	
	
	public FragmentIterator fragmentIterator()
	{
		return new FragmentIterator( this );
	}
}
