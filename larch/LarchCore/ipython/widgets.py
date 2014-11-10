from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import Button, Checkbox, IntSlider, ToggleButton

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer


class IPythonWidgetView (object):
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


class IntSliderView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			sync_data = {'value': new_value}
			self._internal_update(sync_data)
			self._comm.send({'method': 'backbone', 'sync_data': sync_data})
			value_live.setLiteralValue(new_value)

		value = int(self.value)
		value_live = LiveValue(value)
		slider_min = int(self.min)
		slider_max = int(self.max)
		if slider_min >= 0  or  slider_max <= 0:
			pivot = int((slider_min + slider_max) * 0.5)
		else:
			pivot = 0
		return Row([Label(self.description), Spacer(10.0, 0.0), IntSlider(value_live, slider_min, slider_max, pivot, 400.0, _on_change)])


class CheckboxView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			sync_data = {'value': new_value}
			self._internal_update(sync_data)
			self._comm.send({'method': 'backbone', 'sync_data': sync_data})
			value_live.setLiteralValue(new_value)

		value = bool(self.value)
		value_live = LiveValue(value)
		return Checkbox.checkboxWithLabel(self.description, value_live, _on_change)


class ToggleButtonView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			sync_data = {'value': new_value}
			self._internal_update(sync_data)
			self._comm.send({'method': 'backbone', 'sync_data': sync_data})
			value_live.setLiteralValue(new_value)

		value = bool(self.value)
		value_live = LiveValue(value)
		return ToggleButton.toggleButtonWithLabel(self.description, value_live, _on_change)


class ButtonView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_click(control, event):
			self._comm.send({'method': 'custom', 'content': {'event': 'click'}})

		return Button.buttonWithLabel(self.description, _on_click)


_view_name_to_class = {
	'IntSliderView': IntSliderView,
	'CheckboxView': CheckboxView,
	'ToggleButtonView': ToggleButtonView,
	'ButtonView': ButtonView,
}

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


	def _on_closed_remotely(self, comm, data):
		self.open = False
		print 'IPythonWidgetModel._on_closed_remotely'


	def _on_message(self, comm, data):
		method = data['method']
		if method == 'update':
			state = data['state']
			self._state.update(state)
			view_name = state['_view_name']
			view_class = _view_name_to_class.get(view_name)
			if view_class is None:
				print 'IPythonWidgetModel._on_message: no view class for {0}'.format(view_name)
				self._view = None
			else:
				if view_class is not type(self._view):
					self._view = view_class(self.comm, self._state)
					self.__incr.onChanged()
				else:
					self._view.update(self._state)
		elif method == 'display':
			print 'IPythonWidgetModel display {0}'.format(data)
			self.result._display_widget(self)
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


