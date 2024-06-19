
package com.endside.util;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class WebUtil {

  /**
   * 요청한 client 의 ip 를 조회 한다.
   *
   * @method getClientIp
   * @see
   * @param request
   * @return String
   */
  public String getClientIp(HttpServletRequest request) {
    String clientIp = request.getHeader("X-Forwarded-For");

    if(clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
      clientIp = request.getHeader("Proxy-Client-IP");
    }
    if(clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
        clientIp = request.getHeader("WL-Proxy-Client-IP");
    }
    if(clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
        clientIp = request.getHeader("HTTP_CLIENT_IP");
    }
    if(clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
        clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if(clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
        clientIp = request.getRemoteAddr();
    }

    return clientIp;
  }
}
