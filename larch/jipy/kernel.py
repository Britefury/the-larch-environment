##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
import os, sys, json, hmac, uuid, datetime, hashlib

from org.zeromq import ZMQ
from org.python.core.util import StringUtil


def load_connection_file(kernel_name):
	p = os.path.expanduser(os.path.join('~', '.ipython', 'profile_default', 'security',
					    'kernel-{0}.json'.format(kernel_name)))

	if os.path.exists(p):
		with open(p, 'r') as f:
			return json.load(f)
	else:
		raise ValueError, 'Could not find connection file for kernel {0}'.format(kernel_name)


DELIM = StringUtil.toBytes("<IDS|MSG>")
KERNEL_PROTOCOL_VERSION = b'5.0'


def _unpack_ident(ident):
	return [StringUtil.fromBytes(x) for x in ident]


class _MessageRouter(object):
	'''
	    Message router

	    Takes an incoming message and invokes a corresponding handler method on the attached object
	    '''

	def __init__(self, instance, socket_name):
		'''
		Message router constructor

		:param instance: the object on which handler methods can be found
		:param socket_name: the name of the socket that the router receives messages from
		'''
		self.__handler_method_cache = {}
		self.__instance = instance
		self.__socket_name = socket_name


	def handle(self, idents, msg):
		'''
		Handle a message

		Will look for a handler method on the attached instance (given as an argument to the constructor).
		Will look for a method called _handle_msg_<socket_name>_<msg_type>.
		Handler methods should be of the form:
		def _handle_msg_iopub_status(self, idents, msg)

		The message router for the 'iopub' socket will router messages whose msg_type is 'status' to
		the above method.

		:param idents: the ZeroMQ idents
		:param msg: the message to route
		'''

		msg_type = msg['msg_type']
		try:
			bound_method = self.__handler_method_cache[msg_type]
		except KeyError:
			method_name = '_handle_msg_{0}_{1}'.format(self.__socket_name, msg_type)
			try:
				bound_method = getattr(self.__instance, method_name)
			except AttributeError:
				bound_method = None
			self.__handler_method_cache[msg_type] = bound_method

		if bound_method is not None:
			return bound_method(idents, msg)
		else:
			print 'WARNING: socket {0} did not handle message of type {1} with ident {2}'.format(
				self.__socket_name, msg_type, idents)


class Comm(object):
	def __init__(self, kernel_connection, comm_id, target_name):
		self.__kernel = kernel_connection
		self.comm_id = comm_id
		self.target_name = target_name

		self.on_message = None
		self.on_closed_remotely = None


	def message(self, data):
		kernel = self.__kernel
		kernel.session.send(kernel.shell, 'comm_msg', {
			'comm_id': self.comm_id,
			'data': data
		})

	def close(self, data):
		kernel = self.__kernel
		kernel.session.send(kernel.shell, 'comm_close', {
			'comm_id': self.comm_id,
			'data': data
		})
		kernel._notify_comm_closed(self)



def _get_parent_msg_id(msg):
	return msg['parent_header']['msg_id']


class KernelRequestListener (object):
	def __init__(self):
		self.__refcount = 0


	def ref(self, n=1):
		self.__refcount += n

	def unref(self, n=1):
		self.__refcount -= n
		return self.__refcount <= 0


	def on_stream(self, stream_name, data):
		"""
		'stream' message on IOPUB socket

		:param stream_name: the stream name, e.g. stdout, stderr
		:param data: the text written to the stream
		:return:
		"""
		pass

	def on_display_data(self, source, data, metadata):
		"""
		'display_data' message on IOPUB socket

		:param source:
		:param data:
		:param metadata:
		:return:
		"""
		pass

	def on_status(self, busy):
		pass

	def on_execute_input(self, execution_count, code):
		pass

	def on_input_request(self, prompt, password, reply_callback):
		'''
		'input_request' message on STDIN socket

		:param prompt: the prompt to the user
		:param password: if True, do not echo text back to the user
		:param reply_callback: function of the form f(value) that your code should invoke when input is available to send
		:return:
		'''
		pass


	def on_execute_ok(self, execution_count, payload, user_expressions):
		pass

	def on_execute_error(self, ename, evalue, traceback):
		pass

	def on_execute_abort(self):
		pass

	def on_execute_result(self, execution_count, data, metadata):
		pass

	def on_error(self, ename, evalue, traceback):
		pass


	def on_inspect_ok(self, data, metadata):
		pass

	def on_inspect_error(self, ename, evalue, traceback):
		pass


	def on_complete_ok(self, matches, cursor_start, cursor_end, metadata):
		pass

	def on_complete_error(self, ename, evalue, traceback):
		pass



