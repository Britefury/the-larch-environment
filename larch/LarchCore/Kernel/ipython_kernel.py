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

from java.awt import Color
from javax.swing import Timer

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


POLL_TIMEOUT = 0


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
		# print 'KernelListener.on_execute_ok'
		self.finished(self.result)

	def on_execute_error(self, ename, evalue, traceback):
		# print 'KernelListener.on_execute_error'
		tb = Column([Label(x.decode('utf8'))   for x in traceback])
		fields = []
		fields.append(HorizontalField('Exception name:', Label(ename)))
		fields.append(HorizontalField('Exception value:', Label(evalue)))
		fields.append(VerticalField('Traceback:', tb))
		self.result.caught_exception = ErrorBoxWithFields('EXCEPTION IN IPYTHON KERNEL', fields)
		self.finished(self.result)

	def on_execute_abort(self):
		# print 'KernelListener.on_execute_abort'
		self.result.result = [_aborted]
		self.finished(self.result)


	def on_execute_result(self, execution_count, data, metadata):
		# print 'KernelListener.on_execute_result'
		text = data.get('text/plain')
		if text is not None:
			self.result.result = [text]


	def on_stream(self, stream_name, data):
		# print 'KernelListener.on_stream'
		if stream_name == 'stdout':
			self.std.out.write(data)
		elif stream_name == 'stderr':
			self.std.err.write(data)
		else:
			raise ValueError, 'Unknown stream name {0}'.format(stream_name)




class IPythonKernel (abstract_kernel.AbstractKernel):
	def __init__(self, ctx, kernel_process):
		self.__krn_proc = kernel_process
		self.__kernel = kernel_process.connection

		self.__stdout = sys.stdout
		self.__stderr = sys.stderr

		self.__ctx = ctx


	def close(self):
		self.__krn_proc.close()
		self.__ctx._notify_closed(self)



	def new_module(self, name):
		return IPythonModule(self, name)


	def _queue_exec(self, module, code, evaluate_last_expression, result_callback):
		# print 'IPythonKernel._queue_exec'
		listener = _KernelListener(result_callback)

		src = CodeGenerator.compileSourceForExecution(code, module.name)
		std = Execution.MultiplexedRichStream(['out', 'err'])
		self.__stdout = std.out
		self.__stderr = std.err
		self.__kernel.execute_request(src, listener=listener)
		self.__queue_poll()


	def __poll_kernel(self):
		work_done = self.__kernel.poll(POLL_TIMEOUT)
		self.__queue_poll()
		return work_done


	def __queue_poll(self):
		if self.__kernel.is_open():
			self.__ctx._timer_enqueue(self.__poll_kernel, unique=True)



class IPythonContext (object):
	def __init__(self):
		self.__kernels = []
		self.__timer = None
		self.__timer_queue = []


	def _notify_closed(self, kernel):
		self.__kernels.remove(kernel)

	def close(self):
		krns = self.__kernels[:]
		for krn in krns:
			krn.close()
		if self.__timer is not None:
			self.__timer.stop()


	def start_kernel(self, on_kernel_started, connection_file_path=None):
		krn_proc = kernel.IPythonKernelProcess(connection_file_path=connection_file_path)

		# Poll the connection
		def check_connection():
			if krn_proc.connection is not None:
				# print 'Kernel started'

				krn = IPythonKernel(self, krn_proc)
				self.__kernels.append(krn)

				on_kernel_started(krn)
				return True
			else:
				self._timer_enqueue(check_connection)
				return False

		check_connection()


	def __handle_timer_queue(self):
		work_done = False
		queue_contents = self.__timer_queue[:]
		del self.__timer_queue[:]
		for fn in queue_contents:
			if fn():
				work_done = True
		if work_done:
			self.__timer.setDelay(10)
		else:
			self.__timer.setDelay(100)



	def _timer_enqueue(self, fn, unique=False):
		def action(event):
			self.__handle_timer_queue()

		if self.__timer is None:
			self.__timer = Timer(100, action)
			self.__timer.setInitialDelay(100)
			self.__timer.start()

		if not unique  or  fn not in self.__timer_queue:
			self.__timer_queue.append(fn)









