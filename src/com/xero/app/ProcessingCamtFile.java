package com.xero.app;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.xero.api.ApiClient;
import com.xero.api.client.IdentityApi;
import com.xero.app.models.Document;
import com.xero.app.models.ReportEntry2;
import com.xero.models.identity.Connection;

// Get camt file and convert to CSV
@WebServlet(urlPatterns = "/upload")
@MultipartConfig
public class ProcessingCamtFile extends HttpServlet {

	private static final long serialVersionUID = 1273074928096412095L;

	public static final String FILES_FOLDER = "/Images";

	public String uploadPath;

	public ProcessingCamtFile() {
		super();
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {

		Part part = null;

		if (request.getParts().size() > 0)
			part = request.getParts().iterator().next();
		else
			return;

		String fileName = getFileName(part);
		// String fullPath = uploadPath + File.separator + fileName;
		System.out.println("Filename => " + fileName);
		System.out.println("p.getName() => " + part.getName());
		// part.write(fullPath);

		try {
			System.out.println(System.getProperty("user.dir"));
			File stylesheet = new File(
					"/home/descartes/eclipse/workspace/xero-swiss-camt05x-converter-java/src/com/xero/converter/process.xsl");
			System.out.println("Premier fichier lu");
			File xmlSource = new File(
					"/home/descartes/eclipse/workspace/xero-swiss-camt05x-converter-java/src/com/xero/converter/camt.053.xml");
			System.out.println("Deuxieme fichier lu");

			/*
			 * DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			 * DocumentBuilder builder = factory.newDocumentBuilder(); Document document =
			 * builder.parse(xmlSource);
			 * 
			 * StreamSource stylesource = new StreamSource(stylesheet); Transformer
			 * transformer = TransformerFactory.newInstance() .newTransformer(stylesource);
			 * Source source = new DOMSource(document); Result outputTarget = new
			 * StreamResult(new File("/home/descartes/Desktop/x.csv"));
			 * transformer.transform(source, outputTarget);
			 */

			try {
				File file = new File("/home/descartes/eclipse/workspace/xero-swiss-camt05x-converter-java/src/com/xero/converter/camt.053.xml");
				JAXBContext jaxbContext = JAXBContext.newInstance(Document.class);

				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				Document document = (Document) jaxbUnmarshaller.unmarshal(file);

				List<ReportEntry2> list = document.getBkToCstmrStmt().getStmt().get(0).getNtry();
				for (ReportEntry2 ntry : list)
					System.out.println("AMOUNT => "+ntry.getAmt().getValue());

			} catch (JAXBException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		resp.sendRedirect("./AuthenticatedResource");
		// TODO: Start the convert process here
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("Processing here...");
		ApiClient defaultIdentityClient = new ApiClient("https://api.xero.com", null, null, null, null);
		System.out.println("Processing here 1...");
		IdentityApi idApi = new IdentityApi(defaultIdentityClient);
		System.out.println("Processing here 2...");
		TokenStorage store = new TokenStorage();
		System.out.println("id_token from Authorization => " + store.get(request, "id_token"));
		System.out.println("jwt_token from Authorization => " + store.get(request, "jwt_token"));
		System.out.println("access_token from Authorization => " + store.get(request, "access_token"));
		List<Connection> connections = idApi.getConnections(store.get(request, "access_token"), null);
		if (connections.size() > 0)
			for (Connection connection : connections)
				idApi.deleteConnection(store.get(request, "access_token"), connection.getId());
		response.sendRedirect("/xero_java_oauth2_app_war");
	}

	private String getFileName(Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename"))
				return content.substring(content.indexOf("=") + 2, content.length() - 1);
		}
		return "Default.file";
	}

}