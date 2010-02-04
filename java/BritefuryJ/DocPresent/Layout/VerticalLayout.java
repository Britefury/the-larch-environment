//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class VerticalLayout
{
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

	public static void computeRequisitionY(LReqBox box, LReqBoxInterface children[], int refPointIndex, double spacing)
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
			double refY = 0.0;
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface chBox = children[i];
				
				if ( i == refPointIndex )
				{
					refY = reqY + chBox.getRefY();
				}
				
				reqHeight = reqY + chBox.getReqHeight() ;
				reqAdvance = reqHeight + chBox.getReqVSpacing();
				reqY = reqAdvance + spacing;
			}
			
			box.setRequisitionY( reqHeight, reqAdvance - reqHeight, refY );
		}
	}




	public static void allocateX(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[])
	{
		for (int i = 0; i < children.length; i++)
		{
			allocBox.allocateChildXAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, allocBox.getAllocationX() );
		}
	}

	
	
	
	public static boolean allocateSpaceY(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[], int refPointIndex)
	{
		double numExpandBeforeRef = 0.0, numExpandAfterRef = 0.0;
		
		// Count the number of children that should expand to use additional space
		if ( children.length > 0 )
		{
			int index = 0;
			for (int flags: childAllocationFlags)
			{
				VAlignment alignment = ElementAlignment.getVAlignment( flags );
				if ( alignment == VAlignment.EXPAND  ||  alignment == VAlignment.REFY_EXPAND )
				{
					if ( index < refPointIndex )
					{
						numExpandBeforeRef += 1.0;
					}
					else if ( index > refPointIndex )
					{
						numExpandAfterRef += 1.0;
					}
					else if ( index == refPointIndex )
					{
						numExpandBeforeRef += 0.5;
						numExpandAfterRef += 0.5;
					}
				}
				index++;
			}
		}

		if ( allocBox.getAllocationY() <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||
				( numExpandBeforeRef == 0.0  &&  numExpandAfterRef == 0.0 ) )			// if allocation <= required   or   numExpand == 0
		{
			// Allocate children their preferred width
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				allocBox.allocateChildHeightAsRequisition( childAlloc, child );
			}
			
			return false;
		}
		else
		{
			// Allocate children their preferred size, plus any extra to those for which the expand flag is set
			double totalExpand = allocBox.getAllocationY() - box.getReqHeight();
			double expandBeforeRef = allocBox.getRefY() - box.getRefY();
			double expandAfterRef = totalExpand - expandBeforeRef;
			double expandPerChildBeforeRef = numExpandBeforeRef > 0.0  ?  expandBeforeRef / numExpandBeforeRef  :  0.0;
			double expandPerChildAfterRef = numExpandAfterRef > 0.0  ?  expandAfterRef / numExpandAfterRef  :  0.0;
			
			
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				VAlignment alignment = ElementAlignment.getVAlignment( childAllocationFlags[i] );
				if ( alignment == VAlignment.EXPAND  ||  alignment == VAlignment.REFY_EXPAND )
				{
					if ( i < refPointIndex )
					{
						allocBox.allocateChildHeightPaddedRequisition( childAlloc, child, child.getReqHeight() + expandPerChildBeforeRef );
					}
					else if ( i > refPointIndex )
					{
						allocBox.allocateChildHeightPaddedRequisition( childAlloc, child, child.getReqHeight() + expandPerChildAfterRef );
					}
					else
					{
						allocBox.allocateChildHeight( childAlloc, child, child.getReqHeight() + ( expandPerChildBeforeRef + expandPerChildAfterRef ) * 0.5, child.getRefY() + expandPerChildBeforeRef * 0.5 );
					}
				}
				else
				{
					allocBox.allocateChildHeightAsRequisition( childAlloc, child );
				}
			}
			
			return numExpandBeforeRef > 0.0;
		}
	}
	
	public static void allocateY(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[], int refPointIndex, double spacing)
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		boolean bExpandBeforeRefPoint = allocateSpaceY( box, children, allocBox, childrenAlloc, childAllocationFlags, refPointIndex );
		
		double size = 0.0;
		double pos = 0.0;

		if ( !bExpandBeforeRefPoint )
		{
			// No expansion before ref point
			double offsetY = Math.max( allocBox.getRefY() - box.getRefY(),  0.0 );
			pos += offsetY;
		}
		
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
		if ( allocBox.getAllocationY() <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||  !bExpand )		// if allocation <= required   or   numExpand == 0
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
			double expandPerChild = totalExpand / (double)children.length;
			
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				if ( bExpand )
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

			// Allocate child position
			allocBox.allocateChildPositionY( childAlloc, pos );

			// Accumulate width and x
			size = pos + childAlloc.getAllocationY();
			pos = size + child.getReqVSpacing() + spacing;
		}
	}
}
