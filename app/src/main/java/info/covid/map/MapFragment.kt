package info.covid.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.core.map.series.Choropleth
import com.anychart.enums.SelectionMode
import com.anychart.enums.SidePosition
import com.anychart.scales.LinearColor
import info.covid.databinding.FragmentMapBinding
import info.covid.utils.Const
import info.covid.utils.toNumber


class MapFragment : Fragment() {
    private lateinit var binding: FragmentMapBinding
    private val viewModel by viewModels<MapViewModel>()
    private lateinit var series: Choropleth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.anyChartView.setProgressBar(binding.progressBar)

        val map = AnyChart.map()
        map.geoData("anychart.maps.india")


        map.colorRange().apply {
            enabled(true)
                .colorLineSize(10)
                .stroke("#B9B9B9")
                .labels("{ 'padding': 3 }")
                .labels("{ 'size': 7 }")

            ticks()
                .enabled(true)
                .stroke("#B9B9B9")
                .position(SidePosition.OUTSIDE)
                .length(10)

            minorTicks()
                .enabled(true)
                .stroke("#B9B9B9")
                .position("outside")
                .length(5)

            orientation("top")

            padding(30, 0, 0, 0)
        }


        map.interactivity().selectionMode(SelectionMode.NONE)

        map.padding(10, 10, 80, 10)

        series = map.choropleth(getData())

        val linearColor = LinearColor.instantiate()

        linearColor.colors(arrayOf("#c2e9fb", "#81d4fa", "#01579b", "#002746"))
        series.colorScale(linearColor)

        series.hovered().apply {
            fill("#f48fb1")
            stroke("#f99fb9")
        }

        series.selected().apply {
            fill("#c2185b")
            stroke("#c2185b")
        }

        series.labels().apply {
            enabled(true)
            fontSize(11)
            fontColor("#000000")
            format("{%Value}")
        }


        series.tooltip()
            .useHtml(true)
            .format(
                ("function() {\n" +
                        "  return '<span style=\"font-size: 13px\">' + this.value + ' Active</span>';\n" +
                        "}")
            )

        binding.anyChartView.addScript("file:///android_asset/india.js")
        binding.anyChartView.addScript("file:///android_asset/proj4.js")
        binding.anyChartView.setChart(map)

        viewModel.states.observe(viewLifecycleOwner, Observer {

            val list = arrayListOf<DataEntry>()

            it.forEach { state ->
                list.add(
                    CustomDataEntry(
                        "IN." + Const.statesCodes[state.state ?: ""],
                        state.state ?: "",
                        state.active.toNumber()
                    )
                )
            }

            series.data(list)
        })
    }


    private fun getData(): List<DataEntry> {
        val data = ArrayList<DataEntry>()
        data.add(CustomDataEntry("IN.DUMYY", "DUMMY", 0))
        return data
    }

    internal inner class CustomDataEntry : DataEntry {
        constructor(id: String, name: String, value: Number) {
            setValue("id", id)
            setValue("name", name)
            setValue("value", value)
        }

        constructor(id: String, name: String, value: Number, label: LabelDataEntry) {
            setValue("id", id)
            setValue("name", name)
            setValue("value", value)
            setValue("label", label)
        }
    }

    internal inner class LabelDataEntry(enabled: Boolean?) : DataEntry() {
        init {
            setValue("enabled", enabled)
        }
    }
}