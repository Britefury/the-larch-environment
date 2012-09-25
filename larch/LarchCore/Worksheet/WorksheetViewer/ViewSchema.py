##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Pres import InnerFragment


from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet import AbstractViewSchema





class _Projection (object):
	__dispatch_num_args__ = 1
	

	def __call__(self, node, worksheet):
		return methodDispatch( self, node, worksheet )

	@DMObjectNodeDispatchMethod( Schema.Worksheet )
	def worksheet(self, worksheet, node):
		return WorksheetView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Body )
	def body(self, worksheet, node):
		return BodyView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def paragraph(self, worksheet, node):
		return ParagraphView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.TextSpan )
	def textSpan(self, worksheet, node):
		return TextSpanView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.Link )
	def link(self, worksheet, node):
		return LinkView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def pythonCode(self, worksheet, node):
		return PythonCodeView( worksheet, node )
	
	@DMObjectNodeDispatchMethod( Schema.InlineEmbeddedObject )
	def inlineEmbeddedObject(self, worksheet, node):
		return InlineEmbeddedObjectView( worksheet, node )

	@DMObjectNodeDispatchMethod( Schema.ParagraphEmbeddedObject )
	def paragraphEmbeddedObject(self, worksheet, node):
		return ParagraphEmbeddedObjectView( worksheet, node )



class WorksheetView (AbstractViewSchema.WorksheetAbstractView):
	_projection = _Projection()

		

class BodyView (AbstractViewSchema.BodyAbstractView):
	pass

		

class ParagraphView (AbstractViewSchema.ParagraphAbstractView):
	pass

		
		
class TextSpanView (AbstractViewSchema.TextSpanAbstractView):
	pass



class LinkView (AbstractViewSchema.LinkAbstractView):
	pass

		
		
class PythonCodeView (AbstractViewSchema.PythonCodeAbstractView):
	pass



class InlineEmbeddedObjectView (AbstractViewSchema.InlineEmbeddedObjectAbstractView):
	pass



class ParagraphEmbeddedObjectView (AbstractViewSchema.ParagraphEmbeddedObjectAbstractView):
	pass
