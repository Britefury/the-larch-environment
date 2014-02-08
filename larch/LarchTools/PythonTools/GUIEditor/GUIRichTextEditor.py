##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.util import List
from java.lang import System
from java.awt.datatransfer import DataFlavor
from javax.imageio import ImageIO
from javax.swing import JFileChooser

import itertools

from copy import copy

from BritefuryJ.Controls import Button, Hyperlink, MenuItem, VPopupMenu

from BritefuryJ.Live import TrackedLiveValue

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Border, Image, Label
from BritefuryJ.Pres.RichText import NormalText, Heading1, Heading2, Heading3, Heading4, Heading5, Heading6, Title, RichSpan, Body
from BritefuryJ.Pres.UI import Section, SectionHeading2, ControlsRow

from BritefuryJ.LSpace.TextFocus import TextSelection
from BritefuryJ.LSpace.Input import DndHandler
from BritefuryJ.LSpace.Marker import Marker

from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Editor.RichText import RichTextController

from LarchCore.Languages.Python2 import Schema as Py

from LarchTools.PythonTools.GUIEditor.DataModel import GUIObject, ValueField, ListField, TypedField, ChildListField
from LarchTools.PythonTools.GUIEditor.Component import GUIComponent
from LarchTools.PythonTools.GUIEditor.ComponentPalette import paletteItem, registerPaletteSubsection, PaletteComponentDrag





class GUIRichTextController (RichTextController):
	def setModelContents(self, model, contents):
		model.contents = contents
	
	def modelToEditorModel(self, model):
		return model._editorModel
	
	
	def buildInlineEmbed(self, value):
		return InlineEmbed(value)
	
	def buildParagraphEmbed(self, value):
		return ParaEmbed(value)
	
	def buildParagraph(self, contents, styleAttrs):
		return Para(contents, styleAttrs.get('style'))
	
	def buildSpan(self, contents, styleAttrs):
		return Style(contents, dict(styleAttrs))
	
	
	def isDataModelObject(self, x):
		return isinstance(x, RTElem)
	
	def insertParagraphIntoBlockAfter(self, block, paragraph, p):
		block.insertAfter(paragraph, p)
	
	def deleteParagraphFromBlock(self, block, paragraph):
		block.removeParagraph(paragraph)
	
	def deepCopyInlineEmbedValue(self, value):
		return value


GUIRichTextController.instance = GUIRichTextController('GUI editor rich text editor')



class RTElem (GUIObject):
	def __init__(self, **values):
		self._editorModel = None
		self.__parentElement = None
		super(RTElem, self).__init__(**values)


	@property
	def parent(self):
		return self.__parentElement

	@property
	def document(self):
		return self.__parentElement.document   if self.__parentElement is not None   else None


	def _register(self, parent):
		if self.__parentElement is not None:
			raise RuntimeError, 'Cannot register an element that already has a parent'
		self.__parentElement = parent
		doc = parent.document
		if doc is not None:
			self._addedToDocument(doc)

	def _unregister(self, prevParent):
		if self.__parentElement is None:
			raise RuntimeError, 'Cannot unregister an element that does not have a parent'
		self.__parentElement = None
		doc = prevParent.document
		if doc is not None:
			self._removedFromDocument(doc)


	def _addedToDocument(self, document):
		pass

	def _removedFromDocument(self, document):
		pass



	def __py_evalmodel__(self, codeGen):
		raise NotImplementedError, 'abstract'



class AbstractText (RTElem):
	_contents = ListField()


	@_contents.on_change
	def _contents_changed(self, field, oldContents, newContents):
		if self._editorModel is not None:
			self._editorModel.setModelContents(GUIRichTextController.instance, newContents)

		o = set(oldContents)
		n = set(newContents)
		added = n - o
		removed = o - n
		for x in removed:
			if isinstance(x, RTElem):
				x._unregister(self)
		for x in added:
			if isinstance(x, RTElem):
				x._register(self)


	def __init__(self, contents):
		super(AbstractText, self).__init__(_contents=contents)


	def __setstate__(self, state):
		super(AbstractText, self).__setstate__(state)
		self._editorModel = None



	@property
	def contents(self):
		return self._contents.value

	@contents.setter
	def contents(self, xs):
		self._contents.value = xs


	def _addedToDocument(self, document):
		for x in self._contents.value:
			if isinstance(x, RTElem):
				x._addedToDocument(document)

	def _removedFromDocument(self, document):
		for x in self._contents.value:
			if isinstance(x, RTElem):
				x._removedFromDocument(document)


	def _contents_py_evalmodel_(self, codeGen):
		coerceObj = lambda x: x.__py_evalmodel__(codeGen)
		return Py.coerceToModel(self._contents.value[:], coerceObj=coerceObj)







