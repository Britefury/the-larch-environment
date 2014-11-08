##-*************************
##-* This software can be used, redistributed and/or modified under
##-* the terms of the BSD 2-clause license as found in the file
##-* 'License.txt' in this distribution.
##-* This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************

import uuid, datetime, subprocess, tempfile, json

from .util import *
from .session import Session
from .comm import Comm, CommManager
from .request_listener import *



class ConnectionFileNotFoundError (Exception):
	pass

class InvalidConnectionFileError (Exception):
	pass




def load_connection_file(kernel_name=None, kernel_path=None):
	if kernel_name is not None  and  kernel_path is None:
		kernel_path = os.path.expanduser(os.path.join('~', '.ipython', 'profile_default', 'security',
						    'kernel-{0}.json'.format(kernel_name)))
	elif kernel_path is not None  and  kernel_name is None:
		pass
	elif kernel_name is None  and  kernel_path is None:
		raise ValueError, 'Either kernel_name OR kernel_path must have a value'

	if os.path.exists(kernel_path):
		with open(kernel_path, 'r') as f:
			try:
				return json.load(f)
			except ValueError:
				raise InvalidConnectionFileError
	else:
		raise ConnectionFileNotFoundError, 'Could not find connection file for kernel {0} at {1}'.format(kernel_name, kernel_path)


def _unpack_ident(ident):
	return [bytes_to_str(x) for x in ident]



def _get_parent_msg_id(msg):
	return msg['parent_header'].get('msg_id')



