##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import _ast


from GSymCore.Languages.Python25 import Schema



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
				return Schema.UNPARSED( value=name )
		return method( node )


	
def _extractParameters(node):
	numDefaultParams = len( node.defaults )
	numSimpleParams = len( node.args )  -  ( numDefaultParams )
	
	params = [ Schema.SimpleParam( name=name.id )   for name in node.args[:numSimpleParams] ]
	params.extend( [ Schema.DefaultValueParam( name=name.id, defaultValue=_expr( value ) )   for name, value in zip( node.args[numSimpleParams:], node.defaults ) ] )
	
	if node.vararg is not None:
		params.append( Schema.ParamList( name=node.vararg ) )
	if node.kwarg is not None:
		params.append( Schema.KWParamList( name=node.kwarg ) )
	
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
		return Schema.SingleTarget( name=node.id )
	
	def Attribute(self, node):
		return Schema.AttributeRef( target=_expr( node.value ), name=node.attr )
	
	def Tuple(self, node):
		return Schema.TupleTarget( targets=[ _target( x )   for x in node.elts ] )
	
	def List(self, node):
		return Schema.ListTarget( targets=[ _target( x )   for x in node.elts ] )




	
class _ListCompImporter (_Importer):
	# List comprehension
	def comprehension(self, node):
		return [ Schema.ComprehensionFor( target=_target( node.target ), source=_expr( node.iter ) ) ]  +  [ Schema.ComprehensionIf( condition=_expr( x ) )   for x in node.ifs ]
	
	def comprehensionType(self, node):
		return self.comprehension( node )


class _GenExprImporter (_Importer):
	# Generator expression comprehension
	def comprehension(self, node):
		return [ Schema.ComprehensionFor( target=_target( node.target ), source=_expr( node.iter ) ) ]  +  [ Schema.ComprehensionIf( condition=_expr( x ) )   for x in node.ifs ]

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
			return Schema.ModuleImport( name=node.name )
		else:
			return Schema.ModuleImportAs( name=node.name, asName=node.asname )
		
	def aliasType(self, node):
		return self.alias( node )


class _ImportFromImporter (_Importer):
	# Import statement
	def alias(self, node):
		if node.asname is None:
			return Schema.ModuleContentImport( name=node.name )
		else:
			return Schema.ModuleContentImportAs( name=node.name, asName=node.asname )
		
	def aliasType(self, node):
		return self.alias( node )

	
class _DecoratorImporter (_Importer):
	def Name(self, node):
		return Schema.Decorator( name=node.id, args=None )

	def Call(self, node):
		_starArg = lambda name, x:   [ [ name, _expr( x ) ] ]   if x is not None   else   []
		return Schema.Decorator( name=node.func.id, args=[ _expr( x )   for x in node.args ]  +  [ _expr( x )   for x in node.keywords ]  +  _starArg( 'argList', node.starargs )  +  _starArg( 'kwArgList', node.kwargs ) )

	def keyword(self, node):
		return Schema.CallKWArg( name=node.arg, value=_expr( node.value ) )

	def keywordType(self, node):
		return self.keyword( node )


	
	
_binOpNameTable = { 'Pow' : Schema.Pow,  'Mult' : Schema.Mul,  'Div' : Schema.Div,  'Mod' : Schema.Mod,  'Add' : Schema.Add,  'Sub' : Schema.Sub,  'LShift' : Schema.LShift,  'RShift' : Schema.RShift,  'BitAnd' : Schema.BitAnd,  'BitXor' : Schema.BitXor,  'BitOr' : Schema.BitOr }	
_unaryOpNameTable = { 'Invert' : Schema.Invert,  'USub' : Schema.Negate,  'UAdd' : Schema.Pos,  'Not' : Schema.NotTest }	
_boolOpNameTable = { 'And' : Schema.AndTest,  'Or' : Schema.OrTest }	
_cmpOpTable = { 'Lt' : Schema.CmpOpLt,  'LtE' : Schema.CmpOpLte,  'Eq' : Schema.CmpOpEq,  'NotEq' : Schema.CmpOpNeq,  'Gt' : Schema.CmpOpGt,  'GtE' : Schema.CmpOpGte,  'Is' : Schema.CmpOpIs,  'IsNot' : Schema.CmpOpIsNot,  'In' : Schema.CmpOpIn,  'NotIn' : Schema.CmpOpNotIn }
	
	
def _getOpNodeName(op):
	if hasattr( op, 'name' ):
		return op.name()
	else:
		return _getNodeTypeName( op )

	
