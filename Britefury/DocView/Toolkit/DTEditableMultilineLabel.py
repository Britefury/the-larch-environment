##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Util.SignalSlot import *

from Britefury.Math.Math import Colour3f

from Britefury.DocView.Toolkit.DTBin import DTBin
from Britefury.DocView.Toolkit.DTMultilineLabel import DTMultilineLabel
from Britefury.DocView.Toolkit.DTEntry import DTEntry




class DTEditableMultilineLabel (DTMultilineLabel):
	startEditingSignal = ClassSignal()		# self, text
	finishEditingSignal = ClassSignal()	# self, text


	def __init__(self, text='', bMarkup=False, font=None, colour=Colour3f( 0.0, 0.0, 0.0 ), hAlign=DTMultilineLabel.HALIGN_CENTRE, vAlign=DTMultilineLabel.VALIGN_CENTRE):
		markup = None
		if bMarkup:
			markup = text
			text = ''
		self._bMarkup = bMarkup
		super( DTEditableMultilineLabel, self ).__init__( text, markup, font, colour, hAlign, vAlign )
		self._bEditWindowVisible = False


	def _o_onButtonDown(self, localPos, button, state):
		super( DTEditableMultilineLabel, self )._o_onButtonDown( localPos, button, state )
		return button == 1

	def _o_onButtonUp(self, localPos, button, state):
		super( DTEditableMultilineLabel, self )._o_onButtonUp( localPos, button, state )
		if button == 1:
			self.startEditing()
			return True
		else:
			return False



	def startEditing(self):
		self.grabFocus()
		if not self._bEditWindowVisible:
			self._p_showEditWindow()
			if self._bMarkup:
				text = self.markup
			else:
				text = self.text
			self.startEditingSignal.emit( self, text )

	def finishEditing(self):
		self.ungrabFocus()


	def startEditingOnLeft(self):
		self.startEditing()
		buf = self._textView.get_buffer()
		buf.place_cursor( buf.get_start_iter() )

	def startEditingOnRight(self):
		self.startEditing()
		buf.place_cursor( buf.get_end_iter() )



	def _o_onLoseFocus(self):
		super( DTEditableMultilineLabel, self )._o_onLoseFocus()
		self._o_queueFullRedraw()
		self._p_hideEditorWindow()


	def _p_showEditWindow(self):
		if not self._bEditWindowVisible:
			if self._bMarkup:
				text = self.markup
			else:
				text = self.text
			self._textView = gtk.TextView()
			self._textView.get_buffer().set_text( text )
			self._textView.set_wrap_mode( gtk.WRAP_WORD )
			self._textView.show()

			scrolledWindow = gtk.ScrolledWindow()
			scrolledWindow.set_policy( gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC )
			scrolledWindow.add( self._textView )
			scrolledWindow.set_size_request( 400, 300 )
			scrolledWindow.show()

			okButton = gtk.Button( stock=gtk.STOCK_OK )
			okButton.show()
			okButton.connect( 'clicked', self._p_onEditWindowOk )

			buttonBox = gtk.HBox()
			buttonBox.pack_end( okButton, False, False, padding=20 )
			buttonBox.show()

			vbox = gtk.VBox( spacing=10 )
			vbox.pack_start( scrolledWindow )
			vbox.pack_start( buttonBox )
			vbox.show()

			self._editWindow = gtk.Window( gtk.WINDOW_TOPLEVEL )
			#self._editWindow.set_transient_for( self._window )
			self._editWindow.add( vbox )
			self._editWindow.set_title( 'Python source' )
			self._editWindow.set_border_width( 20 )

			finishAccel = gtk.accelerator_parse( '<control>Return' )
			accelGroup = gtk.AccelGroup()
			accelGroup.connect_group( finishAccel[0], finishAccel[1], gtk.ACCEL_VISIBLE, self._p_onEditorFinishAccel )
			self._editWindow.add_accel_group( accelGroup )

			self._editWindow.show()


			self._bEditWindowVisible = True


	def _p_hideEditorWindow(self):
		if self._bEditWindowVisible:
			buf = self._textView.get_buffer()
			text = buf.get_text( buf.get_start_iter(), buf.get_end_iter() )
			if self._bMarkup:
				self.markup = text
			else:
				self.text = text
			self._editWindow.destroy()
			self._editWindow = None
			self._textView = None

			self._bEditWindowVisible = False

			self.finishEditingSignal.emit( self, text )


	def _p_onEditWindowOk(self, widget):
		self._p_hideEditorWindow()

	def _p_onEditorFinishAccel(self, accelGroup, widget, key, modifier):
		self._p_hideEditorWindow()









if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk
	import cairo

	from Britefury.DocView.Toolkit.DTDocument import DTDocument
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()



	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	entry = DTEditableMultilineLabel( 'Hello world' )
	doc.child = entry


	window.add( doc )
	window.show()

	gtk.main()
