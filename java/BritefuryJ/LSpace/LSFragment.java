//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.LSpace.FragmentTree.FragmentIterator;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;

public class LSFragment extends LSProxy
{
	protected FragmentContext context;


	public LSFragment(FragmentContext context, LSElement child)
	{
		this( context, ContainerStyleParams.defaultStyleParams, child );
	}

	public LSFragment(FragmentContext context, ContainerStyleParams styleParams, LSElement child)
	{
		super( styleParams, child );
		
		this.context = context;
	}
	
	
	//
	// Context
	//
	
	// DPElement.getContext() is the public method to get the element context, this helper method
	// simply retrieves the context at this element, DPElement.getContext() will search parent elements
	// for it.
	protected FragmentContext getContextOfFragment()
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
