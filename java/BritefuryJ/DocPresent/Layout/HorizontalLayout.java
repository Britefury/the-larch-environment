//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class HorizontalLayout
{
	public static void computeRequisitionX(LReqBox box, LReqBox children[], double spacing)
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
			
			minWidth = minX + child.minWidth;
			prefWidth = prefX + child.prefWidth;
			minAdvance = minWidth + child.minHSpacing;
			prefAdvance = prefWidth + child.prefHSpacing;
			minX = minAdvance + spacing;
			prefX = prefAdvance + spacing;
		}
		
		box.setRequisitionX( minWidth, prefWidth, minAdvance - minWidth, prefAdvance - prefWidth );
	}

	
	
	public static void computeRequisitionY(LReqBox box, LReqBox children[], int childAllocationFlags[])
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
		
		double reqAscent = 0.0, reqDescent = 0.0, reqDescentAndSpacing = 0.0, reqHeight = 0.0, reqAdvance = 0.0;
		boolean bHasBaseline = false;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			VAlignment v = ElementAlignment.getVAlignment( childAllocationFlags[i] );
			
			boolean bBaseline = v == VAlignment.BASELINES  ||  v == VAlignment.BASELINES_EXPAND;
			if ( bBaseline )
			{
				bHasBaseline = true;
			}
			
			if ( bBaseline  &&  child.hasBaseline() )
			{
				double childReqDescentAndSpacing = child.reqDescent + child.reqVSpacing;
				reqAscent = Math.max( reqAscent, child.reqAscent );
				reqDescent = Math.max( reqDescent, child.reqDescent );
				reqDescentAndSpacing = Math.max( reqDescentAndSpacing, childReqDescentAndSpacing );
			}
			else
			{
				double childReqHeight = child.getReqHeight();
				double childReqAdvance = childReqHeight + child.reqVSpacing;
				reqHeight = Math.max( reqHeight, childReqHeight );
				reqAdvance = Math.max( reqAdvance, childReqAdvance );
			}
		}
		
		if ( bHasBaseline )
		{
			double advance = Math.max( reqAdvance, reqAscent + reqDescentAndSpacing );
			if ( reqHeight  >  ( reqAscent + reqDescent ) )
			{
				double deltaY = ( reqHeight  -  ( reqAscent + reqDescent ) )  *  0.5;
				box.setRequisitionY( reqAscent + deltaY, reqDescent + deltaY, advance - reqHeight );
			}
			else
			{
				box.setRequisitionY( reqAscent, reqDescent, advance - ( reqAscent + reqDescent ) );
			}
		}
		else
		{
			// No children had baseline alignment; result in a box that has no baseline
			box.setRequisitionY( reqHeight, reqAdvance - reqHeight );
		}
	}




	public static void allocateSpaceX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], int childAllocationFlags[])
	{
		int numExpand = 0;
		
		// Compute the amount of space required, and count the number of children that should expand to use additional space
		double minSizeTotal = 0.0, prefSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (int i = 0; i < children.length; i++)
			{
				LReqBox child = children[i];
				HAlignment h = ElementAlignment.getHAlignment( childAllocationFlags[i] );
				minSizeTotal += child.getMinWidth();
				prefSizeTotal += child.getPrefWidth();
				if ( h == HAlignment.EXPAND )
				{
					numExpand++;
				}
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
					allocBox.allocateChildWidth( childrenAlloc[i], children[i].getPrefWidth() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationX - box.getPrefWidth();
				double expandPerChild = totalExpand / (double)numExpand;
				
				for (int i = 0; i < children.length; i++)
				{
					HAlignment h = ElementAlignment.getHAlignment( childAllocationFlags[i] );
					if ( h == HAlignment.EXPAND )
					{
						allocBox.allocateChildWidth( childrenAlloc[i], children[i].getPrefWidth() + expandPerChild );
					}
					else
					{
						allocBox.allocateChildWidth( childrenAlloc[i], children[i].getPrefWidth() );
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
				allocBox.allocateChildWidth( childrenAlloc[i], children[i].getMinWidth() );
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
					allocBox.allocateChildWidth( childrenAlloc[i], children[i].getMinWidth() + delta * fraction );
				}
			}
		}
	}
	
	

	public static void allocateX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], int childAllocationFlags[], double spacing)
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceX( box, children, allocBox, childrenAlloc, childAllocationFlags );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];

			// Compute the spacing
			// Use 'preferred' spacing, if the child was allocated its preferred amount of space, or more
			double childSpacing = ( childAlloc.allocationX >= child.prefWidth * LReqBox.ONE_MINUS_EPSILON )  ?  child.prefHSpacing  :  child.minHSpacing;

			// Allocate child position
			allocBox.allocateChildPositionX( childAlloc, pos );

			// Accumulate width and x
			size = pos + childAlloc.allocationX;
			pos = size + childSpacing + spacing;
		}
	}


	
	public static LAllocV computeVerticalAllocationForRow(LReqBox box, LAllocBox allocBox)
	{
		// Compute the amount of space allocated (do not allow to fall below minimum requirement)
		double allocationHeight = Math.max( allocBox.getAllocationY(), box.getReqHeight() );
		
		double ascent = 0.0, descent = 0.0;
		
		if ( allocBox.hasBaseline() )
		{
			ascent = Math.max( allocBox.getAllocationAscent(), box.getReqAscent() );
			descent = Math.max( allocBox.getAllocationDescent(), box.getReqDescent() );
			return new LAllocV( ascent, descent );
		}
		else
		{
			// Compute the difference (clamped to >0) between the allocation and the preferred height 
			double delta = Math.max( allocationHeight - box.getReqHeight(), 0.0 );
		
			// Compute the default ascent and descent (distribute the 'delta' around the contents)
			if ( box.hasBaseline() )
			{
				ascent = box.getReqAscent() + delta * 0.5;
				descent = box.getReqDescent() + delta * 0.5;
				return new LAllocV( ascent, descent );
			}
			else
			{
				return new LAllocV( allocationHeight );
			}
		}
	}
	
	public static void allocateY(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], int childAllocationFlags[])
	{
		LAllocV h = computeVerticalAllocationForRow( box, allocBox );
		
		for (int i = 0; i < children.length; i++)
		{
			allocBox.allocateChildYAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, h );
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
					allocBox.allocateChildWidth( childrenAlloc[i], children[i].getPrefWidth() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationX - box.getPrefWidth();
				double expandPerChild = bExpand  ?  totalExpand / (double)children.length  :  0.0;
				
				for (int i = 0; i < children.length; i++)
				{
					allocBox.allocateChildWidth( childrenAlloc[i], children[i].getPrefWidth() + expandPerChild );
				}
			}
		}
		else if ( allocBox.allocationX <= box.getMinWidth() * LReqBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their preferred size
			for (int i = 0; i < children.length; i++)
			{
				allocBox.allocateChildWidth( childrenAlloc[i], children[i].getMinWidth() );
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
					allocBox.allocateChildWidth( childrenAlloc[i], child.getMinWidth() + delta * fraction );
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
