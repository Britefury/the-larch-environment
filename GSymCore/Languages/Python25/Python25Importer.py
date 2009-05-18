##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import _ast


import GSymCore.Languages.Python25.NodeClasses as Nodes
from Britefury.Util.NodeUtil import makeNullNode



# Would need to generate an 'oldLambdaExpr' node when necessary



def _getNodeTypeName(node):
	name = node.__class__.__name__
	if '.' in name:
		return name[name.rfind( '.' )+1:]
	else:
		return name


class _Importer (object):
	def __call__(self, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return Nodes.UNPARSED( value=name )
		return method( node )


	
def _extractParameters(node):
	numDefaultParams = len( node.defaults )
	numSimpleParams = len( node.args )  -  ( numDefaultParams )
	
	params = [ Nodes.SimpleParam( name=name.id )   for name in node.args[:numSimpleParams] ]
	params.extend( [ Nodes.DefaultValueParam( name=name.id, defaultValue=_expr( value ) )   for name, value in zip( node.args[numSimpleParams:], node.defaults ) ] )
	
	if node.vararg is not None:
		params.append( Nodes.ParamList( name=node.vararg ) )
	if node.kwarg is not None:
		params.append( Nodes.KWParamList( name=node.kwarg ) )
	
	return params





class _TargetImporter (_Importer):
	def __call__(self, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return _expr( node )
		return method( node )

	
	# Targets
	#def AssName(self, node):
		#return [ 'singleTarget', node.id ]
	
	#def AssAttr(self, node):
		#return [ 'attributeRef', _target( node.expr ), node.attr ]
	
	#def AssTuple(self, node):
		#return [ 'tupleTarget' ]  +  [ _target( x )   for x in node.elts ]
	
	#def AssList(self, node):
		#return [ 'listTarget' ]  +  [ _target( x )   for x in node.elts ]

	
	def Name(self, node):
		return Nodes.SingleTarget( name=node.id )
	
	def Attribute(self, node):
		return Nodes.AttributeRef( target=_expr( node.value ), name=node.attr )
	
	def Tuple(self, node):
		return Nodes.TupleTarget( targets=[ _target( x )   for x in node.elts ] )
	
	def List(self, node):
		return Nodes.ListTarget( targets=[ _target( x )   for x in node.elts ] )




	
class _ListCompImporter (_Importer):
	# List comprehension
	def comprehension(self, node):
		return [ Nodes.ComprehensionFor( target=_target( node.target ), source=_expr( node.iter ) ) ]  +  [ Nodes.ComprehensionIf( condition=_expr( x ) )   for x in node.ifs ]
	
	def comprehensionType(self, node):
		return self.comprehension( node )


class _GenExprImporter (_Importer):
	# Generator expression comprehension
	def comprehension(self, node):
		return [ Nodes.ComprehensionFor( target=_target( node.target ), source=_expr( node.iter ) ) ]  +  [ Nodes.ComprehensionIf( condition=_expr( x ) )   for x in node.ifs ]

	def comprehensionType(self, node):
		return self.comprehension( node )


class _LambdaArgsImporter (_Importer):
	# Lambda arguments
	def arguments(self, node):
		return _extractParameters( node )
	
	def argumentsType(self, node):
		return self.arguments( node )

	
class _ImportImporter (_Importer):
	# Import statement
	def alias(self, node):
		if node.asname is None:
			return Nodes.ModuleImport( name=node.name )
		else:
			return Nodes.ModuleImportAs( name=node.name, asName=node.asname )
		
	def aliasType(self, node):
		return self.alias( node )


class _ImportFromImporter (_Importer):
	# Import statement
	def alias(self, node):
		if node.asname is None:
			return Nodes.ModuleContentImport( name=node.name )
		else:
			return Nodes.ModuleContentImportAs( name=node.name, asName=node.asname )
		
	def aliasType(self, node):
		return self.alias( node )

	
class _DecoratorImporter (_Importer):
	def Name(self, node):
		return Nodes.DecoStmt( name=node.id, args=makeNullNode() )

	def Call(self, node):
		_starArg = lambda name, x:   [ [ name, _expr( x ) ] ]   if x is not None   else   []
		return Nodes.DecoStmt( name=node.func.id, args=[ _expr( x )   for x in node.args ]  +  [ _expr( x )   for x in node.keywords ]  +  _starArg( 'argList', node.starargs )  +  _starArg( 'kwArgList', node.kwargs ) )

	def keyword(self, node):
		return Nodes.CallKWArg( name=node.arg, value=_expr( node.value ) )

	def keywordType(self, node):
		return self.keyword( node )


	
	
_binOpNameTable = { 'Pow' : Nodes.Pow,  'Mult' : Nodes.Mul,  'Div' : Nodes.Div,  'Mod' : Nodes.Mod,  'Add' : Nodes.Add,  'Sub' : Nodes.Sub,  'LShift' : Nodes.LShift,  'RShift' : Nodes.RShift,  'BitAnd' : Nodes.BitAnd,  'BitXor' : Nodes.BitXor,  'BitOr' : Nodes.BitOr }	
_unaryOpNameTable = { 'Invert' : Nodes.Invert,  'USub' : Nodes.Negate,  'UAdd' : Nodes.Pos,  'Not' : Nodes.NotTest }	
_boolOpNameTable = { 'And' : Nodes.AndTest,  'Or' : Nodes.OrTest }	
_cmpOpTable = { 'Lt' : Nodes.CmpOpLt,  'LtE' : Nodes.CmpOpLte,  'Eq' : Nodes.CmpOpEq,  'NotEq' : Nodes.CmpOpNeq,  'Gt' : Nodes.CmpOpGt,  'GtE' : Nodes.CmpOpGte,  'Is' : Nodes.CmpOpIs,  'IsNot' : Nodes.CmpOpIsNot,  'In' : Nodes.CmpOpIn,  'NotIn' : Nodes.CmpOpNotIn }
	
	
def _getOpNodeName(op):
	if hasattr( op, 'name' ):
		return op.name()
	else:
		return _getNodeTypeName( op )

	
class _ExprImporter (_Importer):
	def __call__(self, node, method=None):
		if node is None:
			return makeNullNode()
		else:
			if method is None:
				name = _getNodeTypeName( node )
				try:
					method = getattr( self, name )
				except AttributeError:
					return Nodes.UNPARSED( value=name )
			return method( node )

	
	# Expression
	def Expression(self, node):
		return _expr( node.body )
		
		
		
	# Number literal
	def Num(self, node):
		value = node.n
		if isinstance( value, int ):
			return Nodes.IntLiteral( format='decimal', numType='int', value=repr( value ) )
		elif isinstance( value, long ):
			return Nodes.IntLiteral( format='decimal', numType='long', value=repr( value )[:-1] )
		elif isinstance( value, float ):
			return Nodes.FloatLiteral( value=repr( value ) )
		elif isinstance( value, complex ):
			if value.real == 0.0:
				return Nodes.ImaginaryLiteral( value=repr( value.imag ) + 'j' )
			else:
				return Nodes.Add( x=Nodes.FloatLiteral( value=repr( value.real ) ), y=Nodes.ImaginaryLiteral( value=repr( value.imag ) + 'j' ) )
		else:
			print 'Const: could not handle value', value
			raise ValueError
		
	
	# String literal
	def Str(self, node):
		value = node.s
		if isinstance( value, str ):
			return Nodes.StringLiteral( format='ascii', quotation='single', value=repr( value )[1:-1] )
		elif isinstance( value, unicode ):
			return Nodes.StringLiteral( format='unicode', quotation='single', value=repr( value )[2:-1] )
		else:
			print 'Const: could not handle value', value
			raise ValueError

		
		
	# Variable ref
	def Name(self, node):
		return Nodes.Load( name=node.id )


	# Tuple literal
	def Tuple(self, node):
		return Nodes.TupleLiteral( values=[ _expr( x )   for x in node.elts ] )
	
	
	
	# List literal
	def List(self, node):
		return Nodes.ListLiteral( values=[ _expr( x )   for x in node.elts ] )
	
	
	
	# List comprehension
	def ListComp(self, node):
		gens = []
		for x in node.generators:
			gens.extend( _listComp( x ) )
		return Nodes.ListComp( resultExpr=_expr( node.elt ), comprehensionItems=gens )
	
	
	
	# Generator expression
	def GeneratorExp(self, node):
		gens = []
		for x in node.generators:
			gens.extend( _genExp( x ) )
		return Nodes.GeneratorExpr( resultExpr=_expr( node.elt ), comprehensionItems=gens )
	
	
	
	# Dictionary literal
	def Dict(self, node):
		return Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=_expr( k ), value=[ _expr( v ) ] )   for k, v in zip( node.keys, node.values ) ] )
	
	
	
	# Yield expresion
	def Yield(self, node):
		return Nodes.YieldAtom( value=[ _expr( node.value ) ] )
	
	
	
	# Attribute ref
	def Attribute(self, node):
		return Nodes.AttributeRef( target=_expr( node.value ), name=node.attr )
		
	
	
	# Subscript	
	def Subscript(self, node):
		return Nodes.Subscript( target=_expr( node.value ), index=_expr( node.slice ) )
	
	def Index(self, node):
		return _expr( node.value )
	
	def Slice(self, node):
		def _s(x):
			s = _expr( x )
			return s   if s is not None   else   makeNullNode()
		if node.step is None:
			return Nodes.SubscriptSlice( lower=_s( node.lower ), upper=_s( node.upper ) )
		else:
			return Nodes.SubscriptLongSlice( lower=_s( node.lower ), upper=_s( node.upper ), stride=_s( node.step ) )
	
	def ExtSlice(self, node):
		return Nodes.TupleLiteral( values=[ _expr( x )   for x in node.dims ] )
	
	def Ellipsis(self, node):
		return Nodes.SubscriptEllipsis()
	
	
	
	# Call
	def Call(self, node):
		_starArg = lambda nodeClass, x:   [ nodeClass( value=_expr( x ) ) ]   if x is not None   else   []
		return Nodes.Call( target=_expr( node.func ), args=[ _expr( x )   for x in node.args ]  +  [ _expr( x )   for x in node.keywords ]  +  _starArg( Nodes.CallArgList, node.starargs )  +  _starArg( Nodes.CallKWArgList, node.kwargs ) )

	def keyword(self, node):
		return Nodes.CallKWArg( name=node.arg, value=_expr( node.value ) )
			
	def keywordType(self, node):
		return self.keyword( node )
				
	
	
	# Binary operator
	def BinOp(self, node):
		opName = _getOpNodeName( node.op )
		opNodeClass = _binOpNameTable[opName]
		return opNodeClass( x=_expr( node.left ), y=_expr( node.right ) )
		
	
	
	# Unary operator
	def UnaryOp(self, node):
		opName = _getOpNodeName( node.op )
		opNodeClass = _unaryOpNameTable[opName]
		return opNodeClass( x=_expr( node.operand ) )
		

	
	# Compare
	def Compare(self, node):
		def _cmpClass(x):
			return _cmpOpTable[ _getOpNodeName( x ) ]
		# HACK
		# Workaround for a bug in Jython; use first code segment when this is fixed
		# HACK
		#return Nodes.Cmp( x=_expr( node.left ), ops = [ _cmpClass( op )( y=_expr( comparator ) )   for op, comparator in zip( node.ops, node.comparators ) ] )
		return Nodes.Cmp( x=_expr( node.left ), ops = [ _cmpClass( node.ops[i] )( y=_expr( node.comparators[i] ) )   for i in xrange( 0, len( node.ops ) ) ] )
	
	
	
	# Boolean operations
	def BoolOp(self, node):
		opName = _getOpNodeName( node.op )
		opNodeClass = _boolOpNameTable[opName]
		return reduce( lambda left, right: opNodeClass( x=left, y=_expr( right ) ),   node.values[1:],  _expr( node.values[0] ) )
		
	
	# Lambda expression
	def Lambda(self, node):
		return Nodes.LambdaExpr( params=_lambdaArgs( node.args ), expr=_expr( node.body ) )
	
	
	
	# Conditional expression
	def IfExp(self, node):
		return Nodes.ConditionalExpr( condition=_expr( node.test ), expr=_expr( node.body ), elseExpr=_expr( node.orelse ) )
	
	
	
	
	
