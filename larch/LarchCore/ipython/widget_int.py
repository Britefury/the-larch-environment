from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue, LiveFunction

from BritefuryJ.Controls import Button, Checkbox, IntSlider, ToggleButton

from BritefuryJ.Pres import Pres
from BritefuryJ.Pres.Primitive import Blank, Label, Row, Spacer

from LarchCore.ipython.widget import IPythonWidgetView



class IntSliderView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			sync_data = {'value': new_value}
			self._internal_update(sync_data)
			self.model.send({'method': 'backbone', 'sync_data': sync_data})
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


