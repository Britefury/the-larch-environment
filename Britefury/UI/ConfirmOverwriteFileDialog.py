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


def confirmOverwriteFileDialog(filename, parent):
	dialog = gtk.Dialog( _( 'Confirm' ), parent, gtk.DIALOG_MODAL | gtk.DIALOG_DESTROY_WITH_PARENT, ( gtk.STOCK_YES, gtk.RESPONSE_ACCEPT, gtk.STOCK_NO, gtk.RESPONSE_REJECT ) )
	dialog.vbox.pack_start( gtk.Label( _( 'The file %s already exists' ) % ( filename, ) ) )
	dialog.vbox.pack_start( gtk.Label( _( 'Overwrite?' ) ) )
	dialog.show_all()
	response = dialog.run()
	dialog.destroy()
	return response == gtk.RESPONSE_ACCEPT
