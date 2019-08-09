/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.codahale.metrics.newrelic.transformer.interfaces;

import static java.util.stream.Collectors.toSet;

import com.codahale.metrics.Clock;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricAttribute;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MeteredTransformer implements DropWizardComponentTransformer<Metered> {

  private final Clock clock;
  private final long rateFactor;

  public MeteredTransformer(long rateFactor) {
    this(Clock.defaultClock(), rateFactor);
  }

  // exists for testing
  MeteredTransformer(Clock clock, long rateFactor) {
    this.clock = clock;
    this.rateFactor = rateFactor;
  }

  @Override
  public Collection<Metric> transform(
      String name, Metered metered, Supplier<Attributes> baseAttributes) {
    long timestamp = clock.getTime();

    Gauge mean =
        makeGauge(
            name,
            timestamp,
            convertRate(metered.getMeanRate()),
            MetricAttribute.MEAN_RATE,
            baseAttributes);
    Gauge oneMinuteRate =
        makeGauge(
            name,
            timestamp,
            convertRate(metered.getOneMinuteRate()),
            MetricAttribute.M1_RATE,
            baseAttributes);
    Gauge fiveMinuteRate =
        makeGauge(
            name,
            timestamp,
            convertRate(metered.getFiveMinuteRate()),
            MetricAttribute.M5_RATE,
            baseAttributes);
    Gauge fifteenMinuteRate =
        makeGauge(
            name,
            timestamp,
            convertRate(metered.getFifteenMinuteRate()),
            MetricAttribute.M15_RATE,
            baseAttributes);

    return Stream.of(mean, oneMinuteRate, fiveMinuteRate, fifteenMinuteRate).collect(toSet());
  }

  private double convertRate(double meanRate) {
    return rateFactor * meanRate;
  }

  private Gauge makeGauge(
      String name,
      long timestamp,
      double count,
      MetricAttribute attribute,
      Supplier<Attributes> attributes) {
    return new Gauge(
        name,
        count,
        timestamp,
        attributes.get().put("rate", attribute.getCode()).put("groupingAs", "rates"));
  }
}
