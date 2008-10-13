//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocView;

import java.util.Arrays;

import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocTree.DocTree;
import BritefuryJ.DocTree.DocTreeList;
import BritefuryJ.DocView.DVNode;
import BritefuryJ.DocView.DocView;
import BritefuryJ.DocView.DocViewNodeTable;

import junit.framework.TestCase;

public class Test_DocViewNodeTable extends TestCase
{
	private DMList da, db, dc, dd;
	private DocTreeList ta, tb, tc, tbd, tcd;
	private DocTree tree;
	private DocViewNodeTable table;
	private DVNode va, vb, vc, vbd, vcd;
	private DocView view;
	

	
	public void setUp()
	{
		dd = new DMList( Arrays.asList( new Object[] { "d" } ) );
		dc = new DMList( Arrays.asList( new Object[] { dd } ) );
		db = new DMList( Arrays.asList( new Object[] { dd } ) );
		da = new DMList( Arrays.asList( new Object[] { db, dc } ) );
		
		
		tree = new DocTree();
		ta = (DocTreeList)tree.treeNode( da );
		tb = (DocTreeList)ta.get( 0 );
		tc = (DocTreeList)ta.get( 1 );
		tbd = (DocTreeList)tb.get( 0 );
		tcd = (DocTreeList)tc.get( 0 );
		
		
		view = new DocView( tree, ta, null, null );
		
		va = new DVNode( view, ta, null );
		vb = new DVNode( view, tb, null );
		vc = new DVNode( view, tc, null );
		vbd = new DVNode( view, tbd, null );
		vcd = new DVNode( view, tcd, null );
		
		
		table = new DocViewNodeTable();

		table.put( ta, va );
		table.put( tb, vb );
		table.put( tc, vc );
		table.put( tbd, vbd );
		table.put( tcd, vcd );
	}
	
	public void tearDown()
	{
		da = db = dc = dd = null;
		
		tree = null;
		ta = tb = tc = tbd = tcd = null;
		
		table = null;

		view = null;
		va = vb = vc = vbd = vcd = null;
	}
	
	
	
	public void testAccessors()
	{
		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 2 );
		
		assertTrue( table.containsKey( ta ) );
		assertTrue( table.containsKey( tb ) );
		assertTrue( table.containsKey( tc ) );
		assertTrue( table.containsKey( tbd ) );
		assertTrue( table.containsKey( tcd ) );

