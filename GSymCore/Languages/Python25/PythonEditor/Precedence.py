##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMObjectInterface, DMListInterface, DMClassAttribute

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod, dmObjectNodeMethodDispatch
from Britefury.Util.NodeUtil import isStringNode

from GSymCore.Languages.Python25 import Schema


PRECEDENCE_NONE = -1
PRECEDENCE_UNPARSED = PRECEDENCE_NONE
PRECEDENCE_COMMENT = PRECEDENCE_NONE
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
PRECEDENCE_EMBEDDEDOBJECTEXPR = 1000

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
PRECEDENCE_CONTAINER_EXTERNALEXPR = PRECEDENCE_NONE
PRECEDENCE_CONTAINER_QUOTE = PRECEDENCE_NONE

PRECEDENCE_CONTAINER_UNPARSED = PRECEDENCE_NONE





nodePrecedence = DMClassAttribute( 'nodePrecedence', Schema.Node )
nodePrecedence[Schema.Node] = PRECEDENCE_NONE

nodePrecedence[Schema.CommentStmt] = PRECEDENCE_COMMENT
nodePrecedence[Schema.BlankLine] = PRECEDENCE_COMMENT
nodePrecedence[Schema.UNPARSED] = PRECEDENCE_UNPARSED
nodePrecedence[Schema.Target] = PRECEDENCE_TARGET

nodePrecedence[Schema.Stmt] = PRECEDENCE_STMT
nodePrecedence[Schema.Expr] = PRECEDENCE_EXPR


nodePrecedence[Schema.Literal] = PRECEDENCE_LITERALVALUE
nodePrecedence[Schema.Load] = PRECEDENCE_LOAD
nodePrecedence[Schema.SingleTarget] = PRECEDENCE_SINGLETARGET
nodePrecedence[Schema.TupleLiteral] = PRECEDENCE_TUPLE
nodePrecedence[Schema.TupleTarget] = PRECEDENCE_TUPLE
nodePrecedence[Schema.ListLiteral] = PRECEDENCE_LISTDISPLAY
nodePrecedence[Schema.ListTarget] = PRECEDENCE_LISTDISPLAY
nodePrecedence[Schema.ListComp] = PRECEDENCE_LISTDISPLAY
nodePrecedence[Schema.GeneratorExpr] = PRECEDENCE_GENERATOREXPRESSION
nodePrecedence[Schema.DictLiteral] = PRECEDENCE_DICTDISPLAY
nodePrecedence[Schema.YieldExpr] = PRECEDENCE_YIELDEXPR

nodePrecedence[Schema.Pow] = PRECEDENCE_POW
nodePrecedence[Schema.Invert] = PRECEDENCE_INVERT_NEGATE_POS
nodePrecedence[Schema.Negate] = PRECEDENCE_INVERT_NEGATE_POS
nodePrecedence[Schema.Pos] = PRECEDENCE_INVERT_NEGATE_POS
nodePrecedence[Schema.Mul] = PRECEDENCE_MULDIVMOD
nodePrecedence[Schema.Div] = PRECEDENCE_MULDIVMOD
nodePrecedence[Schema.Mod] = PRECEDENCE_MULDIVMOD
nodePrecedence[Schema.Add] = PRECEDENCE_ADDSUB
nodePrecedence[Schema.Sub] = PRECEDENCE_ADDSUB
nodePrecedence[Schema.LShift] = PRECEDENCE_SHIFT
nodePrecedence[Schema.RShift] = PRECEDENCE_SHIFT
nodePrecedence[Schema.BitAnd] = PRECEDENCE_BITAND
nodePrecedence[Schema.BitXor] = PRECEDENCE_BITXOR
nodePrecedence[Schema.BitOr] = PRECEDENCE_BITOR
nodePrecedence[Schema.Cmp] = PRECEDENCE_CMP
nodePrecedence[Schema.NotTest] = PRECEDENCE_NOT
nodePrecedence[Schema.AndTest] = PRECEDENCE_AND
nodePrecedence[Schema.OrTest] = PRECEDENCE_OR
nodePrecedence[Schema.LambdaExpr] = PRECEDENCE_LAMBDAEXPR
nodePrecedence[Schema.ConditionalExpr] = PRECEDENCE_CONDITIONAL
nodePrecedence[Schema.ExternalExpr] = PRECEDENCE_EXTERNALEXPR

nodePrecedence[Schema.EmbeddedObjectExpr] = PRECEDENCE_EMBEDDEDOBJECTEXPR
nodePrecedence[Schema.EmbeddedObjectStmt] = PRECEDENCE_STMT


nodePrecedence.commit()

	
	
rightAssociative = DMClassAttribute( 'rightAssociative', Schema.BinOp )
rightAssociative[Schema.BinOp] = False
rightAssociative[Schema.Pow] = True
rightAssociative.commit()



parensRequired = DMClassAttribute( 'parensRequired', Schema.Node )
parensRequired[Schema.Node] = False
parensRequired[Schema.Expr] = True
parensRequired[Schema.Target] = True
parensRequired.commit()