class Para (AbstractText):
	_style = TypedField(str, 'normal')


	@_style.on_change
	def _style_changed(self, field, oldValue, newValue):
		self._editorModel.setStyleAttrs({'style':newValue})



	def __init__(self, contents, style=None):
		super(Para, self).__init__(contents)
		style = style   if style is not None   else 'normal'
		self._editorModel = GUIRichTextController.instance.editorModelParagraph(self, contents, {'style':style})
		self._style.value = style


	@property
	def style(self):
		return self._style.value

	@style.setter
	def style(self, s):
		self._style.value = s

	
	_styleMap = {'normal':NormalText, 'h1':Heading1, 'h2':Heading2, 'h3':Heading3, 'h4':Heading4, 'h5':Heading5, 'h6':Heading6, 'title':Title}
	

	def __py_evalmodel__(self, codeGen):
		combinatorClass = self._styleMap[self._style.value]
		py_combinatorClass = codeGen.embeddedValue(combinatorClass)
		return Py.Call(target=py_combinatorClass, args=[self._contents_py_evalmodel_(codeGen)])


	def __present__(self, fragment, inheritedState):
		combinatorClass = self._styleMap[self._style.value]
		x = combinatorClass(self._contents.value[:])
		x = GUIRichTextController.instance.editableParagraph(self, x)
		return x



class _TempBlankPara (RTElem):
	_style = TypedField(str, 'normal')



	def __init__(self, block):
		super(_TempBlankPara, self).__init__()
		
		self._block = block
		self._editorModel = GUIRichTextController.instance.editorModelParagraph(None, [], {'style':'normal'})
		

	@property
	def contents(self):
		return []

	@contents.setter
	def contents(self, contents):
		if len(contents) == 0:
			return
		elif len(contents) == 1 and contents[0] == '':
			return
		p = Para(contents, self._style.value)
		self._block.append(p)


	@property
	def style(self):
		return self._style.value

	@style.setter
	def style(self, s):
		self._style.value = s


	_styleMap = {'normal':NormalText, 'h1':Heading1, 'h2':Heading2, 'h3':Heading3, 'h4':Heading4, 'h5':Heading5, 'h6':Heading6, 'title':Title}
	
	def __present__(self, fragment, inheritedState):
		x = NormalText('')
		x = GUIRichTextController.instance.editableParagraph(self, x)
		return x




class Style (AbstractText):
	_styleAttrs = ValueField()


	@_styleAttrs.on_change
	def _styleAttrsChanged(self, field, oldValue, newValue):
		self._styleSheet = self._mapStyles(newValue)
		if self._editorModel is not None:
			self._editorModel.setStyleAttrs(newValue)


	def __init__(self, contents, styleAttrs):
		super(Style, self).__init__(contents)
		self._styleSheet = None
		self._styleAttrs.value = {}
		self._editorModel = GUIRichTextController.instance.editorModelSpan(contents, styleAttrs)
		self.styleAttrs = styleAttrs


	@property
	def styleAttrs(self):
		return self._styleAttrs.value

	@styleAttrs.setter
	def styleAttrs(self, s):
		self._styleAttrs.value = s
	

	def __py_evalmodel__(self, codeGen):
		py_richSpanClass = codeGen.embeddedValue(RichSpan)
		richSpan = Py.Call(target=py_richSpanClass, args=[self._contents_py_evalmodel_(codeGen)])

		py_styleSheet = codeGen.embeddedValue(self._styleSheet)
		return Py.Call(target=Py.AttributeRef(target=py_styleSheet, name='applyTo'), args=[richSpan])


	def __present__(self, fragment, inheritedState):
		x = self._styleSheet.applyTo(RichSpan(self._contents.value[:]))
		x = GUIRichTextController.instance.editableSpan(self, x)
		return x
	
	
	_styleMap = {}
	_styleMap['italic'] = lambda x: (Primitive.fontItalic, bool(x))
	_styleMap['bold'] = lambda x: (Primitive.fontBold, bool(x))
	
	def _mapStyles(self, styleAttrs):
		styleSheet = StyleSheet.instance
		for k in styleAttrs:
			f = self._styleMap[k]
			(attrib, value) = f(styleAttrs[k])
			styleSheet = styleSheet.withAttr(attrib, value)
		return styleSheet
	




