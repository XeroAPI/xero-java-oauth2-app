package com.xero.app;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenStorage {

    public TokenStorage() {
        super();
    }

    public String get(HttpServletRequest request, String key) {
        String item = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(key)) {
                    item = cookies[i].getValue();
                }
            }
        }
        return item;
    }

    public void clear(HttpServletResponse response) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("jwt_token", "");
        map.put("id_token", "");        
        map.put("access_token", "");
        map.put("refresh_token", "");
        map.put("expires_in_seconds", "");
        map.put("xero_tenant_id", "");

        save(response, map);
    }

    public void saveItem(HttpServletResponse response, String key, String value) {
        Cookie t = new Cookie(key, value);
        response.addCookie(t);
    }

    public void save(HttpServletResponse response, HashMap<String, String> map) {
        Set<Entry<String, String>> set = map.entrySet();
        Iterator<Entry<String, String>> iterator = set.iterator();

        while (iterator.hasNext()) {
            Map.Entry<?, ?> mentry = iterator.next();
            String key = (String) mentry.getKey();
            String value = (String) mentry.getValue();

            Cookie t = new Cookie(key, value);
            response.addCookie(t);
        }
    }
}