##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass, DMNode




schema = DMSchema( 'SWYN', 'swyn', 'LarchTools.PythonTools.SWYN', 0 )


Node = schema.newClass( 'Node', [] )


SWYNRegEx = schema.newClass( 'SWYNRegEx', Node, [ 'expr' ] )


UNPARSED = schema.newClass( 'UNPARSED', Node, [ 'value' ] )



EscapedChar = schema.newClass( 'EscapedChar', Node, [ 'char' ] )
LiteralChar = schema.newClass( 'LiteralChar', Node, [ 'char' ] )


AnyChar = schema.newClass( 'AnyChar', Node, [] )
StartOfLine = schema.newClass( 'StartOfLine', Node, [] )
EndOfLine = schema.newClass( 'EndOfLine', Node, [] )


CharClass = schema.newClass( 'CharClass', Node, [ 'cls' ] )


CharSet = schema.newClass( 'CharSet', Node, [ 'invert', 'items' ] )
CharSetChar = schema.newClass( 'CharSetChar', Node, [ 'char' ] )
CharSetRange = schema.newClass( 'CharSetRange', Node, [ 'min', 'max' ] )


Group = schema.newClass( 'Group', Node, [ 'subexp', 'capturing' ] )
SetFlags = schema.newClass( 'SetFlags', Node, [ 'flags' ] )
DefineNamedGroup = schema.newClass( 'DefineNamedGroup', Node, [ 'subexp', 'name' ] )
MatchNamedGroup = schema.newClass( 'MatchNamedGroup', Node, [ 'name' ] )
Comment = schema.newClass( 'Comment', Node, [ 'text' ] )

Look = schema.newClass( 'Look', Node, [ 'subexp' ] )
Lookahead = schema.newClass( 'Lookahead', Look, [] )
NegativeLookahead = schema.newClass( 'NegativeLookahead', Look, [] )
Lookbehind = schema.newClass( 'Lookbehind', Look, [] )
NegativeLookbehind = schema.newClass( 'NegativeLookbehind', Look, [] )


Repetition = schema.newClass( 'Repetition', Node, [ 'subexp' ] )
RepetitionGreedyOption = schema.newClass( 'RepetitionGreedyOption', Repetition, [ 'greedy' ] )

Repeat = schema.newClass( 'Repeat', Repetition, [ 'repetitions' ] )
ZeroOrMore = schema.newClass( 'ZeroOrMore', RepetitionGreedyOption, [] )
OneOrMore = schema.newClass( 'OneOrMore', RepetitionGreedyOption, [] )
Optional = schema.newClass( 'Optional', RepetitionGreedyOption, [] )
RepeatRange = schema.newClass( 'RepeatRange', RepetitionGreedyOption, [ 'min', 'max' ] )


Sequence = schema.newClass( 'Sequence', Node, [ 'subexps' ] )
Choice = schema.newClass( 'Choice', Node, [ 'subexps' ] )

