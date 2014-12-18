from java.awt import Color

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import SwitchButton, OptionMenu, RadioButton, ListSelect

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer, Column

from LarchCore.ipython.widget import IPythonWidgetView



class ToggleButtonsView (IPythonWidgetView):
	def _on_edit(self, value_name):
		self._state_sync(value_name=value_name)

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
		self._state_sync(value_name=value_name)

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



class RadioButtonsView (IPythonWidgetView):
	def _on_edit(self, value_name):
		self._state_sync(value_name=value_name)

	def __present__(self, fragment, inh):
		def on_choice(control, new_choice):
			self._on_edit(new_choice)
			value_live.setLiteralValue(new_choice)

		self._incr.onAccess()
		value_name = unicode(self.value_name)
		value_live = LiveValue(value_name)
		radios = [RadioButton.radioButtonWithLabel(choice, choice, value_live, on_choice)   for choice in self.value_names]
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    self._radio_border.surround(Column(radios))])

	_radio_border = SolidBorder(1.0, 3.0, 4.0, 4.0, Color(0.85, 0.85, 0.85), None)



class SelectView (IPythonWidgetView):
	def _on_edit(self, value_name):
		self._state_sync(value_name=value_name)

	def __present__(self, fragment, inh):
		def on_choice(control, new_choice):
			self._on_edit(new_choice)
			value_live.setLiteralValue(new_choice)

		self._incr.onAccess()
		value_name = unicode(self.value_name)
		value_live = LiveValue(value_name)
		choices = list(self.value_names)
		select = ListSelect.listSelectWithLabels(choices, choices, value_live, on_choice)
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    select])



