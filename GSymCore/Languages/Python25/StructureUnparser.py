##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeMethodDispatchMetaClass, ObjectNodeDispatchMethod, objectNodeMethodDispatch
from Britefury.Dispatch.Dispatch import DispatchError

from GSymCore.Languages.Python25 import NodeClasses as Nodes



def _join(xs):
	return reduce( lambda a, b: a + b, xs, [] )


class Python25StructureUnparser (object):
	__metaclass__ = ObjectNodeMethodDispatchMetaClass
	__dispatch_module__ = Nodes.module
	__dispatch_num_args__ = 0


	def __call__(self, node):
		return objectNodeMethodDispatch( self, node )




	def _handleHeaderSuite(self, node, header, suite):
		return [ header, Nodes.Indent() ]  +  suite  +  [ Nodes.Dedent() ]


	def _handleHeader(self, node, header):
		return header


	def _elseSuite(self, node, suite):
		if suite is not None:
			return self._handleHeaderSuite( node, Nodes.ElseStmtHeader(), suite )
		else:
			return []


	def _finallySuite(self, node, suite):
		if suite is not None:
			return self._handleHeaderSuite( node, Nodes.FinallyStmtHeader(), suite )
		else:
			return []


	@ObjectNodeDispatchMethod
	def IfStmt(self, node, condition, suite, elifBlocks, elseSuite):
		return self._handleHeaderSuite( node, Nodes.IfStmtHeader( condition=condition ), suite )  +  \
		       _join( [ self._handleHeaderSuite( node, Nodes.ElifStmtHeader( condition=b['condition'] ), b['suite'] )   for b in elifBlocks ] )  +  \
		       self._elseSuite(node, elseSuite )

	@ObjectNodeDispatchMethod
	def WhileStmt(self, node, condition, suite, elseSuite):
		return self._handleHeaderSuite( node, Nodes.WhileStmtHeader( condition=condition ), suite )  +  \
		       self._elseSuite( node, elseSuite )

	@ObjectNodeDispatchMethod
	def ForStmt(self, node, target, source, suite, elseSuite):
		return self._handleHeaderSuite( node, Nodes.ForStmtHeader( target=target, source=source ), suite )  +  \
		       self._elseSuite( node, elseSuite )

	@ObjectNodeDispatchMethod
	def TryStmt(self, node, suite, exceptBlocks, elseSuite, finallySuite):
		return self._handleHeaderSuite( node, Nodes.TryStmtHeader(), suite )  +  \
		       _join( [ self._handleHeaderSuite( node, Nodes.ExceptStmtHeader( exception=b['exception'], target=b['target'] ), b['suite'] )   for b in exceptBlocks ] )  +  \
		       self._elseSuite( node, elseSuite )  +  \
		       self._finallySuite( node, finallySuite )

	@ObjectNodeDispatchMethod
	def WithStmt(self, node, expr, target, suite):
		return self._handleHeaderSuite( node, Nodes.WithStmtHeader( expr=expr, target=target ), suite )

	@ObjectNodeDispatchMethod
	def DefStmt(self, node, decorators, name, params, paramsTrailingSeparator, suite):
		return [ self._handleHeader( node, Nodes.DecoStmtHeader( name=d['name'], args=d['args'], argsTrailingSeparator=d['argsTrailingSeparator'] ) )  for d in decorators ]  +  \
		       self._handleHeaderSuite( node, Nodes.DefStmtHeader( name=name, params=params, paramsTrailingSeparator=paramsTrailingSeparator ), suite )

	@ObjectNodeDispatchMethod
	def ClassStmt(self, node, name, bases, basesTrailingSeparator, suite):
		return self._handleHeaderSuite( node, Nodes.ClassStmtHeader( name=name, bases=bases, basesTrailingSeparator=basesTrailingSeparator ), suite )


	@ObjectNodeDispatchMethod
	def IndentedBlock(self, node, suite):
		return [ Nodes.Indent() ]  +  suite  +  [ Nodes.Dedent() ]


	@ObjectNodeDispatchMethod
	def SimpleStmt(self, node):
		return [ node ]

	@ObjectNodeDispatchMethod
	def CompountStmtHeader(self, node):
		return [ node ]

	@ObjectNodeDispatchMethod
	def CommentStmt(self, node):
		return [ node ]

	@ObjectNodeDispatchMethod
	def BlankLine(self, node):
		return [ node ]

	@ObjectNodeDispatchMethod
	def UNPARSED(self, node):
		return [ node ]



import unittest

