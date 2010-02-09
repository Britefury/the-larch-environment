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
import BritefuryJ.DocPresent.Layout.ElementAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;

public abstract class ArrangedLayoutNode extends BranchLayoutNode implements LReqBoxInterface, LAllocBoxInterface
{
	protected DPContainer element;
	
	
	public ArrangedLayoutNode(DPContainer element)
	{
		this.element = element;
		req_lineBreakCost = -1;
	}


	public DPWidget getElement()
	{
		return element;
	}
	

	
	
	public LReqBoxInterface getRequisitionBox()
	{
		return this;
	}
	
	public LAllocBoxInterface getAllocationBox()
	{
		return this;
	}
	
	
	
	
	
	public double getAllocationInParentSpaceX()
	{
		return getAllocationX()  *  getScale();
	}
	
	public double getAllocationInParentSpaceY()
	{
		return getAllocationY()  *  getScale();
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return getAllocation().mul( getScale() );
	}
	
	
	
	
	
	public LReqBoxInterface refreshRequisitionX()
	{
		if ( !element.isSizeUpToDate() )
		{
			updateRequisitionX();
		}
		return this;
	}
	
	public LReqBoxInterface refreshRequisitionY()
	{
		if ( !element.isSizeUpToDate() )
		{
			updateRequisitionY();
		}
		return this;
	}
	

	
	protected abstract void updateRequisitionX();
	protected abstract void updateRequisitionY();


	
	
	
	public void refreshAllocationX(double prevWidth)
	{
		if ( !element.isSizeUpToDate()  ||  getAllocationX() != prevWidth )
		{
			updateAllocationX();
			element.clearFlagSizeUpToDate();
		}
	}
	
