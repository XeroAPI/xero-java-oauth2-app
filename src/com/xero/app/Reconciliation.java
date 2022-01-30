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

    @SuppressWarnings("unchecked")
	@Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        try {
            Integer position = Integer.valueOf(request.getParameter("position"));
            List<Map<String, String>> entry;
            List<List<Map<String, String>>> entries = (List<List<Map<String, String>>>) session.getAttribute("entries");
            if (entries != null) {
                if (session.getAttribute("currentEntry") == null || position <= 0 || position > entries.size()) {
                    entry = entries.get(position >= entries.size() ? entries.size() - 1 : 0);
                } else {
                    entry = entries.get(position - 1);
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
