//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class HorizontalLayout
{
	public static void computeRequisitionX(LReqBox box, LReqBox children[], double spacing, BoxPackingParams packingParams[])
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
			LReqBox child = children[i];
			
			BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
			double padding = params != null  ?  params.padding  :  0.0;
			
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

	
	
	public static void computeRequisitionY(LReqBox box, LReqBox children[], VAlignment alignment)
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
			for (LReqBox child: children)
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
			for (LReqBox child: children)
			{
				double childReqHeight = child.getReqHeight();
				double childReqAdvance = childReqHeight + child.reqVSpacing;
				reqHeight = Math.max( reqHeight, childReqHeight );
				reqAdvance = Math.max( reqAdvance, childReqAdvance );
			}
			
			box.setRequisitionY( reqHeight, reqAdvance - reqHeight );
		}
	}




	public static void allocateSpaceX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], BoxPackingParams packingParams[])
	{
		int numExpand = 0;
		
		// Count the number of children that should expand to use additional space
		if ( packingParams != null )
		{
			assert packingParams.length == children.length;
			for (BoxPackingParams params: packingParams)
			{
				if ( params != null )
				{
					if ( LReqBox.testPackFlagExpand( params.packFlags ) )
					{
						numExpand++;
					}
				}
			}
		}
		
		
		// Compute the amount of space required
		double minSizeTotal = 0.0, prefSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (LReqBox child: children)
			{
				minSizeTotal += child.getMinWidth();
				prefSizeTotal += child.getPrefWidth();
			}
		}
		double minSpacingTotal = box.getMinWidth() - minSizeTotal; 

		if ( allocBox.allocationX >= box.getPrefWidth() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( allocBox.allocationX <= box.getPrefWidth() * LReqBox.ONE_PLUS_EPSILON  ||  numExpand == 0 )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred width
				for (int i = 0; i < children.length; i++)
				{
					allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getPrefWidth() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationX - box.getPrefWidth();
				double expandPerChild = totalExpand / (double)numExpand;
				
				for (int i = 0; i < children.length; i++)
				{
					BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
					if ( params != null  &&  LReqBox.testPackFlagExpand( params.packFlags ) )
					{
						allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getPrefWidth() + expandPerChild );
					}
					else
					{
						allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getPrefWidth() );
					}
				}
			}
		}
		else if ( allocBox.allocationX <= box.getMinWidth() * LReqBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their preferred size
			for (int i = 0; i < children.length; i++)
			{
				allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getMinWidth() );
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
			double allocToShare = allocBox.allocationX - minSpacingTotal - minSizeTotal;
			
			// Compute the fraction that determines the interpolation factor used to blend the minimum and preferred sizes
			double fraction = allocToShare / deltaMinPref;
			
			if ( children.length >= 1 )
			{
				for (int i = 0; i < children.length; i++)
				{
					double delta = children[i].getPrefWidth() - children[i].getMinWidth();
					allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getMinWidth() + delta * fraction );
				}
			}
		}
	}
	
	

	public static void allocateX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], double spacing, BoxPackingParams packingParams[])
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceX( box, children, allocBox, childrenAlloc, packingParams );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];

			// Get the padding
			BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
			double padding = params != null  ?  params.padding  :  0.0;
			
			// Compute the spacing
			// Use 'preferred' spacing, if the child was allocated its preferred amount of space, or more
			double childSpacing = ( childAlloc.allocationX >= child.prefWidth * LReqBox.ONE_MINUS_EPSILON )  ?  child.prefHSpacing  :  child.minHSpacing;
			// padding consumes child spacing
			childSpacing = Math.max( childSpacing - padding, 0.0 );

			// Offset the child position using padding
			double childX = pos + padding;
			
			// Allocate child position
			allocBox.allocateChildPositionX( childAlloc, childX );

			// Accumulate width and x
			size = pos + childAlloc.allocationX + padding * 2.0;
			pos = size + childSpacing + spacing;
		}
	}


	
	
	public static void allocateY(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], VAlignment alignment)
	{
		if ( alignment == VAlignment.BASELINES  &&  box.hasBaseline() )
		{
			// Compute the amount of space allocated (do not allow to fall below minimum requirement)
			double allocation = Math.max( allocBox.allocationY, box.getReqHeight() );
			
			// Compute the difference (clamped to >0) between the allocation and the preferred height 
			double delta = Math.max( allocation - box.getReqHeight(), 0.0 );
			
			// Compute the baseline position (distribute the 'delta' around the contents)
			double baselineY = box.getReqAscent() + delta * 0.5; 
			
			for (int i = 0; i < children.length; i++)
			{
				LReqBox child = children[i];
				double childAscent, childDescent;
				
				if ( child.hasBaseline() )
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
				
				allocBox.allocateChildY( childrenAlloc[i], baselineY - childAscent, childAscent + childDescent );
			}
		}
		else
		{
			double allocation = Math.max( allocBox.allocationY, box.getReqHeight() );
			for (int i = 0; i < children.length; i++)
			{
				LReqBox child = children[i];
				LAllocBox childAlloc = childrenAlloc[i];
				
				if ( alignment == VAlignment.EXPAND )
				{
					allocBox.allocateChildY( childAlloc, 0.0, allocation );
				}
				else
				{
					double childHeight = Math.min( allocation, child.getReqHeight() );
					
					if ( alignment == VAlignment.TOP )
					{
						allocBox.allocateChildY( childAlloc, 0.0, childHeight );
					}
					else if ( alignment == VAlignment.CENTRE  ||  ( alignment == VAlignment.BASELINES  &&  !box.hasBaseline() ) )
					{
						allocBox.allocateChildY( childAlloc, ( allocation - childHeight )  *  0.5, childHeight );
					}
					else if ( alignment == VAlignment.BOTTOM )
					{
						allocBox.allocateChildY( childAlloc, allocation - childHeight, childHeight );
					}
					else
					{
						throw new RuntimeException( "Invalid v-alignment" );
					}
				}
			}
		}
	}




	public static void allocateSpaceX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], boolean bExpand)
	{
		// Compute the amount of space required
		double minSizeTotal = 0.0, prefSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (LReqBox child: children)
			{
				minSizeTotal += child.getMinWidth();
				prefSizeTotal += child.getPrefWidth();
			}
		}
		double minSpacingTotal = box.getMinWidth() - minSizeTotal; 

		if ( allocBox.allocationX >= box.getPrefWidth() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( allocBox.allocationX <= box.getPrefWidth() * LReqBox.ONE_PLUS_EPSILON )			// if allocation == preferred
			{
				// Allocate children their preferred width
				for (int i = 0; i < children.length; i++)
				{
					allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getPrefWidth() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationX - box.getPrefWidth();
				double expandPerChild = bExpand  ?  totalExpand / (double)children.length  :  0.0;
				
				for (int i = 0; i < children.length; i++)
				{
					allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getPrefWidth() + expandPerChild );
				}
			}
		}
		else if ( allocBox.allocationX <= box.getMinWidth() * LReqBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their preferred size
			for (int i = 0; i < children.length; i++)
			{
				allocBox.allocateChildSpaceX( childrenAlloc[i], children[i].getMinWidth() );
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
			double allocToShare = allocBox.allocationX - minSpacingTotal - minSizeTotal;
			
			// Compute the fraction that determines the interpolation factor used to blend the minimum and preferred sizes
			double fraction = allocToShare / deltaMinPref;
			
			if ( children.length >= 1 )
			{
				for (int i = 0; i < children.length; i++)
				{
					LReqBox child = children[i];
					double delta = child.getPrefWidth() - child.getMinWidth();
					allocBox.allocateChildSpaceX( childrenAlloc[i], child.getMinWidth() + delta * fraction );
				}
			}
		}
	}
	
	

	public static void allocateX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], double spacing, boolean bExpand)
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceX( box, children, allocBox, childrenAlloc, bExpand );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];

			// Compute the spacing
			// Use 'preferred' spacing, if the child was allocated its preferred amount of space, or more
			double childSpacing = ( childAlloc.allocationX >= child.prefWidth * LReqBox.ONE_MINUS_EPSILON )  ?  child.prefHSpacing  :  child.minHSpacing;
			// padding consumes child spacing
			childSpacing = Math.max( childSpacing, 0.0 );

			// Allocate child position
			allocBox.allocateChildPositionX( childAlloc, pos );

			// Accumulate width and x
			size = pos + childAlloc.allocationX;
			pos = size + childSpacing + spacing;
		}
	}


	
	
}
