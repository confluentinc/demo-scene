package io.confluent.demo.util;

/*
 Helper class to hold count and sum of ratings
 */
public class CountAndSum<T1, T2> {

  public T1 count;
  public T2 sum;

  public CountAndSum(T1 count, T2 sum) {
    this.count = count;
    this.sum = sum;
  }

}
