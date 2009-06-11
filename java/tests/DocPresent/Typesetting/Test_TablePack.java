//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Typesetting;

import BritefuryJ.DocPresent.Typesetting.BoxPackingParams;
import BritefuryJ.DocPresent.Typesetting.HorizontalPack;
import BritefuryJ.DocPresent.Typesetting.TSBox;
import BritefuryJ.DocPresent.Typesetting.TablePack;
import BritefuryJ.DocPresent.Typesetting.TablePackingParams;
import BritefuryJ.DocPresent.Typesetting.VAlignment;

public class Test_TablePack extends Test_BoxPack_base
{
	private TablePackingParams pack(int x, int y)
	{
		return new TablePackingParams( x, 1, false, 0.0, y, 1, false, 0.0 );
	}
	
	private TablePackingParams pack(int x, int y, int colspan, int rowspan)
	{
		return new TablePackingParams( x, colspan, false, 0.0, y, rowspan, false, 0.0 );
	}
	
	private TablePackingParams pack(int x, int y, int colspan, int rowspan, double paddingX, double paddingY)
	{
		return new TablePackingParams( x, colspan, false, paddingX, y, rowspan, false, paddingY );
	}
	
	//
	//
	// REQUISITION TESTS
	//
	//
	
	private void tablePackReqTest(TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingX, double spacingY, VAlignment vAlignment,
			TSBox expectedColumnBoxes[], TSBox expectedRowBoxes[], TSBox expectedParentBox)
	{
		TSBox box = new TSBox();
		TSBox columnBoxes[] = TablePack.computeRequisitionX( box, children, packingParams, numColumns, numRows, spacingX, spacingY );
		TSBox rowBoxes[] = TablePack.computeRequisitionY( box, children, packingParams, numColumns, numRows, spacingX, spacingY, vAlignment );
		
		for (int i = 0; i < numColumns; i++)
		{
			if ( !columnBoxes[i].equals( expectedColumnBoxes[i] ) )
			{
				System.out.println( "COLUMN BOX FOR COLUMN " + i + " IS NOT AS EXPECTED" );
				System.out.println( "EXPECTED" );
				System.out.println( expectedColumnBoxes[i] );
				System.out.println( "RESULT" );
				System.out.println( columnBoxes[i] );
			}
			assertEquals( columnBoxes[i], expectedColumnBoxes[i] );
		}
		
		for (int i = 0; i < numRows; i++)
		{
			if ( !rowBoxes[i].equals( expectedRowBoxes[i] ) )
			{
				System.out.println( "ROW BOX FOR ROW " + i + " IS NOT AS EXPECTED" );
				System.out.println( "EXPECTED" );
				System.out.println( expectedRowBoxes[i] );
				System.out.println( "RESULT" );
				System.out.println( rowBoxes[i] );
			}
			assertEquals( rowBoxes[i], expectedRowBoxes[i] );
		}

		if ( !box.equals( expectedParentBox ) )
		{
			System.out.println( "PARENT BOX IS NOT AS EXPECTED" );
			System.out.println( "EXPECTED" );
			System.out.println( expectedParentBox );
			System.out.println( "RESULT" );
			System.out.println( box );
		}
		assertEquals( box, expectedParentBox );
	}
	
	
	public void test_requisition()
	{
		// 1x1
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, VAlignment.CENTRE,
				new TSBox[] {
						box( 10, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
				},
				box( 10, 0, 10, 0 )
			);

	
		// 2x2
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 4, 0, 4, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),
				},
				2, 2, 0.0, 0.0, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 5, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
				},
				box( 20, 0, 14, 0 )
			);

	
		// 2x2 with spacing
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 4, 0, 4, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),
				},
				2, 2, 1.0, 1.0, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 5, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
				},
				box( 21, 0, 15, 0 )
			);

	

		
		// 3x3
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 20, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 8, 0, 4, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 4, 0 ),		box( 6, 0, 8, 0 ),		box( 10, 0, 6, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 43, 0, 22, 0 )
			);

	
	
		// 3x3 with spacing
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 20, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 8, 0, 4, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 4, 0 ),		box( 6, 0, 8, 0 ),		box( 10, 0, 6, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 1.0, 2.0, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 45, 0, 26, 0 )
			);

	
	
	
		// 3x3 with a gap
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 20, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),							box( 10, 0, 4, 0 ),
						box( 5, 0, 4, 0 ),		box( 6, 0, 8, 0 ),		box( 10, 0, 6, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),								pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 6, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 41, 0, 22, 0 )
			);

	
	
	
	}
}
