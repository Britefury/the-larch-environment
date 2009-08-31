//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Layout;


public class ScriptLayout
{
	private static double superOffset = 0.333;
	private static double subOffset = 0.333;

	private static double superOffsetFraction = superOffset  /  ( superOffset + subOffset );
	private static double subOffsetFraction = subOffset  /  ( superOffset + subOffset );

	
	
	public static void computeRequisitionX(LReqBox box, LReqBox columnBoxes[], LReqBox leftSuper, LReqBox leftSub, LReqBox main, LReqBox rightSuper, LReqBox rightSub, double spacing, double scriptSpacing)
	{
		// Compute boxes for the left, main, and right columns
		columnBoxes[0].clearRequisitionX();
		columnBoxes[1].clearRequisitionX();
		columnBoxes[2].clearRequisitionX();
		if ( leftSuper != null )
		{
			columnBoxes[0].setRequisitionX( leftSuper );
		}
		if ( leftSub != null )
		{
			columnBoxes[0].maxRequisitionX( leftSub );
		}
		
		if ( main != null )
		{
			columnBoxes[1].setRequisitionX( main );
		}
		
		if ( rightSuper != null )
		{
			columnBoxes[2].setRequisitionX( rightSuper );
		}
		if ( rightSub != null )
		{
			columnBoxes[2].maxRequisitionX( rightSub );
		}

		LReqBox leftColumn = columnBoxes[0], rightColumn = columnBoxes[2];
		
		// Compute the spacing that is placed between the columns
		double leftSpacing = 0.0, mainSpacing = 0.0;
		if ( ( leftSuper != null  ||  leftSub != null )   &&   ( main != null  ||  rightSuper != null  ||  rightSub != null ) )
		{
			leftSpacing = spacing;
		}
		
		if ( main != null   &&   ( rightSuper != null  ||  rightSub != null ) )
		{
			mainSpacing = spacing;
		}
		
		
		// Compute the overall width and spacing
		double minW = leftColumn.minWidth  +  Math.max( leftColumn.minHSpacing, leftSpacing )  +  main.minWidth  +  Math.max( main.minHSpacing, mainSpacing )  +  rightColumn.minWidth;
		double prefW = leftColumn.prefWidth  +  Math.max( leftColumn.prefHSpacing, leftSpacing )  +  main.prefWidth  +  Math.max( main.prefHSpacing, mainSpacing )  +  rightColumn.prefWidth;
		double minHSpacing = 0.0, prefHSpacing = 0.0;
		
		if ( rightSuper != null  ||  rightSub != null )
		{
			minHSpacing = rightColumn.minHSpacing;
			prefHSpacing = rightColumn.prefHSpacing;
		}
		else if ( main != null )
		{
			minHSpacing = main.minHSpacing;
			prefHSpacing = main.prefHSpacing;
		}
		else if ( leftSuper != null  ||  leftSub != null )
		{
			minHSpacing = leftColumn.minHSpacing;
			prefHSpacing = leftColumn.prefHSpacing;
		}
		
		
		// Set the requisition
		box.setRequisitionX( minW, prefW, minHSpacing, prefHSpacing );
	}



