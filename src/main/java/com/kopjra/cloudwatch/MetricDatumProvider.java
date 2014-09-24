package com.kopjra.cloudwatch;

import java.util.Collection;

import com.amazonaws.services.cloudwatch.model.MetricDatum;

public interface MetricDatumProvider {
	Collection<MetricDatum> getMetricDatumCollection();
}
