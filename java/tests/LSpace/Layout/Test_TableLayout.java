//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.LSpace.Layout;

import BritefuryJ.LSpace.Layout.LAllocBox;
import BritefuryJ.LSpace.Layout.LReqBox;
import BritefuryJ.LSpace.Layout.TableLayout;

public class Test_TableLayout extends Test_Layout_base
{
	private TableLayout.TablePackingParams pack(int x, int y)
	{
		return new TableLayout.TablePackingParams( x, 1, y, 1 );
	}
	
	private TableLayout.TablePackingParams pack(int x, int y, int colspan, int rowspan)
	{
		return new TableLayout.TablePackingParams( x, colspan, y, rowspan );
	}
	
	
	
	
	//
	//
	// REQUISITION TESTS
	//
	//
	
	private void reqTest(LReqBox children[], TableLayout.TablePackingParams packingParams[], int childAlignmentFlags[],
			int numColumns, int numRows, double columnSpacing, double rowSpacing,
			LReqBox expectedColumnBoxes[], LReqBox expectedRowBoxes[], LReqBox expectedParentBox)
	{
		LReqBox box = new LReqBox();
		LReqBox columnBoxes[] = TableLayout.computeRequisitionX( box, children, packingParams, numColumns, numRows, columnSpacing, rowSpacing );
		LReqBox rowBoxes[] = TableLayout.computeRequisitionY( box, children, packingParams, childAlignmentFlags, numColumns, numRows, columnSpacing, rowSpacing );
		
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
				new LReqBox[] {
						box( 10, 10, 10, 0 ) ,
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),
				},
				new int[] {
						HCENTRE | VCENTRE,
				},
				1, 1, 0.0, 0.0,
				new LReqBox[] {
						box( 10, 10, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
				},
				box( 10, 10, 10, 0 )
			);

	
		// 2x2
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),
						box( 15, 15, 2, 0 ),		box( 4, 4, 4, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
				},
				2, 2, 0.0, 0.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 5, 5, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
				},
				box( 20, 20, 14, 0 )
			);

	
		// 2x2 with spacing
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),
						box( 15, 15, 2, 0 ),		box( 4, 4, 4, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
				},
				2, 2, 1.0, 1.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 5, 5, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
				},
				box( 21, 21, 15, 0 )
			);

	

		
		// 3x3
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),		box( 20, 20, 5, 0 ),
						box( 15, 10, 2, 0 ),		box( 8, 8, 4, 0 ),		box( 10, 10, 4, 0 ),
						box( 5, 5, 4, 0 ),		box( 6, 6, 8, 0 ),		box( 10, 10, 6, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
				},
				3, 3, 0.0, 0.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 8, 8, 0, 0 ),		box( 20, 20, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 43, 43, 22, 0 )
			);

	
	
		// 3x3 with spacing
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),		box( 20, 20, 5, 0 ),
						box( 15, 15, 2, 0 ),		box( 8, 8, 4, 0 ),		box( 10, 10, 4, 0 ),
						box( 5, 5, 4, 0 ),		box( 6, 6, 8, 0 ),		box( 10, 10, 6, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
				},
				3, 3, 1.0, 2.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 8, 8, 0, 0 ),		box( 20, 20, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 45, 45, 26, 0 )
			);

	
	
	
		// 3x3 with a gap
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),		box( 20, 20, 5, 0 ),
						box( 15, 15, 2, 0 ),							box( 10, 10, 4, 0 ),
						box( 5, 5, 4, 0 ),		box( 6, 6, 8, 0 ),		box( 10, 10, 6, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),								pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,						HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
				},
				3, 3, 0.0, 0.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 6, 6, 0, 0 ),		box( 20, 20, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 4, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 41, 41, 22, 0 )
			);

		
		
		
		// 3x3 with a gap and a child with colspan
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),		box( 20, 20, 5, 0 ),
						box( 15, 15, 2, 0 ),		box( 40, 40, 6, 0 ),
						box( 5, 5, 4, 0 ),		box( 10, 10, 8, 0 ),		box( 10, 10, 6, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
				},
				3, 3, 0.0, 0.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 15, 15, 0, 0 ),		box( 25, 25, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 6, 0 ),
						box( 0, 0, 8, 0 ),
				},
				box( 55, 55, 24, 0 )
			);

		
		
		
		// 3x3 with a gap and a child with rowspan
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),		box( 20, 20, 5, 0 ),
						box( 15, 15, 2, 0 ),		box( 8, 8, 20, 0 ),		box( 10, 10, 4, 0 ),
						box( 5, 5, 4, 0 ),							box( 10, 10, 6, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 1, 2 ),		pack( 2, 1 ),
						pack( 0, 2 ),								pack( 2, 2 ),
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,						HCENTRE | VCENTRE,
				},
				3, 3, 0.0, 0.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 8, 8, 0, 0 ),		box( 20, 20, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 9, 0 ),
						box( 0, 0, 11, 0 ),
				},
				box( 43, 43, 30, 0 )
			);

		
		
		
		// 3x3 with caps and a child with colspan and rowspan
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),		box( 20, 20, 5, 0 ),
						box( 15, 15, 2, 0 ),		box( 35, 35, 20, 0 ),		
						box( 5, 5, 4, 0 ),							
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 2, 2 ),		
						pack( 0, 2 ),								
				},
				new int[] {
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,	HCENTRE | VCENTRE,
						HCENTRE | VCENTRE,
				},
				3, 3, 0.0, 0.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 10, 10, 0, 0 ),		box( 25, 25, 0, 0 ),
				},
				new LReqBox[] {
						box( 0, 0, 10, 0 ),
						box( 0, 0, 9, 0 ),
						box( 0, 0, 11, 0 ),
				},
				box( 50, 50, 30, 0 )
			);
		
		
		
		
		// 3x3 with baseline v-alignment
		reqTest(
				new LReqBox[] {
						box( 10, 10, 10, 0 ),		box( 5, 5, 5, 0 ),		box( 20, 20, 5, 0 ),
						rbox( 15, 15, 8, 0, 4 ),	rbox( 8, 8, 8, 0, 2 ),		rbox( 10, 10, 7, 0, 5 ),
						box( 5, 5, 4, 0 ),		box( 6, 6, 8, 0 ),		box( 10, 10, 6, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HCENTRE | VREFY,	HCENTRE | VREFY,	HCENTRE | VREFY,
						HCENTRE | VREFY,	HCENTRE | VREFY,	HCENTRE | VREFY,
						HCENTRE | VREFY,	HCENTRE | VREFY,	HCENTRE | VREFY,
				},
				3, 3, 0.0, 0.0,
				new LReqBox[] {
						box( 15, 15, 0, 0 ),		box( 8, 8, 0, 0 ),		box( 20, 20, 0, 0 ),
				},
				new LReqBox[] {
						rbox( 0, 0, 10, 0, 5 ),
						rbox( 0, 0, 11, 0, 5 ),
						rbox( 0, 0, 8, 0, 4 ),
				},
				box( 43, 43, 29, 0 )
			);
	}



	

	//
	//
	// ALLOCATION TESTS
	//
	//
	
	private void allocTest(LReqBox children[], TableLayout.TablePackingParams packingParams[], int childAlignmentFlags[], int numColumns, int numRows, double columnSpacing, double rowSpacing,
			boolean bColumnExpand, boolean bRowExpand, double allocX, double allocY, LAllocBox expectedChildAllocations[], LAllocBox expectedColAllocations[], LAllocBox expectedRowAllocations[])
	{
		LReqBox box = new LReqBox();
		LAllocBox boxAlloc = new LAllocBox( null );

		LAllocBox childrenAllocs[] = new LAllocBox[children.length];
		for (int i = 0; i < childrenAllocs.length; i++)
		{
			childrenAllocs[i] = new LAllocBox( null );
		}
		

		LReqBox columnBoxes[] = TableLayout.computeRequisitionX( box, children, packingParams, numColumns, numRows, columnSpacing, rowSpacing );
		LAllocBox columnAllocs[] = new LAllocBox[columnBoxes.length];
		for (int i = 0; i < columnAllocs.length; i++)
		{
			columnAllocs[i] = new LAllocBox( null );
		}
		
		boxAlloc.setAllocationX( allocX, allocX );
		TableLayout.allocateX( box, columnBoxes, children, boxAlloc, columnAllocs, childrenAllocs, packingParams, childAlignmentFlags, numColumns, numRows, columnSpacing, rowSpacing, bColumnExpand, bRowExpand );

		LReqBox rowBoxes[] = TableLayout.computeRequisitionY( box, children, packingParams, childAlignmentFlags, numColumns, numRows, columnSpacing, rowSpacing );
		LAllocBox rowAllocs[] = new LAllocBox[rowBoxes.length];
		for (int i = 0; i < rowAllocs.length; i++)
		{
			rowAllocs[i] = new LAllocBox( null );
		}

		boxAlloc.setAllocationY( allocY, allocY * 0.5 );
		TableLayout.allocateY( box, rowBoxes, children, boxAlloc, rowAllocs, childrenAllocs, packingParams, childAlignmentFlags, numColumns, numRows, columnSpacing, rowSpacing, bColumnExpand, bRowExpand );
		
		
		for (int i = 0; i < numColumns; i++)
		{
			assertAllocsEqual( columnAllocs[i], expectedColAllocations[i], "COLUMN ALLOCATION FOR COLUMN " + i );
		}
		
		for (int i = 0; i < numRows; i++)
		{
			assertAllocsEqual( rowAllocs[i], expectedRowAllocations[i], "ROW ALLOCATION FOR ROW " + i );
		}
		
		for (int i = 0; i < children.length; i++)
		{
			assertAllocsEqual( childrenAllocs[i], expectedChildAllocations[i], "CHILD ALLOCATION FOR ROW " + i );
		}
	}



	public void test_allocation()
	{
		// 1x1
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),
				},
				new int[] {
						HLEFT | VTOP,
				},
				1, 1, 0.0, 0.0, false, false,
				10, 10,
				new LAllocBox[] {
						alloc( 0, 0, 10, 10 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 10, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 10 ),
				}
			);

		// 1x1, extra space, left-top
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),
				},
				new int[] {
						HLEFT | VTOP,
				},
				1, 1, 0.0, 0.0, true, true,
				20, 20,
				new LAllocBox[] {
						alloc( 0, 0, 10, 10 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, centre-centre
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),
				},
				new int[] {
						HCENTRE | VCENTRE,
				},
				1, 1, 0.0, 0.0, true, true,
				20, 20,
				new LAllocBox[] {
						alloc( 5, 5, 10, 10 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, right-bottom
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),
				},
				new int[] {
						HRIGHT | VBOTTOM,
				},
				1, 1, 0.0, 0.0, true, true,
				20, 20,
				new LAllocBox[] {
						alloc( 10, 10, 10, 10 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);

		// 1x1, extra space, expand-expand
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ) ,
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),
				},
				new int[] {
						HEXPAND | VEXPAND,
				},
				1, 1, 0.0, 0.0, true, true,
				20, 20,
				new LAllocBox[] {
						alloc( 0, 0, 20, 20 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 20, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 20 ),
				}
			);


	
		// 3x3, extra space, no expand, top-left alignment
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HLEFT | VTOP,			HLEFT | VTOP,			HLEFT | VTOP,
						HLEFT | VTOP,			HLEFT | VTOP,			HLEFT | VTOP,
						HLEFT | VTOP,			HLEFT | VTOP,			HLEFT | VTOP,
				},
				3, 3, 0.0, 0.0, false, false,
				40, 35,
				new LAllocBox[] {
						alloc( 0, 0, 10, 10 ),		alloc( 10, 0, 5, 5 ),		alloc( 15, 0, 5, 5 ),
						alloc( 0, 10, 5, 2 ),		alloc( 10, 10, 4, 5 ),		alloc( 15, 10, 10, 4 ),
						alloc( 0, 15, 5, 2 ),		alloc( 10, 15, 2, 2 ),		alloc( 15, 15, 10, 5 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 10, 0 ),		alloc( 10, 0, 5, 0 ),		alloc( 15, 0, 10, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 10 ),
						alloc( 0, 10, 0, 5 ),
						alloc( 0, 15, 0, 5 ),
				}
			);

	
	


		
		// 3x3, extra space, no expand, expand-expand alignment
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
				},
				3, 3, 0.0, 0.0, false, false,
				40, 35,
				new LAllocBox[] {
						alloc( 0, 0, 10, 10 ),		alloc( 10, 0, 5, 10 ),		alloc( 15, 0, 10, 10 ),
						alloc( 0, 10, 10, 5 ),		alloc( 10, 10, 5, 5 ),		alloc( 15, 10, 10, 5 ),
						alloc( 0, 15, 10, 5 ),		alloc( 10, 15, 5, 5 ),		alloc( 15, 15, 10, 5 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 10, 0 ),		alloc( 10, 0, 5, 0 ),		alloc( 15, 0, 10, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 10 ),
						alloc( 0, 10, 0, 5 ),
						alloc( 0, 15, 0, 5 ),
				}
			);

	
	


		
		// 3x3, extra space, expand in x and y, top-left alignment
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HLEFT | VTOP,			HLEFT | VTOP,			HLEFT | VTOP,
						HLEFT | VTOP,			HLEFT | VTOP,			HLEFT | VTOP,
						HLEFT | VTOP,			HLEFT | VTOP,			HLEFT | VTOP,
				},
				3, 3, 0.0, 0.0, true, true,
				40, 35,
				new LAllocBox[] {
						alloc( 0, 0, 10, 10 ),		alloc( 15, 0, 5, 5 ),		alloc( 25, 0, 5, 5 ),
						alloc( 0, 15, 5, 2 ),		alloc( 15, 15, 4, 5 ),		alloc( 25, 15, 10, 4 ),
						alloc( 0, 25, 5, 2 ),		alloc( 15, 25, 2, 2 ),		alloc( 25, 25, 10, 5 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 15, 0 ),		alloc( 15, 0, 10, 0 ),		alloc( 25, 0, 15, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 15 ),
						alloc( 0, 15, 0, 10 ),
						alloc( 0, 25, 0, 10 ),
				}
			);

	
	

		
		// 3x3, extra space, expand in x and y, expand-expand alignment
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 2, 0 ),		box( 4, 0, 5, 0 ),		box( 10, 0, 4, 0 ),
						box( 5, 0, 2, 0 ),		box( 2, 0, 2, 0 ),		box( 10, 0, 5, 0 ),
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1 ),			pack( 2, 1 ),
						pack( 0, 2 ),			pack( 1, 2 ),			pack( 2, 2 ),
				},
				new int[] {
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
				},
				3, 3, 0.0, 0.0, true, true,
				40, 35,
				new LAllocBox[] {
						alloc( 0, 0, 15, 15 ),		alloc( 15, 0, 10, 15 ),		alloc( 25, 0, 15, 15 ),
						alloc( 0, 15, 15, 10 ),		alloc( 15, 15, 10, 10 ),	alloc( 25, 15, 15, 10 ),
						alloc( 0, 25, 15, 10 ),		alloc( 15, 25, 10, 10 ),	alloc( 25, 25, 15, 10 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 15, 0 ),		alloc( 15, 0, 10, 0 ),		alloc( 25, 0, 15, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 15 ),
						alloc( 0, 15, 0, 10 ),
						alloc( 0, 25, 0, 10 ),
				}
			);

	

		
		// 3x3, gap, colspan and rowspan, extra space, expand in x and y, expand-expand alignment
		allocTest(
				new LReqBox[] {
						box( 10, 0, 10, 0 ),		box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 5, 0 ),		box( 5, 0, 5, 0 ),
						box( 5, 0, 5, 0 ),		
				},
				new TableLayout.TablePackingParams[] {
						pack( 0, 0 ),			pack( 1, 0 ),			pack( 2, 0 ),
						pack( 0, 1 ),			pack( 1, 1, 2, 2 ),
						pack( 0, 2 ),			
				},
				new int[] {
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
						HEXPAND | VEXPAND,		HEXPAND | VEXPAND,
						HEXPAND | VEXPAND,	
				},
				3, 3, 0.0, 0.0, true, true,
				35, 35,
				new LAllocBox[] {
						alloc( 0, 0, 15, 15 ),		alloc( 15, 0, 10, 15 ),		alloc( 25, 0, 10, 15 ),
						alloc( 0, 15, 15, 10 ),		alloc( 15, 15, 20, 20 ),
						alloc( 0, 25, 15, 10 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 15, 0 ),		alloc( 15, 0, 10, 0 ),		alloc( 25, 0, 10, 0 ),
				},
				new LAllocBox[] {
						alloc( 0, 0, 0, 15 ),
						alloc( 0, 15, 0, 10 ),
						alloc( 0, 25, 0, 10 ),
				}
			);
	}
}
