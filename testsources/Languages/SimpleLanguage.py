##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Colour3f


from Britefury.Parser import Parser

from Britefury.gSym.gSymCompiler import GSymCompiler

from Britefury.gSym.View.Tokeniser import TokenDefinition, Tokeniser

from Britefury.gSym.View.gSymView import activeBorder, border, indent, hline, label, markupLabel, entry, markupEntry, customEntry, hbox, ahbox, vbox, wrappedHBox, wrappedHBoxSep, \
     script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, focus, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import WrappedListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, tokenListEventMethod, Interactor

from Britefury.gSym.View.EditOperations import replace

from Britefury.gSym.View.gSymStyleSheet import GSymStyleSheet

from Britefury.gSym.gSymLanguage import GSymLanguage


class SimpleLanguageCompiler (GSymCompiler):
	def add(self, node, x, y):
		return '( '  +  self( x )  +  ' + '  +  self( y )  +  ' )'
	
	def sub(self, node, x, y):
		return '( '  +  self( x )  +  ' - '  +  self( y )  +  ' )'
	
	def mul(self, node, x, y):
		return '( '  +  self( x )  +  ' * '  +  self( y )  +  ' )'
	
	def div(self, node, x, y):
		return '( '  +  self( x )  +  ' / '  +  self( y )  +  ' )'
	
	def mod(self, node, x, y):
		return '( '  +  self( x )  +  ' % '  +  self( y )  +  ' )'
	
	def pow(self, node, x, y):
		return '( '  +  self( x )  +  ' ** '  +  self( y )  +  ' )'
	
	def loadLocal(self, node, name):
		return name
	
	def nilExpr(self, node):
		return '<NIL>'
	
	

divBoxStyle = GSymStyleSheet( alignment='expand' )
nilStyle = GSymStyleSheet( colour=Colour3f( 0.75, 0.0, 0.0 ), font='Sans 11 italic' )


tokeniser = Tokeniser( [
	TokenDefinition( 'identifier', Parser.identifier ),
	TokenDefinition( 'op_pow', Parser.Literal( '**' ) ),
	TokenDefinition( 'op_add', Parser.Literal( '+' ) ),
	TokenDefinition( 'op_sub', Parser.Literal( '-' ) ),
	TokenDefinition( 'op_mul', Parser.Literal( '*' ) ),
	TokenDefinition( 'op_div', Parser.Literal( '/' ) ),
	TokenDefinition( 'op_mod', Parser.Literal( '%' ) )
	] )


class ExpressionInteractor (Interactor):
	@keyEventMethod( '+' )
	def keyAdd(self, event, node):
		return replace( node, [ 'add', node, [ 'nilExpr' ] ] )
	
	@tokenListEventMethod( 'op_add' )
	def tokAdd(self, token, node):
		return replace( node, [ 'add', node, [ 'nilExpr' ] ] )
	
	
	@keyEventMethod( '-' )
	def keySub(self, event, node):
		return replace( node, [ 'sub', node, [ 'nilExpr' ] ] )
	
	@tokenListEventMethod( 'op_sub' )
	def tokSub(self, token, node):
		return replace( node, [ 'sub', node, [ 'nilExpr' ] ] )
	
	
	@keyEventMethod( '*' )
	def keyMul(self, event, node):
		return replace( node, [ 'mul', node, [ 'nilExpr' ] ] )
	
	@tokenListEventMethod( 'op_mul' )
	def tokMul(self, token, node):
		return replace( node, [ 'mul', node, [ 'nilExpr' ] ] )
	
	
	@keyEventMethod( '/' )
	def keyDiv(self, event, node):
		return replace( node, [ 'div', node, [ 'nilExpr' ] ] )
	
	@tokenListEventMethod( 'op_div' )
	def tokDiv(self, token, node):
		return replace( node, [ 'div', node, [ 'nilExpr' ] ] )
	
	
	@keyEventMethod( '%' )
	def keyMod(self, event, node):
		return replace( node, [ 'mod', node, [ 'nilExpr' ] ] )
	
	@tokenListEventMethod( 'op_mod' )
	def tokMod(self, token, node):
		return replace( node, [ 'mod', node, [ 'nilExpr' ] ] )
	
	
	@accelEventMethod( '<ctrl>8' )
	def keyPow(self, event, node):
		return replace( node, [ 'pow', node, [ 'nilExpr' ] ] )
	
	@tokenListEventMethod( 'op_pow' )
	def tokPow(self, token, node):
		return replace( node, [ 'pow', node, [ 'nilExpr' ] ] )
	
	
	
	eventMethods = [ keyAdd, tokAdd,  keySub, tokSub,  keyMul, tokMul,  keyDiv, tokDiv,  keyMod, tokMod,  keyPow, tokPow ]
	


	

