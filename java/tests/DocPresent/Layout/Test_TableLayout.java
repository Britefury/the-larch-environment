//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocPresent.Layout;

import BritefuryJ.DocPresent.Layout.HAlignment;
import BritefuryJ.DocPresent.Layout.LBox;
import BritefuryJ.DocPresent.Layout.TableLayout;
import BritefuryJ.DocPresent.Layout.TablePackingParams;
import BritefuryJ.DocPresent.Layout.VAlignment;

public class Test_TableLayout extends Test_Layout_base
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
	
	
	
	
	//
	//
	// REQUISITION TESTS
	//
	//
	
	private void reqTest(LBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingX, double spacingY, boolean bExpandX, boolean bExpandY,
			HAlignment colAlignment, VAlignment rowAlignment, LBox expectedColumnBoxes[], LBox expectedRowBoxes[], LBox expectedParentBox)
	{
		LBox box = new LBox();
		LBox columnBoxes[] = TableLayout.computeRequisitionX( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		LBox rowBoxes[] = TableLayout.computeRequisitionY( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		
		for (int i = 0; i < numColumns; i++)
		{
			assertBoxesEqual( columnBoxes[i], expectedColumnBoxes[i], "COLUMN BOX FOR COLUMN " + i );
		}
		
		for (int i = 0; i < numRows; i++)
		{
			assertBoxesEqual( rowBoxes[i], expectedRowBoxes[i], "ROW BOX FOR ROW " + i );
		}

		assertBoxesEqual( box, expectedParentBox, "PARENT BOX" );
	}
	
	
	public void test_requisition()
	{
		// 1x1
		reqTest(
				new LBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
				new LBox[] {
						box( 10, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
				},
				box( 10, 0, 10, 0 )
			);

	
		// 2x2
		reqTest(
				new LBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 4, 0, 4, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),
				},
				2, 2, 0.0, 0.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 5, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
				},
				box( 20, 0, 14, 0 )
			);

	
		// 2x2 with spacing
		reqTest(
				new LBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),
						box( 15, 0, 2, 0 ),		box( 4, 0, 4, 0 ),
				},
				new TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),
				},
				2, 2, 1.0, 1.0, false, false, HAlignment.CENTRE, VAlignment.CENTRE,
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 5, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
				},
				box( 21, 0, 15, 0 )
			);

	

		
		// 3x3
		reqTest(
				new LBox[] {
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
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 43, 0, 22, 0 )
			);

	
	
		// 3x3 with spacing
		reqTest(
				new LBox[] {
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
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 45, 0, 26, 0 )
			);

	
	
	
		// 3x3 with a gap
		reqTest(
				new LBox[] {
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
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 6, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 41, 0, 22, 0 )
			);

		
		
		
		// 3x3 with a gap and a child with colspan
		reqTest(
				new LBox[] {
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
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 15, 0, 0, 0 ),		box( 25, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 6, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 55, 0, 24, 0 )
			);

		
		
		
		// 3x3 with a gap and a child with rowspan
		reqTest(
				new LBox[] {
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
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 9, 0 ),
						box( 0, 0, 11, 0 ),
				},
				box( 43, 0, 30, 0 )
			);

		
		
		
		// 3x3 with caps and a child with colspan and rowspan
		reqTest(
				new LBox[] {
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
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 10, 0, 0, 0 ),		box( 25, 0, 0, 0 ),
				},
				new LBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 9, 0 ),
						box( 0, 0, 11, 0 ),
				},
				box( 50, 0, 30, 0 )
			);
		
		
		
		
		// 3x3 with baseline v-alignment
		reqTest(
				new LBox[] {
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
				new LBox[] {
						box( 15, 0, 0, 0 ),		box( 8, 0, 0, 0 ),		box( 20, 0, 0, 0 ),
				},
				new LBox[] {
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
	
	private void allocTest(LBox children[], TablePackingParams packingParams[], int numColumns, int numRows, double spacingX, double spacingY, boolean bExpandX, boolean bExpandY,
			HAlignment colAlignment, VAlignment rowAlignment,
			double allocX, double allocY, LBox expectedChildAllocations[], LBox expectedColAllocations[], LBox expectedRowAllocations[])
	{
		LBox expectedChildren[] = new LBox[children.length];
		for (int i = 0; i < children.length; i++)
		{
			expectedChildren[i] = children[i].copy();
			expectedChildren[i].setAllocationFrom( expectedChildAllocations[i] );
		}

		
		LBox box = new LBox();

		LBox columnBoxes[] = TableLayout.computeRequisitionX( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		
		box.setAllocationX( allocX );
		TableLayout.allocateX( box, columnBoxes, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		LBox rowBoxes[] = TableLayout.computeRequisitionY( box, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment);
		box.setAllocationY( allocY );
		TableLayout.allocateY( box, rowBoxes, children, packingParams, numColumns, numRows, spacingX, spacingY, bExpandX, bExpandY, colAlignment, rowAlignment );
		
		
		for (int i = 0; i < numColumns; i++)
		{
			LBox expectedColumnBox = columnBoxes[i].copy();
			expectedColumnBox.setAllocationFrom( expectedColAllocations[i] );
			assertBoxesEqual( columnBoxes[i], expectedColumnBox, "COLUMN ALLOCATION FOR COLUMN " + i );
		}
		
		for (int i = 0; i < numRows; i++)
		{
			LBox expectedRowBox = rowBoxes[i].copy();
			expectedRowBox.setAllocationFrom( expectedRowAllocations[i] );
			assertBoxesEqual( rowBoxes[i], expectedRowBox, "ROW ALLOCATION FOR ROW " + i );
		}
		
		for (int i = 0; i < children.length; i++)
		{
			assertBoxesEqual( children[i], expectedChildren[i], "CHILD ALLOCATION FOR ROW " + i );
		}
		
		box.setAllocationX( 0.0 );
		box.setAllocationY( 0.0 );
	}



	public void test_allocation()
	{
		// 1x1
		allocTest(
				new LBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, false, false, HAlignment.LEFT, VAlignment.TOP,
				10, 10,
				new LBox[] {
						alloc( 0, 0, 10, 10 ),
				},
				new LBox[] {
						alloc( 0, 0, 10, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 10 ),
				}
			);

		// 1x1, extra space, left-top
		allocTest(
				new LBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.LEFT, VAlignment.TOP,
				20, 20,
				new LBox[] {
						alloc( 0, 0, 10, 10 ),
				},
				new LBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, centre-centre
		allocTest(
				new LBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.CENTRE, VAlignment.CENTRE,
				20, 20,
				new LBox[] {
						alloc( 5, 5, 10, 10 ),
				},
				new LBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, right-bottom
		allocTest(
				new LBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.RIGHT, VAlignment.BOTTOM,
				20, 20,
				new LBox[] {
						alloc( 10, 10, 10, 10 ),
				},
				new LBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, expand-expand
		allocTest(
				new LBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TablePackingParams[] {
						pack( 0, 0 ),
				},
				1, 1, 0.0, 0.0, true, true, HAlignment.EXPAND, VAlignment.EXPAND,
				20, 20,
				new LBox[] {
						alloc( 0, 0, 20, 20 ),
				},
				new LBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);


	
		// 3x3, extra space, no expand, top-left alignment
		allocTest(
				new LBox[] {
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
				new LBox[] {
						alloc( 0, 0, 10, 10 ),	alloc( 10, 0, 5, 5 ),		alloc( 15, 0, 5, 5 ),
						alloc( 0, 10, 5, 2 ),		alloc( 10, 10, 4, 5 ),	alloc( 15, 10, 10, 4 ),
						alloc( 0, 15, 5, 2 ),		alloc( 10, 15, 2, 2 ),	alloc( 15, 15, 10, 5 ),
				},
				new LBox[] {
						alloc( 0, 0, 10, 0 ),		alloc( 10, 0, 5, 0 ),		alloc( 15, 0, 10, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 10 ),
						alloc( 0, 10, 0, 5 ),
						alloc( 0, 15, 0, 5 ),
				}
			);

	
	


		
		// 3x3, extra space, no expand, expand-expand alignment
		allocTest(
				new LBox[] {
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
				new LBox[] {
						alloc( 0, 0, 10, 10 ),	alloc( 10, 0, 5, 10 ),	alloc( 15, 0, 10, 10 ),
						alloc( 0, 10, 10, 5 ),	alloc( 10, 10, 5, 5 ),	alloc( 15, 10, 10, 5 ),
						alloc( 0, 15, 10, 5 ),	alloc( 10, 15, 5, 5 ),	alloc( 15, 15, 10, 5 ),
				},
				new LBox[] {
						alloc( 0, 0, 10, 0 ),		alloc( 10, 0, 5, 0 ),		alloc( 15, 0, 10, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 10 ),
						alloc( 0, 10, 0, 5 ),
						alloc( 0, 15, 0, 5 ),
				}
			);

	
	


		
		// 3x3, extra space, expand in x and y, top-left alignment
		allocTest(
				new LBox[] {
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
				new LBox[] {
						alloc( 0, 0, 10, 10 ),	alloc( 15, 0, 5, 5 ),		alloc( 25, 0, 5, 5 ),
						alloc( 0, 15, 5, 2 ),		alloc( 15, 15, 4, 5 ),	alloc( 25, 15, 10, 4 ),
						alloc( 0, 25, 5, 2 ),		alloc( 15, 25, 2, 2 ),	alloc( 25, 25, 10, 5 ),
				},
				new LBox[] {
						alloc( 0, 0, 15, 0 ),		alloc( 15, 0, 10, 0 ),	alloc( 25, 0, 15, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 15 ),
						alloc( 0, 15, 0, 10 ),
						alloc( 0, 25, 0, 10 ),
				}
			);

	
	

		
		// 3x3, extra space, expand in x and y, expand-expand alignment
		allocTest(
				new LBox[] {
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
				new LBox[] {
						alloc( 0, 0, 15, 15 ),	alloc( 15, 0, 10, 15 ),	alloc( 25, 0, 15, 15 ),
						alloc( 0, 15, 15, 10 ),	alloc( 15, 15, 10, 10 ),	alloc( 25, 15, 15, 10 ),
						alloc( 0, 25, 15, 10 ),	alloc( 15, 25, 10, 10 ),	alloc( 25, 25, 15, 10 ),
				},
				new LBox[] {
						alloc( 0, 0, 15, 0 ),		alloc( 15, 0, 10, 0 ),	alloc( 25, 0, 15, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 15 ),
						alloc( 0, 15, 0, 10 ),
						alloc( 0, 25, 0, 10 ),
				}
			);

	

		
		// 3x3, gap, colspan and rowspan, extra space, expand in x and y, expand-expand alignment
		allocTest(
				new LBox[] {
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
				new LBox[] {
						alloc( 0, 0, 15, 15 ),	alloc( 15, 0, 10, 15 ),	alloc( 25, 0, 10, 15 ),
						alloc( 0, 15, 15, 10 ),	alloc( 15, 15, 20, 20 ),
						alloc( 0, 25, 15, 10 ),
				},
				new LBox[] {
						alloc( 0, 0, 15, 0 ),		alloc( 15, 0, 10, 0 ),	alloc( 25, 0, 10, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 15 ),
						alloc( 0, 15, 0, 10 ),
						alloc( 0, 25, 0, 10 ),
				}
			);

	

		
		// 3x3, gap, colspan and rowspan, padding, extra space, expand in x and y, expand-expand alignment
		allocTest(
				new LBox[] {
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
				new LBox[] {
						alloc( 1, 1, 15, 15 ),	alloc( 18, 1, 10, 15 ),	alloc( 30, 1, 10, 15 ),
						alloc( 1, 18, 15, 10 ),	alloc( 18, 18, 22, 22 ),
						alloc( 1, 30, 15, 10 ),
				},
				new LBox[] {
						alloc( 0, 0, 17, 0 ),		alloc( 17, 0, 12, 0 ),	alloc( 29, 0, 12, 0 ),
				},
				new LBox[] {
						alloc( 0, 0, 0, 17 ),
						alloc( 0, 17, 0, 12 ),
						alloc( 0, 29, 0, 12 ),
				}
			);
	}
}
