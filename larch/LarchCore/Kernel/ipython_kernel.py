##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import sys
JARS = ['jeromq-0.3.5-SNAPSHOT.jar', 'guava-17.0.jar']
for jar in JARS:
	if jar not in sys.path:
		sys.path.append(jar)
from collections import deque
import subprocess, os, atexit

from java.awt import Color
from javax.swing import SwingUtilities

from jipy import kernel

from BritefuryJ.Pres.Primitive import Primitive, Label, Column
from BritefuryJ.Pres.ObjectPres import ErrorBoxWithFields, HorizontalField, VerticalField
from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.StyleSheet import StyleSheet

from . import abstract_kernel
from LarchCore.Languages.Python2 import CodeGenerator
from LarchCore.Languages.Python2.Execution import Execution


_aborted_border = SolidBorder(1.5, 2.0, 5.0, 5.0, Color(1.0, 0.5, 0.0), Color(1.0, 1.0, 0.9))
_aborted_style = StyleSheet.style(Primitive.foreground(Color(1.0, 0.0, 0.0)))
_aborted = _aborted_border.surround(_aborted_style.applyTo(Label('ABORTED')))


POLL_TIMEOUT = 10


class IPythonModule (abstract_kernel.AbstractModule):
	def __init__(self, kernel, name):
		self.__kernel = kernel
		self.name = name

	def getResultOfExecution(self, code, evaluate_last_expression, result_callback):
		self.__kernel._queue_exec(self, code, evaluate_last_expression, result_callback)


class _KernelListener (kernel.KernelRequestListener):
	def __init__(self, finished):
		super(_KernelListener, self).__init__()
		self.std = Execution.MultiplexedRichStream(['out', 'err'])
		self.finished = finished
		self.result = Execution.ExecutionResult(self.std)


	def on_execute_ok(self, execution_count, payload, user_expressions):
		print 'KernelListener.on_execute_ok'
		self.finished(self.result)

	def on_execute_error(self, ename, evalue, traceback):
		print 'KernelListener.on_execute_error'
		tb = Column([Label(x)   for x in traceback])
		fields = []
		fields.append(HorizontalField('Exception name:', Label(ename)))
		fields.append(HorizontalField('Exception value:', Label(evalue)))
		fields.append(VerticalField('Traceback:', tb))
		self.result.caught_exception = ErrorBoxWithFields('EXCEPTION IN IPYTHON KERNEL', fields)
		self.finished(self.result)

	def on_execute_abort(self):
		print 'KernelListener.on_execute_abort'
		self.result.result = [_aborted]
		self.finished(self.result)


	def on_execute_result(self, execution_count, data, metadata):
		print 'KernelListener.on_execute_result'
		text = data.get('text/plain')
		if text is not None:
			self.result.result = [text]


	def on_stream(self, stream_name, data):
		print 'KernelListener.on_stream'
		if stream_name == 'stdout':
			self.std.out.write(data)
		elif stream_name == 'stderr':
			self.std.err.write(data)
		else:
			raise ValueError, 'Unknown stream name {0}'.format(stream_name)




class IPythonKernel (abstract_kernel.AbstractKernel):
	__open_kernels = []


	def __init__(self, kernel_name=None, kernel_path=None):
		self.__kernel = kernel.KernelConnection(kernel_name=kernel_name, kernel_path=kernel_path)

		self.__stdout = sys.stdout
		self.__stderr = sys.stderr

		self.__poll_queued = False

		self.__open_kernels.append(self)


	def close(self):
		self.__kernel.close()
		self.__open_kernels.remove(self)


	@classmethod
	def close_all_open_kernels(cls):
		kernels = cls.__open_kernels[:]
		for k in kernels:
			k.close()



	def new_module(self, name):
		return IPythonModule(self, name)


	def _queue_exec(self, module, code, evaluate_last_expression, result_callback):
		print 'IPythonKernel._queue_exec'
		listener = _KernelListener(result_callback)

		src = CodeGenerator.compileSourceForExecution(code, module.name)
		std = Execution.MultiplexedRichStream(['out', 'err'])
		self.__stdout = std.out
		self.__stderr = std.err
		self.__kernel.execute_request(src, listener=listener)
		self.__queue_poll()


	# def __poll_kernel(self):
	# 	print 'Polling kernel'
	# 	self.__kernel.poll(POLL_TIMEOUT)
	# 	print 'Polled kernel'
	# 	self.__queue_poll()


	def __queue_poll(self):
		# _queue_poll(self.__poll_kernel, unique=True)
		if not self.__poll_queued:
			def _poll():
				self.__poll_queued = False
				self.__kernel.poll(POLL_TIMEOUT)
				self.__queue_poll()

			SwingUtilities.invokeLater(_poll)
			self.__poll_queued = True




__connection_file_paths = []
__ipython_processes = []


def start_ipython_kernel(on_kernel_started, connection_file_path=None):
	# If no connection file path was specified, generate one
	if connection_file_path is None:
		connection_file_path = './kernel_1.json'

		i = 1
		while os.path.exists(connection_file_path):
			i += 1
			connection_file_path = './kernel_{0}.json'.format(i)

	# Spawn the kernel
	proc = subprocess.Popen(['ipython', 'kernel', '-f', connection_file_path])
	__ipython_processes.append(proc)

	# Poll the connection file
	def check_connection_file():
		if os.path.exists(connection_file_path):
			print 'Found connection file {0}'.format(connection_file_path)
			krn = IPythonKernel(kernel_path=connection_file_path)
			__connection_file_paths.append(connection_file_path)
			on_kernel_started(krn)
		else:
			_queue_poll(check_connection_file)

	check_connection_file()




def shutdown():
	print 'Cleanup'
	IPythonKernel.close_all_open_kernels()
	for p in __connection_file_paths:
		print 'Removing connection file {0}'.format(p)
		os.remove(p)
	for proc in __ipython_processes:
		proc.terminate()





_poll_queue = deque()
_queue_handle_invoked = False

def _handle_poll_queue():
	global _queue_handle_invoked

	while len(_poll_queue) > 0:
		fn = _poll_queue.popleft()
		fn()
	_queue_handle_invoked = False



def _queue_poll(fn, unique=False):
	global _queue_handle_invoked

	if not unique  or  fn not in _poll_queue:
		_poll_queue.append(fn)

	if not _queue_handle_invoked:
		SwingUtilities.invokeLater(_handle_poll_queue)
		_queue_handle_invoked = True


