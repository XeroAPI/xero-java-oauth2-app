package com.xero.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xero.api.ApiClient;
import com.xero.api.client.IdentityApi;
import com.xero.models.identity.Connection;

@WebServlet("/Logout")
public class Logout extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public Logout() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            if (session.getAttribute("xero_tenant_id") == null
                    || session.getAttribute("id_token") == null
                    || session.getAttribute("jwt_token") == null
                    || session.getAttribute("access_token") == null
                    || session.getAttribute("refresh_token") == null
                    || session.getAttribute("expires_in_seconds") == null
                    || session.getAttribute("xero_tenant_id") == null
                    || session.getAttribute("connection_tenant_id") == null
            ) {
                response.sendRedirect("./");
                return;
            }
        } else {
            response.sendRedirect("./");
            return;
        }

        ApiClient defaultIdentityClient = new ApiClient("https://api.xero.com", null, null, null, null);
        System.out.println(defaultIdentityClient.toString());
        IdentityApi idApi = new IdentityApi(defaultIdentityClient);

        //idApi.deleteConnection(session.getAttribute("access_token").toString(), session.getAttribute("connection_tenant_id"));

        response.sendRedirect("./");
    }
}