//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.IncrementalTree;

import java.util.Arrays;

import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTable;
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTableWithDuplicates;

public class Test_IncrementalTreeNodeTableWithDuplicates extends Test_IncrementalTreeNodeTable
{
	// Extend DocViewNodeTable so that the @refViewNode and @unrefViewNode methods are accessible
	private static class TestTable extends IncrementalTreeNodeTableWithDuplicates
	{
		protected void refIncrementalNode(IncrementalTreeNode node)
		{
			super.refIncrementalNode( node );
		}

		protected void unrefIncrementalNode(IncrementalTreeNode node)
		{
			super.unrefIncrementalNode( node );
		}
	}
	
	
	protected IncrementalTreeNodeTable createTable()
	{
		return new TestTable();
	}

	protected void refIncrementalNode(IncrementalTreeNode node)
	{
		((TestTable)table).refIncrementalNode( node );
	}
	
	protected void unrefIncrementalNode(IncrementalTreeNode node)
	{
		((TestTable)table).unrefIncrementalNode( node );
	}



	
	
	
	public void testAccessors()
	{
		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 2 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );
		
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ia } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id1, id2 } ), table.get( dd ) );
	}
	
	public void testRemove()
	{
		table.remove( ia );
		table.remove( id1 );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2 } ), table.get( dd ) );
	}

	public void testPut()
	{
		IncrementalTreeNode ix = new IncrementalTreeNode( tree, da, null  );
		IncrementalTreeNode iy = new IncrementalTreeNode( tree, dd, null );
		
		table.put( da, ix );
		table.put( dd, iy );

		assertEquals( table.size(), 7 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 3 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ia, ix } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id1, id2, iy } ), table.get( dd ) );
	}

	public void testViewGC()
	{
		ia = null;
		id1 = null;
		System.gc();
		
		// Need to call DocViewNodeTable.clean() in order to clean away all weak-refs, etc
		table.clean();

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2 } ), table.get( dd ) );
	}
	
	
	public void testUnref()
	{
		unrefIncrementalNode( ia );
		unrefIncrementalNode( id1 );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumIncrementalNodesForDocNode( da ), 0 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 1 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( da ), 1 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2 } ), table.get( dd ) );
		
		table.clean();

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
	}

	
	
	public void testUnrefReref()
	{
		unrefIncrementalNode( ia );
		unrefIncrementalNode( id1 );
		refIncrementalNode( ia );
		refIncrementalNode( id1 );

		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumIncrementalNodesForDocNode( da ), 1 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 2 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( da ), 0 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( dd ), 0 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ia } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2, id1 } ), table.get( dd ) );
		
		table.clean();

		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
	}





	public void testReuseUnrefed()
	{
		table.remove( id1 );
		unrefIncrementalNode( id2 );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 0 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( dd ), 1 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertFalse( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ia } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( dd ) );
		
		
		// Reuse
		IncrementalTreeNode val = table.takeUnusedIncrementalNodeFor( dd, null );
		assertSame( val, id2 );
		assertSame( val.getDocNode(), dd );
		
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2 } ), table.get( dd ) );
		assertTrue( table.containsKey( dd ) );
		assertEquals( table.size(), 4 );
		
		
		
		// Unref again
		unrefIncrementalNode( id2 );
		assertEquals( table.size(), 3 );
		
		// Take an unused node again
		val = table.takeUnusedIncrementalNodeFor( dd, null );
		assertSame( val, id2 );
		
		
		// Ensure that no unused nodes remain
		assertSame( table.takeUnusedIncrementalNodeFor( dd, null ), null );
	}
}
