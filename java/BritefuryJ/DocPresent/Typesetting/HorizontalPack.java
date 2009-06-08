//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;

public class HorizontalPack
{
	public static void computeRequisitionX(TSBox box, TSBox children[], double spacing, double childPadding[])
	{
		// Accumulate the width required for all the children
		
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater
		
		double minWidth = 0.0, prefWidth = 0.0;
		double minX = 0.0, prefX = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox chBox = children[i];
			
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			// Filter out any h-spacing that is within the amount of padding
			double minChildSpacing = Math.max( chBox.minHSpacing - padding, 0.0 );
			double prefChildSpacing = Math.max( chBox.prefHSpacing - padding, 0.0 );
			double interCellSpacing = ( i < children.length - 1 )  ?  spacing  :  0.0;  
			
			minWidth = minX + chBox.minWidth  +  padding * 2.0;
			prefWidth = prefX + chBox.prefWidth  +  padding * 2.0;
			minX = minWidth + minChildSpacing + interCellSpacing;
			prefX = prefWidth + prefChildSpacing + interCellSpacing;
		}
		
		box.setRequisitionX( minWidth, prefWidth, minX - minWidth, prefX - prefWidth );
	}

	
	
	public static void computeRequisitionY(TSBox box, TSBox children[], VAlignment alignment)
	{
		// The resulting box should have the following properties:
		// In the case where alignment is BASELINES:
		//	- maximum ascent of all children
		//	- maximum descent of children
		//	- sum of descent and v-spacing should be the max of that of all children
		//	- for any child that does not have a base line:
		//		- assume that there is one at the centre, offset by 'x' units upwards, where 'x' is half the ascent of
		//			the children that do have baselines
		//	//
		// Else:
		//	- maximum height of all children
		//	- sum of height and v-spacing should be the max of that of all children

		
		box.clearRequisitionY();
		
		if ( alignment == VAlignment.BASELINES )
		{
			double minAscent = 0.0, minDescent = 0.0, minDescentAndSpacing = 0.0, minHeight = 0.0, minAdvance = 0.0, minHalfHeight = 0.0, minHalfHeightAndSpacing = 0.0;
			double prefAscent = 0.0, prefDescent = 0.0, prefDescentAndSpacing = 0.0, prefHeight = 0.0, prefAdvance = 0.0, prefHalfHeight = 0.0, prefHalfHeightAndSpacing = 0.0;
			int baselineCount = 0;
			for (TSBox child: children)
			{
				if ( child.hasBaseline() )
				{
					double childMinDescentAndSpacing = child.minDescent + child.minVSpacing;
					double childPrefDescentAndSpacing = child.prefDescent + child.prefVSpacing;
					minAscent = Math.max( minAscent, child.minAscent );
					prefAscent = Math.max( prefAscent, child.prefAscent );
					minDescent = Math.max( minDescent, child.minDescent );
					prefDescent = Math.max( prefDescent, child.prefDescent );
					minDescentAndSpacing = Math.max( minDescentAndSpacing, childMinDescentAndSpacing );
					prefDescentAndSpacing = Math.max( prefDescentAndSpacing, childPrefDescentAndSpacing );
					baselineCount++;
				}
				else
				{
					double childMinHeight = child.getMinHeight();
					double childPrefHeight = child.getPrefHeight();
					double childMinAdvance = childMinHeight + child.minVSpacing;
					double childPrefAdvance = childPrefHeight + child.prefVSpacing;
					minHeight = Math.max( minHeight, childMinHeight );
					prefHeight = Math.max( prefHeight, childPrefHeight );
					minAdvance = Math.max( minAdvance, childMinAdvance );
					prefAdvance = Math.max( prefAdvance, childPrefAdvance );

					double childMinHalfHeight = childMinHeight * 0.5;
					double childPrefHalfHeight = childPrefHeight * 0.5;
					double childMinHalfHeightAndSpacing = childMinHalfHeight + child.minVSpacing;
					double childPrefHalfHeightAndSpacing = childPrefHalfHeight + child.prefVSpacing;
					minHalfHeight = Math.max( minHalfHeight, childMinHalfHeight );
					prefHalfHeight = Math.max( prefHalfHeight, childPrefHalfHeight );
					minHalfHeightAndSpacing = Math.max( minHalfHeightAndSpacing, childMinHalfHeightAndSpacing );
					prefHalfHeightAndSpacing = Math.max( prefHalfHeightAndSpacing, childPrefHalfHeightAndSpacing );
				}
			}
			
			if ( baselineCount == 0 )
			{
				// No children had baselines; result in a box that has no baseline
				box.setRequisitionY( minHeight, prefHeight, minAdvance - minHeight, prefAdvance - prefHeight );
			}
			else
			{
				if ( baselineCount < children.length )
				{
					// Some children did not have baselines; combine requirements
					
					// For the children that do not have a baseline, assume that there is one that is 'x' units below their centre,
					// where x is half the size of the ascent of the typeset children
					double offset = minAscent * 0.5;
					double noBaselineMinAscent = minHalfHeight + offset, noBaselinePrefAscent = prefHalfHeight + offset;
					double noBaselineMinDescent = minHalfHeight - offset, noBaselinePrefDescent = prefHalfHeight - offset;
					double noBaselineMinDescentAndSpacing = minHalfHeightAndSpacing - offset, noBaselinePrefDescentAndSpacing = prefHalfHeightAndSpacing - offset;
					minAscent = Math.max( minAscent, noBaselineMinAscent );
					prefAscent = Math.max( prefAscent, noBaselinePrefAscent );
					minDescent = Math.max( minDescent, noBaselineMinDescent );
					prefDescent = Math.max( prefDescent, noBaselinePrefDescent );
					minDescentAndSpacing = Math.max( minDescentAndSpacing, noBaselineMinDescentAndSpacing );
					prefDescentAndSpacing = Math.max( prefDescentAndSpacing, noBaselinePrefDescentAndSpacing );
				}
				
				box.setRequisitionY( minAscent, prefAscent, minDescent, prefDescent, minDescentAndSpacing - minDescent, prefDescentAndSpacing - prefDescent );
			}
		}
		else
		{
			double minHeight = 0.0, prefHeight = 0.0;
			double minAdvance = 0.0, prefAdvance = 0.0;
			for (TSBox child: children)
			{
				double childMinHeight = child.getMinHeight();
				double childPrefHeight = child.getPrefHeight();
				double childMinAdvance = childMinHeight + child.minVSpacing;
				double childPrefAdvance = childPrefHeight + child.prefVSpacing;
				minHeight = Math.max( minHeight, childMinHeight );
				prefHeight = Math.max( prefHeight, childPrefHeight );
				minAdvance = Math.max( minAdvance, childMinAdvance );
				prefAdvance = Math.max( prefAdvance, childPrefAdvance );
			}
			
			box.setRequisitionY( minHeight, prefHeight, minAdvance - minHeight, prefAdvance - prefHeight );
		}
	}




	public static void allocateSpaceX(TSBox box, TSBox children[], int packFlags[])
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
	
	

	public static void allocateX(TSBox box, TSBox children[], double spacing, double childPadding[], int packFlags[])
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceX( box, children, packFlags );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox child = children[i];

			// Get the padding
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			// Compute the spacing
			// Use 'preferred' spacing, if the child was allocated its preferred amount of space, or more
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


	
	
	public static void allocateY(TSBox box, TSBox children[], VAlignment alignment)
	{
		if ( alignment == VAlignment.BASELINES  &&  box.bHasBaseline )
		{
			// Compute the amount of space allocated (do not allow to fall below minimum requirement)
			double allocation = Math.max( box.allocationY, box.getMinHeight() );
			
			// Compute the 'fraction' between the minimum and preferred heights
			double fraction = ( allocation - box.getMinHeight() ) / ( box.getPrefHeight() - box.getMinHeight() );
			fraction = Math.min( fraction, 1.0 );
			
			// Compute the amount of allocated ascent and descent
			double allocationAscent = box.getMinAscent()  +  ( box.getPrefAscent() - box.getMinAscent() ) * fraction;
			double allocationDescent = box.getMinDescent()  +  ( box.getPrefDescent() - box.getMinDescent() ) * fraction;
			
			// Compute the difference (clamped to >0) between the allocation and the preferred height 
			double delta = Math.max( allocation - box.getPrefHeight(), 0.0 );
			
			// Compute the baseline position (distribute the 'delta' around the contents)
			double baselineY = allocationAscent + delta * 0.5; 
			
			for (TSBox child: children)
			{
				double childAscent = Math.min( allocationAscent, child.getPrefAscent() );
				double childDescent = Math.min( allocationDescent, child.getPrefDescent() );
				
				box.allocateChildY( child, baselineY - childAscent, childAscent + childDescent );
			}
		}
		else
		{
			double allocation = Math.max( box.allocationY, box.getMinHeight() );
			for (TSBox child: children)
			{
				if ( alignment == VAlignment.EXPAND )
				{
					box.allocateChildY( child, 0.0, allocation );
				}
				else
				{
					double childHeight = Math.min( allocation, child.getPrefHeight() );
					
					if ( alignment == VAlignment.TOP )
					{
						box.allocateChildY( child, 0.0, childHeight );
					}
					else if ( alignment == VAlignment.CENTRE  ||  ( alignment == VAlignment.BASELINES  &&  !box.bHasBaseline ) )
					{
						box.allocateChildY( child, ( allocation - childHeight )  *  0.5, childHeight );
					}
					else if ( alignment == VAlignment.BOTTOM )
					{
						box.allocateChildY( child, allocation - childHeight, childHeight );
					}
					else
					{
						throw new RuntimeException( "Invalid v-alignment" );
					}
				}
			}
		}
	}
}
