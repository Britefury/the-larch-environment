##-*************************
##-* This software can be used, redistributed and/or modified under
##-* the terms of the BSD 2-clause license as found in the file
##-* 'License.txt' in this distribution.
##-* This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import re

_ansi_escape_pattern = re.compile(r'\x1b\[([0-9,A-Z]{1,2}(;[0-9]{1,2})?(;[0-9]{3})?)?[m|K]?')



class KernelRequestListener (object):
	"""
	A listener passed to request methods of the `KernelConnection` class.

	This listener will receive events back from the kernel as they come.


	Notes about event ordering:

	Events will be received in-order on a specific socket. The order of messages arriving on different
	is arbitrary.
	"""
	def __init__(self, comm_manager=None):
		self._kernel = None
		self._source_msg_id = None
		self._comm_manager = comm_manager


	def detach(self):
		self._kernel._detach_listener(self)


	def on_stream(self, stream_name, data):
		"""
		'stream' message on IOPUB socket

		:param stream_name: the stream name, e.g. stdout, stderr
		:param data: the text written to the stream
		"""
		pass

	def on_display_data(self, source, data, metadata):
		"""
		'display_data' message on IOPUB socket

		:param source: who created the data
		:param data: dictionary mapping MIME type to raw data representation in that format
		:param metadata: metadata describing the content of `data`
		"""
		pass

	def on_input_request(self, prompt, password, reply_callback):
		'''
		'input_request' message on STDIN socket

		:param prompt: the prompt to the user
		:param password: if True, do not echo text back to the user
		:param reply_callback: function of the form f(value) that your code should invoke when input is available to send
		'''
		pass

	def on_status(self, busy):
		"""
		'status' message on IOPUB socket

		:param busy: boolean indicating the busy status of the kernel
		"""
		pass

	def on_comm_open(self, comm, data):
		"""
		'comm_open' message on IOPUB socket

		:param comm: the Comm object that has been opened
		:param data: extra JSON information for initialising the comm
		"""
		if self._comm_manager is not None:
			self._comm_manager.on_comm_open(comm, data)



	#
	# EXECUTE REQUEST EVENTS
	#

	def on_execute_input(self, execution_count, code):
		"""
		'execute_input' message on IOPUB socket

		:param execution_count: the execution count
		:param code: the code that was scheduled for execution
		"""
		pass


	def on_execute_ok(self, execution_count, payload, user_expressions):
		"""
		'execute_reply' message with status='ok' on SHELL socket

		:param execution_count: execution count
		:param payload: list of dicts; see http://ipython.org/ipython-doc/dev/development/messaging.html
		:param user_expressions: reply to user_expressions in execute_request message
		"""
		pass

	def on_execute_error(self, ename, evalue, traceback):
		"""
		'execute_reply' message with status='error' on SHELL socket

		:param ename: exception name
		:param evalue: exception value/message
		:param traceback: traceback as a list of strings
		"""
		pass

	def on_execute_abort(self):
		"""
		'execute_reply' message with status='abort' on SHELL socket
		"""
		pass

	def on_execute_result(self, execution_count, data, metadata):
		"""
		'execute_result' message on IOPUB socket

		:param execution_count: execution count
		:param data: dictionary mapping MIME type to raw data representation in that format
		:param metadata: metadata describing the content of `data`
		"""
		pass

	def on_error(self, ename, evalue, traceback):
		"""
		'error' message on IOPUB socket

		:param ename: exception name
		:param evalue: exception value/message
		:param traceback: traceback as a list of strings
		"""
		pass



	#
	#
	# INSPECT REQUEST EVENTS
	#
	#

	def on_inspect_ok(self, data, metadata):
		"""
		'inspect_reply' message with status='ok' on SHELL socket

		:param data:
		:param metadata:
		"""
		pass

	def on_inspect_error(self, ename, evalue, traceback):
		"""
		'inspect_reply' message with status='error' on SHELL socket

		:param ename: exception name
		:param evalue: exception value/message
		:param traceback: traceback as a list of strings
		"""
		pass



	#
	#
	# COMPLETE REQUEST EVENTS
	#
	#

	def on_complete_ok(self, matches, cursor_start, cursor_end, metadata):
		"""
		'complete_reply' message with status='ok' on SHELL socket

		:param matches:
		:param cursor_start:
		:param cursor_end:
		:param metadata:
		"""
		pass

	def on_complete_error(self, ename, evalue, traceback):
		"""
		'complete_reply' message with status='error' on SHELL socket

		:param ename: exception name
		:param evalue: exception value/message
		:param traceback: traceback as a list of strings
		"""
		pass