class _ExprImporter (_Importer):
	def __call__(self, node, method=None):
		if node is None:
			return None
		else:
			if method is None:
				name = _getNodeTypeName( node )
				try:
					method = getattr( self, name )
				except AttributeError:
					return Schema.UNPARSED( value=name )
			return method( node )

	
	# Expression
	def Expression(self, node):
		return _expr( node.body )
		
		
		
	# Number literal
	def Num(self, node):
		value = node.n
		if isinstance( value, int ):
			return Schema.IntLiteral( format='decimal', numType='int', value=repr( value ) )
		elif isinstance( value, long ):
			return Schema.IntLiteral( format='decimal', numType='long', value=repr( value )[:-1] )
		elif isinstance( value, float ):
			return Schema.FloatLiteral( value=repr( value ) )
		elif isinstance( value, complex ):
			if value.real == 0.0:
				return Schema.ImaginaryLiteral( value=repr( value.imag ) + 'j' )
			else:
				return Schema.Add( x=Schema.FloatLiteral( value=repr( value.real ) ), y=Schema.ImaginaryLiteral( value=repr( value.imag ) + 'j' ) )
		else:
			print 'Const: could not handle value', value
			raise ValueError
		
	
	# String literal
	def Str(self, node):
		value = node.s
		if isinstance( value, str ):
			return Schema.StringLiteral( format='ascii', quotation='single', value=repr( value )[1:-1] )
		elif isinstance( value, unicode ):
			return Schema.StringLiteral( format='unicode', quotation='single', value=repr( value )[2:-1] )
		else:
			print 'Const: could not handle value', value
			raise ValueError

		
		
	# Variable ref
	def Name(self, node):
		return Schema.Load( name=node.id )


	# Tuple literal
	def Tuple(self, node):
		return Schema.TupleLiteral( values=[ _expr( x )   for x in node.elts ] )
	
	
	
	# List literal
	def List(self, node):
		return Schema.ListLiteral( values=[ _expr( x )   for x in node.elts ] )
	
	
	
	# List comprehension
	def ListComp(self, node):
		gens = []
		for x in node.generators:
			gens.extend( _listComp( x ) )
		return Schema.ListComp( resultExpr=_expr( node.elt ), comprehensionItems=gens )
	
	
	
	# Generator expression
	def GeneratorExp(self, node):
		gens = []
		for x in node.generators:
			gens.extend( _genExp( x ) )
		return Schema.GeneratorExpr( resultExpr=_expr( node.elt ), comprehensionItems=gens )
	
	
	
	# Dictionary literal
	def Dict(self, node):
		return Schema.DictLiteral( values=[ Schema.DictKeyValuePair( key=_expr( k ), value=_expr( v ) )   for k, v in zip( node.keys, node.values ) ] )
	
	
	
	# Yield expresion
	def Yield(self, node):
		return Schema.YieldExpr( value=_expr( node.value ) )
	
	
	
	# Attribute ref
	def Attribute(self, node):
		return Schema.AttributeRef( target=_expr( node.value ), name=node.attr )
		
	
	
	# Subscript	
	def Subscript(self, node):
		return Schema.Subscript( target=_expr( node.value ), index=_expr( node.slice ) )
	
	def Index(self, node):
		return _expr( node.value )
	
	def Slice(self, node):
		def _s(x):
			s = _expr( x )
			return s   if s is not None   else   None
		if node.step is None:
			return Schema.SubscriptSlice( lower=_s( node.lower ), upper=_s( node.upper ) )
		else:
			return Schema.SubscriptLongSlice( lower=_s( node.lower ), upper=_s( node.upper ), stride=_s( node.step ) )
	
	def ExtSlice(self, node):
		return Schema.TupleLiteral( values=[ _expr( x )   for x in node.dims ] )
	
	def Ellipsis(self, node):
		return Schema.SubscriptEllipsis()
	
	
	
	# Call
	def Call(self, node):
		_starArg = lambda nodeClass, x:   [ nodeClass( value=_expr( x ) ) ]   if x is not None   else   []
		return Schema.Call( target=_expr( node.func ), args=[ _expr( x )   for x in node.args ]  +  [ _expr( x )   for x in node.keywords ]  +  _starArg( Schema.CallArgList, node.starargs )  +  _starArg( Schema.CallKWArgList, node.kwargs ) )

	def keyword(self, node):
		return Schema.CallKWArg( name=node.arg, value=_expr( node.value ) )
			
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
		#return Schema.Cmp( x=_expr( node.left ), ops = [ _cmpClass( op )( y=_expr( comparator ) )   for op, comparator in zip( node.ops, node.comparators ) ] )
		return Schema.Cmp( x=_expr( node.left ), ops = [ _cmpClass( node.ops[i] )( y=_expr( node.comparators[i] ) )   for i in xrange( 0, len( node.ops ) ) ] )
	
	
	
	# Boolean operations
	def BoolOp(self, node):
		opName = _getOpNodeName( node.op )
		opNodeClass = _boolOpNameTable[opName]
		return reduce( lambda left, right: opNodeClass( x=left, y=_expr( right ) ),   node.values[1:],  _expr( node.values[0] ) )
		
	
	# Lambda expression
	def Lambda(self, node):
		return Schema.LambdaExpr( params=_lambdaArgs( node.args ), expr=_expr( node.body ) )
	
	
	
	# Conditional expression
	def IfExp(self, node):
		return Schema.ConditionalExpr( condition=_expr( node.test ), expr=_expr( node.body ), elseExpr=_expr( node.orelse ) )
	
	
	
	
	
_augAssignOpTable = { 'Add' : '+=',  'Sub' : '-=',  'Mult' : '*=',  'Div' : '/=',  'Mod' : '%=',  'Pow' : '**=',  'LShift' : '<<=',  'RShift' : '>>=',  'BitAnd' : '&=',  'BitOr' : '|=', 'BitXor' : '^=' }
	
