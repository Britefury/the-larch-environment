//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.WidgetFilter;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class ArrangedLayoutNode extends BranchLayoutNode
{
	protected DPContainer element;
	protected LReqBox layoutReqBox;
	protected LAllocBox layoutAllocBox;
	
	
	public ArrangedLayoutNode(DPContainer element)
	{
		this.element = element;
		layoutReqBox = new LReqBox();
		layoutAllocBox = new LAllocBox( this );
	}


	public DPWidget getElement()
	{
		return element;
	}
	

	
	
	public LReqBoxInterface getRequisitionBox()
	{
		return layoutReqBox;
	}
	
	public LAllocBoxInterface getAllocationBox()
	{
		return layoutAllocBox;
	}
	
	
	
	
	
	public double getPositionInParentSpaceX()
	{
		return layoutAllocBox.getPositionInParentSpaceX();
	}
	
	public double getPositionInParentSpaceY()
	{
		return layoutAllocBox.getPositionInParentSpaceY();
	}
	
	public Point2 getPositionInParentSpace()
	{
		return layoutAllocBox.getPositionInParentSpace();
	}

	public double getAllocationX()
	{
		return layoutAllocBox.getAllocationX();
	}
	
	public double getAllocationY()
	{
		return layoutAllocBox.getAllocationY();
	}

	public Vector2 getAllocation()
	{
		return layoutAllocBox.getAllocation();
	}

	public LAllocV getAllocV()
	{
		return layoutAllocBox.getAllocV();
	}

	public double getAllocationInParentSpaceX()
	{
		return layoutAllocBox.getAllocationX()  *  getScale();
	}
	
	public double getAllocationInParentSpaceY()
	{
		return layoutAllocBox.getAllocationY()  *  getScale();
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return layoutAllocBox.getAllocation().mul( getScale() );
	}
	
	
	
	
	
	public LReqBoxInterface refreshRequisitionX()
	{
		if ( !element.isSizeUpToDate() )
		{
			updateRequisitionX();
		}
		return layoutReqBox;
	}
	
	public LReqBoxInterface refreshRequisitionY()
	{
		if ( !element.isSizeUpToDate() )
		{
			updateRequisitionY();
		}
		return layoutReqBox;
	}
	

	
	protected abstract void updateRequisitionX();
	protected abstract void updateRequisitionY();


	
	
	
	public void refreshAllocationX(double prevWidth)
	{
		if ( !element.isSizeUpToDate()  ||  layoutAllocBox.getAllocationX() != prevWidth )
		{
			updateAllocationX();
			element.clearFlagSizeUpToDate();
		}
	}
	
	public void refreshAllocationY(LAllocV prevHeight)
	{
		if ( !element.isSizeUpToDate()  ||  !layoutAllocBox.getAllocV().equals( prevHeight ) )
		{
			updateAllocationY();
		}
		onSizeRefreshed();
	}
	

	
	
	protected void updateAllocationX()
	{
	}

	protected void updateAllocationY()
	{
	}

	
	

	
	
	
	protected void onAllocationXRefreshed()
	{
		element.clearFlagSizeUpToDate();
	}
	
	protected void onAllocationYRefreshed()
	{
		onSizeRefreshed();
	}
	
	protected void onSizeRefreshed()
	{
		element.clearFlagResizeQueued();
		element.setFlagSizeUpToDate();
		DPContainer parent = element.getParent();
		while ( parent != null )
		{
			LayoutNode parentLayout = parent.getLayoutNode();
			if ( parentLayout != null )
			{
				parentLayout.onChildSizeRefreshed();
				break;
			}
			parent = parent.getParent();
		}
	}
	
	protected void onChildSizeRefreshed()
	{
	}

	public AABox2[] computeBranchBoundsBoxes(DPContainer branch)
	{
		throw new RuntimeException( "No collateable layout found" );
	}
	

	
	
	
	protected LReqBoxInterface[] getChildrenRefreshedRequistionXBoxes(List<DPWidget> nodes)
	{
		LReqBoxInterface[] boxes = new LReqBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().refreshRequisitionX();
		}
		return boxes;
	}


	protected LReqBoxInterface[] getChildrenRefreshedRequistionYBoxes(List<DPWidget> nodes)
	{
		LReqBoxInterface[] boxes = new LReqBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().refreshRequisitionY();
		}
		return boxes;
	}

	
	
	
	protected LReqBoxInterface[] getChildrenRequisitionBoxes(List<DPWidget> nodes)
	{
		LReqBoxInterface[] boxes = new LReqBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().getRequisitionBox();
		}
		return boxes;
	}

	
	
	protected LAllocBoxInterface[] getChildrenAllocationBoxes(List<DPWidget> nodes)
	{
		LAllocBoxInterface[] boxes = new LAllocBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().getAllocationBox();
		}
		return boxes;
	}
	
	
	
	protected int[] getChildrenAlignmentFlags(List<DPWidget> nodes)
	{
		int alignmentFlags[] = new int[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			alignmentFlags[i] = nodes.get( i ).getAlignmentFlags();
		}
		return alignmentFlags;
	}
	
	
	
	
	@SuppressWarnings("unchecked")
	protected <T extends PackingParams> T[] getChildrenPackingParams(List<DPWidget> nodes, T packingParams[])
	{
		for (int i = 0; i < nodes.size(); i++)
		{
			packingParams[i] = (T)nodes.get( i ).getParentPacking();
		}
		return packingParams;
	}
	
	
	
	
	protected double[] getChildrenAllocationX(List<DPWidget> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().x;
		}
		return values;
	}



	protected double[] getChildrenAllocationY(List<DPWidget> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().y;
		}
		return values;
	}



	protected LAllocV[] getChildrenAllocV(List<DPWidget> nodes)
	{
		LAllocV[] values = new LAllocV[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocV();
		}
		return values;
	}




	public DPWidget getLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		return getChildLeafClosestToLocalPoint( localPos, filter );
	}

	protected abstract DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter);
	
	protected DPWidget getLeafClosestToLocalPointFromChild(DPWidget child, Point2 localPos, WidgetFilter filter)
	{
		return child.getLayoutNode().getLeafClosestToLocalPoint( child.getParentToLocalXform().transform( localPos ), filter );
	}
	
	protected DPWidget getChildLeafClosestToLocalPointHorizontal(List<DPWidget> searchList, Point2 localPos, WidgetFilter filter)
	{
		if ( searchList.size() == 0 )
		{
			return null;
		}
		else if ( searchList.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( searchList.get( 0 ), localPos, filter );
		}
		else
		{
			DPWidget start = null;
			int startIndex = -1;
			DPWidget childI = searchList.get( 0 );
			for (int i = 0; i < searchList.size() - 1; i++)
			{
				DPWidget childJ = searchList.get( i + 1 );
				double iUpperX = childI.getPositionInParentSpace().x + childI.getAllocationInParentSpace().x;
				double jLowerX = childJ.getPositionInParentSpace().x;
				
				double midx = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midx )
				{
					startIndex = i;
					start = childI;
					break;
				}
				
				childI = childJ;
			}
			
			if ( start == null )
			{
				startIndex = searchList.size() - 1;
				start = searchList.get( startIndex );
			}
			
			DPWidget c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPWidget next = null;
				DPWidget nextC = null;
				for (int j = startIndex + 1; j < searchList.size(); j++)
				{
					nextC = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( nextC != null )
					{
						next = searchList.get( j );
						break;
					}
				}

				DPWidget prev = null;
				DPWidget prevC = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prevC = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( prevC != null )
					{
						prev = searchList.get( j );
						break;
					}
				}
				

				if ( prev == null  &&  next == null )
				{
					return null;
				}
				else if ( prev == null  &&  next != null )
				{
					return nextC;
				}
				else if ( prev != null  &&  next == null )
				{
					return prevC;
				}
				else
				{
					double distToPrev = localPos.x - ( prev.getPositionInParentSpace().x + prev.getAllocationInParentSpace().x );
					double distToNext = next.getPositionInParentSpace().x - localPos.x;
					
					return distToPrev > distToNext  ?  prevC  :  nextC;
				}
			}
		}
	}
	
	protected DPWidget getChildLeafClosestToLocalPointVertical(List<DPWidget> searchList, Point2 localPos, WidgetFilter filter)
	{
		if ( searchList.size() == 0 )
		{
			return null;
		}
		else if ( searchList.size() == 1 )
		{
			return getLeafClosestToLocalPointFromChild( searchList.get( 0 ), localPos, filter );
		}
		else
		{
			DPWidget start = null;
			int startIndex = -1;
			DPWidget childI = searchList.get( 0 );
			for (int i = 0; i < searchList.size() - 1; i++)
			{
				DPWidget childJ = searchList.get( i + 1 );
				double iUpperY = childI.getPositionInParentSpace().y + childI.getAllocationInParentSpace().y;
				double jLowerY = childJ.getPositionInParentSpace().y;
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					startIndex = i;
					start = childI;
					break;
				}
				
				childI = childJ;
			}
			
			if ( start == null )
			{
				startIndex = searchList.size() - 1;
				start = searchList.get( startIndex );
			}
			
			DPWidget c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPWidget next = null;
				for (int j = startIndex + 1; j < searchList.size(); j++)
				{
					next = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( next != null )
					{
						break;
					}
				}

				DPWidget prev = null;
				for (int j = startIndex - 1; j >= 0; j--)
				{
					prev = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( prev != null )
					{
						break;
					}
				}
				
				
				if ( prev == null  &&  next == null )
				{
					return null;
				}
				else if ( prev == null  &&  next != null )
				{
					return next;
				}
				else if ( prev != null  &&  next == null )
				{
					return prev;
				}
				else
				{
					double distToPrev = localPos.y - ( prev.getPositionInParentSpace().y + prev.getAllocationInParentSpace().y );
					double distToNext = next.getPositionInParentSpace().y - localPos.y;
					
					return distToPrev > distToNext  ?  prev  :  next;
				}
			}
		}
	}



	protected double getScale()
	{
		return element.getScale();
	}
}