class _Embed (RTElem):
	_value = ValueField()

	@_value.on_change
	def _valueChanged(self, field, oldValue, newValue):
		if self._editorModel is not None:
			self._editorModel.setValue(newValue)


	def _addedToDocument(self, document):
		value = self._value.value
		document._addChild(value)

	def _removedFromDocument(self, document):
		value = self._value.value
		document._removeChild(value)


	def __py_evalmodel__(self, codeGen):
		return self._value.value.__py_evalmodel__(codeGen)



class InlineEmbed (_Embed):
	def __init__(self, value):
		super(InlineEmbed, self).__init__(_value=value)
		self._editorModel = GUIRichTextController.instance.editorModelInlineEmbed(value)

	def __present__(self, fragment, inheritedState):
		x = Pres.coerce(self._value.value).withContextMenuInteractor(_inlineEmbedContextMenuFactory)
		x = GUIRichTextController.instance.editableInlineEmbed(self, x)
		return x



class ParaEmbed (_Embed):
	def __init__(self, value):
		super(ParaEmbed, self).__init__(_value=value)
		self._editorModel = GUIRichTextController.instance.editorModelParagraphEmbed(self, value)
	
	def __present__(self, fragment, inheritedState):
		x = Border(self._value.value).withContextMenuInteractor(_paraEmbedContextMenuFactory)
		x = GUIRichTextController.instance.editableParagraphEmbed(self, x)
		return x




def _inlineEmbedContextMenuFactory(element, menu):
	def deleteInlineEmbed(menuItem):
		GUIRichTextController.instance.deleteInlineEmbedContainingElement(element)
	
	
	deleteItem = MenuItem.menuItemWithLabel('Delete', deleteInlineEmbed)
	
	menu.add(deleteItem.alignHExpand())
	
	return True

def _paraEmbedContextMenuFactory(element, menu):
	def deleteEmbedPara(menuItem):
		GUIRichTextController.instance.deleteParagraphContainingElement(element)
	
	
	deleteItem = MenuItem.menuItemWithLabel('Delete', deleteEmbedPara)
	
	menu.add(deleteItem.alignHExpand())
	
	return True




class Block (RTElem):
	_contents = ListField()


	@_contents.on_change
	def _contentsChanged(self, field, oldContents, newContents):
		if self._editorModel is not None:
			self._editorModel.setModelContents(GUIRichTextController.instance, newContents)

		o = set(oldContents)
		n = set(newContents)
		added = n - o
		removed = o - n
		for x in removed:
			if isinstance(x, RTElem):
				x._unregister(self)
		for x in added:
			if isinstance(x, RTElem):
				x._register(self)



	def __init__(self, contents, document):
		self.__document = document
		super(Block, self).__init__(_contents=contents)
		self._editorModel = GUIRichTextController.instance.editorModelBlock(contents)


	@property
	def document(self):
		return self.__document



	def _filterContents(self, xs):
		return [x   for x in xs   if not isinstance(x, _TempBlankPara)]


	@property
	def contents(self):
		return self._contents.value

	@contents.setter
	def contents(self, xs):
		self._contents.value = self._filterContents(list(xs))

	
	
	def append(self, para):
		if not isinstance(para, _TempBlankPara):
			self._contents.value.append(para)

	def insertAfter(self, para, p):
		if not isinstance(para, _TempBlankPara):
			index = -1
			for (i, x) in enumerate(self._contents.value):
				if p is x:
					index = i
			self._contents.value.insert(index + 1, para)

	def removeParagraph(self, para):
		index = -1
		for (i, x) in enumerate(self._contents.value):
			if para is x:
				index = i
		if index != -1:
			del self._contents.value[index]
		else:
			raise ValueError, 'could not find para'
	
	
	def __py_evalmodel__(self, codeGen):
		py_body = codeGen.embeddedValue(Body)
		coerceObj = lambda x: x.__py_evalmodel__(codeGen)
		return Py.Call(target=py_body, args=[Py.coerceToModel(self._contents.value[:], coerceObj=coerceObj)])


	def __present__(self, fragment, inheritedState):
		xs = self._contents.value
		xs = xs[:]   if len(xs) > 0   else [_TempBlankPara(self)]
		x = Body(xs)
		x = GUIRichTextController.instance.editableBlock(self, x)
		return x





