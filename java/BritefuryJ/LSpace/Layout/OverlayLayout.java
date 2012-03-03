//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Layout;

public class OverlayLayout
{
	public static void computeRequisitionX(LReqBoxInterface box, LReqBoxInterface children[])
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
				minWidth = Math.max( minWidth, child.getReqMinWidth() );
				prefWidth = Math.max( prefWidth, child.getReqPrefWidth() );
				minHAdvance = Math.max( minHAdvance, child.getReqMinHAdvance() );
				prefHAdvance = Math.max( prefHAdvance, child.getReqPrefHAdvance() );
			}

			box.setRequisitionX( minWidth, prefWidth, minHAdvance, prefHAdvance );
		}
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
				double childHeightAboveRef = child.getReqRefY();
				double childHeightBelowRef = childHeight - childHeightAboveRef;
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

	
	
	public static void allocateX(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[])
	{
		for (int i = 0; i < children.length; i++)
		{
			LAllocHelper.allocateChildXAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, allocBox.getAllocWidth() );
		}
	}
	
	public static LAllocV computeVerticalAllocationForOverlay(LReqBoxInterface reqBox, LAllocBoxInterface allocBox)
	{
		double allocBoxAllocationY = allocBox.getAllocHeight();
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
		LAllocV h = computeVerticalAllocationForOverlay( box, allocBox );
		
		//System.out.println( "HorizontalLayout.allocateY(): box=" + box + ", allocBox=" + allocBox + ", h=" + h );
		
		for (int i = 0; i < children.length; i++)
		{
			LAllocHelper.allocateChildYAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, h );
		}
	}	
}
