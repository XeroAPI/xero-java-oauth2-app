package com.xero.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.xero.api.ApiClient;
import com.xero.api.client.AccountingApi;
import com.xero.app.models.CustomEntry;
import com.xero.app.models.CustomEntryItem;
import com.xero.models.accounting.Account;
import com.xero.models.accounting.BankTransaction;
import com.xero.models.accounting.BankTransactions;
import com.xero.models.accounting.Contact;
import com.xero.models.accounting.Contacts;
import com.xero.models.accounting.LineItem;

@WebServlet(urlPatterns = "/reconciliation")
@MultipartConfig
public class Reconciliation extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private Gson gson = new Gson();
	private AccountingApi accountingApi = null;

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

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = request.getReader();
		String line;
		while ((line = reader.readLine()) != null) {
			buffer.append(line);
			buffer.append(System.lineSeparator());
		}
		String data = buffer.toString();

		JSONArray entries = new JSONArray(data);

		List<CustomEntry> customEntryList = new ArrayList<CustomEntry>();

		HttpSession session = request.getSession(false);

		if (entries.length() >= 1) {
			for (int i = 0; i < entries.length(); ++i) {
				CustomEntry customEntry = new CustomEntry();
				JSONArray entry = entries.getJSONArray(i);
				if (entries.length() >= 1) {
					for (int j = 0; j < entry.length(); ++j) {
						CustomEntryItem customEntryItem = new CustomEntryItem();
						JSONObject jsonObject = entry.getJSONObject(j);
						if (jsonObject.has("label"))
							customEntryItem.setLabel(jsonObject.getString("label"));
						if (jsonObject.has("fieldName"))
							customEntryItem.setFieldName(jsonObject.getString("fieldName"));
						if (jsonObject.has("targetColumn"))
							customEntryItem.setTargetColumn(jsonObject.getString("targetColumn"));
						if (jsonObject.has("value"))
							customEntryItem.setValue(jsonObject.getString("value"));
						customEntry.addItem(customEntryItem);
					}
				}
				customEntryList.add(customEntry);
			}
		}

		for (CustomEntry customEntry : customEntryList) {
			for (CustomEntryItem customEntryItem : customEntry.getCustomEntryItems()) {
				if (customEntryItem.getTargetColumn() != null) {
					System.out.println("Le " + customEntryItem.getLabel() + " est définie à "
							+ customEntryItem.getTargetColumn() + " et sa valeur est " + customEntryItem.getValue());
				}
			}
		}

		if (session.getAttribute("xero_tenant_id") == null || session.getAttribute("access_token") == null) {
			session.setAttribute("access_token",
					"eyJhbGciOiJSUzI1NiIsImtpZCI6IjFDQUY4RTY2NzcyRDZEQzAyOEQ2NzI2RkQwMjYxNTgxNTcwRUZDMTkiLCJ0eXAiOiJKV1QiLCJ4NXQiOiJISy1PWm5jdGJjQW8xbkp2MENZVmdWY09fQmsifQ.eyJuYmYiOjE2NDM3MTQxMzIsImV4cCI6MTY0MzcxNTkzMiwiaXNzIjoiaHR0cHM6Ly9pZGVudGl0eS54ZXJvLmNvbSIsImF1ZCI6Imh0dHBzOi8vaWRlbnRpdHkueGVyby5jb20vcmVzb3VyY2VzIiwiY2xpZW50X2lkIjoiMEU4ODIxMjlERkM3NDZCRjlDODI5MTAyMkIwMDZCNTkiLCJzdWIiOiJkMTFmMDI5YTlkNjA1ZmQ4YmE1ZTk0MThlMGYxYWMxMSIsImF1dGhfdGltZSI6MTY0MzcxMzMxNiwieGVyb191c2VyaWQiOiJmOThlMTY2OC0xZTczLTRiODQtYjU2OC1hNzkxYTVhOTE1ZDQiLCJnbG9iYWxfc2Vzc2lvbl9pZCI6IjM5ODljMzk5ODlmYTQ0YzY5NzA5Njk5NDZiNmQ5YTYwIiwianRpIjoiOTkzMzUwZGNmNDRlNGU3Mjk0NDIwMDE3NWUyMGM0ODQiLCJhdXRoZW50aWNhdGlvbl9ldmVudF9pZCI6IjVkOGE0MTIyLTM5NjktNDQzNS1iYmUwLTM4YTQ3N2NhYTE4ZiIsInNjb3BlIjpbImVtYWlsIiwicHJvZmlsZSIsIm9wZW5pZCIsImFjY291bnRpbmcucmVwb3J0cy5yZWFkIiwicGF5cm9sbC5lbXBsb3llZXMiLCJwYXlyb2xsLnBheXJ1bnMiLCJwYXlyb2xsLnBheXNsaXAiLCJwYXlyb2xsLnRpbWVzaGVldHMiLCJwcm9qZWN0cyIsImFjY291bnRpbmcuc2V0dGluZ3MiLCJhY2NvdW50aW5nLmF0dGFjaG1lbnRzIiwiYWNjb3VudGluZy50cmFuc2FjdGlvbnMiLCJhY2NvdW50aW5nLmpvdXJuYWxzLnJlYWQiLCJhc3NldHMiLCJhY2NvdW50aW5nLmNvbnRhY3RzIiwicGF5cm9sbC5zZXR0aW5ncyIsImFjY291bnRpbmcuYnVkZ2V0cy5yZWFkIiwib2ZmbGluZV9hY2Nlc3MiXX0.AJWxF-71gkhmt5okw6WHfSdB0B3DzhhyEizu-cKelopTiHsJp4JSJMZ_qSfsDW2d2r0GwcxpzGy_c7qLNKMkjJnsAYjMP3jIJTlN6A3Yy0IBp5oQkATElHJU4g5c653ik03kNjXImF5GJj1_SrOzbNdh0jLx7TQk5oKOCjUPMQvra8L33P6sASnDdlw4QlfTV7lNksbcuzMWOl2af-TYogxaFj7gQ_4DXWxG6pMwFGu85wRh0eQprGBhV2BQn041Vg-sDZEEt6Alx-yQXPQrbG5PxoPIzQnwlNSkvfqIaQztQHVe2nL8dknCqnjzPPej5COoiBtTAZ5xY5Ms0ZniZw");
			session.setAttribute("xero_tenant_id", "c40dee4f-adae-42ef-82dd-bfd48c58f330");
		}

		List<Account> accountList = (List<Account>) session.getAttribute("accountsUnparsed");

		Account account = accountList.stream()
				.filter(a -> a.getAccountID().toString().equals(request.getParameter("accountID")))
				.collect(Collectors.toList()).get(0);

		ApiClient defaultClient = new ApiClient();
		defaultClient.setConnectionTimeout(6000);
		accountingApi = AccountingApi.getInstance(defaultClient);

		Contacts contacts = accountingApi.getContacts(session.getAttribute("access_token").toString(),
				session.getAttribute("xero_tenant_id").toString(), null, null, null, new ArrayList<UUID>(), null, null,
				null, null);
		Contact useContact = new Contact();
		if (contacts.getContacts().size() > 0) {
			useContact.setContactID(contacts.getContacts().get(0).getContactID());
		}

		// Maker sure we have at least 1 bank
		if (account != null) {
			Account bankAcct = new Account();
			bankAcct.setAccountID(account.getAccountID());
			BankTransactions bts = new BankTransactions();

			for (CustomEntry customEntry : customEntryList) {
				CustomEntryItem amountCode = customEntry.getCustomEntryItems().stream()
						.filter(cei -> "amountCode".equals(cei.getTargetColumn())).findFirst().orElse(null);
				CustomEntryItem unitAmount = customEntry.getCustomEntryItems().stream()
						.filter(cei -> "transactionAmount".equals(cei.getTargetColumn())).findFirst().orElse(null);
				CustomEntryItem description = customEntry.getCustomEntryItems().stream()
						.filter(cei -> "description".equals(cei.getTargetColumn())).findFirst().orElse(null);

				if (amountCode != null && unitAmount != null && description != null) {
					List<LineItem> lineItems = new ArrayList<>();

					LineItem li = new LineItem();
					li.setAccountCode(amountCode.getValue());
					li.setDescription(description.getValue());
					li.setUnitAmount(Double.valueOf(unitAmount.getValue()));
					lineItems.add(li);

					BankTransaction bt = new BankTransaction();
					bt.setBankAccount(bankAcct);
					bt.setContact(useContact);
					bt.setLineItems(lineItems);
					bt.setType(Double.valueOf(unitAmount.getValue()) < 0
							? com.xero.models.accounting.BankTransaction.TypeEnum.SPEND
							: com.xero.models.accounting.BankTransaction.TypeEnum.RECEIVE);

					bts.addBankTransactionsItem(bt);
				}
			}

			if (bts.getBankTransactions().size() > 0) {
//				BankTransactions newBankTransactions = accountingApi.createBankTransactions(
//						session.getAttribute("access_token").toString(),
//						session.getAttribute("xero_tenant_id").toString(), bts, false, 4);

//				System.out.println("Create new BankTransactions: count: " + newBankTransactions.getBankTransactions().size());
				System.out.println(bts.getBankTransactions().size() + " transactions viables trouvées !");
			} else {
				System.out.println("Aucune transaction viable trouvée !");
			}

		}

		PrintWriter out = response.getWriter();
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		out.print(this.gson.toJson(customEntryList));
		out.flush();
	}

}
