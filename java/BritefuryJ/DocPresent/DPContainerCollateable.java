//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class DPContainerCollateable extends DPContainer implements Collateable
{
	protected DPContainerSequenceCollationRoot collationRoot;
	protected int collationRangeStart, collationRangeEnd;
	protected AABox2 boundsBoxes[];
	
	
	public DPContainerCollateable(ElementContext context)
	{
		super( context );
		
		layoutReqBox = null;
		layoutAllocBox = null;
		collationRangeStart = -1;
		collationRangeEnd = -1;
	}

	
	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return null;
	}
	
	
	

	//
	// Geometry methods
	//
	
	public Point2 getPositionInParentSpace()
	{
		return new Point2( 0.0, 0.0 );
	}
	
	public double getPositionInParentSpaceX()
	{
		return 0.0;
	}
	
	public double getPositionInParentSpaceY()
	{
		return 0.0;
	}
	
	public double getAllocationX()
	{
		return collationRoot.getAllocationX();
	}
	
	public double getAllocationY()
	{
		return collationRoot.getAllocationY();
	}
	
	public Vector2 getAllocation()
	{
		return collationRoot.getAllocation();
	}
	
	public double getAllocationInParentSpaceX()
	{
		return collationRoot.getAllocationX();
	}
	
	public double getAllocationInParentSpaceY()
	{
		return collationRoot.getAllocationY();
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return collationRoot.getAllocation();
	}

	
	
	public boolean containsParentSpacePoint(Point2 p)
	{
		refreshBoundsBoxes();
		
		for (AABox2 box: boundsBoxes)
		{
			if ( box.containsPoint( p ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean containsLocalSpacePoint(Point2 p)
	{
		refreshBoundsBoxes();
		
		for (AABox2 box: boundsBoxes)
		{
			if ( box.containsPoint( p ) )
			{
				return true;
			}
		}
		
		return false;
	}
	

	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	protected void updateRequisitionX()
	{
	}

	protected void updateRequisitionY()
	{
	}

	
	protected void updateAllocationX()
	{
	}

	protected void updateAllocationY()
	{
	}

	
	



	//
	//
	// COLLATION METHODS
	//
	//
	
	private void setCollationRoot(DPContainerSequenceCollationRoot root)
	{
		collationRoot = root;
		boundsBoxes = null;
		
		for (DPWidget child: getChildren())
		{
			if ( child instanceof Collateable )
			{
				((Collateable)child).onCollateableParentCollationRootChanged( root );
			}
		}
	}
	
	public void onCollateableParentCollationRootChanged(DPContainerSequenceCollationRoot root)
	{
		if ( parent instanceof Collateable )
		{
			setCollationRoot( root );
		}
	}

	public DPContainerSequenceCollationRoot getCollationRoot()
	{
		return collationRoot;
	}

	public void setCollationRange(int start, int end)
	{
		collationRangeStart = start;
		collationRangeEnd = end;
	}
	
	
	protected void refreshBoundsBoxes()
	{
		if ( boundsBoxes == null )
		{
			if ( collationRoot != null  &&  collationRangeEnd > collationRangeStart )
			{
				boundsBoxes = collationRoot.computeCollatedBranchBoundsBoxes( this, collationRangeStart, collationRangeEnd );
			}
			else
			{
				boundsBoxes = new AABox2[0];
			}
		}
	}
	
	
	
	protected void onParentChanged()
	{
		super.onParentChanged();
		if ( parent instanceof Collateable )
		{
			setCollationRoot( ((Collateable)parent).getCollationRoot() );
		}
		else if ( parent instanceof DPContainerSequenceCollationRoot )
		{
			setCollationRoot( (DPContainerSequenceCollationRoot)parent );
		}
		else
		{
			setCollationRoot( null );
		}
	}
	
	
	protected void handleQueueResize()
	{
		super.handleQueueResize();
		
		boundsBoxes = null;
	}
	
	protected void onChildListModified()
	{
		super.onChildListModified();
		
		if ( collationRoot != null )
		{
			collationRoot.onCollatedBranchChildListModified( this );
		}

		boundsBoxes = null;
	}

	protected void onChildSizeRefreshed()
	{
		super.onChildSizeRefreshed();
		
		boundsBoxes = null;
	}
	
	
	protected void setParent(DPContainer parent, DPPresentationArea area)
	{
		if ( parent != this.parent   &&   parent != null )
		{
			if ( !(parent instanceof Collateable)  &&  !(parent instanceof DPContainerSequenceCollationRoot) )
			{
				throw new RuntimeException( "Collateable elements must be placed in either a) another collateable element, or b) a collated element; " + parent.getClass().getName() + " does not qualify" );
			}
		}

		super.setParent( parent, area );
	}

	


	//
	// Focus navigation methods
	//
	
	protected DPContentLeaf getContentLeafToLeftFromChild(DPWidget child)
	{
		if ( collationRoot != null )
		{
			return collationRoot.getContentLeafToLeftFromChild( child );
		}
		else
		{
			System.out.println( "parent = " + parent );
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafToRightFromChild(DPWidget child)
	{
		if ( collationRoot != null )
		{
			return collationRoot.getContentLeafToRightFromChild( child );
		}
		else
		{
			return null;
		}
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelowFromChild(DPWidget child, boolean bBelow, Point2 localCursorPos, boolean bSkipWhitespace)
	{
		if ( collationRoot != null )
		{
			return collationRoot.getContentLeafAboveOrBelowFromChild( child, bBelow, localCursorPos, bSkipWhitespace );
		}
		else
		{
			return null;
		}
	}
}
