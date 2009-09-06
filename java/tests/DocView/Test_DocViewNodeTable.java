//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocView;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.DocView.DocViewNodeTable;

public class Test_DocViewNodeTable extends TestCase
{
	// Extend DocViewNodeTable so that the @refViewNode and @unrefViewNode methods are accessible
	private static class TestTable extends DocViewNodeTable
	{
		protected void refViewNode(DVNode node)
		{
			super.refViewNode( node );
		}

		protected void unrefViewNode(DVNode node)
		{
			super.unrefViewNode( node );
		}
	}
	
	
	private DMList da, db, dc, dd;
	private TestTable table;
	private DVNode va, vb, vc, vd1, vd2;
	private DocView view;
	

	
	public void setUp()
	{
		dd = new DMList( Arrays.asList( new Object[] { "d" } ) );
		dc = new DMList( Arrays.asList( new Object[] { dd } ) );
		db = new DMList( Arrays.asList( new Object[] { dd } ) );
		da = new DMList( Arrays.asList( new Object[] { db, dc } ) );
		
		
		view = new DocView( da, null );
		
		va = new DVNode( view, da, null );
		vb = new DVNode( view, db, null );
		vc = new DVNode( view, dc, null );
		vd1 = new DVNode( view, dd, null );
		vd2 = new DVNode( view, dd, null );
		
		
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
		
		assertEquals( Arrays.asList( new DVNode[] { va } ), table.get( da ) );
		assertEquals( Arrays.asList( new DVNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new DVNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new DVNode[] { vd1, vd2 } ), table.get( dd ) );
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

		assertEquals( Arrays.asList( new DVNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new DVNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new DVNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new DVNode[] { vd2 } ), table.get( dd ) );
	}

	public void testPut()
	{
		DVNode vx = new DVNode( view, da, null  );
		DVNode vy = new DVNode( view, dd, null );
		
		table.put( da, vx );
		table.put( dd, vy );

		assertEquals( table.size(), 7 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 3 );
		
		assertTrue( table.containsKey( da ) );
		assertTrue( table.containsKey( db ) );
		assertTrue( table.containsKey( dc ) );
		assertTrue( table.containsKey( dd ) );

		assertEquals( Arrays.asList( new DVNode[] { va, vx } ), table.get( da ) );
		assertEquals( Arrays.asList( new DVNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new DVNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new DVNode[] { vd1, vd2, vy } ), table.get( dd ) );
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

		assertEquals( Arrays.asList( new DVNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new DVNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new DVNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new DVNode[] { vd2 } ), table.get( dd ) );
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

		assertEquals( Arrays.asList( new DVNode[] {} ), table.get( da ) );
		assertEquals( Arrays.asList( new DVNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new DVNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new DVNode[] { vd2 } ), table.get( dd ) );
		
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

		assertEquals( Arrays.asList( new DVNode[] { va } ), table.get( da ) );
		assertEquals( Arrays.asList( new DVNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new DVNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new DVNode[] { vd2, vd1 } ), table.get( dd ) );
		
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

		assertEquals( Arrays.asList( new DVNode[] { va } ), table.get( da ) );
		assertEquals( Arrays.asList( new DVNode[] { vb } ), table.get( db ) );
		assertEquals( Arrays.asList( new DVNode[] { vc } ), table.get( dc ) );
		assertEquals( Arrays.asList( new DVNode[] {} ), table.get( dd ) );
		
		
		// Reuse
		DVNode val = table.takeUnusedViewNodeFor( dd, null );
		assertSame( val, vd2 );
		assertSame( val.getDocNode(), dd );
		
		assertEquals( Arrays.asList( new DVNode[] { vd2 } ), table.get( dd ) );
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
