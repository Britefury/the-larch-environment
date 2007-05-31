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



def confirmDialog(title, message, yesButtonText, noButtonText, yesAccel, noAccel, bModal, parentWindow):
	dialogFlags = 0
	if bModal:
		dialogFlags |= gtk.DIALOG_MODAL
	if parentWindow is not None:
		dialogFlags |= gtk.DIALOG_DESTROY_WITH_PARENT


	# Dialog
	dialog = gtk.Dialog( title, parentWindow, dialogFlags )

	# Buttons
	yes = dialog.add_button( yesButtonText, gtk.RESPONSE_OK )
	no = dialog.add_button( noButtonText, gtk.RESPONSE_CANCEL )

	# Message text
	messagesBox = gtk.VBox()
	for m in message.split( '\n' ):
		messagesBox.pack_start( gtk.Label( m ) )

	# Message padding
	messagePaddingBox = gtk.HBox()
	messagePaddingBox.pack_start( messagesBox, padding=15 )

	# Pack into dialog
	dialog.vbox.pack_start( messagePaddingBox, padding=30 )

	# Accelerators
	accelerators = gtk.AccelGroup()
	dialog.add_accel_group( accelerators )
	yesAccelKey, yesMods = gtk.accelerator_parse( yesAccel )
	noAccelKey, noMods = gtk.accelerator_parse( noAccel )
	yes.add_accelerator( 'clicked', accelerators, yesAccelKey, yesMods, gtk.ACCEL_VISIBLE )
	no.add_accelerator( 'clicked', accelerators, noAccelKey, noMods, gtk.ACCEL_VISIBLE )

	dialog.show_all()
	dialog.grab_focus()


	# Run
	response = dialog.run()

	dialog.destroy()

	return response == gtk.RESPONSE_OK