class _StmtImporter (_Importer):
	# Assert statement
	def Assert(self, node):
		return Schema.AssertStmt( condition=_expr( node.test ), fail=_expr( node.msg )   if node.msg is not None   else   None )
	
	
	# Assignment statement
	def Assign(self, node):
		return Schema.AssignStmt( targets=[ _target( x )   for x in node.targets ], value=_expr( node.value ) )
	
	
	# Augmented assignment statement
	def AugAssign(self, node):
		return Schema.AugAssignStmt( op=_augAssignOpTable[ _getOpNodeName( node.op ) ], target=_target( node.target ), value=_expr( node.value ) )
	
	
	# Pass
	def Pass(self, node):
		return Schema.PassStmt()
	
	
	# Del
	def Delete(self, node):
		if len( node.targets ) == 1:
			return Schema.DelStmt( target=_target( node.targets[0] ) )
		else:
			return Schema.DelStmt( target=Schema.TupleTarget( targets=[ _target( t )   for t in node.targets ] ) )
		
	
	# Return
	def Return(self, node):
		return Schema.ReturnStmt( value=_expr( node.value ) )

	
	# Yield
	def Yield(self, node):
		return Schema.YieldStmt( value=_expr( node.value ) )

	
	# Raise
	def Raise(self, node):
		return Schema.RaiseStmt( excType=_expr( node.type ), excValue=_expr( node.inst ), traceback=_expr( node.tback ) )
	

	# Break
	def Break(self, node):
		return Schema.BreakStmt()
	
	
	# Continue
	def Continue(self, node):
		return Schema.ContinueStmt()
	
	
	# Import
	def Import(self, node):
		return Schema.ImportStmt( modules=[ _import( x )   for x in node.names ] )
	
	def ImportFrom(self, node):
		if len( node.names ) == 1   and   node.names[0].name == '*':
			return Schema.FromImportAllStmt( module=Schema.RelativeModule( name=node.module ) )
		else:
			return Schema.FromImportStmt( module=Schema.RelativeModule( name=node.module ), imports=[ _importFrom( x )   for x in node.names ] )
		
		
	# Global
	def Global(self, node):
		return Schema.GlobalStmt( vars=[ Schema.GlobalVar( name=name )   for name in node.names ] )
	
	
	# Exec
	def Exec(self, node):
		return Schema.ExecStmt( source=_expr( node.body ), globals=_expr( node.globals ), locals=_expr( node.locals ) )
	
	
	# Print
	def Print(self, node):
		return Schema.PrintStmt( destination=_expr( node.dest ), values=[ _expr( x )   for x in node.values ] )
	
	
	
	
	# Expression
	def Expr(self, node):
		return Schema.ExprStmt( expr=_expr( node.value ) )
	

	
	
