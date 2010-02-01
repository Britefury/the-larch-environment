//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class VerticalLayout
{
	private static void applyTypesettingToRequisitionY(LReqBox box, LReqBoxInterface children[], VTypesetting typesetting, double height, double vspacing)
	{
		if ( typesetting == VTypesetting.NONE )
		{
			box.setRequisitionY( height, vspacing );
		}
		else if ( typesetting == VTypesetting.ALIGN_WITH_TOP )
		{
			LReqBoxInterface top = children[0];
			if ( top.hasBaseline() )
			{
				box.setRequisitionY( top.getReqAscent(), height - top.getReqAscent(), vspacing );
			}
			else
			{
				box.setRequisitionY( top.getReqHeight(), height - top.getReqHeight(), vspacing );
			}
		}
		else if ( typesetting == VTypesetting.ALIGN_WITH_BOTTOM )
		{
			LReqBoxInterface bottom = children[children.length-1];
			if ( bottom.hasBaseline() )
			{
				box.setRequisitionY( height - bottom.getReqDescent(), bottom.getReqDescent(), vspacing );
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


	public static void computeRequisitionX(LReqBox box, LReqBoxInterface children[])
	{
		// The resulting box should have the following properties:
		// - maximum width of all children
		// - sum of width and h-spacing should be the max of that of all children

		
		box.clearRequisitionX();
		
		if ( children.length == 1 )
		{
			box.setRequisitionX( children[0] );
		}
		else if ( children.length > 1 )
		{
			double minWidth = 0.0, minHAdvance = 0.0;
			double prefWidth = 0.0, prefHAdvance = 0.0;
			for (LReqBoxInterface child: children)
			{
				minWidth = Math.max( minWidth, child.getMinWidth() );
				prefWidth = Math.max( prefWidth, child.getPrefWidth() );
				minHAdvance = Math.max( minHAdvance, child.getMinHAdvance() );
				prefHAdvance = Math.max( prefHAdvance, child.getPrefHAdvance() );
			}

			box.setRequisitionX( minWidth, prefWidth, minHAdvance, prefHAdvance );
		}
	}

	public static void computeRequisitionY(LReqBox box, LReqBoxInterface children[], VTypesetting typesetting, double spacing)
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
				LReqBoxInterface chBox = children[i];
				
				reqHeight = reqY + chBox.getReqHeight() ;
				reqAdvance = reqHeight + chBox.getReqVSpacing();
				reqY = reqAdvance + spacing;
			}
			
			applyTypesettingToRequisitionY( box, children, typesetting, reqHeight, reqAdvance - reqHeight );
		}
	}




	public static void allocateX(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[])
	{
		for (int i = 0; i < children.length; i++)
		{
			allocBox.allocateChildXAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, allocBox.getAllocationX() );
		}
	}

	
	
	
	public static void allocateSpaceY(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[])
	{
		int numExpand = 0;
		
		// Count the number of children that should expand to use additional space
		if ( children.length > 0 )
		{
			for (int flags: childAllocationFlags)
			{
				VAlignment alignment = ElementAlignment.getVAlignment( flags );
				if ( alignment == VAlignment.EXPAND  ||  alignment == VAlignment.BASELINES_EXPAND )
				{
					numExpand++;
				}
			}
		}

		if ( allocBox.getAllocationY() <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||  numExpand == 0 )			// if allocation <= required   or   numExpand == 0
		{
			// Allocate children their preferred width
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				allocBox.allocateChildHeightAsRequisition( childAlloc, child );
			}
		}
		else
		{
			// Allocate children their preferred size, plus any extra to those for which the expand flag is set
			double totalExpand = allocBox.getAllocationY() - box.getReqHeight();
			double expandPerChild = totalExpand / (double)numExpand;
			
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				VAlignment alignment = ElementAlignment.getVAlignment( childAllocationFlags[i] );
				if ( alignment == VAlignment.EXPAND  ||  alignment == VAlignment.BASELINES_EXPAND )
				{
					allocBox.allocateChildHeightPaddedRequisition( childAlloc, child, child.getReqHeight() + expandPerChild );
				}
				else
				{
					allocBox.allocateChildHeightAsRequisition( childAlloc, child );
				}
			}
		}
	}
	
	public static void allocateY(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[], double spacing)
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		allocateSpaceY( box, children, allocBox, childrenAlloc, childAllocationFlags );
		
		double size = 0.0;
		double pos = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			LReqBoxInterface child = children[i];
			LAllocBoxInterface childAlloc = childrenAlloc[i];

			// Allocate child position
			allocBox.allocateChildPositionY( childAlloc, pos );

			// Accumulate width and x
			size = pos + childAlloc.getAllocationY();
			pos = size + child.getReqVSpacing() + spacing;
		}
	}





	public static void allocateSpaceY(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], boolean bExpand)
	{
		// Compute the amount of space required
		double reqSizeTotal = 0.0;
		if ( children.length > 0 )
		{
			for (LReqBoxInterface child: children)
			{
				reqSizeTotal += child.getReqHeight();
			}
		}

		if ( allocBox.getAllocationY() >= box.getReqHeight() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= required
		{
			if ( allocBox.getAllocationY() <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred height
				for (int i = 0; i < children.length; i++)
				{
					LReqBoxInterface child = children[i];
					LAllocBoxInterface childAlloc = childrenAlloc[i];
					allocBox.allocateChildHeightAsRequisition( childAlloc, child );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.getAllocationY() - box.getReqHeight();
				double expandPerChild = bExpand  ?  totalExpand / (double)children.length  :  0.0;
				
				for (int i = 0; i < children.length; i++)
				{
					LReqBoxInterface child = children[i];
					LAllocBoxInterface childAlloc = childrenAlloc[i];
					allocBox.allocateChildHeightPaddedRequisition( childAlloc, child, child.getReqHeight() + expandPerChild );
				}
			}
		}
		else			// if allocation < required
		{
			// Allocation is smaller than required size
			
			// Allocate children their required size
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				allocBox.allocateChildHeightAsRequisition( childAlloc, child );
			}
		}
	}
	
	public static void allocateY(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], double spacing, boolean bExpand)
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
			LReqBoxInterface child = children[i];
			LAllocBoxInterface childAlloc = childrenAlloc[i];

			// Compute the spacing; padding consumes child spacing
			double childSpacing = Math.max( child.getReqVSpacing(), 0.0 );

			// Allocate child position
			allocBox.allocateChildPositionY( childAlloc, pos );

			// Accumulate width and x
			size = pos + childAlloc.getAllocationY();
			pos = size + childSpacing + spacing;
		}
	}
}
