from BritefuryJ.Live import LiveValue

from BritefuryJ.Controls import RealSlider, ProgressBar, RealRangeSlider

from BritefuryJ.Pres.Primitive import Label, Row, Spacer

from LarchCore.ipython.widget import IPythonWidgetView



class FloatTextView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()

		value = float(self.value)
		return Row([Label(self.description), Spacer(10.0, 0.0), value])


class FloatSliderView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()
		def _on_change(control, new_value):
			sync_data = {'value': new_value}
			self._internal_update(sync_data)
			self.model.send_sync(sync_data)
			value_live.setLiteralValue(new_value)


		def _on_range_change(control, new_lower, new_upper):
			_on_change(control, (new_lower, new_upper))

		slider_min = float(self.min)
		slider_max = float(self.max)
		slider_step = float(self.step)
		if isinstance(self.value, tuple)  or  isinstance(self.value, list):
			value = (float(self.value[0]), float(self.value[1]))
			value_live = LiveValue(value)
			slider = RealRangeSlider(value_live, slider_min, slider_max, slider_step, 400.0, _on_range_change)
		else:
			value = float(self.value)
			value_live = LiveValue(value)
			slider = RealSlider(value_live, slider_min, slider_max, slider_step, 400.0, _on_change)
		return Row([Label(self.description), Spacer(10.0, 0.0),
			    slider,
			    Spacer(10.0, 0.0), value_live])


class ProgressView (IPythonWidgetView):
	def __present__(self, fragment, inh):
		self._incr.onAccess()

		value = float(self.value)
		value_live = LiveValue(value)
		slider_min = float(self.min)
		slider_max = float(self.max)
		return Row([Label(self.description), Spacer(10.0, 0.0),
				ProgressBar(value_live, slider_min, slider_max, 400.0),
			    Spacer(10.0, 0.0), value_live])