class GUIRichTextDocument (GUIComponent):
	componentName = 'Document'


	_contents = ValueField()
	children = ChildListField()


	def __init__(self):
		super(GUIRichTextDocument, self).__init__(_contents=Block([], self))
		self._editorModel = None

	


	def removeChild(self, child):
		self._removeChild(child)

	def getNextSiblingOf(self, child):
		nodes = self.children.nodes
		try:
			index = nodes.index(child) + 1
		except ValueError:
			return None
		if index < len(nodes):
			return nodes[index]
		else:
			return None



	def _addChild(self, child):
		self.children.nodes.append(child)


	def _removeChild(self, child):
		self.children.nodes.remove(child)



	def __iter__(self):
		return iter(self.children.nodes)

	def __len__(self):
		return len(self.children.nodes)




	def _lookFor(self, x):
		for item in self.children.nodes:
			if item.lookFor(x):
				return True
		return False


	def __component_py_evalmodel__(self, codeGen):
		return self._contents.value.__py_evalmodel__(codeGen)


	def _presentContents(self, fragment, inheritedState):
		d = Pres.coerce(self._contents.value).withContextMenuInteractor(_documentContextMenuFactory)
		d = d.withDropDest(PaletteComponentDrag, None, _dndHighlight, _onDropFromPalette)
		d = GUIRichTextController.instance.region(d)
		return d






_documentItem = paletteItem(Label('Document'), lambda: GUIRichTextDocument())


registerPaletteSubsection('Rich text document', [_documentItem])




def _documentContextMenuFactory(element, menu):
	region = element.getRegion()
	rootElement = element.getRootElement()
	
	def makeParagraphStyleFn(style):
		def setParagraphStyle(model):
			model.style = style
		
		def _onLink(link, event):
			caret = rootElement.getCaret()
			if caret is not None and caret.isValid():
				caretElement = caret.getElement()
				if caretElement.getRegion() is region:
					GUIRichTextController.instance.modifyParagraphAtMarker(caret.getMarker(), setParagraphStyle)
		return _onLink
	
	normalStyle = Hyperlink('Normal', makeParagraphStyleFn('normal'))
	h1Style = Hyperlink('H1', makeParagraphStyleFn('h1'))
	h2Style = Hyperlink('H2', makeParagraphStyleFn('h2'))
	h3Style = Hyperlink('H3', makeParagraphStyleFn('h3'))
	h4Style = Hyperlink('H4', makeParagraphStyleFn('h4'))
	h5Style = Hyperlink('H5', makeParagraphStyleFn('h5'))
	h6Style = Hyperlink('H6', makeParagraphStyleFn('h6'))
	titleStyle = Hyperlink('Title', makeParagraphStyleFn('title'))
	paraStyles = ControlsRow([normalStyle, h1Style, h2Style, h3Style, h4Style, h5Style, h6Style, titleStyle])
	menu.add(Section(SectionHeading2('Paragraph styles'), paraStyles))

	
	def makeStyleFn(attrName):
		def computeStyleValues(styleAttrDicts):
			value = dict(styleAttrDicts[0]).get(attrName, False)
			value = not value
			attrs = {}
			attrs[attrName] = value
			return attrs
		
		def onButton(button, event):
			selection = rootElement.getSelection()
			if isinstance(selection, TextSelection):
				if selection.getRegion() == region:
					GUIRichTextController.instance.applyStyleToSelection(selection, computeStyleValues)
		return onButton
	
	italicStyle = Button.buttonWithLabel('I', makeStyleFn('italic'))
	boldStyle = Button.buttonWithLabel('B', makeStyleFn('bold'))
	styles = ControlsRow([italicStyle, boldStyle]).alignHLeft()

	menu.add(Section(SectionHeading2('Selection styles'), styles))

	
	return True


def _dndHighlight(element, graphics, pos, action):
	marker = Marker.atPointIn(element, pos, False)
	if marker is not None and marker.isValid():
		DndHandler.drawCaretDndHighlight(graphics, element, marker)

def _onDropFromPalette(element, pos, data, action):
	marker = Marker.atPointIn(element, pos, True)
	if marker is not None and marker.isValid():
		def _onDropInline(control):
			assert isinstance(data, PaletteComponentDrag)
			def factory():
				return data.getItem()
			GUIRichTextController.instance.insertInlineEmbedAtMarker(marker, factory)
		
		def _onDropParagraph(control):
			def factory():
				return ParaEmbed(data.getItem())
			GUIRichTextController.instance.insertParagraphAtMarker(marker, factory)
		
		menu = VPopupMenu([MenuItem.menuItemWithLabel('Inline', _onDropInline), MenuItem.menuItemWithLabel('As paragraph', _onDropParagraph)])
		menu.popupMenuAtMousePosition(marker.getElement())
	return True
