from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import SwitchButton, OptionMenu

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer, Column

from LarchCore.ipython.widget import IPythonWidgetView



class ToggleButtonsView (IPythonWidgetView):
	def _on_edit(self, value_name):
		sync_data = {'value_name': value_name}
		self._internal_update(sync_data)
		self.model.send_sync(sync_data)

	def __present__(self, fragment, inh):
		def on_choice(control, prev_index, new_index):
			value_name = self.value_names[new_index]
			self._on_edit(value_name)
			value_live.setLiteralValue(new_index)

		self._incr.onAccess()
		value_name = unicode(self.value_name)
		value_live = LiveValue(self.value_names.index(value_name))
		choices = [Label(choice)   for choice in self.value_names]
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    SwitchButton(choices, choices, SwitchButton.Orientation.HORIZONTAL, value_live, on_choice)])



class DropdownView (IPythonWidgetView):
	def _on_edit(self, value_name):
		sync_data = {'value_name': value_name}
		self._internal_update(sync_data)
		self.model.send_sync(sync_data)

	def __present__(self, fragment, inh):
		def on_choice(control, prev_index, new_index):
			value_name = self.value_names[new_index]
			self._on_edit(value_name)
			value_live.setLiteralValue(new_index)

		self._incr.onAccess()
		value_name = unicode(self.value_name)
		value_live = LiveValue(self.value_names.index(value_name))
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    OptionMenu([Label(choice)   for choice in self.value_names], value_live, on_choice)])