		assertSame( table.get( ta ), va );
		assertSame( table.get( tb ), vb );
		assertSame( table.get( tc ), vc );
		assertSame( table.get( tbd ), vbd );
		assertSame( table.get( tcd ), vcd );
	}
	
	public void testRemove()
	{
		table.remove( ta );
		table.remove( tbd );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( ta ) );
		assertTrue( table.containsKey( tb ) );
		assertTrue( table.containsKey( tc ) );
		assertFalse( table.containsKey( tbd ) );
		assertTrue( table.containsKey( tcd ) );

		assertSame( table.get( ta ), null );
		assertSame( table.get( tb ), vb );
		assertSame( table.get( tc ), vc );
		assertSame( table.get( tbd ), null );
		assertSame( table.get( tcd ), vcd );
	}

	public void testPut()
	{
		DVNode vx = new DVNode( view, ta, null  );
		DVNode vy = new DVNode( view, tbd, null );
		
		table.put( ta, vx );
		table.put( tbd, vy );

		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 2 );
		
		assertTrue( table.containsKey( ta ) );
		assertTrue( table.containsKey( tb ) );
		assertTrue( table.containsKey( tc ) );
		assertTrue( table.containsKey( tbd ) );
		assertTrue( table.containsKey( tcd ) );

		assertSame( table.get( ta ), vx );
		assertSame( table.get( tb ), vb );
		assertSame( table.get( tc ), vc );
		assertSame( table.get( tbd ), vy );
		assertSame( table.get( tcd ), vcd );
	}

	public void testGC()
	{
		va = null;
		vbd = null;
		System.gc();
		
		// Need to call DocViewNodeTable.clean() in order to clean away all weak-refs, etc
		table.clean();

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( ta ) );
		assertTrue( table.containsKey( tb ) );
		assertTrue( table.containsKey( tc ) );
		assertFalse( table.containsKey( tbd ) );
		assertTrue( table.containsKey( tcd ) );

		assertSame( table.get( ta ), null );
		assertSame( table.get( tb ), vb );
		assertSame( table.get( tc ), vc );
		assertSame( table.get( tbd ), null );
		assertSame( table.get( tcd ), vcd );
	}
	
	
	public void testUnref()
	{
		table.unrefViewNode( va );
		table.unrefViewNode( vbd );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( da ), 0 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 1 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( da ), 1 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( dd ), 1 );
		
		assertFalse( table.containsKey( ta ) );
		assertTrue( table.containsKey( tb ) );
		assertTrue( table.containsKey( tc ) );
		assertFalse( table.containsKey( tbd ) );
		assertTrue( table.containsKey( tcd ) );

		assertSame( table.get( ta ), null );
		assertSame( table.get( tb ), vb );
		assertSame( table.get( tc ), vc );
		assertSame( table.get( tbd ), null );
		assertSame( table.get( tcd ), vcd );
		
		table.clearUnused();

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 3 );
	}

	
	
	public void testUnrefReref()
	{
		table.unrefViewNode( va );
		table.unrefViewNode( vbd );
		table.refViewNode( va );
		table.refViewNode( vbd );

		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( da ), 1 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 2 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( da ), 0 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( dd ), 0 );
		
		assertTrue( table.containsKey( ta ) );
		assertTrue( table.containsKey( tb ) );
		assertTrue( table.containsKey( tc ) );
		assertTrue( table.containsKey( tbd ) );
		assertTrue( table.containsKey( tcd ) );

		assertSame( table.get( ta ), va );
		assertSame( table.get( tb ), vb );
		assertSame( table.get( tc ), vc );
		assertSame( table.get( tbd ), vbd );
		assertSame( table.get( tcd ), vcd );
		
		table.clearUnused();

		assertEquals( table.size(), 5 );
		assertEquals( table.getNumDocNodes(), 4 );
	}





	public void testReuseUnrefed()
	{
		table.remove( tbd );
		table.unrefViewNode( vcd );

		assertEquals( table.size(), 3 );
		assertEquals( table.getNumDocNodes(), 4 );
		assertEquals( table.getNumViewNodesForDocNode( dd ), 0 );
		assertEquals( table.getNumUnrefedViewNodesForDocNode( dd ), 1 );
		
		assertTrue( table.containsKey( ta ) );
		assertTrue( table.containsKey( tb ) );
		assertTrue( table.containsKey( tc ) );
		assertFalse( table.containsKey( tbd ) );
		assertFalse( table.containsKey( tcd ) );

		assertSame( table.get( ta ), va );
		assertSame( table.get( tb ), vb );
		assertSame( table.get( tc ), vc );
		assertSame( table.get( tbd ), null );
		assertSame( table.get( tcd ), null );
		
		
		// Reuse
		DVNode val = table.takeUnusedViewNodeFor( tcd );
		assertSame( val, vcd );
		assertSame( val.getDocNode(), dd );
		assertSame( val.getTreeNode(), tcd );
		
		assertSame( table.get( tcd ), vcd );
		assertFalse( table.containsKey( tbd ) );
		assertTrue( table.containsKey( tcd ) );
		assertEquals( table.size(), 4 );
		
		
		
		// Unref again
		table.unrefViewNode( vcd );
		assertEquals( table.size(), 3 );
		
		
		// Reuse for a different key this time
		val = table.takeUnusedViewNodeFor( tbd );
		assertSame( val, vcd );					// This is the DVNode vcd
		assertSame( val.getDocNode(), dd );			// dd is the doc-node, (as previously)
		assertSame( val.getTreeNode(), tbd );		// the tree-node is now tbd  (changed)
		
		assertSame( table.get( tbd ), vcd );
		assertTrue( table.containsKey( tbd ) );		// contains tbd
		assertFalse( table.containsKey( tcd ) );		// but not tcd
		assertEquals( table.size(), 4 );
		
		
		// Ensure that there is no unused node
		assertSame( table.takeUnusedViewNodeFor( tbd ), null );
	}
}
