##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.Incremental import IncrementalValueMonitor


from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchCore.Worksheet import Schema
from LarchCore.Worksheet import AbstractViewSchema





class _Projection (object):
	__dispatch_num_args__ = 1
	

	def __call__(self, node, worksheet):
		return methodDispatch( self, node, worksheet )

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

	@DMObjectNodeDispatchMethod( Schema.InlinePythonCode )
	def inlinePythonCode(self, worksheet, node):
		return InlinePythonCodeView( worksheet, node )

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

		
		
class InlinePythonCodeView (AbstractViewSchema.InlinePythonCodeAbstractView):
	pass



class InlineEmbeddedObjectView (AbstractViewSchema.InlineEmbeddedObjectAbstractView):
	pass



class ParagraphEmbeddedObjectView (AbstractViewSchema.ParagraphEmbeddedObjectAbstractView):
	pass
