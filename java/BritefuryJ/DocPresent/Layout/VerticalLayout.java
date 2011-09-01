//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;

public class VerticalLayout
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

	public static void computeRequisitionY(LReqBoxInterface box, LReqBoxInterface children[], int refPointIndex, double spacing)
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
					refY = reqY + chBox.getReqRefY();
				}
				
				reqHeight = reqY + chBox.getReqHeight() ;
				reqAdvance = reqHeight + chBox.getReqVSpacing();
				reqY = reqAdvance + spacing;
			}
			
			if ( refPointIndex == -1 )
			{
				box.setRequisitionY( reqHeight, reqAdvance - reqHeight );
			}
			else
			{
				box.setRequisitionY( reqHeight, reqAdvance - reqHeight, refY );
			}
		}
	}




	public static void allocateX(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[])
	{
		for (int i = 0; i < children.length; i++)
		{
			LAllocHelper.allocateChildXAligned( childrenAlloc[i], children[i], childAllocationFlags[i], 0.0, allocBox.getAllocWidth() );
		}
	}

	
	
	
	public static boolean allocateSpaceY(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[], int refPointIndex)
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

		double allocBoxAllocationY = allocBox.getAllocHeight();

		if ( allocBoxAllocationY <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||
				( numExpandBeforeRef == 0.0  &&  numExpandAfterRef == 0.0 ) )			// if allocation <= required   or   numExpand == 0
		{
			// Allocate children their preferred width
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				LAllocHelper.allocateChildHeightAsRequisition( childAlloc, child );
			}
			
			return false;
		}
		else
		{
			// Allocate children their preferred size, plus any extra to those for which the expand flag is set
			double totalExpand = allocBoxAllocationY - box.getReqHeight();
			double expandBeforeRef = allocBox.getAllocRefY() - box.getReqRefY();
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
						LAllocHelper.allocateChildHeightPaddedRequisition( childAlloc, child, child.getReqHeight() + expandPerChildBeforeRef );
					}
					else if ( i > refPointIndex )
					{
						LAllocHelper.allocateChildHeightPaddedRequisition( childAlloc, child, child.getReqHeight() + expandPerChildAfterRef );
					}
					else
					{
						LAllocHelper.allocateChildHeight( childAlloc, child, child.getReqHeight() + ( expandPerChildBeforeRef + expandPerChildAfterRef ) * 0.5, child.getReqRefY() + expandPerChildBeforeRef * 0.5 );
					}
				}
				else
				{
					LAllocHelper.allocateChildHeightAsRequisition( childAlloc, child );
				}
			}
			
			return numExpandBeforeRef > 0.0  &&  refPointIndex != -1;
		}
	}
	
	public static void allocateY(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], int childAllocationFlags[], int refPointIndex, double spacing)
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater

		if ( refPointIndex == -1 )
		{
			int numFill = 0;
			
			// Count the number of children that should fill additional space
			if ( children.length > 0 )
			{
				for (int flags: childAllocationFlags)
				{
					VAlignment alignment = ElementAlignment.getVAlignment( flags );
					if ( !VAlignment.isPack( alignment ) )
					{
						numFill++;
					}
				}
			}
			
			double allocBoxAllocationY = allocBox.getAllocHeight();
			
			if ( allocBoxAllocationY <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||  numFill == 0 )			// if allocation <= required   or   numFill == 0
			{
				double size = 0.0;
				double pos = 0.0;
				
				// Allocate children their required size
				for (int i = 0; i < children.length; i++)
				{
					LReqBoxInterface child = children[i];
					LAllocBoxInterface childAlloc = childrenAlloc[i];
					LAllocHelper.allocateChildYAsRequisition( childAlloc, child, pos );

					// Accumulate height and y
					size = pos + childAlloc.getAllocHeight();
					pos = size + child.getReqVSpacing() + spacing;
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those which fill additional space
				double additionalSpace = allocBoxAllocationY - box.getReqHeight();
				double fillPerChild = numFill > 0  ?  additionalSpace / (double)numFill  :  0.0;
				
				
				double size = 0.0;
				double pos = 0.0;
				
				for (int i = 0; i < children.length; i++)
				{
					LReqBoxInterface child = children[i];
					LAllocBoxInterface childAlloc = childrenAlloc[i];
					VAlignment alignment = ElementAlignment.getVAlignment( childAllocationFlags[i] );
					double childSpace = VAlignment.isPack( alignment )  ?  child.getReqHeight()  :  child.getReqHeight() + fillPerChild;
					
					if ( VAlignment.isPack( alignment ) )
					{
						LAllocHelper.allocateChildYAsRequisition( childAlloc, child, pos );
					}
					else
					{
						LAllocHelper.allocateChildYAligned( childAlloc, child, alignment, pos, new LAllocV( child ).expandToHeight( childSpace ) );
					}

					// Accumulate height and y
					size = pos + childSpace;
					pos = size + child.getReqVSpacing() + spacing;
				}
			}
		}
		else
		{
			double numFillBeforeRef = 0.0, numFillAfterRef = 0.0;
			
			// Count the number of children that should expand to use additional space
			if ( children.length > 0 )
			{
				int index = 0;
				for (int flags: childAllocationFlags)
				{
					VAlignment alignment = ElementAlignment.getVAlignment( flags );
					if ( !VAlignment.isPack( alignment ) )
					{
						if ( index < refPointIndex )
						{
							numFillBeforeRef += 1.0;
						}
						else if ( index > refPointIndex )
						{
							numFillAfterRef += 1.0;
						}
						else if ( index == refPointIndex )
						{
							numFillBeforeRef += 0.5;
							numFillAfterRef += 0.5;
						}
					}
					index++;
				}
			}

			double allocBoxAllocationY = allocBox.getAllocHeight();

			if ( allocBoxAllocationY <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||
					( numFillBeforeRef == 0.0  &&  numFillAfterRef == 0.0 ) )			// if allocation <= required   or   numFill == 0
			{
				// No children fill space, before the ref point
				double offsetY = Math.max( allocBox.getAllocRefY() - box.getReqRefY(),  0.0 );

				double size = 0.0;
				double pos = offsetY;

				// Allocate children their preferred height
				for (int i = 0; i < children.length; i++)
				{
					LReqBoxInterface child = children[i];
					LAllocBoxInterface childAlloc = childrenAlloc[i];

					// Allocate child
					LAllocHelper.allocateChildYAsRequisition( childAlloc, child, pos );

					// Accumulate height and y
					size = pos + child.getReqHeight();
					pos = size + child.getReqVSpacing() + spacing;
				}
			}
			else
			{
				// Allocate children their preferred size, plus any extra to those for which the expand flag is set
				double extraSpace = allocBoxAllocationY - box.getReqHeight();
				double extraSpaceBeforeRef = allocBox.getAllocRefY() - box.getReqRefY();
				double extraSpaceAfterRef = extraSpace - extraSpaceBeforeRef;
				double fillPerChildBeforeRef = numFillBeforeRef > 0.0  ?  extraSpaceBeforeRef / numFillBeforeRef  :  0.0;
				double fillPerChildAfterRef = numFillAfterRef > 0.0  ?  extraSpaceAfterRef / numFillAfterRef  :  0.0;
				

				double size = 0.0;
				double pos = 0.0;
				
				if ( numFillBeforeRef == 0.0 )
				{
					// No expansion before ref point
					double offsetY = Math.max( allocBox.getAllocRefY() - box.getReqRefY(),  0.0 );
					pos += offsetY;
				}

				for (int i = 0; i < children.length; i++)
				{
					LReqBoxInterface child = children[i];
					LAllocBoxInterface childAlloc = childrenAlloc[i];
					VAlignment alignment = ElementAlignment.getVAlignment( childAllocationFlags[i] );
					
					if ( VAlignment.isPack( alignment ) )
					{
						LAllocHelper.allocateChildYAsRequisition( childAlloc, child, pos );
					}
					else
					{
						LAllocV childAllocV;
						if ( i < refPointIndex )
						{
							childAllocV = new LAllocV( child ).expandToHeight( child.getReqHeight() + fillPerChildBeforeRef );
						}
						else if ( i > refPointIndex )
						{
							childAllocV = new LAllocV( child ).expandToHeight( child.getReqHeight() + fillPerChildAfterRef );
						}
						else
						{
							childAllocV = new LAllocV( child.getReqHeight() + ( fillPerChildBeforeRef + fillPerChildAfterRef ) * 0.5, child.getReqRefY() + fillPerChildBeforeRef * 0.5 );
						}

						LAllocHelper.allocateChildYAligned( childAlloc, child, alignment, pos, childAllocV );
					}

					// Accumulate height and xy
					size = pos + childAlloc.getAllocHeight();
					pos = size + child.getReqVSpacing() + spacing;
				}
			}
		}
	}


	public static void allocateY(LReqBoxInterface box, LReqBoxInterface children[], LAllocBoxInterface allocBox, LAllocBoxInterface childrenAlloc[], double spacing, boolean bExpand)
	{
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own v-spacing if it is greater

		double allocBoxAllocationY = allocBox.getAllocHeight();
		if ( allocBoxAllocationY <= box.getReqHeight() * LReqBox.ONE_PLUS_EPSILON  ||  !bExpand )		// if allocation <= required   or   numExpand == 0
		{
			double size = 0.0;
			double pos = 0.0;
			// Allocate children their preferred width
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				
				// Allocate child height and position
				LAllocHelper.allocateChildYAsRequisition( childAlloc, child, pos );

				// Accumulate height and y
				size = pos + childAlloc.getAllocHeight();
				pos = size + child.getReqVSpacing() + spacing;
			}
		}
		else
		{
			// Allocate children their preferred size, plus any extra
			double totalExpand = allocBoxAllocationY - box.getReqHeight();
			double expandPerChild = totalExpand / (double)children.length;
			
			double size = 0.0;
			double pos = 0.0;
			for (int i = 0; i < children.length; i++)
			{
				LReqBoxInterface child = children[i];
				LAllocBoxInterface childAlloc = childrenAlloc[i];
				double childHeight;
				if ( bExpand )
				{
					childHeight = child.getReqHeight() + expandPerChild;
					LAllocHelper.allocateChildYAsPaddedRequisition( childAlloc, child, pos, childHeight );
				}
				else
				{
					childHeight = child.getReqHeight();
					LAllocHelper.allocateChildYAsRequisition( childAlloc, child, pos );
				}

				size = pos + childHeight;
				pos = size + child.getReqVSpacing() + spacing;
			}
		}
	}
}
