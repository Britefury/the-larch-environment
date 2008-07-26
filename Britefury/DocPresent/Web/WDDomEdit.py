##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Web.SharedObject import SharedObject, JSMethod, JSClassMethod, JSClassNamedMethod


class WDDomEdit (SharedObject):
	def __init__(self):
		super( WDDomEdit, self ).__init__()
		
		self.placeHolderIDs = []
		self.html = ''
		self.nodeID = ''
	__init__.jsFunction = \
"""
function ()
{
	this.placeHolderIDs = [];
	this.html = "";
	this.nodeID = "";
}
"""
	
	
	
	def jsonContent(self):
		return [ self.placeHolderIDs, self.html, self.nodeID ]
	jsonContent.jsFunction =\
"""function ()
{
	return [ this.placeHolderIDs, this.html, this.nodeID ];
}
"""
	
	@classmethod
	def fromJSonContent(cls, content):
		o = WDDomEdit()
		o.placeHolderIDs = content[0]
		o.html = content[1]
		o.nodeID = content[2]
		return o
	
	
	fromJSonContent_js = JSClassNamedMethod( 'fromJSonContent', \
"""function (content)
{
	var o = new WDDomEdit();
	o.placeHolderIDs = content[0];
	o.html = content[1];
	o.nodeID = content[2];
	return o;
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

	
	
	
		
	