	public static void computeRequisitionY(LReqBox box, double rowBaselineY[], LReqBox leftSuper, LReqBox leftSub, LReqBox main, LReqBox rightSuper, LReqBox rightSub, double spacing, double scriptSpacing)
	{
		double superBaselineY = 0.0, mainBaselineY = 0.0, subBaselineY = 0.0;
		
		double Ma = 0.0, Md = 0.0, Mh = 0.0;
		if ( main != null )
		{
			Ma = main.reqAscent;
			Md = main.reqDescent;
			Mh = main.getReqHeight();
		}

		double Pa = Math.max( leftSuper != null  ?  leftSuper.reqAscent  :  0.0, rightSuper != null  ?  rightSuper.reqAscent  :  0.0 );
		double Pd = Math.max( leftSuper != null  ?  leftSuper.reqDescent  :  0.0, rightSuper != null  ?  rightSuper.reqDescent  :  0.0 );

		double Ba = Math.max( leftSub != null  ?  leftSub.reqAscent  :  0.0, rightSub != null  ?  rightSub.reqAscent  :  0.0 );
		double Bd = Math.max( leftSub != null  ?  leftSub.reqDescent  :  0.0, rightSub != null  ?  rightSub.reqDescent  :  0.0 );
		
		
		// top: TOP
		// bottom: BOTTOM
		// All y-co-ordinates are relative to an arbitrary co-ordinate system until the end of the calculation
		//
		// q: spacing between super bottom and sub top
		// r: min distance between super baseline and main baseline (1/3 of Mh)
		// s: min distance between main baseline and sub baseline (1/3 of Mh)
		// r and s can be thought of as springs
		double superTop, superBaseline, superBottom, mainTop, mainBaseline, mainBottom, subTop, subBaseline, subBottom, bottom, top, q, r, s;
		
		if ( ( leftSuper != null  ||  rightSuper != null )  &&  ( leftSub != null  ||  rightSub != null ) )
		{
			// We have both superscript and subscript children; either left or right
			
			q = scriptSpacing;
			
			// Start with main-baseline = 0
			mainBaseline = 0.0;
			
			// We can compute main-top and main-bottom immediately
			mainTop = mainBaseline - Ma;
			mainBottom = mainBaseline + Md;
			
			// Next compute super-baseline and sub-baseline
			// The distance between c and h is max( Pd + Ba + q, r + s )
			r = Mh * superOffset;
			s = Mh * subOffset;
			double deltaBaseline = Math.max( Pd + Ba + q,   r + s );
		
			// Divide cToH between r and s according to their proportion
			r = deltaBaseline  *  superOffsetFraction;
			s = deltaBaseline  *  subOffsetFraction;
			
			// We can now compute superscript-baseline and subscript-baseline
			superBaseline = mainBaseline - r;
			subBaseline = mainBaseline + s;
			
			// We can compute super-top and super-bottom
			superTop = superBaseline - Pa;
			superBottom = superBaseline + Pd;
			
			// We can compute subscript-top and subscript-bottom
			subTop = subBaseline - Ba;
			subBottom = subBaseline + Bd;
			
			// We can now compute the top and the bottom
			top = Math.min( mainTop, superTop );
			bottom = Math.max( mainBottom, subBottom );
			
			superBaselineY = superBaseline - top;
			mainBaselineY = mainBaseline - top;
			subBaselineY = subBaseline - top;
		}
		else if ( leftSuper != null  ||  rightSuper != null )
		{
			// We have only superscipt children
			
			// Start with main-baseline = 0
			mainBaseline = 0.0;
			
			// We can compute main-top and main-bottom immediately
			mainTop = mainBaseline - Ma;
			mainBottom = mainBaseline + Md;
			
			// R
			r = Mh * superOffset;
			
			// Superscript-baseline
			superBaseline = mainBaseline - r;

			// We can compute super-top and super-bottom
			superTop = superBaseline - Pa;
			superBottom = superBaseline + Pd;
			
			// We can now compute the top and the bottom
			top = Math.min( mainTop, superTop );
			bottom = Math.max( mainBottom, superBottom );
			
			superBaselineY = superBaseline - top;
			mainBaselineY = mainBaseline - top;
			subBaselineY = mainBaseline - top;
		}
		else if ( leftSub != null  ||  rightSub != null )
		{
			// We have only subscipt children
			
			// Start with main-baseline = 0
			mainBaseline = 0.0;
			
			// We can compute main-top and main-bottom immediately
			mainTop = mainBaseline - Ma;
			mainBottom = mainBaseline + Md;
			
			// S
			s = Mh * subOffset;
			
			// We can now compute subscript-baseline
			subBaseline = mainBaseline + s;
			
			// We can compute sub-top and sub-bottom 
			subTop = subBaseline - Ba;
			subBottom = subBaseline + Bd;
			
			// We can now compute the top and the bottom
			top = Math.min( mainTop, subTop );
			bottom = Math.max( mainBottom, subBottom );
			
			superBaselineY = mainBaseline - top;
			mainBaselineY = mainBaseline - top;
			subBaselineY = subBaseline - top;
		}
		else
		{
			mainBaseline = 0.0;
			
			top = -Ma;
			bottom = Md;
			
			superBaselineY = mainBaseline - top;
			mainBaselineY = mainBaseline - top;
			subBaselineY = mainBaseline - top;
		}
		
		
		double height = bottom - top;
		
		double descent = height - mainBaselineY;
		
		box.setRequisitionY( mainBaselineY, descent, spacing );
		
		rowBaselineY[0] = superBaselineY;
		rowBaselineY[1] = mainBaselineY;
		rowBaselineY[2] = subBaselineY;
	}


	
	public static void allocateX(LReqBox box, LReqBox leftSuper, LReqBox leftSub, LReqBox main, LReqBox rightSuper, LReqBox rightSub, LReqBox columnBoxes[],
			LAllocBox allocBox, LAllocBox leftSuperAlloc, LAllocBox leftSubAlloc, LAllocBox mainAlloc, LAllocBox rightSuperAlloc, LAllocBox rightSubAlloc,
			double spacing, double scriptSpacing)
	{
		double t;
		if ( box.prefWidth > box.minWidth )
		{
			t = ( allocBox.allocationX - box.minWidth )  /  ( box.prefWidth - box.minWidth );
			t = Math.max( t, 0.0 );
			t = Math.min( t, 1.0 );
		}
		else
		{
			t = 1.0;
		}
		
		LReqBox leftColumn = columnBoxes[0], mainColumn = columnBoxes[1];
		
		double overallWidth = box.minWidth  +  ( box.prefWidth - box.minWidth ) * t;
		double leftWidth = leftColumn.minWidth  +  ( leftColumn.prefWidth - leftColumn.minWidth ) * t;
		double leftHSpacing = leftColumn.minHSpacing  +  ( leftColumn.prefHSpacing - leftColumn.minHSpacing ) * t;
		double mainWidth = mainColumn.minWidth  +  ( mainColumn.prefWidth - mainColumn.minWidth ) * t;
		double mainHSpacing = mainColumn.minHSpacing  +  ( mainColumn.prefHSpacing - mainColumn.minHSpacing ) * t;
	
		double padding = Math.max( ( allocBox.allocationX - overallWidth )  *  0.5, 0.0 );
		double x = padding;
		
		// Allocate left children
		if ( leftSuper != null )
		{
			double childWidth = leftSuper.minWidth  +  ( leftSuper.prefWidth - leftSuper.minWidth ) * t;
			allocBox.allocateChildX( leftSuperAlloc, x  +  ( leftWidth - childWidth ), childWidth );
		}
		if ( leftSub != null )
		{
			double childWidth = leftSub.minWidth  +  ( leftSub.prefWidth - leftSub.minWidth ) * t;
			allocBox.allocateChildX( leftSubAlloc, x  +  ( leftWidth - childWidth ), childWidth ); 
		}
		
		if ( leftSuper != null  ||  leftSub != null )
		{
			leftHSpacing = Math.max( leftHSpacing, spacing );
		}
		
		x += leftWidth + leftHSpacing;
		
		
		// Allocate main child
		if ( main != null )
		{
			allocBox.allocateChildX( mainAlloc, x, mainWidth );
			mainHSpacing = Math.max( mainHSpacing, spacing );
		}
		
		x += mainWidth + mainHSpacing;
		
		
		// Allocate right children
		if ( rightSuper != null )
		{
			double childWidth = rightSuper.minWidth  +  ( rightSuper.prefWidth - rightSuper.minWidth ) * t;
			allocBox.allocateChildX( rightSuperAlloc, x, childWidth );
		}
		if ( rightSub != null )
		{
			double childWidth = rightSub.minWidth  +  ( rightSub.prefWidth - rightSub.minWidth ) * t;
			allocBox.allocateChildX( rightSubAlloc, x, childWidth ); 
		}
	}

	
	public static void allocateY(LReqBox box, LReqBox leftSuper, LReqBox leftSub, LReqBox main, LReqBox rightSuper, LReqBox rightSub, double rowBaselineY[],
			LAllocBox allocBox, LAllocBox leftSuperAlloc, LAllocBox leftSubAlloc, LAllocBox mainAlloc, LAllocBox rightSuperAlloc, LAllocBox rightSubAlloc,
			double spacing, double scriptSpacing)
	{
		double padding = Math.max( ( allocBox.getAllocationY() - box.getReqHeight() ) * 0.5, 0.0 );
		
		
		double superBaselineY = rowBaselineY[0], mainBaselineY = rowBaselineY[1], subBaselineY = rowBaselineY[2];
		
		// Allocate superscript children
		double y = padding + superBaselineY;
		if ( leftSuper != null )
		{
			allocBox.allocateChildYAsRequisition( leftSuperAlloc, leftSuper, y - leftSuper.reqAscent );
		}
		if ( rightSuper != null )
		{
			allocBox.allocateChildYAsRequisition( rightSuperAlloc, rightSuper, y - rightSuper.reqAscent );
		}
		
		
		// Allocate main child
		y = padding + mainBaselineY;
		if ( main != null )
		{
			allocBox.allocateChildYAsRequisition( mainAlloc, main, y - main.reqAscent );
		}
		

		// Allocate subscript children
		y = padding + subBaselineY;
		if ( leftSub != null )
		{
			allocBox.allocateChildYAsRequisition( leftSubAlloc, leftSub, y - leftSub.reqAscent );
		}
		if ( rightSub != null )
		{
			allocBox.allocateChildYAsRequisition( rightSubAlloc, rightSub, y - rightSub.reqAscent );
		}
	}

}
