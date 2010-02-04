//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class HorizontalLayout
{
	public static void computeRequisitionX(LReqBox box, LReqBoxInterface children[], double spacing)
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
			LReqBoxInterface child = children[i];
			
			minWidth = minX + child.getMinWidth();
			prefWidth = prefX + child.getPrefWidth();
			minAdvance = minX + child.getMinHAdvance();
			prefAdvance = prefX + child.getPrefHAdvance();
			minX = minAdvance + spacing;
			prefX = prefAdvance + spacing;
		}
		
		box.setRequisitionX( minWidth, prefWidth, minAdvance, prefAdvance );
	}

	
	
	public static void computeRequisitionY(LReqBox box, LReqBoxInterface children[], int childAllocationFlags[])
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
		
		double rowHeight = 0.0, rowHeightAboveRef = 0.0,  rowHeightBelowRef = 0.0, rowHeightAndSpacing = 0.0, rowHeightBelowRefAndSpacing = 0.0;
		boolean bRefYAligned = false;
		for (int i = 0; i < children.length; i++)
		{
			LReqBoxInterface child = children[i];
			VAlignment v = ElementAlignment.getVAlignment( childAllocationFlags[i] );
			
			double childHeight = child.getReqHeight();
			
			if ( v == VAlignment.REFY  ||  v == VAlignment.REFY_EXPAND )
			{
				double childRefY = child.getRefY();
				double childHeightAboveRef = childRefY;
				double childHeightBelowRef = childHeight - childRefY;
				double childHeightBelowRefAndSpacing = childHeightBelowRef + child.getReqVSpacing();
				
				rowHeight = Math.max( rowHeight, childHeight );
				rowHeightAboveRef = Math.max( rowHeightAboveRef, childHeightAboveRef );
				rowHeightBelowRef = Math.max( rowHeightBelowRef, childHeightBelowRef );
				rowHeightBelowRefAndSpacing = Math.max( rowHeightBelowRefAndSpacing, childHeightBelowRefAndSpacing );
				
				bRefYAligned = true;
			}
			else
			{
				double childHeightAndSpacing = childHeight + child.getReqVSpacing();

				rowHeight = Math.max( rowHeight, childHeight );
				rowHeightAndSpacing = Math.max( rowHeightAndSpacing, childHeightAndSpacing );
			}
		}
		rowHeight = Math.max( rowHeight, rowHeightAboveRef + rowHeightBelowRef );
		rowHeightAndSpacing = Math.max( rowHeightAndSpacing, rowHeightAboveRef + rowHeightBelowRefAndSpacing );
		
		if ( bRefYAligned )
		{
			box.setRequisitionY( rowHeight, rowHeightAndSpacing - rowHeight, rowHeightAboveRef );
		}
		else
		{
			box.setRequisitionY( rowHeight, rowHeightAndSpacing - rowHeight );
		}
	}




	public static void allocateX(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAlignmentFlags[], double spacing)
	{
		int numExpand = 0;
		
		// Count the number of children that should expand to use additional space
		if ( children.length > 0 )
		{
			for (int i = 0; i < children.length; i++)
			{
				HAlignment h = ElementAlignment.getHAlignment( childAlignmentFlags[i] );
				if ( h == HAlignment.EXPAND )
				{
					numExpand++;
				}
			}
		}

		if ( allocBox.allocationX >= box.getPrefWidth() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( allocBox.allocationX <= box.getPrefWidth() * LReqBox.ONE_PLUS_EPSILON  ||  numExpand == 0 )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred width
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getPrefWidth() );
					pos += ( children[i].getPrefHAdvance() + spacing );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationX - box.getPrefWidth();
				double expandPerChild = totalExpand / (double)numExpand;
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					HAlignment h = ElementAlignment.getHAlignment( childAlignmentFlags[i] );
					if ( h == HAlignment.EXPAND )
					{
						allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getPrefWidth() + expandPerChild );
						pos += ( children[i].getPrefHAdvance() + expandPerChild + spacing );
					}
					else
					{
						allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getPrefWidth() );
						pos += ( children[i].getPrefHAdvance() + spacing );
					}
				}
			}
		}
		else if ( allocBox.allocationX <= box.getMinWidth() * LReqBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their minimum width
			double pos = 0.0;
			for (int i = 0; i < children.length; i++)
			{
				allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getMinWidth() );
				pos += ( children[i].getMinHAdvance() + spacing );
			}
		}
		else
		{
			// Allocation is between minimum and preferred size
			
			if ( children.length >= 1 )
			{
				// Allocate children their minimum size, distributing additional space equally
				double delta = allocBox.allocationX - box.getMinWidth();
				double fraction = delta / ( box.getPrefWidth() - box.getMinWidth() );
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					double expand = ( children[i].getPrefWidth() - children[i].getMinWidth() )  *  fraction;
					allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getMinWidth() + expand );
					pos += ( children[i].getMinHAdvance() + expand + spacing );
				}
			}
		}
	}

	
	
	public static void allocateX(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], double spacing, boolean bExpand)
	{
		if ( allocBox.allocationX >= box.getPrefWidth() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( allocBox.allocationX <= box.getPrefWidth() * LReqBox.ONE_PLUS_EPSILON  ||  !bExpand )			// if allocation == preferred   or   not bExpand
			{
				// Allocate children their preferred width
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getPrefWidth() );
					pos += ( children[i].getPrefHAdvance() + spacing );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBox.allocationX - box.getPrefWidth();
				double expandPerChild = totalExpand / (double)children.length;
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getPrefWidth() + expandPerChild );
					pos += ( children[i].getPrefHAdvance() + expandPerChild + spacing );
				}
			}
		}
		else if ( allocBox.allocationX <= box.getMinWidth() * LReqBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their minimum width
			double pos = 0.0;
			for (int i = 0; i < children.length; i++)
			{
				allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getMinWidth() );
				pos += ( children[i].getMinHAdvance() + spacing );
			}
		}
		else
		{
			// Allocation is between minimum and preferred size
			
			if ( children.length >= 1 )
			{
				// Allocate children their minimum size, distributing additional space equally
				double delta = allocBox.allocationX - box.getMinWidth();
				double fraction = delta / ( box.getPrefWidth() - box.getMinWidth() );
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					double expand = ( children[i].getPrefWidth() - children[i].getMinWidth() )  *  fraction;
					allocBox.allocateChildX( childrenAlloc[i], pos, children[i].getMinWidth() + expand );
					pos += ( children[i].getMinHAdvance() + expand + spacing );
				}
			}
		}
	}

	
	
	public static LAllocV computeVerticalAllocationForRow(LReqBox reqBox, LAllocBox allocBox)
	{
		//return new LAllocV( Math.max( reqBox.getReqHeight(), allocBox.getAllocationY() ),  Math.max( reqBox.getRefY(), ))
		if ( allocBox.getAllocationY() < reqBox.getReqHeight() )
		{
			return new LAllocV( reqBox.getReqHeight(), reqBox.getRefY() );
		}
		else
		{
			double reqHeight = reqBox.getReqHeight();
			double reqHeightAboveBaseline = reqBox.getRefY();
			double reqHeightBelowBaseline = reqHeight - reqHeightAboveBaseline;
			
			double allocHeight = allocBox.getAllocationY();
			double minRefY = reqHeightAboveBaseline;
			double maxRefY = allocHeight - reqHeightBelowBaseline;
			
			double refY = Math.min( Math.max( allocBox.getRefY(), minRefY ), maxRefY );

			return new LAllocV( allocHeight, refY );
		}
	}
	
	public static void allocateY(LReqBox box, LReqBoxInterface children[], LAllocBox allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[])
	{
		LAllocV h = computeVerticalAllocationForRow( box, allocBox );
		
		//System.out.println( "HorizontalLayout.allocatey(): box=" + box + ", allocBox=" + allocBox + ", h=" + h );
		
		for (int i = 0; i < children.length; i++)
		{
			allocBox.allocateChildYAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, h );
		}
	}	
}
