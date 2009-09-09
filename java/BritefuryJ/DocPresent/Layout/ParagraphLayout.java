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
	private static class BreakEntry
	{
		public LReqBox breakBox;
		public int indexInChildList;
		public double xAtBreak, xAfterBreak;
		
		
		public BreakEntry(LReqBox breakBox, int indexInChildList, double xAtBreak)
		{
			this.breakBox = breakBox;
			this.indexInChildList = indexInChildList;
			this.xAtBreak = xAtBreak;
			this.xAfterBreak = xAtBreak;
		}
	}
	
	
	public static class Line
	{
		protected LReqBox lineReqBox;
		protected LAllocBox lineAllocBox;
		protected LReqBox children[];
		protected LAllocBox childrenAlloc[];
		protected int childAllocationFlags[];
		protected int startIndex, endIndex;
		
		
		private Line(LReqBox ch[], LAllocBox chAlloc[], int chAllocFlags[], double indentation, double spacing, double allocation, int startIndex, int endIndex)
		{
			children = ch;
			childrenAlloc = chAlloc;
			childAllocationFlags = chAllocFlags;
			
			lineReqBox = new LReqBox();
			lineAllocBox = new LAllocBox( null );
			HorizontalLayout.computeRequisitionX( lineReqBox, children, spacing );
			lineAllocBox.allocationX = allocation - indentation;
			HorizontalLayout.allocateX( lineReqBox, children, lineAllocBox, childrenAlloc, chAllocFlags, spacing );
			for (LAllocBox childAlloc: childrenAlloc)
			{
				childAlloc.positionInParentSpaceX += indentation;
			}
			
			this.startIndex = startIndex;
			this.endIndex = endIndex;
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
		
		public int getRangeStart()
		{
			return startIndex;
		}
		
		public int getRangeEnd()
		{
			return endIndex;
		}
		
		
		private void computeRequisitionY()
		{
			HorizontalLayout.computeRequisitionY( lineReqBox, children, childAllocationFlags );
		}

		private void allocateY()
		{
			HorizontalLayout.allocateY( lineReqBox, children, lineAllocBox, childrenAlloc, childAllocationFlags );
			
			for (LAllocBox childAlloc: childrenAlloc)
			{
				childAlloc.positionInParentSpaceY  +=  lineAllocBox.positionInParentSpaceY;
			}
		}
		
		
		public static Line createRangeTestLine(int startIndex, int endIndex)
		{
			return new Line( new LReqBox[] {}, new LAllocBox[] {}, new int[] {}, 0.0, 0.0, 0.0, startIndex, endIndex );
		}
		
		
		
		public static int searchForStartLine(Line lines[], int startIndex)
		{
			int lo = 0;
			int hi = lines.length;
			while ( lo < hi )
			{
				int mid = ( lo + hi ) / 2;
				if ( startIndex < lines[mid].startIndex )
				{
					hi = mid;
				}
				else if ( startIndex >= lines[mid].endIndex )
				{
					lo = mid + 1;
				}
				else
				{
					return mid;
				}
			}
			
			return Math.min( lo, lines.length - 1 );
		}

	
		public static int searchForEndLine(Line lines[], int endIndex)
		{
			int lo = 0;
			int hi = lines.length;
			while ( lo < hi )
			{
				int mid = ( lo + hi ) / 2;
				if ( endIndex < lines[mid].startIndex )
				{
					hi = mid;
				}
				else if ( endIndex >= lines[mid].endIndex )
				{
					lo = mid + 1;
				}
				else
				{
					return mid;
				}
			}
			
			return lo-1;
		}
	}
	
	

	public static void computeRequisitionX(LReqBox box, LReqBox children[], double indentation, double hSpacing)
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
			
			prefWidth = prefX + child.prefWidth;
			prefAdvance = prefX + child.prefHAdvance;
			prefX = prefAdvance + hSpacing;
			
		
			if ( child.isLineBreak() )
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
				lineWidth = lineX + child.minWidth;
				lineAdvance = lineX + child.minHAdvance;
				lineX = lineAdvance + hSpacing;
			}
		}
		
		
		minWidth = Math.max( minWidth, lineWidth );
		minAdvance = Math.max( minAdvance, lineAdvance );
		minX = Math.max( minX, lineX );

		box.setRequisitionX( minWidth, prefWidth, minAdvance, prefAdvance );
	}

	
	public static void computeRequisitionY(LReqBox box, Line lines[], double vSpacing)
	{
		LReqBox lineBoxes[] = new LReqBox[lines.length];
		
		int i = 0;
		for (Line line: lines)
		{
			line.computeRequisitionY();
			lineBoxes[i++] = line.lineReqBox;
		}
		
		if ( lines.length == 1 )
		{
			box.setRequisitionY( lineBoxes[0] );
		}
		else
		{
			VerticalLayout.computeRequisitionY( box, lineBoxes, VTypesetting.NONE, vSpacing );
		}
	}




	public static Line[] allocateX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], int childAllocationFlags[], double indentation, double spacing)
	{
		// The paragraph-flow algorithm works as follows:
		// Children are positioned left-to-right, sequentially. Their positions are accumulated.
		// When the amount of horizontal space accumulated, exceeds the amount of width allocated to the paragraph, start a new line.
		//
		// A new line is started by choosing a line break, and breaking the current line at that point, and starting the next line
		// from the next child onwards.
		// As children are allocated/accumulated, any line breaks are picked out. Two line breaks are considered when breaking
		// a line; the best line break (the line break which has the least cost, out of all encountered on the current line), and the
		// most recent. In some cases, the children subsequent to the best line break would accumulate sufficient width to overflow
		// the paragraph width allocation; we would need to break again. In this case, the most recent line break is chosen.
		//
		// This is accomplished by keeping a list of all line breaks encountered. When a line break is used (to break a line), the list
		// is rescanned from that point onwards, so that a new best line break can be chosen.
		
		
		boolean bFirstLine = true;
		
		ArrayList<Line> lines = new ArrayList<Line>();
		int lineStartIndex = 0;
		
		double lineWidth = 0.0;
		double lineAdvance = 0.0;
		double lineX = 0.0;
		
		// We keep a list of all line breaks, and the x position 
		ArrayList<BreakEntry> lineBreaks = new ArrayList<BreakEntry>();
		BreakEntry bestLineBreak = null, lastLineBreak = null;
		int bestLineBreakEntryIndex = -1, lastLineBreakEntryIndex = -1;
		int lineBreakEntryListOffset = 0;
		
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			
			// Keep track of the best and most recent line break boxes
			if ( child.isLineBreak() )
			{
				BreakEntry entry = new BreakEntry( child, i, lineX );
				if ( bestLineBreak == null  ||  child.lineBreakCost <= bestLineBreak.breakBox.lineBreakCost )
				{
					bestLineBreak = entry;
					bestLineBreakEntryIndex = lineBreaks.size();
				}
				
				lastLineBreak = entry;
				lastLineBreakEntryIndex = lineBreaks.size();
				
				// Add afterwards so that size() will give the index of the entry to be added
				lineBreaks.add( entry );
			}
			
			// Accumulate width, advance, and x
			lineWidth = lineX + child.prefWidth;
			lineAdvance = lineX + child.prefHAdvance;
			lineX = lineAdvance + spacing;
			
			// Note the x position after a line break
			if ( lastLineBreak != null  &&  child == lastLineBreak.breakBox )
			{
				lastLineBreak.xAfterBreak = lineX;
			}
			
			
			// Compute a line 'progress' value, as the minimum of the width and the advance; depending upon the content, the advance may be smaller or larger
			// than the width. Use the smallest one, otherwise problems can arise in some circumstances.
			double lineProgress = Math.min( lineWidth, lineAdvance );
			if ( lineProgress > ( allocBox.allocationX * LReqBox.ONE_PLUS_EPSILON )   &&   bestLineBreak != null  &&  i > lineStartIndex )
			{
				// We need to start a new line
				
				// Pick a line break
				int lineBreakIndex;
				double xAfterLineBreak;
				if ( lastLineBreak != null  &&  child != lastLineBreak.breakBox  &&  ( lineProgress - bestLineBreak.xAtBreak )  >  allocBox.allocationX )
				{
					// We still go over the allocation limit even if we do split at the best line break.
					// In this case, choose the most recent line break instead
					lineBreakIndex = lastLineBreak.indexInChildList;
					xAfterLineBreak = lastLineBreak.xAfterBreak;
					lineBreakEntryListOffset = lastLineBreakEntryIndex + 1;
				}
				else
				{
					// Go with the best line break (the one with the least cost)
					lineBreakIndex = bestLineBreak.indexInChildList;
					xAfterLineBreak = bestLineBreak.xAfterBreak;
					lineBreakEntryListOffset = bestLineBreakEntryIndex + 1;
				}
				
				// Build a list of child boxes for the line
				int lineLength = lineBreakIndex - lineStartIndex;
				LReqBox lineChildren[] = new LReqBox[lineLength];
				LAllocBox lineChildrenAlloc[] = new LAllocBox[lineLength];
				int lineChildAllocFlags[] = new int[lineLength];
				System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
				System.arraycopy( childrenAlloc, lineStartIndex, lineChildrenAlloc, 0, lineLength );
				System.arraycopy( childAllocationFlags, lineStartIndex, lineChildAllocFlags, 0, lineLength );
				lines.add( new Line( lineChildren, lineChildrenAlloc, childAllocationFlags, bFirstLine  ?  0.0  :  indentation, spacing, allocBox.allocationX, lineStartIndex, lineBreakIndex ) );
				
				// Next line
				lineStartIndex = lineBreakIndex + 1;
				
				lineWidth -= xAfterLineBreak;
				lineAdvance -= xAfterLineBreak;
				lineX -= xAfterLineBreak;
				
				lineWidth += indentation;
				lineAdvance += indentation;
				lineX += indentation;
				
				// Reset line break
				bestLineBreak = null;

				// Scan for new line break
				for (int j = lineBreakEntryListOffset; j < lineBreaks.size(); j++)
				{
					BreakEntry entry = lineBreaks.get( j );
					
					entry.xAtBreak -= xAfterLineBreak;
					entry.xAfterBreak -= xAfterLineBreak;
					entry.xAtBreak += indentation;
					entry.xAfterBreak += indentation;
					
					if ( bestLineBreak == null  ||  entry.breakBox.lineBreakCost <= bestLineBreak.breakBox.lineBreakCost )
					{
						// Found a better line break
						bestLineBreak = entry;
						bestLineBreakEntryIndex = j;
					}
					
					lastLineBreak = entry;
					lastLineBreakEntryIndex = j;
				}
				bFirstLine = false;
			}
		}
		
		if ( lineStartIndex < children.length )
		{
			// Create the last line
			int lineLength = children.length - lineStartIndex;
			LReqBox lineChildren[] = new LReqBox[lineLength];
			LAllocBox lineChildrenAlloc[] = new LAllocBox[lineLength];
			int lineChildAllocFlags[] = new int[lineLength];
			System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
			System.arraycopy( childrenAlloc, lineStartIndex, lineChildrenAlloc, 0, lineLength );
			System.arraycopy( childAllocationFlags, lineStartIndex, lineChildAllocFlags, 0, lineLength );
			lines.add( new Line( lineChildren, lineChildrenAlloc, lineChildAllocFlags, bFirstLine  ?  0.0  :  indentation, spacing, allocBox.allocationX, lineStartIndex, children.length ) );
		}
		
		
		Line[] lineArray = new Line[lines.size()];
		return lines.toArray( lineArray );
	}





	public static void allocateY(LReqBox box, LAllocBox allocBox, Line lines[], double lineSpacing)
	{
		LReqBox lineReqBoxes[] = new LReqBox[lines.length];
		LAllocBox lineAllocBoxes[] = new LAllocBox[lines.length];
		
		int i = 0;
		for (Line line: lines)
		{
			lineReqBoxes[i] = line.lineReqBox;
			lineAllocBoxes[i++] = line.lineAllocBox;
		}
		
		if ( lines.length == 1 )
		{
			allocBox.allocateChildYAsRequisition( lineAllocBoxes[0], lineReqBoxes[0], 0.0 );
		}
		else
		{
			VerticalLayout.allocateY( box, lineReqBoxes, allocBox, lineAllocBoxes, lineSpacing, false );
		}
		
		for (Line line: lines)
		{
			line.allocateY();
		}
	}

}
