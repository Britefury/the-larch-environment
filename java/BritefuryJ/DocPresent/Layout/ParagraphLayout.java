//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

import java.util.ArrayList;
import java.util.Stack;

public class ParagraphLayout
{
	private static class IndentationEntry
	{
		public int indexInChildList;
		public double indentation;
		public boolean bOnStack;
		
		
		public IndentationEntry(int indexInChildList, double indentation)
		{
			this.indexInChildList = indexInChildList;
			this.indentation = indentation;
			this.bOnStack = true;
		}
	}
	
	
	private static class BreakEntry
	{
		public LReqBoxInterface breakBox;
		public int indexInChildList;
		public double xAtBreak, xAfterBreak;
		public IndentationEntry lineIndentation;
		
		
		public BreakEntry(LReqBoxInterface child, int indexInChildList, double xAtBreak, double xAfterBreak, IndentationEntry lineIndentation)
		{
			this.breakBox = child;
			this.indexInChildList = indexInChildList;
			this.xAtBreak = xAtBreak;
			this.xAfterBreak = xAfterBreak;
			this.lineIndentation = lineIndentation;
		}
	}
	
	
	public static class Line
	{
		protected LReqBox lineReqBox;
		protected LAllocBox lineAllocBox;
		protected LReqBoxInterface children[];
		protected LAllocBoxInterface childrenAlloc[];
		protected int childAllocationFlags[];
		protected int startIndex, endIndex;
		protected double lineIndentation;
		
		
		private Line(LReqBoxInterface ch[], LAllocBoxInterface chAlloc[], int chAllocFlags[], double lineIndentation, int startIndex, int endIndex)
		{
			children = ch;
			childrenAlloc = chAlloc;
			childAllocationFlags = chAllocFlags;
			
			lineReqBox = new LReqBox();
			lineAllocBox = new LAllocBox( null );
			
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.lineIndentation = lineIndentation;
		}
		
		
		public LReqBox getLineBox()
		{
			return lineReqBox;
		}
		
		public LAllocBox getLineAllocBox()
		{
			return lineAllocBox;
		}
		
		public LReqBoxInterface[] getChildBoxes()
		{
			return children;
		}
		
		public LAllocBoxInterface[] getChildAllocBoxes()
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
		
		public double getLineIndentation()
		{
			return lineIndentation;
		}
		
		
		private void computeRequisitionY()
		{
			HorizontalLayout.computeRequisitionY( lineReqBox, children, childAllocationFlags );
		}

		private void allocateX(double spacing, double allocation)
		{
			HorizontalLayout.computeRequisitionX( lineReqBox, children, spacing );
			lineAllocBox.allocationX = allocation - lineIndentation;
			HorizontalLayout.allocateX( lineReqBox, children, lineAllocBox, childrenAlloc, childAllocationFlags, spacing );
			for (LAllocBoxInterface childAlloc: childrenAlloc)
			{
				childAlloc.setAllocPositionInParentSpaceX( childAlloc.getAllocPositionInParentSpaceX() + lineIndentation );
			}
		}
		
