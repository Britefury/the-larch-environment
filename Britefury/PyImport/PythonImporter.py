##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import compiler
from compiler import ast

from Britefury.SheetGraph.SheetGraph import SheetGraph

from Britefury.CodeGraph.CGArguments import CGArguments
from Britefury.CodeGraph.CGAssignment import CGAssignment
from Britefury.CodeGraph.CGBinaryOperator import *
from Britefury.CodeGraph.CGBlock import CGBlock
from Britefury.CodeGraph.CGBreak import CGBreak
from Britefury.CodeGraph.CGCall import CGCall
from Britefury.CodeGraph.CGClass import CGClass
from Britefury.CodeGraph.CGContinue import CGContinue
from Britefury.CodeGraph.CGDef import CGDef
from Britefury.CodeGraph.CGFloatLiteral import CGFloatLiteral
from Britefury.CodeGraph.CGGetAttr import CGGetAttr
from Britefury.CodeGraph.CGIf import CGIf
from Britefury.CodeGraph.CGIfBlock import CGIfBlock
from Britefury.CodeGraph.CGIntLiteral import CGIntLiteral
from Britefury.CodeGraph.CGLambda import CGLambda
from Britefury.CodeGraph.CGList import CGList
from Britefury.CodeGraph.CGModule import CGModule
from Britefury.CodeGraph.CGParameters import CGParameters
from Britefury.CodeGraph.CGParameterVar import CGParameterVar
from Britefury.CodeGraph.CGReturn import CGReturn
from Britefury.CodeGraph.CGSubscript import CGSubscript
from Britefury.CodeGraph.CGStringLiteral import CGStringLiteral
from Britefury.CodeGraph.CGTuple import CGTuple
from Britefury.CodeGraph.CGUnaryOperator import CGNegate, CGNot
from Britefury.CodeGraph.CGUnboundRef import CGUnboundRef
from Britefury.CodeGraph.CGVar import CGVar
from Britefury.CodeGraph.CGWhile import CGWhile




def _processNode(graph, node):
	if node is not None:
		nodeClass = node.__class__
		try:
			procFunc = _nodeClassToProcFunction[nodeClass]
		except KeyError:
			print nodeClass
			print dir( node )
			print list( node )
			print node
			return None
		else:
			return procFunc( graph, node )
	else:
		return None



def _processModule(graph, node):
	mainModule = CGModule()
	graph.nodes.append( mainModule )

	block = _processNode( graph, node.node )
	if block is not None:
		mainModule.block.append( block.parent )

	return mainModule


def _processStmt(graph, node):
	block = CGBlock()
	graph.nodes.append( block )

	for n in node.nodes:
		g = _processNode( graph, n )
		if g is not None:
			block.statements.append( g.parent )

	return block


def _processFunction(graph, node):
	func = CGDef()
	graph.nodes.append( func )

	declVar = CGVar()
	graph.nodes.append( declVar )

	params = CGParameters()
	graph.nodes.append( params )

	func.declVar.append( declVar.declaration )
	func.parameters.append( params.parent )


	declVar.name = node.name
	if node.doc is not None:
		func.functionDoc = node.doc

	for argname in node.argnames:
		p = CGParameterVar()
		graph.nodes.append( p )

		params.params.append( p.declaration )

		p.name = argname


	block = _processNode( graph, node.code )
	if block is not None:
		func.block.append( block.parent )

	return func


def _processClass(graph, node):
	clsCG = CGClass()
	graph.nodes.append( clsCG )

	declVar = CGVar()
	graph.nodes.append( declVar )

	clsCG.declVar.append( declVar.declaration )


	declVar.name = node.name

	for base in node.bases:
		g = _processNode( graph, base )
		if g is not None:
			clsCG.bases.append( g.parent )

	block = _processNode( graph, node.code )
	if block is not None:
		clsCG.block.append( block.parent )

	return clsCG


def _processDiscard(graph, node):
	return _processNode( graph, node.expr )


def _processReturn(graph, node):
	rtn = CGReturn()
	graph.nodes.append( rtn )

	g = _processNode( graph, node.value )
	if g is not None:
		rtn.value.append( g.parent )
	return rtn


def _processName(graph, node):
	ref = CGUnboundRef()
	graph.nodes.append( ref )
	ref.targetName = node.name
	return ref


