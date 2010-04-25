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
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTableWithUniques;

public class Test_IncrementalTreeNodeTableWithUniques extends Test_IncrementalTreeNodeTable
{
	// Extend DocViewNodeTable so that the @refViewNode and @unrefViewNode methods are accessible
	private static class TestTable extends IncrementalTreeNodeTableWithUniques
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
		assertEquals( table.size(), 4 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 1 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );
		
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ia } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2 } ), table.get( dd ) );
	}
	
	public void testUnref()
	{
		unrefIncrementalNode( ia );
		unrefIncrementalNode( id2 );
		
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( da ), 1 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( dd ), 1 );
		
		table.clean();

		assertEquals( table.size(), 2 );
		assertEquals( table.getNumDocNodes(), 2 );
		assertEquals( table.getNumIncrementalNodesForDocNode( da ), 0 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 0 );
		
		assertFalse( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertFalse( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( dd ) );
	}

	
	
	public void testUnrefReref()
	{
		unrefIncrementalNode( ia );
		unrefIncrementalNode( id2 );
		refIncrementalNode( ia );
		refIncrementalNode( id2 );
		
		table.clean();

		assertEquals( table.size(), 4 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumIncrementalNodesForDocNode( da ), 1 );
		assertEquals( table.getNumIncrementalNodesForDocNode( dd ), 1 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( da ), 0 );
		assertEquals( table.getNumUnrefedIncrementalNodesForDocNode( dd ), 0 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ia } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ib } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { ic } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2 } ), table.get( dd ) );
		
		table.clean();

		assertEquals( table.size(), 4 );
		assertEquals( table.getNumDocNodes(), 4 );
	}





	public void testReuseUnrefed()
	{
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
		IncrementalTreeNode val = table.getUnrefedIncrementalNodeFor( dd, null );
		refIncrementalNode( val );
		assertSame( val, id2 );
		assertSame( val.getDocNode(), dd );
		
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { id2 } ), table.get( dd ) );
		assertTrue( table.containsKey( dd ) );
		assertEquals( table.size(), 4 );
		
		
		
		// Unref again
		unrefIncrementalNode( id2 );
		assertEquals( table.size(), 3 );
		
		// Take an unused node again
		val = table.getUnrefedIncrementalNodeFor( dd, null );
		refIncrementalNode( val );
		assertSame( val, id2 );
		
		
		// Ensure that no unused nodes remain
		assertSame( table.getUnrefedIncrementalNodeFor( dd, null ), null );
	}
}