class _ExceptHandlerImporter (_Importer):
	def __call__(self, structTab, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return Schema.UNPARSED( value=name )
		return method( structTab, node )
	
	# Except block
	def ExceptHandler(self, structTab, node):
		exception = _expr( node.type )   if node.type is not None   else None
		target = _target( node.name )   if node.name is not None   else None
		suite = _flattenedCompound( structTab, node.body )
		
		return Schema.ExceptBlock( exception=exception, target=target, suite=suite )
		
	def ExceptHandlerType(self, structTab, node):
		return self.ExceptBlock( structTab, node )

	


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
		condition = _expr( node.test )
		suite = _flattenedCompound( structTab, node.body )
		
		elifBlocks = []
		src = node
		while len( src.orelse ) == 1   and   isinstance( src.orelse[0], _ast.If ):
			src = src.orelse[0]
			e = Schema.ElifBlock( condition=_expr( src.test ), suite=_flattenedCompound( structTab, src.body ) )
			elifBlocks.append( e )
		
		elseSuite = _flattenedCompound( structTab, src.orelse )   if len( src.orelse ) > 0   else   None
			
		return [ Schema.IfStmt( condition=condition, suite=suite, elifBlocks=elifBlocks, elseSuite=elseSuite ) ]

	
	# While
	def While(self, structTab, node):
		condition = _expr( node.test )
		suite = _flattenedCompound( structTab, node.body )
		elseSuite = _flattenedCompound( structTab, node.orelse )   if len( node.orelse ) > 0   else   None
		return [ Schema.WhileStmt( condition=condition, suite=suite, elseSuite=elseSuite ) ]

	
	# For
	def For(self, structTab, node):
		target = _target( node.target )
		source = _expr( node.iter )
		suite = _flattenedCompound( structTab, node.body )
		elseSuite = _flattenedCompound( structTab, node.orelse )   if len( node.orelse ) > 0   else   None
		return [ Schema.ForStmt( target=target, source=source, suite=suite, elseSuite=elseSuite ) ]
	
	
	# Try
	def TryExcept(self, structTab, node):
		suite=_flattenedCompound( structTab, node.body )
		exceptBlocks = [ _except( structTab, h )   for h in node.handlers ]
		elseSuite = _flattenedCompound( structTab, node.orelse )   if len( node.orelse ) > 0   else   None
		return [ Schema.TryStmt( suite=suite, exceptBlocks=exceptBlocks, elseSuite=elseSuite, finallySuite=None ) ]

	def TryFinally(self, structTab, node):
		body = node.body[0]
		if isinstance( node.body[0], _ast.TryExcept ):
			body = node.body[0]
			suite=_flattenedCompound( structTab, body.body )
			exceptBlocks = [ _except( structTab, h )   for h in body.handlers ]
			elseSuite = _flattenedCompound( structTab, body.orelse )   if len( body.orelse ) > 0   else   None
		else:
			suite=_flattenedCompound( structTab, node.body )
			finallySuite = _flattenedCompound( structTab, node.finalbody )   if len( node.finalbody ) > 0   else   None
			return [ Schema.TryStmt( suite=suite, exceptBlocks=[], elseSuite=None, finallySuite=finallySuite ) ]
		finallySuite = _flattenedCompound( structTab, node.finalbody )   if len( node.finalbody ) > 0   else   None
		return [ Schema.TryStmt( suite=suite, exceptBlocks=exceptBlocks, elseSuite=elseSuite, finallySuite=finallySuite ) ]
	
	
	# With
	def With(self, structTab, node):
		expr = _expr( node.expr )
		target = _target( node.vars )   if node.vars is not None   else   None
		suite = _flattenedCompound( structTab, node.body )
		return [ Schema.WithStmt( expr=expr, target=target, suite=suite ) ]
	
	
	# Function
	def FunctionDef(self, structTab, node):
		decorators = [ _decorator( dec )   for dec in node.decorator_list ]
		name = node.name
		params = _extractParameters( node.args )
		suite = _flattenedCompound( structTab, node.body )
		return [ Schema.DefStmt( decorators=decorators, name=name, params=params, suite=suite ) ]
		
	

	# Class
	def ClassDef(self, structTab, node):
		if len( node.bases ) == 0:
			bases = None
		elif len( node.bases ) == 1:
			bases = [ _expr( node.bases[0] ) ]
		else:
			bases = [ _expr( b )   for b in node.bases ]
		return [ Schema.ClassStmt( name=node.name, bases=bases, suite=_flattenedCompound( structTab, node.body ) ) ]
	



class _ModuleImporter (_Importer):
	def __call__(self, structTab, node, method=None):
		if method is None:
			name = _getNodeTypeName( node )
			try:
				method = getattr( self, name )
			except AttributeError:
				return Schema.UNPARSED( value=name )
		return method( structTab, node )

	
	# Module
	def Module(self, structTab, node):
		return Schema.PythonModule( suite=_flattenedCompound( structTab, node.body ) )
	
	
	
	
	
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
	xs.extend( structTab.nodesToNextStatement() )
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
		self.isStatement = [ False ] * len( lines )
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
					self.table[i] = Schema.BlankLine()
			elif x.startswith( '#' ):
				commentPos = pos + line.index( '#' )
				if not _isQuoted( commentPos, quoteTable, tableIndex ):
					self.table[i] = Schema.CommentStmt( comment=x[x.index( '#' )+1:] )
			else:
				self.isStatement[i] = True
			
			pos += lineEndPos + 1
					
		self.pos = 0
		
		
		
		
	
	def nodesTo(self, pos):
		if pos > self.pos:
			xs = self.table[self.pos:pos]
			xs = [ x   for x in xs   if x is not None ]
			self.pos = pos + 1
			return xs
		else:
			return []
	
	
	def nodesToNextStatement(self):
		pos = len( self.isStatement )
		for i in xrange( self.pos, len( self.isStatement ) ):
			if self.isStatement[i]:
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
		result = result['suite'][0]
		self.assert_( result.isInstanceOf( Schema.ExprStmt ) )
		result = result['expr']
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
		result = result['suite'][0]
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
		result = result['suite']
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
		self._moduleTest( 'a ** b', Schema.PythonModule( suite=[ Schema.ExprStmt( expr=Schema.Pow( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ) ] ) )

	def testStmt(self):
		self._stmtTest( 'a ** b', Schema.ExprStmt( expr=Schema.Pow( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) ) )

	def testExpr(self):
		self._exprTest( 'a ** b', Schema.Pow( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		
	

	def testName(self):
		self._exprTest( 'a', Schema.Load( name='a' ) )
	
	def testStr(self):
		self._exprTest( "'a'", Schema.StringLiteral( format='ascii', quotation='single', value='a' ) )
		self._exprTest( "u'a'", Schema.StringLiteral( format='unicode', quotation='single', value=u'a' ) )
		
		
	def testNum(self):
		self._exprTest( '1', Schema.IntLiteral( format='decimal', numType='int', value='1' ) )
		self._exprTest( '1L', Schema.IntLiteral( format='decimal', numType='long', value='1' ) )
		self._exprTest( '1.0', Schema.FloatLiteral( value='1.0' ) )
		self._exprTest( '1j', Schema.ImaginaryLiteral( value='1.0j' ) )
	
	
	
	def testTuple(self):
		self._exprTest( 'a,b',  Schema.TupleLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ] ) )
		
		

	def testList(self):
		self._exprTest( '[a,b]',  Schema.ListLiteral( values=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ] ) )
		
		
	def testListComp(self):
		self._exprTest( '[a   for a in x]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a,b in x]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ), Schema.SingleTarget( name='b' ) ] ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for [a,b] in x]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.ListTarget( targets=[ Schema.SingleTarget( name='a' ), Schema.SingleTarget( name='b' ) ] ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a.b in x]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a[b] in x]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='b' ) ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '[a   for a in x   if q]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ), Schema.ComprehensionIf( condition=Schema.Load( name='q' ) ) ] ) )
		self._exprTest( '[a   for a in x   if q  if w]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ), Schema.ComprehensionIf( condition=Schema.Load( name='q' ) ), Schema.ComprehensionIf( condition=Schema.Load( name='w' ) ) ] ) )
		self._exprTest( '[a   for a in x   if q  if w   for b in f]',   Schema.ListComp( resultExpr=Schema.Load( name='a' ),  comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ),   Schema.ComprehensionIf( condition=Schema.Load( name='q' ) ),   Schema.ComprehensionIf( condition=Schema.Load( name='w' ) ),  Schema.ComprehensionFor( target=Schema.SingleTarget( name='b' ), source=Schema.Load( name='f' ) ) ] ) )
		
		
		
	def testGeneratorExp(self):
		self._exprTest( '(a   for a in x)',   Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a,b in x)',   Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ), Schema.SingleTarget( name='b' ) ] ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for [a,b] in x)',   Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.ListTarget( targets=[ Schema.SingleTarget( name='a' ), Schema.SingleTarget( name='b' ) ] ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a.b in x)',   Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a[b] in x)',   Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='b' ) ), source=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( '(a   for a in x   if q)',   Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ), Schema.ComprehensionIf( condition=Schema.Load( name='q' ) ) ] ) )
		self._exprTest( '(a   for a in x   if q  if w)',   Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ), comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ), Schema.ComprehensionIf( condition=Schema.Load( name='q' ) ), Schema.ComprehensionIf( condition=Schema.Load( name='w' ) ) ] ) )
		self._exprTest( '(a   for a in x   if q  if w   for b in f)',  Schema.GeneratorExpr( resultExpr=Schema.Load( name='a' ),  comprehensionItems=[ Schema.ComprehensionFor( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='x' ) ),   Schema.ComprehensionIf( condition=Schema.Load( name='q' ) ),   Schema.ComprehensionIf( condition=Schema.Load( name='w' ) ),  Schema.ComprehensionFor( target=Schema.SingleTarget( name='b' ), source=Schema.Load( name='f' ) ) ] ) )

		
	
	def testDict(self):
		self._exprTest( '{a:b, c:d}',  Schema.DictLiteral( values=[ Schema.DictKeyValuePair( key=Schema.Load( name='a' ), value=Schema.Load( name='b' ) ), Schema.DictKeyValuePair( key=Schema.Load( name='c' ), value=Schema.Load( name='d' ) ) ] ) )
		
		
	def testYieldExpr(self):
		self._exprTest( '(yield a)', Schema.YieldExpr( value=Schema.Load( name='a' ) ) )
		
		
	def testAttribute(self):
		self._exprTest( 'a.b', Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ) )
		
		

	def testSubscript(self):
		self._exprTest( 'a[b]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='b' ) ) )
		self._exprTest( 'a[b,c]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.TupleLiteral( values=[ Schema.Load( name='b' ), Schema.Load( name='c' ) ] ) ) )
		
	def testSlice(self):	
		self._exprTest( 'a[b:c]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptSlice( lower=Schema.Load( name='b' ), upper=Schema.Load( name='c' ) ) ) )
		self._exprTest( 'a[b:]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptSlice( lower=Schema.Load( name='b' ), upper=None ) ) )
		self._exprTest( 'a[:c]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptSlice( lower=None, upper=Schema.Load( name='c' ) ) ) )
		self._exprTest( 'a[b:c:d]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=Schema.Load( name='b' ), upper=Schema.Load( name='c' ), stride=Schema.Load( name='d' ) ) ) )
		self._exprTest( 'a[b:c:]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=Schema.Load( name='b' ), upper=Schema.Load( name='c' ), stride=Schema.Load( name='None' ) ) ) )
		self._exprTest( 'a[b::d]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=Schema.Load( name='b' ), upper=None, stride=Schema.Load( name='d' ) ) ) )
		self._exprTest( 'a[:c:d]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptLongSlice( lower=None, upper=Schema.Load( name='c' ), stride=Schema.Load( name='d' ) ) ) )
		self._exprTest( 'a[b:c,d:e]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.TupleLiteral( values=[ Schema.SubscriptSlice( lower=Schema.Load( name='b' ), upper=Schema.Load( name='c' ) ), Schema.SubscriptSlice( lower=Schema.Load( name='d' ), upper=Schema.Load( name='e' ) ) ] ) ) )
		self._exprTest( 'a[b:c,d:e:f]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.TupleLiteral( values=[ Schema.SubscriptSlice( lower=Schema.Load( name='b' ), upper=Schema.Load( name='c' ) ), Schema.SubscriptLongSlice( lower=Schema.Load( name='d' ), upper=Schema.Load( name='e' ), stride=Schema.Load( name='f' ) ) ] ) ) )
		
	def testEllipsis(self):
		self._exprTest( 'a[...]',  Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.SubscriptEllipsis() ) )
		
	

	def testCall(self):
		self._exprTest( 'a()',   Schema.Call( target=Schema.Load( name='a' ), args=[] ) )
		self._exprTest( 'a(f)',   Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ) ] ) )
		self._exprTest( 'a(f,g=x)',   Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.CallKWArg( name='g', value=Schema.Load( name='x' ) ) ] ) )
		self._exprTest( 'a(f,g=x,*h)',   Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.CallKWArg( name='g', value=Schema.Load( name='x' ) ), Schema.CallArgList( value=Schema.Load( name='h' ) ) ] ) )
		self._exprTest( 'a(f,g=x,*h,**i)',   Schema.Call( target=Schema.Load( name='a' ), args=[ Schema.Load( name='f' ), Schema.CallKWArg( name='g', value=Schema.Load( name='x' ) ), Schema.CallArgList( value=Schema.Load( name='h' ) ), Schema.CallKWArgList( value=Schema.Load( name='i' ) ) ] ) )
	
	
	def testBinOp(self):
		self._exprTest( 'a ** b', Schema.Pow( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a * b', Schema.Mul( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a / b', Schema.Div( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a % b', Schema.Mod( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a + b', Schema.Add( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a - b', Schema.Sub( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a << b', Schema.LShift( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a >> b', Schema.RShift( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a & b', Schema.BitAnd( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a & b & c', Schema.BitAnd( x=Schema.BitAnd( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Load( name='c' ) ) )
		self._exprTest( 'a ^ b', Schema.BitXor( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a ^ b ^ c', Schema.BitXor( x=Schema.BitXor( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Load( name='c' ) ) )
		self._exprTest( 'a | b', Schema.BitOr( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a | b | c', Schema.BitOr( x=Schema.BitOr( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ), y=Schema.Load( name='c' ) ) )
		
		
	def testUnaryOp(self):
		self._exprTest( '~a', Schema.Invert( x=Schema.Load( name='a' ) ) )
		self._exprTest( '+a', Schema.Pos( x=Schema.Load( name='a' ) ) )
		self._exprTest( '-a', Schema.Negate( x=Schema.Load( name='a' ) ) )
		self._exprTest( 'not a', Schema.NotTest( x=Schema.Load( name='a' ) ) )
		
		
	def testCompare(self):
		self._exprTest( 'a < b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpLt( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a < b < c', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpLt( y=Schema.Load( name='b' ) ), Schema.CmpOpLt( y=Schema.Load( name='c' ) ) ] ) )
		self._exprTest( 'a <= b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpLte( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a == b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpEq( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a != b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpNeq( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a > b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpGt( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a >= b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpGte( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a is b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpIs( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a is not b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpIsNot( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a in b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpIn( y=Schema.Load( name='b' ) ) ] ) )
		self._exprTest( 'a not in b', Schema.Cmp( x=Schema.Load( name='a' ), ops=[ Schema.CmpOpNotIn( y=Schema.Load( name='b' ) ) ] ) )
		
		
	def testBoolOp(self):
		self._exprTest( 'a and b', Schema.AndTest( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		self._exprTest( 'a or b', Schema.OrTest( x=Schema.Load( name='a' ), y=Schema.Load( name='b' ) ) )
		
		
	def testLambda(self):
		self._exprTest( 'lambda: x', Schema.LambdaExpr( params=[], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g,m=a: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ), Schema.DefaultValueParam( name='m', defaultValue=Schema.Load( name='a' ) ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g,m=a,n=b: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ), Schema.DefaultValueParam( name='m', defaultValue=Schema.Load( name='a' ) ), Schema.DefaultValueParam( name='n', defaultValue=Schema.Load( name='b' ) ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,g,m=a,n=b,*p: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.SimpleParam( name='g' ), Schema.DefaultValueParam( name='m', defaultValue=Schema.Load( name='a' ) ), Schema.DefaultValueParam( name='n', defaultValue=Schema.Load( name='b' ) ), Schema.ParamList( name='p' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,m=a,*p,**w: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.DefaultValueParam( name='m', defaultValue=Schema.Load( name='a' ) ), Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,m=a,*p: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.DefaultValueParam( name='m', defaultValue=Schema.Load( name='a' ) ), Schema.ParamList( name='p' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,m=a,**w: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.DefaultValueParam( name='m', defaultValue=Schema.Load( name='a' ) ), Schema.KWParamList( name='w' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda f,*p,**w: x', Schema.LambdaExpr( params=[ Schema.SimpleParam( name='f' ), Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda m=a,*p,**w: x', Schema.LambdaExpr( params=[ Schema.DefaultValueParam( name='m', defaultValue=Schema.Load( name='a' ) ), Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda *p,**w: x', Schema.LambdaExpr( params=[ Schema.ParamList( name='p' ), Schema.KWParamList( name='w' ) ], expr=Schema.Load( name='x' ) ) )
		self._exprTest( 'lambda **w: x', Schema.LambdaExpr( params=[ Schema.KWParamList( name='w' ) ], expr=Schema.Load( name='x' ) ) )
		
		
	def testConditionalExpr(self):
		self._exprTest( 'a   if b   else   c', Schema.ConditionalExpr( condition=Schema.Load( name='b' ), expr=Schema.Load( name='a' ), elseExpr=Schema.Load( name='c' ) ) )
		
		

	
	#
	# Simple statements
	#
	
	def testAssert(self):
		self._stmtTest( 'assert a', Schema.AssertStmt( condition=Schema.Load( name='a' ), fail=None ) )
		self._stmtTest( 'assert a,b', Schema.AssertStmt( condition=Schema.Load( name='a' ), fail=Schema.Load( name='b' ) ) )
		

	def testAssign(self):
		self._stmtTest( 'a=x', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='a' ) ], value=Schema.Load( name='x' ) ) )
		self._stmtTest( 'a,b=c,d=x', Schema.AssignStmt( targets=[ Schema.TupleTarget( targets=[ Schema.SingleTarget( name='a' ), Schema.SingleTarget( name='b' ) ] ),  Schema.TupleTarget( targets=[ Schema.SingleTarget( name='c' ), Schema.SingleTarget( name='d' ) ] ) ], value=Schema.Load( name='x' ) ) )
		self._stmtTest( 'a=yield x', Schema.AssignStmt( targets=[ Schema.SingleTarget( name='a' ) ], value=Schema.YieldExpr( value=Schema.Load( name='x' ) ) ) )
	
		
	def testAugAssignStmt(self):
		self._stmtTest( 'a += b', Schema.AugAssignStmt( op='+=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a -= b', Schema.AugAssignStmt( op='-=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a *= b', Schema.AugAssignStmt( op='*=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a /= b', Schema.AugAssignStmt( op='/=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a %= b', Schema.AugAssignStmt( op='%=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a **= b', Schema.AugAssignStmt( op='**=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a >>= b', Schema.AugAssignStmt( op='>>=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a <<= b', Schema.AugAssignStmt( op='<<=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a &= b', Schema.AugAssignStmt( op='&=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a ^= b', Schema.AugAssignStmt( op='^=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a |= b', Schema.AugAssignStmt( op='|=', target=Schema.SingleTarget( name='a' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a.b += b', Schema.AugAssignStmt( op='+=', target=Schema.AttributeRef( target=Schema.Load( name='a' ), name='b' ), value=Schema.Load( name='b' ) ) )
		self._stmtTest( 'a[x] += b', Schema.AugAssignStmt( op='+=', target=Schema.Subscript( target=Schema.Load( name='a' ), index=Schema.Load( name='x' ) ), value=Schema.Load( name='b' ) ) )

	def testPassStmt(self):
		self._stmtTest( 'pass', Schema.PassStmt() )
		
		
	def testDelStmt(self):
		self._stmtTest( 'del x', Schema.DelStmt( target=Schema.SingleTarget( name='x' ) ) )
		
		
	def testReturnStmt(self):
		self._stmtTest( 'return x', Schema.ReturnStmt( value=Schema.Load( name='x' ) ) )
		
	
	def testYieldStmt(self):
		self._stmtTest( 'yield x', Schema.ExprStmt( expr=Schema.YieldExpr( value=Schema.Load( name='x' ) ) ) )
		
		
	def testRaiseStmt(self):
		self._stmtTest( 'raise', Schema.RaiseStmt( excType=None, excValue=None, traceback=None ) )
		self._stmtTest( 'raise x', Schema.RaiseStmt( excType=Schema.Load( name='x' ), excValue=None, traceback=None ) )
		self._stmtTest( 'raise x,y', Schema.RaiseStmt( excType=Schema.Load( name='x' ), excValue=Schema.Load( name='y' ), traceback=None ) )
		self._stmtTest( 'raise x,y,z', Schema.RaiseStmt( excType=Schema.Load( name='x' ), excValue=Schema.Load( name='y' ), traceback=Schema.Load( name='z' ) ) )
		
		
	def testBreakStmt(self):
		self._stmtTest( 'break', Schema.BreakStmt() )
		
		
	def testContinueStmt(self):
		self._stmtTest( 'continue', Schema.ContinueStmt() )
		
		
	def testImportStmt(self):
		self._stmtTest( 'import a', Schema.ImportStmt( modules=[ Schema.ModuleImport( name='a' ) ] ) )
		self._stmtTest( 'import a.b', Schema.ImportStmt( modules=[ Schema.ModuleImport( name='a.b' ) ] ) )
		self._stmtTest( 'import a.b as x', Schema.ImportStmt( modules=[ Schema.ModuleImportAs( name='a.b', asName='x' ) ] ) )
		self._stmtTest( 'import a.b as x, c.d as y', Schema.ImportStmt( modules=[ Schema.ModuleImportAs( name='a.b', asName='x' ), Schema.ModuleImportAs( name='c.d', asName='y' ) ] ) )
		self._stmtTest( 'from x import a', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._stmtTest( 'from x import a as p', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._stmtTest( 'from x import a as p, b as q', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._stmtTest( 'from x import (a)', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._stmtTest( 'from x import (a,)', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImport( name='a' ) ] ) )
		self._stmtTest( 'from x import (a as p)', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._stmtTest( 'from x import (a as p,)', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ) ] ) )
		self._stmtTest( 'from x import ( a as p, b as q )', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._stmtTest( 'from x import ( a as p, b as q, )', Schema.FromImportStmt( module=Schema.RelativeModule( name='x' ), imports=[ Schema.ModuleContentImportAs( name='a', asName='p' ), Schema.ModuleContentImportAs( name='b', asName='q' ) ] ) )
		self._stmtTest( 'from x import *', Schema.FromImportAllStmt( module=Schema.RelativeModule( name='x' ) ) )
		
		
	def testGlobalStmt(self):
		self._stmtTest( 'global x', Schema.GlobalStmt( vars=[ Schema.GlobalVar( name='x' ) ] ) )
		self._stmtTest( 'global x, y', Schema.GlobalStmt( vars=[ Schema.GlobalVar( name='x' ), Schema.GlobalVar( name='y' ) ] ) )
	
		
	def testExecStmt(self):
		self._stmtTest( 'exec a', Schema.ExecStmt( source=Schema.Load( name='a' ), locals=None, globals=None ) )
		self._stmtTest( 'exec a in b', Schema.ExecStmt( source=Schema.Load( name='a' ), globals=Schema.Load( name='b' ), locals=None ) )
		self._stmtTest( 'exec a in b,c', Schema.ExecStmt( source=Schema.Load( name='a' ), globals=Schema.Load( name='b' ), locals=Schema.Load( name='c' ) ) )
		
		
	def testPrintnl(self):
		self._stmtTest( 'print', Schema.PrintStmt( values=[] ) )
		self._stmtTest( 'print x', Schema.PrintStmt( values=[ Schema.Load( name='x' ) ] ) )
		self._stmtTest( 'print x,y', Schema.PrintStmt( values=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ) )
		self._stmtTest( 'print >> a', Schema.PrintStmt( destination=Schema.Load( name='a' ), values=[] ) )
		self._stmtTest( 'print >> a, x', Schema.PrintStmt( destination=Schema.Load( name='a' ), values=[ Schema.Load( name='x' ) ] ) )
		self._stmtTest( 'print >> a, x,y', Schema.PrintStmt( destination=Schema.Load( name='a' ), values=[ Schema.Load( name='x' ), Schema.Load( name='y' ) ] ) )
		

		
	#
	# Compound statements
	#
	
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
	y
elif c:
	z"""
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
elif c:
	z
else:
	w"""
		self._compStmtTest( src1, [ Schema.BlankLine(), Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elifBlocks=[], elseSuite=None ) ] )
		self._compStmtTest( src2, [ Schema.BlankLine(), Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elifBlocks=[ Schema.ElifBlock( condition=Schema.Load( name='b' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='y' ) ) ] ), Schema.ElifBlock( condition=Schema.Load( name='c' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='z' ) ) ] ) ], elseSuite=None ) ] )
		self._compStmtTest( src3, [ Schema.BlankLine(), Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elifBlocks=[], elseSuite=[ Schema.ExprStmt( expr=Schema.Load( name='z' ) ) ] ) ] )
		self._compStmtTest( src4, [ Schema.BlankLine(), Schema.IfStmt( condition=Schema.Load( name='a' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elifBlocks=[ Schema.ElifBlock( condition=Schema.Load( name='b' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='y' ) ) ] ), Schema.ElifBlock( condition=Schema.Load( name='c' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='z' ) ) ] ) ], elseSuite=[ Schema.ExprStmt( expr=Schema.Load( name='w' ) ) ] ) ] )

		
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
		self._compStmtTest( src1, [ Schema.BlankLine(), Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elseSuite=None ) ] )
		self._compStmtTest( src2, [ Schema.BlankLine(), Schema.WhileStmt( condition=Schema.Load( name='a' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elseSuite=[ Schema.ExprStmt( expr=Schema.Load( name='z' ) ) ] ) ] )

		
		
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
		self._compStmtTest( src1, [ Schema.BlankLine(), Schema.ForStmt( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='q' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elseSuite=None ) ] )
		self._compStmtTest( src2, [ Schema.BlankLine(), Schema.ForStmt( target=Schema.SingleTarget( name='a' ), source=Schema.Load( name='q' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], elseSuite=[ Schema.ExprStmt( expr=Schema.Load( name='z' ) ) ] ) ] )

		
		
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
	r"""
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
		src4 = \
"""
try:
	pass
except:
	pass
else:
	pass
finally:
	pass"""
		src5 = \
"""
try:
	pass
finally:
	pass"""
		self._compStmtTest( src1, [ Schema.BlankLine(), Schema.TryStmt( suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], exceptBlocks=[ Schema.ExceptBlock( exception=None, target=None, suite=[ Schema.ExprStmt( expr=Schema.Load( name='p' ) ) ] ),   Schema.ExceptBlock( exception=Schema.Load( name='a' ), target=None, suite=[ Schema.ExprStmt( expr=Schema.Load( name='q' ) ) ] ),    Schema.ExceptBlock( exception=Schema.Load( name='a' ), target=Schema.SingleTarget( name='b' ), suite=[ Schema.ExprStmt( expr=Schema.Load( name='r' ) ) ] ) ], elseSuite=None, finallySuite=None ) ] )
		self._compStmtTest( src2, [ Schema.BlankLine(), Schema.TryStmt( suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ], exceptBlocks=[ Schema.ExceptBlock( exception=None, target=None, suite=[ Schema.ExprStmt( expr=Schema.Load( name='p' ) ) ] ) ], elseSuite=[ Schema.ExprStmt( expr=Schema.Load( name='z' ) ) ], finallySuite=None ) ] )
		self._compStmtTest( src3, [ Schema.BlankLine(), Schema.TryStmt( suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ],  exceptBlocks=[ Schema.ExceptBlock( exception=None, target=None, suite=[ Schema.ExprStmt( expr=Schema.Load( name='p' ) ) ] ) ],   elseSuite=None, finallySuite=[ Schema.ExprStmt( expr=Schema.Load( name='z' ) ) ] ) ] )
		self._compStmtTest( src4, [ Schema.BlankLine(), Schema.TryStmt( suite=[ Schema.PassStmt() ],  exceptBlocks=[ Schema.ExceptBlock( exception=None, target=None, suite=[ Schema.PassStmt() ] ) ],   elseSuite=[ Schema.PassStmt() ], finallySuite=[ Schema.PassStmt() ] ) ] )
		self._compStmtTest( src5, [ Schema.BlankLine(), Schema.TryStmt( suite=[ Schema.PassStmt() ],  exceptBlocks=[],   elseSuite=None, finallySuite=[ Schema.PassStmt() ] ) ] )

		
		
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
		#self._compStmtTest( src1, [ [ 'withStmt', [ 'var', 'a' ], None, [ [ 'var', 'x' ] ] ] ] )
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
		self._compStmtTest( src1, [ Schema.BlankLine(), Schema.DefStmt( decorators=[], name='f', params=[], suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )
		self._compStmtTest( src2, [ Schema.BlankLine(), Schema.DefStmt( decorators=[], name='f', params=[ Schema.SimpleParam( name='a' ), Schema.DefaultValueParam( name='b', defaultValue=Schema.Load( name='q' ) ), Schema.ParamList( name='c' ), Schema.KWParamList( name='d' ) ], suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )
		self._compStmtTest( src3, [ Schema.BlankLine(), Schema.DefStmt( decorators=[ Schema.Decorator( name='p', args=None ) ], name='f', params=[], suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )
		self._compStmtTest( src4, [ Schema.BlankLine(), Schema.DefStmt( decorators=[ Schema.Decorator( name='p', args=[ Schema.Load( name='h' ) ] ) ], name='f', params=[], suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )
		self._compStmtTest( src5, [ Schema.BlankLine(), Schema.DefStmt( decorators=[ Schema.Decorator( name='p', args=[ Schema.Load( name='h' ) ] ), Schema.Decorator( name='q', args=[ Schema.Load( name='j' ) ] ) ], name='f', params=[], suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )

		
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
		self._compStmtTest( src1, [ Schema.BlankLine(), Schema.ClassStmt( name='Q', bases=None, suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )
		self._compStmtTest( src2, [ Schema.BlankLine(), Schema.ClassStmt( name='Q', bases=[ Schema.Load( name='object' ) ], suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )
		self._compStmtTest( src3, [ Schema.BlankLine(), Schema.ClassStmt( name='Q', bases=[ Schema.Load( name='a' ), Schema.Load( name='b' ) ], suite=[ Schema.ExprStmt( expr=Schema.Load( name='x' ) ) ] ) ] )

		
if __name__ == '__main__':
	unittest.main()
		
