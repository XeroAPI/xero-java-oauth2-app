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

import org.apache.http.NoHttpResponseException;
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
				System.out.println("Entries are null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

/*		for (CustomEntry customEntry : customEntryList) {
			for (CustomEntryItem customEntryItem : customEntry.getCustomEntryItems()) {
				if (customEntryItem.getTargetColumn() != null) {
					System.out.println("Le " + customEntryItem.getLabel() + " est définie à "
							+ customEntryItem.getTargetColumn() + " et sa valeur est " + customEntryItem.getValue());
				}
			}
		}*/

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
				session.setAttribute("entries", null);
				session.setAttribute("accounts", null);
				session.setAttribute("accountsUnparsed", null);
			} else {
				System.out.println("Exception: no transaction to send");
				throw new NoHttpResponseException("Account was not found");
			}

			PrintWriter out = response.getWriter();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			out.print(this.gson.toJson(bts));
			out.flush();
			return;
		}
		System.out.println("Exception");
		throw new NoHttpResponseException("Account was not found");
	}

}
