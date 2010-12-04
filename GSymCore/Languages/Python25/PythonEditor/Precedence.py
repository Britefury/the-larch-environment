##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction, Transformation, TransformationFunction

from BritefuryJ.DocModel import DMObjectInterface, DMListInterface, DMClassAttribute

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch
from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Util.NodeUtil import isStringNode

from GSymCore.Languages.Python25 import Schema


PRECEDENCE_NONE = None
PRECEDENCE_UNPARSED = None
PRECEDENCE_COMMENT = None
PRECEDENCE_STMT = 10000
PRECEDENCE_EXPR = 0
PRECEDENCE_TARGET = 0


PRECEDENCE_LITERALVALUE = 0
PRECEDENCE_LOAD = 0
PRECEDENCE_SINGLETARGET = 0
PRECEDENCE_TUPLE = 0
PRECEDENCE_LISTDISPLAY = 0
PRECEDENCE_GENERATOREXPRESSION = 0
PRECEDENCE_DICTDISPLAY = 0
PRECEDENCE_YIELDEXPR = 200

PRECEDENCE_CONDITIONAL = 100

PRECEDENCE_EXTERNALEXPR = 1000
PRECEDENCE_INLINEOBJECTEXPR = 1000

PRECEDENCE_LAMBDAEXPR = 50

PRECEDENCE_OR = 14
PRECEDENCE_AND = 13
PRECEDENCE_NOT = 12
PRECEDENCE_CMP = 9
PRECEDENCE_BITOR = 8
PRECEDENCE_BITXOR = 7
PRECEDENCE_BITAND = 6
PRECEDENCE_SHIFT = 5
PRECEDENCE_ADDSUB = 4
PRECEDENCE_MULDIVMOD = 3
PRECEDENCE_INVERT_NEGATE_POS = 2
PRECEDENCE_POW = 1
PRECEDENCE_CALL = 0
PRECEDENCE_SUBSCRIPT = 0
PRECEDENCE_ATTR = 0

PRECEDENCE_CONTAINER_ELEMENT = 500
PRECEDENCE_CONTAINER_ATTRIBUTEREFTARGET = 0
PRECEDENCE_CONTAINER_SUBSCRIPTTARGET = 0
PRECEDENCE_CONTAINER_SUBSCRIPTINDEX = 500
PRECEDENCE_CONTAINER_CALLTARGET = 0
PRECEDENCE_CONTAINER_CALLARG = 500

# The lambda expression should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_YIELDEXPR = 199

# The lambda expression should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_LAMBDAEXPR = 50

# The condition and true-expression subtrees of a conditional expression should only expect orTest
PRECEDENCE_CONTAINER_CONDITIONALEXPR = 25

# The comprehension for statements should only expect orTest
PRECEDENCE_CONTAINER_COMPREHENSIONFOR = 25

# The comprehension if statements should only expect orTest or lambda expression
PRECEDENCE_CONTAINER_COMPREHENSIONIF = 50


# 
PRECEDENCE_CONTAINER_EXTERNALEXPR = None
PRECEDENCE_CONTAINER_QUOTE = None

PRECEDENCE_CONTAINER_UNPARSED = None


PRECEDENCE_IMPORTCONTENT = 0




precedence = DMClassAttribute( 'precedence', Schema.Node )
precedence[Schema.CommentStmt] = PRECEDENCE_COMMENT
precedence[Schema.BlankLine] = PRECEDENCE_COMMENT
precedence[Schema.UNPARSED] = PRECEDENCE_UNPARSED
precedence[Schema.Target] = PRECEDENCE_TARGET

precedence[Schema.Stmt] = PRECEDENCE_STMT
precedence[Schema.Expr] = PRECEDENCE_EXPR


precedence[Schema.Literal] = PRECEDENCE_LITERALVALUE
precedence[Schema.Load] = PRECEDENCE_LOAD
precedence[Schema.SingleTarget] = PRECEDENCE_SINGLETARGET
precedence[Schema.TupleLiteral] = PRECEDENCE_TUPLE
precedence[Schema.TupleTarget] = PRECEDENCE_TUPLE
precedence[Schema.ListLiteral] = PRECEDENCE_LISTDISPLAY
precedence[Schema.ListTarget] = PRECEDENCE_LISTDISPLAY
precedence[Schema.ListComp] = PRECEDENCE_LISTDISPLAY
precedence[Schema.GeneratorExpr] = PRECEDENCE_GENERATOREXPRESSION
precedence[Schema.DictLiteral] = PRECEDENCE_DICTDISPLAY
precedence[Schema.YieldExpr] = PRECEDENCE_YIELDEXPR

precedence[Schema.Pow] = PRECEDENCE_POW
precedence[Schema.Invert] = PRECEDENCE_INVERT_NEGATE_POS
precedence[Schema.Negate] = PRECEDENCE_INVERT_NEGATE_POS
precedence[Schema.Pos] = PRECEDENCE_INVERT_NEGATE_POS
precedence[Schema.Mul] = PRECEDENCE_MULDIVMOD
precedence[Schema.Div] = PRECEDENCE_MULDIVMOD
precedence[Schema.Mod] = PRECEDENCE_MULDIVMOD
precedence[Schema.Add] = PRECEDENCE_ADDSUB
precedence[Schema.Sub] = PRECEDENCE_ADDSUB
precedence[Schema.LShift] = PRECEDENCE_SHIFT
precedence[Schema.RShift] = PRECEDENCE_SHIFT
precedence[Schema.BitAnd] = PRECEDENCE_BITAND
precedence[Schema.BitXor] = PRECEDENCE_BITXOR
precedence[Schema.BitOr] = PRECEDENCE_BITOR
precedence[Schema.Cmp] = PRECEDENCE_CMP
precedence[Schema.NotTest] = PRECEDENCE_NOT
precedence[Schema.AndTest] = PRECEDENCE_AND
precedence[Schema.OrTest] = PRECEDENCE_OR
precedence[Schema.LambdaExpr] = PRECEDENCE_LAMBDAEXPR
precedence[Schema.ConditionalExpr] = PRECEDENCE_CONDITIONAL
precedence[Schema.ExternalExpr] = PRECEDENCE_EXTERNALEXPR

precedence[Schema.InlineObjectExpr] = PRECEDENCE_INLINEOBJECTEXPR
precedence[Schema.InlineObjectStmt] = PRECEDENCE_STMT


precedence.commit()

	
	
rightAssociative = DMClassAttribute( 'rightAssociative', Schema.BinOp )
rightAssociative[Schema.BinOp] = False
rightAssociative[Schema.Pow] = True
rightAssociative.commit()



def getNumParens(node):
	try:
		p = node['parens']
	except KeyError:
		print 'Attempted to get number of parens for %s'  %  node
		raise
	
	numParens = 0
	if p is not None   and   isStringNode( p ):
		p = str( p )
		try:
			numParens = int( p )
		except ValueError:
			pass
	return numParens
