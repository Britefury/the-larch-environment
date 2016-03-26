//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import java.util.ArrayList;
import java.util.List;

import BritefuryJ.LSpace.LSContainer;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSScript;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.LSpace.Layout.LAllocBoxInterface;
import BritefuryJ.LSpace.Layout.LAllocV;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.LSpace.Layout.LReqBoxInterface;
import BritefuryJ.LSpace.Layout.ScriptLayout;
import BritefuryJ.LSpace.StyleParams.ScriptStyleParams;
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
	
	
	public LayoutNodeScript(LSScript element)
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
		LSScript script = (LSScript)element;
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = script.getWrappedChild( i );
			boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionX()  :  null;
		}
		
		ScriptLayout.computeRequisitionX( layoutReqBox, columnBoxes, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getColumnSpacing(), getRowSpacing() );
	}

	protected void updateRequisitionY()
	{
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSScript script = (LSScript)element;
		
		LReqBoxInterface boxes[] = new LReqBoxInterface[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = script.getWrappedChild( i );
			boxes[i] = child != null  ?  child.getLayoutNode().refreshRequisitionY()  :  null;
		}
		
		ScriptLayout.computeRequisitionY( layoutReqBox, rowBaselineY, boxes[LEFTSUPER], boxes[LEFTSUB], boxes[MAIN], boxes[RIGHTSUPER], boxes[RIGHTSUB], getColumnSpacing(), getRowSpacing() );
	}
	

	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX( );
		
		LReqBoxInterface layoutReqBox = getRequisitionBox();
		LSScript script = (LSScript)element;
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		double prevChildWidths[] = new double[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = script.getWrappedChild( i );
			reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildWidths[i] = child != null  ?  allocBoxes[i].getAllocWidth()  :  0.0;
		}
		
		ScriptLayout.allocateX( layoutReqBox, reqBoxes[LEFTSUPER], reqBoxes[LEFTSUB], reqBoxes[MAIN], reqBoxes[RIGHTSUPER], reqBoxes[RIGHTSUB], columnBoxes,
				getAllocationBox(), allocBoxes[LEFTSUPER], allocBoxes[LEFTSUB], allocBoxes[MAIN], allocBoxes[RIGHTSUPER], allocBoxes[RIGHTSUB],
				getColumnSpacing(), getRowSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = script.getWrappedChild( i );
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
		LSScript script = (LSScript)element;
		
		LReqBoxInterface reqBoxes[] = new LReqBoxInterface[NUMCHILDREN];
		LAllocBoxInterface allocBoxes[] = new LAllocBoxInterface[NUMCHILDREN];
		LAllocV prevChildAllocVs[] = new LAllocV[NUMCHILDREN];
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = script.getWrappedChild( i );
			reqBoxes[i] = child != null  ?  child.getLayoutNode().getRequisitionBox()  :  null;
			allocBoxes[i] = child != null  ?  child.getLayoutNode().getAllocationBox()  :  null;
			prevChildAllocVs[i] = child != null  ?  allocBoxes[i].getAllocV()  :  null;
		}
		
		ScriptLayout.allocateY( layoutReqBox, reqBoxes[LEFTSUPER], reqBoxes[LEFTSUB], reqBoxes[MAIN], reqBoxes[RIGHTSUPER], reqBoxes[RIGHTSUB], rowBaselineY,
				getAllocationBox(), allocBoxes[LEFTSUPER], allocBoxes[LEFTSUB], allocBoxes[MAIN], allocBoxes[RIGHTSUPER], allocBoxes[RIGHTSUB],
				getColumnSpacing(), getRowSpacing() );
		
		for (int i = 0; i < NUMCHILDREN; i++)
		{
			LSElement child = script.getWrappedChild( i );
			if ( child != null )
			{
				child.getLayoutNode().refreshAllocationY( prevChildAllocVs[i] );
			}
		}
	}
	

	
	
	
	private ArrayList<LSElement> getChildrenInSlots(int[] slots)
	{
		LSScript script = (LSScript)element;

		ArrayList<LSElement> entries = new ArrayList<LSElement>();
		for (int slot: slots)
		{
			LSElement child = script.getWrappedChild( slot );
			if ( child != null )
			{
				entries.add( child );
			}
		}
		
		return entries;
	}
	
	private ArrayList<LSElement> getChildrenInColumn(int column)
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
	
	
	private double getColumnXEdge(ArrayList<LSElement> column, boolean bRightEdge)
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
	
	
	
	private LSElement getLeafClosestToLocalPointInColumn(ArrayList<LSElement> column, Point2 localPos, ElementFilter filter)
	{
		// Now determine which child entry is the closest
		if ( column.size() == 1 )
		{
			// One entry; only 1 choice
			return getLeafClosestToLocalPointFromChild( column.get( 0 ), localPos, filter );
		}
		else if ( column.size() == 2 )
		{
			LSElement childI = column.get( 0 );
			LSElement childJ = column.get( 1 );
			double iUpperY = childI.getPositionInParentSpaceY() + childI.getActualHeightInParentSpace();
			double jLowerY = childJ.getPositionInParentSpaceY();
				
			double midY = ( iUpperY + jLowerY ) * 0.5;
			
			LSElement childA = localPos.y < midY  ?  childI  :  childJ;
			LSElement c = getLeafClosestToLocalPointFromChild( childA, localPos, filter );
			if ( c != null )
			{
				return c;
			}

			LSElement childB = childA == childI  ?  childJ  :  childI;
			return getLeafClosestToLocalPointFromChild( childB, localPos, filter );
		}
		else
		{
			throw new RuntimeException();
		}
	}
	
	protected LSElement getChildLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		List<LSElement> layoutChildren = ( (LSContainer)element ).getLayoutChildren();
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
			ArrayList< ArrayList<LSElement> > childrenByColumn = new ArrayList< ArrayList<LSElement> >();
			
			int[] columns = { LEFTCOLUMN, MAINCOLUMN, RIGHTCOLUMN };
			
			for (int col: columns)
			{
				ArrayList<LSElement> childEntries = getChildrenInColumn( col );
				
				if ( childEntries.size() > 0 )
				{
					childrenByColumn.add( childEntries );
				}
			}
			
			
			// Determine which column is closest
			ArrayList<LSElement> closestColumn = null;
			int columnIndex = -1;
			
			if ( childrenByColumn.size() == 1 )
			{
				closestColumn = childrenByColumn.get( 0 );
				columnIndex = 0;
			}
			else
			{
				ArrayList<LSElement> colI = childrenByColumn.get( 0 );
				for (int i = 0; i < childrenByColumn.size() - 1; i++)
				{
					ArrayList<LSElement> colJ = childrenByColumn.get( i + 1 );
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
			
			
			
			LSElement c = getLeafClosestToLocalPointInColumn( closestColumn, localPos, filter );
			
			if ( c != null )
			{
				return c;
			}
			else
			{
				LSElement next = null;
				for (int j = columnIndex + 1; j < childrenByColumn.size(); j++)
				{
					next = getLeafClosestToLocalPointInColumn( childrenByColumn.get( j ), localPos, filter );
					if ( next != null )
					{
						break;
					}
				}

				LSElement prev = null;
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
					double sqrDistToPrev = prev.getLocalToAncestorXform( element ).transform( prev.getLocalAABox() ).sqrDistanceTo( localPos );
					double sqrDistToNext = next.getLocalToAncestorXform( element ).transform( next.getLocalAABox() ).sqrDistanceTo( localPos );
					return sqrDistToPrev > sqrDistToNext  ?  prev  :  next;
				}
			}
		}
	}


	
	//
	// Focus navigation methods
	//
	
	public List<LSElement> horizontalNavigationList()
	{
		return ( (LSContainer)element ).getLayoutChildren();
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
