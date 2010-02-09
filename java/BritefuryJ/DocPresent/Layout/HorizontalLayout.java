//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class HorizontalLayout
{
	public static void computeRequisitionX(LReqBoxInterface box, LReqBoxInterface children[], double spacing)
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
			
			minWidth = minX + child.getReqMinWidth();
			prefWidth = prefX + child.getReqPrefWidth();
			minAdvance = minX + child.getReqMinHAdvance();
			prefAdvance = prefX + child.getReqPrefHAdvance();
			minX = minAdvance + spacing;
			prefX = prefAdvance + spacing;
		}
		
		box.setRequisitionX( minWidth, prefWidth, minAdvance, prefAdvance );
	}

	
	
	public static void computeRequisitionY(LReqBoxInterface box, LReqBoxInterface children[], int childAllocationFlags[])
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
				double childRefY = child.getReqRefY();
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




	public static void allocateX(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAlignmentFlags[], double spacing)
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

		double allocBoxAllocationX = allocBox.getAllocationX();

		if ( allocBoxAllocationX >= box.getReqPrefWidth() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( allocBoxAllocationX <= box.getReqPrefWidth() * LReqBox.ONE_PLUS_EPSILON  ||  numExpand == 0 )			// if allocation == preferred   or   numExpand == 0
			{
				// Allocate children their preferred width
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqPrefWidth() );
					pos += ( children[i].getReqPrefHAdvance() + spacing );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBoxAllocationX - box.getReqPrefWidth();
				double expandPerChild = totalExpand / (double)numExpand;
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					HAlignment h = ElementAlignment.getHAlignment( childAlignmentFlags[i] );
					if ( h == HAlignment.EXPAND )
					{
						LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqPrefWidth() + expandPerChild );
						pos += ( children[i].getReqPrefHAdvance() + expandPerChild + spacing );
					}
					else
					{
						LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqPrefWidth() );
						pos += ( children[i].getReqPrefHAdvance() + spacing );
					}
				}
			}
		}
		else if ( allocBoxAllocationX <= box.getReqMinWidth() * LReqBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their minimum width
			double pos = 0.0;
			for (int i = 0; i < children.length; i++)
			{
				LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqMinWidth() );
				pos += ( children[i].getReqMinHAdvance() + spacing );
			}
		}
		else
		{
			// Allocation is between minimum and preferred size
			
			if ( children.length >= 1 )
			{
				// Allocate children their minimum size, distributing additional space equally
				double delta = allocBoxAllocationX - box.getReqMinWidth();
				double fraction = delta / ( box.getReqPrefWidth() - box.getReqMinWidth() );
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					double expand = ( children[i].getReqPrefWidth() - children[i].getReqMinWidth() )  *  fraction;
					LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqMinWidth() + expand );
					pos += ( children[i].getReqMinHAdvance() + expand + spacing );
				}
			}
		}
	}

	
	
	public static void allocateX(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], double spacing, boolean bExpand)
	{
		double allocBoxAllocationX = allocBox.getAllocationX();
		if ( allocBoxAllocationX >= box.getReqPrefWidth() * LReqBox.ONE_MINUS_EPSILON )		// if allocation >= prefferred
		{
			if ( allocBoxAllocationX <= box.getReqPrefWidth() * LReqBox.ONE_PLUS_EPSILON  ||  !bExpand )			// if allocation == preferred   or   not bExpand
			{
				// Allocate children their preferred width
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqPrefWidth() );
					pos += ( children[i].getReqPrefHAdvance() + spacing );
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double totalExpand = allocBoxAllocationX - box.getReqPrefWidth();
				double expandPerChild = totalExpand / (double)children.length;
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqPrefWidth() + expandPerChild );
					pos += ( children[i].getReqPrefHAdvance() + expandPerChild + spacing );
				}
			}
		}
		else if ( allocBoxAllocationX <= box.getReqMinWidth() * LReqBox.ONE_PLUS_EPSILON )		// if allocation <= minimum
		{
			// Allocation is smaller than minimum size
			
			// Allocate children their minimum width
			double pos = 0.0;
			for (int i = 0; i < children.length; i++)
			{
				LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqMinWidth() );
				pos += ( children[i].getReqMinHAdvance() + spacing );
			}
		}
		else
		{
			// Allocation is between minimum and preferred size
			
			if ( children.length >= 1 )
			{
				// Allocate children their minimum size, distributing additional space equally
				double delta = allocBoxAllocationX - box.getReqMinWidth();
				double fraction = delta / ( box.getReqPrefWidth() - box.getReqMinWidth() );
				
				double pos = 0.0;
				for (int i = 0; i < children.length; i++)
				{
					double expand = ( children[i].getReqPrefWidth() - children[i].getReqMinWidth() )  *  fraction;
					LAllocHelper.allocateChildX( childrenAlloc[i], pos, children[i].getReqMinWidth() + expand );
					pos += ( children[i].getReqMinHAdvance() + expand + spacing );
				}
			}
		}
	}

	
	
	public static LAllocV computeVerticalAllocationForRow(LReqBoxInterface reqBox, LAllocBoxInterface allocBox)
	{
		//return new LAllocV( Math.max( reqBox.getReqHeight(), allocBox.getAllocationY() ),  Math.max( reqBox.getRefY(), ))
		double allocBoxAllocationY = allocBox.getAllocationY();
		if ( allocBoxAllocationY < reqBox.getReqHeight() )
		{
			return new LAllocV( reqBox.getReqHeight(), reqBox.getReqRefY() );
		}
		else
		{
			double reqHeight = reqBox.getReqHeight();
			double reqHeightAboveBaseline = reqBox.getReqRefY();
			double reqHeightBelowBaseline = reqHeight - reqHeightAboveBaseline;
			
			double allocHeight = allocBoxAllocationY;
			double minRefY = reqHeightAboveBaseline;
			double maxRefY = allocHeight - reqHeightBelowBaseline;
			
			double refY = Math.min( Math.max( allocBox.getAllocRefY(), minRefY ), maxRefY );

			return new LAllocV( allocHeight, refY );
		}
	}
	
	public static void allocateY(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[])
	{
		LAllocV h = computeVerticalAllocationForRow( box, allocBox );
		
		//System.out.println( "HorizontalLayout.allocatey(): box=" + box + ", allocBox=" + allocBox + ", h=" + h );
		
		for (int i = 0; i < children.length; i++)
		{
			LAllocHelper.allocateChildYAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, h );
		}
	}	
}