_augAssignOpTable = { 'Add' : '+=',  'Sub' : '-=',  'Mult' : '*=',  'Div' : '/=',  'Mod' : '%=',  'Pow' : '**=',  'LShift' : '<<=',  'RShift' : '>>=',  'BitAnd' : '&=',  'BitOr' : '|=', 'BitXor' : '^=' }
	
class _StmtImporter (_Importer):
	# Assert statement
	def Assert(self, node):
		return Nodes.AssertStmt( condition=_expr( node.test ), fail=_expr( node.msg )   if node.msg is not None   else   makeNullNode() )
	
	
	# Assignment statement
	def Assign(self, node):
		return Nodes.AssignStmt( targets=[ _target( x )   for x in node.targets ], value=_expr( node.value ) )
	
	
	# Augmented assignment statement
	def AugAssign(self, node):
		return Nodes.AugAssignStmt( op=_augAssignOpTable[ _getOpNodeName( node.op ) ], target=_target( node.target ), value=_expr( node.value ) )
	
	
	# Pass
	def Pass(self, node):
		return Nodes.PassStmt()
	
	
	# Del
	def Delete(self, node):
		if len( node.targets ) == 1:
			return Nodes.DelStmt( target=_target( node.targets[0] ) )
		else:
			return Nodes.DelStmt( target=Nodes.TupleTarget( targets=[ _target( t )   for t in node.targets ] ) )
		
	
	# Return
	def Return(self, node):
		return Nodes.ReturnStmt( value=_expr( node.value ) )

	
	# Yield
	def Yield(self, node):
		return Nodes.YieldStmt( value=_expr( node.value ) )

	
	# Raise
	def Raise(self, node):
		return Nodes.RaiseStmt( excType=_expr( node.type ), excValue=_expr( node.inst ), traceback=_expr( node.tback ) )
	

	# Break
	def Break(self, node):
		return Nodes.BreakStmt()
	
	
	# Continue
	def Continue(self, node):
		return Nodes.ContinueStmt()
	
	
	# Import
	def Import(self, node):
		return Nodes.ImportStmt( modules=[ _import( x )   for x in node.names ] )
	
	def ImportFrom(self, node):
		if len( node.names ) == 1   and   node.names[0].name == '*':
			return Nodes.FromImportAllStmt( module=Nodes.RelativeModule( name=node.module ) )
		else:
			return Nodes.FromImportStmt( module=Nodes.RelativeModule( name=node.module ), imports=[ _importFrom( x )   for x in node.names ] )
		
		
	# Global
	def Global(self, node):
		return Nodes.GlobalStmt( vars=[ Nodes.GlobalVar( name=name )   for name in node.names ] )
	
	
	# Exec
	def Exec(self, node):
		return Nodes.ExecStmt( source=_expr( node.body ), locals=_expr( node.locals ), globals=_expr( node.globals ) )
	
	
	# Print
	def Print(self, node):
		return Nodes.Call( target=Nodes.Load( name='print' ), args=[ _expr( x )   for x in node.values ] )
	
	
	
	
	# Expression
	def Expr(self, node):
		return Nodes.ExprStmt( expr=_expr( node.value ) )
	

	
	
