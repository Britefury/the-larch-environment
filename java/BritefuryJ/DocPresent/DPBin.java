//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.LayoutTree.LayoutNodeBin;
import BritefuryJ.DocPresent.StyleParams.ContainerStyleParams;


public class DPBin extends DPContainer
{
	public DPBin()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public DPBin(ContainerStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeBin( this );
	}
	
	
	//
	//
	// Child access / modification
	//
	//
	
	public DPElement getChild()
	{
		if ( registeredChildren.size() > 0 )
		{
			return registeredChildren.get( 0 );
		}
		else
		{
			return null;
		}
	}
	
	public void setChild(DPElement child)
	{
		DPElement prevChild = getChild();
		if ( child != prevChild )
		{
			if ( child != null  &&  child.getLayoutNode() == null )
			{
				throw new ChildHasNoLayoutException();
			}

			if ( prevChild != null )
			{
				unregisterChild( prevChild );
				registeredChildren.remove( 0 );
			}
			
			if ( child != null )
			{
				registeredChildren.add( child );
				registerChild( child );				
			}
			
			onChildListModified();
			queueResize();
		}
	}
	
	
	protected void replaceChildWithEmpty(DPElement child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	protected void replaceChild(DPElement child, DPElement replacement)
	{
		assert child == this.getChild();
		setChild( replacement );
	}
	
	

	public List<DPElement> getChildren()
	{
		return registeredChildren;
	}

	
	public boolean isSingleElementContainer()
	{
		return true;
	}
}
