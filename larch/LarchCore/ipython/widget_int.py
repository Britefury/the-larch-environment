from BritefuryJ.Live import LiveValue

from BritefuryJ.Controls import IntSlider, IntRangeSlider

from BritefuryJ.Pres.Primitive import Label, Row, Spacer

from LarchCore.ipython.widget import IPythonWidgetView



class IntTextView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()

		value = int(self.value)
		return Row([Label(self.description), Spacer(10.0, 0.0), value])


class IntSliderView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			sync_data = {'value': new_value}
			self._internal_update(sync_data)
			self.model.send_sync(sync_data)
			value_live.setLiteralValue(new_value)

		def _on_range_change(control, new_lower, new_upper):
			_on_change(control, (new_lower, new_upper))

		slider_min = int(self.min)
		slider_max = int(self.max)
		slider_step = int(self.step)
		if isinstance(self.value, tuple)  or  isinstance(self.value, list):
			value = (int(self.value[0]), int(self.value[1]))
			value_live = LiveValue(value)
			slider = IntRangeSlider(value_live, slider_min, slider_max, slider_step, 400.0, _on_range_change)
		else:
			value = int(self.value)
			value_live = LiveValue(value)
			slider = IntSlider(value_live, slider_min, slider_max, slider_step, 400.0, _on_change)
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    slider,
			    Spacer(10.0, 0.0), value_live])