class _ExceptHandlerImporter (_Importer):
	def __call__(self, structTab, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return Nodes.UNPARSED( value=name )
		return method( structTab, node )
	# Import statement
	def ExceptHandler(self, structTab, node):
		return Nodes.ExceptStmt( exception=_expr( node.type )   if node.type is not None   else makeNullNode(), target=_target( node.name )   if node.name is not None   else makeNullNode(), suite=_flattenedCompound( structTab, node.body ) )
		
	def ExceptHandlerType(self, structTab, node):
		return self.ExceptHandler( structTab, node )

	


class _CompoundStmtImporter (_Importer):
	def __call__(self, structTab, node, method=None):
		xs = structTab.nodesTo( node.lineno - 1 )
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return xs + [ _stmt( node ) ]
		return xs + method( structTab, node )

	
	# If
	def If(self, structTab, node):
		def _handleNode(n, bFirst):
			nodeClass = Nodes.IfStmt   if bFirst   else Nodes.ElifStmt
			result = [ nodeClass( condition=_expr( n.test ), suite= _flattenedCompound( structTab, n.body ) ) ]
			if len( n.orelse ) == 1   and   isinstance( n.orelse[0], _ast.If ):
				xs = structTab.nodesTo( n.orelse[0].lineno - 1 )
				result.extend( xs )
				result.extend( _handleNode( n.orelse[0], False ) )
			elif len( n.orelse ) == 0:
				pass
			else:
				xs = structTab.nodesTo( n.orelse[0].lineno - 1 )
				result.extend( xs )
				result.extend( [ Nodes.ElseStmt( suite=_flattenedCompound( structTab, n.orelse ) ) ] )
			return result;
			
		return _handleNode( node, True )

	
	# While
	def While(self, structTab, node):
		result = [ Nodes.WhileStmt( condition=_expr( node.test ), suite=_flattenedCompound( structTab, node.body ) ) ]
		if len( node.orelse ) > 0:
			xs = structTab.nodesTo( node.orelse[0].lineno - 1 )
			result.extend( xs )
			result.append( Nodes.ElseStmt( suite=_flattenedCompound( structTab, node.orelse ) + xs ) )
		return result

	
	# For
	def For(self, structTab, node):
		result = [ Nodes.ForStmt( target=_target( node.target ), source=_expr( node.iter ), suite=_flattenedCompound( structTab, node.body ) ) ]
		if len( node.orelse ) > 0:
			xs = structTab.nodesTo( node.orelse[0].lineno - 1 )
			result.extend( xs )
			result.append( Nodes.ElseStmt( suite=_flattenedCompound( structTab, node.orelse ) ) )
		return result
	
	
	# Try
	def TryExcept(self, structTab, node):
		result = [ Nodes.TryStmt( suite=_flattenedCompound( structTab, node.body ) ) ]
		for h in node.handlers:
			xs = structTab.nodesTo( h.lineno - 1 )
			result.extend( xs )
			result.append( _except( structTab, h ) )
		if len( node.orelse ) > 0:
			xs = structTab.nodesTo( node.orelse[0].lineno - 1 )
			result.extend( xs )
			result.append( Nodes.ElseStmt( suite=_flattenedCompound( structTab, node.orelse ) ) )
		return result
	
	def TryFinally(self, structTab, node):
		body = _flattenedCompound( structTab, node.body )
		body.append( Nodes.FinallyStmt( suite=_flattenedCompound( structTab, node.finalbody ) ) )
		return body
	
	
	# With
	def With(self, structTab, node):
		return [ Nodes.WithStmt( expr=_expr( node.expr ), target=_target( node.vars )   if node.vars is not None   else   makeNullNode(), suite=_flattenedCompound( structTab, node.body ) ) ]
	
	
	# Function
	def FunctionDef(self, structTab, node):
		result = [ _decorator( dec )   for dec in node.decorator_list ]
		params = _extractParameters( node.args )
		result.append( Nodes.DefStmt( name=node.name, params=params, suite=_flattenedCompound( structTab, node.body ) ) )
		return result
		
	

	# Class
	def ClassDef(self, structTab, node):
		if len( node.bases ) == 0:
			bases = makeNullNode()
		elif len( node.bases ) == 1:
			bases = [ _expr( node.bases[0] ) ]
		else:
			bases = [ _expr( b )   for b in node.bases ]
		return [ Nodes.ClassStmt( name=node.name, bases=bases, suite=_flattenedCompound( structTab, node.body ) ) ]
	



class _ModuleImporter (_Importer):
	def __call__(self, structTab, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return Nodes.UNPARSED( value=name )
		return method( structTab, node )

	
	# Module
	def Module(self, structTab, node):
		return Nodes.PythonModule( contents=_flattenedCompound( structTab, node.body ) )
	
	
	
	
	
_target = _TargetImporter()
_listComp = _ListCompImporter()
_genExp = _GenExprImporter()
_lambdaArgs = _LambdaArgsImporter()
_import = _ImportImporter()
_importFrom = _ImportFromImporter()
_except = _ExceptHandlerImporter()
_expr = _ExprImporter()
_decorator = _DecoratorImporter()
_stmt = _StmtImporter()
_compound = _CompoundStmtImporter()
_module = _ModuleImporter()


def _flattenedCompound(structTab, nodeList):
	xs = []
	for node in nodeList:
		xs.extend( _compound( structTab, node ) )
	xs.extend( structTab.nodesToIndentationChange() )
	return xs
	


class MisMatchedMultilineQuoteError (Exception):
	pass



def _findTripleQuote(source, pos):
	try:
		s = source.index( "'''", pos )
	except ValueError:
		s = None
	
	try:
		d = source.index( '"""', pos )
	except ValueError:
		d = None
		
	if s is None  and  d is None:
		return None, None
	elif s is not None  and  d is None:
		return s, "'''"
	elif s is None  and  d is not None:
		return d, '"""'
	else:
		if s < d:
			return s, "'''"
		else:
			return d, '"""'
		
		
def _findComment(source, pos):
	try:
		return source.index( '#', pos )
	except ValueError:
		return None
	
			
			
def _buildMultiLineQuoteTable(source):
	quoteTable = []
	pos = 0
	start = None
	quote = None
	while pos < len( source ):
		# Look for the next comment, and the next triple quote
		commentPos = _findComment( source, pos )
		if start is None:
			start, quote = _findTripleQuote( source, pos )
			
		if commentPos is None  and  start is None:
			break
		
		if commentPos is not None  and  ( start is None  or  commentPos < start ):
			# Got a comment; find the newline that ends it
			try:
				newLinePos = source.index( '\n', commentPos )
			except ValueError:
				pos = len( source )
				break
			else:
				# Move on to this position; next loop iteration
				pos = newLinePos + 1
				if start is not None  and  start < pos:
					# The start of the triple quoted string lies within this comment; ignore the quotes
					start = None
					quote = None
				else:
					continue
		
		if start is not None:
			if start < pos:
				# The start of the triple quoted string lies within this comment; ignore the quotes
				pass
			else:
				# We have a quote, look for the end quote
				try:
					stop = source.index( quote, start + 3 )
				except ValueError:
					raise MisMatchedMultilineQuoteError
				quoteTable.append( ( start, stop + 3 ) )
				pos = stop + 3
				
			start = None
			quote = None
			
				
	return quoteTable
			

def _isQuoted(charPos, quoteTable, tableIndex):
	for i in xrange( tableIndex, len( quoteTable ) ):
		start, stop = quoteTable[i]
		if charPos >= start  and  charPos < stop:
			return True
		elif charPos < start:
			return False
	return False




class _StructureTable (object):
	def __init__(self, source):
		quoteTable = _buildMultiLineQuoteTable( source )
		
		lines = source.split( '\n' )
		pos = 0
		self.table = [ None ] * len( lines )
		self.indentationChange = [ False ] * len( lines )
		tableIndex = 0
		
		prevIndentation = None
	
		for i, line in enumerate( lines ):
			lineEndPos = pos + len( line )
			
			start, stop = None, None
			while tableIndex < len( quoteTable ):
				start, stop = quoteTable[tableIndex]
				if stop > pos:
					break
				tableIndex += 1
			
			x = line.strip()
			if x == '':
				if start is None  or  lineEndPos < start:
					self.table[i] = Nodes.BlankLine()
			elif x.startswith( '#' ):
				commentPos = pos + line.index( '#' )
				if not _isQuoted( commentPos, quoteTable, tableIndex ):
					self.table[i] = Nodes.CommentStmt( comment=x[x.index( '#' )+1:] )
			
			pos += lineEndPos + 1
			
			
			if x != '':
				indentation = line[:line.index( x )]
				if prevIndentation is not None  and  indentation != prevIndentation:
					self.indentationChange[i] = True
					
				prevIndentation = indentation
					
		self.pos = 0
		
		
		
		
	
	def nodesTo(self, pos):
		if pos > self.pos:
			xs = self.table[self.pos:pos]
			xs = [ x   for x in xs   if x is not None ]
			self.pos = pos + 1
			return xs
		else:
			return []
	
	
	def nodesToIndentationChange(self):
		pos = len( self.indentationChange )
		for i in xrange( self.pos, len( self.indentationChange ) ):
			if self.indentationChange[i]:
				pos = i
				break
			
		return self.nodesTo( pos )
		
				
		
		
		


	
	
def importPy25Source(source, moduleName, mode):
	tree = compile( source, moduleName, mode, _ast.PyCF_ONLY_AST )
	structTab = _StructureTable( source )
	return _module( structTab, tree )


def importPy25File(filename):
	source = open( filename, 'r' ).read()
	tree = compile( source, filename, 'exec', _ast.PyCF_ONLY_AST )
	structTab = _StructureTable( source )
	return _module( structTab, tree )



import unittest
from BritefuryJ.DocModel import DMNode


class ImporterTestCase (unittest.TestCase):
	def _moduleTest(self, source, expectedResult):
		result = importPy25Source( source, '<test_module>', 'exec' )
		result = DMNode.coerce( result )
		expectedResult = DMNode.coerce( expectedResult )
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
		
	def _exprTest(self, source, expectedResult):
		result = importPy25Source( source, '<text_expr>', 'exec' )
		result = DMNode.coerce( result )
		expectedResult = DMNode.coerce( expectedResult )
		result = result['contents'][0]
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
	def _stmtTest(self, source, expectedResult):
		result = importPy25Source( source, '<test_stmt>', 'exec' )
		result = DMNode.coerce( result )
		expectedResult = DMNode.coerce( expectedResult )
		result = result['contents'][0]
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
	def _compStmtTest(self, source, expectedResult):
		result = importPy25Source( source, '<test_stmt>', 'exec' )
		result = DMNode.coerce( result )
		expectedResult = DMNode.coerce( expectedResult )
		result = result['contents']
		if result != expectedResult:
			print 'EXPECTED:'
			print expectedResult
			print 'RESULT:'
			print result
		self.assert_( result == expectedResult )
		
		
	def test_buildMultiLineQuoteTable(self):
		self.assert_( _buildMultiLineQuoteTable( 'abc' ) == [] )
		self.assertRaises( MisMatchedMultilineQuoteError, lambda: _buildMultiLineQuoteTable( 'abc"""' ) )
		self.assertRaises( MisMatchedMultilineQuoteError, lambda: _buildMultiLineQuoteTable( "abc'''" ) )
		self.assert_( _buildMultiLineQuoteTable( 'abc"""q"""' ) == [ ( 3, 10 ) ] )
		self.assert_( _buildMultiLineQuoteTable( 'abc"""q"""xxy' + "'''q'''" ) == [ ( 3, 10 ), ( 13, 20 ) ] )
		self.assert_( _buildMultiLineQuoteTable( "abc'''q'''xxy" + '"""q"""' ) == [ ( 3, 10 ), ( 13, 20 ) ] )
		self.assertRaises( MisMatchedMultilineQuoteError, lambda: _buildMultiLineQuoteTable( 'abc"""q"""xxy' + "'''q''" ) )

		self.assert_( _buildMultiLineQuoteTable( 'abc"""q"""xxy#' + "'''q'''" ) == [ ( 3, 10 ) ] )
		self.assert_( _buildMultiLineQuoteTable( '#abc"""q"""xxy' + "'''q'''" ) == [] )
		self.assert_( _buildMultiLineQuoteTable( '#abc"""q"""\nxxy' + "'''q'''" ) == [ ( 15, 22 ) ] )
		self.assert_( _buildMultiLineQuoteTable( 'abc"""q#"""xxy' + "'''q'''" ) == [ ( 3, 11 ),  ( 14, 21 ) ] )
		self.assert_( _buildMultiLineQuoteTable( 'abc"""q"""#xxy\n' + "'''q'''" ) == [ ( 3, 10 ), ( 15, 22 ) ] )
		self.assert_( _buildMultiLineQuoteTable( 'abc"""q"""#xxy\n' + "'''q\n'''" ) == [ ( 3, 10 ), ( 15, 23 ) ] )
		self.assert_( _buildMultiLineQuoteTable( 'abc"""q"""#xxy\n' + "'''q#'''" ) == [ ( 3, 10 ), ( 15, 23 ) ] )
		

		
		
	def testModule(self):
		self._moduleTest( 'a ** b', Nodes.PythonModule( contents=[ Nodes.Pow( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) ] ) )

	def testStmt(self):
		self._stmtTest( 'a ** b', Nodes.Pow( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )

	def testExpr(self):
		self._exprTest( 'a ** b', Nodes.Pow( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		
	

	def testName(self):
		self._exprTest( 'a', Nodes.Load( name='a' ) )
	
	def testStr(self):
		self._exprTest( "'a'", Nodes.StringLiteral( format='ascii', quotation='single', value='a' ) )
		self._exprTest( "u'a'", Nodes.StringLiteral( format='unicode', quotation='single', value=u'a' ) )
		
		
	def testNum(self):
		self._exprTest( '1', Nodes.IntLiteral( format='decimal', numType='int', value='1' ) )
		self._exprTest( '1L', Nodes.IntLiteral( format='decimal', numType='long', value='1' ) )
		self._exprTest( '1.0', Nodes.FloatLiteral( value='1.0' ) )
		self._exprTest( '1j', Nodes.ImaginaryLiteral( value='1.0j' ) )
	
	
	
	def testTuple(self):
		self._exprTest( 'a,b',  Nodes.TupleLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		
		

	def testList(self):
		self._exprTest( '[a,b]',  Nodes.ListLiteral( values=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ] ) )
		
		
	def testListComp(self):
		self._exprTest( '[a   for a in x]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a,b in x]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ] ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for [a,b] in x]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ] ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a.b in x]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a[b] in x]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='b' ) ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a in x   if q]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ), Nodes.ComprehensionIf( condition=Nodes.Load( name='q' ) ) ] ) )
		self._exprTest( '[a   for a in x   if q  if w]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ), Nodes.ComprehensionIf( condition=Nodes.Load( name='q' ) ), Nodes.ComprehensionIf( condition=Nodes.Load( name='w' ) ) ] ) )
		self._exprTest( '[a   for a in x   if q  if w   for b in f]',   Nodes.ListComp( resultExpr=Nodes.Load( name='a' ),  comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),   Nodes.ComprehensionIf( condition=Nodes.Load( name='q' ) ),   Nodes.ComprehensionIf( condition=Nodes.Load( name='w' ) ),  Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='b' ), source=Nodes.Load( name='f' ) ) ] ) )
		
		
		
	def testGeneratorExp(self):
		self._exprTest( '(a   for a in x)',   Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a,b in x)',   Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ] ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for [a,b] in x)',   Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.ListTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ] ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a.b in x)',   Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a[b] in x)',   Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='b' ) ), source=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a in x   if q)',   Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ), Nodes.ComprehensionIf( condition=Nodes.Load( name='q' ) ) ] ) )
		self._exprTest( '(a   for a in x   if q  if w)',   Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ), comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ), Nodes.ComprehensionIf( condition=Nodes.Load( name='q' ) ), Nodes.ComprehensionIf( condition=Nodes.Load( name='w' ) ) ] ) )
		self._exprTest( '(a   for a in x   if q  if w   for b in f)',  Nodes.GeneratorExpr( resultExpr=Nodes.Load( name='a' ),  comprehensionItems=[ Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='x' ) ),   Nodes.ComprehensionIf( condition=Nodes.Load( name='q' ) ),   Nodes.ComprehensionIf( condition=Nodes.Load( name='w' ) ),  Nodes.ComprehensionFor( target=Nodes.SingleTarget( name='b' ), source=Nodes.Load( name='f' ) ) ] ) )

		
	
	def testDict(self):
		self._exprTest( '{a:b, c:d}',  Nodes.DictLiteral( values=[ Nodes.DictKeyValuePair( key=Nodes.Load( name='a' ), value=[ Nodes.Load( name='b' ) ] ), Nodes.DictKeyValuePair( key=Nodes.Load( name='c' ), value=[ Nodes.Load( name='d' ) ] ) ] ) )
		
		
	def testYieldExpr(self):
		self._exprTest( '(yield a)', Nodes.YieldAtom( value=[ Nodes.Load( name='a' ) ] ) )
		
		
	def testAttribute(self):
		self._exprTest( 'a.b', Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ) )
		
		

	def testSubscript(self):
		self._exprTest( 'a[b]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a[b,c]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.TupleLiteral( values=[ Nodes.Load( name='b' ), Nodes.Load( name='c' ) ] ) ) )
		
	def testSlice(self):	
		self._exprTest( 'a[b:c]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='b' ), upper=Nodes.Load( name='c' ) ) ) )
		self._exprTest( 'a[b:]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=Nodes.Load( name='b' ), upper=makeNullNode() ) ) )
		self._exprTest( 'a[:c]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptSlice( lower=makeNullNode(), upper=Nodes.Load( name='c' ) ) ) )
		self._exprTest( 'a[b:c:d]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='b' ), upper=Nodes.Load( name='c' ), stride=Nodes.Load( name='d' ) ) ) )
		self._exprTest( 'a[b:c:]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='b' ), upper=Nodes.Load( name='c' ), stride=Nodes.Load( name='None' ) ) ) )
		self._exprTest( 'a[b::d]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=Nodes.Load( name='b' ), upper=makeNullNode(), stride=Nodes.Load( name='d' ) ) ) )
		self._exprTest( 'a[:c:d]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptLongSlice( lower=makeNullNode(), upper=Nodes.Load( name='c' ), stride=Nodes.Load( name='d' ) ) ) )
		self._exprTest( 'a[b:c,d:e]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.TupleLiteral( values=[ Nodes.SubscriptSlice( lower=Nodes.Load( name='b' ), upper=Nodes.Load( name='c' ) ), Nodes.SubscriptSlice( lower=Nodes.Load( name='d' ), upper=Nodes.Load( name='e' ) ) ] ) ) )
		self._exprTest( 'a[b:c,d:e:f]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.TupleLiteral( values=[ Nodes.SubscriptSlice( lower=Nodes.Load( name='b' ), upper=Nodes.Load( name='c' ) ), Nodes.SubscriptLongSlice( lower=Nodes.Load( name='d' ), upper=Nodes.Load( name='e' ), stride=Nodes.Load( name='f' ) ) ] ) ) )
		
	def testEllipsis(self):
		self._exprTest( 'a[...]',  Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.SubscriptEllipsis() ) )
		
	

	def testCall(self):
		self._exprTest( 'a()',   Nodes.Call( target=Nodes.Load( name='a' ), args=[] ) )
		self._exprTest( 'a(f)',   Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ) ] ) )
		self._exprTest( 'a(f,g=x)',   Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='g', value=Nodes.Load( name='x' ) ) ] ) )
		self._exprTest( 'a(f,g=x,*h)',   Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='g', value=Nodes.Load( name='x' ) ), Nodes.CallArgList( value=Nodes.Load( name='h' ) ) ] ) )
		self._exprTest( 'a(f,g=x,*h,**i)',   Nodes.Call( target=Nodes.Load( name='a' ), args=[ Nodes.Load( name='f' ), Nodes.CallKWArg( name='g', value=Nodes.Load( name='x' ) ), Nodes.CallArgList( value=Nodes.Load( name='h' ) ), Nodes.CallKWArgList( value=Nodes.Load( name='i' ) ) ] ) )
	
	
	def testBinOp(self):
		self._exprTest( 'a ** b', Nodes.Pow( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a * b', Nodes.Mul( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a / b', Nodes.Div( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a % b', Nodes.Mod( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a + b', Nodes.Add( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a - b', Nodes.Sub( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a << b', Nodes.LShift( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a >> b', Nodes.RShift( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a & b', Nodes.BitAnd( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a & b & c', Nodes.BitAnd( x=Nodes.BitAnd( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Load( name='c' ) ) )
		self._exprTest( 'a ^ b', Nodes.BitXor( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a ^ b ^ c', Nodes.BitXor( x=Nodes.BitXor( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Load( name='c' ) ) )
		self._exprTest( 'a | b', Nodes.BitOr( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a | b | c', Nodes.BitOr( x=Nodes.BitOr( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ), y=Nodes.Load( name='c' ) ) )
		
		
	def testUnaryOp(self):
		self._exprTest( '~a', Nodes.Invert( x=Nodes.Load( name='a' ) ) )
		self._exprTest( '+a', Nodes.Pos( x=Nodes.Load( name='a' ) ) )
		self._exprTest( '-a', Nodes.Negate( x=Nodes.Load( name='a' ) ) )
		self._exprTest( 'not a', Nodes.NotTest( x=Nodes.Load( name='a' ) ) )
		
		
	def testCompare(self):
		self._exprTest( 'a < b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpLt( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a < b < c', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpLt( y=Nodes.Load( name='b' ) ), Nodes.CmpOpLt( y=Nodes.Load( name='c' ) ) ] ) )
		self._exprTest( 'a <= b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpLte( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a == b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpEq( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a != b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpNeq( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a > b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpGt( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a >= b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpGte( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a is b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIs( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a is not b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIsNot( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a in b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpIn( y=Nodes.Load( name='b' ) ) ] ) )
		self._exprTest( 'a not in b', Nodes.Cmp( x=Nodes.Load( name='a' ), ops=[ Nodes.CmpOpNotIn( y=Nodes.Load( name='b' ) ) ] ) )
		
		
	def testBoolOp(self):
		self._exprTest( 'a and b', Nodes.AndTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		self._exprTest( 'a or b', Nodes.OrTest( x=Nodes.Load( name='a' ), y=Nodes.Load( name='b' ) ) )
		
		
	def testLambda(self):
		self._exprTest( 'lambda: x', Nodes.LambdaExpr( params=[], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g,m=a: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g,m=a,n=b: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g,m=a,n=b,*p: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.SimpleParam( name='g' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.DefaultValueParam( name='n', defaultValue=Nodes.Load( name='b' ) ), Nodes.ParamList( name='p' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,m=a,*p,**w: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,m=a,*p: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,m=a,**w: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.KWParamList( name='w' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda f,*p,**w: x', Nodes.LambdaExpr( params=[ Nodes.SimpleParam( name='f' ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda m=a,*p,**w: x', Nodes.LambdaExpr( params=[ Nodes.DefaultValueParam( name='m', defaultValue=Nodes.Load( name='a' ) ), Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda *p,**w: x', Nodes.LambdaExpr( params=[ Nodes.ParamList( name='p' ), Nodes.KWParamList( name='w' ) ], expr=Nodes.Load( name='x' ) ) )
		self._exprTest( 'lambda **w: x', Nodes.LambdaExpr( params=[ Nodes.KWParamList( name='w' ) ], expr=Nodes.Load( name='x' ) ) )
		
		
	def testConditionalExpr(self):
		self._exprTest( 'a   if b   else   c', Nodes.ConditionalExpr( condition=Nodes.Load( name='b' ), expr=Nodes.Load( name='a' ), elseExpr=Nodes.Load( name='c' ) ) )
		
		

	
	def testAssert(self):
		self._stmtTest( 'assert a', Nodes.AssertStmt( condition=Nodes.Load( name='a' ), fail=makeNullNode() ) )
		self._stmtTest( 'assert a,b', Nodes.AssertStmt( condition=Nodes.Load( name='a' ), fail=Nodes.Load( name='b' ) ) )
		

	def testAssign(self):
		self._stmtTest( 'a=x', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.Load( name='x' ) ) )
		self._stmtTest( 'a,b=c,d=x', Nodes.AssignStmt( targets=[ Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='a' ), Nodes.SingleTarget( name='b' ) ] ),  Nodes.TupleTarget( targets=[ Nodes.SingleTarget( name='c' ), Nodes.SingleTarget( name='d' ) ] ) ], value=Nodes.Load( name='x' ) ) )
		self._stmtTest( 'a=yield x', Nodes.AssignStmt( targets=[ Nodes.SingleTarget( name='a' ) ], value=Nodes.YieldAtom( value=[ Nodes.Load( name='x' ) ] ) ) )
	
		
	def testAugAssignStmt(self):
		self._stmtTest( 'a += b', Nodes.AugAssignStmt( op='+=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a -= b', Nodes.AugAssignStmt( op='-=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a *= b', Nodes.AugAssignStmt( op='*=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a /= b', Nodes.AugAssignStmt( op='/=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a %= b', Nodes.AugAssignStmt( op='%=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a **= b', Nodes.AugAssignStmt( op='**=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a >>= b', Nodes.AugAssignStmt( op='>>=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a <<= b', Nodes.AugAssignStmt( op='<<=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a &= b', Nodes.AugAssignStmt( op='&=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a ^= b', Nodes.AugAssignStmt( op='^=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a |= b', Nodes.AugAssignStmt( op='|=', target=Nodes.SingleTarget( name='a' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a.b += b', Nodes.AugAssignStmt( op='+=', target=Nodes.AttributeRef( target=Nodes.Load( name='a' ), name='b' ), value=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'a[x] += b', Nodes.AugAssignStmt( op='+=', target=Nodes.Subscript( target=Nodes.Load( name='a' ), index=Nodes.Load( name='x' ) ), value=Nodes.Load( name='b' ) ) )

	def testPassStmt(self):
		self._stmtTest( 'pass', Nodes.PassStmt() )
		
		
	def testDelStmt(self):
		self._stmtTest( 'del x', Nodes.DelStmt( target=Nodes.SingleTarget( name='x' ) ) )
		
		
	def testReturnStmt(self):
		self._stmtTest( 'return x', Nodes.ReturnStmt( value=Nodes.Load( name='x' ) ) )
		
	
	def testYieldStmt(self):
		self._stmtTest( 'yield x', Nodes.YieldAtom( value=[ Nodes.Load( name='x' ) ] ) )
		
		
	def testRaiseStmt(self):
		self._stmtTest( 'raise', Nodes.RaiseStmt( excType=makeNullNode(), excValue=makeNullNode(), traceback=makeNullNode() ) )
		self._stmtTest( 'raise x', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=makeNullNode(), traceback=makeNullNode() ) )
		self._stmtTest( 'raise x,y', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=Nodes.Load( name='y' ), traceback=makeNullNode() ) )
		self._stmtTest( 'raise x,y,z', Nodes.RaiseStmt( excType=Nodes.Load( name='x' ), excValue=Nodes.Load( name='y' ), traceback=Nodes.Load( name='z' ) ) )
		
		
	def testBreakStmt(self):
		self._stmtTest( 'break', Nodes.BreakStmt() )
		
		
	def testContinueStmt(self):
		self._stmtTest( 'continue', Nodes.ContinueStmt() )
		
		
	def testImportStmt(self):
		self._stmtTest( 'import a', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a' ) ] ) )
		self._stmtTest( 'import a.b', Nodes.ImportStmt( modules=[ Nodes.ModuleImport( name='a.b' ) ] ) )
		self._stmtTest( 'import a.b as x', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._stmtTest( 'import a.b as x, c.d as y', Nodes.ImportStmt( modules=[ Nodes.ModuleImportAs( name='a.b', asName='x' ), Nodes.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._stmtTest( 'from x import a', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._stmtTest( 'from x import a as p', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._stmtTest( 'from x import a as p, b as q', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._stmtTest( 'from x import (a)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._stmtTest( 'from x import (a,)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImport( name='a' ) ] ) )
		self._stmtTest( 'from x import (a as p)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._stmtTest( 'from x import (a as p,)', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._stmtTest( 'from x import ( a as p, b as q )', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._stmtTest( 'from x import ( a as p, b as q, )', Nodes.FromImportStmt( module=Nodes.RelativeModule( name='x' ), imports=[ Nodes.ModuleContentImportAs( name='a', asName='p' ), Nodes.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._stmtTest( 'from x import *', Nodes.FromImportAllStmt( module=Nodes.RelativeModule( name='x' ) ) )
		
		
	def testGlobalStmt(self):
		self._stmtTest( 'global x', Nodes.GlobalStmt( vars=[ Nodes.GlobalVar( name='x' ) ] ) )
		self._stmtTest( 'global x, y', Nodes.GlobalStmt( vars=[ Nodes.GlobalVar( name='x' ), Nodes.GlobalVar( name='y' ) ] ) )
	
		
	def testExecStmt(self):
		self._stmtTest( 'exec a', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=makeNullNode(), globals=makeNullNode() ) )
		self._stmtTest( 'exec a in b', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=makeNullNode(), globals=Nodes.Load( name='b' ) ) )
		self._stmtTest( 'exec a in b,c', Nodes.ExecStmt( source=Nodes.Load( name='a' ), locals=Nodes.Load( name='c' ), globals=Nodes.Load( name='b' ) ) )
		
		
	def testPrintnl(self):
		self._stmtTest( 'print x', Nodes.Call( target=Nodes.Load( name='print' ), args=[ Nodes.Load( name='x' ) ] ) )
		self._stmtTest( 'print x,y', Nodes.Call( target=Nodes.Load( name='print' ), args=[ Nodes.Load( name='x' ), Nodes.Load( name='y' ) ] ) )
		
		
	def testIf(self):
		src1 = \
"""
if a:
	x"""
		src2 = \
"""
if a:
	x
elif b:
	y"""
		src3 = \
"""
if a:
	x
else:
	z"""
		src4 = \
"""
if a:
	x
elif b:
	y
else:
	z"""
		self._compStmtTest( src1, [ Nodes.BlankLine(), Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src2, [ Nodes.BlankLine(), Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.Load( name='x' ) ] ), Nodes.ElifStmt( condition=Nodes.Load( name='b' ), suite=[ Nodes.Load( name='y' ) ] ) ] )
		self._compStmtTest( src3, [ Nodes.BlankLine(), Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.Load( name='x' ) ] ), Nodes.ElseStmt( suite=[ Nodes.Load( name='z' ) ] ) ] )
		self._compStmtTest( src4, [ Nodes.BlankLine(), Nodes.IfStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.Load( name='x' ) ] ), Nodes.ElifStmt( condition=Nodes.Load( name='b' ), suite=[ Nodes.Load( name='y' ) ] ), Nodes.ElseStmt( suite=[ Nodes.Load( name='z' ) ] ) ] )

		
	def testWhile(self):
		src1 = \
"""
while a:
	x
"""
		src2 = \
"""
while a:
	x
else:
	z"""
		self._compStmtTest( src1, [ Nodes.BlankLine(), Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src2, [ Nodes.BlankLine(), Nodes.WhileStmt( condition=Nodes.Load( name='a' ), suite=[ Nodes.Load( name='x' ) ] ), Nodes.ElseStmt( suite=[ Nodes.Load( name='z' ) ] ) ] )

		
		
	def testFor(self):
		src1 = \
"""
for a in q:
	x"""
		src2 = \
"""
for a in q:
	x
else:
	z"""
		self._compStmtTest( src1, [ Nodes.BlankLine(), Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='q' ), suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src2, [ Nodes.BlankLine(), Nodes.ForStmt( target=Nodes.SingleTarget( name='a' ), source=Nodes.Load( name='q' ), suite=[ Nodes.Load( name='x' ) ] ), Nodes.ElseStmt( suite=[ Nodes.Load( name='z' ) ] ) ] )

		
		
	def testTry(self):
		src1 = \
"""
try:
	x
except:
	p
except a:
	q
except a,b:
	r
"""
		src2 = \
"""
try:
	x
except:
	p
else:
	z"""
		src3 = \
"""
try:
	x
except:
	p
finally:
	z"""
		self._compStmtTest( src1, [ Nodes.BlankLine(), Nodes.TryStmt( suite=[ Nodes.Load( name='x' ) ] ),  Nodes.ExceptStmt( exception=makeNullNode(), target=makeNullNode(), suite=[ Nodes.Load( name='p' ) ] ),   Nodes.ExceptStmt( exception=Nodes.Load( name='a' ), target=makeNullNode(), suite=[ Nodes.Load( name='q' ) ] ),    Nodes.ExceptStmt( exception=Nodes.Load( name='a' ), target=Nodes.SingleTarget( name='b' ), suite=[ Nodes.Load( name='r' ) ] ) ] )
		self._compStmtTest( src2, [ Nodes.BlankLine(), Nodes.TryStmt( suite=[ Nodes.Load( name='x' ) ] ),  Nodes.ExceptStmt( exception=makeNullNode(), target=makeNullNode(), suite=[ Nodes.Load( name='p' ) ] ),   Nodes.ElseStmt( suite=[ Nodes.Load( name='z' ) ] ) ] )
		self._compStmtTest( src3, [ Nodes.BlankLine(), Nodes.TryStmt( suite=[ Nodes.Load( name='x' ) ] ),  Nodes.ExceptStmt( exception=makeNullNode(), target=makeNullNode(), suite=[ Nodes.Load( name='p' ) ] ),   Nodes.FinallyStmt( suite=[ Nodes.Load( name='z' ) ] ) ] )

		
		
	#def testWith(self):
		#src1 = \
#"""
#with a:
	#x
#"""
		#src2 = \
#"""
#with a as b:
	#x
#"""
		#self._compStmtTest( src1, [ [ 'withStmt', [ 'var', 'a' ], makeNullNode(), [ [ 'var', 'x' ] ] ] ] )
		#self._compStmtTest( src2, [ [ 'withStmt', [ 'var', 'a' ], [ 'singleTarget', 'b' ], [ [ 'var', 'x' ] ] ] ] )

	
	def testFunction(self):
		src1 = \
"""
def f():
	x
"""
		src2 = \
"""
def f(a,b=q,*c,**d):
	x
"""
		src3 = \
"""
@p
def f():
	x"""
		src4 = \
"""
@p(h)
def f():
	x"""
		src5 = \
"""
@p(h)
@q(j)
def f():
	x"""
		self._compStmtTest( src1, [ Nodes.BlankLine(), Nodes.DefStmt( name='f', params=[], suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src2, [ Nodes.BlankLine(), Nodes.DefStmt( name='f', params=[ Nodes.SimpleParam( name='a' ), Nodes.DefaultValueParam( name='b', defaultValue=Nodes.Load( name='q' ) ), Nodes.ParamList( name='c' ), Nodes.KWParamList( name='d' ) ], suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src3, [ Nodes.BlankLine(), Nodes.DecoStmt( name='p', args=makeNullNode() ), Nodes.DefStmt( name='f', params=[], suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src4, [ Nodes.BlankLine(), Nodes.DecoStmt( name='p', args=[ Nodes.Load( name='h' ) ] ), Nodes.DefStmt( name='f', params=[], suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src5, [ Nodes.BlankLine(), Nodes.DecoStmt( name='p', args=[ Nodes.Load( name='h' ) ] ), Nodes.DecoStmt( name='q', args=[ Nodes.Load( name='j' ) ] ), Nodes.DefStmt( name='f', params=[], suite=[ Nodes.Load( name='x' ) ] ) ] )

		
	def testClass(self):
		src1 = \
"""
class Q:
	x
"""
		src2 = \
"""
class Q (object):
	x
"""
		src3 = \
"""
class Q (a,b):
	x
"""
		self._compStmtTest( src1, [ Nodes.BlankLine(), Nodes.ClassStmt( name='Q', bases=makeNullNode(), suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src2, [ Nodes.BlankLine(), Nodes.ClassStmt( name='Q', bases=[ Nodes.Load( name='object' ) ], suite=[ Nodes.Load( name='x' ) ] ) ] )
		self._compStmtTest( src3, [ Nodes.BlankLine(), Nodes.ClassStmt( name='Q', bases=[ Nodes.Load( name='a' ), Nodes.Load( name='b' ) ], suite=[ Nodes.Load( name='x' ) ] ) ] )

		
if __name__ == '__main__':
	unittest.main()
		
