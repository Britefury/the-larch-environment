##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.DocPresent.Web.ModifierKeys import modifierKeyStringToFlags, MOD_ALT, MOD_CTRL, MOD_SHIFT
from Britefury.DocPresent.Web.SharedObject import SharedObject, JSMethod, JSClassMethod, JSClassNamedMethod
from Britefury.DocPresent.Web.EventFromClient import EventFromClient




class EditAsTextCommit (EventFromClient):
	def __init__(self, sourceID, text, bUserEvent):
		super( EditAsTextCommit, self ).__init__( sourceID )
		
		self.text = text
		self.bUserEvent = bUserEvent
	__init__.jsFunction = \
"""
function (sourceID, text, bUserEvent)
{
	this.sourceID = sourceID;
	this.text = text;
	this.bUserEvent = bUserEvent;
}
"""
	
	
	
	def jsonContent(self):
		return [ self.sourceID, self.text, self.bUserEvent ]
	jsonContent.jsFunction =\
"""function ()
{
	return [ this.sourceID, this.text, this.bUserEvent ];
}
"""
	
	@classmethod
	def fromJSonContent(cls, content):
		return EditAsTextCommit( content[0], content[1], content[2] )
	
	
	fromJSonContent_js = JSClassNamedMethod( 'fromJSonContent', \
"""function (content)
{
	return new EditAsTextCommit( content[0], content[1], content[2] );
}
""" )

	
	
	
	
class EditAsTextHtmlClass (object):
	def __init__(self, className, modifierKeysValue, modifierKeysMask):
		self._className = className
		self._modifierKeysValue = modifierKeyStringToFlags( modifierKeysValue )
		self._modifierKeysMask = modifierKeyStringToFlags( modifierKeysMask )
		
		
	def onLoadJS(self):
		def jsBool(b):
			return 'true'   if b   else   'false'
		return '%s = new EditAsTextClass( %s, %s, %s, %s, %s, %s );\n'  %  ( self._className,
											    jsBool( self._modifierKeysValue & MOD_CTRL != 0 ),
											    jsBool( self._modifierKeysValue & MOD_SHIFT != 0 ),
											    jsBool( self._modifierKeysValue & MOD_ALT != 0 ),
											    jsBool( self._modifierKeysMask & MOD_CTRL != 0 ),
											    jsBool( self._modifierKeysMask & MOD_SHIFT != 0 ),
											    jsBool( self._modifierKeysMask & MOD_ALT != 0 ) )
		
	def apply(self, nodeContext, html, text, handler):
		def _handler(event):
			handler( event.text, event.bUserEvent )
		
		nodeID = nodeContext.viewContext.allocID( 'editastext' )
		editAsTextHtml = '<span id="%s">%s</span><script type="text/javascript">%s.applyTo( $("#%s"), "%s" );</script>'  %  ( nodeID, html, self._className, nodeID, text.replace( '"', '\\"' ) )
		nodeContext.viewContext.registerEventHandler( nodeID, _handler )
		return editAsTextHtml
	
	
	
def editAsText(nodeContext, editAsTextClass, html, text, handler):
	return editAsTextClass.apply( nodeContext, html, text, handler )






	
	
	