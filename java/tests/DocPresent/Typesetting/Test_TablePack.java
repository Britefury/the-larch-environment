//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Typesetting;

import BritefuryJ.DocPresent.Typesetting.HAlignment;
import BritefuryJ.DocPresent.Typesetting.TSBox;
import BritefuryJ.DocPresent.Typesetting.TablePack;
import BritefuryJ.DocPresent.Typesetting.TablePackingParams;
import BritefuryJ.DocPresent.Typesetting.VAlignment;

public class Test_TablePack extends Test_BoxPack_base
{
	private TablePackingParams pack(int x, int y)
	{
		return new TablePackingParams( x, 1, 0.0, y, 1, 0.0 );
	}
	
	private TablePackingParams pack(int x, int y, int colspan, int rowspan)
	{
		return new TablePackingParams( x, colspan, 0.0, y, rowspan, 0.0 );
	}
	
	private TablePackingParams pack(int x, int y, double paddingX, double paddingY)
	{
		return new TablePackingParams( x, 1, paddingX, y, 1, paddingY );
	}
	
	private TablePackingParams pack(int x, int y, int colspan, int rowspan, double paddingX, double paddingY)
	{
		return new TablePackingParams( x, colspan, paddingX, y, rowspan, paddingY );
	}
	
	private TSBox alloced(double x, double y, double w, double h)
	{
		TSBox box = new TSBox();
		box.setAllocationX( w );
		box.setAllocationY( h );
		box.setPositionInParentSpaceX( x );
		box.setPositionInParentSpaceY( y );
		return box;
	}
	
	
	
	
	//
	//
	// REQUISITION TESTS
	//
	//
	
