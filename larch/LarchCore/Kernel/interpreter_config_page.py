##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter
from java.net import URI

from java.awt import Color, BasicStroke

import os
import sys

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import Hyperlink, MenuItem

from BritefuryJ.Graphics import SolidBorder, FilledOutlinePainter

from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Border, Row, Column, Table
from BritefuryJ.Pres.RichText import Body, NormalText, EmphSpan
from BritefuryJ.Pres.UI import Section, SectionHeading2, SectionHeading3, NotesText
from BritefuryJ.StyleSheet import StyleSheet

from Britefury.Config.UserConfig import loadUserConfig, saveUserConfig
from Britefury.Config import Configuration
from Britefury.Config.ConfigurationPage import ConfigurationPage

from LarchCore.Kernel import kernel_factory, ipython_kernel


_interpreter_type_style = StyleSheet.style(Primitive.fontSize(10), Primitive.fontSmallCaps(True), Primitive.foreground(Color(0.4, 0.4, 0.4)))
_interpreter_descr_style = StyleSheet.style(Primitive.foreground(Color(0.2, 0.2, 0.6)))
_interpreter_border = SolidBorder(1.0, 3.0, 5.0, 5.0, Color(0.3, 0.3, 0.3), None)

_item_hover_highlight_style = StyleSheet.style( Primitive.hoverBackground( FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )


_interpreter_config_filename = 'interpreters'


def _present_interpreter(kernel_type, kernel_description):
	ktype = _interpreter_type_style.applyTo(Label(kernel_type))
	descr = _interpreter_descr_style.applyTo(Label(kernel_description.human_description))
	return _interpreter_border.surround(Column([ktype, descr.padX(7.0, 0.0)]))



class IPythonInterpreterConfigEntry (object):
	def __init__(self, kernel_description, ipython_path):
		self.__ipython_path = ipython_path
		self.__kernel_description = kernel_description
		self.__kernel_factory = None
		self._config_page = None


	def kernel_factory(self, ipython_context):
		if self.__kernel_factory is None:
			def create_kernel(kernel_created_callback):
				ipython_context.start_kernel(kernel_created_callback, ipython_path=self.__ipython_path)
			self.__kernel_factory = kernel_factory.KernelFactory(self.__kernel_description, create_kernel)
		return self.__kernel_factory


	def __getstate__(self):
		state = {}
		state['ipython_path'] = self.__ipython_path
		state['kernel_description'] = self.__kernel_description
		return state

	def __setstate__(self, state):
		self.__ipython_path = state.get('ipython_path')
		self.__kernel_description = state.get('kernel_description')
		self.__kernel_factory = None
		self._config_page = None


	def __present__(self, fragment, inh):
		return _present_interpreter('IPython', self.__kernel_description)






class InterpreterConfigurationPage (ConfigurationPage):
	def __init__(self):
		super( InterpreterConfigurationPage, self ).__init__()
		self.__kernels = []
		self._incr = IncrementalValueMonitor()
		self._kernel_context = ipython_kernel.IPythonContext()


	def __getstate__(self):
		state = super( InterpreterConfigurationPage, self ).__getstate__()
		state['kernels'] = self.__kernels
		return state

	def __setstate__(self, state):
		super( InterpreterConfigurationPage, self ).__setstate__( state )
		self.__kernels = state['kernels']
		self._incr = IncrementalValueMonitor()
		self._kernel_context = ipython_kernel.IPythonContext()
		for kernel in self.__kernels:
			kernel._config_page = self




	@property
	def kernels(self):
		return self.__kernels


	def getSubjectTitle(self):
		return '[CFG] Interpreters'

	def getTitleText(self):
		return 'Interpreter Configuration'

	def getLinkText(self):
		return 'Interpreters'


	def _check_python_path(self, python_path):
		if os.path.isdir(python_path):
			return True
		else:
			print 'WARNING: {0} is not directory'.format(python_path)
		return False


	def _make_kernel_config_entry(self, entry_callback, python_path):
		# Get the description of the kernel at the given path
		ipython_path = os.path.join(python_path, 'bin', 'ipython')
		def on_descr(kernel_information):
			kernel_desc = kernel_factory.KernelDescription.from_kernel_information(kernel_information)
			entry = IPythonInterpreterConfigEntry(kernel_desc, python_path)
			entry._config_page = self
			entry_callback(entry)

		self._kernel_context.get_kernel_description(on_descr, ipython_path=ipython_path)


	def present_interpreter_list(self, kernel_list):
		def interpreter_item(index):
			def _on_delete(menuItem):
				del kernel_list[index]
				self._incr.onChanged()

			def build_context_menu(element, menu):
				menu.add( MenuItem.menuItemWithLabel( 'Delete', _on_delete ) )
				return True

			return _item_hover_highlight_style.applyTo( kernel_list[index] ).withContextMenuInteractor( build_context_menu )


		def _on_new(hyperlink, event):
			component = hyperlink.getElement().getRootElement().getComponent()
			openDialog = JFileChooser()
			openDialog.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY )
			response = openDialog.showDialog( component, 'Choose python distribution path' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None  and  self._check_python_path(filename):
						def on_entry_made(entry):
							kernel_list.append(entry)
							self._incr.onChanged()

						self._make_kernel_config_entry(on_entry_made, filename)

		newLink = Hyperlink( 'NEW', _on_new )
		controls = newLink.pad( 10.0, 5.0 )

		kernels = Column( [ interpreter_item( i )   for i in xrange( len( kernel_list ) ) ] )
		return Column( [ controls, kernels.padX( 5.0 ) ] )


	def interpreters_section(self, title, pathList):
		pathsPres = self.present_interpreter_list( pathList )
		return Section( SectionHeading2( title ), pathsPres )



	def __present_contents__(self, fragment, inheritedState):
		self._incr.onAccess()
		ipython_kernels = self.interpreters_section( 'IPython interpreters', self.__kernels )
		return Body( [ ipython_kernels ] )


def _load_interpreter_config():
	return loadUserConfig( _interpreter_config_filename )


def save_interpreter_config():
	saveUserConfig( _interpreter_config_filename, _interpreter_config )




_interpreter_config = None


def init_interpreter_config():
	global _interpreter_config

	if _interpreter_config is None:
		_interpreter_config = _load_interpreter_config()

		if _interpreter_config is None:
			_interpreter_config = InterpreterConfigurationPage()

		Configuration.registerSystemConfigurationPage( _interpreter_config )


def get_interpreter_config():
	global _interpreter_config

	if _interpreter_config is None:
		_interpreter_config = _load_interpreter_config()

		if _interpreter_config is None:
			_interpreter_config = InterpreterConfigurationPage()

	return _interpreter_config

