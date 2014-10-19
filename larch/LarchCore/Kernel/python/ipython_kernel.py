##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import sys, ast, json
from java.awt import Color
from javax.swing import Timer

from mipy import kernel, request_listener

from BritefuryJ.Pres.Primitive import Primitive, Label, Column
from BritefuryJ.Pres.ObjectPres import ErrorBoxWithFields, HorizontalField, VerticalField
from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Incremental import IncrementalValueMonitor

from .. import execution_result, execution_pres
from . import python_kernel
from LarchCore.Languages.Python2 import CodeGenerator


_aborted_border = SolidBorder(1.5, 2.0, 5.0, 5.0, Color(1.0, 0.5, 0.0), Color(1.0, 1.0, 0.9))
_aborted_style = StyleSheet.style(Primitive.foreground(Color(1.0, 0.0, 0.0)))
_aborted = _aborted_border.surround(_aborted_style.applyTo(Label('ABORTED')))


POLL_TIMEOUT = 0


class IPythonLiveModule (python_kernel.AbstractPythonLiveModule):
	def __init__(self, kernel, name):
		self.__kernel = kernel
		self.name = name

	def evaluate(self, code, result_callback):
		self.__kernel._queue_eval(self, code, result_callback)

	def execute(self, code, evaluate_last_expression, result_callback):
		self.__kernel._queue_exec(self, code, evaluate_last_expression, result_callback)


class _KernelListener (request_listener.ExecuteRequestListener):
	def __init__(self, finished):
		super(_KernelListener, self).__init__()
		self.result = IPythonExecutionResult()
		self.std = self.result.streams
		self.finished = finished


	def on_execute_ok(self, execution_count, payload, user_expressions):
		pass

	def on_execute_error(self, ename, evalue, traceback):
		# print 'KernelListener.on_execute_error'
		self.result.set_error(IPythonExecutionError(ename, evalue, traceback))

	def on_execute_abort(self):
		# print 'KernelListener.on_execute_abort'
		self.result.notify_aborted()


	def on_execute_result(self, execution_count, data, metadata):
		# print 'KernelListener.on_execute_result'
		text = data.get('text/plain')
		if text is not None:
			self.result.set_result(ast.literal_eval(text))


	def on_stream(self, stream_name, data):
		# print 'KernelListener.on_stream'
		if stream_name == 'stdout':
			self.std.stdout.write(data)
		elif stream_name == 'stderr':
			self.std.stderr.write(data)
		else:
			raise ValueError, 'Unknown stream name {0}'.format(stream_name)


	def on_execute_finished(self):
		self.finished(self.result)




class IPythonKernel (python_kernel.AbstractPythonKernel):
	def __init__(self, ctx, kernel_process):
		super(IPythonKernel, self).__init__()
		self.__krn_proc = kernel_process
		self.__kernel = kernel_process.connection

		self.__stdout = sys.stdout
		self.__stderr = sys.stderr

		self.__ctx = ctx


	def _shutdown(self):
		self.__krn_proc.close()
		self.__ctx._notify_closed(self)



	def new_live_module(self, full_name):
		return IPythonLiveModule(self, full_name)


	def _queue_exec(self, module, code, evaluate_last_expression, result_callback):
		# print 'IPythonKernel._queue_exec'
		listener = _KernelListener(result_callback)

		if isinstance(code, str)  or  isinstance(code, unicode):
			src = code
		else:
			src = CodeGenerator.compileSourceForExecution(code, module.name)
		std = execution_result.MultiplexedRichStream()
		self.__stdout = std.stdout
		self.__stderr = std.stderr
		self.__kernel.execute_request(src, listener=listener)
		self.__queue_poll()


	def _queue_eval(self, module, expr, result_callback):
		# print 'IPythonKernel._queue_exec'
		listener = _KernelListener(result_callback)

		if isinstance(expr, str)  or  isinstance(expr, unicode):
			src = expr
		else:
			src = CodeGenerator.compileForEvaluation(expr, module.name)
		std = execution_result.MultiplexedRichStream()
		self.__stdout = std.stdout
		self.__stderr = std.stderr
		self.__kernel.execute_request(src, listener=listener)
		self.__queue_poll()


	def __poll_kernel(self):
		work_done = self.__kernel.poll(POLL_TIMEOUT)
		self.__queue_poll()
		return work_done


	def __queue_poll(self):
		if self.__kernel.is_open():
			self.__ctx._timer_enqueue(self.__poll_kernel, unique=True)

	def is_in_process(self):
		return False



