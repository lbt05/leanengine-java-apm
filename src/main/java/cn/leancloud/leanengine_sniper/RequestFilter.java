package cn.leancloud.leanengine_sniper;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.leancloud.leanengine_sniper.RequestRecord.RequestType;

@WebFilter(filterName = "requestUserAuthFilter", urlPatterns = {"/*"})
public class RequestFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {

  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      String servletPath = ((HttpServletRequest) request).getServletPath();
      String path = ((HttpServletRequest) request).getPathInfo();
      String method = ((HttpServletRequest) request).getMethod();
      RequestRecord info = new RequestRecord(servletPath, path, method, RequestType.REQUEST);
      chain.doFilter(request, response);
      info.end(((HttpServletResponse) response).getStatus());
      APM.addRequestInfo(info);
    } else {
      chain.doFilter(request, response);
    }
  }

  public void destroy() {

  }
}
