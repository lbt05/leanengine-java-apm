package cn.leancloud.leanengine_sniper;


class RequestMetricItem {
  double responseTime;
  String url;
  String method;
  int statusCode;
  int count;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public double getResponseTime() {
    return responseTime / (1000000.0 * this.getCount());
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
    responseTime = responseTime + item.responseTime;
    count = count + item.count;
  }
}