class IPythonContext (python_kernel.AbstractPythonContext):
	def __init__(self):
		self.__kernels = []
		self.__timer = None
		self.__timer_queue = []


	def _notify_closed(self, kernel):
		self.__kernels.remove(kernel)

	def close(self):
		"""
		Shutdown
		"""
		krns = self.__kernels[:]
		for krn in krns:
			krn._shutdown()
		if self.__timer is not None:
			self.__timer.stop()


	def start_kernel(self, on_kernel_started, ipython_path=None, connection_file_path=None):
		"""
		Start an IPython kernel

		:param on_kernel_started: kernel started callback fn(kernel)
		:param connection_file_path: [optional] path where the temporary connection file should be stored
		:param python_path: the python distribution from where the bin subdirectory contains the IPython executable
		"""
		if ipython_path is None:
			ipython_path = 'ipython'

		krn_proc = kernel.IPythonKernelProcess(ipython_path=ipython_path, connection_file_path=connection_file_path)

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


	def get_kernel_description(self, kernel_description_callback, ipython_path=None):
		"""
		Get a kernel description
		"""

		code = 'import sys, platform, json\n' + \
			'json.dumps([platform.python_implementation(), platform.python_version_tuple(), sys.version])\n'

		def _on_kernel_started(krn):
			def on_result(result):
				krn._shutdown()
				kernel_information = json.loads(result.result)
				kernel_description_callback(kernel_information)

			mod = krn.new_live_module('test')
			mod.evaluate(code, on_result)

		self.start_kernel(_on_kernel_started, ipython_path=ipython_path)




class IPythonExecutionError (object):
	def __init__(self, ename, evalue, traceback):
		self.ename = ename
		self.evalue = evalue
		self.traceback = traceback


	def __present__(self, fragment, inh):
		tb = Column([Label(x.decode('utf8'))   for x in self.traceback])
		fields = []
		fields.append(HorizontalField('Error:', Label(self.ename)))
		fields.append(HorizontalField('Value:', Label(self.evalue)))
		fields.append(VerticalField('Traceback:', tb))
		return ErrorBoxWithFields('ERROR IN IPYTHON KERNEL', fields)



class IPythonExecutionResult (execution_result.AbstractExecutionResult):
	def __init__(self, streams=None, caughtException=None, result_in_tuple=None):
		super( IPythonExecutionResult, self ).__init__(streams)
		self.__incr = IncrementalValueMonitor()
		self._error = None
		self._result = result_in_tuple
		self._aborted = False


	@property
	def streams(self):
		return self._streams


	@property
	def caught_exception(self):
		return self._error

	def set_error(self, error):
		self._error = error
		self.__incr.onChanged()


	def has_result(self):
		return self._result is not None

	@property
	def result(self):
		return self._result[0]   if self._result is not None   else None

	def set_result(self, result):
		self._result = (result,)
		self.__incr.onChanged()


	def was_aborted(self):
		return self._aborted

	def notify_aborted(self):
		self._aborted = True
		self.__incr.onChanged()


	def errorsOnly(self):
		return IPythonExecutionResult( self._streams.suppress_stream( 'out' ), self._error, None )



	def hasErrors(self):
		return self._error is not None  or  self._streams.has_content_for( 'err' )


	def view(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		self.__incr.onAccess()
		result = _aborted   if self._aborted   else self._result
		return execution_pres.execution_result_box( self._streams, self._error, result, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )


	def minimalView(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		self.__incr.onAccess()
		result = _aborted   if self._aborted   else self._result
		return execution_pres.minimal_execution_result_box( self._streams, self._error, result, bUseDefaultPerspecitveForException, bUseDefaultPerspectiveForResult )