class LoadLocalInteractor (Interactor):
	@tokenListEventMethod( 'identifier' )
	def tokIdentifier(self, name, node):
		return replace( node, [ 'loadLocal', name ] )
	
	eventMethods = [ tokIdentifier ]



class OperatorInteractor (Interactor):
	@tokenListEventMethod( 'op_add' )
	def tokAdd(self, token, node):
		replace( node[0], 'add' )
		return node[2]
	
	@tokenListEventMethod( 'op_sub' )
	def tokSub(self, token, node):
		replace( node[0], 'sub' )
		return node[2]
	
	@tokenListEventMethod( 'op_mul' )
	def tokMul(self, token, node):
		replace( node[0], 'mul' )
		return node[2]
	
	@tokenListEventMethod( 'op_div' )
	def tokDiv(self, token, node):
		replace( node[0], 'div' )
		return node[2]
	
	@tokenListEventMethod( 'op_mod' )
	def tokMod(self, token, node):
		replace( node[0], 'mod' )
		return node[2]
	
	@tokenListEventMethod( 'op_pow' )
	def tokPow(self, token, node):
		replace( node[0], 'pow' )
		return node[2]
	
	
	
	eventMethods = [ tokAdd, tokSub, tokMul, tokDiv, tokMod, tokPow ]




def operatorEditor(view, node, op):
	return interact( activeBorder( focus( entry( op, op, tokeniser ) ) ), OperatorInteractor( node ) )

def divideEditor(view, node):
	return focus( customEntry( hline(), '/', tokeniser ) )





class SimpleLanguageView (GSymView):
	def add(self, node, x, y):
		return interact( activeBorder( hbox( [ viewEval( x ), operatorEditor( self, node, '+' ), viewEval( y ) ] ) ),  ExpressionInteractor( node ) )

	def sub(self, node, x, y):
		return interact( activeBorder( hbox( [ viewEval( x ), operatorEditor( self, node, '-' ), viewEval( y ) ] ) ),  ExpressionInteractor( node ) )
	
	def mul(self, node, x, y):
		return interact( activeBorder( hbox( [ viewEval( x ), operatorEditor( self, node, '*' ), viewEval( y ) ] ) ),  ExpressionInteractor( node ) )
	
	def div(self, node, x, y):
		return interact( activeBorder( vbox( [ viewEval( x ), divideEditor( self, node ), viewEval( y ) ], divBoxStyle ) ),  ExpressionInteractor( node ) )
	
	def mod(self, node, x, y):
		return interact( activeBorder( hbox( [ viewEval( x ), operatorEditor( self, node, '%' ), viewEval( y ) ] ) ),  ExpressionInteractor( node ) )
	
	def pow(self, node, x, y):
		return interact( activeBorder( scriptRSuper( viewEval( x ), hbox( [ operatorEditor( self, node, '**' ), viewEval( y ) ] ) ) ),  ExpressionInteractor( node ) )
	
	def loadLocal(self, node, name):
		return interact( activeBorder( focus( entry( name, name, tokeniser ) ) ),  LoadLocalInteractor( node ), ExpressionInteractor( node ) )
	
	def nilExpr(self, node):
		return interact( activeBorder( focus( entry( '<expr>', '', tokeniser, nilStyle ) ) ),  LoadLocalInteractor( node ) )
	
	def listLiteral(self, node, *x):
		return listView( VerticalListViewLayout( 0.0, 0.0, 45.0 ), '[', ']', ',', mapViewEval( x ) )
	
	
	

	
language = GSymLanguage()
language.registerCompilerFactory( 'ascii', SimpleLanguageCompiler)
language.registerViewFactory( SimpleLanguageView )
	
	
	