class DebugKernelRequestListener (KernelRequestListener):
	def __init__(self, name, stdin_callback=None):
		super(DebugKernelRequestListener, self).__init__()
		self.name = name


	def on_stream(self, stream_name, data):
		print '[{0}]: stream: stream_name={1}, data={2}'.format(self.name, stream_name, data)

	def on_display_data(self, source, data, metadata):
		print '[{0}]: display_data: source={1}, data={2}, metadata={3}'.format(self.name, source, data, metadata)

	def on_status(self, busy):
		print '[{0}]: status: busy={1}'.format(self.name, busy)

	def on_execute_input(self, execution_count, code):
		print '[{0}]: execute_input: execution_count={1}, code={2}'.format(self.name, execution_count, code)

	def on_input_request(self, prompt, password, reply_callback):
		print '[{0}]: input_request: prompt={1}, password={2}'.format(self.name, prompt, password)
		data = raw_input(prompt)
		reply_callback(data)


	def on_execute_ok(self, execution_count, payload, user_expressions):
		print '[{0}]: execute_reply OK: execution_count={1}, payload={2}, user_expressions={3}'.format(self.name, execution_count, payload, user_expressions)

	def on_execute_error(self, ename, evalue, traceback):
		print '[{0}]: execute_reply ERROR: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)

	def on_execute_abort(self):
		print '[{0}]: execute_reply ABORT'.format(self.name)

	def on_execute_result(self, execution_count, data, metadata):
		print '[{0}]: execute_result: execution_count={1}, data={2}, metadata={3}'.format(self.name, execution_count, data, metadata)

	def on_error(self, ename, evalue, traceback):
		print '[{0}]: error: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)


	def on_inspect_ok(self, data, metadata):
		print '[{0}]: inspect_reply OK: data={1}, metadata={2}'.format(self.name, data, metadata)

	def on_inspect_error(self, ename, evalue, traceback):
		print '[{0}]: inspect_reply ERROR: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)


	def on_complete_ok(self, matches, cursor_start, cursor_end, metadata):
		print '[{0}]: complete_reply OK: matches={1}, cursor_start={2}, cursor_end={3}, metadata={4}'.format(self.name, matches,
														     cursor_start, cursor_end, metadata)

	def on_complete_error(self, ename, evalue, traceback):
		print '[{0}]: complete_reply ERROR: ename={1}, evalue={2}, traceback={3}'.format(self.name, ename, evalue, traceback)






