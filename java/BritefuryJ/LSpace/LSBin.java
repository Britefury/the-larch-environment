//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.LSpace;

import java.util.List;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeBin;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;


public class LSBin extends LSContainerNonOverlayed
{
	public LSBin()
	{
		this( ContainerStyleParams.defaultStyleParams );
	}

	public LSBin(ContainerStyleParams styleParams)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeBin( this );
	}
	
	
	//
	//
	// Child access / modification
	//
	//
	
	public LSElement getChild()
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
	
	public void setChild(LSElement child)
	{
		LSElement prevChild = getChild();
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
	
	
	protected void replaceChildWithEmpty(LSElement child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	protected void replaceChild(LSElement child, LSElement replacement)
	{
		assert child == this.getChild();
		setChild( replacement );
	}
	
	

	public List<LSElement> getChildren()
	{
		return registeredChildren;
	}

	
	public boolean isSingleElementContainer()
	{
		return true;
	}
}
