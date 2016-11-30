package cn.leancloud.leanengine_sniper;

import java.util.LinkedList;
import java.util.List;

class RequestMetricItem {
  double responseTime;
  String url;
  String method;
  int statusCode;
  List<Double> additionRequestTime;

  public int getCount() {
    return additionRequestTime == null ? 1 : additionRequestTime.size() + 1;
  }

  public double getResponseTime() {
    double totalResponseTime = responseTime;
    if (additionRequestTime != null) {
      for (Double time : additionRequestTime) {
        totalResponseTime = totalResponseTime + time;
      }
    }
    return totalResponseTime / this.getCount();
  }

  public void setResponseTime(double responseTime) {
    this.responseTime = responseTime;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public void addRequestMetric(RequestMetricItem item) {
    if (additionRequestTime == null) {
      additionRequestTime = new LinkedList<Double>();
    }
    additionRequestTime.add(item.responseTime);
    if (item.additionRequestTime != null) {
      additionRequestTime.addAll(item.additionRequestTime);
    }
  }
}
