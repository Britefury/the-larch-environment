//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import java.util.ArrayList;

public class ParagraphLayout
{
	public static class Line
	{
		protected LReqBox lineReqBox;
		protected LAllocBox lineAllocBox;
		protected LReqBox children[];
		protected LAllocBox childrenAlloc[];
		
		
		private Line(LReqBox ch[], LAllocBox chAlloc[], double indentation, double spacing, BoxPackingParams packingParams[], double allocation)
		{
			children = ch;
			childrenAlloc = chAlloc;
			
			lineReqBox = new LReqBox();
			lineAllocBox = new LAllocBox( null );
			HorizontalLayout.computeRequisitionX( lineReqBox, children, spacing, packingParams );
			lineAllocBox.allocationX = allocation - indentation;
			HorizontalLayout.allocateX( lineReqBox, children, lineAllocBox, childrenAlloc, spacing, packingParams );
			for (LAllocBox childAlloc: childrenAlloc)
			{
				childAlloc.positionInParentSpaceX += indentation;
			}
		}
		
		
		public LReqBox getLineBox()
		{
			return lineReqBox;
		}
		
		public LAllocBox getLineAllocBox()
		{
			return lineAllocBox;
		}
		
		public LReqBox[] getChildBoxes()
		{
			return children;
		}
		
		public LAllocBox[] getChildAllocBoxes()
		{
			return childrenAlloc;
		}
		
		
		private void computeRequisitionY(VAlignment vAlignment)
		{
			HorizontalLayout.computeRequisitionY( lineReqBox, children, vAlignment );
		}

		private void allocateY(VAlignment vAlignment)
		{
			HorizontalLayout.allocateY( lineReqBox, children, lineAllocBox, childrenAlloc, vAlignment );
			
			for (LAllocBox childAlloc: childrenAlloc)
			{
				childAlloc.positionInParentSpaceY  +=  lineAllocBox.positionInParentSpaceY;
			}
		}
	}
	
	

	public static void computeRequisitionX(LReqBox box, LReqBox children[], double indentation, double hSpacing, BoxPackingParams packingParams[])
	{
		// Accumulate the width required for all the children
		
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater
		
		double minWidth = 0.0, lineWidth = 0.0, prefWidth = 0.0;
		double minAdvance = 0.0, lineAdvance = 0.0, prefAdvance = 0.0;
		double minX = 0.0, lineX = 0.0, prefX = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			
			BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
			double padding = params != null  ?  params.padding  :  0.0;
			
			// Filter out any h-spacing that is within the amount of padding
			double prefChildSpacing = Math.max( child.prefHSpacing - padding, 0.0 );
			
			prefWidth = prefX + child.prefWidth  +  padding * 2.0;
			prefAdvance = prefWidth + prefChildSpacing;
			prefX = prefAdvance + hSpacing;

		
			if ( child.bLineBreak )
			{
				minWidth = Math.max( minWidth, lineWidth );
				minAdvance = Math.max( minAdvance, lineAdvance );
				minX = Math.max( minX, lineX );
				
				// New line
				lineWidth = 0.0;
				lineAdvance = 0.0;
				lineX = indentation;
			}
			else
			{
				double minChildSpacing = Math.max( child.minHSpacing - padding, 0.0 );
				
				lineWidth = lineX + child.minWidth  +  padding * 2.0;
				lineAdvance = lineWidth + minChildSpacing;
				lineX = lineAdvance + hSpacing;
			}
		}
		
		
		minWidth = Math.max( minWidth, lineWidth );
		minAdvance = Math.max( minAdvance, lineAdvance );
		minX = Math.max( minX, lineX );