class TestCase_Python25StructureParser (unittest.TestCase):
	def _unparseTest(self, input, result):
		u = Python25StructureUnparser()
		self.assertEqual( result, u( input ) )



	def test_unparsed(self):
		self._unparseTest( Nodes.UNPARSED( value='x' ), [ Nodes.UNPARSED( value='x' ) ] )


	def test_blankLine(self):
		self._unparseTest( Nodes.BlankLine(), [ Nodes.BlankLine() ] )


	def test_comment(self):
		self._unparseTest( Nodes.CommentStmt( comment='x' ), [ Nodes.CommentStmt( comment='x' ) ] )


	def test_simpleStmt(self):
		self._unparseTest( Nodes.ReturnStmt( value=Nodes.Load( name='a' ) ), [ Nodes.ReturnStmt( value=Nodes.Load( name='a' ) ) ] )


	def test_compoundStmtHeader(self):
		self._unparseTest( Nodes.IfStmtHeader( condition=Nodes.Load( name='x' ) ), [ Nodes.IfStmtHeader( condition=Nodes.Load( name='x' ) ) ] )


	def test_ifStmt(self):
		self._unparseTest( Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[] ),
				   [
					   Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ),
				   [
					   Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ElseStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elifBlocks=[ Nodes.ElifBlock( condition=Nodes.Load( name='b' ), suite=[ Nodes.CommentStmt( comment='y' ) ] ) ] ),
				   [
					   Nodes.IfStmtHeader( condition=Nodes.Load( name='a' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ElifStmtHeader( condition=Nodes.Load( name='b' ) ),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='y' ),
					   Nodes.Dedent() ] )

	def test_whileStmt(self):
		self._unparseTest( Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ),
				   [
					   Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ElseStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent() ] )


	def test_forStmt(self):
		self._unparseTest( Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ], elseSuite=[ Nodes.CommentStmt( comment='x' ) ] ),
				   [
					   Nodes.ForStmtHeader( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ElseStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent() ] )


	def test_tryStmt(self):
		self._unparseTest( Nodes.TryStmt( suite=[ Nodes.BlankLine() ], exceptBlocks=[], finallySuite=[ Nodes.CommentStmt( comment='x' ) ] ),
				   [
					   Nodes.TryStmtHeader(),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.FinallyStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
						  exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ] ),
				   [
					   Nodes.TryStmtHeader(),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
						  exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ),
								 Nodes.ExceptBlock( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ), suite=[ Nodes.CommentStmt( comment='y' ) ] )] ),
				   [
					   Nodes.TryStmtHeader(),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent(),
					   Nodes.ExceptStmtHeader( exception=Nodes.Load( name='k' ), target=Nodes.SingleTarget( name='q' ) ),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='y' ),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
						  exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						  elseSuite=[ Nodes.CommentStmt( comment='y' ) ] ),
				   [
					   Nodes.TryStmtHeader(),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent(),
					   Nodes.ElseStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='y' ),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
						  exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						  finallySuite=[ Nodes.CommentStmt( comment='y' ) ] ),
				   [
					   Nodes.TryStmtHeader(),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent(),
					   Nodes.FinallyStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='y' ),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.TryStmt( suite=[ Nodes.BlankLine() ],
						  exceptBlocks=[ Nodes.ExceptBlock( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ), suite=[ Nodes.CommentStmt( comment='x' ) ] ) ],
						  elseSuite=[ Nodes.CommentStmt( comment='y' ) ], finallySuite=[ Nodes.CommentStmt( comment='z' ) ] ),
				   [
					   Nodes.TryStmtHeader(),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent(),
					   Nodes.ExceptStmtHeader( exception=Nodes.Load( name='j' ), target=Nodes.SingleTarget( name='p' ) ),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='x' ),
					   Nodes.Dedent(),
					   Nodes.ElseStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='y' ),
					   Nodes.Dedent(),
					   Nodes.FinallyStmtHeader(),
					   Nodes.Indent(),
					   Nodes.CommentStmt( comment='z' ),
					   Nodes.Dedent() ] )


	def test_withStmt(self):
		self._unparseTest( Nodes.WithStmt( expr=Nodes.SingleTarget( name='a' ), target=Nodes.Load( name='x' ), suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.WithStmtHeader( expr=Nodes.SingleTarget( name='a' ), target=Nodes.Load( name='x' ) ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )


	def test_defStmt(self):
		self._unparseTest( Nodes.DefStmt( decorators=[], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.DefStmt( decorators=[ Nodes.Decorator( name='a', args=[ Nodes.Load( name='x' ) ] ) ], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.DecoStmtHeader( name='a', args=[ Nodes.Load( name='x' ) ] ),
					   Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )
		self._unparseTest( Nodes.DefStmt( decorators=[ Nodes.Decorator( name='a', args=[ Nodes.Load( name='x' ) ] ), Nodes.Decorator( name='b', args=[ Nodes.Load( name='y' ) ] ) ],
						  name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.DecoStmtHeader( name='a', args=[ Nodes.Load( name='x' ) ] ),
					   Nodes.DecoStmtHeader( name='b', args=[ Nodes.Load( name='y' ) ] ),
					   Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )


	def test_classStmt(self):
		self._unparseTest( Nodes.ClassStmt( name='A', bases=[ Nodes.Load( name='x' ) ], suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.ClassStmtHeader( name='A', bases=[ Nodes.Load( name='x' ) ] ),
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )


	def test_indentedBlock(self):
		self._unparseTest( Nodes.IndentedBlock( suite=[ Nodes.BlankLine() ] ),
				   [
					   Nodes.Indent(),
					   Nodes.BlankLine(),
					   Nodes.Dedent() ] )


	#def test_nestedStructure(self):
		#self._parseListTest( g.suiteItem(),
					#[
						#Nodes.ClassStmtHeader( name='A', bases=[ Nodes.Load( name='x' ) ] ),
						#Nodes.Indent(),
						#Nodes.DefStmtHeader( name='f', params=[ Nodes.SimpleParam( name='x' ) ] ),
						#Nodes.Indent(),
							#Nodes.WhileStmtHeader( condition=Nodes.Load( name='a' ) ),
							#Nodes.Indent(),
								#Nodes.BlankLine(),
							#Nodes.Dedent(),
						#Nodes.Dedent(),
						#Nodes.Dedent() ],
					#Nodes.ClassStmt( name='A', bases=[ Nodes.Load( name='x' ) ], suite=[
						#Nodes.DefStmt( decorators=[], name='f', params=[ Nodes.SimpleParam( name='x' ) ], suite=[ Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.BlankLine() ] ) ] ) ] ) )