def _processConst(graph, node):
	value = node.value
	if isinstance( value, int ):
		g = CGIntLiteral()
		g.strValue = str( value )
	elif isinstance( value, float ):
		g = CGFloatLiteral()
		g.strValue = str( value )
	elif isinstance( value, str ):
		g = CGStringLiteral()
		g.value = str( value )
	else:
		raise TypeError, 'constant type not supported'
	graph.nodes.append( g )
	return g


def _processGetAttr(graph, node):
	attr = CGGetAttr()
	graph.nodes.append( attr )

	g = _processNode( graph, node.expr )
	if g is not None:
		attr.targetObject.append( g.parent )

	attr.attrName = node.attrname

	return attr


def _processSubscript(graph, node):
	subs = CGSubscript()
	graph.nodes.append( subs )


	g = _processNode( graph, node.expr )
	if g is not None:
		subs.target.append( g.parent )

	if len( node.subs ) == 1:
		g = _processNode( graph, node.subs[0] )
		if g is not None:
			subs.key.append( g.parent )
	else:
		tup = CGTuple()
		graph.nodes.append( tup )
		for n in node.subs:
			g = _processNode( graph, n )
			if g is not None:
				tup.args.append( g.parent )
		subs.key.append( tup.parent )

	return subs


def _processNot(graph, node):
	notCG = CGNot()
	graph.nodes.append( notCG )

	g = _processNode( graph, node.expr )
	if g is not None:
		notCG.expr.append( g.parent )

	return notCG


def _processUnarySub(graph, node):
	negateCG = CGNegate()
	graph.nodes.append( negateCG )

	g = _processNode( graph, node.expr )
	if g is not None:
		negateCG.expr.append( g.parent )

	return negateCG


def _makeBinOpProcessFunction(cgNodeClass):
	def _processBinOp(graph, node):
		binop = cgNodeClass()
		graph.nodes.append( binop )


		g = _processNode( graph, node.left )
		if g is not None:
			binop.left.append( g.parent )

		g = _processNode( graph, node.right )
		if g is not None:
			binop.right.append( g.parent )

		return binop
	return _processBinOp


def _processCompare(graph, node):
	if len( node.ops ) > 1:
		raise TypeError, 'compare node has more than 1 child'
	operator, testValue = node.ops[0]
	try:
		binop = _compareSymbolToGraphNodeClass[operator]()
	except KeyError:
		raise TypeError, 'unknown compare operator'

	graph.nodes.append( binop )

	g = _processNode( graph, node.expr )
	if g is not None:
		binop.left.append( g.parent )

	g = _processNode( graph, testValue )
	if g is not None:
		binop.right.append( g.parent )

	return binop


def _processCallFunc(graph, node):
	call = CGCall()
	graph.nodes.append( call )

	args = CGArguments()
	graph.nodes.append( args )

	call.arguments.append( args.parent )

	g = _processNode( graph, node.node )
	if g is not None:
		call.targetObject.append( g.parent )

	for n in node.args:
		g = _processNode( graph, n )
		if g is not None:
			args.args.append( g.parent )

	return call


def _processTuple(graph, node):
	tupCG = CGTuple()
	graph.nodes.append( tupCG )

	for n in node.nodes:
		g = _processNode( graph, n )
		if g is not None:
			tupCG.args.append( g.parent )

	return tupCG


def _processList(graph, node):
	lsCG = CGList()
	graph.nodes.append( lsCG )

	for n in node.nodes:
		g = _processNode( graph, n )
		if g is not None:
			lsCG.args.append( g.parent )

	return lsCG


def _processLambda(graph, node):
	lambdaCG = CGLambda()
	graph.nodes.append( lambdaCG )

	params = CGParameters()
	graph.nodes.append( params )

	lambdaCG.parameters.append( params.parent )


	for argname in node.argnames:
		p = CGVar()
		graph.nodes.append( p )

		params.params.append( p.declaration )

		p.name = argname


	expr = _processNode( graph, node.code )
	if expr is not None:
		lambdaCG.valueExpr.append( expr.parent )

	return lambdaCG


def _processAssign(graph, node):
	assign = CGAssignment()
	graph.nodes.append( assign )


	for n in node.nodes:
		g = _processNode( graph, n )
		if g is not None:
			assign.targets.append( g.parent )

	g = _processNode( graph, node.expr )
	if g is not None:
		assign.value.append( g.parent )

	return assign


