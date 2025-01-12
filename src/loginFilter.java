import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class loginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        System.out.println("LoginFilter: Requested URI = " + requestURI);

        // Allow specific public URLs without login
        if (this.isUrlAllowedWithoutLogin(requestURI)) {
            System.out.println("URL allowed without login: " + requestURI);
            chain.doFilter(request, response);
            return;
        }

        // Retrieve the User object from the session
        User user = (User) httpRequest.getSession().getAttribute("user");

        // Redirect to login page if user is not logged in
        if (user == null) {
            System.out.println("User not found in session. Redirecting to main login page.");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
            return;
        }

        // Restrict access to /_dashboard to employees only
        if (requestURI.contains("/_dashboard") && !"employee".equals(user.getRole())) {
            System.out.println("Access denied for non-employee user to employee dashboard.");
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard/login.html");
            return;
        }

        // User is authenticated and authorized, proceed with request
        System.out.println("User found in session with role: " + user.getRole() + ". Proceeding with request.");
        chain.doFilter(request, response);
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        return allowedURIs.stream().anyMatch(requestURI::endsWith);
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("login.js");
        allowedURIs.add("api/login");
        allowedURIs.add("api/employee-login");
        System.out.println("Allowed URIs for LoginFilter: " + allowedURIs);
    }

    public void destroy() {
        // ignored.
    }
}
