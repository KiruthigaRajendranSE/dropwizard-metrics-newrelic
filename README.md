# New Relic Dropwizard Reporter
A [Dropwizard metrics](https://metrics.dropwizard.io/4.0.0/) reporter for sending dimensional metrics to New Relic using the New Relic Java Telemetry SDK.
For the juicy details on how Dropwizard metrics are mapped to New Relic dimensional metrics,
please visit [the exporter specs documentation repo](https://github.com/newrelic/exporter-specs/tree/master/dropwizard). 

# How To Use

## gradle

`build.gradle:`
```
compile("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.1.1")
compile("com.newrelic.telemetry:telemetry-http-okhttp:0.3.0")
```

or if you're using kotlin build gradle...

`build.gradle.kts:`
```
implementation("com.newrelic.telemetry:dropwizard-metrics-newrelic:0.1.1")
implementation("com.newrelic.telemetry:telemetry-http-okhttp:0.3.0")
```

If you do not want to depend on okhttp, you can remove the dependency on `telemetry-http-okhttp`, 
but you will need to construct a `MetricBatchSender` instance using its builder and provide your
own implementation of the `com.newrelic.telemetry.http.HttpPoster` interface.


```
MetricBatchSender sender = MetricBatchSender.builder().httpPoster(<your implementation>);
```

Note: to use the sample code below, you will need the `telemetry-http-okhttp` library mentioned above. It provides
implementations communicating via HTTP using the okhttp libraries, respectively.

## Start The Reporter

Early in the lifecycle of your application, you will want to create and
start a `NewRelicReporter`, similar to this:

```
MetricRegistry metricRegistry = new MetricRegistry(); // If you're already using dropwizard-metrics you may already have one of these.
...
String apiKey = "<YOUR_SECRET_API_KEY>";
MetricBatchSender metricBatchSender = SimpleMetricBatchSender.builder(apiKey).build();

Attributes commonAttributes = new Attributes()
            .put("host", InetAddress.getLocalHost().getHostName())
            .put("appName", "Your Application Name Here")
            .put("other", "any other common attributes you wish");
            
NewRelicReporter reporter = NewRelicReporter.build(metricRegistry, metricBatchSender)
        .commonAttributes(commonAttributes)
        .build();
        
reporter.start(15, TimeUnit.SECONDS);
```

## Dropwizard Integration

If you are using the actual Dropwizard REST framework, you can get a reference to the 
`MetricRegistry` in order to register you reporter.  It might like something like this:


```$java
public class MyApplication extends Application<MyConfig> {

...
    @Override
    public void run(MyConfig configuration, Environment environment) {
        MetricRegistry registry = environment.metrics();
        MetricBatchSender metricBatchSender = buildMetricBatchSender(); // see above for more complete example
        NewRelicReporter reporter = NewRelicReporter.build(registry, metricBatchSender)
                .commonAttributes(commonAttributes)
                .build();
        reporter.start(15, TimeUnit.SECONDS);

        ...
    }
...
}
``` 