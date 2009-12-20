//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.IncrementalTree;

import java.util.Arrays;

import BritefuryJ.DocModel.DMList;
import BritefuryJ.IncrementalTree.IncrementalTree;
import BritefuryJ.IncrementalTree.IncrementalTreeNode;
import BritefuryJ.IncrementalTree.IncrementalTreeNodeTable;
import junit.framework.TestCase;

public class Test_IncrementalTreeNodeTable extends TestCase
{
	// Extend DocViewNodeTable so that the @refViewNode and @unrefViewNode methods are accessible
	private static class TestTable extends IncrementalTreeNodeTable
	{
		protected void refViewNode(IncrementalTreeNode node)
		{
			super.refViewNode( node );
		}

		protected void unrefViewNode(IncrementalTreeNode node)
		{
			super.unrefViewNode( node );
		}
	}
	
	
	private DMList da, db, dc, dd;
	private TestTable table;
	private IncrementalTreeNode va, vb, vc, vd1, vd2;
	private IncrementalTree view;
	

	
	public void setUp()
	{
		dd = new DMList( Arrays.asList( new Object[] { "d" } ) );
		dc = new DMList( Arrays.asList( new Object[] { dd } ) );
		db = new DMList( Arrays.asList( new Object[] { dd } ) );
		da = new DMList( Arrays.asList( new Object[] { db, dc } ) );
		
		
		view = new IncrementalTree( da, null );
		
		va = new IncrementalTreeNode( view, da, null );
		vb = new IncrementalTreeNode( view, db, null );
		vc = new IncrementalTreeNode( view, dc, null );
		vd1 = new IncrementalTreeNode( view, dd, null );
		vd2 = new IncrementalTreeNode( view, dd, null );
		
		
		table = new TestTable();

		table.put( da, va );
		table.put( db, vb );
		table.put( dc, vc );
		table.put( dd, vd1 );
		table.put( dd, vd2 );
	}
	
	public void tearDown()
	{
		da = db = dc = dd = null;
		
		table = null;

		view = null;
		va = vb = vc = vd1 = vd2 = null;
	}
	
	
	
	public void testAccessors()
	{
		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 2 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );
		
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { va } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vd1, vd2 } ), table.get( dd ) );
	}
	
	public void testRemove()
	{
		table.remove( va );
		table.remove( vd1 );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vd2 } ), table.get( dd ) );
	}

	public void testPut()
	{
		IncrementalTreeNode vx = new IncrementalTreeNode( view, da, null  );
		IncrementalTreeNode vy = new IncrementalTreeNode( view, dd, null );
		
		table.put( da, vx );
		table.put( dd, vy );

		assertEquals( table.size(), 7 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 3 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] { va, vx } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vd1, vd2, vy } ), table.get( dd ) );
	}

	public void testViewGC()
	{
		va = null;
		vd1 = null;
		System.gc();
		
		// Need to call DocViewNodeTable.clean() in order to clean away all weak-refs, etc
		table.clean();

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vd2 } ), table.get( dd ) );
	}
	
	
	public void testUnref()
	{
		table.unrefViewNode( va );
		table.unrefViewNode( vd1 );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( da ), 0 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 1 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( da ), 1 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vd2 } ), table.get( dd ) );
		
		table.clean();

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
	}

	
	
	public void testUnrefReref()
	{
		table.unrefViewNode( va );
		table.unrefViewNode( vd1 );
		table.refViewNode( va );
		table.refViewNode( vd1 );

		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( da ), 1 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 2 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( da ), 0 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( dd ), 0 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] { va } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vd2, vd1 } ), table.get( dd ) );
		
		table.clean();

		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
	}





	public void testReuseUnrefed()
	{
		table.remove( vd1 );
		table.unrefViewNode( vd2 );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 0 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( dd ), 1 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertFalse( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new IncrementalTreeNode[] { va } ), table.get( da ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new IncrementalTreeNode[] {} ), table.get( dd ) );
		
		
		// Reuse
		IncrementalTreeNode val = table.takeUnusedViewNodeFor( dd, null );
		assertSame( val, vd2 );
		assertSame( val.getDocNode(), dd );
		
		assertEquals( Arrays.asList( new IncrementalTreeNode[] { vd2 } ), table.get( dd ) );
		assertTrue( table.containsKey( dd ) );
		assertEquals( table.size(), 4 );
		
		
		
		// Unref again
		table.unrefViewNode( vd2 );
		assertEquals( table.size(), 3 );
		
		// Take an unused node again
		val = table.takeUnusedViewNodeFor( dd, null );
		assertSame( val, vd2 );
		
		
		// Ensure that no unused nodes remain
		assertSame( table.takeUnusedViewNodeFor( dd, null ), null );
	}
}
