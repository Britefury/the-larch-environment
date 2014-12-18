##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import os, platform

from javax.swing import JFileChooser, JOptionPane
from java.awt import Color, BasicStroke
from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Controls import Hyperlink, MenuItem, VPopupMenu, Checkbox
from BritefuryJ.Graphics import FilledOutlinePainter
from BritefuryJ.DefaultPerspective import DefaultPerspective
from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Column
from BritefuryJ.Pres.RichText import Body, RichSpan
from BritefuryJ.Pres.UI import Section, SectionHeading2, SectionHeading3, NotesText
from BritefuryJ.StyleSheet import StyleSheet
from Britefury.Config.UserConfig import loadUserConfig, saveUserConfig
from Britefury.Config import Configuration
from Britefury.Config.ConfigurationPage import ConfigurationPage
from LarchCore.Kernel import kernel_factory
from LarchCore.Kernel.python import inproc_kernel, ipython_kernel


_item_hover_highlight_style = StyleSheet.style( Primitive.hoverBackground( FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )


_interpreter_config_filename = 'interpreters'



class CouldNotFindIPythonError (Exception):
	pass

def _find_ipython(python_path):
	paths = [os.path.join(python_path, 'bin', 'ipython'),	# POSIX
		 os.path.join(python_path, 'scripts', 'ipython.exe')	# Windows
	]

	for p in paths:
		if os.path.exists(p):
			return p

	return None




class AbstractInterpreterConfigEntry (object):
	def __init__(self):
		self._config_page = None


	@property
	def kernel_description(self):
		raise NotImplementedError, 'abstract'

	@property
	def kernel_factory(self):
		raise NotImplementedError, 'abstract'


	def __getstate__(self):
		return {}

	def __setstate__(self, state):
		self._config_page = None



class InProcessInterpreterConfigEntry (AbstractInterpreterConfigEntry):
	def __init__(self):
		super(InProcessInterpreterConfigEntry, self).__init__()
		self.__kernel_description = kernel_factory.KernelDescription.standard_format('inprocess',
											     kernel_factory.VM_JYTHON,
											     platform.python_version_tuple(),
											     kernel_factory.VARIANT_NONE,
											     kernel_factory.DISTRIBUTION_NONE)
		self.__kernel_factory = None


	@property
	def kernel_description(self):
		return self.__kernel_description


	@property
	def kernel_factory(self):
		if self.__kernel_factory is None:
			inproc_context = self._config_page._inproc_context
			def create_kernel(kernel_created_callback):
				inproc_context.start_kernel(kernel_created_callback)

			self.__kernel_factory = kernel_factory.KernelFactory(self.__kernel_description, create_kernel)
		return self.__kernel_factory


	def __getstate__(self):
		raise NotImplementedError, 'Operation not supported'

	def __setstate__(self, state):
		raise NotImplementedError, 'Operation not supported'


	def __present__(self, fragment, inh):
		return Pres.coerce(self.__kernel_description)



class IPythonInterpreterConfigEntry (AbstractInterpreterConfigEntry):
	def __init__(self, kernel_description, ipython_path):
		super(IPythonInterpreterConfigEntry, self).__init__()
		self.__ipython_path = ipython_path
		self.__kernel_description = kernel_description
		self.__kernel_factory = None


	@property
	def kernel_description(self):
		return self.__kernel_description


	@property
	def kernel_factory(self):
		ipython_context = self._config_page._ipython_context
		if self.__kernel_factory is None:
			def create_kernel(kernel_created_callback):
				ipython_context.start_kernel(kernel_created_callback, ipython_path=self.__ipython_path)
			self.__kernel_factory = kernel_factory.KernelFactory(self.__kernel_description, create_kernel)
		return self.__kernel_factory


	def __getstate__(self):
		state = super(IPythonInterpreterConfigEntry, self).__getstate__()
		state['ipython_path'] = self.__ipython_path
		state['kernel_description'] = self.__kernel_description
		return state

	def __setstate__(self, state):
		super(IPythonInterpreterConfigEntry, self).__setstate__(state)
		self.__ipython_path = state.get('ipython_path')
		self.__kernel_description = state.get('kernel_description')
		self.__kernel_factory = None


	def __present__(self, fragment, inh):
		return Pres.coerce(self.__kernel_description)




class InterpreterConfigurationPage (ConfigurationPage):
	def __init__(self):
		super( InterpreterConfigurationPage, self ).__init__()
		self.__kernels = []
		self.__enable_in_proc_kernels = False
		self.__initialise()


	def __initialise(self):
		self._incr = IncrementalValueMonitor()
		self._inproc_context = inproc_kernel.InProcessContext()
		self._ipython_context = ipython_kernel.IPythonContext()
		self.__inproc_entry = InProcessInterpreterConfigEntry()
		self.__register_kernel(self.__inproc_entry)
		for kernel in self.__kernels:
			self.__register_kernel(kernel)




	def __getstate__(self):
		state = super( InterpreterConfigurationPage, self ).__getstate__()
		state['kernels'] = self.__kernels
		state['enable_in_proc_kernels'] = self.__enable_in_proc_kernels
		return state

	def __setstate__(self, state):
		super( InterpreterConfigurationPage, self ).__setstate__( state )
		self.__kernels = state['kernels']
		self.__enable_in_proc_kernels = state.get('enable_in_proc_kernels', False)
		self.__initialise()


	def __register_kernel(self, kernel):
		kernel._config_page = self

	@property
	def kernel_entries(self):
		if self.__enable_in_proc_kernels:
			return self.__kernels + [self.__inproc_entry]
		else:
			return self.__kernels

	@property
	def kernel_descriptions(self):
		return [entry.kernel_description   for entry in self.kernel_entries]

	@property
	def kernel_factories(self):
		return [entry.kernel_factory   for entry in self.kernel_entries]


	def get_best_kernel_factory(self, kernel_description):
		return kernel_description.get_best_factory(self.kernel_factories)


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
		ipython_path = _find_ipython(python_path)

		if ipython_path is not None:
			def on_descr(kernel_information):
				kernel_desc = kernel_factory.KernelDescription.from_kernel_information('IPython', kernel_information)
				entry = IPythonInterpreterConfigEntry(kernel_desc, ipython_path)
				entry._config_page = self
				entry_callback(entry)

			self._ipython_context.get_kernel_description(on_descr, ipython_path=ipython_path)
		else:
			raise CouldNotFindIPythonError


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

						try:
							self._make_kernel_config_entry(on_entry_made, filename)
						except CouldNotFindIPythonError:
							JOptionPane.showMessageDialog(component, "Could not find IPython", "Error", JOptionPane.ERROR_MESSAGE)


		newLink = Hyperlink( 'NEW', _on_new )
		controls = newLink.pad( 10.0, 5.0 )

		def on_enable_inproc(checkbox, state):
			self.__enable_in_proc_kernels = True
			self._incr.onChanged()

		in_proc_header = SectionHeading3('In-process kernels')
		in_proc_warning = NotesText([self._in_proc_warning_style.applyTo(RichSpan(self._in_proc_warning_txt))])
		enable_in_proc_checkbox = Checkbox.checkboxWithLabel('Enable in-process kernels', self.__enable_in_proc_kernels, on_enable_inproc)
		in_proc_section = Section(in_proc_header, Column([in_proc_warning, enable_in_proc_checkbox]))


		kernels = Column( [ interpreter_item( i )   for i in xrange( len( kernel_list ) ) ] )
		return Column( [ controls, kernels.padX( 5.0 ), in_proc_section.padY(19,9) ] )


	def interpreters_section(self, title, pathList):
		pathsPres = self.present_interpreter_list( pathList )
		return Section( SectionHeading2( title ), pathsPres )



	def __present_contents__(self, fragment, inheritedState):
		self._incr.onAccess()
		ipython_kernels = self.interpreters_section( 'IPython interpreters', self.__kernels )
		return Body( [ ipython_kernels ] )


	def interpreter_chooser_menu(self, on_choose_kernel_factory):
		def make_on_menu_item(krn_entry):
			def on_menu_item(menu_item):
				on_choose_kernel_factory(krn_entry.kernel_factory)
			return on_menu_item

		menu_items = [MenuItem(DefaultPerspective.instance.applyTo(krn_entry), make_on_menu_item(krn_entry)) \
			      for krn_entry in self.kernel_entries]
		return VPopupMenu(menu_items)


	_in_proc_warning_style = StyleSheet.style(Primitive.foreground(Color(0.6, 0.0, 0.0)))
	_in_proc_warning_txt = \
		'WARNING: executing buggy code in an in-process kernel can lock up Larch, preventing you from saving your work. ' + \
		'Only enable and use in-process kernels if you find this risk acceptable.'


def _load_interpreter_config():
	return loadUserConfig( _interpreter_config_filename )


def save_interpreter_config():
	saveUserConfig( _interpreter_config_filename, _interpreter_config )


def shutdown_interpreter_config():
	_interpreter_config._ipython_context.close()



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

