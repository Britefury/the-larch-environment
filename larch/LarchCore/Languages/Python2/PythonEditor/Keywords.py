##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************



andKeyword = 'and'
asKeyword = 'as'
assertKeyword = 'assert'
breakKeyword = 'break'
classKeyword = 'class'
continueKeyword = 'continue'
defKeyword = 'def'
delKeyword = 'del'
elifKeyword = 'elif'
elseKeyword = 'else'
exceptKeyword = 'except'
execKeyword = 'exec'
finallyKeyword = 'finally'
forKeyword = 'for'
fromKeyword = 'from'
globalKeyword = 'global'
ifKeyword = 'if'
importKeyword = 'import'
inKeyword = 'in'
isKeyword = 'is'
lambdaKeyword = 'lambda'
notKeyword = 'not'
orKeyword = 'or'
passKeyword = 'pass'
printKeyword = 'print'
raiseKeyword = 'raise'
returnKeyword = 'return'
tryKeyword = 'try'
whileKeyword = 'while'
withKeyword = 'with'
yieldKeyword = 'yield'

keywords = [ 'and', 'as', 'assert', 'break', 'class', 'continue', 'def', 'del', 'elif', 'else', 'except', 'exec', 'finally', 'for', 'from', 'global', 'if', 'import', 'in', 'is', 'lambda', 'not', 'or', 'pass', 'print', 'raise', 'return', 'try', 'while', 'with', 'yield' ]
keywordsSet = set( keywords )
nonIdentifierKeywordsSet = set( [ k   for k in keywords   if k != 'print' ] )


augAssignOps = [ '+=', '-=', '*=', '/=', '%=', '**=', '>>=', '<<=', '&=', '^=', '|=' ]

