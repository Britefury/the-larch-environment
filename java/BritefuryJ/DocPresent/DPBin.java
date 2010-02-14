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
	
	
	
	public DPWidget getChild()
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
	
	public void setChild(DPWidget child)
	{
		DPWidget prevChild = getChild();
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
	
	
	protected void replaceChildWithEmpty(DPWidget child)
	{
		assert child == this.getChild();
		setChild( null );
	}
	
	

	public List<DPWidget> getChildren()
	{
		return registeredChildren;
	}

	
	
	
	//
	// Text representation methods
	//
	
	protected String computeSubtreeTextRepresentation()
	{
		DPWidget child = getChild();
		return child != null  ?  child.getTextRepresentation()  :  "";
	}
}
