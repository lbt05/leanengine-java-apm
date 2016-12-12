package cn.leancloud.leanengine_sniper;


class RequestMetricItem {
  double responseTime;
  String url;
  String method;
  int statusCode;
  int count;

  public RequestMetricItem(String url, String method, int status, int count, double responseTime) {
    this.responseTime = responseTime;
    this.url = url;
    this.method = method;
    this.statusCode = status;
    this.count = count;
  }

  public int getCount() {
    return count;
  }

  public double getResponseTime() {
    return responseTime / (1000000.0 * this.getCount());
  }

  public String getUrl() {
    return url;
  }

  public String getMethod() {
    return method;
  }


  public int getStatusCode() {
    return statusCode;
  }

  public void addRequestMetric(RequestMetricItem item) {
    responseTime = responseTime + item.responseTime;
    count = count + item.count;
  }
}