		box.setRequisitionX( minWidth, prefWidth, minAdvance - minWidth, prefAdvance - prefWidth );
	}

	
	public static void computeRequisitionY(LReqBox box, Line lines[], double vSpacing, VAlignment vAlignment)
	{
		LReqBox lineBoxes[] = new LReqBox[lines.length];
		
		int i = 0;
		for (Line line: lines)
		{
			line.computeRequisitionY( vAlignment );
			lineBoxes[i++] = line.lineReqBox;
		}
		
		VerticalLayout.computeRequisitionY( box, lineBoxes, vSpacing, null );
	}




	public static Line[] allocateX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], double indentation, double hSpacing, BoxPackingParams packingParams[])
	{
		boolean bFirstLine = true;
		
		ArrayList<Line> lines = new ArrayList<Line>();
		int lineStartIndex = 0;
		
		double lineWidth = 0.0;
		double lineAdvance = 0.0;
		double lineX = 0.0;
		
		LReqBox bestLineBreak = null;
		int bestLineBreakIndex = -1;
		double xAtBestLineBreak = 0.0, xAfterBestLineBreak = 0.0;
		
		LReqBox lastLineBreak = null;
		int lastLineBreakIndex = -1;
		double xAfterLastLineBreak = 0.0;
		
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			
			BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
			double padding = params != null  ?  params.padding  :  0.0;
			
			// Filter out any h-spacing that is within the amount of padding
			double childSpacing = Math.max( child.prefHSpacing - padding, 0.0 );
			
			// Keep track of the best and most recent line break boxes
			if ( child.bLineBreak )
			{
				if ( bestLineBreak == null  ||  child.lineBreakCost <= bestLineBreak.lineBreakCost )
				{
					// Found a better line break
					bestLineBreak = child;
					bestLineBreakIndex = i;
					xAtBestLineBreak = lineX;
				}
				
				lastLineBreak = child;
				lastLineBreakIndex = i;
			}
			
			// Accumulate width, advance, and x
			lineWidth = lineX + child.prefWidth  +  padding * 2.0;
			lineAdvance = lineWidth + childSpacing;
			lineX = lineAdvance + hSpacing;
			
			// Note the x position after the best and most recent line breaks
			if ( child == bestLineBreak )
			{
				xAfterBestLineBreak = lineX;
			}
			
			if ( child == lastLineBreak )
			{
				xAfterLastLineBreak = lineX;
			}
			
			
			if ( lineWidth > allocBox.allocationX  &&  bestLineBreak != null  &&  i > lineStartIndex )
			{
				// We need to start a new line
				
				// Pick a line break
				int lineBreakIndex;
				double xAfterLineBreak;
				if ( ( lineWidth - xAtBestLineBreak )  >  allocBox.allocationX  &&  child != lastLineBreak  &&  lastLineBreak != null )
				{
					// We still go over the allocation limit even if we do split at the best line break.
					// In this case, choose the most recent line break instead
					lineBreakIndex = lastLineBreakIndex;
					xAfterLineBreak = xAfterLastLineBreak;
				}
				else
				{
					// Go with the best line break (the one with the least cost)
					lineBreakIndex = bestLineBreakIndex;
					xAfterLineBreak = xAfterBestLineBreak;
				}
				
				// Build a list of child boxes for the line
				int lineLength = lineBreakIndex - lineStartIndex;
				LReqBox lineChildren[] = new LReqBox[lineLength];
				LAllocBox lineChildrenAlloc[] = new LAllocBox[lineLength];
				System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
				System.arraycopy( childrenAlloc, lineStartIndex, lineChildrenAlloc, 0, lineLength );
				BoxPackingParams linePackingParams[] =  null;
				if ( packingParams != null )
				{
					linePackingParams = new BoxPackingParams[lineLength];
					System.arraycopy( packingParams, lineStartIndex, linePackingParams, 0, lineLength );
				}
				lines.add( new Line( lineChildren, lineChildrenAlloc, bFirstLine  ?  0.0  :  indentation, hSpacing, linePackingParams, allocBox.allocationX ) );
				
				// Next line
				lineStartIndex = lineBreakIndex + 1;
				
				lineWidth -= xAfterLineBreak;
				lineAdvance -= xAfterLineBreak;
				lineX -= xAfterLineBreak;
				
				lineWidth += indentation;
				lineAdvance += indentation;
				lineX += indentation;
				
				bestLineBreak = null;
				bestLineBreakIndex = -1;
				xAtBestLineBreak = 0.0;
				xAfterBestLineBreak = 0.0;

				bFirstLine = false;
			}
		}
		
		if ( lineStartIndex < children.length )
		{
			// Create the last line
			int lineLength = children.length - lineStartIndex;
			LReqBox lineChildren[] = new LReqBox[lineLength];
			LAllocBox lineChildrenAlloc[] = new LAllocBox[lineLength];
			System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
			System.arraycopy( childrenAlloc, lineStartIndex, lineChildrenAlloc, 0, lineLength );
			BoxPackingParams linePackingParams[] =  null;
			if ( packingParams != null )
			{
				linePackingParams = new BoxPackingParams[lineLength];
				System.arraycopy( packingParams, lineStartIndex, linePackingParams, 0, lineLength );
			}
			lines.add( new Line( lineChildren, lineChildrenAlloc, bFirstLine  ?  0.0  :  indentation, hSpacing, linePackingParams, allocBox.allocationX ) );
		}
		
		
		Line[] lineArray = new Line[lines.size()];
		return lines.toArray( lineArray );
	}





	public static void allocateY(LReqBox box, LAllocBox allocBox, Line lines[], double vSpacing, VAlignment vAlignment)
	{
		LReqBox lineReqBoxes[] = new LReqBox[lines.length];
		LAllocBox lineAllocBoxes[] = new LAllocBox[lines.length];
		
		int i = 0;
		for (Line line: lines)
		{
			lineReqBoxes[i] = line.lineReqBox;
			lineAllocBoxes[i++] = line.lineAllocBox;
		}
		
		VerticalLayout.allocateY( box, lineReqBoxes, allocBox, lineAllocBoxes, vSpacing, null );
		
		for (Line line: lines)
		{
			line.allocateY( vAlignment );
		}
	}

}
