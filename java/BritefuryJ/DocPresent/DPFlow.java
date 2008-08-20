package BritefuryJ.DocPresent;

import java.lang.Math;
import java.awt.Color;
import java.util.Vector;

import BritefuryJ.Math.Point2;



public class DPFlow extends DPContainerSequence
{
	public static class CouldNotFindInsertionPointException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	}


	protected static class FlowChildEntry extends DPContainerSequence.ChildEntry
	{
		public double padding;
		
		public FlowChildEntry(DPWidget child, double padding)
		{
			super( child );
			
			this.padding = padding;
		}
	}

	
	
	protected double spacing, padding, indentation;
	protected Vector< Vector<FlowChildEntry> > lines;
	protected VMetrics[] lineVMetrics;

	
	
	public DPFlow()
	{
		this( 0.0, 0.0, 0.0 );
	}

	public DPFlow(double spacing, double padding, double indentation)
	{
		this( spacing, padding, indentation, null );
	}
	
	public DPFlow(double spacing, double padding, double indentation, Color backgroundColour)
	{
		super( backgroundColour );
		
		this.spacing = spacing;
		this.padding = padding;
		this.indentation = indentation;

		lines = new Vector< Vector<FlowChildEntry> >();
	}


	
	public double getSpacing()
	{
		return spacing;
	}

	public void setSpacing(double spacing)
	{
		this.spacing = spacing;
		queueResize();
	}

	
	public double getPadding()
	{
		return padding;
	}

	public void setPadding(double padding)
	{
		this.padding = padding;
		queueResize();
	}

	
	public double getIndentation()
	{
		return indentation;
	}

	public void setIndentation(double indentation)
	{
		this.indentation = indentation;
		queueResize();
	}

	
	
	public void append(DPWidget child)
	{
		appendChildEntry( createChildEntryForChild( child ) );
	}

	public void extend(DPWidget[] children)
	{
		ChildEntry[] entries = new ChildEntry[children.length];
		
		for (int i = 0; i < children.length; i++)
		{
			entries[i] = createChildEntryForChild( children[i] );
		}
		
		extendChildEntries( entries );
	}

	
	public void insert(int index, DPWidget child)
	{
		insertChildEntry( index, createChildEntryForChild( child ) );
	}

	public void remove(DPWidget child)
	{
		removeChildEntry( childToEntry.get( child ) );
	}

	

	public int getInsertIndex(Point2 localPos)
	{
		//Return the index at which an item could be inserted.
		// localPos is checked against the contents of the box in order to determine the insert index
		
		if ( size() == 0 )
		{
			return 0;
		}
	
		double pos = localPos.x;
		
		double[] midPoints = new double[childEntries.size()];
		
		for (int i = 0; i < midPoints.length; i++)
		{
			ChildEntry entry = childEntries.get( i );
			midPoints[i] = entry.pos.x  +  entry.size.x * 0.5;
		}
		
		if ( pos < midPoints[0] )
		{
			return size();
		}
		else if ( pos > midPoints[midPoints.length-1] )
		{
			return 0;
		}
		else
		{
			for (int i = 0; i < midPoints.length-1; i++)
			{
				double lower = midPoints[i];
				double upper = midPoints[i+1];
				if ( pos >= lower  &&  pos <= upper )
				{
					return i + 1;
				}
			}
			
			throw new CouldNotFindInsertionPointException();
		}
	}

	
	
	protected FlowChildEntry createChildEntryForChild(DPWidget child)
	{
		return new FlowChildEntry( child, padding );
	}

	
	protected void childListModified()
	{
	}


	

	protected HMetrics computeRequiredHMetrics()
	{
		if ( childEntries.isEmpty() )
		{
			return new HMetrics();
		}
		else
		{
			// Get the hmetrics for the children
			HMetrics[] childHMetrics = new HMetrics[childEntries.size()];
			for (int i = 0; i < childHMetrics.length; i++)
			{
				childHMetrics[i] = childEntries.get( i ).child.getRequiredHMetrics();
			}
			
			// Accumulate the width required for all the children
			double width = 0.0, advance = 0.0;
			for (int i = 0; i < childHMetrics.length; i++)
			{
				HMetrics chm = childHMetrics[i];
				FlowChildEntry entry = (FlowChildEntry)childEntries.get( i );
				
				// The spacing for the box is @spacing if this is NOT the last child; else 0.0
				double boxSpacing = ( i == childHMetrics.length - 1 )  ?  0.0  :  spacing;
				// Compute the spacing inherent in the advance field of the child h-metrics
				double advanceSpacing = chm.advance - chm.width;
				// Compute the spacing for this child; the greater of the advance spacing and the box spacing
				double childSpacing = Math.max( advanceSpacing, boxSpacing );
				
				width = advance + chm.width + entry.padding * 2.0;
				advance = width + childSpacing;
			}
			
			return new HMetrics( width, advance );
		}
	}

	
	
	protected HMetrics onAllocateX(double allocation)
	{
		lines.clear();
		Vector<FlowChildEntry> currentLine = new Vector<FlowChildEntry>();
		boolean bFirstChildInLine = true;
		boolean bFirstLine = true;
		
		double boxWidth = 0.0, boxAdvance = 0.0;
		
		double x = 0.0, width = 0.0;

		for (ChildEntry baseEntry: childEntries)
		{
			FlowChildEntry entry = (FlowChildEntry)baseEntry;
			
			double childWidth = Math.min( entry.child.hmetrics.width, allocation );
			double childRight = x + childWidth + entry.padding * 2.0;
			
			if ( childRight > allocation )
			{
				// We have gone beyond the end of the line
				
				if ( bFirstChildInLine )
				{
					// First child in line takes up whole line
					
					// This line has 1 child
					currentLine.add( entry );
					lines.add( currentLine );
					
					// Put it at the start
					if ( bFirstLine )
					{
						x = 0.0;
					}
					else
					{
						x = indentation;
					}
					
					// Allocate
					double childAlloc = Math.min( childWidth, allocation - ( x + entry.padding * 2.0 ) );
					HMetrics childAllocatedMetrics = allocateChildX( entry.child, x + entry.padding, childAlloc );
					
					boxWidth = Math.max( boxWidth, childAllocatedMetrics.width );
					boxAdvance = Math.max( boxAdvance, childAllocatedMetrics.advance );

					// Start a new line
					// Nothing in the current line
					currentLine = new Vector<FlowChildEntry>();
					// Start @x at indentation
					width = 0.0; 
					x = indentation;
					// Not the first line
					bFirstLine = false;
					// Next child will be first in line
					bFirstChildInLine = true;
				}
				else
				{
					// Terminate the current line, put @entry into the next line
					
					// Terminate the current line
					lines.add( currentLine );
					currentLine = new Vector<FlowChildEntry>();
					
					// Start a new line
					// Start with @entry in the new line
					currentLine.add( entry );
					// Start @x at indentation
					x = indentation;
					// Not the first line
					bFirstLine = false;
					// Next child will be second in line
					bFirstChildInLine = false;

					// Allocate
					double childAlloc = Math.min( childWidth, allocation - ( x + entry.padding * 2.0 ) );
					HMetrics childAllocatedMetrics = allocateChildX( entry.child, x + entry.padding, childAlloc );
					
					// Move @x on
					// Compute child spacing
					double advanceSpacing = childAllocatedMetrics.advance - childAllocatedMetrics.width;
					double childSpacing = Math.max( advanceSpacing, spacing );
					width = x + childAllocatedMetrics.width + entry.padding * 2.0;
					double advance = x + childAllocatedMetrics.advance + entry.padding * 2.0;
					x = width + childSpacing;

					boxWidth = Math.max( boxWidth, width );
					boxAdvance = Math.max( boxAdvance, advance );
				}
			}
			else
			{
				// Continue existing line
				
				// Allocate
				double childAlloc = Math.min( childWidth, allocation - ( x + entry.padding * 2.0 ) );
				HMetrics childAllocatedMetrics = allocateChildX( entry.child, x + entry.padding, childAlloc );

				// Add @entry to the new line
				currentLine.add( entry );
				// Move @x on
				// Compute child spacing
				double advanceSpacing = childAllocatedMetrics.advance - childAllocatedMetrics.width;
				double childSpacing = Math.max( advanceSpacing, spacing );
				width = x + childAllocatedMetrics.width + entry.padding * 2.0;
				double advance = x + childAllocatedMetrics.advance + entry.padding * 2.0;
				x = width + childSpacing;

				// Next child will not be the first in line
				bFirstChildInLine = false;

				boxWidth = Math.max( boxWidth, width );
				boxAdvance = Math.max( boxAdvance, advance );
			}
		}
		
		
		if ( currentLine.size() > 0 )
		{
			lines.add( currentLine );
		}
		
		return new HMetrics( boxWidth, boxAdvance );
	}
	
	
	
	

	
	
	private VMetrics computeRequiredVMetricsForLine(Vector<FlowChildEntry> line)
	{
		if ( line.size() == 0 )
		{
			return new VMetrics();
		}
		else
		{
			VMetrics[] childVMetrics = new VMetrics[line.size()];
			for (int i = 0; i < childVMetrics.length; i++)
			{
				childVMetrics[i] = line.get( i ).child.getRequiredVMetrics();
			}
			
			// @baseline is the position of the baseline relative to the baseline at the start. A positive value indices that it has been moved down
			double ascent = 0.0, descent = 0.0, baseline = 0.0, descentAndVSpacing = 0.0;
			boolean bBaselineIsOffset = false;
			
			for (int i = 0; i < childVMetrics.length; i++)
			{
				VMetrics chm = childVMetrics[i];
				
				// Get the ascent and descent for the child
				double childAscent, childDescent;
				if ( chm instanceof VMetricsTypeset )
				{
					VMetricsTypeset chmt = (VMetricsTypeset)chm;
					childAscent = chmt.ascent;
					childDescent = chmt.descent;
				}
				else
				{
					double childHeight = chm.height;
					childAscent = childHeight * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
					childDescent = childHeight * 0.5  +  NON_TYPESET_CHILD_BASELINE_OFFSET;
				}
				
				// Apply any active offset
				if ( bBaselineIsOffset )
				{
					// Offsetting the baseline downards (+ve value of @baseline) reduces the ascent above the origin baseline
					childAscent -= baseline;
					childDescent += baseline;
				}
				
				// Accumulate the ascent and descent
				ascent = Math.max( ascent, childAscent );
				descent = Math.max( descent, childDescent );
				
				// Accumulate the baseline offset of the child, if it has one
				if ( chm instanceof VMetricsTypesetWithBaselineOffset )
				{
					VMetricsTypesetWithBaselineOffset chmtb = (VMetricsTypesetWithBaselineOffset)chm;
					bBaselineIsOffset = true;
					baseline += chmtb.baselineOffset;
				}

				// Handle the v-spacing
				double childDescentAndVSpacing = childDescent + chm.vspacing;
				descentAndVSpacing = Math.max( descentAndVSpacing, childDescentAndVSpacing );
			}
			
			if ( bBaselineIsOffset )
			{
				return new VMetricsTypesetWithBaselineOffset( ascent, descent, baseline, descentAndVSpacing - descent );
			}
			else
			{
				return new VMetricsTypeset( ascent, descent, descentAndVSpacing - descent );
			}
		}
	}
	
	
	private VMetrics lineMetricsToFlowVMetrics(double height, VMetrics[] lineMetrics)
	{
		VMetrics topMetrics = lineMetrics[0], bottomMetrics = lineMetrics[lineMetrics.length-1];

		// The vertical spacing to go below @this is the vspacing of the bottom child
		double vspacing = bottomMetrics.vspacing;
		
		// Need the metrics for the top and bottom entries
		VMetricsTypeset topTSMetrics = null, bottomTSMetrics = null;
		
		if ( topMetrics  instanceof VMetricsTypeset )
		{
			topTSMetrics = (VMetricsTypeset)topMetrics;
		}

		if ( bottomMetrics  instanceof VMetricsTypeset )
		{
			bottomTSMetrics = (VMetricsTypeset)bottomMetrics;
		}

		double topAscent, bottomDescent;

		if ( topTSMetrics != null )
		{
			topAscent = topTSMetrics.ascent;
		}
		else
		{
			topAscent = topMetrics.height;
		}

		if ( bottomTSMetrics != null )
		{
			bottomDescent = bottomTSMetrics.descent;
		}
		else
		{
			bottomDescent = 0.0;
		}
		
		return new VMetricsTypesetWithBaselineOffset( topAscent, height - topAscent, height - topAscent - bottomDescent, vspacing );
	}


	protected VMetrics computeRequiredVMetrics()
	{
		if ( lines.isEmpty() )
		{
			return new VMetrics();
		}
		else
		{
			// Get the vmetrics for the children
			lineVMetrics = new VMetrics[lines.size()];
			for (int i = 0; i < lineVMetrics.length; i++)
			{
				lineVMetrics[i] = computeRequiredVMetricsForLine( lines.get( i ) );
			}
			
			// Accumulate the height required for all the children
			double height = 0.0, y = 0.0;
			for (int i = 0; i < lineVMetrics.length; i++)
			{
				VMetrics chm = lineVMetrics[i];
				
				height = y + chm.height;
				y = height + chm.vspacing;
			}
			
			return lineMetricsToFlowVMetrics( height, lineVMetrics );
		}
	}

	private VMetrics allocateLineY(Vector<FlowChildEntry> line, VMetrics vm, double localPosY, double localHeight)
	{
		if ( !line.isEmpty() )
		{
			VMetricsTypeset lineMetrics = (VMetricsTypeset)vm;
			
			// Start at the baseline of the line. This will change if baseline offsets are encountered
			double y = localPosY + lineMetrics.ascent;
	
			for (int i = 0; i < line.size(); i++)
			{
				FlowChildEntry entry = line.get( i );
				VMetrics chm = entry.child.vmetrics;
				
				// Get the ascent and descent for the line
				double childAscent, childDescent;
				if ( chm instanceof VMetricsTypeset )
				{
					VMetricsTypeset chmt = (VMetricsTypeset)chm;
					childAscent = chmt.ascent;
					childDescent = chmt.descent;
				}
				else
				{
					double childHeight = chm.height;
					childAscent = childHeight * 0.5  -  NON_TYPESET_CHILD_BASELINE_OFFSET;
					childDescent = childHeight * 0.5  +  NON_TYPESET_CHILD_BASELINE_OFFSET;
				}
	
				// Allocate the line
				allocateChildY( entry.child, y - childAscent, childAscent + childDescent );
				
				// Accumulate the baseline offset of the child, if it has one
				if ( chm instanceof VMetricsTypesetWithBaselineOffset )
				{
					VMetricsTypesetWithBaselineOffset chmtb = (VMetricsTypesetWithBaselineOffset)chm;
					y += chmtb.baselineOffset;
				}
			}
		}
		
		return vm;
	}
	
	
	protected VMetrics onAllocateY(double allocation)
	{
		if ( lines.size() == 0 )
		{
			return new VMetrics();
		}
		else
		{
			double y = 0.0, height = 0.0;
			VMetrics[] lineMetrics = new VMetrics[lines.size()];
			
			for (int i = 0; i < lines.size(); i++)
			{
				Vector<FlowChildEntry> line = lines.get( i );
				VMetrics vm = lineVMetrics[i];
				
				VMetrics childAllocatedMetrics = allocateLineY( line, vm, y, vm.height  );
				height = y + childAllocatedMetrics.height;
				y = height + childAllocatedMetrics.vspacing;
				
				lineMetrics[i] = childAllocatedMetrics;
			}

			return lineMetricsToFlowVMetrics( height, lineMetrics );
		}
	}
}
