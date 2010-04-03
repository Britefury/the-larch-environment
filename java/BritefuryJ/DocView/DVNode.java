//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocView;

import BritefuryJ.DocPresent.DPFragment;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.FragmentContext;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;

public class DVNode extends IncrementalTreeNode
{
	public static class CannotChangeDocNodeException extends Exception
	{
		private static final long serialVersionUID = 1L;
	}

	

	
	
	private DPFragment fragmentElement;
	private DPElement element;
	
	
	
	public DVNode(DocView view, Object docNode, NodeResultChangeListener resultChangeListener)
	{
		super( view, docNode, resultChangeListener );
		
		// Fragment element, with null context, initially; later set in @setContext method
		fragmentElement = new DPFragment( null );
		element = null;
	}
	
	
	
	//
	//
	// Result acquisition methods
	//
	//
	
	public DPElement getElementNoRefresh()
	{
		return fragmentElement;
	}
	
	public DPElement getElement()
	{
		refresh();
		return fragmentElement;
	}
	
	
	public DPElement getInnerElementNoRefresh()
	{
		return element;
	}
	
	
	public Object getResultNoRefresh()
	{
		return fragmentElement;
	}
	
	public Object getResult()
	{
		refresh();
		return fragmentElement;
	}
	

	
	
	//
	//
	// Document view and node / tree methods
	//
	//
	
	public DocView getDocView()
	{
		return (DocView)getIncrementalTree();
	}
	
	
	
	
	//
	//
	// Context methods
	//
	//
	
	public void setFragmentContext(FragmentContext elementContext)
	{
		fragmentElement.setFragmentContext( elementContext );
	}
	
	
	
	
	//
	//
	// Refresh methods
	//
	//
	
	protected Object computeNodeResult()
	{
		getDocView().profile_startJava();
		Object result = super.computeNodeResult();
		getDocView().profile_stopJava();
		return result;
	}

	protected void updateNodeResult(Object r)
	{
		getDocView().profile_startUpdateNodeElement();
		super.updateNodeResult( r );
		if ( r != element )
		{
			if ( r != null )
			{
				element = (DPElement)r;
				fragmentElement.setChild( element );
			}
			else
			{
				element = null;
				fragmentElement.setChild( null );
			}
		}
		getDocView().profile_stopUpdateNodeElement();
	}
}
