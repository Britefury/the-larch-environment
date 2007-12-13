##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************



class DocViewEvent (object):
	"""
	DocView event

	receivingNodeViewPath  -  the view nodes along the path
	receivingDocNodeKeyPath  -  the document node keys along the path

	Events proceed along paths:
		The event is sent to a node N.
			If N can handle the event, then the path is [ N ]
			If N cannot handle the event, it is sent to the parent node P, the path is [ P, N ]
				If P cannot handle the event, it is sent to P's parent, etc.
	"""

	def __init__(self, receivingNodeViewPath):
		self.receivingNodeViewPath = receivingNodeViewPath
		self.receivingDocNodeKeyPath = [ nodeView._docNodeKey   for nodeView in receivingNodeViewPath ]
		self.nodeView = receivingNodeViewPath[0]
		self.docNodeKey = self.receivingDocNodeKeyPath[0]



class DocViewEventKey (DocViewEvent):
	def __init__(self, receivingNodeViewPath, keyPressEvent):
		super( DocViewEventKey, self ).__init__( receivingNodeViewPath )
		self.keyPressEvent = keyPressEvent



class DocViewEventEmpty (DocViewEvent):
	pass



class DocViewEventToken (DocViewEvent):
	def __init__(self, receivingNodeViewPath, token, tokenIndex, numTokens):
		super( DocViewEventToken, self ).__init__( receivingNodeViewPath )
		self.token = token
		self.tokenIndex = tokenIndex
		self.numTokens = numTokens
		self.bFirst = tokenIndex == 0
		self.bLast = tokenIndex == ( numTokens - 1 )


