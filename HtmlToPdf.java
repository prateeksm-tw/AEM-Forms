package com.example.core.servlets;

import java.io.File;
import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.aemfd.docmanager.Document;
import com.adobe.pdfg.result.HtmlToPdfResult;
import com.adobe.pdfg.service.api.GeneratePDFService;

/**
 *
 * @author ptewari
 */

@Component(service = Servlet.class, property = { Constants.SERVICE_DESCRIPTION + "=Generates HTML to PDF by passing URL ",
		"sling.servlet.methods=POST", "sling.servlet.methods=GET", "sling.servlet.paths=/bin/htmlToPdf" })

public class HtmlToPdf extends SlingAllMethodsServlet {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	@Reference
	GeneratePDFService generatePdfService;
	@Reference
	TransactionManager txm;

	@Override
	protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {
		String url = request.getParameter("url");
		try {
			htmlToPdf(url, null, null, null, null);

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response)
			throws ServletException, IOException {

	}

	public File htmlToPdf(String inputUrl, String fileTypeSettingsName, String securitySettingsName, File settingsFile,
			File xmpFile) throws Exception {
		{
			Transaction tx = txm.getTransaction();
			// Begin transaction
			if (tx == null)
				txm.begin();
			
			String outputFolder = "D:\\new";
			Document convertedDoc = null;
			Document settingsDoc = null;
			Document xmpDoc = null;
			try {
				if (settingsFile != null && settingsFile.exists() && settingsFile.isFile())
					settingsDoc = new Document(settingsFile);
				if (xmpFile != null && xmpFile.exists() && xmpFile.isFile())
					xmpDoc = new Document(xmpFile);
				HtmlToPdfResult result = generatePdfService.htmlToPdf2(inputUrl, fileTypeSettingsName,
						securitySettingsName, settingsDoc, xmpDoc);
				;
				convertedDoc = result.getCreatedDocument();
				txm.commit();
				File outputFile = new File(outputFolder, "Output.pdf");
				convertedDoc.copyToFile(outputFile);
				return outputFile;
			} catch (Exception e) {
				if (txm.getTransaction() != null)
					txm.rollback();
				throw e;
			} finally {
				if (convertedDoc != null) {
					convertedDoc.dispose();
					convertedDoc = null;
				}
				if (xmpDoc != null) {
					xmpDoc.dispose();
					xmpDoc = null;
				}
				if (settingsDoc != null) {
					settingsDoc.dispose();
					settingsDoc = null;
				}
			}
		}
	}
}