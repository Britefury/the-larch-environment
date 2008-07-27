##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Web.SharedObject import SharedObject, JSMethod, JSClassMethod, JSClassNamedMethod


class WDDomEdit (SharedObject):
	def __init__(self, nodeID, html, placeHolderIDs):
		super( WDDomEdit, self ).__init__()
		
		self.nodeID = nodeID
		self.html = html
		self.placeHolderIDs = placeHolderIDs
	__init__.jsFunction = \
"""
function (nodeID, html, placeHolderIDs)
{
	this.nodeID = nodeID;
	this.html = html;
	this.placeHolderIDs = placeHolderIDs;
}
"""
	
	
	
	def jsonContent(self):
		return [ self.nodeID, self.html, self.placeHolderIDs ]
	jsonContent.jsFunction =\
"""function ()
{
	return [ this.nodeID, this.html, this.placeHolderIDs ];
}
"""
	
	@classmethod
	def fromJSonContent(cls, content):
		return WDDomEdit( content[0], content[1], content[2] )
	
	
	fromJSonContent_js = JSClassNamedMethod( 'fromJSonContent', \
"""function (content)
{
	return new WDDomEdit( content[0], content[1], content[2] );
}
""" )


	__js__handle = JSMethod( 'handle', \
"""function ()
{
	if ( this.nodeID != "" )
	{
		// We have a node ID
		var placeHolderContentClones = [];
		
		for (i in this.placeHolderIDs)
		{
			var phID = this.placeHolderIDs[i];
			placeHolderContentClones.push( $("#"+phID).clone( true ) );
		}
		
		var nodeToReplace = $("#"+this.nodeID);
		nodeToReplace.replaceWith( this.html );
		
		for (i in this.placeHolderIDs)
		{
			var phID = this.placeHolderIDs[i];
			$("#"+phID).replaceWith( placeHolderContentClones[i] );
		}
	}
}
""" )

	
	
	
		
	