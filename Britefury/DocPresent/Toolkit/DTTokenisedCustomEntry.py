##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pyparsing

from Britefury.Util.SignalSlot import *

from Britefury.Math.Math import Colour3f

from Britefury.DocPresent.Toolkit.DTBin import DTBin
from Britefury.DocPresent.Toolkit.DTCustomEntry import DTCustomEntry





class DTTokenisedCustomEntry (DTCustomEntry):
	textModifiedSignal = ClassSignal()			# ( entry, text, parseResult )
	finishEditingSignal = ClassSignal()			# ( entry, text, parseResult, bUserEvent )



	def __init__(self, tokeniser, entryText=None, font=None):
		super( DTTokenisedCustomEntry, self ).__init__( entryText, font )

		self._tokeniser = tokeniser



	def _o_emitFinishEditing(self, bUserEvent):
		text = self.getEntryText()
		self.finishEditingSignal.emit( self, text, self._p_tokeniseText( text ), bUserEvent )

	def _o_emitTextInserted(self, position, bAppended, textInserted):
		super( DTTokenisedCustomEntry, self )._o_emitTextInserted( position, bAppended, textInserted )
		text = self.getEntryText()
		self.textModifiedSignal.emit( self, text, self._p_tokeniseText( text ) )

	def _o_emitTextDeleted(self, start, end, textDeleted):
		super( DTTokenisedCustomEntry, self )._o_emitTextDeleted( start, end, textDeleted )
		text = self.getEntryText()
		self.textModifiedSignal.emit( self, text, self._p_tokeniseText( text ) )


	def _p_tokeniseText(self, text):
		try:
			return self._tokeniser.tokenise( text )
		except pyparsing.ParseException:
			return None







if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk
	import cairo

	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
	from Britefury.DocPresent.Toolkit.DTHLine import DTHLine
	from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
	from Britefury.DocPresent.Toolkit.DTBox import DTBox
	from Britefury.gSym.View.Tokeniser import TokenDefinition, Tokeniser
	from Britefury.Math.Math import Colour3f
	import traceback
	import string
	import pyparsing

	word = TokenDefinition( 'word', pyparsing.Word( pyparsing.alphas ) )
	whitespace = TokenDefinition( 'whitespace', pyparsing.Word( string.whitespace ) )
	dot = TokenDefinition( 'dot', pyparsing.Literal( '.' ) )

	tokeniser = Tokeniser( [ word, whitespace, dot ] )

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	def onFinish(entry, text, parseResult, bUserEvent):
		print 'FINISHED: ', text, parseResult

	def onModified(entry, text, parseResult):
		print 'MODIFIED: ', text, parseResult


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.getGtkWidget().show()
	
	box = DTBox( direction=DTBox.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_EXPAND )

	entry1 = DTTokenisedCustomEntry( tokeniser, 'Hello world' )
	entry1.setCustomChild( DTHLine() )
	box.append( entry1 )
	entry1.finishEditingSignal.connect( onFinish )
	entry1.textModifiedSignal.connect( onModified )
	
	
	entry2 = DTTokenisedCustomEntry( tokeniser, 'Hi there' )
	entry2.setCustomChild( DTLabel( 'abc xyz' ) )
	box.append( entry2 )
	entry2.finishEditingSignal.connect( onFinish )
	entry2.textModifiedSignal.connect( onModified )
	
	
	doc.child = box


	window.add( doc.getGtkWidget() )
	window.show()

	gtk.main()
