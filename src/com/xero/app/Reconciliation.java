package com.xero.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

@WebServlet(urlPatterns = "/reconciliation")
@MultipartConfig
public class Reconciliation extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Gson gson = new Gson();

    public Reconciliation() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.getServletContext().getRequestDispatcher("/reconciliation.jsp").forward(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        try {
            System.out.println("Je suis la! => " + request.getParameter("position"));
            Integer position = Integer.valueOf(request.getParameter("position"));
            Map<String, String> entry = (Map<String, String>) session.getAttribute("currentEntry");
            List<Map<String, String>> entries = (List<Map<String, String>>) session.getAttribute("entries");
            if (entries != null) {
                if (entry == null || position <= 0 || position > entries.size()) {
                    System.out.println("Position 1 => " + position + " Size => " + entries.size());
                    entry = entries.get(position >= entries.size() ? entries.size() - 1 : 0);
                } else {
                    System.out.println("Position 2 => " + (position - 1) + " Size => " + entries.size());
                    entry = entries.get(position - 1);
                    System.out.println(entries.get(1));
                }


                PrintWriter out = response.getWriter();
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                out.print(this.gson.toJson(entry));
                out.flush();

            } else {
                System.out.println("Il y'a un gros null ici !");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
