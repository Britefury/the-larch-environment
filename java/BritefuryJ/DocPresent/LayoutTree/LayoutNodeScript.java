//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPScript;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.LAllocBoxInterface;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.LReqBoxInterface;
import BritefuryJ.DocPresent.Layout.ScriptLayout;
import BritefuryJ.DocPresent.StyleParams.ScriptStyleParams;
import BritefuryJ.Math.Point2;

public class LayoutNodeScript extends ArrangedLayoutNode
{
	public static int LEFTSUPER = 0;
	public static int LEFTSUB = 1;
	public static int MAIN = 2;
	public static int RIGHTSUPER = 3;
	public static int RIGHTSUB = 4;
	
	public static int NUMCHILDREN = 5;

	private static int LEFTCOLUMN = 0;
	private static int MAINCOLUMN = 1;
	private static int RIGHTCOLUMN = 2;
	
	protected LReqBox columnBoxes[];
	protected double rowBaselineY[];
	
	
	public LayoutNodeScript(DPScript element)
	{
		super( element );

		columnBoxes = new LReqBox[3];
		columnBoxes[0] = new LReqBox();
		columnBoxes[1] = new LReqBox();
		columnBoxes[2] = new LReqBox();
		rowBaselineY = new double[3];
	}

	

	protected void updateRequisitionX()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPScript script = (DPScript)element;
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPElement child = script.getWrappedChild( i );
			boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionX()  :  null;
		}
		
		ScriptLayout.computeRequisitionX( layoutReqBox, columnBoxes, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getColumnSpacing(), getRowSpacing() );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPScript script = (DPScript)element;
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPElement child = script.getWrappedChild( i );
			boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionY()  :  null;
		}
		
		ScriptLayout.computeRequisitionY( layoutReqBox, rowBaselineY, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getColumnSpacing(), getRowSpacing() );
	}
	

	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPScript script = (DPScript)element;
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		double prevChildWidths[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPElement child = script.getWrappedChild( i );
			reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildWidths[i] = child != null  ?  allocBoxes[i].getAllocWidth()  :  0.0;
		}
		
		ScriptLayout.allocateX( layoutReqBox, reqBoxes[LEFTSUPER], reqBoxes[LEFTSUB], reqBoxes[MAIN], reqBoxes[RIGHTSUPER], reqBoxes[RIGHTSUB], columnBoxes,
				getAllocationBox(), allocBoxes[LEFTSUPER], allocBoxes[LEFTSUB], allocBoxes[MAIN], allocBoxes[RIGHTSUPER], allocBoxes[RIGHTSUB],
				getColumnSpacing(), getRowSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPElement child = script.getWrappedChild( i );
			if ( child != null )
			{
				child.getLayoutNode().refreshAllocationX( prevChildWidths[i] );
			}
		}
	}

	
	protected void updateAllocationY()
	{
		super.updateAllocationY( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		DPScript script = (DPScript)element;
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		LAllocV prevChildAllocVs[] = new LAllocV[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPElement child = script.getWrappedChild( i );
			reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildAllocVs[i] = child != null  ?  allocBoxes[i].getAllocV()  :  null;
		}
		
		ScriptLayout.allocateY( layoutReqBox, reqBoxes[LEFTSUPER], reqBoxes[LEFTSUB], reqBoxes[MAIN], reqBoxes[RIGHTSUPER], reqBoxes[RIGHTSUB], rowBaselineY,
				getAllocationBox(), allocBoxes[LEFTSUPER], allocBoxes[LEFTSUB], allocBoxes[MAIN], allocBoxes[RIGHTSUPER], allocBoxes[RIGHTSUB],
				getColumnSpacing(), getRowSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			DPElement child = script.getWrappedChild( i );
			if ( child != null )
			{
				child.getLayoutNode().refreshAllocationY( prevChildAllocVs[i] );
			}
		}
	}
	

	
	
	
	private ArrayList<DPElement> getChildrenInSlots(int[] slots)
	{
		DPScript script = (DPScript)element;

		ArrayList<DPElement> entries = new ArrayList<DPElement>();
		for (int slot: slots)
		{
			DPElement child = script.getWrappedChild( slot );
			if ( child != null )
			{
				entries.add( child );
			}
		}
		
		return entries;
	}
	
	private ArrayList<DPElement> getChildrenInColumn(int column)
	{
		if ( column == LEFTCOLUMN )
		{
			int slots[] = { LEFTSUPER, LEFTSUB };
			return getChildrenInSlots( slots );
		}
		else if ( column == MAINCOLUMN )
		{
			int slots[] = { MAIN };
			return getChildrenInSlots( slots );
		}
		else if ( column == RIGHTCOLUMN )
		{
			int slots[] = { RIGHTSUPER, RIGHTSUB };
			return getChildrenInSlots( slots );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	
	private double getColumnXEdge(ArrayList<DPElement> column, boolean bRightEdge)
	{
		double columnEdgeX = 0.0;
		for (int i = 0; i < column.size(); i++)
		{
			LayoutNode childNode = column.get( i ).getLayoutNode();
			
			double edgeX = 0.0;
			if ( bRightEdge )
			{
				edgeX = childNode.getAllocPositionInParentSpaceX() + childNode.getActualWidthInParentSpace();
			}
			else
			{
				edgeX = childNode.getAllocPositionInParentSpaceX();
			}
			
			if ( i > 0 )
			{
				if ( bRightEdge )
				{
					columnEdgeX = Math.max( columnEdgeX, edgeX );
				}
				else
				{
					columnEdgeX = Math.min( columnEdgeX, edgeX );
				}
			}
			else
			{
				columnEdgeX= edgeX;
			}
		}
		
		return columnEdgeX;
	}
	
	
	
	private DPElement getLeafClosestToLocalPointInColumn(ArrayList<DPElement> column, Point2 localPos, ElementFilter filter)
	{
		// Now determine which child entry is the closest
		if ( column.size() == 1 )
		{
			// One entry; only 1 choice
			return getLeafClosestToLocalPointFromChild( column.get( 0 ), localPos, filter );
		}
		else if ( column.size() == 2 )
		{
			DPElement childI = column.get( 0 );
			DPElement childJ = column.get( 1 );
			double iUpperY = childI.getPositionInParentSpaceY() + childI.getActualHeightInParentSpace();
			double jLowerY = childJ.getPositionInParentSpaceY();
				
			double midY = ( iUpperY + jLowerY ) * 0.5;
			
			DPElement childA = localPos.y < midY  ?  childI  :  childJ;
			DPElement c = getLeafClosestToLocalPointFromChild( childA, localPos, filter );
			if ( c != null )
			{
				return c;
			}

			DPElement childB = childA == childI  ?  childJ  :  childI;
			return getLeafClosestToLocalPointFromChild( childB, localPos, filter );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	protected DPElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		List<DPElement> layoutChildren = element.getLayoutChildren();
		if ( layoutChildren.size() == 0 )
		{
			// No children
			return null;
		}
		else if ( layoutChildren.size() == 1 )
		{
			// Only 1 child
			return layoutChildren.get( 0 );
		}
		else
		{
			// Group children by column
			ArrayList< ArrayList<DPElement> > childrenByColumn = new ArrayList< ArrayList<DPElement> >();
			
			int[] columns = { LEFTCOLUMN, MAINCOLUMN, RIGHTCOLUMN };
			
			for (int col: columns)
			{
				ArrayList<DPElement> childEntries = getChildrenInColumn( col );
				
				if ( childEntries.size() > 0 )
				{
					childrenByColumn.add( childEntries );
				}
			}
			
			
			// Determine which column is closest
			ArrayList<DPElement> closestColumn = null;
			int columnIndex = -1;
			
			if ( childrenByColumn.size() == 1 )
			{
				closestColumn = childrenByColumn.get( 0 );
				columnIndex = 0;
			}
			else
			{
				ArrayList<DPElement> colI = childrenByColumn.get( 0 );
				for (int i = 0; i < childrenByColumn.size() - 1; i++)
				{
					ArrayList<DPElement> colJ = childrenByColumn.get( i + 1 );
					double rightEdgeI = getColumnXEdge( colI, true );
					double leftEdgeJ = getColumnXEdge( colJ, false );
					
					double midPoint = ( rightEdgeI + leftEdgeJ ) * 0.5;
					
					if ( localPos.x < midPoint )
					{
						closestColumn = colI;
						columnIndex = i;
						break;
					}
					
					colI = colJ;
				}
				
				if ( closestColumn == null )
				{
					columnIndex = childrenByColumn.size() - 1;
					closestColumn = childrenByColumn.get( columnIndex );
				}
			}
			
			
			
			DPElement c = getLeafClosestToLocalPointInColumn( closestColumn, localPos, filter );
			
			if ( c != null )
			{
				return c;
			}
			else
			{
				DPElement next = null;
				for (int j = columnIndex + 1; j < childrenByColumn.size(); j++)
				{
					next = getLeafClosestToLocalPointInColumn( childrenByColumn.get( j ), localPos, filter );
					if ( next != null )
					{
						break;
					}
				}

				DPElement prev = null;
				for (int j = columnIndex - 1; j >= 0; j--)
				{
					prev = getLeafClosestToLocalPointInColumn( childrenByColumn.get( j ), localPos, filter );
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
					double distToPrev = localPos.y - ( prev.getPositionInParentSpaceY() + prev.getActualHeightInParentSpace() );
					double distToNext = next.getPositionInParentSpaceY() - localPos.y;
					
					return distToPrev > distToNext  ?  prev  :  next;
				}
			}
		}
	}


	
	//
	// Focus navigation methods
	//
	
	public List<DPElement> horizontalNavigationList()
	{
		return element.getLayoutChildren();
	}
	
	
	
	
	//
	// STYLESHEET METHODS
	//
	
	protected double getColumnSpacing()
	{
		return ((ScriptStyleParams)element.getStyleParams()).getColumnSpacing();
	}

	protected double getRowSpacing()
	{
		return ((ScriptStyleParams)element.getStyleParams()).getRowSpacing();
	}
}
