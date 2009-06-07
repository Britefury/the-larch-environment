//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Typesetting;


public class BoxPackingRequisition
{
	public static void maximumX(TSBox box, TSBox children[])
	{
		// The resulting box should have the following properties:
		// - maximum width of all children
		// - sum of width and h-spacing should be the max of that of all children

		
		box.clearRequisitionX();
		
		double minWidth = 0.0, minAdvance = 0.0;
		double prefWidth = 0.0, prefAdvance = 0.0;
		for (TSBox child: children)
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
	
	
	public static void accumulateX(TSBox box, TSBox children[], double spacing, double childPadding[])
	{
		// Accumulate the width required for all the children
		
		// Each packed child consists of:
		//	- start padding
		//	- child width
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater
		
		double minWidth = 0.0, prefWidth = 0.0;
		double minX = 0.0, prefX = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox chBox = children[i];
			
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			// Filter out any h-spacing that is within the amount of padding
			double minChildSpacing = Math.max( chBox.minHSpacing - padding, 0.0 );
			double prefChildSpacing = Math.max( chBox.prefHSpacing - padding, 0.0 );
			double interCellSpacing = ( i < children.length - 1 )  ?  spacing  :  0.0;  
			
			minWidth = minX + chBox.minWidth  +  padding * 2.0;
			prefWidth = prefX + chBox.prefWidth  +  padding * 2.0;
			minX = minWidth + minChildSpacing + interCellSpacing;
			prefX = prefWidth + prefChildSpacing + interCellSpacing;
		}
		
		box.setRequisitionX( minWidth, prefWidth, minX - minWidth, prefX - prefWidth );
	}




	public static void maximumY(TSBox box, TSBox children[], VAlignment alignment)
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
			double minAscent = 0.0, minDescent = 0.0, minDescentAndSpacing = 0.0, minHeight = 0.0, minAdvance = 0.0, minHalfHeight = 0.0, minHalfHeightAndSpacing = 0.0;
			double prefAscent = 0.0, prefDescent = 0.0, prefDescentAndSpacing = 0.0, prefHeight = 0.0, prefAdvance = 0.0, prefHalfHeight = 0.0, prefHalfHeightAndSpacing = 0.0;
			int baselineCount = 0;
			for (TSBox child: children)
			{
				if ( child.hasBaseline() )
				{
					double childMinDescentAndSpacing = child.minDescent + child.minVSpacing;
					double childPrefDescentAndSpacing = child.prefDescent + child.prefVSpacing;
					minAscent = Math.max( minAscent, child.minAscent );
					prefAscent = Math.max( prefAscent, child.prefAscent );
					minDescent = Math.max( minDescent, child.minDescent );
					prefDescent = Math.max( prefDescent, child.prefDescent );
					minDescentAndSpacing = Math.max( minDescentAndSpacing, childMinDescentAndSpacing );
					prefDescentAndSpacing = Math.max( prefDescentAndSpacing, childPrefDescentAndSpacing );
					baselineCount++;
				}
				else
				{
					double childMinHeight = child.getMinHeight();
					double childPrefHeight = child.getPrefHeight();
					double childMinAdvance = childMinHeight + child.minVSpacing;
					double childPrefAdvance = childPrefHeight + child.prefVSpacing;
					minHeight = Math.max( minHeight, childMinHeight );
					prefHeight = Math.max( prefHeight, childPrefHeight );
					minAdvance = Math.max( minAdvance, childMinAdvance );
					prefAdvance = Math.max( prefAdvance, childPrefAdvance );

					double childMinHalfHeight = childMinHeight * 0.5;
					double childPrefHalfHeight = childPrefHeight * 0.5;
					double childMinHalfHeightAndSpacing = childMinHalfHeight + child.minVSpacing;
					double childPrefHalfHeightAndSpacing = childPrefHalfHeight + child.prefVSpacing;
					minHalfHeight = Math.max( minHalfHeight, childMinHalfHeight );
					prefHalfHeight = Math.max( prefHalfHeight, childPrefHalfHeight );
					minHalfHeightAndSpacing = Math.max( minHalfHeightAndSpacing, childMinHalfHeightAndSpacing );
					prefHalfHeightAndSpacing = Math.max( prefHalfHeightAndSpacing, childPrefHalfHeightAndSpacing );
				}
			}
			