class KernelConnection(object):
	__ctx = None
	__ctx_ref_count = 0

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

	def __init__(self, kernel_name=None, kernel_path=None, username=''):
		'''
		IPython kernel connection constructor

		Note that only one of kernel_name and kernel_path should be provided

		:param kernel_name: kernel name used to identify connection file
		:param kernel_path: path of connection file
		:param username: username
		:return:
		'''
		# Load the connection file and find out where we have to connect to
		connection = load_connection_file(kernel_name=kernel_name, kernel_path=kernel_path)

		key = connection['key'].encode('utf8')
		transport = connection['transport']
		address = connection['ip']

		shell_port = connection['shell_port']
		iopub_port = connection['iopub_port']
		stdin_port = connection['stdin_port']
		control_port = connection['control_port']

		# JeroMQ context
		cls = type(self)
		if cls.__ctx is None:
			cls.__ctx = ZMQ_new_context(1)
		cls.__ctx_ref_count += 1

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
		zmq_subscribe_socket(self.iopub, '')

		# Create a poller to monitor the four sockets for incoming messages
		self.__poller = ZMQReadPoller()
		self.__shell_poll_index = self.__poller.register(self.shell)
		self.__iopub_poll_index = self.__poller.register(self.iopub)
		self.__stdin_poll_index = self.__poller.register(self.stdin)
		self.__control_poll_index = self.__poller.register(self.control)

		# Create a session for message packing and unpacking
		self.session = Session(key, username)

		# Create a message handler for each socket
		self._shell_handler = MessageRouter(self, 'shell')
		self._iopub_handler = MessageRouter(self, 'iopub')
		self._stdin_handler = MessageRouter(self, 'stdio')
		self._control_handler = MessageRouter(self, 'control')

		self.__socket_handlers = {}
		self.__socket_handlers[self.shell] = self._shell_handler
		self.__socket_handlers[self.iopub] = self._iopub_handler
		self.__socket_handlers[self.stdin] = self._stdin_handler
		self.__socket_handlers[self.control] = self._control_handler

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
		self._open = True



	def is_open(self):
		return self._open


	def close(self):
		'''
		Shutdown
		:return: None
		'''
		self.shell.close()
		self.iopub.close()
		self.stdin.close()
		self.control.close()
		cls = type(self)
		cls.__ctx_ref_count -= 1
		if cls.__ctx_ref_count == 0:
			cls.__ctx.term()
			cls.__ctx = None
		self._open = False


	def poll(self, timeout=0):
		'''
		Poll input sockets for incoming messages

		:param timeout: The amount of time to wait for a message in milliseconds.
			-1 = wait indefinitely, 0 = return immediately,
		:return: a boolean indicating if events were processed
		'''
		n_events = 0
		if self._open:
			def _on_read_event(socket):
				handler = self.__socket_handlers[socket]
				ident, msg = self.session.recv(socket)
				ident = _unpack_ident(ident)
				handler.handle(ident, msg)


			n_events = self.__poller.poll(timeout, _on_read_event)

		return n_events > 0


	@property
	def busy(self):
		return self.__busy


	def __unref_kernel_listener(self, parent_msg_id, listener, count=1):
		if listener.unref(count):
			del self.__request_listeners[parent_msg_id]
			listener.on_request_finished()


	def execute_request(self, code, silent=False, store_history=True, user_expressions=None, allow_stdin=True,
			    listener=None):
		'''
		Send an execute request to the remote kernel via the SHELL socket

		:param code: the code to execute
		:param silent: (default False) if True, an entry will not be created in the history
		(store_history will be forced to False), will not broadcast output ot IOPUB channel
		(no execute_input event) and will not have an execute_result (no execute_result event)
		:param store_history: (default True) if False a history entry will not be created
		:param user_expressions: (default None) a dictionary mapping names to expressions to be evaluated
		:param allow_stdin: (default True) if False, the kernel will raise StdInNotImplementedError
		if stdin is attempted
		:param listener: None, or an object that implements KernelRequestListener and ExecuteRequestListenerMixin
		:return: message ID
		'''
		if self._open:
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
		:param listener: None, or an object that implements KernelRequestListener and InspectRequestListenerMixin
		:return: message ID
		'''
		if self._open:
			msg, msg_id = self.session.send(self.shell, 'inspect_request', {
				'code': code,
				'cursor_pos': cursor_pos,
				'detail_level': detail_level
			})

			if listener is not None:
				self.__request_listeners[msg_id] = listener
				listener.ref(2)

			return msg_id


	def complete_request(self, code, cursor_pos, listener=None):
		'''
		Send a complete request to the remote kernel via the SHELL socket

		:param code: the code to complete
		:param cursor_pos: the position of the cursor (in unicode characters) where completion is requested
		:param listener: None, or an object that implements KernelRequestListener and CompleteRequestListenerMixin
		:return: message ID
		'''
		if self._open:
			msg, msg_id = self.session.send(self.shell, 'complete_request', {
				'code': code,
				'cursor_pos': cursor_pos
			})

			if listener is not None:
				self.__request_listeners[msg_id] = listener
				listener.ref(2)

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
		if self._open:
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
		if self._open:
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
		if self._open:
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
		if self._open:
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
		if self._open:
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
		if self._open:
			msg, msg_id = self.session.send(self.shell, 'shutdown', {})

			if on_shutdown is not None:
				self.__shutdown_reply_handlers[msg_id] = on_shutdown

			return msg_id


	def open_comm(self, target_name, data=None, listener=None):
		'''
		Open a comm

		:param target_name: name identifying the constructor on the other end
		:param data: extra initialisation data
		:param listener: a Kernel
		:return: a Comm object
		'''
		if self._open:
			if data is None:
				data = {}

			comm_id = str(uuid.uuid4())
			comm = Comm(self, comm_id, target_name, True)
			self.__comm_id_to_comm[comm_id] = comm

			msg, msg_id = self.session.send(self.shell, 'comm_open',
					  {'comm_id': str(comm_id), 'target_name': target_name, 'data': data})

			if listener is not None:
				self.__request_listeners[msg_id] = listener
				listener.ref(1)

			return comm


	def _notity_comm_closed(self, comm):
		del self.__comm_id_to_comm[comm.comm_id]


	def _handle_msg_shell_execute_reply(self, ident, msg):
		content = msg['content']
		status = content['status']
		parent_msg_id = _get_parent_msg_id(msg)
		kernel_request_listener = self.__request_listeners.get(parent_msg_id)
		if kernel_request_listener is not None:
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
			elif status == 'abort'  or  status == 'aborted':
				kernel_request_listener.on_execute_abort()
			else:
				raise ValueError, 'Unknown execute_reply status {0}'.format(status)
			self.__unref_kernel_listener(parent_msg_id, kernel_request_listener)
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
			self.__unref_kernel_listener(parent_msg_id, kernel_request_listener)
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
				raise ValueError, 'Unknown complete_reply status'
			self.__unref_kernel_listener(parent_msg_id, kernel_request_listener)
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
				self.__unref_kernel_listener(parent_msg_id, kernel_request_listener)
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

		comm = Comm(self, comm_id, target_name, False)
		self.__comm_id_to_comm[comm_id] = comm

		if self.on_comm_open is not None:
			self.on_comm_open(comm, data)

	def _handle_msg_iopub_comm_msg(self, ident, msg):
		content = msg['content']

		comm_id = content['comm_id']
		data = content['data']

		comm = self.__comm_id_to_comm[comm_id]
		comm._handle_message(data)

	def _handle_msg_iopub_comm_close(self, ident, msg):
		content = msg['content']

		comm_id = content['comm_id']
		data = content['data']

		comm = self.__comm_id_to_comm[comm_id]
		comm._handle_closed_remotely(data)
		del self.__comm_id_to_comm[comm_id]


__connection_file_paths = []
__ipython_processes = []




class IPythonKernelProcess (object):
	__kernels = []

	def __init__(self, ipython_path='ipython', connection_file_path=None):
		# If no connection file path was specified, generate one
		if connection_file_path is None:
			handle, connection_file_path = tempfile.mkstemp(suffix='.json', prefix='kernel')
			os.close(handle)
			os.remove(connection_file_path)

		self.__connection_file_path = connection_file_path

		# Spawn the kernel in a sub-process
		env = None

		self.__proc = subprocess.Popen([ipython_path, 'kernel', '-f', self.__connection_file_path],
					       env=env, stdout=subprocess.PIPE)

		self.__connection = None

		self.__kernels.append(self)


	def is_open(self):
		return self.__connection.is_open()   if self.__connection is not None   else None


	def close(self):
		if self.__connection is not None:
			self.__connection.close()
		self.__proc.terminate()
		if os.path.exists(self.__connection_file_path):
			os.remove(self.__connection_file_path)





	@property
	def connection(self):
		if self.__connection is None:
			if os.path.exists(self.__connection_file_path):
				try:
					self.__connection = KernelConnection(kernel_path=self.__connection_file_path)
				except InvalidConnectionFileError:
					# Try again
					pass
		return self.__connection



import unittest, sys, os, time

class TestCase_jipy_kernel (unittest.TestCase):
	@classmethod
	def setUpClass(cls):
		ipython_path = os.environ.get('IPYTHON_PATH', 'ipython')

		# Start the IPython kernel process
		cls.krn_proc = IPythonKernelProcess(ipython_path=ipython_path)

		while cls.krn_proc.connection is None:
			time.sleep(0.1)

		cls.krn = cls.krn_proc.connection


	@classmethod
	def tearDownClass(cls):
		cls.krn = None
		cls.krn_proc.close()

	def __show_evs(self, actual, expected):
		print 'ACTUAL EVENTS LIST'
		print '------------------'
		for a in actual:
			print a
		print ''
		print 'EXPECTED EVENTS LIST'
		print '--------------------'
		for a in expected:
			print a

	def assertEventListsEqual(self, actual, expected):
		evs_a = actual[:]
		evs_b = expected[:]
		for a in evs_a:
			try:
				evs_b.remove(a)
			except ValueError:
				print ''
				print 'Event\n{0}\npresent in ACTUAL list but NOT present in EXPECTED'.format(a)
				self.__show_evs(actual, expected)
				print
				self.fail()
		for b in evs_b:
			print ''
			print 'Event\n{0}\nNOT present in ACTUAL list but present in EXPECTED'.format(b)
			self.__show_evs(actual, expected)
			print
			self.fail()



	def _make_event_log_listener(self, ListenerType, on_input=None):
		if on_input is None:
			on_input = lambda prompt: 'test_input'
		return ListenerType(on_input)


	def test_010_krn_import_time(self):
		ev = self._make_event_log_listener(EventLogExecuteRequestListener)

		code = 'import time, sys\n'

		self.krn.execute_request(code, listener=ev)
		while len(ev.events) < 4:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code, execution_count=1),
			krn_event('on_execute_ok', execution_count=1, payload=[], user_expressions={}),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev.events[-1]['event_name'], 'on_request_finished')


	def test_020_krn_sleep(self):
		ev = self._make_event_log_listener(EventLogExecuteRequestListener)

		code = 'time.sleep(0.1)\n'

		self.krn.execute_request(code, listener=ev)
		while len(ev.events) < 5:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code, execution_count=2),
			krn_event('on_execute_ok', execution_count=2, payload=[], user_expressions={}),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev.events[-1]['event_name'], 'on_request_finished')


	def test_030_krn_stdout(self):
		ev = self._make_event_log_listener(EventLogExecuteRequestListener)

		code = 'print "Hello world"\n'

		self.krn.execute_request(code, listener=ev)
		while len(ev.events) < 6:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code, execution_count=3),
			krn_event('on_execute_ok', execution_count=3, payload=[], user_expressions={}),
			krn_event('on_stream', stream_name='stdout', data='Hello world\n'),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev.events[-1]['event_name'], 'on_request_finished')


	def test_040_expr(self):
		ev = self._make_event_log_listener(EventLogExecuteRequestListener)

		code = '3.141\n'

		self.krn.execute_request(code, listener=ev)
		while len(ev.events) < 6:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code, execution_count=4),
			krn_event('on_execute_ok', execution_count=4, payload=[], user_expressions={}),
			krn_event('on_execute_result', execution_count=4, data={'text/plain': '3.141'}, metadata={}),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev.events[-1]['event_name'], 'on_request_finished')


	def test_050_raise(self):
		ev = self._make_event_log_listener(EventLogExecuteRequestListener)

		code = 'raise ValueError\n'

		self.krn.execute_request(code, listener=ev)
		while len(ev.events) < 6:
			self.krn.poll(-1)

		tb = [u'---------------------------------------------------------------------------\nValueError                                Traceback (most recent call last)',
		      u'<ipython-input-5-94ef6d30a139> in <module>()\n----> 1 raise ValueError\n', u'ValueError: ']

		self.assertEventListsEqual(ev.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code, execution_count=5),
			krn_event('on_execute_error', ename='ValueError', evalue='', traceback=tb),
			krn_event('on_error', ename='ValueError', evalue='', traceback=tb),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev.events[-1]['event_name'], 'on_request_finished')


	def test_060_execute_no_history(self):
		ev = self._make_event_log_listener(EventLogExecuteRequestListener)

		code = 'print "Hello world"\n3.141\n'

		self.krn.execute_request(code, listener=ev, store_history=False)
		while len(ev.events) < 7:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code, execution_count=6),
			krn_event('on_execute_ok', execution_count=5, payload=[], user_expressions={}),
			krn_event('on_execute_result', execution_count=6, data={'text/plain': '3.141'}, metadata={}),
			krn_event('on_stream', stream_name='stdout', data='Hello world\n'),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev.events[-1]['event_name'], 'on_request_finished')


	def test_070_execute_silent(self):
		ev = self._make_event_log_listener(EventLogExecuteRequestListener)

		code = 'print "Hello world"\n3.141\n'

		self.krn.execute_request(code, listener=ev, silent=True)
		while len(ev.events) < 5:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_ok', execution_count=5, payload=[], user_expressions={}),
			krn_event('on_stream', stream_name='stdout', data='Hello world\n'),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev.events[-1]['event_name'], 'on_request_finished')


	def test_080_open_comm_from_frontend(self):
		ev_exec = self._make_event_log_listener(EventLogExecuteRequestListener)

		code1 = """
from IPython.core.getipython import get_ipython

received_comm = None
def on_mipy_test_open(comm, data):
	global received_comm

	received_comm = comm

	def reply(data):
		received_comm.send({'reply_to': data['content']['data']})

	print 'mipy test opened {0}'.format(data['content']['data'])
	received_comm.on_msg(reply)

comm_manager = get_ipython().comm_manager
comm_manager.register_target('mipy_test', on_mipy_test_open)
"""

		code2 = """
received_comm.send({'text': 'Hi there'})
"""

		self.krn.execute_request(code1, listener=ev_exec, store_history=False)
		while len(ev_exec.events) < 5:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev_exec.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code1, execution_count=6),
			krn_event('on_execute_ok', execution_count=5, payload=[], user_expressions={}),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev_exec.events[-1]['event_name'], 'on_request_finished')

		ev_open_comm = self._make_event_log_listener(EventLogKernelRequestListener)
		comm = self.krn.open_comm('mipy_test', {'a': 1}, listener=ev_open_comm)

		received_messages = []
		def on_comm_message(comm, data):
			received_messages.append(data)
		comm.on_message = on_comm_message

		while len(ev_open_comm.events) < 4:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev_open_comm.events, [
			krn_event('on_status', busy=True),
			krn_event('on_stream', stream_name='stdout', data="mipy test opened {u'a': 1}\n"),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])


		ev_exec.clear()
		self.krn.execute_request(code2, listener=ev_exec, store_history=False)
		while len(ev_exec.events) < 5:
			self.krn.poll(-1)

		self.assertEqual(received_messages, [{'text': 'Hi there'}])
		del received_messages[:]


		comm.send({'b': 2})
		while len(received_messages) < 1:
			self.krn.poll(-1)
		self.assertEqual(received_messages, [{'reply_to': {'b': 2}}])


	def test_090_open_comm_from_kernel(self):
		manager = CommManager()

		received_messages = []
		def on_message(comm, msg):
			received_messages.append(msg)

		open_comms = []
		open_comm_data = []
		def on_mipy_test_opened(comm, data):
			open_comms.append(comm)
			open_comm_data.append(data)
			comm.on_message = on_message

		manager.register_comm_open_handler('mipy_test', on_mipy_test_opened)


		manager.attach_to_kernel(self.krn)

		ev_exec = self._make_event_log_listener(EventLogExecuteRequestListener)

		code1 = """
from IPython.kernel.comm.comm import Comm

comm = Comm(target_name='mipy_test', data={'a': 1})
"""

		code2 = """
comm.send({'text': 'Hi there'})
"""

		self.krn.execute_request(code1, listener=ev_exec, store_history=False)
		while len(ev_exec.events) < 5:
			self.krn.poll(-1)

		self.assertEventListsEqual(ev_exec.events, [
			krn_event('on_status', busy=True),
			krn_event('on_execute_input', code=code1, execution_count=6),
			krn_event('on_execute_ok', execution_count=5, payload=[], user_expressions={}),
			krn_event('on_status', busy=False),
			krn_event('on_request_finished'),
			])
		self.assertEqual(ev_exec.events[-1]['event_name'], 'on_request_finished')

		self.assertEqual(1, len(open_comms))
		self.assertEqual([{'a': 1}], open_comm_data)


		ev_exec.clear()
		self.krn.execute_request(code2, listener=ev_exec, store_history=False)
		while len(ev_exec.events) < 5:
			self.krn.poll(-1)


		self.assertEqual([{'text': 'Hi there'}], received_messages)





def test_poll_speed():
	krn_proc = IPythonKernelProcess()

	while krn_proc.connection is None:
		time.sleep(0.1)

	krn = krn_proc.connection

	N_POLLS = 1024
	t1 = datetime.datetime.now()
	for i in xrange(N_POLLS):
		krn.poll(0)
	t2 = datetime.datetime.now()
	print 'Polling {0} times took {1}'.format(N_POLLS, t2 - t1)
