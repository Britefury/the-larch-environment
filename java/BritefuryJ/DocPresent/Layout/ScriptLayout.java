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

	
	
	public static void computeRequisitionX(LReqBoxInterface box, LReqBox columnBoxes[],
			LReqBoxInterface leftSuper, LReqBoxInterface leftSub, LReqBoxInterface main, LReqBoxInterface rightSuper, LReqBoxInterface rightSub,
			double columnSpacing, double rowSpacing)
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
			leftSpacing = columnSpacing;
		}
		
		if ( main != null   &&   ( rightSuper != null  ||  rightSub != null ) )
		{
			mainSpacing = columnSpacing;
		}
		
		
		// Compute the overall width and spacing
		double minW = 0.0, prefW = 0.0, minAdv = 0.0, prefAdv = 0.0;
		if ( ( leftSuper != null  ||  leftSub != null )  &&  ( main == null  &&  rightSuper == null  &&  rightSub == null ) )
		{
			// Has a left column, no main or right columns
			
			minW = leftColumn.getReqMinWidth();
			prefW = leftColumn.getReqPrefWidth();
			minAdv = leftColumn.getReqMinHAdvance();
			prefAdv = leftColumn.getReqPrefHAdvance();
		}
		if ( main != null   &&   ( rightSuper == null  &&  rightSub == null ) )
		{
			// Has a main column, no right column
			
			double minX = Math.max( leftColumn.minWidth, leftColumn.minHAdvance )  +  leftSpacing;
			double prefX = Math.max( leftColumn.prefWidth, leftColumn.prefHAdvance )  +  leftSpacing;
			minW = minX + main.getReqMinWidth();
			prefW = prefX + main.getReqPrefWidth();
			minAdv = minX + main.getReqMinHAdvance();
			prefAdv = prefX + main.getReqPrefHAdvance();
		}
		else
		{
			double mainMinW = main != null  ?  Math.max( main.getReqMinWidth(), main.getReqMinHAdvance() )  :  0.0;
			double mainPrefW = main != null  ?  Math.max( main.getReqPrefWidth(), main.getReqPrefHAdvance() )  :  0.0;  
			minAdv = minW = Math.max( leftColumn.minWidth, leftColumn.minHAdvance )  +  leftSpacing  +
					mainMinW  +  mainSpacing  +
					Math.max( rightColumn.minWidth, rightColumn.minHAdvance );
			prefAdv = prefW = Math.max( leftColumn.prefWidth, leftColumn.prefHAdvance )  +  leftSpacing  +
					mainPrefW  +  mainSpacing  +
					Math.max( rightColumn.prefWidth, rightColumn.prefHAdvance );
		}
		
		// Set the requisition
		box.setRequisitionX( minW, prefW, minAdv, prefAdv );
	}



	public static void computeRequisitionY(LReqBoxInterface box, double rowBaselineY[],
			LReqBoxInterface leftSuper, LReqBoxInterface leftSub, LReqBoxInterface main, LReqBoxInterface rightSuper, LReqBoxInterface rightSub,
			double columnSpacing, double rowSpacing)
	{
		double superBaselineY = 0.0, mainBaselineY = 0.0, subBaselineY = 0.0;
		
		double Ma = 0.0, Md = 0.0, Mh = 0.0;
		if ( main != null )
		{
			Ma = main.getReqRefY();
			Md = main.getReqHeightBelowRefPoint();
			Mh = main.getReqHeight();
		}

		double Pa = Math.max( leftSuper != null  ?  leftSuper.getReqRefY()  :  0.0, rightSuper != null  ?  rightSuper.getReqRefY()  :  0.0 );
		double Pd = Math.max( leftSuper != null  ?  leftSuper.getReqHeightBelowRefPoint()  :  0.0, rightSuper != null  ?  rightSuper.getReqHeightBelowRefPoint()  :  0.0 );

		double Ba = Math.max( leftSub != null  ?  leftSub.getReqRefY()  :  0.0, rightSub != null  ?  rightSub.getReqRefY()  :  0.0 );
		double Bd = Math.max( leftSub != null  ?  leftSub.getReqHeightBelowRefPoint()  :  0.0, rightSub != null  ?  rightSub.getReqHeightBelowRefPoint()  :  0.0 );
		
		
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
			
			q = rowSpacing;
			
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
		
		box.setRequisitionY( mainBaselineY + descent, columnSpacing, mainBaselineY );
		
		rowBaselineY[0] = superBaselineY;
		rowBaselineY[1] = mainBaselineY;
		rowBaselineY[2] = subBaselineY;
	}


	
	public static void allocateX(LReqBoxInterface box, LReqBoxInterface leftSuper, LReqBoxInterface leftSub, LReqBoxInterface main, LReqBoxInterface rightSuper, LReqBoxInterface rightSub, LReqBox columnBoxes[],
			LAllocBoxInterface allocBox, LAllocBoxInterface leftSuperAlloc, LAllocBoxInterface leftSubAlloc, LAllocBoxInterface mainAlloc, LAllocBoxInterface rightSuperAlloc, LAllocBoxInterface rightSubAlloc,
			double columnSpacing, double rowSpacing)
	{
		double t;
		double boxReqMinWidth = box.getReqMinWidth();
		double boxReqPrefWidth = box.getReqPrefWidth();
		double allocAllocationX = Math.max( allocBox.getAllocWidth(), box.getReqMinWidth() );
		if ( boxReqPrefWidth > boxReqMinWidth )
		{
			t = ( allocAllocationX - boxReqMinWidth )  /  ( boxReqPrefWidth - boxReqMinWidth );
			t = Math.max( t, 0.0 );
			t = Math.min( t, 1.0 );
		}
		else
		{
			t = 1.0;
		}
		
		LReqBox leftColumn = columnBoxes[0], mainColumn = columnBoxes[1];
		
		double overallWidth = boxReqMinWidth  +  ( boxReqPrefWidth - boxReqMinWidth ) * t;
		double leftWidth = leftColumn.minWidth  +  ( leftColumn.prefWidth - leftColumn.minWidth ) * t;
		double leftHAdvance = leftColumn.minHAdvance  +  ( leftColumn.prefHAdvance - leftColumn.minHAdvance ) * t;
		double mainWidth = mainColumn.minWidth  +  ( mainColumn.prefWidth - mainColumn.minWidth ) * t;
		double mainHAdvance = mainColumn.minHAdvance  +  ( mainColumn.prefHAdvance - mainColumn.minHAdvance ) * t;
	
		double padding = Math.max( ( allocAllocationX - overallWidth )  *  0.5, 0.0 );
		double x = padding;
		
		// Allocate left children
		if ( leftSuper != null )
		{
			double childWidth = leftSuper.getReqMinWidth()  +  ( leftSuper.getReqPrefWidth() - leftSuper.getReqMinWidth() ) * t;
			LAllocHelper.allocateChildX( leftSuperAlloc, x  +  ( leftWidth - childWidth ), childWidth, childWidth );
		}
		if ( leftSub != null )
		{
			double childWidth = leftSub.getReqMinWidth()  +  ( leftSub.getReqPrefWidth() - leftSub.getReqMinWidth() ) * t;
			LAllocHelper.allocateChildX( leftSubAlloc, x  +  ( leftWidth - childWidth ), childWidth, childWidth ); 
		}
		
		if ( leftSuper != null  ||  leftSub != null )
		{
			x += Math.max( leftWidth, leftHAdvance )  +  columnSpacing;
		}
		
		
		// Allocate main child
		if ( main != null )
		{
			LAllocHelper.allocateChildX( mainAlloc, x, mainWidth, mainWidth );
			x += Math.max( mainWidth, mainHAdvance )  +  columnSpacing;
		}
		
		
		// Allocate right children
		if ( rightSuper != null )
		{
			double childWidth = rightSuper.getReqMinWidth()  +  ( rightSuper.getReqPrefWidth() - rightSuper.getReqMinWidth() ) * t;
			LAllocHelper.allocateChildX( rightSuperAlloc, x, childWidth, childWidth );
		}
		if ( rightSub != null )
		{
			double childWidth = rightSub.getReqMinWidth()  +  ( rightSub.getReqPrefWidth() - rightSub.getReqMinWidth() ) * t;
			LAllocHelper.allocateChildX( rightSubAlloc, x, childWidth, childWidth ); 
		}
	}

	
	public static void allocateY(LReqBoxInterface box, LReqBoxInterface leftSuper, LReqBoxInterface leftSub, LReqBoxInterface main, LReqBoxInterface rightSuper, LReqBoxInterface rightSub, double rowBaselineY[],
			LAllocBoxInterface allocBox, LAllocBoxInterface leftSuperAlloc, LAllocBoxInterface leftSubAlloc, LAllocBoxInterface mainAlloc, LAllocBoxInterface rightSuperAlloc, LAllocBoxInterface rightSubAlloc,
			double columnSpacing, double rowSpacing)
	{
		double padding = Math.max( ( allocBox.getAllocHeight() - box.getReqHeight() ) * 0.5, 0.0 );
		
		
		double superBaselineY = rowBaselineY[0], mainBaselineY = rowBaselineY[1], subBaselineY = rowBaselineY[2];
		
		// Allocate superscript children
		double y = padding + superBaselineY;
		if ( leftSuper != null )
		{
			LAllocHelper.allocateChildYAsRequisition( leftSuperAlloc, leftSuper, y - leftSuper.getReqRefY() );
		}
		if ( rightSuper != null )
		{
			LAllocHelper.allocateChildYAsRequisition( rightSuperAlloc, rightSuper, y - rightSuper.getReqRefY() );
		}
		
		
		// Allocate main child
		y = padding + mainBaselineY;
		if ( main != null )
		{
			LAllocHelper.allocateChildYAsRequisition( mainAlloc, main, y - main.getReqRefY() );
		}
		

		// Allocate subscript children
		y = padding + subBaselineY;
		if ( leftSub != null )
		{
			LAllocHelper.allocateChildYAsRequisition( leftSubAlloc, leftSub, y - leftSub.getReqRefY() );
		}
		if ( rightSub != null )
		{
			LAllocHelper.allocateChildYAsRequisition( rightSubAlloc, rightSub, y - rightSub.getReqRefY() );
		}
	}

}
