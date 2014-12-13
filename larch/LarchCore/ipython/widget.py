import collections

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank


_view_name_to_class = {
}



class IPythonWidgetViewType (type):
	def __init__(cls, name, bases, dict):
		super(IPythonWidgetViewType, cls).__init__(name, bases, dict)

		view_name = dict.get('__viewname__', name)
		if view_name is not None:
			_view_name_to_class[view_name] = cls


class IPythonWidgetView (object):
	__metaclass__ = IPythonWidgetViewType

	__viewname__ = None

	def __init__(self, widget_model, widget_manager, comm, state):
		self._incr = IncrementalValueMonitor()
		self._comm = comm
		self.model = widget_model
		self._widget_manager = widget_manager
		self.__dict__.update(state)

	def on_display(self):
		pass

	def update(self, state):
		self.__dict__.update(state)
		self._incr.onChanged()

	def _internal_update(self, state):
		self.__dict__.update(state)
		self._incr.onChanged()



class IPythonWidgetModel (object):
	DEFAULT_MSG_THROTTLE = 3

	def __init__(self, widget_manager, result, comm, data):
		print 'IPythonWidgetModel.__init__: comm.comm_id={0}'.format(comm.comm_id)
		self.__widget_manager = widget_manager
		self.result = result
		self.comm = comm
		self.comm.on_message = self._on_message
		self.comm.on_closed_remotely = self._on_closed_remotely
		self.open = True
		self._state = {}
		self.__incr = IncrementalValueMonitor()
		self._view = None

		self.__sync_kernel_listeners_waiting = collections.deque()
		self.__all_sync_kernel_listeners = []
		self.__num_sync_kernel_listeners = 0
		self.__custom_kernel_listener = self.result.new_kernel_request_listener()


	@property
	def _message_throttle(self):
		try:
			return self.msg_throttle
		except AttributeError:
			return self.DEFAULT_MSG_THROTTLE


	def __on_kernel_request_listener_waiting(self, listener):
		self.__sync_kernel_listeners_waiting.append(listener)

	def _sync_kernel_request_listener(self):
		if len(self.__sync_kernel_listeners_waiting) == 0:
			if self.__num_sync_kernel_listeners < self._message_throttle:
				listener = self.result.new_kernel_request_listener(self.__on_kernel_request_listener_waiting)
				self.__num_sync_kernel_listeners += 1
				self.__all_sync_kernel_listeners.append(listener)
				return listener
			else:
				return None
		else:
			listener = self.__sync_kernel_listeners_waiting.popleft()
			return listener


	def send_sync(self, sync_data):
		listener = self._sync_kernel_request_listener()
		if listener is not None:
			self.__send({'method': 'backbone', 'sync_data': sync_data}, listener)

	def send_custom(self, content):
		self.__send({'method': 'custom', 'content': content}, self.__custom_kernel_listener)

	def __send(self, msg, kernel_request_listener=None):
		self.comm.send(msg, listener=kernel_request_listener)


	def close(self):
		# if self.open:
		# 	self.comm.close()
		# 	self.open = False
		self.__widget_manager._notify_widget_closed(self)
		self.__custom_kernel_listener.detach()
		for listener in self.__all_sync_kernel_listeners:
			listener.detach()


	def _on_closed_remotely(self, comm, data, kernel_request_listener):
		self.open = False
		self.__widget_manager._notify_widget_closed(self)
		self.__custom_kernel_listener.detach()
		for listener in self.__all_sync_kernel_listeners:
			listener.detach()
		print 'IPythonWidgetModel._on_closed_remotely'


	def display(self, result):
		if self._view is not None:
			self._view.on_display()
		result._display_widget(self)

	def _on_message(self, comm, data, kernel_request_listener):
		method = data['method']
		if method == 'update':
			state = data['state']
			self._state.update(state)
			view_name = state['_view_name']
			view_class = _view_name_to_class.get(view_name)
			if view_class is None:
				print 'IPythonWidgetModel._on_message: no view class for {0}, id={1}'.format(view_name, self.comm.comm_id)
				print state
				self._view = None
			else:
				if view_class is not type(self._view):
					self._view = view_class(self, self.__widget_manager, self.comm, self._state)
					self.__incr.onChanged()
				else:
					self._view.update(self._state)
		elif method == 'display':
			print 'IPythonWidgetModel._on_message: {0} display {1}'.format(self._state.get('_view_name'), data)
			if kernel_request_listener is not None:
				result = kernel_request_listener.result
			else:
				result = self.result
			self.display(result)
		else:
			data_no_method = data.copy()
			data_no_method.pop('method')
			print 'IPythonWidgetModel._on_message: method not implemented: method={0}, data={1}'.format(method, data_no_method)


	def __present__(self, fragment, inh):
		self.__incr.onAccess()
		if self._view is not None:
			return Pres.coerce(self._view)
		else:
			return Blank()



class IPythonWidgetManager (object):
	def __init__(self):
		self.__comm_id_to_widget = {}


	def new_widget(self, exec_result, comm, data):
		widget = IPythonWidgetModel(self, exec_result, comm, data)
		self.__comm_id_to_widget[comm.comm_id] = widget
		return widget

	def get_by_comm_id(self, comm_id):
		return self.__comm_id_to_widget.get(comm_id)

	def _notify_widget_closed(self, widget):
		comm_id = widget.comm.comm_id
		try:
			del self.__comm_id_to_widget[comm_id]
		except KeyError:
			pass



