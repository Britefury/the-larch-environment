//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package tests.Benchmarks;

import BritefuryJ.DocPresent.Layout.HorizontalLayout;
import BritefuryJ.DocPresent.Layout.LAllocBox;
import BritefuryJ.DocPresent.Layout.LAllocHelper;
import BritefuryJ.DocPresent.Layout.LReqBox;

public class Layoutbench
{
	private static LReqBox[] createSourceReqBoxes(int N)
	{
		LReqBox boxes[] = new LReqBox[N];
		for (int i = 0; i < N; i++)
		{
			boxes[i] = new LReqBox( i, i, 2.0, 1.0 ); 
		}
		return boxes;
	}
	
	private static LAllocBox[] createSourceAllocBoxes(int N)
	{
		LAllocBox boxes[] = new LAllocBox[N];
		for (int i = 0; i < N; i++)
		{
			boxes[i] = new LAllocBox( null ); 
		}
		return boxes;
	}
	
	
	private static void layout(LReqBox reqBox, LReqBox childReqBoxes[], LAllocBox allocBox, LAllocBox childAllocBoxes[], int childAlignments[])
	{
		HorizontalLayout.computeRequisitionX( reqBox, childReqBoxes, 0.5 );
		LAllocHelper.allocateX( allocBox, reqBox, 0.0, reqBox.getReqPrefWidth() );
		HorizontalLayout.allocateX( reqBox, childReqBoxes, allocBox, childAllocBoxes, childAlignments, 0.0 );

		HorizontalLayout.computeRequisitionY( reqBox, childReqBoxes, childAlignments );
		LAllocHelper.allocateY( allocBox, reqBox, 0.0, reqBox.getReqHeight() );
		HorizontalLayout.allocateY( reqBox, childReqBoxes, allocBox, childAllocBoxes, childAlignments );
	}
	
	
	private static void test(LReqBox reqBoxes[], LReqBox childReqBoxes[], LAllocBox allocBoxes[], LAllocBox childAllocBoxes[], int childAlignments[], int repeats)
	{
		for (int i = 0; i < repeats; i++)
		{
			for (int j = 0; j < reqBoxes.length; j++)
			{
				LReqBox reqBox = reqBoxes[j];
				LAllocBox allocBox = allocBoxes[j];
				layout( reqBox, childReqBoxes, allocBox, childAllocBoxes, childAlignments );
			}
		}
	}
	
	
	private static final int NUM_BOXES = 200;
	private static final int NUM_LAYOUTS = 100;
	private static final int WARMUP = 100;
	private static final int REPEATS = 100;
	
	
	
	public static void main(String args[])
	{
		LReqBox reqBoxes[] = createSourceReqBoxes( NUM_LAYOUTS );
		LReqBox childReqBoxes[] = createSourceReqBoxes( NUM_BOXES );
		LAllocBox allocBoxes[] = createSourceAllocBoxes( NUM_LAYOUTS );
		LAllocBox childAllocBoxes[] = createSourceAllocBoxes( NUM_BOXES );
		int childAlignments[] = new int[NUM_BOXES];
		
		test( reqBoxes, childReqBoxes, allocBoxes, childAllocBoxes, childAlignments, WARMUP );

		long a = System.nanoTime();
		test( reqBoxes, childReqBoxes, allocBoxes, childAllocBoxes, childAlignments, REPEATS );
		long b = System.nanoTime();
		
		System.out.println( "Time: " + (double)( b - a ) * 1.0e-9 );
	}
}
