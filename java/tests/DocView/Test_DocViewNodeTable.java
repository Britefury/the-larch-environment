//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocView;

import java.util.Arrays;
import java.util.List;

import BritefuryJ.DocModel.DMList;

import junit.framework.TestCase;

public class Test_DocViewNodeTable extends TestCase
{
	@SuppressWarnings("unchecked")
	private static Object listRGet(List<Object> xs, int[] is)
	{
		Object x = xs;
		for (int i: is)
		{
			List<Object> xx = (List<Object>)x;
			x = xx.get( i );
		}
		return x;
	}
	


	private static DMList[] buildDiamondDoc()
	{
		DMList dd = new DMList( Arrays.asList( new Object[] { "d" } ) );
		DMList dc = new DMList( Arrays.asList( new Object[] { dd } ) );
		DMList db = new DMList( Arrays.asList( new Object[] { dd } ) );
		DMList da = new DMList( Arrays.asList( new Object[] { db, dc } ) );
		return new DMList[]{ da, db, dc, dd };
	}
	


}


/*
	class _Value (object):
		def __init__(self, docNode, treeNode, x):
			self.docNode = docNode
			self.treeNode = treeNode
			self.x = x


		def _changeTreeNode(self, treeNode):
			assert treeNode.node is self.docNode
			self.treeNode = treeNode



	def _buildDiamondDoc(self):
		dd = DMList( [ 'd' ] )
		dc = DMList( [ dd ] )
		db = DMList( [ dd ] )
		da = DMList( [ db, dc ] )
		return da, db, dc, dd


	def _buildDiamondTree(self):
		da, db, dc, dd = self._buildDiamondDoc()
		tree = DocTree()
		ta = tree.treeNode( da )
		return da, db, dc, dd, tree, ta


	def _buildDiamondTable(self):
		da, db, dc, dd, tree, ta = self._buildDiamondTree()
		table = DocViewNodeTable()

		tb = ta[0]
		tc = ta[1]
		tbd = tb[0]
		tcd = tc[0]

		va = self._Value( da, ta, 'a' )
		vb = self._Value( db, tb, 'b' )
		vc = self._Value( dc, tc, 'c' )
		vbd = self._Value( dd, tbd, 'bd' )
		vcd = self._Value( dd, tcd, 'cd' )

		table[ta] = va
		table[tb] = vb
		table[tc] = vc
		table[tbd] = vbd
		table[tcd] = vcd

		return da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table



	def testAccessors(self):
		da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table = self._buildDiamondTable()

		self.assert_( len( table._table ) == 4 )
		self.assert_( len( table._table[tbd.node] ) == 2 )

		self.assert_( table[ta].x  ==  'a' )
		self.assert_( table[tb].x  ==  'b' )
		self.assert_( table[tc].x  ==  'c' )
		self.assert_( table[tbd].x  ==  'bd' )
		self.assert_( table[tcd].x  ==  'cd' )

		self.assert_( ta in table )
		self.assert_( tb in table )
		self.assert_( tc in table )
		self.assert_( tbd in table )
		self.assert_( tcd in table )

		self.assert_( len( table ) == 5 )
		self.assert_( set( iter( table ) )  ==  set( [ ta, tb, tc, tbd, tcd ] ) )
		self.assert_( set( table.keys() )  ==  set( [ ta, tb, tc, tbd, tcd ] ) )
		self.assert_( set( table.values() )  ==  set( [ va, vb, vc, vbd, vcd ] ) )
		self.assert_( set( table.items() )  ==  set( [ (ta,va), (tb,vb), (tc,vc), (tbd,vbd), (tcd,vcd) ] ) )



	def testDel(self):
		da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table = self._buildDiamondTable()

		del table[ta]
		del table[tbd]

		self.assert_( len( table._table ) == 3 )
		self.assert_( len( table._table[tbd.node] ) == 1 )

		self.assert_( table[tb].x  ==  'b' )
		self.assert_( table[tc].x  ==  'c' )
		self.assert_( table[tcd].x  ==  'cd' )

		self.assert_( ta not in table )
		self.assert_( tb in table )
		self.assert_( tc in table )
		self.assert_( tbd not in table )
		self.assert_( tcd in table )

		self.assert_( len( table ) == 3 )
		self.assert_( set( iter( table ) )  ==  set( [ tb, tc, tcd ] ) )
		self.assert_( set( table.keys() )  ==  set( [ tb, tc, tcd ] ) )
		self.assert_( set( table.values() )  ==  set( [ vb, vc, vcd ] ) )
		self.assert_( set( table.items() )  ==  set( [ (tb,vb), (tc,vc), (tcd,vcd) ] ) )


	def testSet(self):
		da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table = self._buildDiamondTable()

		vx = self._Value( da, ta, 'x' )
		vy = self._Value( dd, tbd, 'y' )

		table[ta] = vx
		table[tbd] = vy

		self.assert_( table[ta].x  ==  'x' )
		self.assert_( table[tb].x  ==  'b' )
		self.assert_( table[tc].x  ==  'c' )
		self.assert_( table[tbd].x  ==  'y' )
		self.assert_( table[tcd].x  ==  'cd' )

		self.assert_( ta in table )
		self.assert_( tb in table )
		self.assert_( tc in table )
		self.assert_( tbd in table )
		self.assert_( tcd in table )

		self.assert_( len( table ) == 5 )
		self.assert_( set( iter( table ) )  ==  set( [ ta, tb, tc, tbd, tcd ] ) )
		self.assert_( set( table.keys() )  ==  set( [ ta, tb, tc, tbd, tcd ] ) )
		self.assert_( set( table.values() )  ==  set( [ vx, vb, vc, vy, vcd ] ) )
		self.assert_( set( table.items() )  ==  set( [ (ta,vx), (tb,vb), (tc,vc), (tbd,vy), (tcd,vcd) ] ) )



	def testGC(self):
		import gc
		da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table = self._buildDiamondTable()

		del va
		del vbd
		gc.collect()

		self.assert_( len( table._table ) == 4 )
		self.assert_( len( table._table[tbd.node] ) == 1 )

		self.assert_( table[tb].x  ==  'b' )
		self.assert_( table[tc].x  ==  'c' )
		self.assert_( table[tcd].x  ==  'cd' )

		self.assert_( ta not in table )
		self.assert_( tb in table )
		self.assert_( tc in table )
		self.assert_( tbd not in table )
		self.assert_( tcd in table )

		self.assert_( len( table ) == 3 )
		self.assert_( set( iter( table ) )  ==  set( [ tb, tc, tcd ] ) )
		self.assert_( set( table.keys() )  ==  set( [ tb, tc, tcd ] ) )
		self.assert_( set( table.values() )  ==  set( [ vb, vc, vcd ] ) )
		self.assert_( set( table.items() )  ==  set( [ (tb,vb), (tc,vc), (tcd,vcd) ] ) )


	def testUnref(self):
		da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table = self._buildDiamondTable()

		table.unrefViewNode( va )
		table.unrefViewNode( vbd )

		self.assert_( len( table._table ) == 4 )
		self.assert_( len( table._table[tbd.node] ) == 1 )
		self.assert_( len( table._table[ta.node]._unrefedNodes ) == 1 )
		self.assert_( len( table._table[tbd.node]._unrefedNodes ) == 1 )

		self.assert_( table[tb].x  ==  'b' )
		self.assert_( table[tc].x  ==  'c' )
		self.assert_( table[tcd].x  ==  'cd' )

		self.assert_( ta not in table )
		self.assert_( tb in table )
		self.assert_( tc in table )
		self.assert_( tbd not in table )
		self.assert_( tcd in table )

		self.assert_( len( table ) == 3 )
		self.assert_( set( iter( table ) )  ==  set( [ tb, tc, tcd ] ) )
		self.assert_( set( table.keys() )  ==  set( [ tb, tc, tcd ] ) )
		self.assert_( set( table.values() )  ==  set( [ vb, vc, vcd ] ) )
		self.assert_( set( table.items() )  ==  set( [ (tb,vb), (tc,vc), (tcd,vcd) ] ) )

		table.clearUnused()

		self.assert_( len( table._table ) == 3 )



	def testUnrefReref(self):
		da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table = self._buildDiamondTable()

		table.unrefViewNode( va )
		table.unrefViewNode( vbd )
		table.refViewNode( va )
		table.refViewNode( vbd )

		self.assert_( len( table._table ) == 4 )
		self.assert_( len( table._table[tbd.node] ) == 2 )

		self.assert_( table[ta].x  ==  'a' )
		self.assert_( table[tb].x  ==  'b' )
		self.assert_( table[tc].x  ==  'c' )
		self.assert_( table[tbd].x  ==  'bd' )
		self.assert_( table[tcd].x  ==  'cd' )

		self.assert_( ta in table )
		self.assert_( tb in table )
		self.assert_( tc in table )
		self.assert_( tbd in table )
		self.assert_( tcd in table )

		self.assert_( len( table ) == 5 )
		self.assert_( set( iter( table ) )  ==  set( [ ta, tb, tc, tbd, tcd ] ) )
		self.assert_( set( table.keys() )  ==  set( [ ta, tb, tc, tbd, tcd ] ) )
		self.assert_( set( table.values() )  ==  set( [ va, vb, vc, vbd, vcd ] ) )
		self.assert_( set( table.items() )  ==  set( [ (ta,va), (tb,vb), (tc,vc), (tbd,vbd), (tcd,vcd) ] ) )


	def testReuseUnrefed(self):
		da, db, dc, dd, tree, ta, tb, tc, tbd, tcd, va, vb, vc, vbd, vcd, table = self._buildDiamondTable()

		del table[tbd]
		table.unrefViewNode( vcd )

		self.assert_( len( table._table ) == 4 )
		self.assert_( len( table._table[tcd.node] ) == 0 )
		self.assert_( len( table._table[tcd.node]._refedNodes ) == 0 )
		self.assert_( len( table._table[tcd.node]._unrefedNodes ) == 1 )

		self.assert_( table[ta].x  ==  'a' )
		self.assert_( table[tb].x  ==  'b' )
		self.assert_( table[tc].x  ==  'c' )

		self.assert_( ta in table )
		self.assert_( tb in table )
		self.assert_( tc in table )
		self.assert_( tbd not in table )
		self.assert_( tcd not in table )

		self.assert_( len( table ) == 3 )
		self.assert_( set( iter( table ) )  ==  set( [ ta, tb, tc ] ) )
		self.assert_( set( table.keys() )  ==  set( [ ta, tb, tc] ) )
		self.assert_( set( table.values() )  ==  set( [ va, vb, vc ] ) )
		self.assert_( set( table.items() )  ==  set( [ (ta,va), (tb,vb), (tc,vc) ] ) )


		# Reuse
		val = table.takeUnusedViewNodeFor( tcd )
		self.assert_( val is vcd )
		self.assert_( val.docNode is dd )
		self.assert_( val.treeNode is tcd )

		self.assert_( table[tcd].x  ==  'cd' )
		self.assert_( tbd not in table )
		self.assert_( tcd in table )
		self.assert_( len( table ) == 4 )


		# Unref again
		table.unrefViewNode( vcd )
		self.assert_( len( table ) == 3 )

		# Reuse for a different key this time
		val = table.takeUnusedViewNodeFor( tbd )
		self.assert_( val is vcd )
		self.assert_( val.docNode is dd )
		self.assert_( val.treeNode is tbd )

		self.assert_( table[tbd].x  ==  'cd' )
		self.assert_( tbd in table )
		self.assert_( tcd not in table )
		self.assert_( len( table ) == 4 )


		self.assertRaises( KeyError, lambda: table.takeUnusedViewNodeFor( tbd ) )
*/
