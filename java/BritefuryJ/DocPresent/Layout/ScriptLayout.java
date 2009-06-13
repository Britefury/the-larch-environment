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

	
	
	public static LBox[] computeRequisitionX(LBox box, LBox leftSuper, LBox leftSub, LBox main, LBox rightSuper, LBox rightSub, double spacing, double scriptSpacing)
	{
		// Compute boxes for the left, main, and right columns
		LBox leftColumn = leftSuper != null  ?  leftSuper.copy()  :  new LBox();
		if ( leftSub != null )
		{
			leftColumn.maxRequisitionX( leftSub );
		}
		
		LBox mainColumn = main != null  ?  main  :  new LBox();
		
		LBox rightColumn = rightSuper != null  ?  rightSuper.copy()  :  new LBox();
		if ( rightSub != null )
		{
			rightColumn.maxRequisitionX( rightSub );
		}
		
		
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
		
		return new LBox[] { leftColumn, mainColumn, rightColumn };
	}



	public static double[] computeRequisitionY(LBox box, LBox leftSuper, LBox leftSub, LBox main, LBox rightSuper, LBox rightSub, double spacing, double scriptSpacing)
	{
		double superBaselineY = 0.0, mainBaselineY = 0.0, subBaselineY = 0.0;
		
		double Ma = 0.0, Md = 0.0, Mh = 0.0;
		if ( main != null )
		{
			Ma = main.reqAscent;
			Md = main.reqAscent;
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
		
		return new double[] { superBaselineY, mainBaselineY, subBaselineY };
	}


	
	public static void allocateX(LBox box, LBox leftSuper, LBox leftSub, LBox main, LBox rightSuper, LBox rightSub, LBox columnBoxes[], double spacing, double scriptSpacing)
	{
		double t;
		if ( box.prefWidth > box.minWidth )
		{
			t = ( box.allocationX - box.minWidth )  /  ( box.prefWidth - box.minWidth );
			t = Math.max( t, 0.0 );
			t = Math.min( t, 1.0 );
		}
		else
		{
			t = 1.0;
		}
		
		LBox leftColumn = columnBoxes[0], mainColumn = columnBoxes[1];
		
		double overallWidth = box.minWidth  +  ( box.prefWidth - box.minWidth ) * t;
		double leftWidth = leftColumn.minWidth  +  ( leftColumn.prefWidth - leftColumn.minWidth ) * t;
		double leftHSpacing = leftColumn.minHSpacing  +  ( leftColumn.prefHSpacing - leftColumn.minHSpacing ) * t;
		double mainWidth = mainColumn.minWidth  +  ( mainColumn.prefWidth - mainColumn.minWidth ) * t;
		double mainHSpacing = mainColumn.minHSpacing  +  ( mainColumn.prefHSpacing - mainColumn.minHSpacing ) * t;
	
		double padding = Math.max( ( box.allocationX - overallWidth )  *  0.5, 0.0 );
		double x = padding;
		
		// Allocate left children
		if ( leftSuper != null )
		{
			double childWidth = leftSuper.minWidth  +  ( leftSuper.prefWidth - leftSuper.minWidth ) * t;
			box.allocateChildX( leftSuper, x  +  ( leftWidth - childWidth ), childWidth );
		}
		if ( leftSub != null )
		{
			double childWidth = leftSub.minWidth  +  ( leftSub.prefWidth - leftSub.minWidth ) * t;
			box.allocateChildX( leftSub, x  +  ( leftWidth - childWidth ), childWidth ); 
		}
		
		if ( leftSuper != null  ||  leftSub != null )
		{
			leftHSpacing = Math.max( leftHSpacing, spacing );
		}
		
		x += leftWidth + leftHSpacing;
		
		
		// Allocate main child
		if ( main != null )
		{
			box.allocateChildX( main, x, mainWidth );
			mainHSpacing = Math.max( mainHSpacing, spacing );
		}
		
		x += mainWidth + mainHSpacing;
		
		
		// Allocate right children
		if ( rightSuper != null )
		{
			double childWidth = rightSuper.minWidth  +  ( rightSuper.prefWidth - rightSuper.minWidth ) * t;
			box.allocateChildX( rightSuper, x, childWidth );
		}
		if ( rightSub != null )
		{
			double childWidth = rightSub.minWidth  +  ( rightSub.prefWidth - rightSub.minWidth ) * t;
			box.allocateChildX( rightSub, x, childWidth ); 
		}
	}

	
	public static void allocateY(LBox box, LBox leftSuper, LBox leftSub, LBox main, LBox rightSuper, LBox rightSub, double rowBaselineY[], double spacing, double scriptSpacing)
	{
		double padding = Math.max( ( box.allocationY - box.getReqHeight() ) * 0.5, 0.0 );
		
		
		double superBaselineY = rowBaselineY[0], mainBaselineY = rowBaselineY[1], subBaselineY = rowBaselineY[2];
		
		// Allocate superscript children
		double y = padding + superBaselineY;
		if ( leftSuper != null )
		{
			box.allocateChildY( leftSuper, y - leftSuper.reqAscent, leftSuper.getReqHeight() );
		}
		if ( rightSuper != null )
		{
			box.allocateChildY( rightSuper, y - rightSuper.reqAscent, rightSuper.getReqHeight() );
		}
		
		
		// Allocate main child
		y = padding + mainBaselineY;
		if ( main != null )
		{
			box.allocateChildY( main, y - main.reqAscent, main.getReqHeight() );
		}
		

		// Allocate subscript children
		y = padding + subBaselineY;
		if ( leftSub != null )
		{
			box.allocateChildY( leftSub, y - leftSub.reqAscent, leftSub.getReqHeight() );
		}
		if ( rightSub != null )
		{
			box.allocateChildY( rightSub, y - rightSub.reqAscent, rightSub.getReqHeight() );
		}
	}

}
