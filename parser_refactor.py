import java.util.List

from Britefury.InitBritefuryJ import initBritefuryJ
initBritefuryJ()

from BritefuryJ.PatternMatch import *
from BritefuryJ.DocModel import DMList

from Britefury.Transformation.Transformation import Transformation
from Britefury.Transformation.PatternMatchTransformation import PatternMatchTransformation

from GSymCore.Languages.Python25.IdentityTransformation import Python25IdentityTransformation
from GSymCore.Languages.Python25.Python25Importer import importPy25File, importPy25Source
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from datetime import datetime




xs = importPy25File( 'GSymCore/Languages/Python25/Parser.py' )
xs = DMList( xs )




def _call(target, params):
	return MatchExpression.toMatchExpression( [ 'call', target, params ] )


def _methodInvoke(target, methodName, params):
	return _call( MatchExpression.toMatchExpression( [ 'attributeRef', target, methodName ] ), params )
						  

def _parserMethodInvoke(subexp):
	return _methodInvoke( subexp, Choice( [ 'action', 'condition', 'bindTo', 'clearBindings', 'suppress', 'optional', 'zeroOrMore', 'oneOrMore' ] ), Anything() )


def _parserBinOp(subexp):
	return MatchExpression.toMatchExpression( [ Choice( [ 'add', 'sub', 'bitOr', 'bitAnd', 'bitXor' ] ), subexp, subexp ] )


def _strLit():
	return MatchExpression.toMatchExpression( [ 'stringLiteral', Anything(), Anything(), Anything() ] )


def _var(name):
	return MatchExpression.toMatchExpression( [ 'var', name ] )


def _terminalInstanceExpression():
	return _call( Choice( [ 'Keyword', 'Literal', 'RegEx', 'Word' ] ), Anything() )  |		\
	       _strLit().action( lambda input, x, bindings: [ 'call', [ 'var', 'Literal' ], x ] )  |		\
	       _var( Anything() )


def _branchInstanceExpression(subexp):
	_unaryInstance = Choice( [ _call( _var( 'Action' ), [ subexp, Anything() ] ),
				   _call( _var( 'Bind' ), [ Anything(), subexp ] ),
				   _call( _var( 'Condition' ), [ subexp, Anything() ] ),
				   _call( _var( Choice( [ 'OneOrMore', 'Optional', 'Peek', 'PeekNot', 'Suppress', 'ZeroOrMore' ] ) ), [ subexp ] ),
				   _call( _var( 'Repetition' ), [ subexp, Anything(), Anything() ] ) ] )
	_branchInstance = _call( _var( Choice( [ 'BestChoice', 'Choice', 'Combine', 'Sequence' ] ) ), [ [ 'listLiteral', ZeroOrMore( subexp ) ] ] )
	return _unaryInstance | _branchInstance


def _productionInstanceExpression(subexp):
	return _call( _var( 'Production' ), subexp.bindTo( 'subexp' ) )



parserExpression = Forward()
parserExpression.setExpression( ( _parserMethodInvoke( parserExpression )  |  _parserBinOp( parserExpression )  |  _branchInstanceExpression( parserExpression )  |  _terminalInstanceExpression() ) )
production = Forward()
prod = _productionInstanceExpression( parserExpression ).action( lambda input, x, bindings: bindings['subexp'] )
production.setExpression( ( _parserMethodInvoke( production )  |  prod ) )


def _makeRuleMethod(input, x, bindings):
	return [ 'defStmt', bindings['ruleName'], [ [ 'simpleParam', 'self' ] ], [ [ 'returnStmt', bindings['ruleExpression'] ] ] ]
parserRule = Production( [ 'assignmentStmt', [ [ 'singleTarget', AnyString().bindTo( 'ruleName' )] ], production.bindTo( 'ruleExpression' ) ] ).action( _makeRuleMethod )
statement = parserRule  |  Anything()
module = Production( [ 'python25Module', ZeroOrMore( statement ) ] )




xform1 = PatternMatchTransformation( module )






xf = Transformation( Python25IdentityTransformation(), [ xform1 ] )


cg = Python25CodeGenerator()

#t1 = datetime.now()
xs2 = xf( xs )
#t2 = datetime.now()

print cg( xs2 )
#def _count(x):
	#if isinstance( x, java.util.List ):
		#c = 0
		#for xx in x:
			#c += _count( xx )
		#return c
	#else:
		#return 1

#print '%d nodes; transformation time: %s'  %  ( _count( xs ), t2 - t1 )
#print type( xs ), isinstance( xs, java.util.List )

