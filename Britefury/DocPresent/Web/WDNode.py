##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Web.Context.WebViewContext import WebViewContext
from Britefury.DocPresent.Web.Context.WebViewNodeContext import WebViewNodeContext

from Britefury.DocPresent.Web.WDDomEdit import WDDomEdit


class WDNode (WebViewNodeContext):
	def __init__(self, viewContext, htmlFn):
		super( WDNode, self ).__init__( viewContext )

		self._htmlFn = htmlFn
		self._id = viewContext.allocID( 'WDNode' )
		self._bRefreshRequired = True
		viewContext._queueNodeRefresh( self )
		
		
	def html(self):
		if self._bRefreshRequired:
			html = '<span class="WDNode" id="%s">'  %  self._id    +    self._htmlFn( self )    +    '</span>'
			self._bRefreshRequired = False
			self.viewContext._dequeueNodeRefresh( self )
			return html
		else:
			top = self.viewContext.topOperation()
			if not isinstance( top, WDDomEdit ):
				raise TypeError, 'top operation must be a WDDomEdit'
			top.placeHolderIDs.append( self._id )
			return '<span class="__gsym__placeholder" id="%s"></span>'  %  self._id
		
		
	def setHtmlFn(self, htmlFn):
		self._htmlFn = htmlFn
		self._bRefreshRequired = True
		self.viewContext._queueNodeRefresh( self )
		
	def getID(self):
		return self._id
		
		
	
	