	private void tablePackReqTest(TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingX, double spacingY, boolean bExpandX, boolean bExpandY,
			HAlignment colAlignment, VAlignment rowAlignment, TSBox expectedColumnBoxes[], TSBox expectedRowBoxes[], TSBox expectedParentBox)
	{
		TSBox box = new TSBox();
		TSBox columnBoxes[] = TablePack.computeRequisitionX( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		TSBox rowBoxes[] = TablePack.computeRequisitionY( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		
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
				1, 1, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
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
				2, 2, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
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
				2, 2, 1.0, 1.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
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
				3, 3, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
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
				3, 3, 1.0, 2.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
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
				3, 3, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
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

		
		
		
		// 3x3 with a gap and a child with colspan
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 20, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 40, 0, 6, 0 ),
						box( 5, 0, 4, 0 ),		box( 10, 0, 8, 0 ),		box( 10, 0, 6, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 15, 0, 0, 0 ),		box( 25, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 6, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 55, 0, 24, 0 )
			);

		
		
		
		// 3x3 with a gap and a child with rowspan
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 20, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 8, 0, 20, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 4, 0 ),							box( 10, 0, 6, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 1, 2 ),		pack( 2, 1 ),
						pack( 0, 2 ),								pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 9, 0 ),
						box( 0, 0, 11, 0 ),
				},
				box( 43, 0, 30, 0 )
			);

		
		
		
		// 3x3 with caps and a child with colspan and rowspan
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 20, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 35, 0, 20, 0 ),		
						box( 5, 0, 4, 0 ),							
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 2, 2 ),		
						pack( 0, 2 ),								
				},
				3, 3, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 10, 0, 0, 0 ),		box( 25, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 9, 0 ),
						box( 0, 0, 11, 0 ),
				},
				box( 50, 0, 30, 0 )
			);
		
		
		
		
		// 3x3 with baseline v-alignment
		tablePackReqTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 20, 0, 5, 0 ),
						box( 15, 0, 4, 4, 0 ),		box( 8, 0, 2, 6, 0 ),		box( 10, 0, 5, 2, 0 ),
						box( 5, 0, 4, 0 ),		box( 6, 0, 8, 0 ),		box( 10, 0, 6, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.BASELINES,
				new TSBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new TSBox[] {
						box( 0, 0, 5, 5, 0 ),
						box( 0, 0, 5, 6, 0 ),
						box( 0, 0, 4, 4, 0 ),
				},
				box( 43, 0, 29, 0 )
			);
	}



	

	//
	//
	// ALLOCATION TESTS
	//
	//
	
	private void tablePackAllocTest(TSBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingX, double spacingY, boolean bExpandX, boolean bExpandY,
			HAlignment colAlignment, VAlignment rowAlignment,
			double allocX, double allocY, TSBox expectedChildAllocations[], TSBox expectedColAllocations[], TSBox expectedRowAllocations[])
	{
		TSBox expectedChildren[] = new TSBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			expectedChildren[i] = children[i].copy();
			expectedChildren[i].setAllocationFrom( expectedChildAllocations[i] );
		}

		
		TSBox box = new TSBox();

		TSBox columnBoxes[] = TablePack.computeRequisitionX( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		
		box.setAllocationX( allocX );
		TablePack.allocateX( box, columnBoxes, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		TSBox rowBoxes[] = TablePack.computeRequisitionY( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment);
		box.setAllocationY( allocY );
		TablePack.allocateY( box, rowBoxes, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		
		
		for (int i = 0; i < numColumns; i++)
		{
			TSBox expectedColumnBox = columnBoxes[i].copy();
			expectedColumnBox.setAllocationFrom( expectedColAllocations[i] );
			if ( !columnBoxes[i].equals( expectedColumnBox ) )
			{
				System.out.println( "COLUMN ALLOCATION FOR COLUMN " + i + " IS NOT AS EXPECTED" );
				System.out.println( "EXPECTED" );
				System.out.println( expectedColumnBox );
				System.out.println( "RESULT" );
				System.out.println( columnBoxes[i] );
			}
			assertEquals( columnBoxes[i], expectedColumnBox );
		}
		
		for (int i = 0; i < numRows; i++)
		{
			TSBox expectedRowBox = rowBoxes[i].copy();
			expectedRowBox.setAllocationFrom( expectedRowAllocations[i] );
			if ( !rowBoxes[i].equals( expectedRowBox ) )
			{
				System.out.println( "ROW ALLOCATION FOR ROW " + i + " IS NOT AS EXPECTED" );
				System.out.println( "EXPECTED" );
				System.out.println( expectedRowBox );
				System.out.println( "RESULT" );
				System.out.println( rowBoxes[i] );
			}
			assertEquals( rowBoxes[i], expectedRowBox );
		}
		
		for (int i = 0; i < children.length; i++)
		{
			if ( !children[i].equals( expectedChildren[i] ) )
			{
				System.out.println( "CHILD ALLOCATION FOR " + i + " IS NOT AS EXPECTED" );
				System.out.println( "EXPECTED" );
				System.out.println( expectedChildren[i] );
				System.out.println( "RESULT" );
				System.out.println( children[i] );
			}
			assertEquals( children[i], expectedChildren[i] );
		}
		
		box.setAllocationX( 0.0 );
		box.setAllocationY( 0.0 );
	}



	public void test_allocation()
	{
		// 1x1
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, false, false, HAlignment.LEFT, VAlignment.TOP,
				10, 10,
				new TSBox[] {
						alloced( 0, 0, 10, 10 ),
				},
				new TSBox[] {
						alloced( 0, 0, 10, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 10 ),
				}
			);

		// 1x1, extra space, left-top
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.LEFT, VAlignment.TOP,
				20, 20,
				new TSBox[] {
						alloced( 0, 0, 10, 10 ),
				},
				new TSBox[] {
						alloced( 0, 0, 20, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, centre-centre
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.CENTRE, VAlignment.CENTRE,
				20, 20,
				new TSBox[] {
						alloced( 5, 5, 10, 10 ),
				},
				new TSBox[] {
						alloced( 0, 0, 20, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, right-bottom
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.RIGHT, VAlignment.BOTTOM,
				20, 20,
				new TSBox[] {
						alloced( 10, 10, 10, 10 ),
				},
				new TSBox[] {
						alloced( 0, 0, 20, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, expand-expand
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.EXPAND, VAlignment.EXPAND,
				20, 20,
				new TSBox[] {
						alloced( 0, 0, 20, 20 ),
				},
				new TSBox[] {
						alloced( 0, 0, 20, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 20 ),
				}
			);


	
		// 3x3, extra space, no expand, top-left alignment
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, false, false, HAlignment.LEFT, VAlignment.TOP,
				40, 35,
				new TSBox[] {
						alloced( 0, 0, 10, 10 ),	alloced( 10, 0, 5, 5 ),		alloced( 15, 0, 5, 5 ),
						alloced( 0, 10, 5, 2 ),		alloced( 10, 10, 4, 5 ),	alloced( 15, 10, 10, 4 ),
						alloced( 0, 15, 5, 2 ),		alloced( 10, 15, 2, 2 ),	alloced( 15, 15, 10, 5 ),
				},
				new TSBox[] {
						alloced( 0, 0, 10, 0 ),		alloced( 10, 0, 5, 0 ),		alloced( 15, 0, 10, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 10 ),
						alloced( 0, 10, 0, 5 ),
						alloced( 0, 15, 0, 5 ),
				}
			);

	
	


		
		// 3x3, extra space, no expand, expand-expand alignment
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, false, false, HAlignment.EXPAND, VAlignment.EXPAND,
				40, 35,
				new TSBox[] {
						alloced( 0, 0, 10, 10 ),	alloced( 10, 0, 5, 10 ),	alloced( 15, 0, 10, 10 ),
						alloced( 0, 10, 10, 5 ),	alloced( 10, 10, 5, 5 ),	alloced( 15, 10, 10, 5 ),
						alloced( 0, 15, 10, 5 ),	alloced( 10, 15, 5, 5 ),	alloced( 15, 15, 10, 5 ),
				},
				new TSBox[] {
						alloced( 0, 0, 10, 0 ),		alloced( 10, 0, 5, 0 ),		alloced( 15, 0, 10, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 10 ),
						alloced( 0, 10, 0, 5 ),
						alloced( 0, 15, 0, 5 ),
				}
			);

	
	


		
		// 3x3, extra space, expand in x and y, top-left alignment
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, true, true, HAlignment.LEFT, VAlignment.TOP,
				40, 35,
				new TSBox[] {
						alloced( 0, 0, 10, 10 ),	alloced( 15, 0, 5, 5 ),		alloced( 25, 0, 5, 5 ),
						alloced( 0, 15, 5, 2 ),		alloced( 15, 15, 4, 5 ),	alloced( 25, 15, 10, 4 ),
						alloced( 0, 25, 5, 2 ),		alloced( 15, 25, 2, 2 ),	alloced( 25, 25, 10, 5 ),
				},
				new TSBox[] {
						alloced( 0, 0, 15, 0 ),		alloced( 15, 0, 10, 0 ),	alloced( 25, 0, 15, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 15 ),
						alloced( 0, 15, 0, 10 ),
						alloced( 0, 25, 0, 10 ),
				}
			);

	
	

		
		// 3x3, extra space, expand in x and y, expand-expand alignment
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				3, 3, 0.0, 0.0, true, true, HAlignment.EXPAND, VAlignment.EXPAND,
				40, 35,
				new TSBox[] {
						alloced( 0, 0, 15, 15 ),	alloced( 15, 0, 10, 15 ),	alloced( 25, 0, 15, 15 ),
						alloced( 0, 15, 15, 10 ),	alloced( 15, 15, 10, 10 ),	alloced( 25, 15, 15, 10 ),
						alloced( 0, 25, 15, 10 ),	alloced( 15, 25, 10, 10 ),	alloced( 25, 25, 15, 10 ),
				},
				new TSBox[] {
						alloced( 0, 0, 15, 0 ),		alloced( 15, 0, 10, 0 ),	alloced( 25, 0, 15, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 15 ),
						alloced( 0, 15, 0, 10 ),
						alloced( 0, 25, 0, 10 ),
				}
			);

	

		
		// 3x3, gap, colspan and rowspan, extra space, expand in x and y, expand-expand alignment
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 5, 0 ),		
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 2, 2 ),
						pack( 0, 2 ),			
				},
				3, 3, 0.0, 0.0, true, true, HAlignment.EXPAND, VAlignment.EXPAND,
				35, 35,
				new TSBox[] {
						alloced( 0, 0, 15, 15 ),	alloced( 15, 0, 10, 15 ),	alloced( 25, 0, 10, 15 ),
						alloced( 0, 15, 15, 10 ),	alloced( 15, 15, 20, 20 ),
						alloced( 0, 25, 15, 10 ),
				},
				new TSBox[] {
						alloced( 0, 0, 15, 0 ),		alloced( 15, 0, 10, 0 ),	alloced( 25, 0, 10, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 15 ),
						alloced( 0, 15, 0, 10 ),
						alloced( 0, 25, 0, 10 ),
				}
			);

	

		
		// 3x3, gap, colspan and rowspan, padding, extra space, expand in x and y, expand-expand alignment
		tablePackAllocTest(
				new TSBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 5, 0 ),		
				},
				new TablePackingParams[] {
						pack( 0, 0, 1.0, 1.0 ),	pack( 1, 0, 1.0, 1.0 ),	pack( 2, 0, 1.0, 1.0 ),
						pack( 0, 1, 1.0, 1.0 ),	pack( 1, 1, 2, 2, 1.0, 1.0 ),
						pack( 0, 2, 1.0, 1.0 ),			
				},
				3, 3, 0.0, 0.0, true, true, HAlignment.EXPAND, VAlignment.EXPAND,
				41, 41,
				new TSBox[] {
						alloced( 1, 1, 15, 15 ),	alloced( 18, 1, 10, 15 ),	alloced( 30, 1, 10, 15 ),
						alloced( 1, 18, 15, 10 ),	alloced( 18, 18, 22, 22 ),
						alloced( 1, 30, 15, 10 ),
				},
				new TSBox[] {
						alloced( 0, 0, 17, 0 ),		alloced( 17, 0, 12, 0 ),	alloced( 29, 0, 12, 0 ),
				},
				new TSBox[] {
						alloced( 0, 0, 0, 17 ),
						alloced( 0, 17, 0, 12 ),
						alloced( 0, 29, 0, 12 ),
				}
			);
	}
}
