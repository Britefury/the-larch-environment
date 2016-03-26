from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import Button, Checkbox, IntSlider, ToggleButton

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer

from LarchCore.ipython.widget import IPythonWidgetView


class CheckboxView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			self._state_sync(value=new_value)
			value_live.setLiteralValue(new_value)

		value = bool(self.value)
		value_live = LiveValue(value)
		return Checkbox.checkboxWithLabel(self.description, value_live, _on_change)


class ToggleButtonView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			self._state_sync(value=new_value)
			value_live.setLiteralValue(new_value)

		value = bool(self.value)
		value_live = LiveValue(value)
		return ToggleButton.toggleButtonWithLabel(self.description, value_live, _on_change)