		private void allocateY()
		{
			HorizontalLayout.allocateY( lineReqBox, children, lineAllocBox, childrenAlloc, childAllocationFlags );
			
			for (LAllocBoxInterface childAlloc: childrenAlloc)
			{
				childAlloc.setAllocPositionInParentSpaceY( childAlloc.getAllocPositionInParentSpaceY() + lineAllocBox.positionInParentAllocationSpaceY );
			}
		}
		
		
		public static Line createRangeTestLine(int startIndex, int endIndex)
		{
			return new Line( new LReqBox[] {}, new LAllocBox[] {}, new int[] {}, 0.0, startIndex, endIndex );
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
	
	

	public static void computeRequisitionX(LReqBoxInterface box, LReqBoxInterface children[], double indentation, double hSpacing)
	{
		// Accumulate the width required for all the children
		
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater
		
		Stack<IndentationEntry> indentationStack = new Stack<IndentationEntry>();
		indentationStack.add( new IndentationEntry( -1, indentation ) );

		double minWidth = 0.0, lineWidth = 0.0, prefWidth = 0.0;
		double minAdvance = 0.0, lineAdvance = 0.0, prefAdvance = 0.0;
		double lineX = 0.0, prefX = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBoxInterface child = children[i];

			// Handle indentation markers; maintain the indentation stack
			if ( child.isReqParagraphIndentMarker() )
			{
				indentationStack.push( new IndentationEntry( i, lineX ) );
			}
			else if ( child.isReqParagraphDedentMarker() )
			{
				if ( indentationStack.size() > 1 )
				{
					indentationStack.lastElement().bOnStack = false;
					indentationStack.pop();
				}
			}

			prefWidth = prefX + child.getReqPrefWidth();
			prefAdvance = prefX + child.getReqPrefHAdvance();
			prefX = prefAdvance + hSpacing;
			
		
			if ( child.isReqLineBreak() )
			{
				minWidth = Math.max( minWidth, lineWidth );
				minAdvance = Math.max( minAdvance, lineAdvance );
				
				// New line
				lineWidth = 0.0;
				lineAdvance = 0.0;
				lineX = indentationStack.lastElement().indentation;
			}
			else
			{
				lineWidth = lineX + child.getReqMinWidth();
				lineAdvance = lineX + child.getReqMinHAdvance();
				lineX = lineAdvance + hSpacing;
			}
		}
		
		
		minWidth = Math.max( minWidth, lineWidth );
		minAdvance = Math.max( minAdvance, lineAdvance );

		box.setRequisitionX( minWidth, prefWidth, minAdvance, prefAdvance );
	}

	
	public static void computeRequisitionY(LReqBoxInterface box, Line lines[], double vSpacing)
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
			VerticalLayout.computeRequisitionY( box, lineBoxes, 0, vSpacing );
		}
	}




	public static Line[] allocateX(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[], double indentation, double spacing)
	{
		// The paragraph-flow algorithm works as follows:
		// Children are positioned left-to-right, sequentially. Their positions are accumulated.
		// When the amount of horizontal space accumulated, exceeds the amount of width allocated to the paragraph, start a new line.
		//
		// A new line is started by choosing a line break, and breaking the current line at that point, and starting the next line
		// from the next child onwards.
		// As children are allocated/accumulated, any line breaks that are encountered are recorded. A reference to the best line break
		// is maintained (the line break with the minimum cost) by changing whenever a line break with a lower cost is encountered.
		// In some cases, the children subsequent to the best line break would accumulate sufficient width to overflow
		// the paragraph width allocation; this line break is not sufficient. In this case, the subset of all known line breaks that
		// would not result in a subsequence line overflowing the paragraph width, are searched for the line break with the minimum cost.
		//
		// When a line break is chosen, the list of all subsequent line breaks is searched in order to find a new 'best' line break.
		
		
		boolean bFirstLine = true;
		
		ArrayList<Line> lines = new ArrayList<Line>();
		int lineStartIndex = 0;
		
		double lineWidth = 0.0;
		double lineAdvance = 0.0;
		double lineX = 0.0;
		
		double allocBoxAllocationX = allocBox.getAllocationX();
		
		// We keep a list of all line breaks, and the x position 
		ArrayList<BreakEntry> lineBreaks = new ArrayList<BreakEntry>();
		BreakEntry bestLineBreak = null;
		int bestLineBreakEntryIndex = -1;
		int lineBreakEntryListOffset = 0;
		BreakEntry lineBreakAtLineStart = null;
		
		Stack<IndentationEntry> indentationStack = new Stack<IndentationEntry>();
		indentationStack.add( new IndentationEntry( -1, indentation ) );
		
		for (int i = 0; i < children.length; i++)
		{
			LReqBoxInterface child = children[i];
			
			if ( child.isReqParagraphIndentMarker() )
			{
				indentationStack.push( new IndentationEntry( i, lineX ) );
			}
			else if ( child.isReqParagraphDedentMarker() )
			{
				if ( indentationStack.size() > 1 )		// Do not remove entry 0 - it is added at stack creation time, and contains the default indentation
				{
					indentationStack.lastElement().bOnStack = false;
					indentationStack.pop();
				}
			}
			
			double lineXAtChildStart = lineX;
			
			// Accumulate width, advance, and x
			lineWidth = lineX + child.getReqPrefWidth();
			lineAdvance = lineX + child.getReqPrefHAdvance();
			lineX = lineAdvance + spacing;
			
			// Keep track of the best and most recent line break boxes
			if ( child.isReqLineBreak() )
			{
				BreakEntry entry = new BreakEntry( child, i, lineXAtChildStart, lineX, indentationStack.lastElement() );
				if ( bestLineBreak == null  ||  child.getReqLineBreakCost() <= bestLineBreak.breakBox.getReqLineBreakCost() )
				{
					bestLineBreak = entry;
					bestLineBreakEntryIndex = lineBreaks.size();
				}
				
				// Add after testing to see if it is the best line break, so that size() will give the index of the entry to be added
				lineBreaks.add( entry );
			}
			
			
			// Compute a line 'progress' value, as the minimum of the width and the advance; depending upon the content, the advance may be smaller or larger
			// than the width. Use the smallest one, otherwise problems can arise in some circumstances.
			double lineProgress = Math.min( lineWidth, lineAdvance );
			if ( lineProgress > ( allocBoxAllocationX * LReqBox.ONE_PLUS_EPSILON )   &&   bestLineBreak != null  &&  i > lineStartIndex )
			{
				// We need to start a new line
				
				// Pick a line break
				BreakEntry chosenLineBreak = null;
				if ( ( lineProgress - bestLineBreak.xAfterBreak )  >  allocBoxAllocationX )
				{
					// Splitting at the best line break will result in a new line that will also go over the allocation limit;
					// we need to choose a different line break
					
					// Search the list of line breaks encountered backwards from the end, to the 'best line break'
					BreakEntry newBestBreakEntry = null;
					int newBestBreakEntryIndex = -1;
					for (int j = lineBreaks.size() - 1; j > bestLineBreakEntryIndex; j--)
					{
						BreakEntry entry = lineBreaks.get( j );
						
						if ( ( lineProgress - entry.xAfterBreak )  >  allocBoxAllocationX )
						{
							// Over the allocation limit; use the previous one
							break;
						}
						
						// We have found a 'new best break' if break @j has a lower cost than the current 'best'
						if ( newBestBreakEntry == null  ||  entry.breakBox.getReqLineBreakCost() < newBestBreakEntry.breakBox.getReqLineBreakCost() )
						{
							newBestBreakEntry = entry;
							newBestBreakEntryIndex = j;
						}
					}
					
					
					if ( newBestBreakEntry == null )
					{
						newBestBreakEntryIndex = lineBreaks.size() - 1;
						newBestBreakEntry = lineBreaks.get( newBestBreakEntryIndex );
					}
					
					
					// In this case, choose the most recent line break instead
					chosenLineBreak = newBestBreakEntry;
					lineBreakEntryListOffset = newBestBreakEntryIndex + 1;
				}
				else
				{
					// Go with the best line break (the one with the least cost)
					chosenLineBreak = bestLineBreak;
					lineBreakEntryListOffset = bestLineBreakEntryIndex + 1;
				}
				
				
				int lineBreakIndex = chosenLineBreak.indexInChildList;
				double xAfterLineBreak = chosenLineBreak.xAfterBreak;
				double lineBreakIndentation = lineBreakAtLineStart != null  ?  lineBreakAtLineStart.lineIndentation.indentation  :  indentation;
				double nextLineIndentation = chosenLineBreak.lineIndentation.indentation;

				
				// Build a list of child boxes for the line
				int lineLength = lineBreakIndex - lineStartIndex;
				if ( lineLength > 0 )
				{
					LReqBoxInterface lineChildren[] = new LReqBoxInterface[lineLength];
					LAllocBoxInterface lineChildrenAlloc[] = new LAllocBoxInterface[lineLength];
					int lineChildAllocFlags[] = new int[lineLength];
					System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
					System.arraycopy( childrenAlloc, lineStartIndex, lineChildrenAlloc, 0, lineLength );
					System.arraycopy( childAllocationFlags, lineStartIndex, lineChildAllocFlags, 0, lineLength );
					lines.add( new Line( lineChildren, lineChildrenAlloc, lineChildAllocFlags, bFirstLine  ?  0.0  :  lineBreakIndentation, lineStartIndex, lineBreakIndex ) );
				}
				
				// Next line
				lineBreakAtLineStart = chosenLineBreak;

				lineStartIndex = lineBreakIndex + 1;
				
				lineWidth -= xAfterLineBreak;
				lineAdvance -= xAfterLineBreak;
				lineX -= xAfterLineBreak;
				
				lineWidth += nextLineIndentation;
				lineAdvance += nextLineIndentation;
				lineX += nextLineIndentation;
				
				// Reset line break
				bestLineBreak = null;
				bestLineBreakEntryIndex = -1;
				
				for (int j = indentationStack.size() - 1; j >= 1; j--)
				{
					IndentationEntry entry = indentationStack.get( j );
					if ( entry.indexInChildList <= lineBreakIndex )
					{
						break;
					}
					else
					{
						entry.indentation -= xAfterLineBreak;
						entry.indentation += nextLineIndentation;
					}
				}

				// Update each line line break subsequent to the chosen one, and update it's x-co-ordinate fields
				// Scan for new line break
				for (int j = lineBreakEntryListOffset; j < lineBreaks.size(); j++)
				{
					BreakEntry entry = lineBreaks.get( j );
					
					entry.xAtBreak -= xAfterLineBreak;
					entry.xAfterBreak -= xAfterLineBreak;
					entry.xAtBreak += nextLineIndentation;
					entry.xAfterBreak += nextLineIndentation;
					
					IndentationEntry breakIndentation = entry.lineIndentation;
					if ( !breakIndentation.bOnStack )
					{
						// Indentation entry is not on the stack; it will not have been offset in the loop over the indentation stack
						breakIndentation.indentation -= xAfterLineBreak;
						breakIndentation.indentation += nextLineIndentation;
					}
					
					if ( bestLineBreak == null  ||  entry.breakBox.getReqLineBreakCost() <= bestLineBreak.breakBox.getReqLineBreakCost() )
					{
						// Found a better line break
						bestLineBreak = entry;
						bestLineBreakEntryIndex = j;
					}
				}
				
				
				bFirstLine = false;
			}
		}
		
		if ( lineStartIndex < children.length )
		{
			// Create the last line
			int lineLength = children.length - lineStartIndex;
			if ( lineLength > 0 )
			{
				LReqBoxInterface lineChildren[] = new LReqBoxInterface[lineLength];
				LAllocBoxInterface lineChildrenAlloc[] = new LAllocBoxInterface[lineLength];
				int lineChildAllocFlags[] = new int[lineLength];
				System.arraycopy( children, lineStartIndex, lineChildren, 0, lineLength );
				System.arraycopy( childrenAlloc, lineStartIndex, lineChildrenAlloc, 0, lineLength );
				System.arraycopy( childAllocationFlags, lineStartIndex, lineChildAllocFlags, 0, lineLength );
				double lineIndentation = lineBreakAtLineStart != null  ?  lineBreakAtLineStart.lineIndentation.indentation  :  0.0;
				lines.add( new Line( lineChildren, lineChildrenAlloc, lineChildAllocFlags, lineIndentation, lineStartIndex, children.length ) );
			}
		}
		
		
		for (Line line: lines)
		{
			line.allocateX( spacing, allocBoxAllocationX );
		}

		
		return lines.toArray( new Line[0] );
	}





	public static void allocateY(LReqBoxInterface box, LAllocBoxInterface allocBox, Line lines[], double lineSpacing)
	{
		LReqBoxInterface lineReqBoxes[] = new LReqBox[lines.length];
		LAllocBoxInterface lineAllocBoxes[] = new LAllocBox[lines.length];
		
		int i = 0;
		for (Line line: lines)
		{
			lineReqBoxes[i] = line.lineReqBox;
			lineAllocBoxes[i++] = line.lineAllocBox;
		}
		
		if ( lines.length == 1 )
		{
			LAllocHelper.allocateChildYAsRequisition( lineAllocBoxes[0], lineReqBoxes[0], 0.0 );
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
