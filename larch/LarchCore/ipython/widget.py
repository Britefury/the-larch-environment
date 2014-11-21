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

	def __init__(self, comm, state):
		self._incr = IncrementalValueMonitor()
		self._comm = comm
		self.__dict__.update(state)

	def update(self, state):
		self.__dict__.update(state)
		self._incr.onChanged()

	def _internal_update(self, state):
		self.__dict__.update(state)
		self._incr.onChanged()



class IPythonWidgetModel (object):
	def __init__(self, result, comm, data):
		self.result = result
		self.comm = comm
		self.comm.on_message = self._on_message
		self.comm.on_closed_remotely = self._on_closed_remotely
		self.open = True
		self._state = {}
		self.__incr = IncrementalValueMonitor()
		self._view = None


	def close(self):
		# if self.open:
		# 	self.comm.close()
		# 	self.open = False
		pass


	def _on_closed_remotely(self, comm, data, kernel_request_listener):
		self.open = False
		print 'IPythonWidgetModel._on_closed_remotely'


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
					self._view = view_class(self.comm, self._state)
					self.__incr.onChanged()
				else:
					self._view.update(self._state)
		elif method == 'display':
			if kernel_request_listener is not None:
				result = kernel_request_listener.result
			else:
				result = self.result
			result._display_widget(self)
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


