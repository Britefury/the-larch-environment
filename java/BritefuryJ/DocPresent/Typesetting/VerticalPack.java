//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;

public class VerticalPack
{
	public static void computeRequisitionX(TSBox box, TSBox children[])
	{
		// The resulting box should have the following properties:
		// - maximum width of all children
		// - sum of width and h-spacing should be the max of that of all children

		
		box.clearRequisitionX();
		
		double minWidth = 0.0, minAdvance = 0.0;
		double prefWidth = 0.0, prefAdvance = 0.0;
		for (TSBox child: children)
		{
			double childMinAdvance = child.minWidth + child.minHSpacing;
			double childPrefAdvance = child.prefWidth + child.prefHSpacing;
			minWidth = Math.max( minWidth, child.minWidth );
			prefWidth = Math.max( prefWidth, child.prefWidth );
			minAdvance = Math.max( minAdvance, childMinAdvance );
			prefAdvance = Math.max( prefAdvance, childPrefAdvance );
		}
		
		box.setRequisitionX( minWidth, prefWidth, minAdvance - minWidth, prefAdvance - prefWidth );
	}

	public static void computeRequisitionY(TSBox box, TSBox children[], double spacing, double childPadding[])
	{
		// Accumulate the width required for all the children
		
		// Each packed child consists of:
		//	- start padding
		//	- child height
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater
		
		double minHeight = 0.0, prefHeight = 0.0;
		double minY = 0.0, prefY = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox chBox = children[i];
			
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			double minChildSpacing = Math.max( chBox.minVSpacing - padding, 0.0 );
			double prefChildSpacing = Math.max( chBox.prefVSpacing - padding, 0.0 );
			double interCellSpacing = ( i < children.length - 1 )  ?  spacing  :  0.0;  
			
			minHeight = minY + chBox.getMinHeight()  +  padding * 2.0;
			prefHeight = prefY + chBox.getPrefHeight()  +  padding * 2.0;
			minY = minHeight + minChildSpacing + interCellSpacing;
			prefY = prefHeight + prefChildSpacing + interCellSpacing;
		}
		
		box.setRequisitionY( minHeight, prefHeight, minY - minHeight, prefY - prefHeight );
	}




	public static void allocateX(TSBox box, TSBox children[], HAlignment alignment)
	{
		double allocation = Math.max( box.allocationX, box.minWidth );
		for (TSBox child: children)
		{
			if ( alignment == HAlignment.EXPAND )
			{
				box.allocateChildX( child, 0.0, allocation );
			}
			else
			{
				double childWidth = Math.min( allocation, child.prefWidth );
				
				if ( alignment == HAlignment.LEFT )
				{
					box.allocateChildX( child, 0.0, childWidth );
				}
				else if ( alignment == HAlignment.CENTRE )
				{
					box.allocateChildX( child, ( allocation - childWidth )  *  0.5, childWidth );
				}
				else if ( alignment == HAlignment.RIGHT )
				{
					box.allocateChildX( child, allocation - childWidth, childWidth );
				}
				else
				{
					throw new RuntimeException( "Invalid h-alignment" );
				}
			}
		}
	}

	
	
	
	public static void allocateSpaceY(TSBox box, TSBox children[], int packFlags[])
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
	
	public static void allocateY(TSBox box, TSBox children[], double spacing, double childPadding[], int packFlags[])
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceY( box, children, packFlags );
		
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
