##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import sys, ast, json, binascii, re
from java.awt import Color, Font
from java.io import ByteArrayInputStream
from javax.swing import Timer
from javax.imageio import ImageIO

from org.python.core.util import StringUtil

from mipy import kernel, request_listener, comm

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Primitive, Label, Column, Image, Blank
from BritefuryJ.Pres.RichText import NormalText, RichText
from BritefuryJ.Pres.ObjectPres import ErrorBoxWithFields, HorizontalField, VerticalField
from BritefuryJ.Controls import IntSlider
from BritefuryJ.Graphics import SolidBorder
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue

from .. import execution_result, execution_pres
from . import python_kernel, module_finder
from LarchCore.Languages.Python2 import CodeGenerator
from LarchCore.ipython import widgets


_aborted_border = SolidBorder(1.5, 2.0, 5.0, 5.0, Color(1.0, 0.5, 0.0), Color(1.0, 1.0, 0.9))
_aborted_style = StyleSheet.style(Primitive.foreground(Color(1.0, 0.0, 0.0)))
_aborted = _aborted_border.surround(_aborted_style.applyTo(Label('ABORTED')))

_text_result_style = StyleSheet.style(RichText.normalTextAttrs(StyleSheet.style(Primitive.fontFace(Font.MONOSPACED),
										Primitive.editable(False))))

# ANSI escape sequence pattern from http://stackoverflow.com/questions/13506033/filtering-out-ansi-escape-sequences
_ansi_escape_pattern = re.compile(r'\x1b\[([0-9,A-Z]{1,2}(;[0-9]{1,2})?(;[0-9]{3})?)?[m|K]?')

POLL_TIMEOUT = 0


class IPythonLiveModule (python_kernel.AbstractPythonLiveModule):
	def __init__(self, kernel, name):
		super(IPythonLiveModule, self).__init__(kernel)
		self.name = name

	def evaluate(self, code, result):
		result.clear()
		self._kernel._queue_eval(self.name, code, result)

	def execute(self, code, evaluate_last_expression, result):
		result.clear()
		self._kernel._queue_exec(self.name, code, evaluate_last_expression, result)


class _KernelListener (request_listener.KernelRequestListener):
	def __init__(self, comm_manager, result):
		super(_KernelListener, self).__init__(comm_manager)
		self.result = result
		self.std = self.result.streams


	def on_execute_ok(self, execution_count, payload, user_expressions):
		self.result.execution_count = execution_count

	def on_execute_error(self, ename, evalue, traceback):
		# print 'KernelListener.on_execute_error'
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		self.result.set_error(IPythonExecutionError(ename, evalue, traceback))

	def on_execute_abort(self):
		# print 'KernelListener.on_execute_abort'
		self.result.notify_aborted()


	def on_execute_result(self, execution_count, data, metadata):
		# print 'KernelListener.on_execute_result'
		self.result.execution_count = execution_count
		text = data.pop('text/plain', None)
		if text is not None:
			try:
				value = ast.literal_eval(text)
			except:
				# Not a literal
				self.result.set_text_result(text)
			else:
				self.result.set_result(value)
		if len(data) > 0:
			print 'ipyton_kernel._KernelListener.on_execute_result: data keys: {0}'.format(sorted(list(data.keys())))


	def on_stream(self, stream_name, data):
		# print 'KernelListener.on_stream'
		if stream_name == 'stdout':
			self.std.stdout.write(data)
		elif stream_name == 'stderr':
			self.std.stderr.write(data)
		else:
			raise ValueError, 'Unknown stream name {0}'.format(stream_name)


	def on_display_data(self, source, data, metadata):
		"""
		'display_data' message on IOPUB socket

		:param source: who created the data
		:param data: dictionary mapping MIME type to raw data representation in that format
		:param metadata: metadata describing the content of `data`
		"""
		for mime_type, raw in data.items():
			if mime_type.startswith('image/'):
				image = binascii.a2b_base64(raw)
				self.result.display_image(mime_type, image)
		print 'ipython_kernel._KernelListener.on_display_data: data keys: {0}'.format(sorted(list(data.keys())))





