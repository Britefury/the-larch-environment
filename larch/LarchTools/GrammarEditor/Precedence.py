##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMClassAttribute

from LarchTools.GrammarEditor import Schema


PRECEDENCE_NONE = -1
PRECEDENCE_UNPARSED = PRECEDENCE_NONE
PRECEDENCE_COMMENT = PRECEDENCE_NONE
PRECEDENCE_STMT = 10000
PRECEDENCE_EXPR = 0


PRECEDENCE_TERMINAL = 0
PRECEDENCE_INVOKE_RULE = 0

PRECEDENCE_REPEAT = 10
PRECEDENCE_ACTION = 15
PRECEDENCE_SEQUENCE = 20
PRECEDENCE_CHOICE = 30

# 
PRECEDENCE_CONTAINER_UNPARSED = PRECEDENCE_NONE





nodePrecedence = DMClassAttribute( 'nodePrecedence', Schema.Node )
nodePrecedence[Schema.Node] = PRECEDENCE_NONE

nodePrecedence[Schema.CommentStmt] = PRECEDENCE_COMMENT
nodePrecedence[Schema.BlankLine] = PRECEDENCE_COMMENT
nodePrecedence[Schema.UNPARSED] = PRECEDENCE_UNPARSED

nodePrecedence[Schema.Statement] = PRECEDENCE_STMT
nodePrecedence[Schema.Expression] = PRECEDENCE_EXPR


nodePrecedence[Schema.Terminal] = PRECEDENCE_TERMINAL
nodePrecedence[Schema.InvokeRule] = PRECEDENCE_INVOKE_RULE

nodePrecedence[Schema.AbstractRepeat] = PRECEDENCE_REPEAT
nodePrecedence[Schema.Action] = PRECEDENCE_ACTION
nodePrecedence[Schema.Sequence] = PRECEDENCE_SEQUENCE
nodePrecedence[Schema.Choice] = PRECEDENCE_CHOICE

nodePrecedence.commit()




parensRequired = DMClassAttribute( 'parensRequired', Schema.Node )
parensRequired[Schema.Node] = False
parensRequired[Schema.Expression] = True
parensRequired.commit()
