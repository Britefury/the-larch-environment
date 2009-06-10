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
		double minAdvance = 0.0, prefAdvance = 0.0;
		double minX = 0.0, prefX = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox child = children[i];
			
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			// Filter out any h-spacing that is within the amount of padding
			double minChildSpacing = Math.max( child.minHSpacing - padding, 0.0 );
			double prefChildSpacing = Math.max( child.prefHSpacing - padding, 0.0 );
			
			minWidth = minX + child.minWidth  +  padding * 2.0;
			prefWidth = prefX + child.prefWidth  +  padding * 2.0;
			minAdvance = minWidth + minChildSpacing;
			prefAdvance = prefWidth + prefChildSpacing;
			minX = minAdvance + spacing;
			prefX = prefAdvance + spacing;
		}
		
		box.setRequisitionX( minWidth, prefWidth, minAdvance - minWidth, prefAdvance - prefWidth );
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
			double reqAscent = 0.0, reqDescent = 0.0, reqDescentAndSpacing = 0.0, reqHeight = 0.0, reqAdvance = 0.0;
			int baselineCount = 0;
			for (TSBox child: children)
			{
				if ( child.hasBaseline() )
				{
					double childReqDescentAndSpacing = child.reqDescent + child.reqVSpacing;
					reqAscent = Math.max( reqAscent, child.reqAscent );
					reqDescent = Math.max( reqDescent, child.reqDescent );
					reqDescentAndSpacing = Math.max( reqDescentAndSpacing, childReqDescentAndSpacing );
					baselineCount++;
				}
				else
				{
					double childReqHeight = child.getReqHeight();
					double childReqAdvance = childReqHeight + child.reqVSpacing;
					reqHeight = Math.max( reqHeight, childReqHeight );
					reqAdvance = Math.max( reqAdvance, childReqAdvance );

					double childReqHalfHeight = childReqHeight * 0.5;
					reqAscent = Math.max( reqAscent, childReqHalfHeight );
					reqDescent = Math.max( reqDescent, childReqHalfHeight );
					reqDescentAndSpacing = Math.max( reqDescentAndSpacing, childReqHalfHeight + child.reqVSpacing );
				}
			}
			
			if ( baselineCount == 0 )
			{
				// No children had baselines; result in a box that has no baseline
				box.setRequisitionY( reqHeight, reqAdvance - reqHeight );
			}
			else
			{
				box.setRequisitionY( reqAscent, reqDescent, reqDescentAndSpacing - reqDescent );
			}
		}
		else
		{
			double reqHeight = 0.0;
			double reqAdvance = 0.0;
			for (TSBox child: children)
			{
				double childMinHeight = child.getReqHeight();
				double childMinAdvance = childMinHeight + child.reqVSpacing;
				reqHeight = Math.max( reqHeight, childMinHeight );
				reqAdvance = Math.max( reqAdvance, childMinAdvance );
			}
			
			box.setRequisitionY( reqHeight, reqAdvance - reqHeight );
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

			// Offset the child position using padding
			double childX = pos + padding;
			
			// Allocate child position
			box.allocateChildPositionX( child, childX );

			// Accumulate width and x
			size = pos + child.allocationX + padding * 2.0;
			pos = size + childSpacing + spacing;
		}
	}


	
	
	public static void allocateY(TSBox box, TSBox children[], VAlignment alignment)
	{
		if ( alignment == VAlignment.BASELINES  &&  box.bHasBaseline )
		{
			// Compute the amount of space allocated (do not allow to fall below minimum requirement)
			double allocation = Math.max( box.allocationY, box.getReqHeight() );
			
			// Compute the difference (clamped to >0) between the allocation and the preferred height 
			double delta = Math.max( allocation - box.getReqHeight(), 0.0 );
			
			// Compute the baseline position (distribute the 'delta' around the contents)
			double baselineY = box.getReqAscent() + delta * 0.5; 
			
			for (TSBox child: children)
			{
				double childAscent, childDescent;
				
				if ( child.bHasBaseline )
				{
					childAscent = child.getReqAscent();
					childDescent = child.getReqDescent();
				}
				else
				{
					double halfHeight = child.getReqHeight() * 0.5;
					childAscent = halfHeight;
					childDescent = halfHeight;
				}
				
				box.allocateChildY( child, baselineY - childAscent, childAscent + childDescent );
			}
		}
		else
		{
			double allocation = Math.max( box.allocationY, box.getReqHeight() );
			for (TSBox child: children)
			{
				if ( alignment == VAlignment.EXPAND )
				{
					box.allocateChildY( child, 0.0, allocation );
				}
				else
				{
					double childHeight = Math.min( allocation, child.getReqHeight() );
					
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