class IPythonKernel (python_kernel.AbstractPythonKernel):
	def __init__(self, ctx, kernel_process):
		super(IPythonKernel, self).__init__(ctx)
		self.__krn_proc = kernel_process
		self.__kernel = kernel_process.connection

		self.__stdout = sys.stdout
		self.__stderr = sys.stderr

		# Setup module finder
		self.__loader_module_name = module_finder.loader_module_name()

		self.__install_loader()
		self.__matplotlib_inline()

		self.__live_module = IPythonLiveModule(self, '__live__')



	def shutdown(self):
		self.__krn_proc.close()
		super(IPythonKernel, self).shutdown()



	def new_execution_result(self):
		return IPythonExecutionResult()

	def get_live_module(self):
		return self.__live_module


	def set_module_source(self, fullname, source):
		sources = []
		if isinstance(source, str)  or  isinstance(source, unicode):
			sources.append(source)
		elif isinstance(source, list):
			for x in source:
				if isinstance(x, str)  or  isinstance(x, unicode):
					sources.append(x)
				else:
					code = CodeGenerator.compileSourceForExecution(x, fullname)
					sources.append(code)
		else:
			raise TypeError, 'source should be a str, unicode or a list, it is a {0}'.format(type(source))

		src = '\n'.join(sources)
		self.__loader_set_module_source(fullname, src)

	def remove_module(self, fullname):
		self.__loader_remove_module(fullname)

	def is_in_process(self):
		return False



	def _queue_exec(self, module_name, code, evaluate_last_expression, result, silent=False, store_history=True):
		# print 'IPythonKernel._queue_exec'
		listener = result._listener   if result is not None   else None

		if isinstance(code, str)  or  isinstance(code, unicode):
			src = code
		else:
			src = CodeGenerator.compileSourceForExecution(code, module_name)
		std = execution_result.MultiplexedRichStream()
		self.__stdout = std.stdout
		self.__stderr = std.stderr
		self.__kernel.execute_request(src, listener=listener, silent=silent, store_history=store_history)
		self.__queue_poll()


	def _queue_eval(self, module_name, expr, result):
		# print 'IPythonKernel._queue_exec'
		listener = result._listener   if result is not None   else None

		if isinstance(expr, str)  or  isinstance(expr, unicode):
			src = expr
		else:
			src = CodeGenerator.compileSourceForEvaluation(expr, module_name)
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
			self._ctx._timer_enqueue(self.__poll_kernel, unique=True)

	def is_in_process(self):
		return False


	def __module_loader_exec(self, src):
		self._queue_exec(self.__loader_module_name, src, False, None, store_history=False)


	def __install_loader(self):
		src = module_finder.install_loader_src(self.__loader_module_name)
		self.__module_loader_exec(src)

	def __uninstall_loader(self):
		src = module_finder.uninstall_loader_src(self.__loader_module_name)
		self.__module_loader_exec(src)

	def __loader_set_module_source(self, module_name, module_src):
		src = module_finder.loader_set_module_source_src(self.__loader_module_name, module_name, module_src)
		self.__module_loader_exec(src)

	def __loader_remove_module(self, module_name):
		src = module_finder.loader_remove_module_src(self.__loader_module_name, module_name)
		self.__module_loader_exec(src)

	def __loader_unload_all_modules(self):
		src = module_finder.loader_unload_all_modules_src(self.__loader_module_name)
		self.__module_loader_exec(src)


	def __matplotlib_inline(self):
		self.__module_loader_exec('%matplotlib inline')