def _processIf(graph, node):
	ifCG = CGIf()
	graph.nodes.append( ifCG )

	for condition, code in node.tests:
		ifBlock = CGIfBlock()
		graph.nodes.append( ifBlock )

		g = _processNode( graph, condition )
		if g is not None:
			ifBlock.condition.append( g.parent )

		g = _processNode( graph, code )
		if g is not None:
			ifBlock.block.append( g.parent )

		ifCG.ifBlocks.append( ifBlock.ifStatement )


	g = _processNode( graph, node.else_ )
	if g is not None:
		ifCG.elseBlock.append( g.parent )

	return ifCG


def _processBreak(graph, node):
	breakCG = CGBreak()
	graph.nodes.append( breakCG )
	return breakCG


def _processContinue(graph, node):
	continueCG = CGContinue()
	graph.nodes.append( continueCG )
	return continueCG


def _processWhile(graph, node):
	whileCG = CGWhile()
	graph.nodes.append( whileCG )

	g = _processNode( graph, node.test )
	if g is not None:
		whileCG.whileExpr.append( g.parent )

	g = _processNode( graph, node.body )
	if g is not None:
		whileCG.block.append( g.parent )

	g = _processNode( graph, node.else_ )
	if g is not None:
		whileCG.elseBlock.append( g.parent )

	return whileCG


def _processPrintnl(graph, node):
	ref = CGUnboundRef()
	graph.nodes.append( ref )

	call = CGCall()
	graph.nodes.append( call )

	args = CGArguments()
	graph.nodes.append( args )

	ref.targetName = 'print'
	call.arguments.append( args.parent )
	call.targetObject.append( ref.parent )

	for n in node.nodes:
		g = _processNode( graph, n )
		if g is not None:
			args.args.append( g.parent )

	return call





_nodeClassToProcFunction = {
	ast.Module : _processModule,
	ast.Stmt : _processStmt,
	ast.Function : _processFunction,
	ast.Class : _processClass,
	ast.Discard : _processDiscard,
	ast.Return : _processReturn,
	ast.Name : _processName,
	ast.AssName : _processName,
	ast.Const : _processConst,
	ast.Getattr : _processGetAttr,
	ast.Subscript : _processSubscript,
	ast.Not : _processNot,
	ast.UnarySub : _processUnarySub,
	ast.Add : _makeBinOpProcessFunction( CGBinOpAdd ),
	ast.Sub : _makeBinOpProcessFunction( CGBinOpSub ),
	ast.Mul : _makeBinOpProcessFunction( CGBinOpMul ),
	ast.Div : _makeBinOpProcessFunction( CGBinOpDiv ),
	ast.Power : _makeBinOpProcessFunction( CGBinOpPow ),
	ast.Mod : _makeBinOpProcessFunction( CGBinOpMod ),
	ast.Bitand : _makeBinOpProcessFunction( CGBinOpBitAnd ),
	ast.Bitor : _makeBinOpProcessFunction( CGBinOpBitOr ),
	ast.Bitxor : _makeBinOpProcessFunction( CGBinOpBitXor ),
	ast.Compare : _processCompare,
	ast.CallFunc : _processCallFunc,
	ast.List : _processList,
	ast.Tuple : _processTuple,
	ast.AssTuple : _processTuple,
	ast.Lambda : _processLambda,
	ast.AssAttr : _processGetAttr,
	ast.Assign : _processAssign,
	ast.If : _processIf,
	ast.Break : _processBreak,
	ast.Continue : _processContinue,
	ast.While : _processWhile,
	ast.Printnl : _processPrintnl,
}



_compareSymbolToGraphNodeClass = {
	'==' : CGBinOpEq,
	'!=' : CGBinOpNEq,
	'<' : CGBinOpLT,
	'>' : CGBinOpGT,
	'<=' : CGBinOpLTE,
	'>=' : CGBinOpGTE,
}





def _convertAstToCodeGraph(tree):
	graph = SheetGraph()
	root = _processNode( graph, tree )
	if root is None:
		mainModule = CGModule()
		graph.nodes.append( mainModule )
		mainBlock = CGBlock()
		graph.nodes.append( mainBlock )
		mainModule.block.append( mainBlock.parent )
		root = mainModule
	return graph, root



def importPythonSource(source):
	tree = compiler.parse( source )
	return _convertAstToCodeGraph( tree )
