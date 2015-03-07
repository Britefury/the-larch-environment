from java.awt import Color

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import SwitchButton, OptionMenu, RadioButton, ListSelect

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer, Column

from LarchCore.ipython.widget import IPythonWidgetView



class ToggleButtonsView (IPythonWidgetView):
	def _on_edit(self, selected_label):
		self._state_sync(selected_label=selected_label)

	def __present__(self, fragment, inh):
		def on_choice(control, prev_index, new_index):
			selected_label = self._options_labels[new_index]
			self._on_edit(selected_label)
			value_live.setLiteralValue(new_index)

		self._incr.onAccess()
		selected_label = unicode(self.selected_label)
		value_live = LiveValue(self._options_labels.index(selected_label))
		choices = [Label(choice)   for choice in self._options_labels]
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    SwitchButton(choices, choices, SwitchButton.Orientation.HORIZONTAL, value_live, on_choice)])



class DropdownView (IPythonWidgetView):
	def _on_edit(self, selected_label):
		self._state_sync(selected_label=selected_label)

	def __present__(self, fragment, inh):
		def on_choice(control, prev_index, new_index):
			selected_label = self._options_labels[new_index]
			self._on_edit(selected_label)
			value_live.setLiteralValue(new_index)

		self._incr.onAccess()
		selected_label = unicode(self.selected_label)
		value_live = LiveValue(self._options_labels.index(selected_label))
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    OptionMenu([Label(choice)   for choice in self._options_labels], value_live, on_choice)])



class RadioButtonsView (IPythonWidgetView):
	def _on_edit(self, selected_label):
		self._state_sync(selected_label=selected_label)

	def __present__(self, fragment, inh):
		def on_choice(control, new_choice):
			self._on_edit(new_choice)
			value_live.setLiteralValue(new_choice)

		self._incr.onAccess()
		selected_label = unicode(self.selected_label)
		value_live = LiveValue(selected_label)
		radios = [RadioButton.radioButtonWithLabel(choice, choice, value_live, on_choice)   for choice in self._options_labels]
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    self._radio_border.surround(Column(radios))])

	_radio_border = SolidBorder(1.0, 3.0, 4.0, 4.0, Color(0.85, 0.85, 0.85), None)



class SelectView (IPythonWidgetView):
	def _on_edit(self, selected_label):
		self._state_sync(selected_label=selected_label)

	def __present__(self, fragment, inh):
		def on_choice(control, new_choice):
			self._on_edit(new_choice)
			value_live.setLiteralValue(new_choice)

		self._incr.onAccess()
		selected_label = unicode(self.selected_label)
		value_live = LiveValue(selected_label)
		choices = list(self._options_labels)
		select = ListSelect.listSelectWithLabels(choices, choices, value_live, on_choice)
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    select])



