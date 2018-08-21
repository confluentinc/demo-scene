package com.github.megachucky.kafka.streams.machinelearning;

import io.confluent.ksql.function.udf.Udf;
import io.confluent.ksql.function.udf.UdfDescription;

@UdfDescription(name = "multiply", description = "multiplies 2 numbers")
public class Multiply {

  @Udf(description = "multiply two non-nullable INTs.")
  public long multiply(final int v1, final int v2) {
    return v1 * v2;
  }

  @Udf(description = "multiply two non-nullable BIGINTs.")
  public long multiply(final long v1, final long v2) {
    return v1 * v2;
  }

  @Udf(description = "multiply two nullable BIGINTs. If either param is null, null is returned.")
  public Long multiply(final Long v1, final Long v2) {
    return v1 == null || v2 == null ? null : v1 * v2;
  }

  @Udf(description = "multiply two non-nullable DOUBLEs.")
  public double multiply(final double v1, double v2) {
    return v1 * v2;
  }
}