class KernelConnection(object):
	'''
	    An IPython kernel connection

	    Handling events

	    Requests that elicit a reply - e.g. execute_request - accept callbacks as parameters. Replies will be
	    handled by these callbacks

	    Events that are not replies to request have associated callback attributes:
	    on_stream: 'stream' message on IOPUB socket; f(stream_name, data)
	    on_display_data: 'display_data' message on IOPUB socket; f(source, data, metadata)
	    on_status: 'status' message on IOPUB socket; f(busy)
	    on_execute_input: 'execute_input' message on IOPUB socket; f(execution_count, code)
	    on_clear_output: 'clear_output' message on IOPUB socket; f(wait)
	    on_comm_open: 'comm_open' message on IOPUB; f(comm, data); comm is Comm instance
	    '''

	def __init__(self, kernel_name, username=''):
		'''
		IPython kernel connection constructor

		:param kernel_name: kernel name used to identify connection file
		:param username: username
		:return:
		'''
		# Load the connection file and find out where we have to connect to
		connection = load_connection_file(kernel_name)

		key = connection['key'].encode('utf8')
		transport = connection['transport']
		address = connection['ip']

		shell_port = connection['shell_port']
		iopub_port = connection['iopub_port']
		stdin_port = connection['stdin_port']
		control_port = connection['control_port']

		# JeroMQ context
		self.__ctx = ZMQ.context(1)

		# Create the four IPython sockets; SHELL, IOPUB, STDIN and CONTROL
		self.shell = self.__ctx.socket(ZMQ.DEALER)
		self.iopub = self.__ctx.socket(ZMQ.SUB)
		self.stdin = self.__ctx.socket(ZMQ.DEALER)
		self.control = self.__ctx.socket(ZMQ.DEALER)
		# Connect
		self.shell.connect('{0}://{1}:{2}'.format(transport, address, shell_port))
		self.iopub.connect('{0}://{1}:{2}'.format(transport, address, iopub_port))
		self.stdin.connect('{0}://{1}:{2}'.format(transport, address, stdin_port))
		self.control.connect('{0}://{1}:{2}'.format(transport, address, control_port))
		# Subscribe IOPUB to everything
		self.iopub.subscribe(StringUtil.toBytes(''))

		# Create a poller to monitor the four sockets for incoming messages
		self.__poller = ZMQ.Poller(4)
		self.__shell_poll_index = self.__poller.register(self.shell, ZMQ.Poller.POLLIN)
		self.__iopub_poll_index = self.__poller.register(self.iopub, ZMQ.Poller.POLLIN)
		self.__stdin_poll_index = self.__poller.register(self.stdin, ZMQ.Poller.POLLIN)
		self.__control_poll_index = self.__poller.register(self.control, ZMQ.Poller.POLLIN)

		# Create a session for message packing and unpacking
		self.session = Session(key, username)

		# Create a message handler for each socket
		self._shell_handler = _MessageRouter(self, 'shell')
		self._iopub_handler = _MessageRouter(self, 'iopub')
		self._stdin_handler = _MessageRouter(self, 'stdio')
		self._control_handler = _MessageRouter(self, 'control')

		# Reply handlers
		self.__request_listeners = {}
		self.__history_reply_handlers = {}
		self.__connect_reply_handlers = {}
		self.__kernel_info_reply_handlers = {}
		self.__shutdown_reply_handlers = {}

		# Comms
		self.__comm_id_to_comm = {}

		# Event callbacks
		self.on_status = None
		self.on_clear_output = None
		self.on_comm_open = None

		# State
		self.__busy = False


	def close(self):
		'''
		Shutdown
		:return: None
		'''
		self.shell.close()
		self.iopub.close()
		self.stdin.close()
		self.control.close()
		self.__ctx.close()


	def poll(self, timeout=0):
		'''
		Poll input sockets for incoming messages

		:param timeout: The amount of time to wait for a message in milliseconds.
			-1 = wait indefinitely, 0 = return immediately,
		:return:
		'''
		n_events = self.__poller.poll(timeout)
		while n_events > 0:
			if self.__poller.pollin(self.__iopub_poll_index):
				ident, msg = self.session.recv(self.iopub)
				ident = _unpack_ident(ident)
				self._iopub_handler.handle(ident, msg)

			if self.__poller.pollin(self.__stdin_poll_index):
				ident, msg = self.session.recv(self.stdin)
				ident = _unpack_ident(ident)
				self._stdin_handler.handle(ident, msg)

			if self.__poller.pollin(self.__shell_poll_index):
				ident, msg = self.session.recv(self.shell)
				ident = _unpack_ident(ident)
				self._shell_handler.handle(ident, msg)

			if self.__poller.pollin(self.__control_poll_index):
				ident, msg = self.session.recv(self.control)
				ident = _unpack_ident(ident)
				self._control_handler.handle(ident, msg)

			n_events = self.__poller.poll(0)


	@property
	def busy(self):
		return self.__busy


	def execute_request(self, code, silent=False, store_history=True, user_expressions=None, allow_stdin=True,
			    listener=None):
		'''
		Send an execute request to the remote kernel via the SHELL socket

		:param code: the code to execute
		:param silent:
		:param store_history:
		:param user_expressions:
		:param allow_stdin:
		:param on_ok: status=ok callback: f(execution_count, payload, user_expressions)
		:param on_error: status=error callback: f(ename, evalue, traceback)
		:param on_abort: status=abort callback: f()
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'execute_request', {
			'code': code,
			'silent': silent,
			'store_history': store_history,
			'user_expressions': user_expressions if user_expressions is not None   else {},
			'allow_stdin': allow_stdin
		})

		if listener is not None:
			self.__request_listeners[msg_id] = listener
			listener.ref(2)

		return msg_id


	def inspect_request(self, code, cursor_pos, detail_level=0, listener=None):
		'''
		Send an inspect request to the remote kernel via the SHELL socket

		:param code: the code to inspect
		:param cursor_pos: the position of the cursor (in unicode characters) where inspection is requested
		:param detail_level: 0 or 1
		:param on_ok: status=ok callback: f(status, data, metadata)
		:param on_error: status=error callback: f(ename, evalue, traceback)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'inspect_request', {
			'code': code,
			'cursor_pos': cursor_pos,
			'detail_level': detail_level
		})

		if listener is not None:
			self.__request_listeners[msg_id] = listener
			listener.ref()

		return msg_id


	def complete_request(self, code, cursor_pos, listener=None):
		'''
		Send a complete request to the remote kernel via the SHELL socket

		:param code: the code to complete
		:param cursor_pos: the position of the cursor (in unicode characters) where completion is requested
		:param on_ok: status=ok callback: f(matches, cursor_start, cursor_end, metadata)
		:param on_error: status=error callback: f(ename, evalue, traceback)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'complete_request', {
			'code': code,
			'cursor_pos': cursor_pos
		})

		if listener is not None:
			self.__request_listeners[msg_id] = listener
			listener.ref()

		return msg_id


	def history_request_range(self, output=True, raw=False,
				  session=0, start=0, stop=0, on_history=None):
		'''
		Send a range history_request to the remote kernel via the SHELL socket

		:param output:
		:param raw:
		:param session:
		:param start:
		:param stop:
		:param on_history: callback: f(history)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'history_request',
						{'output': output, 'raw': raw, 'hist_access_type': 'range',
						 'session': session, 'start': start, 'stop': stop})

		if on_history is not None:
			self.__history_reply_handlers[msg_id] = on_history

		return msg_id


	def history_request_tail(self, output=True, raw=False,
				 n=1, on_history=None):
		'''
		Send a tail history_request to the remote kernel via the SHELL socket

		:param output:
		:param raw:
		:param n: show the last n entries
		:param on_history: callback: f(history)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'history_request',
						{'output': output, 'raw': raw, 'hist_access_type': 'tail',
						 'n': n})

		if on_history is not None:
			self.__history_reply_handlers[msg_id] = on_history

		return msg_id


	def history_request_search(self, output=True, raw=False,
				   pattern='', unique=False, n=1, on_history=None):
		'''
		Send a search history_request to the remote kernel via the SHELL socket

		:param output:
		:param raw:
		:param patern:
		:param unique:
		:param n: show the last n entries
		:param on_history: callback: f(history)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'history_request',
						{'output': output, 'raw': raw, 'hist_access_type': 'search',
						 'n': n, 'pattern': pattern, 'unique': unique})

		if on_history is not None:
			self.__history_reply_handlers[msg_id] = on_history

		return msg_id


	def connect_request(self, on_connect=None):
		'''
		Send a connect_request to the remote kernel via the SHELL socket

		:param on_connect: callback: f(shell_port, iopub_port, stdin_port, hb_port)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'connect_request', {})

		if on_connect is not None:
			self.__connect_reply_handlers[msg_id] = on_connect

		return msg_id


	def kernel_info_request(self, on_kernel_info=None):
		'''
		Send a kernel_info request to the remote kernel via the SHELL socket

		:param on_kernel_info: callback: f(protocol_version, implementation, implementation_version, language,
			language_version, banner)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'kernel_info', {})

		if on_kernel_info is not None:
			self.__kernel_info_reply_handlers[msg_id] = on_kernel_info

		return msg_id


	def shutdown_request(self, on_shutdown=None):
		'''
		Send a shutdown request to the remote kernel via the SHELL socket

		:param on_shutdown: callback: f(restart)
		:return: message ID
		'''
		msg, msg_id = self.session.send(self.shell, 'shutdown', {})

		if on_shutdown is not None:
			self.__shutdown_reply_handlers[msg_id] = on_shutdown

		return msg_id


	def open_comm(self, target_name, data=None):
		'''
		Open a comm

		:param target_name: name identifying the constructor on the other end
		:param data: extra initialisation data
		:return: a Comm object
		'''
		if data is None:
			data = {}

		comm_id = uuid.uuid4()
		comm = Comm(self, comm_id, target_name)
		self.__comm_id_to_comm[comm_id] = comm

		self.session.send(self.shell, 'comm_open',
				  {'comm_id': comm_id, 'target_name': target_name, 'data': data})

		return comm


	def _notity_comm_closed(self, comm):
		del self.__comm_id_to_comm[comm.comm_id]


	def _handle_msg_shell_execute_reply(self, ident, msg):
		content = msg['content']
		status = content['status']
		parent_msg_id = _get_parent_msg_id(msg)
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			if kernel_request_listener.unref():
				del self.__request_listeners[parent_msg_id]
			if status == 'ok':
				execution_count = content['execution_count']
				payload = content['payload']
				user_expressions = content['user_expressions']
				kernel_request_listener.on_execute_ok(execution_count, payload, user_expressions)
			elif status == 'error':
				ename = content['ename']
				evalue = content['evalue']
				traceback = content['traceback']
				kernel_request_listener.on_execute_error(ename, evalue, traceback)
			elif status == 'abort':
				kernel_request_listener.on_execute_abort()
			else:
				raise ValueError, 'Unknown execute_reply status'
		else:
			print 'No listener for execute_reply'

	def _handle_msg_iopub_pyout(self, ident, msg):
		self._handle_msg_iopub_execute_result(ident, msg)

	def _handle_msg_iopub_execute_result(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		execution_count = content['execution_count']
		data = content['data']
		metadata = content['metadata']
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			kernel_request_listener.on_execute_result(execution_count, data, metadata)
		else:
			print 'No listener for execute_result'

	def _handle_msg_iopub_pyerr(self, ident, msg):
		self._handle_msg_iopub_error(ident, msg)

	def _handle_msg_iopub_error(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		ename = content['ename']
		evalue = content['evalue']
		traceback = content['traceback']
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			kernel_request_listener.on_error(ename, evalue, traceback)
		else:
			print 'No listener for execute_result'


	def _handle_msg_shell_inspect_reply(self, ident, msg):
		content = msg['content']
		status = content['status']
		parent_msg_id = _get_parent_msg_id(msg)
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			if kernel_request_listener.unref():
				del self.__request_listeners[parent_msg_id]
			if status == 'ok':
				data = content['data']
				metadata = content['metadata']
				kernel_request_listener.on_inspect_ok(data, metadata)
			elif status == 'error':
				ename = content['ename']
				evalue = content['evalue']
				traceback = content['traceback']
				kernel_request_listener.on_inspect_error(ename, evalue, traceback)
			else:
				raise ValueError, 'Unknown inspect_reply status'
		else:
			print 'No listener for inspect_reply'

	def _handle_msg_shell_complete_reply(self, ident, msg):
		content = msg['content']
		status = content['status']
		parent_msg_id = _get_parent_msg_id(msg)
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			if kernel_request_listener.unref():
				del self.__request_listeners[parent_msg_id]
			if status == 'ok':
				matches = content['matches']
				cursor_start = content['cursor_start']
				cursor_end = content['cursor_end']
				metadata = content['metadata']
				kernel_request_listener.on_complete_ok(matches, cursor_start, cursor_end, metadata)
			elif status == 'error':
				ename = content['ename']
				evalue = content['evalue']
				traceback = content['traceback']
				kernel_request_listener.on_complete_error(ename, evalue, traceback)
			else:
				raise ValueError, 'Unknown inspect_reply status'
		else:
			print 'No listener for complete_reply'

	def _handle_msg_shell_history_reply(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		on_history = self.__history_reply_handlers.pop(parent_msg_id, None)
		if on_history is not None:
			on_history(content['history'])
		else:
			print 'No listener for history_reply'

	def _handle_msg_shell_connect_reply(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		on_connect = self.__connect_reply_handlers.pop(parent_msg_id, None)
		if on_connect is not None:
			on_connect(content['shell_port'], content['iopub_port'], content['stdin_port'],
				   content['hb_port'])
		else:
			print 'No listener for connect_reply'

	def _handle_msg_shell_kernel_info_reply(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		on_kernel_info = self.__kernel_info_reply_handlers.pop(parent_msg_id, None)
		if on_kernel_info is not None:
			on_kernel_info(content['protocol_version'],
				       content['implementation'],
				       content['implementation_version'],
				       content['language'],
				       content['language_version'],
				       content['banner'])
		else:
			print 'No listener for kernel_info_reply'

	def _handle_msg_shell_shutdown_reply(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		on_shutdown = self.__shutdown_reply_handlers.pop(parent_msg_id, None)
		if on_shutdown is not None:
			on_shutdown(content['restart'])
		else:
			print 'No listener for shutdown_reply'

	def _handle_msg_iopub_stream(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		stream_name = content['name']
		data = content['data']
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			kernel_request_listener.on_stream(stream_name, data)
		else:
			print 'No listener for stream'

	def _handle_msg_iopub_display_data(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		source = content['source']
		data = content['data']
		metadata = content['metadata']
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			kernel_request_listener.on_display_data(source, data, metadata)
		else:
			print 'No listener for display_data'

	def _handle_msg_iopub_status(self, ident, msg):
		content = msg['content']
		execution_state = content['execution_state']
		parent_msg_id = _get_parent_msg_id(msg)
		self.__busy = execution_state == 'busy'
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			kernel_request_listener.on_status(self.__busy)
			if not self.__busy:
				if kernel_request_listener.unref():
					del self.__request_listeners[parent_msg_id]
		if self.on_status is not None:
			self.on_status(parent_msg_id, self.__busy)

	def _handle_msg_iopub_pyin(self, ident, msg):
		self._handle_msg_iopub_execute_input(ident, msg)

	def _handle_msg_iopub_execute_input(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		execution_count = content['execution_count']
		code = content['code']
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			kernel_request_listener.on_execute_input(execution_count, code)
		else:
			print 'No listener for execute_input'

	def _handle_msg_iopub_clear_output(self, ident, msg):
		content = msg['content']
		if self.on_clear_output is not None:
			self.on_clear_output(content['wait'])

	def _handle_msg_stdin_input_request(self, ident, msg):
		content = msg['content']
		parent_msg_id = _get_parent_msg_id(msg)
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
			request_header = msg['header']

			def reply_callback(value):
				self.session.send(self.stdin, 'input_reply', {'value': value}, parent=request_header)

			kernel_request_listener.on_input_request(content['prompt'], content['password'], reply_callback)

	def _handle_msg_iopub_comm_open(self, ident, msg):
		content = msg['content']

		comm_id = content['comm_id']
		target_name = content['target_name']
		data = content['data']

		comm = Comm(self, comm_id, target_name)
		self.__comm_id_to_comm[comm_id] = comm

		if self.on_comm_open is not None:
			self.on_comm_open(comm, data)

	def _handle_msg_iopub_comm_msg(self, ident, msg):
		content = msg['content']

		comm_id = content['comm_id']
		data = content['data']

		comm = self.__comm_id_to_comm[comm_id]
		if comm.on_message is not None:
			comm.on_message(data)

	def _handle_msg_iopub_comm_close(self, ident, msg):
		content = msg['content']

		comm_id = content['comm_id']
		data = content['data']

		comm = self.__comm_id_to_comm[comm_id]
		if comm.on_close is not None:
			comm.on_close(data)
		del self.__comm_id_to_comm[comm_id]


class Session(object):
	def __init__(self, key, username=''):
		'''
		IPython session constructor

		:param key: message authentication key from connection file
		:param username: Username of user (or empty string)
		:return:
		'''
		self.__key = key.encode('utf8')

		self.auth = hmac.HMAC(self.__key, digestmod=hashlib.sha256)

		self.session = str(uuid.uuid4())
		self.username = username

		self.__none = self._pack({})


	def send(self, stream, msg_type, content=None, parent=None, metadata=None, ident=None, buffers=None):
		'''
		Build and sent a message on a JeroMQ stream

		:param stream: the JeroMQ stream over which the message is to be sent
		:param msg_type: the message type (see IPython docs for explanation of these)
		:param content: message content
		:param parent: message parent header
		:param metadata: message metadata
		:param ident: IDENT
		:param buffers: binary data buffers to append to message
		:return: a tuple of (message structure, message ID)
		'''
		msg, msg_id = self.build_msg(msg_type, content, parent, metadata)
		to_send = self.serialize(msg, ident)
		if buffers is not None:
			to_send.extend(buffers)
		for part in to_send[:-1]:
			stream.sendMore(part)
		stream.send(to_send[-1])
		return msg, msg_id

	def recv(self, stream):
		'''
		Receive a message from a stream
		:param stream: the JeroMQ stream from which to read the message
		:return: a tuple: (idents, msg) where msg is the deserialized message
		'''
		msg_list = [stream.recv()]
		while stream.hasReceiveMore():
			msg_list.append(stream.recv())

		# Extract identities
		pos = msg_list.index(DELIM)
		idents, msg_list = msg_list[:pos], msg_list[pos + 1:]
		return idents, self.deserialize(msg_list)


	def serialize(self, msg, ident=None):
		'''
		Serialize a message into a list of byte arrays

		:param msg: the message to serialize
		:param ident: the ident
		:return: the serialize message in the form of a list of byte arrays
		'''
		content = msg.get('content', {})
		if content is None:
			content = self.__none
		else:
			content = self._pack(content)

		payload = [self._pack(msg['header']),
			   self._pack(msg['parent_header']),
			   self._pack(msg['metadata']),
			   content]

		serialized = []

		if isinstance(ident, list):
			serialized.extend(ident)
		elif ident is not None:
			serialized.append(ident)
		serialized.append(DELIM)

		signature = self.sign(payload)
		serialized.append(signature)
		serialized.extend(payload)

		return serialized


	def deserialize(self, msg_list):
		'''
		Deserialize a message, converting it from a list of byte arrays to a message structure (a dict)
		:param msg_list: serialized message in the form of a list of byte arrays
		:return: message structure
		'''
		min_len = 5
		if self.auth is not None:
			signature = msg_list[0]
			check = self.sign(msg_list[1:5])
			if signature != check:
				raise ValueError, 'Invalid signature'
		if len(msg_list) < min_len:
			raise ValueError, 'Message too short'
		header = self._unpack(msg_list[1])
		return {
			'header': header,
			'msg_id': header['msg_id'],
			'msg_type': header['msg_type'],
			'parent_header': self._unpack(msg_list[2]),
			'metadata': self._unpack(msg_list[3]),
			'content': self._unpack(msg_list[4]),
			'buffers': msg_list[5:]
		}


	def build_msg_header(self, msg_type):
		'''
		Build a header for a message of the given type
		:param msg_type: the message type
		:return: the message header
		'''
		msg_id = str(uuid.uuid4())
		return {
			'msg_id': msg_id,
			'msg_type': msg_type,
			'username': self.username,
			'session': self.session,
			'date': datetime.datetime.now().isoformat(),
			'version': KERNEL_PROTOCOL_VERSION
		}

	def build_msg(self, msg_type, content=None, parent=None, metadata=None):
		'''
		Build a message of the given type, with content, parent and metadata
		:param msg_type: the message type
		:param content: message content
		:param parent: message parent header
		:param metadata: metadata
		:return: the message structure
		'''
		header = self.build_msg_header(msg_type)
		msg_id = header['msg_id']
		return {
			       'header': header,
			       'msg_id': msg_id,
			       'msg_type': msg_type,
			       'parent_header': {} if parent is None   else parent,
			       'content': {} if content is None   else content,
			       'metadata': {} if metadata is None   else metadata,
		       }, msg_id


	def sign(self, msg_payload_list):
		'''
		Sign a message payload

		:param msg_payload_list: the message payload (header, parent header, content, metadata)
		:return: signature hash hex digest
		'''
		if self.auth is None:
			return StringUtil.toBytes('')
		else:
			h = self.auth.copy()
			for m in msg_payload_list:
				h.update(m)
			return StringUtil.toBytes(h.hexdigest())


	def _pack(self, x):
		'''
		Pack message data into a byte array

		:param x: message data to pack
		:return: byte array
		'''
		return StringUtil.toBytes(json.dumps(x))

	def _unpack(self, x):
		'''
		Unpack byte array into message data

		:param x: byte array to unpack
		:return: message component
		'''
		return json.loads(StringUtil.fromBytes(x))


