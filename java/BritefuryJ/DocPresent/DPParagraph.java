//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package BritefuryJ.DocPresent;

import java.util.List;

import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocV;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.DocPresent.Layout.PackingParams;
import BritefuryJ.DocPresent.Layout.ParagraphLayout;
import BritefuryJ.DocPresent.StyleSheets.ParagraphStyleSheet;
import BritefuryJ.Math.AABox2;
import BritefuryJ.Math.Point2;



public class DPParagraph extends DPContainerSequenceCollationRoot
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}



	
	private ParagraphLayout.Line lines[];

	
	
	public DPParagraph(ElementContext context)
	{
		this( context, ParagraphStyleSheet.defaultStyleSheet );
	}

	public DPParagraph(ElementContext context, ParagraphStyleSheet styleSheet)
	{
		super( context, styleSheet );
		
		lines = new ParagraphLayout.Line[0];
	}


	
	

	protected void updateRequisitionX()
	{
		refreshCollation();
		
		LReqBox[] childBoxes = new LReqBox[collationLeaves.length];
		for (int i = 0; i < collationLeaves.length; i++)
		{
			childBoxes[i] = collationLeaves[i].refreshRequisitionX();
		}

		ParagraphLayout.computeRequisitionX( layoutReqBox, childBoxes, getIndentation(), getSpacing() );
	}

	protected void updateRequisitionY()
	{
		for (DPWidget child: collationLeaves)
		{
			child.refreshRequisitionY();
		}

		ParagraphLayout.computeRequisitionY( layoutReqBox, lines, getLineSpacing() );
	}
	

	
	protected void updateAllocationX()
	{
		super.updateAllocationX();
		
		LReqBox childBoxes[] = getCollatedChildrenRequisitionBoxes();
		LAllocBox childAllocBoxes[] = getCollatedChildrenAllocationBoxes();
		int childAllocFlags[] = getCollatedChildrenAlignmentFlags();
		double prevWidths[] = getCollatedChildrenAllocationX();
		
		lines = ParagraphLayout.allocateX( layoutReqBox, childBoxes, layoutAllocBox, childAllocBoxes, childAllocFlags, getIndentation(), getSpacing() );
		
		int i = 0;
		for (DPWidget child: collationLeaves)
		{
			child.refreshAllocationX( prevWidths[i] );
			i++;
		}
	}
	
	
	
	protected void updateAllocationY()
	{
		super.updateAllocationY();
		
		LAllocV prevAllocVs[][] = new LAllocV[lines.length][];
		for (int y = 0; y < lines.length; y++)
		{
			LAllocBox[] lineChildren = lines[y].getChildAllocBoxes();
			prevAllocVs[y] = new LAllocV[lineChildren.length];
			for (int x = 0; x < lineChildren.length; x++)
			{
				prevAllocVs[y][x] = lineChildren[x].getAllocV();
			}
		}
		
		ParagraphLayout.allocateY( layoutReqBox, layoutAllocBox, lines, getSpacing() );
		
		for (int y = 0; y < lines.length; y++)
		{
			LAllocBox[] lineChildren = lines[y].getChildAllocBoxes();
			for (int x = 0; x < lineChildren.length; x++)
			{
				lineChildren[x].getElement().refreshAllocationY( prevAllocVs[y][x] );
			}
		}
	}
	

	
	private ParagraphLayout.Line getLineClosestToLocalPoint(Point2 localPos)
	{
		if ( lines.length == 0 )
		{
			return null;
		}
		else if ( lines.length == 1 )
		{
			return lines[0];
		}
		else
		{
			ParagraphLayout.Line lineI = lines[0];
			for (int i = 0; i < lines.length - 1; i++)
			{
				ParagraphLayout.Line lineJ = lines[i+1];
				double iUpperY = lineI.getLineAllocBox().getPositionInParentSpaceY() + lineI.getLineAllocBox().getAllocationY();
				double jLowerY = lineJ.getLineAllocBox().getPositionInParentSpaceY();
				
				double midY = ( iUpperY + jLowerY ) * 0.5;
				
				if ( localPos.y < midY )
				{
					return lineI;
				}
				
				lineI = lineJ;
			}
			
			return lines[lines.length-1];
		}
	}

	private DPWidget getLineChildClosestToLocalPoint(ParagraphLayout.Line line, Point2 localPos)
	{
		LAllocBox children[] = line.getChildAllocBoxes();
		if ( children.length == 0 )
		{
			return null;
		}
		else if ( children.length == 1 )
		{
			return children[0].getElement();
		}
		else
		{
			LAllocBox childI = children[0];
			for (int i = 0; i < children.length - 1; i++)
			{
				LAllocBox childJ = children[i+1];
				double iUpperX = childI.getPositionInParentSpaceX() + childI.getAllocationX();
				double jLowerX = childJ.getPositionInParentSpaceX();
				
				double midX = ( iUpperX + jLowerX ) * 0.5;
				
				if ( localPos.x < midX )
				{
					return childI.getElement();
				}
				
				childI = childJ;
			}
			
			return children[children.length-1].getElement();
		}
	}

	protected DPWidget getChildLeafClosestToLocalPoint(Point2 localPos, WidgetFilter filter)
	{
		ParagraphLayout.Line line = getLineClosestToLocalPoint( localPos );
		
		if ( line != null )
		{
			DPWidget child = getLineChildClosestToLocalPoint( line, localPos );
			
			DPWidget c = getLeafClosestToLocalPointFromChild( child, localPos, filter );
			
			if ( c != null )
			{
				return c;
			}
			
			int index = 0;
			for (DPWidget w: collationLeaves)
			{
				if ( w == child )
				{
					break;
				}
				index++;
			}
			if ( index == collationLeaves.length )
			{
				throw new RuntimeException( "This shouldn't have happened" );
			}
			
			DPWidget next = null;
			for (int j = index + 1; j < collationLeaves.length; j++)
			{
				next = getLeafClosestToLocalPointFromChild( collationLeaves[j], localPos, filter );
				if ( next != null )
				{
					break;
				}
			}

			DPWidget prev = null;
			for (int j = index - 1; j >= 0; j--)
			{
				prev = getLeafClosestToLocalPointFromChild( collationLeaves[j], localPos, filter );
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
				double distToPrev = localPos.x - ( prev.getPositionInParentSpace().x + prev.getAllocationInParentSpace().x );
				double distToNext = next.getPositionInParentSpace().x - localPos.x;
				
				return distToPrev > distToNext  ?  prev  :  next;
			}
		}
		else
		{
			return null;
		}
	}
	

	
	
	
	protected AABox2[] computeCollatedBranchBoundsBoxes(DPContainer collatedBranch, int rangeStart, int rangeEnd)
	{
		refreshCollation();
		
		int startLineIndex = ParagraphLayout.Line.searchForStartLine( lines, rangeStart );
		int endLineIndex = ParagraphLayout.Line.searchForEndLine( lines, rangeEnd );
		
		if ( startLineIndex == endLineIndex )
		{
			ParagraphLayout.Line line = lines[startLineIndex];
			LAllocBox lineChildAllocBoxes[] = line.getChildAllocBoxes();
			int lineRangeStart = line.getRangeStart();
			int startInLine = Math.min( rangeStart - lineRangeStart, lineChildAllocBoxes.length - 1 );
			int endInLine = Math.min( ( rangeEnd - 1 ) - lineRangeStart, lineChildAllocBoxes.length - 1 );
			LAllocBox startBox = lineChildAllocBoxes[startInLine];
			LAllocBox endBox = lineChildAllocBoxes[endInLine];
			LAllocBox lineBox = line.getLineAllocBox();
			double xStart = startBox.getPositionInParentSpaceX();
			double xEnd = endBox.getPositionInParentSpaceX()  +  endBox.getAllocationX();
			double yStart = lineBox.getPositionInParentSpaceY();
			double yEnd = yStart + lineBox.getAllocationY();
			AABox2 box = new AABox2( xStart, yStart, xEnd, yEnd );
			return new AABox2[] { box };
		}
		else
		{
			AABox2 boxes[] = new AABox2[endLineIndex + 1 - startLineIndex];

			ParagraphLayout.Line startLine = lines[startLineIndex];
			int startInLine = rangeStart - startLine.getRangeStart();
			startInLine = Math.min( startInLine, startLine.getChildAllocBoxes().length - 1 );
			LAllocBox startChildBox = startLine.getChildAllocBoxes()[startInLine];
			LAllocBox startLineBox = startLine.getLineAllocBox();
			double xStart = startChildBox.getPositionInParentSpaceX();
			double xEnd = startLineBox.getAllocationX();
			double yStart = startLineBox.getPositionInParentSpaceY();
			double yEnd = yStart + startLineBox.getAllocationY();
			AABox2 startBox = new AABox2( xStart, yStart, xEnd, yEnd );

			ParagraphLayout.Line endLine = lines[startLineIndex];
			int endInLine = ( rangeEnd - 1 ) - endLine.getRangeStart();
			endInLine = Math.min( endInLine, endLine.getChildAllocBoxes().length - 1 );
			LAllocBox endChildBox = endLine.getChildAllocBoxes()[endInLine];
			LAllocBox endLineBox = endLine.getLineAllocBox();
			xStart = 0.0;
			xEnd = endChildBox.getPositionInParentSpaceX() + endChildBox.getAllocationX();
			yStart = endLineBox.getPositionInParentSpaceY();
			yEnd = yStart + endLineBox.getAllocationY();
			AABox2 endBox = new AABox2( xStart, yStart, xEnd, yEnd );
			
			boxes[0] = startBox;
			boxes[boxes.length-1] = endBox;
			
			int j = 1;
			for (int i = startLineIndex + 1; i < endLineIndex; i++)
			{
				LAllocBox lineBox = lines[i].getLineAllocBox();
				xStart = 0.0;
				xEnd = lineBox.getAllocationX();
				yStart = lineBox.getPositionInParentSpaceY();
				yEnd = yStart + lineBox.getAllocationY();
				boxes[j++] = new AABox2( xStart, yStart, xEnd, yEnd );
			}
			
			return boxes;
		}
	}

	
	
	
	//
	// Focus navigation methods
	//
	
	protected DPContentLeaf getContentLeafAboveOrBelowFromChild(DPWidget child, boolean bBelow, Point2 localCursorPos, boolean bSkipWhitespace)
	{
		int childIndex = getCollatedChildren().indexOf( child );
		int lineIndex = ParagraphLayout.Line.searchForEndLine( lines, childIndex );
		Point2 cursorPosInRootSpace = getLocalPointRelativeToRoot( localCursorPos );
		if ( bBelow )
		{
			for (int i = lineIndex + 1; i < lines.length; i++)
			{
				ParagraphLayout.Line line = lines[i];
				DPContentLeaf l = getTopOrBottomContentLeafFromLine( line, false, cursorPosInRootSpace, bSkipWhitespace );
				if ( l != null )
				{
					return l;
				}
			}
		}
		else
		{
			for (int i = lineIndex - 1; i >= 0; i--)
			{
				ParagraphLayout.Line line = lines[i];
				DPContentLeaf l = getTopOrBottomContentLeafFromLine( line, true, cursorPosInRootSpace, bSkipWhitespace );
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		if ( parent != null )
		{
			return parent.getContentLeafAboveOrBelowFromChild( this, bBelow, getLocalPointRelativeToAncestor( parent, localCursorPos ), bSkipWhitespace );
		}
		else
		{
			return null;
		}
	}

	
	
	protected DPContentLeaf getTopOrBottomContentLeafFromLine(ParagraphLayout.Line line, boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		double closestDistance = 0.0;
		DPContentLeaf closestNode = null;
		for (LAllocBox allocBox: line.getChildAllocBoxes())
		{
			DPWidget item = allocBox.getElement();
			
			AABox2 bounds = item.getLocalAABox();
			double lower = item.getLocalPointRelativeToRoot( bounds.getLower() ).x;
			double upper = item.getLocalPointRelativeToRoot( bounds.getUpper() ).x;
			if ( cursorPosInRootSpace.x >=  lower  &&  cursorPosInRootSpace.x <= upper )
			{
				DPContentLeaf l = item.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
				if ( l != null )
				{
					return l;
				}
			}
			else
			{
				double distance;
				if ( cursorPosInRootSpace.x < lower )
				{
					// Cursor to the left of the box
					distance = lower - cursorPosInRootSpace.x;
				}
				else // cursorPosInRootSpace.x > upper
				{
					// Cursor to the right of the box
					distance = cursorPosInRootSpace.x - upper;
				}
				
				if ( closestNode == null  ||  distance < closestDistance )
				{
					DPContentLeaf l = item.getTopOrBottomContentLeaf( bBottom, cursorPosInRootSpace, bSkipWhitespace );
					if ( l != null )
					{
						closestDistance = distance;
						closestNode = l;
					}
				}
			}
		}
		
		if ( closestNode != null )
		{
			return closestNode;
		}
		
		return null;
	}

	
	protected DPContentLeaf getTopOrBottomContentLeaf(boolean bBottom, Point2 cursorPosInRootSpace, boolean bSkipWhitespace)
	{
		if ( bBottom )
		{
			for (int i = lines.length - 1; i >= 0; i--)
			{
				ParagraphLayout.Line line = lines[i];
				DPContentLeaf l = getTopOrBottomContentLeafFromLine( line, bBottom, cursorPosInRootSpace, bSkipWhitespace );
				if ( l != null )
				{
					return l;
				}
			}
		}
		else
		{
			for (ParagraphLayout.Line line: lines)
			{
				DPContentLeaf l = getTopOrBottomContentLeafFromLine( line, bBottom, cursorPosInRootSpace, bSkipWhitespace );
				if ( l != null )
				{
					return l;
				}
			}
		}
		
		return null;
	}

	
	
	protected List<DPWidget> horizontalNavigationList()
	{
		return getCollatedChildren();
	}
	
	
	
	//
	//
	// STYLESHEET METHODS
	//
	//


	protected PackingParams getDefaultPackingParams()
	{
		return null;
	}

	public double getSpacing()
	{
		return ((ParagraphStyleSheet)styleSheet).getSpacing();
	}

	public double getLineSpacing()
	{
		return ((ParagraphStyleSheet)styleSheet).getLineSpacing();
	}

	public double getIndentation()
	{
		return ((ParagraphStyleSheet)styleSheet).getIndentation();
	}
}