class IPythonContext (python_kernel.AbstractPythonContext):
	def __init__(self):
		self.__kernels = []
		self.__timer = None
		self.__timer_queue = []


	def _notify_kernel_shutdown(self, kernel):
		self.__kernels.remove(kernel)

	def close(self):
		"""
		Shutdown
		"""
		krns = self.__kernels[:]
		for krn in krns:
			krn.shutdown()
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

		# Code to get the kernel description
		code = 'import sys, platform, json\n' + \
			'json.dumps([platform.python_implementation(), platform.python_version_tuple(), sys.version])\n'
		# Code to cause the kernel to exit
		exit_code = 'exit()'

		def _on_kernel_started(krn):
			def on_result(result):
				result.result_finished()
				kernel_information = json.loads(result.result)
				kernel_description_callback(kernel_information)

			mod = krn.get_live_module()

			result = krn.new_execution_result()
			result.result_callback = on_result
			mod.evaluate(code, result)

			exit_result = krn.new_execution_result()
			mod.evaluate(exit_code, exit_result)

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
	def __init__(self, streams=None, comm_manager=None):
		super( IPythonExecutionResult, self ).__init__(streams)

		if comm_manager is None:
			comm_manager = comm.CommManager(self.__default_comm_handler)
			comm_manager.register_comm_open_handler('WidgetModel', self.__handle_comm_WidgetModel)

		self.__comm_manager = comm_manager
		self._listener = _KernelListener(comm_manager, self)
		self._error = None
		self._result = None
		self._aborted = False
		self._images = []
		self._execution_count = None
		self._widgets = []
		self._visible_widgets = []


	def __default_comm_handler(self, comm, data):
		print 'IPythonExecutionResult.__default_comm_handler: {0}'.format(comm.target_name)

	def __handle_comm_WidgetModel(self, comm, data):
		widget = widgets.IPythonWidgetModel(self, comm, data)
		self._widgets.append(widget)


	def _display_widget(self, widget):
		self._visible_widgets.append(widget)
		self._incr.onChanged()


	def result_finished(self):
		self._listener.detach()


	def clear(self):
		super(IPythonExecutionResult, self).clear()

		for widget in self._widgets:
			widget.close()
		self._widgets = []
		self._visible_widgets = []

		self._error = None
		self._result = None
		self._aborted = False
		self._images = []
		self._execution_count = None
		self._incr.onChanged()
		self._widgets = []


	@property
	def streams(self):
		self._incr.onAccess()
		return self._streams


	@property
	def caught_exception(self):
		self._incr.onAccess()
		return self._error

	def set_error(self, error):
		self._error = error
		self._incr.onChanged()

	def has_errors(self):
		self._incr.onAccess()
		return self._error is not None  or  self._streams.has_content_for( 'err' )


	def has_result(self):
		self._incr.onAccess()
		return self._result is not None

	@property
	def result(self):
		self._incr.onAccess()
		return self._result[0]   if self._result is not None   else None

	def set_result(self, result):
		self._result = (result,)
		self._incr.onChanged()
		super(IPythonExecutionResult, self).set_result(result)

	def set_text_result(self, text_result):
		lines = text_result.split('\n')
		res = _text_result_style.applyTo(Column([NormalText(line)   for line in lines]))
		self.set_result(res)


	def display_image(self, mime_type, image_data):
		image_bytes = StringUtil.toBytes(image_data)
		in_stream = ByteArrayInputStream(image_bytes)
		img = ImageIO.read(in_stream)
		if img is not None:
			self._images.append(img)
			self._incr.onChanged()
		else:
			print 'display_image: failed to read image of mime type {0} with {1} bytes'.format(mime_type, len(image_data))
			print image_data


	def was_aborted(self):
		self._incr.onAccess()
		return self._aborted

	def notify_aborted(self):
		self._aborted = True
		self._incr.onChanged()


	@property
	def execution_count(self):
		self._incr.onAccess()
		return self._execution_count

	@execution_count.setter
	def execution_count(self, value):
		self._execution_count = value
		self._incr.onChanged()


	def errorsOnly(self):
		res = IPythonExecutionResult( self.__comm_manager, self._streams.suppress_stream( 'out' ) )
		if self._error is not None:
			res.set_error(self._error)
		return res



	def _result_view(self):
		contents = []

		# Images
		for img in self._images:
			contents.append(Image(img, float(img.getWidth()), float(img.getHeight())))

		# Widgets
		for widget in self._visible_widgets:
			contents.append(Pres.coerce(widget))

		# Result value
		if self._aborted:
			contents.append(self._aborted)
		else:
			if self._result is not None:
				contents.append(Pres.coercePresentingNull( self._result[0] ).alignHPack())

		if len(contents) == 0:
			return None
		else:
			return (Column(contents), )



	def view(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		self._incr.onAccess()
		result = self._result_view()
		return execution_pres.execution_result_box( self._streams, self._error, result, bUseDefaultPerspecitveForException,
							    bUseDefaultPerspectiveForResult, self._execution_count )


	def minimalView(self, bUseDefaultPerspecitveForException=True, bUseDefaultPerspectiveForResult=True):
		self._incr.onAccess()
		result = self._result_view()
		return execution_pres.minimal_execution_result_box( self._streams, self._error, result, bUseDefaultPerspecitveForException,
								    bUseDefaultPerspectiveForResult )


