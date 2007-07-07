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

from Britefury.CodeGraph.CGAssignment import CGAssignment
from Britefury.CodeGraph.CGBlock import CGBlock
from Britefury.CodeGraph.CGClass import CGClass
from Britefury.CodeGraph.CGDef import CGDef
from Britefury.CodeGraph.CGGetAttr import CGGetAttr
from Britefury.CodeGraph.CGModule import CGModule
from Britefury.CodeGraph.CGParameters import CGParameters
from Britefury.CodeGraph.CGParameterVar import CGParameterVar
from Britefury.CodeGraph.CGReturn import CGReturn
from Britefury.CodeGraph.CGUnboundRef import CGUnboundRef
from Britefury.CodeGraph.CGVar import CGVar




def _processNode(graph, node):
	nodeClass = node.__class__
	try:
		procFunc = _nodeClassToProcFunction[nodeClass]
	except KeyError:
		print nodeClass
		print dir( node )
		print list( node )
		return None
	else:
		return procFunc( graph, node )



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


def _processGetAttr(graph, node):
	attr = CGGetAttr()
	graph.nodes.append( attr )

	g = _processNode( graph, node.expr )
	if g is not None:
		attr.targetObject.append( g.parent )

	attr.attrName = node.attrname

	return attr


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





_nodeClassToProcFunction = {
	ast.Module : _processModule,
	ast.Stmt : _processStmt,
	ast.Function : _processFunction,
	ast.Class : _processClass,
	ast.Return : _processReturn,
	ast.Name : _processName,
	ast.AssName : _processName,
	ast.Getattr : _processGetAttr,
	ast.AssAttr : _processGetAttr,
	ast.Assign : _processAssign,
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