class PrintKernelRequestListenerMixin (KernelRequestListener):
	def __init__(self, name, comm_manager=None):
		super(PrintKernelRequestListenerMixin, self).__init__(comm_manager)
		self.name = name


	def on_stream(self, stream_name, data):
		print '[{0}]: stream: stream_name={1}, data={2}'.format(self.name, stream_name, data)

	def on_display_data(self, source, data, metadata):
		print '[{0}]: display_data: source={1}, data={2}, metadata={3}'.format(self.name, source, data, metadata)

	def on_status(self, busy):
		print '[{0}]: status: busy={1}'.format(self.name, busy)

	def on_input_request(self, prompt, password, reply_callback):
		print '[{0}]: input_request: prompt={1}, password={2}'.format(self.name, prompt, password)
		data = raw_input(prompt)
		reply_callback(data)

	def on_comm_open(self, comm, data):
		super(PrintKernelRequestListenerMixin, self).on_comm_open(comm, data)
		print '[{0}]: on_comm_open'.format(self.name)



	def on_execute_input(self, execution_count, code):
		print '[{0}]: execute_input: execution_count={1}, code={2}'.format(self.name, execution_count, code)

	def on_execute_ok(self, execution_count, payload, user_expressions):
		print '[{0}]: execute_reply OK: execution_count={1}, payload={2}, user_expressions={3}'.format(self.name, execution_count, payload, user_expressions)

	def on_execute_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		print '[{0}]: execute_reply ERROR: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)

	def on_execute_abort(self):
		print '[{0}]: execute_reply ABORT'.format(self.name)

	def on_execute_result(self, execution_count, data, metadata):
		print '[{0}]: execute_result: execution_count={1}, data={2}, metadata={3}'.format(self.name, execution_count, data, metadata)

	def on_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		print '[{0}]: error: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)


	def on_inspect_ok(self, data, metadata):
		print '[{0}]: inspect_reply OK: data={1}, metadata={2}'.format(self.name, data, metadata)

	def on_inspect_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		print '[{0}]: inspect_reply ERROR: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)



	def on_complete_ok(self, matches, cursor_start, cursor_end, metadata):
		print '[{0}]: complete_reply OK: matches={1}, cursor_start={2}, cursor_end={3}, metadata={4}'.format(self.name, matches,
														     cursor_start, cursor_end, metadata)

	def on_complete_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		print '[{0}]: complete_reply ERROR: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)



def krn_event(name, **kwargs):
	return dict(event_name=name, **kwargs)


class EventLogKernelRequestListener (KernelRequestListener):
	def __init__(self, on_input_callback, comm_manager=None):
		super(EventLogKernelRequestListener, self).__init__(comm_manager)
		self.events = []
		self.__on_input_callback = on_input_callback


	def clear(self):
		self.events = []


	def on_stream(self, stream_name, data):
		self.events.append(krn_event('on_stream', stream_name=stream_name, data=data))

	def on_display_data(self, source, data, metadata):
		self.events.append(krn_event('on_display_data', source=source, data=data, metadata=metadata))

	def on_status(self, busy):
		self.events.append(krn_event('on_status', busy=busy))

	def on_input_request(self, prompt, password, reply_callback):
		self.events.append(krn_event('on_input_request', prompt=prompt, password=password))
		data = self.__on_input_callback(prompt)
		reply_callback(data)

	def on_request_finished(self):
		self.events.append(krn_event('on_request_finished'))

	def on_comm_open(self, comm, data):
		super(EventLogKernelRequestListener, self).on_comm_open(comm, data)
		self.events.append(krn_event('on_comm_open'))



	def on_execute_input(self, execution_count, code):
		self.events.append(krn_event('on_execute_input', execution_count=execution_count, code=code))

	def on_execute_ok(self, execution_count, payload, user_expressions):
		self.events.append(krn_event('on_execute_ok', execution_count=execution_count, payload=payload, user_expressions=user_expressions))

	def on_execute_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		self.events.append(krn_event('on_execute_error', ename=ename, evalue=evalue, traceback=traceback))

	def on_execute_abort(self):
		self.events.append(krn_event('on_execute_abort'))

	def on_execute_result(self, execution_count, data, metadata):
		self.events.append(krn_event('on_execute_result', execution_count=execution_count, data=data, metadata=metadata))

	def on_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		self.events.append(krn_event('on_error', ename=ename, evalue=evalue, traceback=traceback))



	def on_inspect_ok(self, data, metadata):
		self.events.append(krn_event('on_inspect_ok', data=data, metadata=metadata))

	def on_inspect_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		self.events.append(krn_event('on_inspect_error', ename=ename, evalue=evalue, traceback=traceback))



	def on_complete_ok(self, matches, cursor_start, cursor_end, metadata):
		self.events.append(krn_event('on_complete_ok', matches=matches, cursor_start=cursor_start, cursor_end=cursor_end, metadata=metadata))

	def on_complete_error(self, ename, evalue, traceback):
		traceback = [_ansi_escape_pattern.sub('', x)   for x in traceback]
		self.events.append(krn_event('on_complete_error', ename=ename, evalue=evalue, traceback=traceback))





