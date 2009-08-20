//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class VerticalLayout
{
	private static void applyTypesettingToRequisitionY(LReqBox box, LReqBox children[], VTypesetting typesetting, double height, double vspacing)
	{
		if ( typesetting == VTypesetting.NONE )
		{
			box.setRequisitionY( height, vspacing );
		}
		else if ( typesetting == VTypesetting.ALIGN_WITH_TOP )
		{
			LReqBox top = children[0];
			if ( top.bHasBaseline )
			{
				box.setRequisitionY( top.reqAscent, height - top.reqAscent, vspacing );
			}
			else
			{
				box.setRequisitionY( top.getReqHeight(), height - top.getReqHeight(), vspacing );
			}
		}
		else if ( typesetting == VTypesetting.ALIGN_WITH_BOTTOM )
		{
			LReqBox bottom = children[children.length-1];
			if ( bottom.bHasBaseline )
			{
				box.setRequisitionY( height - bottom.reqDescent, bottom.reqDescent, vspacing );
			}
			else
			{
				box.setRequisitionY( height, 0.0, vspacing );
			}
		}
		else
		{
			throw new RuntimeException( "Invalid typesetting value" );
		}
	}


	public static void computeRequisitionX(LReqBox box, LReqBox children[])
	{
		// The resulting box should have the following properties:
		// - maximum width of all children
		// - sum of width and h-spacing should be the max of that of all children

		
		box.clearRequisitionX();
		
		double minWidth = 0.0, minAdvance = 0.0;
		double prefWidth = 0.0, prefAdvance = 0.0;
		for (LReqBox child: children)
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

	public static void computeRequisitionY(LReqBox box, LReqBox children[], VTypesetting typesetting, double spacing, BoxPackingParams packingParams[])
	{
		if ( children.length == 0 )
		{
			box.setRequisitionY( 0.0, 0.0 );
		}
		else
		{
			// Accumulate the height required for all the children
			
			// Each packed child consists of:
			//	- start padding
			//	- child height
			//	- end padding
			//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
			
			// There should be at least the specified amount of spacing between each child, or the child's own v-spacing if it is greater
			
			double reqHeight = 0.0;
			double reqAdvance = 0.0;
			double reqY = 0.0;
			for (int i = 0; i < children.length; i++)
			{
				LReqBox chBox = children[i];
				
				BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
				double padding = params != null  ?  params.padding  :  0.0;
				
				double reqChildSpacing = Math.max( chBox.reqVSpacing - padding, 0.0 );
				
				reqHeight = reqY + chBox.getReqHeight()  +  padding * 2.0;
				reqAdvance = reqHeight + reqChildSpacing;
				reqY = reqAdvance + spacing;
			}
			
			applyTypesettingToRequisitionY( box, children, typesetting, reqHeight, reqAdvance - reqHeight );
		}
	}




	public static void allocateX(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], HAlignment alignment)
	{
		double allocation = allocBox.allocationX;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];
			
			if ( alignment == HAlignment.EXPAND )
			{
				allocBox.allocateChildX( childAlloc, 0.0, allocation );
			}
			else
			{
				double childWidth = Math.min( allocation, child.prefWidth );
				
				if ( alignment == HAlignment.LEFT )
				{
					allocBox.allocateChildX( childAlloc, 0.0, childWidth );
				}
				else if ( alignment == HAlignment.CENTRE )
				{
					allocBox.allocateChildX( childAlloc, ( allocation - childWidth )  *  0.5, childWidth );
				}
				else if ( alignment == HAlignment.RIGHT )
				{
					allocBox.allocateChildX( childAlloc, allocation - childWidth, childWidth );
				}
				else
				{
					throw new RuntimeException( "Invalid h-alignment" );
				}
			}
		}
	}

	
	
	
	public static void allocateSpaceY(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], BoxPackingParams packingParams[])
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
		double reqSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (LReqBox child: children)
			{
				reqSizeTotal += child.getReqHeight();
			}
		}

		if ( allocBox.allocationY >= box.getReqHeight() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= required
		{
			if ( allocBox.allocationY <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||  numExpand == 0 )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred width
				for (int i = 0; i < children.length; i++)
				{
					LReqBox child = children[i];
					LAllocBox childAlloc = childrenAlloc[i];
					allocBox.allocateChildSpaceY( childAlloc, child.getReqHeight() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationY - box.getReqHeight();
				double expandPerChild = totalExpand / (double)numExpand;
				
				for (int i = 0; i < children.length; i++)
				{
					LReqBox child = children[i];
					LAllocBox childAlloc = childrenAlloc[i];
					BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
					if ( params != null  &&  LReqBox.testPackFlagExpand( params.packFlags ) )
					{
						allocBox.allocateChildSpaceY( childAlloc, child.getReqHeight() + expandPerChild );
					}
					else
					{
						allocBox.allocateChildSpaceY( childAlloc, child.getReqHeight() );
					}
				}
			}
		}
		else			// if allocation < required
		{
			// Allocation is smaller than required size
			
			// Allocate children their required size
			for (int i = 0; i < children.length; i++)
			{
				LReqBox child = children[i];
				LAllocBox childAlloc = childrenAlloc[i];
				allocBox.allocateChildSpaceY( childAlloc, child.getReqHeight() );
			}
		}
	}
	
	public static void allocateY(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], double spacing, BoxPackingParams packingParams[])
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceY( box, children, allocBox, childrenAlloc, packingParams );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];

			// Get the padding
			BoxPackingParams params = packingParams != null  ?  packingParams[i]  :  null;
			double padding = params != null  ?  params.padding  :  0.0;
			
			// Compute the spacing; padding consumes child spacing
			double childSpacing = Math.max( child.reqVSpacing - padding, 0.0 );

			// Offset the child position using padding
			double childY = pos + padding;
			
			// Allocate child position
			allocBox.allocateChildPositionY( childAlloc, childY );

			// Accumulate width and x
			size = pos + childAlloc.allocationY + padding * 2.0;
			pos = size + childSpacing + spacing;
		}
	}





	public static void allocateSpaceY(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], boolean bExpand)
	{
		// Compute the amount of space required
		double reqSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (LReqBox child: children)
			{
				reqSizeTotal += child.getReqHeight();
			}
		}

		if ( allocBox.allocationY >= box.getReqHeight() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= required
		{
			if ( allocBox.allocationY <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred width
				for (int i = 0; i < children.length; i++)
				{
					LReqBox child = children[i];
					LAllocBox childAlloc = childrenAlloc[i];
					allocBox.allocateChildSpaceY( childAlloc, child.getReqHeight() );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationY - box.getReqHeight();
				double expandPerChild = bExpand  ?  totalExpand / (double)children.length  :  0.0;
				
				for (int i = 0; i < children.length; i++)
				{
					LReqBox child = children[i];
					LAllocBox childAlloc = childrenAlloc[i];
					allocBox.allocateChildSpaceY( childAlloc, child.getReqHeight() + expandPerChild );
				}
			}
		}
		else			// if allocation < required
		{
			// Allocation is smaller than required size
			
			// Allocate children their required size
			for (int i = 0; i < children.length; i++)
			{
				LReqBox child = children[i];
				LAllocBox childAlloc = childrenAlloc[i];
				allocBox.allocateChildSpaceY( childAlloc, child.getReqHeight() );
			}
		}
	}
	
	public static void allocateY(LReqBox box, LReqBox children[], LAllocBox allocBox, LAllocBox childrenAlloc[], double spacing, boolean bExpand)
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceY( box, children, allocBox, childrenAlloc, bExpand );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBox child = children[i];
			LAllocBox childAlloc = childrenAlloc[i];

			// Compute the spacing; padding consumes child spacing
			double childSpacing = Math.max( child.reqVSpacing, 0.0 );

			// Allocate child position
			allocBox.allocateChildPositionY( childAlloc, pos );

			// Accumulate width and x
			size = pos + childAlloc.allocationY;
			pos = size + childSpacing + spacing;
		}
	}
}
