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

public class DPProxy extends DPBin implements Collateable
{
	protected DPContainerSequenceCollationRoot collationRoot;
	protected int collationRangeStart, collationRangeEnd;
	protected AABox2 boundsBoxes[];
	
	
	public DPProxy()
	{
		super();
		
		collationRangeStart = -1;
		collationRangeEnd = -1;
	}

	



	//
	// Geometry methods
	//
	
	public Point2 getPositionInParentSpace()
	{
		if ( collationRoot == null )
		{
			return super.getPositionInParentSpace();
		}
		else
		{
			return new Point2( 0.0, 0.0 );
		}
	}
	
	public double getPositionInParentSpaceX()
	{
		if ( collationRoot == null )
		{
			return super.getPositionInParentSpaceX();
		}
		else
		{
			return 0.0;
		}
	}
	
	public double getPositionInParentSpaceY()
	{
		if ( collationRoot == null )
		{
			return super.getPositionInParentSpaceY();
		}
		else
		{
			return 0.0;
		}
	}
	
	public double getAllocationX()
	{
		if ( collationRoot == null )
		{
			return super.getAllocationX();
		}
		else
		{
			return collationRoot.getAllocationX();
		}
	}
	
	public double getAllocationY()
	{
		if ( collationRoot == null )
		{
			return super.getAllocationY();
		}
		else
		{
			return collationRoot.getAllocationY();
		}
	}
	
	public Vector2 getAllocation()
	{
		if ( collationRoot == null )
		{
			return super.getAllocation();
		}
		else
		{
			return collationRoot.getAllocation();
		}
	}
	
	public double getAllocationInParentSpaceX()
	{
		if ( collationRoot == null )
		{
			return super.getAllocationInParentSpaceX();
		}
		else
		{
			return collationRoot.getAllocationX();
		}
	}
	
	public double getAllocationInParentSpaceY()
	{
		if ( collationRoot == null )
		{
			return super.getAllocationInParentSpaceY();
		}
		else
		{
			return collationRoot.getAllocationY();
		}
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		if ( collationRoot == null )
		{
			return super.getAllocationInParentSpace();
		}
		else
		{
			return collationRoot.getAllocation();
		}
	}

	
	
	public boolean containsParentSpacePoint(Point2 p)
	{
		if ( collationRoot == null )
		{
			return super.containsParentSpacePoint( p );
		}
		else
		{
			refreshBoundsBoxes();
			
			if ( boundsBoxes != null )
			{
				for (AABox2 box: boundsBoxes)
				{
					if ( box.containsPoint( p ) )
					{
						return true;
					}
				}
			}
			
			return false;
		}
	}
	
	public boolean containsLocalSpacePoint(Point2 p)
	{
		if ( collationRoot == null )
		{
			return super.containsLocalSpacePoint( p );
		}
		else
		{
			refreshBoundsBoxes();
			
			if ( boundsBoxes != null )
			{
				for (AABox2 box: boundsBoxes)
				{
					if ( box.containsPoint( p ) )
					{
						return true;
					}
				}
			}
			
			return false;
		}
	}
	

	
	
	//
	//
	// LAYOUT METHODS
	//
	//
	
	protected void updateRequisitionX()
	{
		if ( collationRoot == null )
		{
			super.updateRequisitionX();
		}
	}

	protected void updateRequisitionY()
	{
		if ( collationRoot == null )
		{
			super.updateRequisitionY();
		}
	}

	
	protected void updateAllocationX()
	{
		if ( collationRoot == null )
		{
			super.updateAllocationX();
		}
	}

	protected void updateAllocationY()
	{
		if ( collationRoot == null )
		{
			super.updateAllocationY();
		}
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
	
	


	//
	// Focus navigation methods
	//
	
	protected DPContentLeaf getContentLeafToLeftFromChild(DPWidget child)
	{
		if ( collationRoot == null )
		{
			return super.getContentLeafToLeftFromChild( child );
		}
		else
		{
			return collationRoot.getContentLeafToLeftFromChild( child );
		}
	}
	
	protected DPContentLeaf getContentLeafToRightFromChild(DPWidget child)
	{
		if ( collationRoot == null )
		{
			return super.getContentLeafToRightFromChild( child );
		}
		else
		{
			return collationRoot.getContentLeafToRightFromChild( child );
		}
	}
	
	protected DPContentLeaf getContentLeafAboveOrBelowFromChild(DPWidget child, boolean bBelow, Point2 localCursorPos, boolean bSkipWhitespace)
	{
		if ( collationRoot == null )
		{
			return super.getContentLeafAboveOrBelowFromChild( child, bBelow, localCursorPos, bSkipWhitespace );
		}
		else
		{
			return collationRoot.getContentLeafAboveOrBelowFromChild( child, bBelow, localCursorPos, bSkipWhitespace );
		}
	}
}
