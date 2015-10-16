##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass, DMNode




schema = DMSchema( 'Grammar', 'grm', 'LarchTools.GrammarEditor.GrammarEditor', 1 )


Node = schema.newClass( 'Node', [] )

UNPARSED = schema.newClass( 'UNPARSED', Node, [ 'value' ] )

# Expressions
Expression = schema.newClass('Expression', Node, ['parens'])

# Terminals
Terminal = schema.newClass('Terminal', Expression, [])
Literal = schema.newClass('Literal', Terminal, ['value', 'quotation'])
Keyword = schema.newClass('Keyword', Terminal, ['value', 'quotation'])
Word = schema.newClass('Word', Terminal, ['value', 'quotation'])
RegEx = schema.newClass('RegEx', Terminal, ['regex'])

# Non-terminals
InvokeRule = schema.newClass('InvokeRule', Expression, ['name'])

# Repetition
AbstractRepeat = schema.newClass('AbstractRepeat', Expression, [])
Optional = schema.newClass('Optional', AbstractRepeat, ['subexp'])
ZeroOrMore = schema.newClass('ZeroOrMore', AbstractRepeat, ['subexp'])
OneOrMore = schema.newClass('OneOrMore', AbstractRepeat, ['subexp'])
Repeat = schema.newClass('Repeat', AbstractRepeat, ['subexp', 'n'])
RepeatRange = schema.newClass('RepeatRange', AbstractRepeat, ['subexp', 'a', 'b'])

# Functions
Action = schema.newClass('Action', Expression, ['subexp', 'action'])
ActionPy = schema.newClass('ActionPy', Expression, ['py'])

# Combinators
Sequence = schema.newClass('Sequence', Expression, ['subexps'])
Choice = schema.newClass('Choice', Expression, ['subexps'])



# Statements
Statement = schema.newClass('Statement', Node, [])

BlankLine = schema.newClass( 'BlankLine', Statement, [] )
CommentStmt = schema.newClass( 'CommentStmt', Statement, [ 'comment' ] )
UnparsedStmt = schema.newClass( 'UnparsedStmt', Statement, [ 'value' ] )
RuleDefinitionStmt = schema.newClass('RuleDefinitionStmt', Statement, ['name', 'body'])

HelperBlockPy = schema.newClass('HelperBlockPy', Statement, ['py'])



GrammarDefinition = schema.newClass('GrammarDefinition', Node, ['rules'])