##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from weakref import ref, WeakValueDictionary




class _ViewTable (object):
	def __init__(self, table, docNode):
		self._table = table
		self._docNode = docNode
		
		self._refedNodes = WeakValueDictionary()
		self._unrefedNodes = {}
		
		
		
	def takeUnusedViewNodeFor(self, treeNode):
		if treeNode in self._refedNodes:
			raise KeyError

		# Look in unref'ed nodes...
		try:
			viewNode = self._unrefedNodes[treeNode]
		except KeyError:
			pass
		else:
			# Found an unused view node; move it into the ref'ed table
			del self._unrefedNodes[treeNode]
			self._refedNodes[treeNode] = viewNode
			return viewNode
		
		# Still not found; re-use one of the un-ref'ed nodes
		if len( self._unrefedNodes ) > 0:
			key, viewNode = self._unrefedNodes.items()[0]
			del self._unrefedNodes[key]
			self._refedNodes[treeNode] = viewNode
			viewNode._changeTreeNode( treeNode )
			if len( self._unrefedNodes )  ==  0:
				self._table._removeViewTableFromUnrefedList( self )
			return viewNode

		raise KeyError


		
	def __getitem__(self, treeNode):
		return self._refedNodes[treeNode]

	def __setitem__(self, treeNode, v):
		self._refedNodes[treeNode] = v

	def __delitem__(self, treeNode):
		del self._refedNodes[treeNode]
		self.__removeIfEmpty()
	
	
	def __contains__(self, treeNode):
		return treeNode in self._refedNodes

	def __len__(self):
		return len( self._refedNodes )
	

	def keys(self):
		return self._refedNodes.keys()

	def values(self):
		return self._refedNodes.values()

	def items(self):
		return self._refedNodes.items()

	
	
	
	def refViewNode(self, viewNode):
		treeNode = viewNode.treeNode
		try:
			del self._unrefedNodes[treeNode]
		except KeyError:
			pass
		else:
			if len( self._unrefedNodes ) == 0:
				self._table._removeViewTableFromUnrefedList( self )
		self._refedNodes[treeNode] = viewNode

	def unrefViewNode(self, viewNode):
		treeNode = viewNode.treeNode
		try:
			del self._refedNodes[treeNode]
		except KeyError:
			pass
		if len( self._unrefedNodes ) == 0:
			self._table._addViewTableToUnrefedList( self )
		self._unrefedNodes[treeNode] = viewNode
		
		
	def clearUnrefedViewnodes(self):
		self._unrefedNodes = {}
		self.__removeIfEmpty()
		
		
	def __removeIfEmpty(self):
		if len( self._refedNodes ) == 0  and  len( self._unrefedNodes ) == 0:
			self._table._remove( self._docNode )
			
			


class DocViewNodeTable (object):
	def __init__(self):
		self._table = {}
		self._unrefedTables = set()
		
		
	def takeUnusedViewNodeFor(self, treeNode):
		return self._table[treeNode.node].takeUnusedViewNodeFor( treeNode )
		
		
	def __getitem__(self, treeNode):
		return self._table[treeNode.node][treeNode]

	def __setitem__(self, treeNode, v):
		try:
			subTable = self._table[treeNode.node]
		except KeyError:
			subTable = _ViewTable( self, treeNode.node )
			self._table[treeNode.node] = subTable
		subTable[treeNode] = v

	def __delitem__(self, treeNode):
		del self._table[treeNode.node][treeNode]
	
	def __contains__(self, treeNode):
		try:
			return treeNode in self._table[treeNode.node]
		except KeyError:
			return False
	
	
	def __len__(self):
		return reduce( lambda a, b:  a + len( b ),  self._table.values(),  0 )


	def __iter__(self):
		for v in self._table.values():
			for k in v.keys():
				yield k

	def keys(self):
		return reduce( lambda a, b:  a + list(b.keys()),  self._table.values(),  [] )
	
	def values(self):
		return reduce( lambda a, b:  a + list(b.values()),  self._table.values(),  [] )

	def items(self):
		return reduce( lambda a, b:  a + list(b.items()),  self._table.values(),  [] )
	
	
	def refViewNode(self, viewNode):
		viewTable = self._table[viewNode.docNode]
		viewTable.refViewNode( viewNode )
		
	def unrefViewNode(self, viewNode):
		viewTable = self._table[viewNode.docNode]
		viewTable.unrefViewNode( viewNode )
			
		
	def _remove(self, docNode):
		del self._table[docNode]
		
		
	def _addViewTableToUnrefedList(self, table):
		self._unrefedTables.add( table )
		
	def _removeViewTableFromUnrefedList(self, table):
		self._unrefedTables.remove( table )
		
		
	def clearUnused(self):
		for table in self._unrefedTables:
			table.clearUnrefedViewnodes()
		self._unrefedTables = set()
		
		

		

import unittest
from Britefury.DocModel.DMNode import DMNode
from Britefury.DocModel.DMList import DMList
from Britefury.DocTree.DocTree import DocTree

class TestCase_DocViewNodeTable (unittest.TestCase):
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
		