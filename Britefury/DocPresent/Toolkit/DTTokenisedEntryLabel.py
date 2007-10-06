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
from Britefury.DocPresent.Toolkit.DTEntryLabel import DTEntryLabel





class DTTokenisedEntryLabel (DTEntryLabel):
	textModifiedSignal = ClassSignal()			# ( entry, text, parseResult )
	finishEditingSignal = ClassSignal()			# ( entry, text, parseResult, bUserEvent )



	def __init__(self, tokeniser, text='', labelFilter=None, bLabelMarkup=False, font=None, textColour=Colour3f( 0.0, 0.0, 0.0 )):
		super( DTTokenisedEntryLabel, self ).__init__( text, labelFilter, bLabelMarkup, font, textColour )

		self._tokeniser = tokeniser



	def _o_emitFinishEditing(self, bUserEvent):
		self.finishEditingSignal.emit( self, self.text, self._p_tokeniseText( self.text ), bUserEvent )

	def _o_emitTextInserted(self, position, bAppended, textInserted):
		super( DTTokenisedEntryLabel, self )._o_emitTextInserted( position, bAppended, textInserted )
		self.textModifiedSignal.emit( self, self.text, self._p_tokeniseText( self.text ) )

	def _o_emitTextDeleted(self, start, end, textDeleted):
		super( DTTokenisedEntryLabel, self )._o_emitTextDeleted( start, end, textDeleted )
		self.textModifiedSignal.emit( self, self.text, self._p_tokeniseText( self.text ) )


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
	from Britefury.DocView.DocViewTokeniser import DocViewTokeniser, DocViewTokenDefinition
	from Britefury.Math.Math import Colour3f
	import traceback
	import string
	import pyparsing

	word = DocViewTokenDefinition( 'word', pyparsing.Word( pyparsing.alphas ) )
	whitespace = DocViewTokenDefinition( 'whitespace', pyparsing.Word( string.whitespace ) )
	dot = DocViewTokenDefinition( 'dot', pyparsing.Literal( '.' ) )

	tokeniser = DocViewTokeniser( [ word, whitespace, dot ] )

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	def onModified(entry, text, parseResult):
		print 'MODIFIED: ', text, parseResult


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	entry = DTTokenisedEntryLabel( tokeniser, 'Hello world' )
	doc.child = entry
	entry.textModifiedSignal.connect( onModified )


	window.add( doc )
	window.show()

	gtk.main()