			if ( baselineCount == 0 )
			{
				// No children had baselines; result in a box that has no baseline
				box.setRequisitionY( minHeight, prefHeight, minAdvance - minHeight, prefAdvance - prefHeight );
			}
			else
			{
				if ( baselineCount < children.length )
				{
					// Some children did not have baselines; combine requirements
					
					// For the children that do not have a baseline, assume that there is one that is 'x' units below their centre,
					// where x is half the size of the ascent of the typeset children
					double offset = minAscent * 0.5;
					double noBaselineMinAscent = minHalfHeight + offset, noBaselinePrefAscent = prefHalfHeight + offset;
					double noBaselineMinDescent = minHalfHeight - offset, noBaselinePrefDescent = prefHalfHeight - offset;
					double noBaselineMinDescentAndSpacing = minHalfHeightAndSpacing - offset, noBaselinePrefDescentAndSpacing = prefHalfHeightAndSpacing - offset;
					minAscent = Math.max( minAscent, noBaselineMinAscent );
					prefAscent = Math.max( prefAscent, noBaselinePrefAscent );
					minDescent = Math.max( minDescent, noBaselineMinDescent );
					prefDescent = Math.max( prefDescent, noBaselinePrefDescent );
					minDescentAndSpacing = Math.max( minDescentAndSpacing, noBaselineMinDescentAndSpacing );
					prefDescentAndSpacing = Math.max( prefDescentAndSpacing, noBaselinePrefDescentAndSpacing );
				}
				
				box.setRequisitionY( minAscent, prefAscent, minDescent, prefDescent, minDescentAndSpacing - minDescent, prefDescentAndSpacing - prefDescent );
			}
		}
		else
		{
			double minHeight = 0.0, prefHeight = 0.0;
			double minAdvance = 0.0, prefAdvance = 0.0;
			for (TSBox child: children)
			{
				double childMinHeight = child.getMinHeight();
				double childPrefHeight = child.getPrefHeight();
				double childMinAdvance = childMinHeight + child.minVSpacing;
				double childPrefAdvance = childPrefHeight + child.prefVSpacing;
				minHeight = Math.max( minHeight, childMinHeight );
				prefHeight = Math.max( prefHeight, childPrefHeight );
				minAdvance = Math.max( minAdvance, childMinAdvance );
				prefAdvance = Math.max( prefAdvance, childPrefAdvance );
			}
			
			box.setRequisitionY( minHeight, prefHeight, minAdvance - minHeight, prefAdvance - prefHeight );
		}
	}
	
	
	public static void accumulateY(TSBox box, TSBox children[], double spacing, double childPadding[])
	{
		// Accumulate the width required for all the children
		
		// Each packed child consists of:
		//	- start padding
		//	- child height
		//	- end padding
		//	- any remaining spacing not 'consumed' by padding; spacing - padding  or  0 if padding > spacing
		
		// There should be at least the specified amount of spacing between each child, or the child's own h-spacing if it is greater
		
		double minHeight = 0.0, prefHeight = 0.0;
		double minY = 0.0, prefY = 0.0;
		for (int i = 0; i < children.length; i++)
		{
			TSBox chBox = children[i];
			
			double padding = childPadding != null  ?  childPadding[i]  :  0.0;
			
			double minChildSpacing = Math.max( chBox.minVSpacing - padding, 0.0 );
			double prefChildSpacing = Math.max( chBox.prefVSpacing - padding, 0.0 );
			double interCellSpacing = ( i < children.length - 1 )  ?  spacing  :  0.0;  
			
			minHeight = minY + chBox.getMinHeight()  +  padding * 2.0;
			prefHeight = prefY + chBox.getPrefHeight()  +  padding * 2.0;
			minY = minHeight + minChildSpacing + interCellSpacing;
			prefY = prefHeight + prefChildSpacing + interCellSpacing;
		}
		
		box.setRequisitionY( minHeight, prefHeight, minY - minHeight, prefY - prefHeight );
	}
}
