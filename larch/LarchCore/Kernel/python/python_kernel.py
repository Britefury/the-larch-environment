##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
from LarchCore.Kernel import abstract_kernel




class AbstractPythonLiveModule (abstract_kernel.AbstractLiveModule):
	pass



class AbstractPythonKernel (abstract_kernel.AbstractKernel):
	def _shutdown(self):
		pass

	def new_live_module(self, full_name):
		raise NotImplementedError, 'abstract'


	def set_module_source(self, fullname, source):
		raise NotImplementedError, 'abstract'

	def remove_module(self, fullname):
		raise NotImplementedError, 'abstract'




class AbstractPythonContext (object):
	def start_kernel(self, on_kernel_started):
		raise NotImplementedError, 'abstract'

	def shutdown_kernel(self, kernel):
		raise NotImplementedError, 'abstract'



