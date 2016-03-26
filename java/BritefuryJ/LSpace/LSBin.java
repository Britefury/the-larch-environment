//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;

import java.util.List;

import BritefuryJ.LSpace.LayoutTree.LayoutNodeBin;
import BritefuryJ.LSpace.StyleParams.ContainerStyleParams;


public class LSBin extends LSContainerNonOverlayed
{
	protected final static int FLAGS_BIN_END = FLAGS_CONTAINERNONOVERLAYED_END;

	
	public LSBin(ContainerStyleParams styleParams, LSElement child)
	{
		super(styleParams);
		
		layoutNode = new LayoutNodeBin( this );

		if ( child != null )
		{
			if ( child.getLayoutNode() == null )
			{
				throw new ChildHasNoLayoutException();
			}

			registeredChildren.add( child );
			registerChild( child );				
		}
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
