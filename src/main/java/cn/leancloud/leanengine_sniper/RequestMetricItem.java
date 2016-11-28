package cn.leancloud.leanengine_sniper;

class RequestMetricItem {
  int count;
  double responseTime;
  String url;
  String method;
  int statusCode;

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public double getResponseTime() {
    return responseTime;
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
    double currentResponseTimeSum = this.count * this.responseTime;
    this.count = this.count + item.count;
    this.responseTime = (currentResponseTimeSum + item.responseTime * item.count) / count;
  }
}
