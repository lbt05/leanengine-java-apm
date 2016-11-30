package cn.leancloud.leanengine_sniper;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.avos.avoscloud.AVUtils;


class RequestRecord {
  String path;
  String context;
  String method;
  RequestType type;
  long startedAt;
  long endedAt;
  int statusCode;

  public enum RequestType {
    REQUEST, CLOUDAPI;
    public String toString() {
      switch (this) {
        case REQUEST:
          return "request";
        case CLOUDAPI:
          return "cloudApi";
        default:
          return "";
      }
    }
  }

  public RequestRecord(String path, String method, RequestType type) {
    startedAt = System.nanoTime();
    this.method = method;
    this.path = path;
    this.type = type;
  }

  public RequestRecord(String servletPath, String pathInfo, String method, RequestType type) {
    this(servletPath, method, type);
    this.context = pathInfo;
  }

  public void end(int statusCode) {
    endedAt = System.nanoTime();
    this.statusCode = statusCode;
  }

  public double getResponseTime() {
    return (endedAt - startedAt) / 1000000.0;
  }

  public RequestMetricItem metric() {
    RequestMetricItem item = new RequestMetricItem();
    item.setResponseTime(getResponseTime());
    item.setStatusCode(statusCode);
    item.setMethod(method);
    item.setUrl(method + " " + parseRequestPath(path, context));
    return item;
  }


  private static Pattern resourcePattern = Pattern
      .compile("^.*\\.(css|js|jpe?g|gif|png|woff2?|ico)$");

  private String parseRequestPath(String servletPath, String pathInfo) {
    String path = servletPath;
    switch (type) {
      case REQUEST:
        if (pathInfo != null) {
          path = path + pathInfo;
        }
        Matcher matcher = resourcePattern.matcher(path);
        if (matcher.matches()) {
          return "*." + matcher.group(1);
        } else {
          String[] paths = path.split("/");
          if (paths.length == 4 && ("call".equals(paths[2]) || "functions".equals(paths[2]))) {
            return path;
          }
        }
        break;
      case CLOUDAPI:
        String[] paths = servletPath.split("/");
        if (paths.length <= 4) {
          return servletPath;
        } else {
          paths[paths.length - 1] = ":id";
          return AVUtils.joinCollection(Arrays.asList(paths), "/");
        }
    }
    if (pathInfo != null) {
      return servletPath + "/*";
    }

    return path;
  }
}
