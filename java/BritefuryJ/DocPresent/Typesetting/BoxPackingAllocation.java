//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;

public class BoxPackingAllocation
{
	public static void allocateSpaceHorizontalPackingX(TSBox box, TSBox children[], int packFlags[])
	{
		int numExpand = 0;
		
		// Count the number of children that should expand to use additional space
		if ( packFlags != null )
		{
			assert packFlags.length == children.length;
			for (int i = 0; i < packFlags.length; i++)
			{
				int f = packFlags[i];
				if ( TSBox.testPackFlagExpand( f ) )
				{
					numExpand++;
				}
			}
		}
		
		
		// Compute the amount of space required
		double minSizeTotal = 0.0, prefSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (TSBox child: children)
			{
				minSizeTotal += child.getMinWidth();
				prefSizeTotal += child.getPrefWidth();
			}
		}
		double minSpacingTotal = box.getMinWidth() - minSizeTotal; 

		if ( box.allocationX >= box.getPrefWidth() * TSBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( box.allocationX <= box.getPrefWidth() * TSBox.ONE_PLUS_EPSILON  ||  numExpand == 0 )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred width
				for (TSBox child: children)
				{
					box.allocateChildSpaceX( child, child.getPrefWidth() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = box.allocationX - box.getPrefWidth();
				double expandPerChild = totalExpand / (double)numExpand;
				
				int i = 0;
				for (TSBox child: children)
				{
					if ( packFlags != null  &&  TSBox.testPackFlagExpand( packFlags[i] ) )
					{
						box.allocateChildSpaceX( child, child.getPrefWidth() + expandPerChild );
					}
					else
					{
						box.allocateChildSpaceX( child, child.getPrefWidth() );
					}
					i++;
				}
			}
		}
		else if ( box.allocationX <= box.getMinWidth() * TSBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their preferred size
			for (TSBox child: children)
			{
				box.allocateChildSpaceX( child, child.getMinWidth() );
			}
		}
		else
		{
			// Allocation is between minimum and preferred size
			
			// For spacing, use minimum spacing as opposed to preferred spacing
			
			// Compute the difference between the minimum and preferred sizes
			double pref = box.getPrefWidth() - minSpacingTotal;
			double deltaMinPref = pref - minSizeTotal;

			// Compute the amount of space over the minimum that is available to share
			double allocToShare = box.allocationX - minSpacingTotal - minSizeTotal;
			
			// Compute the fraction that determines the interpolation factor used to blend the minimum and preferred sizes
			double fraction = allocToShare / deltaMinPref;
			
			if ( children.length >= 1 )
			{
				for (TSBox child: children)
				{
					double delta = child.getPrefWidth() - child.getMinWidth();
					box.allocateChildSpaceX( child, child.getMinWidth() + delta * fraction );
				}
			}
		}
	}
	
	

	
	
	
	public static void allocateSpaceVerticalPackingY(TSBox box, TSBox children[], int packFlags[])
	{
		int numExpand = 0;
		
		// Count the number of children that should expand to use additional space
		if ( packFlags != null )
		{
			assert packFlags.length == children.length;
			for (int i = 0; i < packFlags.length; i++)
			{
				int f = packFlags[i];
				if ( TSBox.testPackFlagExpand( f ) )
				{
					numExpand++;
				}
			}
		}
		
		
		// Compute the amount of space required
		double minSizeTotal = 0.0, prefSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (TSBox child: children)
			{
				minSizeTotal += child.getMinHeight();
				prefSizeTotal += child.getPrefHeight();
			}
		}
		double minSpacingTotal = box.getMinHeight() - minSizeTotal; 

		if ( box.allocationY >= box.getPrefHeight() * TSBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( box.allocationY <= box.getPrefHeight() * TSBox.ONE_PLUS_EPSILON  ||  numExpand == 0 )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred width
				for (TSBox child: children)
				{
					box.allocateChildSpaceY( child, child.getPrefHeight() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = box.allocationY - box.getPrefHeight();
				double expandPerChild = totalExpand / (double)numExpand;
				
				int i = 0;
				for (TSBox child: children)
				{
					if ( packFlags != null  &&  TSBox.testPackFlagExpand( packFlags[i] ) )
					{
						box.allocateChildSpaceY( child, child.getPrefHeight() + expandPerChild );
					}
					else
					{
						box.allocateChildSpaceY( child, child.getPrefHeight() );
					}
					i++;
				}
			}
		}
		else if ( box.allocationY <= box.getMinHeight() * TSBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their preferred size
			for (TSBox child: children)
			{
				box.allocateChildSpaceY( child, child.getMinHeight() );
			}
		}
		else
		{
			// Allocation is between minimum and preferred size
			
			// For spacing, use minimum spacing as opposed to preferred spacing
			
			// Compute the difference between the minimum and preferred sizes
			double pref = box.getPrefHeight() - minSpacingTotal;
			double deltaMinPref = pref - minSizeTotal;

			// Compute the amount of space over the minimum that is available to share
			double allocToShare = box.allocationY - minSpacingTotal - minSizeTotal;
			
			// Compute the fraction that determines the interpolation factor used to blend the minimum and preferred sizes
			double fraction = allocToShare / deltaMinPref;
			
			if ( children.length >= 1 )
			{
				for (TSBox child: children)
				{
					double delta = child.getPrefHeight() - child.getMinHeight();
					box.allocateChildSpaceY( child, child.getMinHeight() + delta * fraction );
				}
			}
		}
	}
	
	

	
	
	

	
	public static void allocateHorizontalPackingX(TSBox box, TSBox children[], double spacing, double childPadding[], int packFlags[])
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceHorizontalPackingX( box, children, packFlags );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox child = children[i];

			// Get the padding
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			// Compute the spacing
			// Use 'prefferred' spacing, if the child was allocated its preferred amount of space, or more
			double childSpacing = ( child.allocationX >= child.prefWidth * TSBox.ONE_MINUS_EPSILON )  ?  child.prefHSpacing  :  child.minHSpacing;
			// padding consumes child spacing
			childSpacing = Math.max( childSpacing - padding, 0.0 );
			double interCellSpacing = ( i < children.length - 1 )  ?  spacing  :  0.0;

			// Offset the child position using padding
			double childX = pos + padding;
			
			// Allocate child position
			box.allocateChildPositionX( child, childX );

			// Accumulate width and x
			size = pos + child.allocationX + padding * 2.0;
			pos = size + childSpacing + interCellSpacing;
		}
	}



	public static void allocateVerticalPackingY(TSBox box, TSBox children[], double spacing, double childPadding[], int packFlags[])
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceVerticalPackingY( box, children, packFlags );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox child = children[i];

			// Get the padding
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			// Compute the spacing
			// Use 'prefferred' spacing, if the child was allocated its preferred amount of space, or more
			double childSpacing = ( child.allocationY >= child.getPrefHeight() * TSBox.ONE_MINUS_EPSILON )  ?  child.prefVSpacing  :  child.minVSpacing;
			// padding consumes child spacing
			childSpacing = Math.max( childSpacing - padding, 0.0 );
			double interCellSpacing = ( i < children.length - 1 )  ?  spacing  :  0.0;

			// Offset the child position using padding
			double childY = pos + padding;
			
			// Allocate child position
			box.allocateChildPositionY( child, childY );

			// Accumulate width and x
			size = pos + child.allocationY + padding * 2.0;
			pos = size + childSpacing + interCellSpacing;
		}
	}
}
