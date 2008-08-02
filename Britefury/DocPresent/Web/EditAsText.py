##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocPresent.Web.ModifierKeys import modifierKeyStringToFlags, MOD_ALT, MOD_CTRL, MOD_SHIFT
from Britefury.DocPresent.Web.SharedObject import SharedObject, JSMethod, JSClassMethod, JSClassNamedMethod


class TextEditCommit (SharedObject):
	def __init__(self, editorID, text):
		super( TextEditCommit, self ).__init__()
		
		self.editorID = editorID
		self.text = text
	__init__.jsFunction = \
"""
function (editorID, text)
{
	this.editorID = editorID;
	this.text = text;
}
"""
	
	
	
	def jsonContent(self):
		return [ self.editorID, self.text ]
	jsonContent.jsFunction =\
"""function ()
{
	return [ this.editorID, this.text ];
}
"""
	
	@classmethod
	def fromJSonContent(cls, content):
		return TextEditCommit( content[0], content[1] )
	
	
	fromJSonContent_js = JSClassNamedMethod( 'fromJSonContent', \
"""function (content)
{
	return new TextEditCommit( content[0], content[1] );
}
""" )


	__js__handle = JSMethod( 'handle', \
"""function ()
{
	log( "Cannot handle TextEditCommit on the client side" );
}
""" )

	
	
	
		
	
class EditAsTextHtmlClass (object):
	def __init__(self, className, modifierKeysValue, modifierKeysMask):
		self._className = className
		self._modifierKeysValue = modifierKeyStringToFlags( modifierKeysValue )
		self._modifierKeysMask = modifierKeyStringToFlags( modifierKeysMask )
		
		
	def onLoadJS(self):
		def jsBool(b):
			if b:
				return 'true'
			else:
				return 'false'
		return '%s = new EditAsTextClass( %s, %s, %s, %s, %s, %s );\n'  %  ( self._className,
											    jsBool( self._modifierKeysValue & MOD_CTRL != 0 ),
											    jsBool( self._modifierKeysValue & MOD_SHIFT != 0 ),
											    jsBool( self._modifierKeysValue & MOD_ALT != 0 ),
											    jsBool( self._modifierKeysMask & MOD_CTRL != 0 ),
											    jsBool( self._modifierKeysMask & MOD_SHIFT != 0 ),
											    jsBool( self._modifierKeysMask & MOD_ALT != 0 ) )
		
	def apply(self, nodeContext, html):
		nodeID = nodeContext.viewContext.allocID( 'editastext' )
		return '<span id="%s">%s</span><script type="text/javascript">%s.applyTo( $("#%s") );</script>'  %  ( nodeID, html, self._className, nodeID )
	
	
	
def editAsText(nodeContext, html, editAsTextClass):
	return editAsTextClass.apply( nodeContext, html )






	
	
	