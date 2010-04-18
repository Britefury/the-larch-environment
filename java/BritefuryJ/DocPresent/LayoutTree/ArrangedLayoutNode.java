//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.List;

import BritefuryJ.DocPresent.DPContainer;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.ElementAlignment;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
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


	public DPElement getElement()
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
		return getAllocationX()  *  getParentAllocationToParentSpaceXform().scale;
	}
	
	public double getAllocationInParentSpaceY()
	{
		return getAllocationY()  *  getParentAllocationToParentSpaceXform().scale;
	}
	
	public Vector2 getAllocationInParentSpace()
	{
		return getAllocation().mul( getParentAllocationToParentSpaceXform().scale );
	}
	
	
	
	
	
	public LReqBoxInterface refreshRequisitionX()
	{
		if ( !element.isAllocationUpToDate() )
		{
			updateRequisitionX();
		}
		return this;
	}
	
	public LReqBoxInterface refreshRequisitionY()
	{
		if ( !element.isAllocationUpToDate() )
		{
			updateRequisitionY();
		}
		return this;
	}
	

	
	protected abstract void updateRequisitionX();
	protected abstract void updateRequisitionY();


	
	
	
	public void refreshAllocationX(double prevWidth)
	{
		if ( !element.isAllocationUpToDate()  ||  getAllocationX() != prevWidth )
		{
			updateAllocationX();
			element.clearFlagAllocationUpToDate();
		}
	}
	
	public void refreshAllocationY(LAllocV prevHeight)
	{
		if ( !element.isAllocationUpToDate()  ||  !getAllocV().equals( prevHeight ) )
		{
			updateAllocationY();
		}
		onAllocationRefreshed();
	}
	

	
	
	protected void updateAllocationX()
	{
	}

	protected void updateAllocationY()
	{
	}

	
	

	
	
	
	protected void onAllocationXRefreshed()
	{
		element.clearFlagAllocationUpToDate();
	}
	
	protected void onAllocationYRefreshed()
	{
		onAllocationRefreshed();
	}
	
	protected void onAllocationRefreshed()
	{
		element.clearFlagResizeQueued();
		element.setFlagAllocationUpToDate();
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
	

	
	
	
	protected LReqBoxInterface[] getChildrenRefreshedRequistionXBoxes(List<DPElement> nodes)
	{
		LReqBoxInterface[] boxes = new LReqBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().refreshRequisitionX();
		}
		return boxes;
	}


	protected LReqBoxInterface[] getChildrenRefreshedRequistionYBoxes(List<DPElement> nodes)
	{
		LReqBoxInterface[] boxes = new LReqBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().refreshRequisitionY();
		}
		return boxes;
	}

	
	
	
	protected LReqBoxInterface[] getChildrenRequisitionBoxes(List<DPElement> nodes)
	{
		LReqBoxInterface[] boxes = new LReqBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().getRequisitionBox();
		}
		return boxes;
	}

	
	
	protected LAllocBoxInterface[] getChildrenAllocationBoxes(List<DPElement> nodes)
	{
		LAllocBoxInterface[] boxes = new LAllocBoxInterface[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			boxes[i] = nodes.get( i ).getLayoutNode().getAllocationBox();
		}
		return boxes;
	}
	
	
	
	protected int[] getChildrenAlignmentFlags(List<DPElement> nodes)
	{
		int alignmentFlags[] = new int[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			alignmentFlags[i] = nodes.get( i ).getAlignmentFlags();
		}
		return alignmentFlags;
	}
	
	
	
	
	protected double[] getChildrenAllocationX(List<DPElement> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().x;
		}
		return values;
	}



	protected double[] getChildrenAllocationY(List<DPElement> nodes)
	{
		double[] values = new double[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocation().y;
		}
		return values;
	}



	protected LAllocV[] getChildrenAllocV(List<DPElement> nodes)
	{
		LAllocV[] values = new LAllocV[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
		{
			values[i] = nodes.get( i ).getAllocV();
		}
		return values;
	}




	public DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		return getChildLeafClosestToLocalPoint( localPos, filter );
	}

	protected abstract DPElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter);
	
	protected DPElement getLeafClosestToLocalPointFromChild(DPElement child, Point2 localPos, ElementFilter filter)
	{
		return child.getLayoutNode().getLeafClosestToLocalPoint( child.getParentToLocalXform().transform( localPos ), filter );
	}
	
	protected DPElement getChildLeafClosestToLocalPointHorizontal(List<DPElement> searchList, Point2 localPos, ElementFilter filter)
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
			DPElement start = null;
			int startIndex = -1;
			DPElement childI = searchList.get( 0 );
			for (int i = 0; i < searchList.size() - 1; i++)
			{
				DPElement childJ = searchList.get( i + 1 );
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
			
			DPElement c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPElement next = null;
				DPElement nextC = null;
				for (int j = startIndex + 1; j < searchList.size(); j++)
				{
					nextC = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( nextC != null )
					{
						next = searchList.get( j );
						break;
					}
				}

				DPElement prev = null;
				DPElement prevC = null;
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
	
	protected DPElement getChildLeafClosestToLocalPointVertical(List<DPElement> searchList, Point2 localPos, ElementFilter filter)
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
			DPElement start = null;
			int startIndex = -1;
			DPElement childI = searchList.get( 0 );
			for (int i = 0; i < searchList.size() - 1; i++)
			{
				DPElement childJ = searchList.get( i + 1 );
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
			
			DPElement c = getLeafClosestToLocalPointFromChild( start, localPos, filter );
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPElement next = null;
				for (int j = startIndex + 1; j < searchList.size(); j++)
				{
					next = getLeafClosestToLocalPointFromChild( searchList.get( j ), localPos, filter );
					if ( next != null )
					{
						break;
					}
				}

				DPElement prev = null;
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

	protected double alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY;
	protected double alloc_allocationX, alloc_allocationY;
	protected double alloc_refY;

	
	public LayoutNode getAllocLayoutNode()
	{
		return this;
	}
	
	
	
	public Point2 getPositionInParentSpace()
	{
		return getParentAllocationToParentSpaceXform().transform( new Point2( alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY ) );
	}
	
	public double getAllocPositionInParentAllocationSpaceX()
	{
		return alloc_positionInParentAllocationSpaceX;
	}
	
	public double getAllocPositionInParentAllocationSpaceY()
	{
		return alloc_positionInParentAllocationSpaceY;
	}
	
	public Point2 getPositionInParentAllocationSpace()
	{
		return new Point2( alloc_positionInParentAllocationSpaceX, alloc_positionInParentAllocationSpaceY );
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
	
	public void setAllocPositionInParentAllocationSpaceX(double x)
	{
		alloc_positionInParentAllocationSpaceX = x;
	}
	
	public void setAllocPositionInParentAllocationSpaceY(double y)
	{
		alloc_positionInParentAllocationSpaceY = y;
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

	public void setPositionInParentAllocationSpaceAndAllocationX(double x, double width)
	{
		alloc_positionInParentAllocationSpaceX = x;
		alloc_allocationX = width;
	}
	
	public void setPositionInParentAllocationSpaceAndAllocationY(double y, double height)
	{
		alloc_positionInParentAllocationSpaceY = y;
		alloc_allocationY = height;
		alloc_refY = height * 0.5;
	}
	
	public void setPositionInParentAllocationSpaceAndAllocationY(double y, double height, double refY)
	{
		alloc_positionInParentAllocationSpaceY = y;
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
