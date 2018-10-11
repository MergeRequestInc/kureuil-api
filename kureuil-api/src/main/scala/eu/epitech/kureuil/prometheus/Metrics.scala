package eu.epitech.kureuil
package prometheus

import kamon.prometheus.PrometheusReporter
import org.lyranthe.prometheus.client._
import org.lyranthe.prometheus.client.gauge.Gauge1
import org.lyranthe.prometheus.client.histogram.Histogram1

object Metrics {
  val kamonReporter: PrometheusReporter = new PrometheusReporter

  implicit val defaultRegistry: Registry = DefaultRegistry()
  private val help: String               = "Latency in seconds"

  private def createHistogram( metric: MetricName ): LabelledHistogram =
    Histogram( metric, help )(
      HistogramBuckets(0.005, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5, 0.75, 1, 2.5, 5, 7.5, 10 )
    ).labels().register
  // Api
  val inProgressRequests: Gauge1 =
    Gauge( metric"api_inprogress_requests", "Inprogress requests." ).labels( label"requests" ).register
  val requestsLatencySeconds: LabelledHistogram = createHistogram( metric"api_requests_latency_seconds" )

  // StreamingSupport
  val streamingLatency: Histogram1 =
    Histogram( metric"api_streaming_action_latency_seconds", help )(
      HistogramBuckets(0.005, 0.01, 0.025, 0.05, 0.075, 0.1, 0.25, 0.5, 0.75, 1, 2.5, 5, 7.5, 10 )
    ).labels( label"streamtype" ).register
}