	public void refreshAllocationY(LAllocV prevHeight)
	{
		if ( !element.isSizeUpToDate()  ||  !getAllocV().equals( prevHeight ) )
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
	
	
	
	
	
	
	
	
	
	
	
	//
	//
	//
	//
	//
	// REQUISITION BOX IMPLEMENTATION
	//
	//
	//
	//
	//
	
	private static int FLAG_LINEBREAK = 0x1  *  ElementAlignment._ELEMENTALIGN_END;
	private static int FLAG_PARAGRAPH_INDENT = 0x2  *  ElementAlignment._ELEMENTALIGN_END;
	private static int FLAG_PARAGRAPH_DEDENT = 0x4  *  ElementAlignment._ELEMENTALIGN_END;

	protected int req_flags = 0;
	protected int req_lineBreakCost;
	
	protected double req_minWidth, req_prefWidth, req_minHAdvance, req_prefHAdvance;
	protected double req_height, req_vSpacing;
	protected double req_refY;
	
	
	
	public double getReqMinWidth()
	{
		return req_minWidth;
	}
	
	public double getReqPrefWidth()
	{
		return req_prefWidth;
	}
	
	public double getReqMinHAdvance()
	{
		return req_minHAdvance;
	}
	
	public double getReqPrefHAdvance()
	{
		return req_prefHAdvance;
	}
	

	public double getReqHeight()
	{
		return req_height;
	}
	
	public double getReqVSpacing()
	{
		return req_vSpacing;
	}
	
	public double getReqRefY()
	{
		return req_refY;
	}
	
	public double getReqHeightBelowRefPoint()
	{
		return req_height - req_refY;
	}
	
	

	public boolean isReqLineBreak()
	{
		return getFlag( FLAG_LINEBREAK );
	}
	
	public boolean isReqParagraphIndentMarker()
	{
		return getFlag( FLAG_PARAGRAPH_INDENT );
	}
	
	public boolean isReqParagraphDedentMarker()
	{
		return getFlag( FLAG_PARAGRAPH_DEDENT );
	}
	
	public int getReqLineBreakCost()
	{
		return req_lineBreakCost;
	}
	
	
	public LReqBoxInterface scaledRequisition(double scale)
	{
		return new LReqBox( this, scale );
	}
	
	
	public void clearRequisitionX()
	{
		req_minWidth = req_prefWidth = req_minHAdvance = req_prefHAdvance = 0.0;
	}
	
	public void clearRequisitionY()
	{
		req_height = req_vSpacing = req_refY = 0.0;
	}
	
	
	
	public void setRequisitionX(double width, double hAdvance)
	{
		req_minWidth = req_prefWidth = width;
		req_minHAdvance = req_prefHAdvance = hAdvance;
	}
	
	public void setRequisitionX(double minWidth, double prefWidth, double minHAdvance, double prefHAdvance)
	{
		this.req_minWidth = minWidth; 
		this.req_prefWidth = prefWidth;
		this.req_minHAdvance = minHAdvance; 
		this.req_prefHAdvance = prefHAdvance;
	}
	
	public void setRequisitionX(LReqBoxInterface box)
	{
		this.req_minWidth = box.getReqMinWidth(); 
		this.req_prefWidth = box.getReqPrefWidth();
		this.req_minHAdvance = box.getReqMinHAdvance(); 
		this.req_prefHAdvance = box.getReqPrefHAdvance();
	}
	
	

	public void setRequisitionY(double height, double vSpacing)
	{
		req_height = height;
		req_vSpacing = vSpacing;
		req_refY = height * 0.5;
	}
	
	public void setRequisitionY(double height, double vSpacing, double refY)
	{
		req_height = height;
		req_vSpacing = vSpacing;
		this.req_refY = refY;
	}
	
	public void setRequisitionY(LReqBoxInterface reqBox)
	{
		req_height = reqBox.getReqHeight();
		req_vSpacing = reqBox.getReqVSpacing();
		req_refY = reqBox.getReqRefY();
	}
	
	
	public void setLineBreakCost(int cost)
	{
		req_lineBreakCost = cost;
		setFlag( FLAG_LINEBREAK, true );
	}

	
	public void borderX(double leftMargin, double rightMargin)
	{
		if ( req_minHAdvance <= req_minWidth )
		{
			req_minWidth += leftMargin + rightMargin;
			req_minHAdvance = req_minWidth;
		}
		else
		{
			double hspacing = req_minHAdvance - req_minWidth;
			hspacing = Math.max( hspacing - rightMargin, 0.0 );
			req_minWidth += leftMargin + rightMargin;
			req_minHAdvance = req_minWidth + hspacing;
		}
		
		if ( req_prefHAdvance <= req_prefWidth )
		{
			req_prefWidth += leftMargin + rightMargin;
			req_prefHAdvance = req_prefWidth;
		}
		else
		{
			double hspacing = req_prefHAdvance - req_prefWidth;
			hspacing = Math.max( hspacing - rightMargin, 0.0 );
			req_prefWidth += leftMargin + rightMargin;
			req_prefHAdvance = req_prefWidth + hspacing;
		}
	}
	
	public void borderY(double topMargin, double bottomMargin)
	{
		req_height += ( topMargin + bottomMargin );
		req_vSpacing = Math.max( req_vSpacing - bottomMargin, 0.0 );
	}
	

	
	
	private static int PACKFLAG_EXPAND = 1;
	
	
	public static int packFlags(boolean bExpand)
	{
		return ( bExpand ? PACKFLAG_EXPAND : 0 );
	}
	
	public static int combinePackFlags(int flags0, int flags1)
	{
		return flags0 | flags1;
	}
	
	public static boolean testPackFlagExpand(int packFlags)
	{
		return ( packFlags & PACKFLAG_EXPAND )  !=  0;
	}
	
	
	private void setFlag(int f, boolean value)
	{
		if ( value )
		{
			req_flags |= f;
		}
		else
		{
			req_flags &= ~f;
		}
	}
	
	private boolean getFlag(int f)
	{
		return ( req_flags & f )  !=  0;
	}
	
	
	
	
	
	
	
	
	
	//
	//
	//
	//
	//
	// ALLOCATION BOX IMPLEMENTATION
	//
	//
	//
	//
	//

	protected double alloc_positionInParentSpaceX, alloc_positionInParentSpaceY;
	protected double alloc_allocationX, alloc_allocationY;
	protected double alloc_refY;

	
	public LayoutNode getAllocLayoutNode()
	{
		return this;
	}
	
	
	
	public double getAllocPositionInParentSpaceX()
	{
		return alloc_positionInParentSpaceX;
	}
	
	public double getAllocPositionInParentSpaceY()
	{
		return alloc_positionInParentSpaceY;
	}
	
	public Point2 getPositionInParentSpace()
	{
		return new Point2( alloc_positionInParentSpaceX, alloc_positionInParentSpaceY );
	}
	
	public double getAllocationX()
	{
		return alloc_allocationX;
	}
	
	public double getAllocationY()
	{
		return alloc_allocationY;
	}
	
	public double getAllocRefY()
	{
		return alloc_refY;
	}
	
	public LAllocV getAllocV()
	{
		return new LAllocV( alloc_allocationY, alloc_refY );
	}
	
	public Vector2 getAllocation()
	{
		return new Vector2( alloc_allocationX, alloc_allocationY );
	}


	
	
	
	//
	// SETTERS
	//
	
	public void setAllocPositionInParentSpaceX(double x)
	{
		alloc_positionInParentSpaceX = x;
	}
	
	public void setAllocPositionInParentSpaceY(double y)
	{
		alloc_positionInParentSpaceY = y;
	}
	
	public void setAllocationX(double width)
	{
		alloc_allocationX = width;
	}

	public void setAllocationY(double height, double refY)
	{
		alloc_allocationY = height;
		this.alloc_refY = refY;
	}

	public void setAllocation(double width, double height, double refY)
	{
		alloc_allocationX = width;
		alloc_allocationY = height;
		this.alloc_refY = refY;
	}

	public void setPositionInParentSpaceAndAllocationX(double x, double width)
	{
		alloc_positionInParentSpaceX = x;
		alloc_allocationX = width;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height)
	{
		alloc_positionInParentSpaceY = y;
		alloc_allocationY = height;
		alloc_refY = height * 0.5;
	}
	
	public void setPositionInParentSpaceAndAllocationY(double y, double height, double refY)
	{
		alloc_positionInParentSpaceY = y;
		alloc_allocationY = height;
		this.alloc_refY = refY;
	}



	public void scaleAllocationX(double scale)
	{
		alloc_allocationX *= scale;
	}

	public void scaleAllocationY(double scale)
	{
		alloc_allocationY *= scale;
		alloc_refY *= scale;
	}
}
