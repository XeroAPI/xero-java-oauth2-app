package com.xero.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.xero.api.*;
import com.xero.models.accounting.*;
import com.xero.models.accounting.Phone.PhoneTypeEnum;
import com.xero.models.assets.Asset;
import com.xero.models.assets.AssetStatus;
import com.xero.models.assets.AssetStatusQueryParam;
import com.xero.models.assets.AssetType;
import com.xero.models.assets.Assets;
import com.xero.models.assets.BookDepreciationDetail;
import com.xero.models.assets.BookDepreciationSetting;
import com.xero.models.assets.Setting;
import com.xero.models.bankfeeds.CreditDebitIndicator;
import com.xero.models.bankfeeds.EndBalance;
import com.xero.models.bankfeeds.FeedConnection;
import com.xero.models.bankfeeds.FeedConnections;
import com.xero.models.bankfeeds.StartBalance;
import com.xero.models.bankfeeds.Statement;
import com.xero.models.bankfeeds.StatementLine;
import com.xero.models.bankfeeds.Statements;
import com.xero.models.bankfeeds.FeedConnection.AccountTypeEnum;
import com.xero.models.assets.BookDepreciationSetting.AveragingMethodEnum;
import com.xero.models.assets.BookDepreciationSetting.DepreciationCalculationMethodEnum;
import com.xero.models.assets.BookDepreciationSetting.DepreciationMethodEnum;
import com.xero.models.assets.FieldValidationErrorsElement;
import com.xero.models.identity.Connection;
import com.xero.models.payrollau.CalendarType;
import com.xero.models.payrollau.DeductionLine;
import com.xero.models.payrollau.DeductionType;
import com.xero.models.payrollau.DeductionTypeCalculationType;
import com.xero.models.payrollau.EarningsRate;
import com.xero.models.payrollau.EarningsType;
import com.xero.models.payrollau.EmploymentTerminationPaymentType;
import com.xero.models.payrollau.HomeAddress;
import com.xero.models.payrollau.LeaveApplication;
import com.xero.models.payrollau.LeaveApplications;
import com.xero.models.payrollau.PayItem;
import com.xero.models.payrollau.PayItems;
import com.xero.models.payrollau.PayRun;
import com.xero.models.payrollau.PayRunStatus;
import com.xero.models.payrollau.PayRuns;
import com.xero.models.payrollau.PayrollCalendar;
import com.xero.models.payrollau.PayrollCalendars;
import com.xero.models.payrollau.Payslip;
import com.xero.models.payrollau.PayslipLines;
import com.xero.models.payrollau.PayslipObject;
import com.xero.models.payrollau.RateType;
import com.xero.models.payrollau.Settings;
import com.xero.models.payrollau.SettingsObject;
import com.xero.models.payrollau.SuperFund;
import com.xero.models.payrollau.SuperFundProducts;
import com.xero.models.payrollau.SuperFundType;
import com.xero.models.payrollau.SuperFunds;
import com.xero.models.payrollau.Timesheet;
import com.xero.models.payrollau.TimesheetLine;
import com.xero.models.payrollau.TimesheetObject;
import com.xero.models.payrollau.TimesheetStatus;
import com.xero.models.payrollau.Timesheets;
import com.xero.models.payrolluk.PayRunCalendar;
import com.xero.models.payrolluk.PayRunCalendars;
import com.xero.models.project.Amount;
import com.xero.models.project.ChargeType;
import com.xero.models.project.CurrencyCode;
import com.xero.models.project.Project;
import com.xero.models.project.ProjectCreateOrUpdate;
import com.xero.models.project.ProjectPatch;
import com.xero.models.project.ProjectStatus;
import com.xero.models.project.ProjectUsers;
import com.xero.models.project.Projects;
import com.xero.models.project.Task;
import com.xero.models.project.TaskCreateOrUpdate;
import com.xero.models.project.Tasks;
import com.xero.models.project.TimeEntries;
import com.xero.models.project.TimeEntry;
import com.xero.models.project.TimeEntryCreateOrUpdate;
import com.xero.api.client.AccountingApi;
import com.xero.api.client.AssetApi;
import com.xero.api.client.BankFeedsApi;
import com.xero.api.client.IdentityApi;
import com.xero.api.client.PayrollAuApi;
import com.xero.api.client.PayrollUkApi;
import com.xero.api.client.ProjectApi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.threeten.bp.*;
import org.threeten.bp.format.DateTimeFormatter;

@WebServlet("/AuthenticatedResource")
public class AuthenticatedResource extends HttpServlet {
    private static final long serialVersionUID = 1L;
    final static Logger logger = LogManager.getLogger(AuthenticatedResource.class);
    private AccountingApi accountingApi = null;
    private PayrollAuApi payrollAuApi = null;
    private PayrollUkApi payrollUkApi = null;
    private AssetApi assetApi = null;
    private ProjectApi projectApi = null;
    private BankFeedsApi bankFeedsApi = null;
    
    private String htmlString = "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" crossorigin=\"anonymous\">"
            + "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css\" integrity=\"sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r\" crossorigin=\"anonymous\">"
            + "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js\" integrity=\"sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS\" crossorigin=\"anonymous\"></script>"
            + "<div class=\"container\"><h1>Xero API - JAVA</h1>" + "<div class=\"form-group\">"
            + "<a href=\"/\" class=\"btn btn-default\" type=\"button\">Logout</a>" + "</div>"
            + "<form action=\"./AuthenticatedResource\" method=\"post\">" + "<div class=\"form-group\">"
            + "<label for=\"object\">Create, Read, Update & Delete</label>"
            + "<select name=\"object\" class=\"form-control\" id=\"object\">"
            + "<option value=\"---\" >-- IDENTITY --</option>"
            + "<option SELECTED value=\"Connections\" >Connections</option>"   
            + "<option SELECTED value=\"Disconnect\" >Disconnect</option>"  
            + "<option value=\"--\" >---- Bank Feeds ----</option>"
            + "<option value=\"BankFeedConnections\" >Connection</option>"
            + "<option value=\"BankStatements\" >Statements</option>"
            + "<option value=\"---\" >-- Payroll UK --</option>"
            + "<option value=\"PayrollUkEmployees\" >Employees</option>"   
            + "<option value=\"PayrollUkEmployment\" >Employment</option>" 
            + "<option value=\"PayrollUkEmployeeTax\" >Employee Tax</option>" 
            + "<option value=\"PayrollUkEmployeeOpeningBalances\" >EmployeeOpeningBalances</option>" 
            + "<option value=\"PayrollUkEmployeeLeave\" >EmployeeLeave</option>" 
            + "<option value=\"PayrollUkEmployeeLeaveBalances\" >Employee Leave Balances</option>" 
            + "<option value=\"PayrollUkEmployeeStatutoryLeaveBalance\" >Employee Statutory Leave Balances</option>" 
            + "<option value=\"PayrollUkEmployeeStatutoryLeaveSummary\" >Employee Statutory Leave Summary</option>" 
            + "<option value=\"PayrollUkEmployeeStatutorySickLeave\" >Employee Statutory Sick Leave</option>" 
            + "<option value=\"PayrollUkEmployeeLeavePeriods\" >Employee Leave Periods</option>" 
            + "<option value=\"PayrollUkEmployeeLeaveType\" >Employee LeaveType</option>" 
            + "<option value=\"PayrollUkEmployeePayTemplates\" >Employee PayTemplates</option>" 
            + "<option value=\"PayrollUkEmployerPensions\" >Employee Employer Pensions</option>" 
            + "<option value=\"PayrollUkDeductions\" >Deductions</option>" 
            + "<option value=\"PayrollUkEarningsOrders\" >Earnings Orders</option>" 
            + "<option value=\"PayrollUkEarningRates\" >Earning Rates</option>" 
            + "<option value=\"PayrollUkLeaveType\" >LeaveType</option>" 
            + "<option value=\"PayrollUkReimbursements\" >Reimbursements</option>" 
            + "<option value=\"PayrollUkTimesheets\" >Timesheets</option>" 
            + "<option value=\"PayrollUkPaymentMethods\" >Payment Methods</option>" 
            + "<option value=\"PayrollUkPayRunCalendars\" >Pay Run Calendars</option>" 
            + "<option value=\"PayrollUkSalaryAndWages\" >Salary And Wages</option>" 
            + "<option value=\"PayrollUkPayruns\" >Payruns</option>" 
            + "<option value=\"PayrollUkPayslips\" >Payslips</option>" 
            + "<option value=\"PayrollUkSettings\" >Settings</option>" 
            + "<option value=\"PayrollUkTrackingCategories\" >Tracking Categories</option>" 
            + "<option value=\"---\" >-- Payroll AU --</option>"
            + "<option value=\"PayrollAuEmployees\" >Employees</option>"   
            + "<option value=\"PayrollAuLeaveApplications\" >LeaveApplications</option>"   
            + "<option value=\"PayrollAuPayItems\" >PayItems</option>"   
            + "<option value=\"PayrollAuEPayrollCalendar\" >PayrollCalendar</option>"   
            + "<option value=\"PayrollAuPayRuns\" >PayRuns</option>"   
            + "<option value=\"PayrollAuPayslips\" >Payslips</option>"
            + "<option value=\"PayrollAuSettings\" >Settings</option>"
            + "<option value=\"PayrollAuSuperfunds\" >Superfunds</option>"
            + "<option value=\"PayrollAuSuperfundProducts\" >Superfund Products</option>"
            + "<option value=\"PayrollAuTimesheets\" >Timesheets</option>"
            + "<option value=\"---\" >-- FIXED ASSETS --</option>"
            + "<option value=\"Assets\" >Assets</option>"
            + "<option value=\"---\" >-- PROJECTS --</option>"
            + "<option value=\"Projects\" >Projects</option>"
            + "<option value=\"ProjectUsers\" >ProjectUsers</option>"
            + "<option value=\"ProjectTasks\" >ProjectTasks</option>"
            + "<option value=\"ProjectTime\" >ProjectTime</option>"
            + "<option value=\"---\" >-- ACCOUNTING --</option>"
            + "<option value=\"Accounts\" >Accounts</option>"
            + "<option value=\"CreateAttachments\">Attachments - Create</option>"
            + "<option value=\"GetAttachments\">Attachments - Get</option>"
            + "<option value=\"BankTransactions\" >BankTransactions</option>"
            + "<option value=\"BankTransfers\" >BankTransfers</option>"
            + "<option value=\"BatchPayments\" >BatchPayments</option>"
            + "<option value=\"BrandingThemes\">BrandingThemes</option>"
            + "<option value=\"Contacts\">Contacts</option>" + "<option value=\"ContactGroups\" >ContactGroups</option>"
            + "<option value=\"ContactGroupContacts\">ContactGroups Contacts</option>"
            + "<option value=\"CreditNotes\" >CreditNotes</option>"
            + "<option value=\"CreditNotesPDF\" >CreditNote As PDF</option>"
            + "<option value=\"Currencies\">Currencies</option>" + "<option value=\"Employees\" >Employees</option>"
            + "<option value=\"ExpenseClaims\">ExpenseClaims</option>" + "<option value=\"Invoices\" >Invoices</option>"
            + "<option value=\"InvoiceReminders\">InvoiceReminders</option>" + "<option value=\"Items\">Items</option>"
            + "<option value=\"Journals\">Journals</option>"
            + "<option value=\"LinkedTransactions\">LinkedTransactions</option>"
            + "<option value=\"ManualJournals\">ManualJournals</option>"
            + "<option value=\"Organisations\" >Organisations</option>"
            + "<option value=\"Overpayments\">Overpayments</option>" + "<option value=\"Payments\">Payments</option>"
            + "<option value=\"PaymentServices\">PaymentServices</option>"
            + "<option value=\"Prepayments\">Prepayments</option>"
            + "<option value=\"PurchaseOrders\">PurchaseOrders</option>"
            + "<option value=\"Quotes\">Quotes</option>"
            + "<option value=\"Receipts\">Receipts</option>"
            + "<option value=\"RepeatingInvoices\" >RepeatingInvoices</option>"
            + "<option value=\"Reports\" >Reports</option>"
            + "<option value=\"TaxRates\">TaxRates</option>"
            + "<option value=\"TrackingCategories\" >TrackingCategories</option>"
            + "<option value=\"Users\">Users</option>" + "<option value=\"Errors\" >Errors</option>"
            + "</select>" + "</div>" + "<div class=\"form-group\">"
            + "<input class=\"btn btn-default\" type=\"submit\" value=\"submit\">" + "</div>" + "</form></div>";

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter respWriter = response.getWriter();
        response.setStatus(200);
        response.setContentType("text/html");
        respWriter.println(htmlString);
    }

    @SuppressWarnings("null")
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        OffsetDateTime ifModifiedSince = null;
        String where = null;
        String order = null;
        boolean summarizeErrors = false;
        int unitdp = 4;
        String ids = null;
        List<UUID> invoiceIds = new ArrayList<UUID>();
        boolean includeArchived = false;
        List<String> invoiceNumbers = new ArrayList<String>();
        List<UUID> contactIds = new ArrayList<UUID>();
        List<String> statuses = null;

        boolean createdByMyApp = false;
        int unitDp = 2;
        Calendar now = Calendar.getInstance();
        int day = now.get(Calendar.DATE);
        int year = now.get(Calendar.YEAR);
        int lastMonth = now.get(Calendar.MONTH) - 1;
        int nextMonth = now.get(Calendar.MONTH) + 1;
        if (lastMonth == 0) {
            lastMonth = 1;
        }
        if (lastMonth == -1) {
            lastMonth = 12;
            year = year - 1;
        }
        if (nextMonth == 13) {
            nextMonth = 1;
            year = year + 1;
        }
        if (day > 28) {
            day = 28;
        }

        PrintWriter respWriter = response.getWriter();
        response.setStatus(200);
        response.setContentType("text/html");
        respWriter.println(htmlString);
        respWriter.println("<div class=\"container\"><hr>begin processing request<hr><div class=\"form-group\">");

        String object = request.getParameter("object");
        ArrayList<String> messages = new ArrayList<String>();

        // Get Tokens and Xero Tenant Id from Storage
        TokenStorage store = new TokenStorage();
        String savedAccessToken = store.get(request, "access_token");
        String savedRefreshToken = store.get(request, "refresh_token");
        String xeroTenantId = store.get(request, "xero_tenant_id");

        // Check expiration of token and refresh if necessary
        // This should be done prior to each API call to ensure your accessToken is
        // valid
        String accessToken = new TokenRefresh().checkToken(savedAccessToken, savedRefreshToken, response);

        // Init AccountingApi client
        ApiClient defaultClient = new ApiClient();
        defaultClient.setConnectionTimeout(6000);
        // Get Singleton - instance of accounting client
        accountingApi = AccountingApi.getInstance(defaultClient);
        
        // Init Payroll client
        ApiClient defaultPayrollAuClient = new ApiClient("https://api.xero.com/payroll.xro/1.0",null,null,null,null);
        // Get Singleton - instance of PayrollAUAPi client
        payrollAuApi = PayrollAuApi.getInstance(defaultPayrollAuClient);
        
        // Init Payroll UK client
        ApiClient defaultPayrollUkClient = new ApiClient("https://api.xero.com/payroll.xro/2.0",null,null,null,null);
        // Get Singleton - instance of PayrollUkApi client
        payrollUkApi = PayrollUkApi.getInstance(defaultPayrollUkClient);

        // Init Assets client
        ApiClient defaultAssetsClient = new ApiClient("https://api.xero.com/assets.xro/1.0",null,null,null,null);
        // Get Singleton - instance of PayrollAUAPi client
        assetApi = AssetApi.getInstance(defaultAssetsClient);
       
        // Init Projects client
        ApiClient defaultProjectsClient = new ApiClient("https://api.xero.com/projects.xro/2.0",null,null,null,null);
        // Get Singleton - instance of Projects client
        projectApi = ProjectApi.getInstance(defaultProjectsClient);
        
        // Init Identity   
        ApiClient defaultIdentityClient = new ApiClient("https://api.xero.com", null, null, null, null);
        IdentityApi idApi = new IdentityApi(defaultIdentityClient);
        
        // Init BankFeeds client
        ApiClient defaultBankFeedsClient = new ApiClient("https://api.xero.com/bankfeeds.xro/1.0",null,null,null,null);
        // Get Singleton - instance of bankfeed client
        bankFeedsApi = BankFeedsApi.getInstance(defaultBankFeedsClient);
        
        
        if(object.equals("Connections")) {
            List<Connection> connection = idApi.getConnections(savedAccessToken,null);           
            messages.add("Connection count:" + connection.size());
    
        } if(object.equals("Disconnect")) {
             // GET ALl connections
             List<Connection> allConnection = idApi.getConnections(savedAccessToken, null);
             idApi.deleteConnection(savedAccessToken, allConnection.get(0).getId());          
             messages.add("You deleted the connection");
        
        } else if(object.equals("BankFeedConnections")) {
            /* BANKFEED CONNECTIONS */
            // Create New Feed Connection
            // Success
            // We need to pause for 10 seconds before using our FeedConnectionId
            UUID feedConnectionId = null;
          
            try {
                FeedConnection newBank = new FeedConnection();
                newBank.setAccountName("SDK Bank " + loadRandomNum());
                newBank.setAccountNumber("1234" + loadRandomNum());
                newBank.setAccountType(AccountTypeEnum.BANK);
                newBank.setAccountToken("foobar" + loadRandomNum());
                newBank.setCurrency(com.xero.models.bankfeeds.CurrencyCode.GBP);
                
                FeedConnections feedConnections = new FeedConnections();
                feedConnections.addItemsItem(newBank);
                
                FeedConnections fc1 = bankFeedsApi.createFeedConnections(accessToken,xeroTenantId,feedConnections);
                messages.add("CREATE Bank with status: " + fc1.getItems().get(0).getStatus());
                feedConnectionId = fc1.getItems().get(0).getId();
                messages.add("CREATED Bank feed ID: " + feedConnectionId);
                try {
                    System.out.println("Sleep for 10 seconds");
                    Thread.sleep(10000);
                } catch(InterruptedException e) {
                    System.out.println(e);
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            // READ a single feed connection
            // Success
            try {
                FeedConnection oneFC = bankFeedsApi.getFeedConnection(accessToken,xeroTenantId,feedConnectionId);
                messages.add("READ One Bank: " + oneFC.getAccountName());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  

            // Delete Feed Connection
            // Success
            try {
                FeedConnections deleteFeedConnections = new FeedConnections();
                FeedConnection feedConnectionOne = new FeedConnection();
                feedConnectionOne.setId(feedConnectionId);
                deleteFeedConnections.addItemsItem(feedConnectionOne);
                
                FeedConnections deletedFeedConnection = bankFeedsApi.deleteFeedConnections(accessToken,xeroTenantId,deleteFeedConnections);              
                messages.add("Deleted Bank status: " + deletedFeedConnection.getItems().get(0).getStatus());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
         
            // Get ALL Feed Connection
            // Fail 500 ERROR
           
            try {
                FeedConnections fc = bankFeedsApi.getFeedConnections(accessToken, xeroTenantId, 1,87654321);
                messages.add("Total Banks found: " + fc.getItems().size());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
           
        } else if(object.equals("BankStatements")) {
            
            // Create One Statement
            /*
             * You need a valid FeedconnectionId
             */
            UUID feedConnectionId = null;
            try {
                FeedConnection newBank = new FeedConnection();
                newBank.setAccountName("SDK Bank " + loadRandomNum());
                newBank.setAccountNumber("1234" + loadRandomNum());
                newBank.setAccountType(AccountTypeEnum.BANK);
                newBank.setAccountToken("foobar" + loadRandomNum());
                newBank.setCurrency(com.xero.models.bankfeeds.CurrencyCode.GBP);
                
                FeedConnections feedConnections = new FeedConnections();
                feedConnections.addItemsItem(newBank);
                
                FeedConnections fc1 = bankFeedsApi.createFeedConnections(accessToken,xeroTenantId,feedConnections);
                feedConnectionId = fc1.getItems().get(0).getId();
                //INVALID ID
                //feedConnectionId = UUID.fromString("ae3a8ef3-005a-4946-b311-95f76afa8553");
                
                messages.add("CREATED new Bank feed ID: " + feedConnectionId);
                try {
                    System.out.println("Sleep for 10 seconds");
                    Thread.sleep(10000);
                } catch(InterruptedException e) {
                    System.out.println(e);
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
               
            //Create a single Statement
            /*
             * You need a valid StatementId later
             */
            
            UUID statementId = null;

            try {
                Statements newStatements = new Statements();
                Statement newStatement = new Statement();
                         
                LocalDate stDate = LocalDate.of(2019, 8, 30);
                LocalDate endDate = LocalDate.of(2019, 8, 31);
                newStatement.setStartDate(stDate);
                newStatement.endDate(endDate);
                StartBalance stBalance = new StartBalance();
                stBalance.setAmount(1100.00);
                stBalance.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement.setStartBalance(stBalance);
                
                EndBalance endBalance = new EndBalance();
                endBalance.setAmount(332.55);
                endBalance.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement.endBalance(endBalance);
                
                newStatement.setFeedConnectionId(feedConnectionId);
                
                StatementLine newStatementLine = new StatementLine();
                newStatementLine.setAmount(267.45);
                newStatementLine.setDescription("Amazon.co.uk AMAZON.CO.UK LU");
                newStatementLine.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatementLine.setTransactionId("a21aa751-f405-43f0-826a-28101e17c100");
                LocalDate postedDate = LocalDate.of(2019, 8, 30);
                newStatementLine.setPostedDate(postedDate);
            
                List<StatementLine> statementLines= new ArrayList<StatementLine>();
                statementLines.add(newStatementLine);
                newStatement.setStatementLines(statementLines);
                newStatements.addItemsItem(newStatement);
                
                Statement newStatement2 = new Statement();
                LocalDate stDate2 = LocalDate.of(2019, 8, 27);
                LocalDate endDate2 = LocalDate.of(2019, 8, 28);
                newStatement2.setStartDate(stDate2);
                newStatement2.endDate(endDate2);
                newStatement2.setStartBalance(stBalance);
                
                StatementLine newStatementLine2 = new StatementLine();
                newStatementLine2.setAmount(267.45);
                newStatementLine2.setDescription("Amazon.co.uk AMAZON.CO.UK LU");
                newStatementLine2.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatementLine2.setTransactionId("a21aa751-f405-43f0-826a-28101e17c100");
                LocalDate postedDate2 = LocalDate.of(2019, 8, 27);
                newStatementLine2.setPostedDate(postedDate2);
            
                List<StatementLine> statementLines2= new ArrayList<StatementLine>();
                statementLines2.add(newStatementLine2);
                newStatement2.endBalance(endBalance);
                newStatement2.setFeedConnectionId(feedConnectionId);
                newStatement2.setStatementLines(statementLines2);
                newStatements.addItemsItem(newStatement2);

                Statements rStatements = bankFeedsApi.createStatements(accessToken,xeroTenantId,newStatements);
                messages.add("New Bank Statement Status: " + rStatements.getItems().get(0).getStatus());
                statementId = rStatements.getItems().get(0).getId();
           
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
           
            // GET a single Statement
            //You'll need an existing Statement ID, to get One Statement    
            // works successfully
            try {
                
                Statement oneStatement = bankFeedsApi.getStatement(accessToken,xeroTenantId,statementId);                
                messages.add("New Bank Statement Status: " + oneStatement.getStatementLineCount());
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            
            // Create Duplicate Statement - to test error handling
            // You'll need an existing FeedConnectionId
            // Works successfully
            // Returns 409 error as expected
            
            try {
                Statements newStatements = new Statements();
                Statement newStatement = new Statement();
                LocalDate stDate = LocalDate.of(year, lastMonth, 01);
                newStatement.setStartDate(stDate);
                LocalDate endDate = LocalDate.of(year, lastMonth, 15);
                newStatement.endDate(endDate);
                StartBalance stBalance = new StartBalance();
                stBalance.setAmount(100.00);
                stBalance.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement.setStartBalance(stBalance);
                
                EndBalance endBalance = new EndBalance();
                endBalance.setAmount(150.00);
                endBalance.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement.endBalance(endBalance);
                
                //FeedConnections fc = bankFeedsApi.getFeedConnections(null,null);
                newStatement.setFeedConnectionId(feedConnectionId);
                
                StatementLine newStatementLine = new StatementLine();
                newStatementLine.setAmount(50.0);
                newStatementLine.setChequeNumber("123");
                newStatementLine.setDescription("My new line");
                newStatementLine.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatementLine.setReference("Foobar" );
                newStatementLine.setPayeeName("StarLord");
                newStatementLine.setTransactionId("1234");
                LocalDate postedDate = LocalDate.of(year, lastMonth, 05);
                newStatementLine.setPostedDate(postedDate);
            
                List<StatementLine> arrayStatementLines = new ArrayList<StatementLine>();
                arrayStatementLines.add(newStatementLine);
                
                newStatement.setStatementLines(arrayStatementLines);
                
                newStatements.addItemsItem(newStatement);
                Statements rStatements2 = bankFeedsApi.createStatements(accessToken, xeroTenantId, newStatements);
                messages.add("New Bank Statement Status: " + rStatements2.getItems().get(0).getStatus());
                
                //DUPLICATE
                Statements rStatements3 = bankFeedsApi.createStatements(accessToken, xeroTenantId, newStatements);
                messages.add("New Bank Statement Status: " + rStatements3.getItems().get(0).getStatus());
                messages.add("New Bank Statement Error: " + rStatements3.getItems().get(0).getErrors().get(0).getDetail());
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            //Get ALL Statements
            try {
                Statements allStatements = bankFeedsApi.getStatements(accessToken,xeroTenantId,1, 3, null, null);
                messages.add("Statement total: " + allStatements.getItems().size());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            
            // Create Multiple Statements in a POST
            UUID feedConnectionId2 = UUID.fromString("2ebe6393-f3bb-48ab-9a0e-b04fa8585a70");
            try {
                Statements arrayOfStatements = new Statements();
                Statement newStatement = new Statement();
                
                LocalDate stDate = LocalDate.of(year, lastMonth, day);              
                newStatement.setStartDate(stDate);

                LocalDate endDate = LocalDate.of(year, lastMonth, day);                         
                newStatement.endDate(endDate);
                StartBalance stBalance = new StartBalance();
                stBalance.setAmount(100.00);
                stBalance.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement.setStartBalance(stBalance);
                
                EndBalance endBalance = new EndBalance();
                endBalance.setAmount(150.00);
                endBalance.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement.endBalance(endBalance);         
                newStatement.setFeedConnectionId(feedConnectionId);
       
                StatementLine newStatementLine = new StatementLine();
                newStatementLine.setAmount(50.00);
                newStatementLine.setChequeNumber("123" + loadRandomNum());
                newStatementLine.setDescription("My new line");
                newStatementLine.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatementLine.setReference("Foobar" + loadRandomNum());
                newStatementLine.setPayeeName("StarLord" + loadRandomNum());
                newStatementLine.setTransactionId("1234" + loadRandomNum());
                
                LocalDate postedDate = LocalDate.of(year, lastMonth, day);              
                newStatementLine.setPostedDate(postedDate);
            
                List<StatementLine> arrayStatementLines = new ArrayList<StatementLine>();
                arrayStatementLines.add(newStatementLine);
                newStatement.setStatementLines(arrayStatementLines);
               
                arrayOfStatements.addItemsItem(newStatement);
                
                Statement newStatement2 = new Statement();
                LocalDate stDate2 = LocalDate.of(year, lastMonth, day);             
                newStatement2.setStartDate(stDate2);

                LocalDate endDate2 = LocalDate.of(year, lastMonth, day);                
                newStatement2.endDate(endDate2);
                StartBalance stBalance2 = new StartBalance();
                stBalance2.setAmount(100.00);
                stBalance2.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement2.setStartBalance(stBalance2);
                
                EndBalance endBalance2 = new EndBalance();
                endBalance2.setAmount(150.00);
                endBalance2.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatement2.endBalance(endBalance2);
                
                newStatement2.setFeedConnectionId(feedConnectionId2);
                
                StatementLine newStatementLine2 = new StatementLine();
                newStatementLine2.setAmount(50.00);
                newStatementLine2.setChequeNumber("123" + loadRandomNum());
                newStatementLine2.setDescription("My new line");
                newStatementLine2.setCreditDebitIndicator(CreditDebitIndicator.CREDIT);
                newStatementLine2.setReference("Foobar" + loadRandomNum());
                newStatementLine2.setPayeeName("StarLord" + loadRandomNum());
                newStatementLine2.setTransactionId("1234" + loadRandomNum());
                LocalDate postedDate2 = LocalDate.of(year, lastMonth, day);
                newStatementLine2.setPostedDate(postedDate2);
            
                List<StatementLine> arrayStatementLines2 = new ArrayList<StatementLine>();
                arrayStatementLines2.add(newStatementLine);
                newStatement.setStatementLines(arrayStatementLines2);
                arrayOfStatements.addItemsItem(newStatement);
                
                Statements rStatements = bankFeedsApi.createStatements(accessToken, xeroTenantId, arrayOfStatements);
                
                messages.add("Statement Status: " + rStatements.getItems().get(0).getStatus());
           
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
          
        } else if(object.equals("Projects")) {
            //PROJECTS
            try {
                // GET ALL Projects
                Projects projects = projectApi.getProjects(accessToken, xeroTenantId, null,null, null, null, null);
                if(projects.getPagination().getItemCount() > 0) {
                    System.out.println("HELLO A");

                    messages.add("Get ALL Project total: " + projects.getPagination().getItemCount());
                    
                    // GET one Project
                    UUID projectId = projects.getItems().get(0).getProjectId();
                    Project singleProject = projectApi.getProject(accessToken, xeroTenantId, projectId);
                    messages.add("Get ONE Project Name: " + singleProject.getName());
                    
                    // UDPATE a Project
                    ProjectCreateOrUpdate project = new ProjectCreateOrUpdate();
                    project.setName("BarProject" + loadRandomNum());
        
                    OffsetDateTime deadlineUtc = OffsetDateTime.of(LocalDateTime.of(2020, 03, 03, 15, 00), ZoneOffset.UTC);
                    project.setDeadlineUtc(deadlineUtc);
                    project.setEstimateAmount(199.99);
                    
                    projectApi.updateProject(accessToken, xeroTenantId, projectId, project);
                    messages.add("Update Project complete: ");
                    
                    // Patch a Project
                    /* PATCH Does Not work in Java SDK :-(
                    try {
                        ProjectPatch patchProject = new ProjectPatch();
                        patchProject.setStatus(ProjectStatus.CLOSED);
                        projectsApi.patchProject(accessToken, xeroTenantId, projectId, patchProject);
                        messages.add("Patch Project complete - Status Closed");
                    } catch (XeroApiException e) {
                        System.out.println(e.getResponseCode());
                        System.out.println(e.getMessage());
    
                    }
                    */
                } else {
                    messages.add("No projects found");                    
                }

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
          
            try {
                // CREATE a Project
                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                if (contacts.getContacts().size() > 0) {
                    UUID contactId = contacts.getContacts().get(0).getContactID();
                    ProjectCreateOrUpdate project = new ProjectCreateOrUpdate();
                    project.setContactId(contactId);
                    project.setName("FooProject" + loadRandomNum());
        
                    OffsetDateTime deadlineUtc = OffsetDateTime.of(LocalDateTime.of(2020, 03, 03, 15, 00), ZoneOffset.UTC);
                    project.setDeadlineUtc(deadlineUtc);
                    project.setEstimateAmount(99.99);
                    
                    Project newProject = projectApi.createProject(accessToken, xeroTenantId, project);
                    messages.add("New Project created Name: " + newProject.getName());
                } else {
                    messages.add("No contacts found - can not create new project");                    
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
          
        } else if(object.equals("ProjectUsers")) {
            //PROJECT USERS
            try {
                ProjectUsers projectUsers = projectApi.getProjectUsers(accessToken, xeroTenantId,1,50);
                messages.add("Project users total: " + projectUsers.getPagination().getItemCount());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
        } else if(object.equals("ProjectTasks")) {
            //PROJECT TASKS
            try {
                // Get ALL Projects
                Projects projects = projectApi.getProjects(accessToken, xeroTenantId, null,null, null, null, null);
                
                if(projects.getPagination().getItemCount() > 0) {
                    UUID projectId = projects.getItems().get(0).getProjectId();
                    UUID taskId = null;
                    // GET ALL TASKS
                    try {
                        Tasks tasks = projectApi.getTasks(accessToken, xeroTenantId, projectId, 1, 50, null);
                        taskId = tasks.getItems().get(0).getTaskId();
                        messages.add("Get All Tasks total: " + tasks.getPagination().getItemCount());
                    } catch (XeroBadRequestException e) {
                        this.addBadRequest(e, messages); 
                    } catch (XeroForbiddenException e) {
                        this.addError(e, messages); 
                    } catch (XeroNotFoundException e) {
                        this.addError(e, messages); 
                    } catch (XeroUnauthorizedException e) {
                        this.addError(e, messages); 
                    } catch (XeroMethodNotAllowedException e) {
                        this.addMethodNotAllowedException(e, messages); 
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }  
                    
                    // GET a single TASK
                    try {
                        Task task = projectApi.getTask(accessToken, xeroTenantId, projectId, taskId);
                        messages.add("Get one Tasks total: " + task.getName());
                    } catch (XeroBadRequestException e) {
                        this.addBadRequest(e, messages); 
                    } catch (XeroForbiddenException e) {
                        this.addError(e, messages); 
                    } catch (XeroNotFoundException e) {
                        this.addError(e, messages); 
                    } catch (XeroUnauthorizedException e) {
                        this.addError(e, messages); 
                    } catch (XeroMethodNotAllowedException e) {
                        this.addMethodNotAllowedException(e, messages); 
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }  
                    
                    // Create a TASK
                    // BROKEN by BUG IN API
                    /*
                    TaskCreateOrUpdate taskCreateOrUpdate = new TaskCreateOrUpdate();
                    taskCreateOrUpdate.setChargeType(ChargeType.FIXED);
                    taskCreateOrUpdate.setEstimateMinutes(30);
                    taskCreateOrUpdate.setName("My New Task");
                    Amount taskRate = new Amount();
                    taskRate.setCurrency(com.xero.models.project.CurrencyCode.NZD);
                    taskRate.setValue(99.99);
                    taskCreateOrUpdate.setRate(taskRate);
                    Tasks tasks = projectApi.createTask(accessToken, xeroTenantId, projectId, taskCreateOrUpdate);
                   */
                    
                    
                    // Update a TASK
                    // BROKEN by BUG IN API
                    /* 
                    TaskCreateOrUpdate taskCreateOrUpdate = new TaskCreateOrUpdate();
                    taskCreateOrUpdate.setChargeType(ChargeType.FIXED);
                    taskCreateOrUpdate.setEstimateMinutes(30);
                    taskCreateOrUpdate.setName("My Updated Task");
                    Amount taskRate = new Amount();
                    taskRate.setCurrency(com.xero.models.project.CurrencyCode.NZD);
                    taskRate.setValue(99.99);
                    taskCreateOrUpdate.setRate(taskRate);
                    Tasks tasks = projectsApi.updateTask(accessToken, xeroTenantId, projectId, taskId, taskCreateOrUpdate);
                    
                    System.out.println(tasks.toString());
                    */
                    
                    
                    // delete a TASK
                    // BROKEN BUG IN API
                    /*
                    projectsApi.deleteTask(accessToken, xeroTenantId, projectId, taskId);
                    System.out.println("Task deleted!");
                    */
                    
                } else {
                    messages.add("No projects found - can not create new task");                    
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
        } else if(object.equals("ProjectTime")) {
            //PROJECT TASKS
            // Get ALL Projects
            Projects projects = projectApi.getProjects(accessToken, xeroTenantId, null,null, null, null, null);
            if(projects.getPagination().getItemCount() > 0) {
                UUID projectId = projects.getItems().get(0).getProjectId();
                UUID timeEntryId = null;
                
                // GET ALL Time Entries
                try {
                    TimeEntries timeEntries = projectApi.getTimeEntries(savedAccessToken, xeroTenantId, projectId, null, null, null, null, null, null, null, null, null, null);
                    messages.add("Get All Time Entries total: " + timeEntries.getPagination().getItemCount());                    
                    timeEntryId = timeEntries.getItems().get(0).getTimeEntryId();
                } catch (XeroBadRequestException e) {
                    this.addBadRequest(e, messages); 
                } catch (XeroForbiddenException e) {
                    this.addError(e, messages); 
                } catch (XeroNotFoundException e) {
                    this.addError(e, messages); 
                } catch (XeroUnauthorizedException e) {
                    this.addError(e, messages); 
                } catch (XeroMethodNotAllowedException e) {
                    this.addMethodNotAllowedException(e, messages); 
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
                // GET a single Time Entry
                try {
                    TimeEntry timeEntry = projectApi.getTimeEntry(accessToken, xeroTenantId, projectId, timeEntryId);
                    messages.add("Get single Time Entries ID: " + timeEntry.getTimeEntryId());
                } catch (XeroBadRequestException e) {
                    this.addBadRequest(e, messages); 
                } catch (XeroForbiddenException e) {
                    this.addError(e, messages); 
                } catch (XeroNotFoundException e) {
                    this.addError(e, messages); 
                } catch (XeroUnauthorizedException e) {
                    this.addError(e, messages); 
                } catch (XeroMethodNotAllowedException e) {
                    this.addMethodNotAllowedException(e, messages); 
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
                // CREATE a single Time Entry
                try {
                    Tasks tasks = projectApi.getTasks(accessToken, xeroTenantId, projectId, null, null, null);
                    ProjectUsers projectUsers = projectApi.getProjectUsers(accessToken, xeroTenantId,1,50);
                    
                    TimeEntryCreateOrUpdate createTimeEntry = new TimeEntryCreateOrUpdate();
                    createTimeEntry.setDescription("My description");
                    createTimeEntry.setTaskId(tasks.getItems().get(0).getTaskId());
                    createTimeEntry.setDuration(30);
                    createTimeEntry.setUserId(projectUsers.getItems().get(0).getUserId());
                    OffsetDateTime dateUtc = OffsetDateTime.of(LocalDateTime.of(2020, 02, 26, 15, 00), ZoneOffset.UTC);
                    
                    createTimeEntry.setDateUtc(dateUtc);
                    TimeEntry timeEntry = projectApi.createTimeEntry(accessToken, xeroTenantId, projectId, createTimeEntry);
                    messages.add("CREATE single Time Entry ID: " + timeEntry.getTimeEntryId());
                } catch (XeroBadRequestException e) {
                    this.addBadRequest(e, messages); 
                } catch (XeroForbiddenException e) {
                    this.addError(e, messages); 
                } catch (XeroNotFoundException e) {
                    this.addError(e, messages); 
                } catch (XeroUnauthorizedException e) {
                    this.addError(e, messages); 
                } catch (XeroMethodNotAllowedException e) {
                    this.addMethodNotAllowedException(e, messages); 
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
                // UPDATE a single Time Entry
                try {
                    Tasks tasks = projectApi.getTasks(accessToken, xeroTenantId, projectId, null, null, null);
                    ProjectUsers projectUsers = projectApi.getProjectUsers(accessToken, xeroTenantId,1,50);
                    
                    TimeEntryCreateOrUpdate updateTimeEntry = new TimeEntryCreateOrUpdate();
                    updateTimeEntry.setDescription("My UPDATED description");
                    updateTimeEntry.setTaskId(tasks.getItems().get(0).getTaskId());
                    updateTimeEntry.setDuration(45);
                    updateTimeEntry.setUserId(projectUsers.getItems().get(0).getUserId());
                    OffsetDateTime dateUtc = OffsetDateTime.of(LocalDateTime.of(2020, 02, 27, 15, 00), ZoneOffset.UTC);
                    updateTimeEntry.setDateUtc(dateUtc);
                    
                    projectApi.updateTimeEntry(accessToken, xeroTenantId, projectId, timeEntryId, updateTimeEntry);
                    messages.add("UPDATE single Time Entry SUCCESS");
                } catch (XeroBadRequestException e) {
                    this.addBadRequest(e, messages); 
                } catch (XeroForbiddenException e) {
                    this.addError(e, messages); 
                } catch (XeroNotFoundException e) {
                    this.addError(e, messages); 
                } catch (XeroUnauthorizedException e) {
                    this.addError(e, messages); 
                } catch (XeroMethodNotAllowedException e) {
                    this.addMethodNotAllowedException(e, messages); 
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                
                // DELETE a single Time Entry
                try {
                    projectApi.deleteTimeEntry(accessToken, xeroTenantId, projectId, timeEntryId);
                    messages.add("DELETE single Time Entry SUCCESS");
                } catch (XeroBadRequestException e) {
                    this.addBadRequest(e, messages); 
                } catch (XeroForbiddenException e) {
                    this.addError(e, messages); 
                } catch (XeroNotFoundException e) {
                    this.addError(e, messages); 
                } catch (XeroUnauthorizedException e) {
                    this.addError(e, messages); 
                } catch (XeroMethodNotAllowedException e) {
                    this.addMethodNotAllowedException(e, messages); 
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }               
            } else {
                messages.add("No projects found - can not create new time entry");                    
            }
                
        } else if(object.equals("Assets")) {
        /* Asset */
        // Create Asset
            try {
                LocalDate purchaseDate = LocalDate.of(2020, Month.JANUARY, 1);
                Asset asset = new Asset();
                asset.setAssetName("Computer" + loadRandomNum());
                asset.setAssetNumber("1234" + loadRandomNum());
                asset.setAssetName("Computer");
                asset.setAssetNumber("1234");
                asset.setAssetStatus(AssetStatus.DRAFT);
                asset.setDisposalPrice(23.23);
                asset.setPurchaseDate(purchaseDate);
                asset.setPurchasePrice(100.00);
                asset.setAccountingBookValue(99.50);
                
                BookDepreciationDetail bookDepreciationDetail = new BookDepreciationDetail();
                bookDepreciationDetail.costLimit(100.00);
                bookDepreciationDetail.currentAccumDepreciationAmount(2.25);
                bookDepreciationDetail.currentCapitalGain(5.32);
                bookDepreciationDetail.currentGainLoss(3.88);
                LocalDate depreciationStartDate = LocalDate.of(2020, Month.JANUARY, 2);
                bookDepreciationDetail.depreciationStartDate(depreciationStartDate);
                asset.setBookDepreciationDetail(bookDepreciationDetail);
                
                BookDepreciationSetting bookDepreciationSetting = new BookDepreciationSetting();
                bookDepreciationSetting.setAveragingMethod(AveragingMethodEnum.ACTUALDAYS);
                bookDepreciationSetting.setDepreciationCalculationMethod(DepreciationCalculationMethodEnum.NONE);
                bookDepreciationSetting.setDepreciationMethod(DepreciationMethodEnum.STRAIGHTLINE);
                bookDepreciationSetting.setDepreciationRate(0.5);
                asset.setBookDepreciationSetting(bookDepreciationSetting);
                
                Asset newAsset = assetApi.createAsset(accessToken, xeroTenantId, asset);
                messages.add("New Asset created: " + newAsset.getAssetName());  
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            UUID assetId = null;
            try {
                String orderBy = null;
                String sortDirection = null;
                String filterBy = null;
                Assets assets = assetApi.getAssets(accessToken, xeroTenantId, AssetStatusQueryParam.DRAFT, null, null, orderBy, sortDirection, filterBy);
                messages.add("Assets Found: " + assets.getItems().get(0).getAssetName());
                messages.add("Assets Purchase Date: " + assets.getItems().get(0).getPurchaseDate());
                assetId = assets.getItems().get(0).getAssetId();
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            try {
                Asset asset = assetApi.getAssetById(accessToken, xeroTenantId, assetId);
                messages.add("Asset ONE Found: " + asset.getAssetName());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            try {       
                List<AssetType> assetTypes = assetApi.getAssetTypes(accessToken, xeroTenantId);
                messages.add("AssetType Found: " + assetTypes.get(0).getAssetTypeName());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
           
            try {
                where = "Type==\"FIXED\"&&Status==\"ACTIVE\"";
                Accounts accountFixedAsset = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                UUID fixedAssetAccountId = accountFixedAsset.getAccounts().get(0).getAccountID();
                where = "Type==\"EXPENSE\"&&Status==\"ACTIVE\"";
                Accounts accountDepreciationExpense = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                UUID depreciationExpenseAccountId = accountDepreciationExpense.getAccounts().get(0).getAccountID();
                where = "Type==\"DEPRECIATN\"&&Status==\"ACTIVE\"";
                Accounts accountAccumulatedDepreciation = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                UUID accumulatedDepreciationAccountId = accountAccumulatedDepreciation.getAccounts().get(0).getAccountID();
                
                AssetType assetType = new AssetType();
                assetType.setAssetTypeName("Machinery" + loadRandomNum());
                assetType.setFixedAssetAccountId(fixedAssetAccountId);
                assetType.setDepreciationExpenseAccountId(depreciationExpenseAccountId);
                assetType.setAccumulatedDepreciationAccountId(accumulatedDepreciationAccountId);
                
                double depreciationRate = 0.05;
                BookDepreciationSetting bookDepreciationSetting = new BookDepreciationSetting();
                bookDepreciationSetting.setAveragingMethod(AveragingMethodEnum.ACTUALDAYS);
                bookDepreciationSetting.setDepreciationCalculationMethod(DepreciationCalculationMethodEnum.NONE);
                bookDepreciationSetting.setDepreciationRate(depreciationRate);
                bookDepreciationSetting.setDepreciationMethod(DepreciationMethodEnum.DIMINISHINGVALUE100);
                assetType.setBookDepreciationSetting(bookDepreciationSetting);
                
                AssetType newAssetType = assetApi.createAssetType(accessToken, xeroTenantId, assetType);   
                messages.add("Asset Type Created: " + newAssetType.getAssetTypeName());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
            try {           
                Setting setting = assetApi.getAssetSettings(accessToken, xeroTenantId);
                messages.add("Asset Setting Start date: " + setting.getAssetStartDate());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
        
        } else if (object.equals("PayrollUkEmployees")) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
            UUID employeeId = null;
            try {
                // GET ALL Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                employeeId = employees.getEmployees().get(0).getEmployeeID();
                messages.add("Get All Employees Total: " + employees.getPagination().getItemCount());
                
                employeeId = UUID.fromString("4ff1e5cc-9835-40d5-bb18-09fdb118db9c");
                // GET single Employee
                com.xero.models.payrolluk.EmployeeObject oneEmployee = payrollUkApi.getEmployee(accessToken, xeroTenantId, employeeId);
                
                System.out.println(oneEmployee.toString());
                
                messages.add("Found employee first name: " + oneEmployee.getEmployee().getFirstName());
            
                // Update Employee
                com.xero.models.payrolluk.Employee employee = new com.xero.models.payrolluk.Employee();
                employee.setTitle("Mr");
                employee.setFirstName("Mike");
                employee.setLastName("John" +  this.loadRandChar() + "son");
                employee.setGender(com.xero.models.payrolluk.Employee.GenderEnum.M);
                employee.setEmail( this.loadRandomNum() + "@starkindustries.com");
                com.xero.models.payrolluk.Address address = new com.xero.models.payrolluk.Address();
                address.setAddressLine1("101 Green St");
                address.setCity("San Francisco");
                address.setCountryName("United Kingdom");
                address.setPostCode("6TGR4F");
                employee.setAddress(address);
                LocalDate dateOfBirth = LocalDate.of(1999, Month.JANUARY, 1);
                employee.setDateOfBirth(dateOfBirth);
                com.xero.models.payrolluk.EmployeeObject upEmployee = payrollUkApi.updateEmployee(accessToken, xeroTenantId, employeeId, employee);
            
                messages.add("Update employee First Name: " + upEmployee.getEmployee().getFirstName() );
            
//                System.out.println(upEmployee.getEmployee().getUpdatedDateUTC());
//                Instant instant = Instant.now();
//                System.out.println(instant);
                
                // Create ALL Employees
                com.xero.models.payrolluk.Employee employee2 = new com.xero.models.payrolluk.Employee();
                employee2.setTitle("Mr");
                employee2.setFirstName("Mike");
                employee2.setLastName("Johnson");
                
                //employee.setLastName("John" +  this.loadRandChar() + "son");
                employee2.setGender(com.xero.models.payrolluk.Employee.GenderEnum.M);
                employee2.setEmail( this.loadRandomNum() + "@starkindustries.com");
                com.xero.models.payrolluk.Address address2 = new com.xero.models.payrolluk.Address();
                address.setAddressLine1("101 Green St");
                address.setCity("San Francisco");
                address.setCountryName("United Kingdom");
                address.setPostCode("6TGR4F");
                employee2.setAddress(address2);
                LocalDate dateOfBirth2 = LocalDate.of(2022, Month.JANUARY, 1);
                employee2.setDateOfBirth(dateOfBirth2);
                com.xero.models.payrolluk.EmployeeObject newEmployee = payrollUkApi.createEmployee(accessToken, xeroTenantId, employee2);
           
                messages.add("New employee First Name: " + newEmployee.getEmployee().getFirstName() );
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("PayrollUkEmployment")) {
            try {
                // GET ALL Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
                
                // GET ALL Payrun Calendars
                PayRunCalendars calendars = payrollUkApi.getPayRunCalendars(accessToken, xeroTenantId, null);
                UUID payrollCalendarID = calendars.getPayRunCalendars().get(0).getPayrollCalendarID();
                
                // Create Employment
                com.xero.models.payrolluk.Employment employment = new com.xero.models.payrolluk.Employment();
                employment.setEmployeeNumber("123ABC");
                
                employment.setNiCategory(com.xero.models.payrolluk.Employment.NiCategoryEnum.A);
                employment.setPayrollCalendarID(payrollCalendarID);
                LocalDate startDate = LocalDate.of(2020, Month.APRIL, 1);
                employment.setStartDate(startDate);
              
                com.xero.models.payrolluk.EmploymentObject newEmployment = payrollUkApi.createEmployment(accessToken, xeroTenantId, employeeId, employment);
                messages.add("Create Employment Number: " + newEmployment.getEmployment().getEmployeeNumber() );
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("PayrollUkEmployeeTax")) {
            try {
                // GET ALL Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
                
                com.xero.models.payrolluk.EmployeeTaxObject tax = payrollUkApi.getEmployeeTax(accessToken, xeroTenantId, employeeId);
                messages.add("Get Employee Tax Code: " + tax.getEmployeeTax().getTaxCode() );
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("PayrollUkEmployeeOpeningBalances")) {

            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
            
                // CREATE Employee Open Balances
                com.xero.models.payrolluk.EmployeeOpeningBalances employeeOpeningBalances = new com.xero.models.payrolluk.EmployeeOpeningBalances();
                employeeOpeningBalances.setPriorEmployeeNumber(10.0);
                employeeOpeningBalances.setStatutoryAdoptionPay(10.0);
                employeeOpeningBalances.setStatutoryMaternityPay(10.0);
                employeeOpeningBalances.setStatutoryPaternityPay(10.0);
                employeeOpeningBalances.setStatutorySharedParentalPay(10.0);
                employeeOpeningBalances.setStatutorySickPay(10.0);
               
                com.xero.models.payrolluk.EmployeeOpeningBalancesObject newEmployeeOpeningBalances = payrollUkApi.createEmployeeOpeningBalances(accessToken, xeroTenantId, employeeId, employeeOpeningBalances);
                
                if(newEmployeeOpeningBalances.getOpeningBalances() != null) {
                    messages.add("Create Open Balances - Number: " + newEmployeeOpeningBalances.getOpeningBalances().getPriorEmployeeNumber() );
                } else {
                    messages.add("Create Open Balances - Problem: " + newEmployeeOpeningBalances.getProblem().getDetail() );    
                }
           
                // UPDATE Employee Open Balances
                com.xero.models.payrolluk.EmployeeOpeningBalances toUpdateEmployeeOpeningBalances = new com.xero.models.payrolluk.EmployeeOpeningBalances();
                toUpdateEmployeeOpeningBalances.setPriorEmployeeNumber(20.0);
                toUpdateEmployeeOpeningBalances.setStatutoryAdoptionPay(20.0);
                toUpdateEmployeeOpeningBalances.setStatutoryMaternityPay(20.0);
                toUpdateEmployeeOpeningBalances.setStatutoryPaternityPay(20.0);
                toUpdateEmployeeOpeningBalances.setStatutorySharedParentalPay(20.0);
                toUpdateEmployeeOpeningBalances.setStatutorySickPay(20.0);
                 
                com.xero.models.payrolluk.EmployeeOpeningBalancesObject upEmployeeOpeningBalances = payrollUkApi.updateEmployeeOpeningBalances(accessToken, xeroTenantId, employeeId, toUpdateEmployeeOpeningBalances);
                messages.add("Update Open Balances - Number: " + upEmployeeOpeningBalances.getOpeningBalances().getPriorEmployeeNumber() );
            
            
                // GET Employee Open Balances
                com.xero.models.payrolluk.EmployeeOpeningBalancesObject employeeOpeningBalancesObject = payrollUkApi.getEmployeeOpeningBalances(accessToken, xeroTenantId, employeeId);
                messages.add("GET Open Balances - Number: " + employeeOpeningBalancesObject.getOpeningBalances().getPriorEmployeeNumber() );
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            
        } else if (object.equals("PayrollUkEmployeeLeave")) {
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
                
                // GET All Employee Leave 
                com.xero.models.payrolluk.EmployeeLeaves employeeLeaves = payrollUkApi.getEmployeeLeaves(accessToken, xeroTenantId,employeeId);
                messages.add("GET ALL Employee Leaves - Total: " + employeeLeaves.getLeave().size() );
                
                // GET Single Employee Leave 
                if (employeeLeaves.getLeave().size() > 0) {
                    UUID leaveID = employeeLeaves.getLeave().get(0).getLeaveID();
                    com.xero.models.payrolluk.EmployeeLeaveObject employeeLeave = payrollUkApi.getEmployeeLeave(accessToken, xeroTenantId,employeeId,leaveID);
                    messages.add("GET Single Employee Leave - Description: " + employeeLeave.getLeave().getDescription() );
                    
                    UUID newLeaveID = null;
                 
                    // CREATE Employee Leave 
                    com.xero.models.payrolluk.EmployeeLeave newEmployeeLeave = new com.xero.models.payrolluk.EmployeeLeave();
                    newEmployeeLeave.setDescription("Creating a Desription"); 
                    LocalDate startDate = LocalDate.of(2020, Month.APRIL, 24);
                    newEmployeeLeave.setStartDate(startDate);
                    LocalDate endDate = LocalDate.of(2020, Month.APRIL, 26);
                    newEmployeeLeave.endDate(endDate);
                    newEmployeeLeave.setLeaveTypeID(employeeLeave.getLeave().getLeaveTypeID());
                    com.xero.models.payrolluk.EmployeeLeaveObject createdEmployeeLeave = payrollUkApi.createEmployeeLeave(accessToken, xeroTenantId,employeeId, newEmployeeLeave);
                    messages.add("Create Employee Leave - Description: " + createdEmployeeLeave.getLeave().getDescription() );
                    newLeaveID = createdEmployeeLeave.getLeave().getLeaveID();
                
                    if (newLeaveID != null) {
                  
                        // UPDATE Employee Leave 
                        com.xero.models.payrolluk.EmployeeLeave upEmployeeLeave = new com.xero.models.payrolluk.EmployeeLeave();
                        upEmployeeLeave.setDescription("Creating a Desription"); 
                        LocalDate startDate2 = LocalDate.of(2020, Month.APRIL, 24);
                        upEmployeeLeave.setStartDate(startDate2);
                        LocalDate endDate2 = LocalDate.of(2020, Month.APRIL, 26);
                        upEmployeeLeave.endDate(endDate2);
                        upEmployeeLeave.setLeaveTypeID(employeeLeave.getLeave().getLeaveTypeID());
                        
                        com.xero.models.payrolluk.LeavePeriod period = new com.xero.models.payrolluk.LeavePeriod();
                        period.setNumberOfUnits(1.0);
                        LocalDate periodStartDate = LocalDate.of(2020, Month.APRIL, 20);
                        period.setPeriodStartDate(periodStartDate);
                        LocalDate periodEndDate = LocalDate.of(2020, Month.APRIL, 26);
                        period.setPeriodEndDate(periodEndDate);
                        period.setPeriodStatus(com.xero.models.payrolluk.LeavePeriod.PeriodStatusEnum.APPROVED);
                        upEmployeeLeave.addPeriodsItem(period);
                        com.xero.models.payrolluk.EmployeeLeaveObject updatedEmployeeLeave = payrollUkApi.updateEmployeeLeave(accessToken, xeroTenantId,employeeId,newLeaveID,upEmployeeLeave);
                    }                  
                }
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        
        } else if (object.equals("PayrollUkEmployeeLeaveBalances")) {
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
           
                // GET All Employee Leave Balances
                com.xero.models.payrolluk.EmployeeLeaveBalances employeeLeaveBalances = payrollUkApi.getEmployeeLeaveBalances(accessToken, xeroTenantId, employeeId);
                if (employeeLeaveBalances.getLeaveBalances() != null) {
                    messages.add("GET Leave Balances - Name: " + employeeLeaveBalances.getLeaveBalances().get(0).getName() );
                } else {
                    messages.add("GET Leave Balances - Problem: " + employeeLeaveBalances.getProblem().getDetail() );
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        
        } else if (object.equals("PayrollUkEmployeeStatutoryLeaveBalance")) {
           
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
               
                // GET All Employee Statutory Leave Balances
                LocalDate asOfDate = LocalDate.of(2020, Month.MARCH, 30);
                com.xero.models.payrolluk.EmployeeStatutoryLeaveBalanceObject employeeStatutoryLeaveBalance = payrollUkApi.getEmployeeStatutoryLeaveBalances(accessToken, xeroTenantId, employeeId, "Sick", asOfDate);
                
                if (employeeStatutoryLeaveBalance.getLeaveBalance() != null) {
                    messages.add("GET Statutory Leave Balance - Units: " + employeeStatutoryLeaveBalance.getLeaveBalance().getUnits() );
                } else {
                    messages.add("GET Statutory Leave Balances - Problem: " + employeeStatutoryLeaveBalance.getProblem().getDetail() );
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            
        } else if (object.equals("PayrollUkEmployeeStatutoryLeaveSummary")) {
            try {
            // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
               
                // GET Statutory Leave Summary
                com.xero.models.payrolluk.EmployeeStatutoryLeavesSummaries employeeStatutoryLeaveSummary = payrollUkApi.getStatutoryLeaveSummary(accessToken, xeroTenantId, employeeId,true);
                
                System.out.println(employeeStatutoryLeaveSummary.toString());

//                messages.add("GET Statutory Leave Summary - Status: " + employeeStatutoryLeaveSummary.getStatutoryLeaves().get(0).getStatus() );
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (XeroServerErrorException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("PayrollUkEmployeeStatutorySickLeave")) {
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
                
                com.xero.models.payrolluk.LeaveTypes leaveTypes = payrollUkApi.getLeaveTypes(accessToken, xeroTenantId,null,null);
                UUID leaveTypeID = leaveTypes.getLeaveTypes().get(0).getLeaveTypeID();
               
                // GET Sick Leave ID
                com.xero.models.payrolluk.EmployeeStatutoryLeavesSummaries employeeStatutoryLeaveSummary = payrollUkApi.getStatutoryLeaveSummary(accessToken, xeroTenantId, employeeId,true);
                UUID statutorySickLeaveID = employeeStatutoryLeaveSummary.getStatutoryLeaves().get(0).getStatutoryLeaveID();
                
                // GET Employee Statutory SickLeaves
                com.xero.models.payrolluk.EmployeeStatutorySickLeaveObject employeeStatutoryLeaveSickLeaves = payrollUkApi.getEmployeeStatutorySickLeave(accessToken, xeroTenantId, statutorySickLeaveID);
                messages.add("GET Statutory Sick Leave - StartDate: " + employeeStatutoryLeaveSickLeaves.getStatutorySickLeave().getStartDate() );
                
                // CREATE Statutory SickLeaves
                com.xero.models.payrolluk.EmployeeStatutorySickLeave employeeStatutorySickLeave = new com.xero.models.payrolluk.EmployeeStatutorySickLeave();
                employeeStatutorySickLeave.setLeaveTypeID(UUID.fromString("aab78802-e9d3-4bbd-bc87-df858054988f"));
                employeeStatutorySickLeave.setEmployeeID(employeeId);
                LocalDate startDate = LocalDate.of(2020, Month.APRIL, 21);
                employeeStatutorySickLeave.setStartDate(startDate);
                LocalDate endDate = LocalDate.of(2020, Month.APRIL, 24);
                employeeStatutorySickLeave.endDate(endDate);
                List<String> workPattern = new ArrayList<>();
                workPattern.add("Monday");
                workPattern.add("Tuesday");
                workPattern.add("Wednesday");
                workPattern.add("Thursday");
                workPattern.add("Friday");
                employeeStatutorySickLeave.setWorkPattern(workPattern);
                employeeStatutorySickLeave.setIsPregnancyRelated(false);
                employeeStatutorySickLeave.setSufficientNotice(true);                
                com.xero.models.payrolluk.EmployeeStatutorySickLeaveObject createdEmployeeStatutoryLeaveSickLeaves = payrollUkApi.createEmployeeStatutorySickLeave(accessToken, xeroTenantId, employeeStatutorySickLeave);
                messages.add("CREATED Statutory Leave Sick Leaves - Start Date: " + createdEmployeeStatutoryLeaveSickLeaves.getStatutorySickLeave().getStartDate() );
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
        
        } else if (object.equals("PayrollUkEmployeeLeavePeriods")) {
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
            
                // GET UK Employee Leave Periods
                LocalDate startDate = LocalDate.of(2020, Month.MARCH, 01);
                LocalDate endDate = LocalDate.of(2020, Month.APRIL, 26);
                com.xero.models.payrolluk.LeavePeriods leavePeriods = payrollUkApi.getEmployeeLeavePeriods(accessToken, xeroTenantId, employeeId, startDate, endDate);
             
                messages.add("GET Leave Periods - Period Start Date: " + leavePeriods.getPeriods().get(0).getPeriodStartDate() );
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
        } else if (object.equals("PayrollUkEmployeeLeaveType")) {
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
                
                // GET All Employee Leave Types
                com.xero.models.payrolluk.EmployeeLeaveTypes employeeLeaveTypes = payrollUkApi.getEmployeeLeaveTypes(accessToken, xeroTenantId,employeeId);
                messages.add("Employee Leave Types found " + employeeLeaveTypes.getLeaveTypes().size());
                         
                // GET All Leave Types
                com.xero.models.payrolluk.LeaveTypes leaveTypes = payrollUkApi.getLeaveTypes(accessToken, xeroTenantId,null,null);
                UUID leaveTypeID = leaveTypes.getLeaveTypes().get(4).getLeaveTypeID();
               
                // CREATE Employee Leave Types
                com.xero.models.payrolluk.EmployeeLeaveType leaveType = new com.xero.models.payrolluk.EmployeeLeaveType();
                leaveType.setHoursAccruedAnnually(10.0);
                leaveType.setMaximumToAccrue(80.0);
                leaveType.setRateAccruedHourly(3.5);
                leaveType.setScheduleOfAccrual(com.xero.models.payrolluk.EmployeeLeaveType.ScheduleOfAccrualEnum.BEGINNINGOFCALENDARYEAR);
                leaveType.setLeaveTypeID(leaveTypeID);
                com.xero.models.payrolluk.EmployeeLeaveTypeObject newEmployeeLeaveType = payrollUkApi.createEmployeeLeaveType(accessToken, xeroTenantId, employeeId, leaveType);
                messages.add("CREATED Employee Leave Type ID: " + newEmployeeLeaveType.getLeaveType().getLeaveTypeID() );
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
        
        } else if (object.equals("PayrollUkEmployeePayTemplates")) {
            /*
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
              
               // GET All  Earnings Rates
                com.xero.models.payrolluk.EarningsRates earningRates = payrollUkApi.getEarningsRates(accessToken, xeroTenantId, 1);
                UUID earningsRateID = earningRates.getEarningsRates().get(0).getEarningsRateID();
                UUID earningsRateID2 = earningRates.getEarningsRates().get(1).getEarningsRateID();
            
            // GET All Employee Pay Templates
            com.xero.models.payrolluk.EmployeePayTemplateObject employeePayTemplate = payrollUkApi.getEmployeePayTemplates(accessToken, xeroTenantId, employeeId);
            if (employeePayTemplate.getPayTemplate().getEarningTemplates().size() > 0) 
            {
                messages.add("All Employee Pay Templates found Total: " + employeePayTemplate.getPayTemplate().getEarningTemplates().size() );
                UUID payTemplateEarningsID = employeePayTemplate.getPayTemplate().getEarningTemplates().get(0).getPayTemplateEarningID();
                UUID currentEarningsRateID = employeePayTemplate.getPayTemplate().getEarningTemplates().get(0).getEarningsRateID();
                
                // UPDATE Employee Pay Templates - Earnings Template
                com.xero.models.payrolluk.EarningsTemplate  earningsTemplate = new com.xero.models.payrolluk.EarningsTemplate();
                earningsTemplate.setNumberOfUnits(4.0);
                earningsTemplate.setRatePerUnit(25.0);
                earningsTemplate.setEarningsRateID(currentEarningsRateID);
                com.xero.models.payrolluk.EarningsTemplateObject updatedEmployeePayTemplate = payrollUkApi.updateEmployeeEarningsTemplate(accessToken, xeroTenantId, employeeId, payTemplateEarningsID, earningsTemplate);
                messages.add("Updated Employee Pay Templates Earning Rate found Rate: " + updatedEmployeePayTemplate.getEarningTemplate().getRatePerUnit() );
          
                // DELETE Employee Pay Templates - Earnings Template
                payrollUkApi.deleteEmployeeEarningsTemplate(accessToken, xeroTenantId, employeeId, payTemplateEarningsID);
                messages.add("DELETED Employee Pay Templates Earning Rate" );        
            }

            //UUID brokenGuid = UUID.fromString("4ff1e5cc-9835-40d5-bb18-09fdb118db9c");
            // CREATE MULTIPLE Employee Pay Templates
            com.xero.models.payrolluk.EarningsTemplate newEarningsTemplate = new com.xero.models.payrolluk.EarningsTemplate();
            newEarningsTemplate.setNumberOfUnits(8.0);
            newEarningsTemplate.setRatePerUnit(20.0);
            newEarningsTemplate.setEarningsRateID(earningsRateID);
            
            com.xero.models.payrolluk.EarningsTemplate newEarningsTemplate2 = new com.xero.models.payrolluk.EarningsTemplate();
            newEarningsTemplate2.setNumberOfUnits(8.0);
            newEarningsTemplate2.setRatePerUnit(0.0);
            newEarningsTemplate2.setEarningsRateID(earningsRateID2);
            
            List<com.xero.models.payrolluk.EarningsTemplate> newEarningsTemplates = new ArrayList<>();
            newEarningsTemplates.add(newEarningsTemplate);
            newEarningsTemplates.add(newEarningsTemplate2);
            
            com.xero.models.payrolluk.EmployeePayTemplateObject multiCreateEmployeePayTemplate = payrollUkApi.createMultipleEmployeeEarningsTemplate(accessToken, xeroTenantId, employeeId, newEarningsTemplates);
            messages.add("CREATED Multiple Employee Pay Templates Earning Rate found Rate: " + multiCreateEmployeePayTemplate.getPayTemplate().getEarningTemplates().get(0).getRatePerUnit() );
           

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            */
            
            //503
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(1).getEmployeeID();
             
                // GET All  Earnings Rates
                com.xero.models.payrolluk.EarningsRates earningRates = payrollUkApi.getEarningsRates(accessToken, xeroTenantId, 1);
                UUID earningsRateID = earningRates.getEarningsRates().get(0).getEarningsRateID();
              
                //CREATE Employee Pay Templates 
                com.xero.models.payrolluk.EarningsTemplate  earningsTemplate = new com.xero.models.payrolluk.EarningsTemplate();
                earningsTemplate.setName("My New One");
                earningsTemplate.setNumberOfUnits(8.0);
                earningsTemplate.setRatePerUnit(20.0);
                earningsTemplate.setEarningsRateID(earningsRateID);           
                com.xero.models.payrolluk.EarningsTemplateObject createdEmployeePayTemplate = payrollUkApi.createEmployeeEarningsTemplate(accessToken, xeroTenantId, employeeId, earningsTemplate);
                System.out.println(createdEmployeePayTemplate.toString());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }  
            
        } else if (object.equals("PayrollUkEmployerPensions")) {
            try {
                // GET All Benefits (EmployerPensions)
                com.xero.models.payrolluk.Benefits benefits = payrollUkApi.getBenefits(accessToken, xeroTenantId, 1);
                messages.add("GET All Benefits (Employer Pensions) found Name: " + benefits.getBenefits().get(0).getName() );
                UUID benefitID = benefits.getBenefits().get(0).getId();
                
                // GET EXPENSE ACCOUNT
                where = "STATUS==\"ACTIVE\"&&Type==\"EXPENSE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                UUID expenseAccountID = accounts.getAccounts().get(0).getAccountID();
                
                // GET EXPENSE ACCOUNT
                where = "STATUS==\"ACTIVE\"&&Type==\"CURRLIAB\"";
                Accounts liabilityAccounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                UUID liabilityAccountID = liabilityAccounts.getAccounts().get(0).getAccountID();
                
                // CREATE Benefits (EmployerPensions)
                com.xero.models.payrolluk.Benefit newBenefit = new com.xero.models.payrolluk.Benefit();
                newBenefit.setName("My Big Bennie");
                newBenefit.setCategory(com.xero.models.payrolluk.Benefit.CategoryEnum.STAKEHOLDERPENSION);
                newBenefit.setLiabilityAccountId(liabilityAccountID);
                newBenefit.setExpenseAccountId(expenseAccountID);
                newBenefit.setCalculationType(com.xero.models.payrolluk.Benefit.CalculationTypeEnum.PERCENTAGEOFGROSS);
                newBenefit.setPercentage(25.0);
                newBenefit.setStandardAmount(50.0);
                
                com.xero.models.payrolluk.BenefitObject createdBenefit = payrollUkApi.createBenefit(accessToken, xeroTenantId, newBenefit);
                messages.add("CREATE Benefits (Employer Pensions) found Name: " + benefits.getBenefits().get(0).getName() );
                
                // GET Single Benefits (EmployerPensions)
                com.xero.models.payrolluk.BenefitObject benefit = payrollUkApi.getBenefit(accessToken, xeroTenantId, benefitID);
                messages.add("GET Single Benefits (Employer Pensions) found Name: " + benefits.getBenefits().get(0).getName() );
           
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkDeductions")) {
            try {
                // GET All Deductions
                com.xero.models.payrolluk.Deductions deductions = payrollUkApi.getDeductions(accessToken, xeroTenantId, 1);
                messages.add("GET All Deductions Total: " + deductions.getPagination().getItemCount() );
                
                // GET Single Deduction
                UUID deductionId = deductions.getDeductions().get(0).getDeductionId();
                com.xero.models.payrolluk.DeductionObject deduction = payrollUkApi.getDeduction(accessToken, xeroTenantId, deductionId);
                messages.add("GET Single Deduction Name: " + deduction.getDeduction().getDeductionName() );
                
                // GET EXPENSE ACCOUNT
                where = "STATUS==\"ACTIVE\"&&Type==\"CURRLIAB\"";
                Accounts liabilityAccounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                UUID liabilityAccountID = liabilityAccounts.getAccounts().get(0).getAccountID();
                
                // CREATE Deduction
                com.xero.models.payrolluk.Deduction newDeduction = new com.xero.models.payrolluk.Deduction();
                newDeduction.deductionName("My new deducation");
                newDeduction.setDeductionCategory(com.xero.models.payrolluk.Deduction.DeductionCategoryEnum.SALARYSACRIFICE);
                newDeduction.setLiabilityAccountId(liabilityAccountID);
                newDeduction.setCalculationType(com.xero.models.payrolluk.Deduction.CalculationTypeEnum.FIXEDAMOUNT);
                com.xero.models.payrolluk.DeductionObject createdDeduction = payrollUkApi.createDeduction(accessToken, xeroTenantId, newDeduction);
                messages.add("GET Single Deduction Name: " + createdDeduction.getDeduction().getDeductionName() );
           
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkEarningsOrders")) {
            try {
                // GET All EarningsOrders
                com.xero.models.payrolluk.EarningsOrders earningsOrders = payrollUkApi.getEarningsOrders(accessToken, xeroTenantId, 1);
                messages.add("GET All EarningsOrders Total: " + earningsOrders.getPagination().getItemCount() );
                
                // GET Single EarningsOrders
                UUID id = earningsOrders.getStatutoryDeductions().get(0).getId();
                com.xero.models.payrolluk.EarningsOrderObject earningsOrder = payrollUkApi.getEarningsOrder(accessToken, xeroTenantId, id);
                messages.add("GET Single EarningsOrders Name: " + earningsOrder.getStatutoryDeduction().getName() );
          
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
            
        } else if (object.equals("PayrollUkEarningRates")) {
            try {
                // GET All  Earnings Rates
                com.xero.models.payrolluk.EarningsRates earningRates = payrollUkApi.getEarningsRates(accessToken, xeroTenantId, 1);
                messages.add("GET All Earnings Rates found Total: " + earningRates.getPagination().getItemCount() );
                
                UUID earningsRateID = earningRates.getEarningsRates().get(0).getEarningsRateID();
                // GET Single Earnings Rates
                com.xero.models.payrolluk.EarningsRateObject earningRate = payrollUkApi.getEarningsRate(accessToken, xeroTenantId, earningsRateID);
                messages.add("GET single Earnings Rates found Name: " + earningRates.getEarningsRates().get(0).getName() );
                
                // GET Expense ACCOUNT
                where = "STATUS==\"ACTIVE\"&&Type==\"EXPENSE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                UUID expenseAccountID = accounts.getAccounts().get(0).getAccountID();
                
                // CREATE Earnings Rates
                com.xero.models.payrolluk.EarningsRate newEarningsRate = new com.xero.models.payrolluk.EarningsRate();
                newEarningsRate.setExpenseAccountID(expenseAccountID);
                newEarningsRate.setName("My Earnings Rate");
                newEarningsRate.setEarningsType(com.xero.models.payrolluk.EarningsRate.EarningsTypeEnum.REGULAREARNINGS);
                newEarningsRate.setRateType(com.xero.models.payrolluk.EarningsRate.RateTypeEnum.RATEPERUNIT);
                newEarningsRate.setTypeOfUnits("hours");
                
                com.xero.models.payrolluk.EarningsRateObject createdEarningRates = payrollUkApi.createEarningsRate(accessToken, xeroTenantId, newEarningsRate);
                messages.add("CREATE Earnings Rates found Name: " + createdEarningRates.getEarningsRate().getName() );
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkLeaveType")) {
            try {
                // GET All Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeId = employees.getEmployees().get(0).getEmployeeID();
               
                // GET All  Leave Types
                com.xero.models.payrolluk.LeaveTypes leaveTypes = payrollUkApi.getLeaveTypes(accessToken, xeroTenantId,null,null);
                messages.add("GET All Leave Types found Total: " + leaveTypes.getPagination().getItemCount() );
                
                // GET All  Leave Types
                UUID leaveTypeId = leaveTypes.getLeaveTypes().get(0).getLeaveTypeID();
                com.xero.models.payrolluk.LeaveTypeObject leaveType = payrollUkApi.getLeaveType(accessToken, xeroTenantId,leaveTypeId);
                messages.add("GET Single Leave Types found Total: " + leaveType.getLeaveType().getName() );
                
                // CREATE Leave Type
                com.xero.models.payrolluk.LeaveType newLeaveType = new com.xero.models.payrolluk.LeaveType();
                newLeaveType.setName("My " + this.loadRandChar() + " Leave");
                newLeaveType.setIsPaidLeave(false);
                newLeaveType.setShowOnPayslip(true);
                com.xero.models.payrolluk.LeaveTypeObject createdLeaveType = payrollUkApi.createLeaveType(accessToken, xeroTenantId, newLeaveType);
                messages.add("CREATED Leave Type found Name: " + createdLeaveType.getLeaveType().getName() );
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkReimbursements")) {
            try {
                // GET ALL Reimbursements for UK Payroll
                com.xero.models.payrolluk.Reimbursements reimbursements = payrollUkApi.getReimbursements(accessToken, xeroTenantId, 1);
                UUID reimbursementID = reimbursements.getReimbursements().get(0).getReimbursementID();
                messages.add("GET All Reimbursement found total: " + reimbursements.getPagination().getItemCount() );
                //System.out.println(reimbursements.toString());
                
                // GET Single Reimbursements for UK Payroll
                com.xero.models.payrolluk.ReimbursementObject reimbursement = payrollUkApi.getReimbursement(accessToken, xeroTenantId, reimbursementID);
                messages.add("GET Single Reimbursement found Name: " + reimbursement.getReimbursement().getName() );
                //System.out.println(reimbursement.toString());
               
                // GET Settings for UK Payroll
                com.xero.models.payrolluk.Settings settings = payrollUkApi.getSettings(accessToken, xeroTenantId);
                UUID accountID = settings.getSettings().getAccounts().get(0).getAccountID();
               
                // CREATE Reimbursement for UK Payroll
                com.xero.models.payrolluk.Reimbursement newReimbursement = new com.xero.models.payrolluk.Reimbursement();
                newReimbursement.setAccountID(accountID);
                newReimbursement.setName("My new Reimburse");
                com.xero.models.payrolluk.ReimbursementObject createdReimbursement = payrollUkApi.createReimbursement(accessToken, xeroTenantId, newReimbursement);
                messages.add("CREATED Reimbursement found Name: " + createdReimbursement.getReimbursement().getName() );
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkTimesheets")) {
            try {
                // GET ALL Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeID = employees.getEmployees().get(0).getEmployeeID();
                
                // GET ALL PayRun Calendars for UK Payroll
                com.xero.models.payrolluk.PayRunCalendars payRunCalendars = payrollUkApi.getPayRunCalendars(accessToken, xeroTenantId, 1);
                UUID payrollCalendarId = payRunCalendars.getPayRunCalendars().get(0).getPayrollCalendarID();
                
                // GET Payruns for UK Payroll
                com.xero.models.payrolluk.PayRuns payRuns = payrollUkApi.getPayRuns(accessToken, xeroTenantId, null,null);
                UUID payRunID = payRuns.getPayRuns().get(0).getPayRunID();
                
                // GET ALL PaySlips for UK Payroll
                com.xero.models.payrolluk.Payslips paySlips = payrollUkApi.getPayslips(accessToken, xeroTenantId, payRunID, 1);
                UUID earningsRateID = paySlips.getPaySlips().get(0).getEarningsLines().get(0).getEarningsRateID();
                
                // GET ALL Time sheets for UK Payroll
                com.xero.models.payrolluk.Timesheets timesheets = payrollUkApi.getTimesheets(accessToken, xeroTenantId, 1, null, null);
                UUID timesheetID = timesheets.getTimesheets().get(0).getTimesheetID();
                messages.add("GET All Timesheets found total: " + timesheets.getPagination().getItemCount() );
                
                // GET Single Timesheet for UK Payroll
                com.xero.models.payrolluk.TimesheetObject oneTimesheet = payrollUkApi.getTimesheet(accessToken, xeroTenantId, timesheetID);
                messages.add("GET Single Timesheets found Status: " + oneTimesheet.getTimesheet().getStatus() );
                
                // CREATE Timesheet Line for UK Payroll
                com.xero.models.payrolluk.TimesheetLine timesheetLine = new com.xero.models.payrolluk.TimesheetLine();
                LocalDate date03 = LocalDate.of(2020, Month.APRIL, 14);
                timesheetLine.setDate(date03);
                timesheetLine.setEarningsRateID(earningsRateID);
                timesheetLine.setNumberOfUnits(1.0);
                com.xero.models.payrolluk.TimesheetLineObject newTimesheetLine = payrollUkApi.createTimesheetLine(accessToken, xeroTenantId, timesheetID, timesheetLine);
                messages.add("CREATED new Timesheet Line found Units: " + newTimesheetLine.getTimesheetLine().getNumberOfUnits() );
                
                UUID timesheetLineID = oneTimesheet.getTimesheet().getTimesheetLines().get(0).getTimesheetLineID();
                
                // UPDATE Timesheet Line for UK Payroll
                com.xero.models.payrolluk.TimesheetLine uptimesheetLine = new com.xero.models.payrolluk.TimesheetLine();
                LocalDate date04 = LocalDate.of(2020, Month.APRIL, 14);
                uptimesheetLine.setDate(date04);
                uptimesheetLine.setEarningsRateID(earningsRateID);
                uptimesheetLine.setNumberOfUnits(2.0);
                com.xero.models.payrolluk.TimesheetLineObject updatedTimesheetLine = payrollUkApi.updateTimesheetLine(accessToken, xeroTenantId, timesheetID, timesheetLineID, uptimesheetLine);
                messages.add("UPDATED existing Timesheet Line found Units: " + updatedTimesheetLine.getTimesheetLine().getNumberOfUnits() );
                //System.out.println(updatedTimesheetLine.toString());
                
                // APPROVE Timesheet for UK Payroll
                com.xero.models.payrolluk.TimesheetObject approvedTimesheet = payrollUkApi.approveTimesheet(accessToken, xeroTenantId, timesheetID);
                messages.add("APPROVE Timesheet found Status: " + approvedTimesheet.getTimesheet().getStatus() );
                //System.out.println(approvedTimesheet.toString());
                
                // REVERT TO DRAFT Timesheet for UK Payroll
                com.xero.models.payrolluk.TimesheetObject revertedTimesheet = payrollUkApi.revertTimesheet(accessToken, xeroTenantId, timesheetID);
                messages.add("REVERTED Timesheet found Status: " + revertedTimesheet.getTimesheet().getStatus() );
                //System.out.println(revertedTimesheet.toString());
                
                // DELETE Single Timesheet for UK Payroll
                payrollUkApi.deleteTimesheet(accessToken, xeroTenantId, timesheetID);
                messages.add("DELETED Timesheet");  
                
                // CREATE Timesheet for UK Payroll
                com.xero.models.payrolluk.Timesheet newTimesheet = new com.xero.models.payrolluk.Timesheet();
                newTimesheet.setEmployeeID(employeeID);
                newTimesheet.setPayrollCalendarID(payrollCalendarId);
                LocalDate startDate = LocalDate.of(2020, Month.APRIL, 13);
                newTimesheet.setStartDate(startDate);
                LocalDate endDate = LocalDate.of(2020, Month.APRIL, 19);
                newTimesheet.setEndDate(endDate);
                
                List<com.xero.models.payrolluk.TimesheetLine> timesheetLines = new ArrayList<>();
                com.xero.models.payrolluk.TimesheetLine timesheetLine01 = new com.xero.models.payrolluk.TimesheetLine();
                timesheetLine01.setNumberOfUnits(8.0);
                LocalDate date01 = LocalDate.of(2020, Month.APRIL, 13);
                timesheetLine01.setDate(date01);
                timesheetLine01.setEarningsRateID(earningsRateID);
                timesheetLines.add(timesheetLine01);
                
                com.xero.models.payrolluk.TimesheetLine timesheetLine02 = new com.xero.models.payrolluk.TimesheetLine();
                timesheetLine02.setNumberOfUnits(6.0);
                LocalDate date02 = LocalDate.of(2020, Month.APRIL, 15);
                timesheetLine02.setDate(date02);
                timesheetLine02.setEarningsRateID(earningsRateID);
                timesheetLines.add(timesheetLine02);
                newTimesheet.setTimesheetLines(timesheetLines);
                
                com.xero.models.payrolluk.TimesheetObject createdTimesheets = payrollUkApi.createTimesheet(accessToken, xeroTenantId, newTimesheet);
                messages.add("CREATED Timesheet found Status: " + createdTimesheets.getTimesheet().getStatus() );
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkPaymentMethods")) {
            try {
                // GET ALL Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeID = employees.getEmployees().get(0).getEmployeeID();
                
                // GET Employee Payment Methods for UK Payroll
                com.xero.models.payrolluk.PaymentMethodObject paymentMethodObject = payrollUkApi.getEmployeePaymentMethod(accessToken, xeroTenantId, employeeID);
                messages.add("GET all Employee Payment Methods found Account Name: " + paymentMethodObject.getPaymentMethod().getBankAccounts().get(0).getAccountName() );
                
                // CREATE Employee Payment Methods for UK Payroll
                com.xero.models.payrolluk.PaymentMethod newPaymentMethod = new com.xero.models.payrolluk.PaymentMethod();
                newPaymentMethod.setPaymentMethod(com.xero.models.payrolluk.PaymentMethod.PaymentMethodEnum.ELECTRONICALLY);
                List<com.xero.models.payrolluk.BankAccount> bankAccounts = new ArrayList<>();
                com.xero.models.payrolluk.BankAccount bankAccount = new com.xero.models.payrolluk.BankAccount();
                bankAccount.setAccountName("Sid BofA");
                bankAccount.setAccountNumber("24987654");
                bankAccount.setSortCode("287654");
                bankAccounts.add(bankAccount);
                newPaymentMethod.setBankAccounts(bankAccounts);
                com.xero.models.payrolluk.PaymentMethodObject createdPaymentMethodObject = payrollUkApi.createEmployeePaymentMethod(accessToken, xeroTenantId, employeeID, newPaymentMethod);
                messages.add("CREATED Employee Payment Methods found Account Name: " + paymentMethodObject.getPaymentMethod().getBankAccounts().get(0).getAccountName() );
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkPayRunCalendars")) {
            try {
                // GET ALL PayRun Calendars for UK Payroll
                com.xero.models.payrolluk.PayRunCalendars payRunCalendars = payrollUkApi.getPayRunCalendars(accessToken, xeroTenantId, 1);
                //messages.add("All Payrun Calendars found total: " + payRunCalendars.getPagination().getItemCount() );
    
                // GET Single PayRun Calendars for UK Payroll
                UUID payRunCalendarID = payRunCalendars.getPayRunCalendars().get(0).getPayrollCalendarID();
                com.xero.models.payrolluk.PayRunCalendarObject payRunCalendarObject = payrollUkApi.getPayRunCalendar(accessToken, xeroTenantId, payRunCalendarID);
                //messages.add("Payrun Calendar found name: " + payRunCalendarObject.getPayRunCalendars().getName() );
                //System.out.println(payRunCalendarObject.toString());
                
                // GET CREATE PayRun Calendars for UK Payroll
                com.xero.models.payrolluk.PayRunCalendar newPayRunCalendar = new com.xero.models.payrolluk.PayRunCalendar();
                newPayRunCalendar.setCalendarType(com.xero.models.payrolluk.PayRunCalendar.CalendarTypeEnum.WEEKLY);
                newPayRunCalendar.setName("My Weekly Cal");
                LocalDate periodStartDate = LocalDate.of(2020, Month.MAY, 01);
                newPayRunCalendar.setPeriodStartDate(periodStartDate);
                LocalDate paymentDate = LocalDate.of(2020, Month.MAY, 15);
                newPayRunCalendar.setPaymentDate(paymentDate);
                com.xero.models.payrolluk.PayRunCalendarObject createdPayRunCalendar = payrollUkApi.createPayRunCalendar(accessToken, xeroTenantId, newPayRunCalendar);
                messages.add("Payrun Calendar found name: " + payRunCalendarObject.getPayRunCalendar().getName() );
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
            
        } else if (object.equals("PayrollUkSalaryAndWages")) {
            try {
                // GET ALL Employees
                com.xero.models.payrolluk.Employees employees = payrollUkApi.getEmployees(accessToken, xeroTenantId, null, null, null);
                UUID employeeID = employees.getEmployees().get(0).getEmployeeID();
                
                // GET ALL Salary And Wages for UK Payroll
                com.xero.models.payrolluk.SalaryAndWages salaryAndWages = payrollUkApi.getEmployeeSalaryAndWages(accessToken, xeroTenantId, employeeID, 1);
                messages.add("GET all Salary and Wages found total: " + salaryAndWages.getPagination().getItemCount() );
    
                // GET Single Salary And Wages for UK Payroll
                UUID salaryAndWagesID = salaryAndWages.getSalaryAndWages().get(0).getSalaryAndWagesID();
                com.xero.models.payrolluk.SalaryAndWages oneSalaryAndWage = payrollUkApi.getEmployeeSalaryAndWage(accessToken, xeroTenantId, employeeID, salaryAndWagesID);
                messages.add("GET Single Salary and Wages found Status: " + oneSalaryAndWage.getSalaryAndWages().get(0).getStatus() );
    
                // GET Payruns for UK Payroll
                com.xero.models.payrolluk.PayRuns payRuns = payrollUkApi.getPayRuns(accessToken, xeroTenantId, null,null);
                UUID payRunID = payRuns.getPayRuns().get(0).getPayRunID();
    
                // GET ALL PaySlips for UK Payroll
                com.xero.models.payrolluk.Payslips paySlips = payrollUkApi.getPayslips(accessToken, xeroTenantId, payRunID, 1);
                UUID earningsRateID = paySlips.getPaySlips().get(0).getEarningsLines().get(0).getEarningsRateID();
               
                // GET Create Salary And Wages for UK Payroll
                com.xero.models.payrolluk.SalaryAndWage newSalaryAndWage = new com.xero.models.payrolluk.SalaryAndWage();
                newSalaryAndWage.setEarningsRateID(earningsRateID);
                newSalaryAndWage.setNumberOfUnitsPerWeek(2.0); 
                newSalaryAndWage.setRatePerUnit(10.0);
                newSalaryAndWage.setNumberOfUnitsPerDay(2.0);
                LocalDate effectiveFrom = LocalDate.of(2020, Month.MAY, 01);
                newSalaryAndWage.setEffectiveFrom(effectiveFrom);
                newSalaryAndWage.setAnnualSalary(100.0);
                newSalaryAndWage.setStatus(com.xero.models.payrolluk.SalaryAndWage.StatusEnum.ACTIVE);
                newSalaryAndWage.setPaymentType(com.xero.models.payrolluk.SalaryAndWage.PaymentTypeEnum.SALARY);
                
                com.xero.models.payrolluk.SalaryAndWageObject createdSalaryAndWages = payrollUkApi.createEmployeeSalaryAndWage(accessToken, xeroTenantId, employeeID, newSalaryAndWage);
                messages.add("CREATED Salary and Wages found Status: " + createdSalaryAndWages.getSalaryAndWages().getStatus() );
                
                // GET Create Salary And Wages for UK Payroll
                UUID salaryAndWagesId = createdSalaryAndWages.getSalaryAndWages().getSalaryAndWagesID();
                com.xero.models.payrolluk.SalaryAndWage upSalaryAndWage = new com.xero.models.payrolluk.SalaryAndWage();
                upSalaryAndWage.setEarningsRateID(earningsRateID);
                upSalaryAndWage.setNumberOfUnitsPerWeek(3.0); 
                upSalaryAndWage.setRatePerUnit(11.0);
                newSalaryAndWage.setNumberOfUnitsPerDay(3.0);
                LocalDate effectiveFromUp = LocalDate.of(2020, Month.MAY, 15);
                upSalaryAndWage.setEffectiveFrom(effectiveFromUp);
                upSalaryAndWage.setAnnualSalary(101.0);
                upSalaryAndWage.setStatus(com.xero.models.payrolluk.SalaryAndWage.StatusEnum.ACTIVE);
                upSalaryAndWage.setPaymentType(com.xero.models.payrolluk.SalaryAndWage.PaymentTypeEnum.SALARY);
                
                com.xero.models.payrolluk.SalaryAndWageObject updatedSalaryAndWages = payrollUkApi.updateEmployeeSalaryAndWage(accessToken, xeroTenantId, employeeID, salaryAndWagesId, upSalaryAndWage);
                messages.add("UPDATED Salary and Wages found Status: " + updatedSalaryAndWages.getSalaryAndWages().getStatus() );
                
                payrollUkApi.deleteEmployeeSalaryAndWage(accessToken, xeroTenantId, employeeID, salaryAndWagesId);
                messages.add("DELETE Salary and Wages" );
          
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
            
        } else if (object.equals("PayrollUkPayruns")) {
            
            try {
                // GET ALL Payruns for UK Payroll
                com.xero.models.payrolluk.PayRuns payRuns = payrollUkApi.getPayRuns(accessToken, xeroTenantId, null,"Draft");
                messages.add("GET all PayRuns found total: " + payRuns.getPagination().getItemCount() );            
              
                // GET Single Payrun for UK Payroll
                UUID payRunID = payRuns.getPayRuns().get(0).getPayRunID();
                com.xero.models.payrolluk.PayRunObject singlePayRun = payrollUkApi.getPayRun(accessToken, xeroTenantId, payRunID);
                messages.add("GET Single PayRuns found Status: " + singlePayRun.getPayRun().getPayRunStatus() );            

                // UPDATE Payrun for UK Payroll
                com.xero.models.payrolluk.PayRun payRun = new com.xero.models.payrolluk.PayRun();
                LocalDate paymentDate = LocalDate.of(2020, Month.MAY, 01);
                payRun.setPaymentDate(paymentDate);
                com.xero.models.payrolluk.PayRunObject upPayRuns = payrollUkApi.updatePayRun(accessToken, xeroTenantId, payRunID, payRun);
                messages.add("UPDATE PayRun found Status: " + payRuns.getPayRuns().get(0).getPayRunStatus() );            
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
           
        } else if (object.equals("PayrollUkPayslips")) {
            try {  
                // GET Payruns for UK Payroll
                com.xero.models.payrolluk.PayRuns payRuns = payrollUkApi.getPayRuns(accessToken, xeroTenantId, null,null);
                UUID payRunID = payRuns.getPayRuns().get(0).getPayRunID();
    
                // GET ALL PaySlips for UK Payroll
                com.xero.models.payrolluk.Payslips paySlips = payrollUkApi.getPayslips(accessToken, xeroTenantId, payRunID, 1);
                messages.add("GET All Payslips in PayRun found for first name: " + paySlips.getPaySlips().get(0).getFirstName() );            
                
                // GET Single PaySlip for UK Payroll
                UUID payslipID = paySlips.getPaySlips().get(0).getPaySlipID();
                com.xero.models.payrolluk.PayslipObject paySlip = payrollUkApi.getPaySlip(accessToken, xeroTenantId, payslipID);
                messages.add("GET Single Payslip found for first name: " + paySlip.getPaySlip().getFirstName() );            
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollUkSettings")) {
            try {
                // GET Settings for UK Payroll
                com.xero.models.payrolluk.Settings settings = payrollUkApi.getSettings(accessToken, xeroTenantId);
                messages.add("GET Settings found first Account name: " + settings.getSettings().getAccounts().get(0).getName());            
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }              
        } else if (object.equals("PayrollUkTrackingCategories")) {
            try {
                // GET Tracking Categories used by UK Payroll
                com.xero.models.payrolluk.TrackingCategories trackingCategories = payrollUkApi.getTrackingCategories(accessToken, xeroTenantId);
                messages.add("GET Tracking category for Timesheets found" + trackingCategories.getTrackingCategories().getTimesheetTrackingCategoryID());            

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            } 
        } else if (object.equals("PayrollAuEmployees")) {
            
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
                
                // GET ALL Employees
                com.xero.models.payrollau.Employees employees = payrollAuApi.getEmployees(accessToken, xeroTenantId, null, null, null, null);
                messages.add("Total employee count: " + employees.getEmployees().size());
                
                UUID employeeID = employees.getEmployees().get(0).getEmployeeID();
                com.xero.models.payrollau.Employees oneEmployees = payrollAuApi.getEmployee(accessToken, xeroTenantId, employeeID);
                messages.add("Get single Employee - Last Updated: " + oneEmployees.getEmployees().get(0).getUpdatedDateUTCAsDate().toString());
//                messages.add("Get single Employee - Termination date: " +oneEmployees.getEmployees().get(0).getTerminationDateAsDate().toString());
//                messages.add("Employee : " + oneEmployees.getEmployees().get(0).getFirstName() + " and " + oneEmployees.getEmployees().get(0).getTerminationDateAsDate() );
                    
                // GET ALL PayItems
                PayItems payItems = payrollAuApi.getPayItems(accessToken, xeroTenantId, null, null, null, null);
                UUID ordinaryEarningsRateID = payItems.getPayItems().getEarningsRates().get(0).getEarningsRateID();     
                UUID deductionTypeID = payItems.getPayItems().getDeductionTypes().get(0).getDeductionTypeID();
        
                //CREATE NEW Employee
                List<com.xero.models.payrollau.Employee> newEmployees = new ArrayList<>();
                
                com.xero.models.payrollau.Employee newEmployee = new com.xero.models.payrollau.Employee();
                newEmployee.setFirstName("Harry");
                newEmployee.setMiddleNames("James");
                newEmployee.setLastName("Potter");
                newEmployee.setEmail("albus" + this.loadRandomNum() + "@hogwarts.edu");
                    
                LocalDate birthDate = LocalDate.of(2000, Month.JANUARY, 1);
                newEmployee.setDateOfBirth(birthDate);
                    
                LocalDate startDate = LocalDate.of(2020, Month.JANUARY, 10);
                newEmployee.setStartDate(startDate);
                   
                HomeAddress homeAddress = new HomeAddress();
                homeAddress.setAddressLine1("101 Green St");
                homeAddress.setCity("Island Bay");
                homeAddress.setRegion(com.xero.models.payrollau.State.NSW);
                homeAddress.setCountry("AUSTRALIA");
                homeAddress.setPostalCode("6023");
                newEmployee.setHomeAddress(homeAddress);
           
                newEmployee.setGender(com.xero.models.payrollau.Employee.GenderEnum.M);
                newEmployee.setIsAuthorisedToApproveLeave(true);
                newEmployee.setIsAuthorisedToApproveTimesheets(true);
                newEmployee.setClassification("corporate");
                newEmployee.setJobTitle("Regional Manager");
                newEmployee.setMobile("555-1212");
                newEmployee.setPhone("444-2323");
                newEmployee.setStatus(com.xero.models.payrollau.EmployeeStatus.ACTIVE);
                newEmployee.setOrdinaryEarningsRateID(ordinaryEarningsRateID);
                newEmployees.add(newEmployee);
                
                
                com.xero.models.payrollau.Employee newEmployee2 = new com.xero.models.payrollau.Employee();
                newEmployee2.setFirstName("Fred");
                newEmployee2.setMiddleNames("James");
                newEmployee2.setLastName("Potter");
                newEmployee2.setEmail("boo" + this.loadRandomNum() + "@hogwarts.edu");
                newEmployee2.setDateOfBirth(birthDate);
                newEmployee2.setStartDate(startDate);
                newEmployee2.setHomeAddress(homeAddress);
                newEmployee2.setGender(com.xero.models.payrollau.Employee.GenderEnum.M);
                newEmployee2.setIsAuthorisedToApproveLeave(true);
                newEmployee2.setIsAuthorisedToApproveTimesheets(true);
                newEmployee2.setClassification("corporate");
                newEmployee2.setJobTitle("Regional Manager");
                newEmployee2.setMobile("555-1212");
                newEmployee2.setPhone("444-2323");
                newEmployee2.setStatus(com.xero.models.payrollau.EmployeeStatus.ACTIVE);
                newEmployee2.setOrdinaryEarningsRateID(ordinaryEarningsRateID);
                newEmployees.add(newEmployee);
                
                   
                com.xero.models.payrollau.Employees empCreated = payrollAuApi.createEmployee(accessToken, xeroTenantId, newEmployees);
                messages.add("Employee created with Date of Birth: " + empCreated.getEmployees().get(0).getDateOfBirthAsDate());
                
                
                
                
                //LocalDate newDOB = empCreated.getEmployees().get(0).getDateOfBirthAsDate();
                                 
                //UUID employeeId = empCreated.getEmployees().get(0).getEmployeeID();
                //List<com.xero.models.payrollau.Employee> upEmployees = new ArrayList<>();
                //com.xero.models.payrollau.Employee upEmployee = new com.xero.models.payrollau.Employee();
                //upEmployee.setMiddleNames("Frank");
                //upEmployees.add(upEmployee);
                 
                //com.xero.models.payrollau.Employees empUpdated = payrollAuApi.updateEmployee(accessToken, xeroTenantId, employeeId, upEmployees);
                //System.out.println(empUpdated.toString());
              
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
           
         } else if (object.equals("PayrollAuLeaveApplications")) {
            
            try {
                com.xero.models.payrollau.Employees employees = payrollAuApi.getEmployees(accessToken, xeroTenantId, null, null, null, null);
                UUID employeeID = employees.getEmployees().get(0).getEmployeeID();
                UUID employeeID2 = employees.getEmployees().get(2).getEmployeeID();
                
                // GET Leave Applications
                LeaveApplications leaveApplications = payrollAuApi.getLeaveApplications(accessToken, xeroTenantId, null, null, null, null);
                
                UUID leaveApplicationId = leaveApplications.getLeaveApplications().get(0).getLeaveApplicationID();
                UUID leaveTypeId = leaveApplications.getLeaveApplications().get(0).getLeaveTypeID();
                
                // GET ONE Leave Applications
                LeaveApplications oneleaveApplications = payrollAuApi.getLeaveApplication(accessToken, xeroTenantId, leaveApplicationId);
               
                // CREATE new Leave Application
                LeaveApplication newLeaveApplication = new LeaveApplication();
                List<LeaveApplication> newLeaveApplications = new ArrayList<>();
                          
                newLeaveApplication.setEmployeeID(employeeID);
                newLeaveApplication.setLeaveTypeID(leaveTypeId);
                newLeaveApplication.setStartDate(LocalDate.of(2020,5, 5));
                newLeaveApplication.setEndDate(LocalDate.of(2020,5, 11));
                newLeaveApplication.setTitle("Hello World");
                newLeaveApplications.add(newLeaveApplication);

                
                // CREATE new Leave Application
                /*
                LeaveApplication newLeaveApplication2 = new LeaveApplication();
                newLeaveApplication2.setEmployeeID(employeeID);
                newLeaveApplication2.setLeaveTypeID(leaveTypeId);
                newLeaveApplication2.setStartDate(LocalDate.of(2019,1, 11));
                newLeaveApplication2.setEndDate(LocalDate.of(2019,10, 31));
                newLeaveApplication2.setTitle("Hello World");
                newLeaveApplications.add(newLeaveApplication2);
                */
                
                LeaveApplications createdLeaveApplications = payrollAuApi.createLeaveApplication(accessToken, xeroTenantId, newLeaveApplications);
               
                // UPDATE Leave Application
                /*
                LeaveApplication upLeaveApplication = new LeaveApplication();
                List<LeaveApplication> upLeaveApplications = new ArrayList<>();
                upLeaveApplication.setDescription("My updated Description");
                upLeaveApplication.setEmployeeID(employeeID);
                upLeaveApplication.setLeaveTypeID(leaveTypeId);
                upLeaveApplication.setStartDate(LAStartDateFormated);
                upLeaveApplication.setEndDate(LAendDateFormated);
               
                upLeaveApplications.add(upLeaveApplication);
                LeaveApplications updatedLeaveApplications = payrollAuApi.updateLeaveApplication(accessToken, xeroTenantId, leaveApplicationId, upLeaveApplications);
                */
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
            
         } else  if (object.equals("PayrollAuPayItems")) {
            
             try {
                // GET PayITems
                PayItems payItems = payrollAuApi.getPayItems(accessToken, xeroTenantId, null, null, null, null);
                UUID ordinaryEarningsRateID = payItems.getPayItems().getEarningsRates().get(0).getEarningsRateID();   
                //messages.add("GET PayItems rate:"      + payItems.getPayItems().getEarningsRates().get(0).getRatePerUnit()  );
                
                messages.add("GET PayItems Earnings rate:"      + payItems.getPayItems().getEarningsRates().size()  );
                messages.add("GET PayItems DeductionTypes:"      + payItems.getPayItems().getDeductionTypes().size()  );
                messages.add("GET PayItems LeaveType:"      + payItems.getPayItems().getLeaveTypes().size()  );
                messages.add("GET PayItems ReimbursementTypes:"      + payItems.getPayItems().getReimbursementTypes().size()  );
                
                
                // CREATE PayITems
                where = "Type==\"EXPENSE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                String accountCodeForPayItem = accounts.getAccounts().get(0).getCode();
                
                PayItem newPayItem = new PayItem();
                
                EarningsRate newEarningsRate = new EarningsRate();
                newEarningsRate.setAccountCode("200");
                //newEarningsRate.setAccrueLeave(1.00);
                newEarningsRate.setAmount(5.00);
                newEarningsRate.setEarningsRateID(ordinaryEarningsRateID);
                newEarningsRate.setEarningsType(EarningsType.ORDINARYTIMEEARNINGS);
                newEarningsRate.setIsExemptFromSuper(true);
                newEarningsRate.setIsExemptFromTax(true);
                newEarningsRate.setIsReportableAsW1(false);
                newEarningsRate.setMultiplier(1.50);
                newEarningsRate.setName("MyRate");
                newEarningsRate.setRatePerUnit("10.0");
                newEarningsRate.setRateType(RateType.MULTIPLE);
                newEarningsRate.setTypeOfUnits("4.00");
                newEarningsRate.setAccountCode(accountCodeForPayItem);
                newEarningsRate.setEmploymentTerminationPaymentType(EmploymentTerminationPaymentType.O);
                List<EarningsRate> newEarningsRates = new ArrayList<>();
                newEarningsRates.add(newEarningsRate);
                newPayItem.setEarningsRates(newEarningsRates);
           
                PayItems createdPayItems = payrollAuApi.createPayItem(accessToken, xeroTenantId, newPayItem);
                //messages.add("CREATED PayItems rate:"      + createdPayItems.getPayItems().getEarningsRates().get(0).getRatePerUnit()  );
                
             } catch (XeroBadRequestException e) {
                 this.addBadRequest(e, messages); 
             } catch (XeroForbiddenException e) {
                 this.addError(e, messages); 
             } catch (XeroNotFoundException e) {
                 this.addError(e, messages); 
             } catch (XeroUnauthorizedException e) {
                 this.addError(e, messages); 
             } catch (XeroMethodNotAllowedException e) {
                 this.addMethodNotAllowedException(e, messages); 
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }   
         
         } else  if (object.equals("PayrollAuEPayrollCalendar")) {
            
            try {
                // GET Payroll Calendars
                PayrollCalendars payrollCalendars = payrollAuApi.getPayrollCalendars(accessToken, xeroTenantId, null,null,null,null);
                
                // GET Single Payroll Calendar
                UUID payrollCalendarId = payrollCalendars.getPayrollCalendars().get(0).getPayrollCalendarID();
                PayrollCalendars onePayrollCalendar = payrollAuApi.getPayrollCalendar(accessToken, xeroTenantId, payrollCalendarId);
         
                // CREATE Payroll Calendar
                SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
                
                PayrollCalendar prCalendar = new PayrollCalendar();
                prCalendar.setCalendarType(CalendarType.WEEKLY);
                prCalendar.setName("MyCal" + this.loadRandomNum());
                
//                String payrollCalendarStartDateString = "06-11-2019";
//                String payrollCalendarPaymentDateString = "12-11-2019";
//                try{
//                   Date payrollCalendarStartDate = sdf.parse(payrollCalendarStartDateString);
//                   String payrollCalendarStartDateFormated = "/Date("+ payrollCalendarStartDate.getTime() + "+0000)/";
//                   prCalendar.setStartDate(payrollCalendarStartDateFormated);
//                   
//                   Date payrollCalendarPaymentDate = sdf.parse(payrollCalendarPaymentDateString);
//                   String payrollCalendarPaymentDateFormated = "/Date("+ payrollCalendarPaymentDate.getTime() + "+0000)/";
//                   prCalendar.setPaymentDate(payrollCalendarPaymentDateFormated);
//
//                }catch(ParseException e){
//                    e.printStackTrace();
//                } 
//                
                List<PayrollCalendar> prCalendars= new ArrayList<>();
                prCalendars.add(prCalendar);
                PayrollCalendars createdPayrollCalendar = payrollAuApi.createPayrollCalendar(accessToken, xeroTenantId, prCalendars);
           
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
            
         } else  if (object.equals("PayrollAuPayRuns")) {
             
             try {
                 SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");

                // GET PayRuns
                PayRuns payRuns = payrollAuApi.getPayRuns(accessToken, xeroTenantId, null,null,null,null);
                messages.add("GET PayRuns:" + payRuns.getPayRuns().size() );
            
                // GET Single PayRun
                UUID payrunId = payRuns.getPayRuns().get(0).getPayRunID();
                PayRuns onePayrun = payrollAuApi.getPayRun(accessToken, xeroTenantId, payrunId);
                messages.add("GET a single PayRuns - Net Pay:" + onePayrun.getPayRuns().get(0).getNetPay() );
                
                // CREATE PayRun
                PayrollCalendars allPayrollCalendars = payrollAuApi.getPayrollCalendars(accessToken, xeroTenantId, null,null,null,null);
                UUID payRunPayrollCalendarId = allPayrollCalendars.getPayrollCalendars().get(0).getPayrollCalendarID();              
                UUID payRunId = null;

                List<PayRun> newPayRuns = new ArrayList<>();
                PayRun newPayRun = new PayRun();
               
                newPayRun.setPayRunPeriodStartDate(LocalDate.of(2019, 11, 1));               
                newPayRun.setPayRunPeriodEndDate(LocalDate.of(2019, 10, 7));
                newPayRun.setPaymentDate(LocalDate.of(2019, 11, 8));
                newPayRun.setPayrollCalendarID(payRunPayrollCalendarId);
                newPayRun.setPayRunStatus(PayRunStatus.DRAFT);
                newPayRuns.add(newPayRun);
                PayRuns createdPayRun = payrollAuApi.createPayRun(accessToken, xeroTenantId, newPayRuns);
                payRunId = createdPayRun.getPayRuns().get(0).getPayRunID();
                messages.add("CREATE PayRuns - ID:" + createdPayRun.getPayRuns().get(0).getPayRunID() );
                
                payRunId = UUID.fromString("d1348fab-f47a-4697-beea-922ee262407a");
                // UPDATE PayRun
                List<PayRun> upPayRuns = new ArrayList<>();
                PayRun upPayRun = new PayRun();
               
                upPayRun.setPayRunStatus(PayRunStatus.POSTED);
                upPayRuns.add(upPayRun);
                PayRuns updatedPayRun = payrollAuApi.updatePayRun(accessToken, xeroTenantId, payRunId, upPayRuns);
             
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
            
         } else  if (object.equals("PayrollAuPayslips")) {
             try {
                 // GET Payrun
                 PayRuns payRuns = payrollAuApi.getPayRuns(accessToken, xeroTenantId, null,null,null,null);
                 messages.add("GET PayRuns - Total:" + payRuns.getPayRuns().size() );
                 UUID payrunId = payRuns.getPayRuns().get(0).getPayRunID();
                 
                 // GET ONE PAYRUN
                 PayRuns onePayrun = payrollAuApi.getPayRun(accessToken, xeroTenantId, payrunId);
                 messages.add("GET one PayRuns - ID:" + onePayrun.getPayRuns().get(0).getPayRunID() );
                 UUID paySlipId = onePayrun.getPayRuns().get(0).getPayslips().get(0).getPayslipID();
                 
                 //GET Payslips
                 PayslipObject getPayslip = payrollAuApi.getPayslip(accessToken, xeroTenantId, paySlipId);
                 messages.add("GET one PaySlip - Name:" + getPayslip.getPayslip().getFirstName() );
                
                 // UPDATE Payslips
                 com.xero.models.payrollau.Employees employees = payrollAuApi.getEmployees(accessToken, xeroTenantId, null, null, null, null);
                 UUID employeeID = employees.getEmployees().get(0).getEmployeeID();
                 
                 // Init array of DeductionLines
                 List<PayslipLines> payslipObjectArray = new ArrayList<>();
                 
                 // Init array of DeductionLine
                 List<DeductionLine> deductionLines = new ArrayList<>();
                 DeductionLine deductionLine = new DeductionLine();
                 deductionLine.setDeductionTypeID(getPayslip.getPayslip().getDeductionLines().get(0).getDeductionTypeID());
                 deductionLine.setNumberOfUnits(99.0);
                 deductionLine.setCalculationType(getPayslip.getPayslip().getDeductionLines().get(0).getCalculationType());
                 deductionLines.add(deductionLine);
                 
                 // Init DeductionLines
                 PayslipLines payslipLines = new PayslipLines();
                 payslipLines.addDeductionLinesItem(deductionLine);
                 payslipObjectArray.add(payslipLines);
                
                 com.xero.models.payrollau.Payslips updatedPaySlip = payrollAuApi.updatePayslip(accessToken, xeroTenantId, paySlipId, payslipObjectArray);
                 messages.add("UPDATED PaySlip - Name:" + updatedPaySlip.getPayslips().get(0).getLastName() );
                 
             } catch (XeroBadRequestException e) {
                 this.addBadRequest(e, messages); 
             } catch (XeroForbiddenException e) {
                 this.addError(e, messages); 
             } catch (XeroNotFoundException e) {
                 this.addError(e, messages); 
             } catch (XeroUnauthorizedException e) {
                 this.addError(e, messages); 
             } catch (XeroMethodNotAllowedException e) {
                 this.addMethodNotAllowedException(e, messages); 
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }   
        } else if (object.equals("PayrollAuSettings")) {  
            try {
                //GET Settings
                SettingsObject settings = payrollAuApi.getSettings(accessToken, xeroTenantId);
                System.out.println(settings.toString());
                messages.add("GET settings - Account Code:" + settings.getSettings().getAccounts().get(0).getCode() );
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   

        } else if (object.equals("PayrollAuSuperfunds")) {       
            try {
                //GET all SuperFunds
                SuperFunds superfunds = payrollAuApi.getSuperfunds(accessToken, xeroTenantId, null, null, null, null);
                messages.add("GET SuperFunds - total:" + superfunds.getSuperFunds().size() );
                UUID superfundID = superfunds.getSuperFunds().get(0).getSuperFundID();
                
                //CREATE  SuperFunds
                List<SuperFund> newSuperFunds = new ArrayList<>();
                SuperFund newSuperFund = new SuperFund();
                newSuperFund.setAccountName("Foo" + this.loadRandomNum());
                newSuperFund.setAccountNumber("FB" + this.loadRandomNum());
                newSuperFund.setName("Bar" + this.loadRandomNum());
                newSuperFund.setType(SuperFundType.REGULATED);
                newSuperFund.setUSI("PTC0133AU");
                newSuperFunds.add(newSuperFund);
                SuperFunds createdSuperfunds = payrollAuApi.createSuperfund(accessToken, xeroTenantId, newSuperFunds);  
                messages.add("CREATE SuperFunds - Account Name:" + createdSuperfunds.getSuperFunds().get(0).getAccountName() );
          
                // UDPATE SuperFunds     
                List<SuperFund> upSuperFunds = new ArrayList<>();
                SuperFund upSuperFund = new SuperFund();
                upSuperFund.setName("Nice" + this.loadRandomNum());
                upSuperFund.setType(SuperFundType.REGULATED);
                upSuperFunds.add(upSuperFund);
                SuperFunds updatedSuperfunds = payrollAuApi.updateSuperfund(accessToken, xeroTenantId, superfundID, upSuperFunds);                    
                messages.add("UPDATED SuperFunds - Account Name:" + updatedSuperfunds.getSuperFunds().get(0).getAccountName() );
            
                // GET one SuperFund
                SuperFunds oneSuperFund = payrollAuApi.getSuperfund(accessToken, xeroTenantId, superfundID);
                messages.add("GET single SuperFunds - Account Name:" + oneSuperFund.getSuperFunds().get(0).getAccountName() );
                
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
           
        } else if (object.equals("PayrollAuSuperfundProducts")) {            
            try {
                //GET all SuperFundProducts            
                SuperFundProducts superfundProducts = payrollAuApi.getSuperfundProducts(accessToken, xeroTenantId, null, "OSF0001AU");
                messages.add("GET  SuperFunds Products - ABN:" + superfundProducts.getSuperFundProducts().get(0).getABN() );
 
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
                
        } else if (object.equals("PayrollAuTimesheets")) {    
            try {
                //GET all Timesheets
                Timesheets timesheets = payrollAuApi.getTimesheets(accessToken, xeroTenantId, null, null, null, null);
                messages.add("GET  Timesheets - total:" + timesheets.getTimesheets().size() );
                UUID timesheetID = timesheets.getTimesheets().get(0).getTimesheetID();
                
                //GET one Timesheet            
                TimesheetObject timesheet = payrollAuApi.getTimesheet(accessToken, xeroTenantId,timesheetID);
                messages.add("GET one Timesheets - employee id:" + timesheet.getTimesheet().getEmployeeID() );
                
                //CREATE Timesheet
                PayItems payItems = payrollAuApi.getPayItems(accessToken, xeroTenantId, null, null, null, null);
                UUID ordinaryEarningsRateID = payItems.getPayItems().getEarningsRates().get(0).getEarningsRateID();     
                
                com.xero.models.payrollau.Employees employees = payrollAuApi.getEmployees(accessToken, xeroTenantId, null, null, null, null);
                UUID employeeID = employees.getEmployees().get(0).getEmployeeID();
                
                Timesheet newTimesheet = new Timesheet();
                List<Timesheet> newTimesheets = new ArrayList<>();
                
                newTimesheet.setStartDate(LocalDate.of(2019, 11, 8));
                newTimesheet.setEndDate(LocalDate.of(2019, 11, 14));
                newTimesheet.setEmployeeID(employeeID);
                newTimesheet.setStatus(TimesheetStatus.DRAFT);
                
                List<TimesheetLine> timesheetLines = new ArrayList<>();
                TimesheetLine timesheetLine = new TimesheetLine();
                timesheetLine.setEarningsRateID(ordinaryEarningsRateID);
                List<Double> numUnits = new ArrayList<Double>();
                numUnits.add(2.0);
                numUnits.add(10.0);
                numUnits.add(0.0);
                numUnits.add(0.0);
                numUnits.add(5.0);
                numUnits.add(0.0);
                numUnits.add(5.0);
                timesheetLine.setNumberOfUnits(numUnits);
                
                //GET Settings
                SettingsObject settings = payrollAuApi.getSettings(accessToken, xeroTenantId);
                UUID trackingCategoryID = settings.getSettings().getTrackingCategories().getTimesheetCategories().getTrackingCategoryID();
                TrackingCategories trackingCategories = accountingApi.getTrackingCategory(accessToken, xeroTenantId, trackingCategoryID);
                UUID trackingItemID = trackingCategories.getTrackingCategories().get(0).getOptions().get(0).getTrackingOptionID();
                timesheetLine.setTrackingItemID(trackingItemID);
                
                timesheetLines.add(timesheetLine);
                newTimesheet.setTimesheetLines(timesheetLines);
                newTimesheets.add(newTimesheet);
                
                Timesheets createdTimesheets = payrollAuApi.createTimesheet(accessToken, xeroTenantId, newTimesheets);
                messages.add("CREATED  Timesheets - ID:" + createdTimesheets.getTimesheets().get(0).getTimesheetID() );                
                UUID timesheetId = createdTimesheets.getTimesheets().get(0).getTimesheetID();
                
                // UPDATE timesheet
                Timesheet upTimesheet = new Timesheet();
                List<Timesheet> upTimesheets = new ArrayList<>();
                
                upTimesheet = createdTimesheets.getTimesheets().get(0);    
                upTimesheet.setStatus(TimesheetStatus.APPROVED);
                upTimesheets.add(upTimesheet);
                Timesheets updatedTimesheets = payrollAuApi.updateTimesheet(accessToken, xeroTenantId, timesheetId, upTimesheets);
                System.out.println(updatedTimesheets.toString());
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }   
          
        } else if (object.equals("debug")) {

            // Create Invoice
            where = "Type==\"REVENUE\"";
            Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
            String accountCodeForInvoice = accounts.getAccounts().get(0).getCode();
            where = null;

            Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);

            UUID contactIDForInvoice = contacts.getContacts().get(0).getContactID();

            for (int i = 0; i > contacts.getContacts().size(); i++) {
                String email = contacts.getContacts().get(i).getEmailAddress().toString();

                if (email != null && !email.isEmpty()) {
                    contactIDForInvoice = contacts.getContacts().get(i).getContactID();
                    break;
                }
            }

            Contact useContact = new Contact();
            useContact.setContactID(contactIDForInvoice);

            Invoices newInvoices = new Invoices();
            Invoice myInvoice = new Invoice();

            LineItem li = new LineItem();
            li.setAccountCode(accountCodeForInvoice);
            li.setDescription("Acme Tires");
            li.setQuantity(2.0090);
            li.setUnitAmount(20.795);
            li.setLineAmount(40.00);
            li.setTaxType("NONE");

            myInvoice.addLineItemsItem(li);
            myInvoice.setContact(useContact);
            LocalDate dueDate = LocalDate.of(2018, Month.DECEMBER, 10);
            myInvoice.setDueDate(dueDate);
            LocalDate todayDate = LocalDate.now();
            myInvoice.setDate(todayDate);
            myInvoice.setType(com.xero.models.accounting.Invoice.TypeEnum.ACCREC);
            myInvoice.setReference("One Fish, Two Fish");
            myInvoice.setStatus(com.xero.models.accounting.Invoice.StatusEnum.AUTHORISED);
            newInvoices.addInvoicesItem(myInvoice);
            newInvoices.addInvoicesItem(myInvoice);

            Invoices newInvoice = accountingApi.createInvoices(accessToken, xeroTenantId, newInvoices,null,4);
            System.out.println(newInvoice.toString());

            /*
            invoiceIds.add(UUID.fromString("d8ba83cc-b43b-4811-a209-9c195c95d23b"));
            invoiceIds.add(UUID.fromString("abea869a-8230-4690-b407-a2e74ed455c1"));

            Invoices invoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order,
                    invoiceIds, invoiceNumbers, contactIDs, statuses, null, includeArchived, createdByMyApp, null);

            System.out.println(invoices.toString());
             */
            /*
            File bytes = new File("/Users/sid.maestre/eclipse-workspace/xero-sdk-oauth2-dev-01/resources/youngsid.jpg");
            String newFileName = bytes.getName();
            System.out.println(newFileName);
            try {
                // CREATE Accounts attachment
                where = "Status==\"ACTIVE\"";
                Accounts myAccounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                if (myAccounts.getAccounts().size() > 0) {
                    UUID accountID = myAccounts.getAccounts().get(0).getAccountID();
                    String accountName = myAccounts.getAccounts().get(0).getName();
                    Attachments createdAttachments = accountingApi.createAccountAttachmentByFileName(accessToken,
                            xeroTenantId, accountID, newFileName, bytes);
                    messages.add("Attachment to Name: " + accountName + " Account ID: " + accountID
                            + " attachment - ID: " + createdAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroApiException xe) {
                this.addError(xe, messages);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
             */
            /*
             * Invoices invoices = new Invoices(); Invoice invoice = new Invoice(); LineItem
             * li = new LineItem(); li.setLineAmount(100.00);
             * 
             * invoice.setType(TypeEnum.ACCREC); invoice.setReference("hello world");
             * invoice.addLineItemsItem(li);
             * 
             * Contact contact1 = new Contact(); contact1.setName("Mr. Sid Maestre" +
             * this.loadRandomNum()); invoice.setContact(contact1);
             * invoices.addInvoicesItem(invoice);
             * 
             * Invoices newInvoices = accountingApi.createInvoice(accessToken, xeroTenantId,
             * invoices, false); System.out.println("New Invoice created");
             * System.out.println(newInvoices.toString());
             */

        } else if (object.equals("Accounts")) {
            // ACCOUNTS
            try {
                // GET all accounts
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, null, null, null);
                messages.add("Get a all Accounts - total : " + accounts.getAccounts().size());

                // GET one account
                UUID id = UUID.fromString("00000000-0000-0000-000-000000000000");
                //Accounts oneAccount = accountingApi.getAccount(accessToken, xeroTenantId, accounts.getAccounts().get(0).getAccountID());
                Accounts oneAccount = accountingApi.getAccount(accessToken, xeroTenantId, id);
                
                messages.add("Get a one Account - name : " + oneAccount.getAccounts().get(0).getName());

                // CREATE account
                Account acct = new Account();
                acct.setName("TEST");
                acct.setCode("123");
                //acct.setName("Bye" + loadRandomNum());
                //acct.setCode("Hello" + loadRandomNum());
                acct.setDescription("Foo boo");
                acct.setType(com.xero.models.accounting.AccountType.EXPENSE);
                Accounts newAccount = accountingApi.createAccount(accessToken, xeroTenantId, acct);
                messages.add("Create a new Account - Name : " + newAccount.getAccounts().get(0).getName()  + " Description : " + newAccount.getAccounts().get(0).getDescription() + "");
                //UUID accountID = newAccount.getAccounts().get(0).getAccountID();

                // CREATE Bank account
                Account bankAcct = new Account();
                bankAcct.setName("Checking " + loadRandomNum());
                bankAcct.setCode("12" + loadRandomNum());
                bankAcct.setType(com.xero.models.accounting.AccountType.BANK);
                bankAcct.setBankAccountNumber("1234" + loadRandomNum());
                Accounts newBankAccount = accountingApi.createAccount(accessToken, xeroTenantId, bankAcct);
                messages.add("Create Bank Account - Name : " + newBankAccount.getAccounts().get(0).getName());

                // GET BANK account
                where = "Status==\"ACTIVE\"&&Type==\"BANK\"";
                Accounts accountsWhere = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                messages.add("Get a all Accounts - total : " + accountsWhere.getAccounts().size());
                UUID accountID = accountsWhere.getAccounts().get(0).getAccountID();
                
                // UDPATE Account
                Account upAcct = new Account();
                Accounts upAccts = new Accounts();
                
                upAcct.setDescription("Monsters Inc.");
                upAcct.setCode("123");
                upAccts.addAccountsItem(upAcct);
                
                Accounts updateAccount = accountingApi.updateAccount(accessToken, xeroTenantId, accountID, upAccts);
                messages.add("Update Account - Name : " + updateAccount.getAccounts().get(0).getName()
                        + " Description : " + updateAccount.getAccounts().get(0).getDescription() + "");

                // ARCHIVE Account
//                Accounts archiveAccounts = new Accounts();
//                Account archiveAccount = new Account();
//                archiveAccount.setStatus(com.xero.models.accounting.Account.StatusEnum.ARCHIVED);
//                archiveAccount.setAccountID(accountID);
//                archiveAccounts.addAccountsItem(archiveAccount);
//                Accounts achivedAccount = accountingApi.updateAccount(accessToken, xeroTenantId, accountID,
//                        archiveAccounts);
//                messages.add("Archived Account - Name : " + achivedAccount.getAccounts().get(0).getName() + " Status: "
//                        + achivedAccount.getAccounts().get(0).getStatus());
//
//                // DELETE Account
//                UUID deleteAccountID = newAccount.getAccounts().get(0).getAccountID();
//                Accounts deleteAccount = accountingApi.deleteAccount(accessToken, xeroTenantId, deleteAccountID);
//                messages.add("Delete account - Status? : " + deleteAccount.getAccounts().get(0).getStatus());

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e,messages);
            } catch (XeroNotFoundException e) {
                //this.addError(e, messages);
            } catch (XeroForbiddenException e) {
                //this.addXeroError(e, messages);
                System.out.println("Nope " + e.getStatusCode());
                System.out.println("Nope " + e.getMessage());
             
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (object.equals("GetAttachments")) {

            try {
                // GET Account Attachment
                where = "Status==\"ACTIVE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                if (accounts.getAccounts().size() > 0) {
                    UUID accountID = accounts.getAccounts().get(0).getAccountID();
                    Attachments accountsAttachments = accountingApi.getAccountAttachments(accessToken, xeroTenantId,
                            accountID);
                    if (accountsAttachments.getAttachments().size() > 0) {
                        UUID attachementId = accountsAttachments.getAttachments().get(0).getAttachmentID();
                        String contentType = accountsAttachments.getAttachments().get(0).getMimeType();
                        ByteArrayInputStream input = accountingApi.getAccountAttachmentById(accessToken, xeroTenantId,
                                accountID, attachementId, contentType);
                        String fileName = "Account_" + accountsAttachments.getAttachments().get(0).getFileName();
                        String saveFilePath = saveFile(input, fileName);
                        messages.add("Get Account attachment - save it here: " + saveFilePath);
                    }
                }

                // GET BankTransactions Attachment
                where = null;
                BankTransactions bankTransactions = accountingApi.getBankTransactions(accessToken, xeroTenantId,
                        ifModifiedSince, where, order, null, null);
                if (bankTransactions.getBankTransactions().size() > 0) {
                    UUID BankTransactionID = bankTransactions.getBankTransactions().get(0).getBankTransactionID();
                    Attachments bankTransactionsAttachments = accountingApi.getBankTransactionAttachments(accessToken,
                            xeroTenantId, BankTransactionID);
                    if (bankTransactionsAttachments.getAttachments().size() > 0) {
                        UUID BankTransactionAttachementID = bankTransactionsAttachments.getAttachments().get(0)
                                .getAttachmentID();
                        String BankTransactionContentType = bankTransactionsAttachments.getAttachments().get(0)
                                .getMimeType();
                        ByteArrayInputStream BankTransactionInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, BankTransactionID, BankTransactionAttachementID,
                                BankTransactionContentType);
                        String BankTransactionFileName = "BankTransaction_"
                                + bankTransactionsAttachments.getAttachments().get(0).getFileName();
                        String BankTransactionSaveFilePath = saveFile(BankTransactionInput, BankTransactionFileName);
                        messages.add("Get BankTransactions attachment - save it here: " + BankTransactionSaveFilePath);
                    }
                }

                // GET BankTransfers Attachment
                BankTransfers bankTransfers = accountingApi.getBankTransfers(accessToken, xeroTenantId, ifModifiedSince,
                        where, order);
                if (bankTransfers.getBankTransfers().size() > 0) {
                    UUID BankTransferID = bankTransfers.getBankTransfers().get(0).getBankTransferID();
                    Attachments bankTransfersAttachments = accountingApi.getBankTransferAttachments(accessToken,
                            xeroTenantId, BankTransferID);
                    if (bankTransfersAttachments.getAttachments().size() > 0) {
                        UUID BankTransferAttachementID = bankTransfersAttachments.getAttachments().get(0)
                                .getAttachmentID();
                        String BankTransferContentType = bankTransfersAttachments.getAttachments().get(0).getMimeType();
                        ByteArrayInputStream BankTransferInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, BankTransferID, BankTransferAttachementID, BankTransferContentType);
                        String BankTransferFileName = "BankTransfer_"
                                + bankTransfersAttachments.getAttachments().get(0).getFileName();
                        String BankTransferSaveFilePath = saveFile(BankTransferInput, BankTransferFileName);
                        messages.add("Get BankTransfers attachment - save it here: " + BankTransferSaveFilePath);
                    }
                }
                // GET Contacts Attachment
                where = "ContactStatus==\"ACTIVE\"";
                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order,contactIds, null, includeArchived);
                if (contacts.getContacts().size() > 0) {
                    UUID ContactID = contacts.getContacts().get(0).getContactID();
                    Attachments contactsAttachments = accountingApi.getContactAttachments(accessToken, xeroTenantId,
                            ContactID);
                    if (contactsAttachments.getAttachments().size() > 0) {
                        UUID ContactAttachementID = contactsAttachments.getAttachments().get(0).getAttachmentID();
                        String ContactContentType = contactsAttachments.getAttachments().get(0).getMimeType();
                        ByteArrayInputStream ContactInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, ContactID, ContactAttachementID, ContactContentType);
                        String ContactFileName = "Contact_" + contactsAttachments.getAttachments().get(0).getFileName();
                        String ContactSaveFilePath = saveFile(ContactInput, ContactFileName);
                        messages.add("Get Contacts attachment - save it here: " + ContactSaveFilePath);
                    }
                }
                // GET CreditNotes Attachment
                where = "Status==\"AUTHORISED\"";
                CreditNotes creditNotes = accountingApi.getCreditNotes(accessToken, xeroTenantId, ifModifiedSince,
                        where, order, null,null);
                if (creditNotes.getCreditNotes().size() > 0) {
                    UUID CreditNoteID = creditNotes.getCreditNotes().get(0).getCreditNoteID();
                    Attachments creditNotesAttachments = accountingApi.getCreditNoteAttachments(accessToken,
                            xeroTenantId, CreditNoteID);
                    if (creditNotesAttachments.getAttachments().size() > 0) {
                        UUID CreditNoteAttachementID = creditNotesAttachments.getAttachments().get(0).getAttachmentID();
                        String CreditNoteContentType = creditNotesAttachments.getAttachments().get(0).getMimeType();
                        ByteArrayInputStream CreditNoteInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, CreditNoteID, CreditNoteAttachementID, CreditNoteContentType);
                        String CreditNoteFileName = "CreditNote_"
                                + creditNotesAttachments.getAttachments().get(0).getFileName();
                        String CreditNoteSaveFilePath = saveFile(CreditNoteInput, CreditNoteFileName);
                        messages.add("Get CreditNotes attachment - save it here: " + CreditNoteSaveFilePath);
                    }
                }

                // GET Invoices Attachment
                Invoices invoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);

                if (invoices.getInvoices().size() > 0) {
                    UUID InvoiceID = invoices.getInvoices().get(0).getInvoiceID();
                    Attachments invoicesAttachments = accountingApi.getInvoiceAttachments(accessToken, xeroTenantId,
                            InvoiceID);
                    if (invoicesAttachments.getAttachments().size() > 0) {
                        UUID InvoiceAttachementID = invoicesAttachments.getAttachments().get(0).getAttachmentID();
                        String InvoiceContentType = invoicesAttachments.getAttachments().get(0).getMimeType();
                        ByteArrayInputStream InvoiceInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, InvoiceID, InvoiceAttachementID, InvoiceContentType);
                        String InvoiceFileName = "Invoice_" + invoicesAttachments.getAttachments().get(0).getFileName();
                        String InvoiceSaveFilePath = saveFile(InvoiceInput, InvoiceFileName);
                        messages.add("Get Invoices attachment - save it here: " + InvoiceSaveFilePath);
                    }
                }

                // GET ManualJournals Attachment
                where = null;
                ManualJournals manualJournals = accountingApi.getManualJournals(accessToken, xeroTenantId,
                        ifModifiedSince, where, order, null);
                if (manualJournals.getManualJournals().size() > 0) {
                    UUID ManualJournalID = manualJournals.getManualJournals().get(0).getManualJournalID();
                    Attachments manualJournalsAttachments = accountingApi.getManualJournalAttachments(accessToken,
                            xeroTenantId, ManualJournalID);
                    if (manualJournalsAttachments.getAttachments().size() > 0) {
                        UUID ManualJournalAttachementID = manualJournalsAttachments.getAttachments().get(0)
                                .getAttachmentID();
                        String ManualJournalContentType = manualJournalsAttachments.getAttachments().get(0)
                                .getMimeType();
                        ByteArrayInputStream ManualJournalInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, ManualJournalID, ManualJournalAttachementID, ManualJournalContentType);
                        String ManualJournalFileName = "ManualJournal_"
                                + manualJournalsAttachments.getAttachments().get(0).getFileName();
                        String ManualJournalSaveFilePath = saveFile(ManualJournalInput, ManualJournalFileName);
                        messages.add("Get ManualJournals attachment - save it here: " + ManualJournalSaveFilePath);
                    }
                }

                
                // GET Quotes Attachment
                where = null;
                LocalDate dateFrom = null;
                LocalDate dateTo = null;
                LocalDate expiryDateFrom = null;
                LocalDate expiryDateTo = null;
                UUID contactID = null;
                String status = null;
                int page = 1;
                Quotes quotes = accountingApi.getQuotes(savedAccessToken, xeroTenantId, ifModifiedSince, dateFrom, dateTo, expiryDateFrom, expiryDateTo, contactID, status, page, order);
                if (quotes.getQuotes().size() > 0) {
                    UUID quoteID = quotes.getQuotes().get(0).getQuoteID();
                    Attachments quotesAttachments = accountingApi.getQuoteAttachments(accessToken, xeroTenantId, quoteID);
                    if (quotesAttachments.getAttachments().size() > 0) {
                        UUID quoteAttachementID = quotesAttachments.getAttachments().get(0)
                                .getAttachmentID();
                        String quoteContentType = quotesAttachments.getAttachments().get(0)
                                .getMimeType();
                        ByteArrayInputStream quoteInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, quoteID, quoteAttachementID, quoteContentType);
                        String quoteFileName = "Quote_"
                                + quotesAttachments.getAttachments().get(0).getFileName();
                        String quoteSaveFilePath = saveFile(quoteInput, quoteFileName);
                        messages.add("Get Quote attachment - save it here: " + quoteSaveFilePath);
                    }
                }

                
                // GET Receipts Attachment
                Receipts receipts = accountingApi.getReceipts(accessToken, xeroTenantId, ifModifiedSince, where, order,
                        null);
                if (receipts.getReceipts().size() > 0) {
                    UUID ReceiptID = receipts.getReceipts().get(0).getReceiptID();
                    Attachments receiptsAttachments = accountingApi.getReceiptAttachments(accessToken, xeroTenantId,
                            ReceiptID);
                    if (receiptsAttachments.getAttachments().size() > 0) {
                        UUID ReceiptAttachementID = receiptsAttachments.getAttachments().get(0).getAttachmentID();
                        String ReceiptContentType = receiptsAttachments.getAttachments().get(0).getMimeType();
                        ByteArrayInputStream ReceiptInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, ReceiptID, ReceiptAttachementID, ReceiptContentType);
                        String ReceiptFileName = "Receipt_" + receiptsAttachments.getAttachments().get(0).getFileName();
                        String ReceiptSaveFilePath = saveFile(ReceiptInput, ReceiptFileName);
                        messages.add("Get Receipts attachment - save it here: " + ReceiptSaveFilePath);
                    }
                }

                // GET RepeatingInvoices Attachment
                RepeatingInvoices repeatingInvoices = accountingApi.getRepeatingInvoices(accessToken, xeroTenantId,
                        where, order);
                if (repeatingInvoices.getRepeatingInvoices().size() > 0) {
                    UUID RepeatingInvoiceID = repeatingInvoices.getRepeatingInvoices().get(0).getRepeatingInvoiceID();
                    Attachments repeatingInvoicesAttachments = accountingApi.getRepeatingInvoiceAttachments(accessToken,
                            xeroTenantId, RepeatingInvoiceID);
                    if (repeatingInvoicesAttachments.getAttachments().size() > 0) {
                        UUID RepeatingInvoiceAttachementID = repeatingInvoicesAttachments.getAttachments().get(0)
                                .getAttachmentID();
                        String RepeatingInvoiceContentType = repeatingInvoicesAttachments.getAttachments().get(0)
                                .getMimeType();
                        ByteArrayInputStream RepeatingInvoiceInput = accountingApi.getAccountAttachmentById(accessToken,
                                xeroTenantId, RepeatingInvoiceID, RepeatingInvoiceAttachementID,
                                RepeatingInvoiceContentType);
                        String RepeatingInvoiceFileName = "RepeatingInvoice_"
                                + repeatingInvoicesAttachments.getAttachments().get(0).getFileName();
                        String RepeatingInvoiceSaveFilePath = saveFile(RepeatingInvoiceInput, RepeatingInvoiceFileName);
                        messages.add(
                                "Get RepeatingInvoices attachment - save it here: " + RepeatingInvoiceSaveFilePath);
                    }
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (object.equals("CreateAttachments")) {
            // JSON
            File bytes = new File("/Users/sid.maestre/eclipse-workspace/xero-sdk-oauth2-dev-01/resources/youngsid.jpg");
            String newFileName = bytes.getName();

            try {
                // CREATE Accounts attachment
                where = "Status==\"ACTIVE\"";
                Accounts myAccounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                if (myAccounts.getAccounts().size() > 0) {
                    UUID accountID = myAccounts.getAccounts().get(0).getAccountID();
                    String accountName = myAccounts.getAccounts().get(0).getName();
                    Attachments createdAttachments = accountingApi.createAccountAttachmentByFileName(accessToken,
                            xeroTenantId, accountID, newFileName, bytes);
                    messages.add("Attachment to Name: " + accountName + " Account ID: " + accountID
                            + " attachment - ID: " + createdAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                where = null;
                // CREATE BankTransactions attachment
                BankTransactions myBanktransactions = accountingApi.getBankTransactions(accessToken, xeroTenantId,
                        ifModifiedSince, where, order, null, null);
                if (myBanktransactions.getBankTransactions().size() > 0) {
                    UUID banktransactionID = myBanktransactions.getBankTransactions().get(0).getBankTransactionID();
                    Attachments createdBanktransationAttachments = accountingApi
                            .createBankTransactionAttachmentByFileName(accessToken, xeroTenantId, banktransactionID,
                                    newFileName, bytes);
                    messages.add("Attachment to BankTransaction ID: " + banktransactionID + " attachment - ID: "
                            + createdBanktransationAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                // CREATE BankTransfer attachment
                BankTransfers myBankTransfer = accountingApi.getBankTransfers(accessToken, xeroTenantId,
                        ifModifiedSince, where, order);
                if (myBankTransfer.getBankTransfers().size() > 0) {
                    UUID bankTransferID = myBankTransfer.getBankTransfers().get(0).getBankTransferID();
                    Attachments createdBankTransferAttachments = accountingApi.createBankTransferAttachmentByFileName(
                            accessToken, xeroTenantId, bankTransferID, newFileName, bytes);
                    messages.add("Attachment to BankTransfer ID: " + bankTransferID + " attachment - ID: "
                            + createdBankTransferAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                // CREATE Contacts attachment
                where = "ContactStatus==\"ACTIVE\"";
                Contacts contactsWhere = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                if (contactsWhere.getContacts().size() > 0) {
                    UUID contactID = contactsWhere.getContacts().get(0).getContactID();
                    Attachments createdContactAttachments = accountingApi.createContactAttachmentByFileName(accessToken,
                            xeroTenantId, contactID, newFileName, bytes);
                    messages.add("Attachment to Contact ID: " + contactID + " attachment - ID: "
                            + createdContactAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                where = "Status==\"AUTHORISED\"";
                // CREATE CreditNotes attachment
                CreditNotes myCreditNotes = accountingApi.getCreditNotes(accessToken, xeroTenantId, ifModifiedSince,
                        where, order, null, null);
                if (myCreditNotes.getCreditNotes().size() > 0) {
                    UUID creditNoteID = myCreditNotes.getCreditNotes().get(0).getCreditNoteID();
                    Attachments createdCreditNoteAttachments = accountingApi.createCreditNoteAttachmentByFileName(
                            accessToken, xeroTenantId, creditNoteID, newFileName, bytes,false);
                    messages.add("Attachment to Credit Notes ID: " + creditNoteID + " attachment - ID: "
                            + createdCreditNoteAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                // CREATE invoice attachment
                Invoices myInvoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                if (myInvoices.getInvoices().size() > 0) {
                    UUID invoiceID = myInvoices.getInvoices().get(0).getInvoiceID();
                    Attachments createdInvoiceAttachments = accountingApi.createInvoiceAttachmentByFileName(accessToken,
                            xeroTenantId, invoiceID, newFileName, bytes,false);
                    messages.add("Attachment to Invoice ID: " + invoiceID + " attachment - ID: "
                            + createdInvoiceAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            try {
                // CREATE ManualJournals attachment
                where = null;
                ManualJournals myManualJournals = accountingApi.getManualJournals(accessToken, xeroTenantId,
                        ifModifiedSince, where, order, null);
                System.out.println(myManualJournals.getManualJournals().size());

                if (myManualJournals.getManualJournals().size() > 0) {
                    UUID manualJournalID = myManualJournals.getManualJournals().get(0).getManualJournalID();
                    Attachments createdManualJournalAttachments = accountingApi.createManualJournalAttachmentByFileName(
                            accessToken, xeroTenantId, manualJournalID, newFileName, bytes);
                    messages.add("Attachment to Manual Journal ID: " + manualJournalID + " attachment - ID: "
                            + createdManualJournalAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            try {
                where = null;
                // CREATE Quote attachment
                where = null;
                LocalDate dateFrom = null;
                LocalDate dateTo = null;
                LocalDate expiryDateFrom = null;
                LocalDate expiryDateTo = null;
                UUID contactID = null;
                String status = null;
                int page = 1;
                Quotes quotes = accountingApi.getQuotes(savedAccessToken, xeroTenantId, ifModifiedSince, dateFrom, dateTo, expiryDateFrom, expiryDateTo, contactID, status, page, order);
                
                if (quotes.getQuotes().size() > 0) {
                    UUID quoteID = quotes.getQuotes().get(0).getQuoteID();
                    Attachments createdQuoteAttachments = accountingApi
                            .createQuoteAttachmentByFileName(accessToken, xeroTenantId, quoteID,
                                    newFileName, bytes);
                    messages.add("Attachment to Quote ID: " + quoteID + " attachment - ID: "
                            + createdQuoteAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                // CREATE Receipts attachment
                Receipts myReceipts = accountingApi.getReceipts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order, null);
                if (myReceipts.getReceipts().size() > 0) {
                    UUID receiptID = myReceipts.getReceipts().get(0).getReceiptID();
                    Attachments createdReceiptsAttachments = accountingApi.createReceiptAttachmentByFileName(
                            accessToken, xeroTenantId, receiptID, newFileName, bytes);
                    messages.add("Attachment to Receipt ID: " + receiptID + " attachment - ID: "
                            + createdReceiptsAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                // CREATE Repeating Invoices attachment
                RepeatingInvoices myRepeatingInvoices = accountingApi.getRepeatingInvoices(accessToken, xeroTenantId,
                        where, order);
                if (myRepeatingInvoices.getRepeatingInvoices().size() > 0) {
                    UUID repeatingInvoiceID = myRepeatingInvoices.getRepeatingInvoices().get(0).getRepeatingInvoiceID();
                    Attachments createdRepeatingInvoiceAttachments = accountingApi
                            .createRepeatingInvoiceAttachmentByFileName(accessToken, xeroTenantId, repeatingInvoiceID,
                                    newFileName, bytes);
                    messages.add("Attachment to Repeating Invoices ID: " + repeatingInvoiceID + " attachment - ID: "
                            + createdRepeatingInvoiceAttachments.getAttachments().get(0).getAttachmentID());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (object.equals("BankTransfers")) {
            /* BANK TRANSFER */
            try {
                where = "Status==\"ACTIVE\"&&Type==\"BANK\"";
                Accounts accountsWhere = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                where = null;
                // Maker sure we have at least 2 banks
                if (accountsWhere.getAccounts().size() > 1) {
                    // CREATE bank transfer
                    BankTransfer bankTransfer = new BankTransfer();
                    bankTransfer.setFromBankAccount(accountsWhere.getAccounts().get(1));
                    bankTransfer.setToBankAccount(accountsWhere.getAccounts().get(0));
                    bankTransfer.setAmount(50.00);
                    BankTransfers newBTs = new BankTransfers();
                    newBTs.addBankTransfersItem(bankTransfer);
                    BankTransfers newBankTranfer = accountingApi.createBankTransfer(accessToken, xeroTenantId, newBTs);
                    messages.add("Get a one Bank Transfer - amount : "
                            + newBankTranfer.getBankTransfers().get(0).getAmount());

                    // GET all Bank Transfers
                    BankTransfers bankTranfers = accountingApi.getBankTransfers(accessToken, xeroTenantId,
                            ifModifiedSince, where, order);
                    messages.add("Get a all Bank Transfers - total : " + bankTranfers.getBankTransfers().size());
                    UUID bankTransferId = bankTranfers.getBankTransfers().get(0).getBankTransferID();

                    // GET one Bank Transfer
                    BankTransfers oneBankTranfer = accountingApi.getBankTransfer(accessToken, xeroTenantId,
                            bankTransferId);
                    messages.add("Get a one Bank Transfer - amount : "
                            + oneBankTranfer.getBankTransfers().get(0).getAmount());

                    // GET Bank Transfer History
                    HistoryRecords hr = accountingApi.getBankTransferHistory(accessToken, xeroTenantId, bankTransferId);
                    messages.add("Get a one Bank Transfer History Record - details :"
                            + hr.getHistoryRecords().get(0).getDetails());

                    // CREATE Bank Transfer History
                    // Error: "The document with the supplied id was not found for this endpoint.
                    
                    HistoryRecords historyRecords = new HistoryRecords(); HistoryRecord
                    historyRecord = new HistoryRecord();
                    historyRecord.setDetails("This is a sample history note");
                    historyRecords.addHistoryRecordsItem(historyRecord); HistoryRecords newHr =
                    accountingApi.createBankTransferHistoryRecord(accessToken, xeroTenantId, bankTransferId,historyRecords);
                    messages.add("Get a one Bank Transfer History Record - details :" +
                    newHr.getHistoryRecords().get(0).getDetails());
                    
                } else {
                    messages.add("Need 2 or more bank accounts for bank transfer");
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (object.equals("BankTransactions")) {
            /* BANK TRANSACTION */
            try {
                where = "Status==\"ACTIVE\"&&Type==\"BANK\"";
                Accounts accountsWhere = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                where = null;
                
                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order,contactIds, null, includeArchived);
                Contact useContact = new Contact();
                if (contacts.getContacts().size() > 0) {
                    useContact.setContactID(contacts.getContacts().get(0).getContactID());
                }
                // Maker sure we have at least 1 bank
                if (accountsWhere.getAccounts().size() > 0) {
                    Account bankAcct = new Account();
                    bankAcct.setAccountID(accountsWhere.getAccounts().get(0).getAccountID());                    

                    // Create SINGLE Bank Transaction
                    List<LineItem> lineItems = new ArrayList<>();
                    LineItem li = new LineItem();
                    li.setAccountCode("400");
                    li.setDescription("Foobar");
                    li.setQuantity(1.7590);
                    li.setUnitAmount(20.1299);
                    lineItems.add(li);
                    BankTransaction bt = new BankTransaction();
                    bt.setBankAccount(bankAcct);
                    bt.setContact(useContact);
                    bt.setLineItems(lineItems);
                    bt.setType(com.xero.models.accounting.BankTransaction.TypeEnum.SPEND);
                    BankTransactions bts = new BankTransactions();

                    bts.addBankTransactionsItem(bt);
                    bts.addBankTransactionsItem(bt);
                    
                    // CREATE MULTIPLE TRANSACTIONS
                    BankTransactions newBankTransactions = accountingApi.createBankTransactions(accessToken, xeroTenantId, bts, summarizeErrors, unitdp);
                    messages.add("Create new BankTransactions: count: " + newBankTransactions.getBankTransactions().size());
                    
                    // MODIFY on Transaction to force and ERROR to summarizeErrors
                    newBankTransactions.getBankTransactions().get(0).getLineItems().get(0).setAccountCode("999999");
                    
                    // UPDATE MULTIPLE TRANSACTIONS
                    BankTransactions updatedBankTransaction = accountingApi.updateOrCreateBankTransactions(accessToken, xeroTenantId,  newBankTransactions, false, null);
                    messages.add("Update multiple BankTransaction : amount:" + updatedBankTransaction.getBankTransactions().get(0).getTotal());

                    // GET all Bank Transaction
                    BankTransactions bankTransactions = accountingApi.getBankTransactions(accessToken, xeroTenantId,
                            ifModifiedSince, where, order, null, null);
                    messages.add("Get a all Bank Transactions - total : " + bankTransactions.getBankTransactions().size());

                    // GET one Bank Transaction
                    if (bankTransactions.getBankTransactions().size() > 0) {                        
                        BankTransactions oneBankTransaction = accountingApi.getBankTransaction(accessToken, xeroTenantId,
                                bankTransactions.getBankTransactions().get(0).getBankTransactionID(),unitdp);
                        messages.add("Get a one Bank Transaction : amount:"
                                + oneBankTransaction.getBankTransactions().get(0).getTotal());

                        // UDPATE Bank Transaction
                        newBankTransactions.getBankTransactions().get(0).setSubTotal(null);
                        newBankTransactions.getBankTransactions().get(0).setTotal(null);
                        newBankTransactions.getBankTransactions().get(0).setReference("You just updated");
                        BankTransactions updateBankTransaction = accountingApi.updateBankTransaction(accessToken, xeroTenantId, newBankTransactions.getBankTransactions().get(0).getBankTransactionID(), newBankTransactions,unitdp);
                        messages.add("Update new BankTransaction : reference:"
                                + updateBankTransaction.getBankTransactions().get(0).getReference());
    
                        // DELETE Bank Transaction
                        newBankTransactions.getBankTransactions().get(0)
                        .setStatus(com.xero.models.accounting.BankTransaction.StatusEnum.DELETED);
                        BankTransactions deletedBankTransaction = accountingApi.updateBankTransaction(accessToken,
                                xeroTenantId, newBankTransactions.getBankTransactions().get(0).getBankTransactionID(),
                                newBankTransactions,unitdp);
                        messages.add("Deleted new Bank Transaction : Status:"+ deletedBankTransaction.getBankTransactions().get(0).getStatus());
    
                        // GET Bank Transaction History
                        HistoryRecords hr = accountingApi.getBankTransactionsHistory(accessToken, xeroTenantId,
                                oneBankTransaction.getBankTransactions().get(0).getBankTransactionID());
                        messages.add("Get a one Bank Transaction History Record - details :"
                                + hr.getHistoryRecords().get(0).getDetails());
                    } else {
                        messages.add("No BankTransactions found");
                    }

                    // CREATE Bank Transaction History
                    // Error: "The document with the supplied id was not found for this end point.
                    /*
                     * HistoryRecords historyRecords = new HistoryRecords(); HistoryRecord
                     * historyRecord = new HistoryRecord();
                     * historyRecord.setDetails("This is a sample history note");
                     * historyRecords.addHistoryRecordsItem(historyRecord); HistoryRecords newHr =
                     * accountingApi.createBankTransactionHistoryRecord(oneBankTransaction.
                     * getBankTransactions().get(0).getBankTransactionID(), historyRecords);
                     * messages.add("Create a one Bank Transaction History Record - details :" +
                     * newHr.getHistoryRecords().get(0).getDetails());
                     */
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("BatchPayments")) {
            // BATCH PAYMENTS
            try {
                // CREATE payment
                where = "Status==\"AUTHORISED\"&&Type==\"ACCREC\"";
                Invoices allInvoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                if (allInvoices.getInvoices().size() > 3) {
                    
                    Invoice inv = new Invoice();
                    inv.setInvoiceID(allInvoices.getInvoices().get(0).getInvoiceID());
                    Invoice inv2 = new Invoice();
                    inv2.setInvoiceID(allInvoices.getInvoices().get(1).getInvoiceID());
                    Invoice inv3 = new Invoice();
                    inv3.setInvoiceID(allInvoices.getInvoices().get(2).getInvoiceID());
                    where = null;
    
                    where = "EnablePaymentsToAccount==true";
                    Accounts accountsWhere = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                            order);
                    
                    if (accountsWhere.getAccounts().size() > 0) {
                        
                        Account paymentAccount = new Account();
                        paymentAccount.setAccountID(accountsWhere.getAccounts().get(0).getAccountID());
                        where = null;
        
                        BatchPayments createBatchPayments = new BatchPayments();
                        BatchPayment createBatchPayment = new BatchPayment();
                        createBatchPayment.setAccount(paymentAccount);
                        createBatchPayment.setAmount(3.0);
                        LocalDate currDate = LocalDate.now();
                        createBatchPayment.setDate(currDate);
                        createBatchPayment.setReference("Foobar" + loadRandomNum());
        
                        Payment payment01 = new Payment();
                        payment01.setAccount(paymentAccount);
                        payment01.setInvoice(inv);
                        payment01.setAmount(1.0);
                        payment01.setDate(currDate);
        
                        Payment payment02 = new Payment();
                        payment02.setAccount(paymentAccount);
                        payment02.setInvoice(inv2);
                        payment02.setAmount(1.0);
                        payment02.setDate(currDate);
        
                        Payment payment03 = new Payment();
                        payment03.setAccount(paymentAccount);
                        payment03.setInvoice(inv3);
                        payment03.setAmount(0.0);
                        payment03.setDate(currDate);
        
                        createBatchPayment.addPaymentsItem(payment01);
                        createBatchPayment.addPaymentsItem(payment02);
                        createBatchPayment.addPaymentsItem(payment03);
        
                        createBatchPayments.addBatchPaymentsItem(createBatchPayment);
        
                        BatchPayments newBatchPayments = accountingApi.createBatchPayment(accessToken, xeroTenantId,
                                createBatchPayments, false);
                        messages.add(
                                "Create BatchPayments - ID : " + newBatchPayments.getBatchPayments().get(0).getTotalAmount());
        
                        // GET all Payments
                        BatchPayments allBatchPayments = accountingApi.getBatchPayments(accessToken, xeroTenantId,
                                ifModifiedSince, where, order);
                        messages.add("Get BatchPayments - Total : " + allBatchPayments.getBatchPayments().size());
                    } else {
                        messages.add("No bank accounts found");
                    }

                } else {
                    messages.add("Not enough invoices found");
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("BrandingThemes")) {
            /* BRANDING THEME */
            try {
                // GET all BrandingTheme
                BrandingThemes bt = accountingApi.getBrandingThemes(accessToken, xeroTenantId);
                messages.add("Get a All Branding Themes - total : " + bt.getBrandingThemes().size());

                // GET one BrandingTheme
                UUID btID = bt.getBrandingThemes().get(0).getBrandingThemeID();
                BrandingThemes oneBt = accountingApi.getBrandingTheme(accessToken, xeroTenantId, btID);
                messages.add("Get a one Branding Themes - name : " + oneBt.getBrandingThemes().get(0).getName());
                
                // Create PaymentService for a Branding Theme 
                PaymentServices paymentServices  = accountingApi.getPaymentServices(accessToken, xeroTenantId); 
                UUID paymentServiceID = paymentServices.getPaymentServices().get(0).getPaymentServiceID();
                
                PaymentService btPaymentService = new PaymentService();
                btPaymentService.setPaymentServiceType("Custom");
                btPaymentService.setPaymentServiceID(paymentServiceID); PaymentServices
                createdPaymentService = accountingApi.createBrandingThemePaymentServices(accessToken, xeroTenantId, btID, btPaymentService);
                messages.add("Created payment services for Branding Themes - name : " + createdPaymentService.getPaymentServices().get(0).getPaymentServiceName());
                
                // GET Payment Services for a single Branding Theme
                PaymentServices paymentServicesForBrandingTheme = accountingApi.getBrandingThemePaymentServices(accessToken, xeroTenantId, btID);
                messages.add("Get payment services for Branding Themes - name : " + paymentServicesForBrandingTheme.getPaymentServices().get(0).getPaymentServiceName());

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Contacts")) {
            /* CONTACTS */
            try {
                // CREATE Single contact
                Contact contact = new Contact();
                //contact.setName("Foo" + loadRandomNum());
                contact.setName("Foo");
                contact.setEmailAddress("sid" + loadRandomNum() + "@blah.com");
                List<Phone> phones = new ArrayList<Phone>();
                Phone phone = new Phone();
                phone.setPhoneType(PhoneTypeEnum.MOBILE);
                phone.setPhoneNumber("555-1212");
                phone.setPhoneAreaCode("415");
                phones.add(phone);
                contact.setPhones(phones);
               
                Contacts contacts = new Contacts();
                Contact contact2 = new Contact();
                contact2.setName("Foo"  + loadRandomNum());
                contact2.setName("Foo");
                contacts.addContactsItem(contact2);
                
                Contact contact3 = new Contact();
                contact3.setName("Foo"  + loadRandomNum());
                contacts.addContactsItem(contact3);

                //CREATE MULTIPLE CONTACTS
                Contacts newContacts = accountingApi.createContacts(accessToken, xeroTenantId, contacts, true);
                messages.add("Create multiple Contacts - count : " + newContacts.getContacts().size());

                // MODIFY One contact to force error and test SummarizeErrors
                newContacts.getContacts().get(0).setSalesDefaultAccountCode("001");                
                // UPDATE MULTIPLE CONTACTS                
                Contacts updatedContacts = accountingApi.updateOrCreateContacts(accessToken, xeroTenantId, newContacts, false);
                messages.add("Update multiple Contact - Name : " + updatedContacts.getContacts().get(0).getName());

                // GET all contact
                Contacts contactsAll = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                messages.add("Get a All Contacts - Total : " + contacts.getContacts().size());

                // GET one contact
                UUID oneContactID = contactsAll.getContacts().get(0).getContactID();
                Contacts oneContact = accountingApi.getContact(accessToken, xeroTenantId, oneContactID);
                messages.add("Get a One Contact - Name : " + oneContact.getContacts().get(0).getName());

                // GET contact cisSettings
                where = "Name==\"sidney\"";
                Contacts cisContact = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                if (cisContact.getContacts().size() > 0) {
                    CISSettings cisSettings = accountingApi.getContactCISSettings(accessToken, xeroTenantId,
                            cisContact.getContacts().get(0).getContactID());
                    messages.add("Get a Contact cisSettings - Enabled? : "
                            + cisSettings.getCiSSettings().get(0).getCiSEnabled());
                }

                where = null;
                // GET active contacts
                where = "ContactStatus==\"ACTIVE\"";
                Contacts contactsWhere = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                messages.add("Get a all ACTIVE Contacts - Total : " + contactsWhere.getContacts().size());
                where = null;

                // Get Contact History
                HistoryRecords contactHistory = accountingApi.getContactHistory(accessToken, xeroTenantId, oneContactID);
                messages.add("Contact History - count : " + contactHistory.getHistoryRecords().size());

                // Create Contact History
                HistoryRecords newHistoryRecords = new HistoryRecords();
                HistoryRecord newHistoryRecord = new HistoryRecord();
                newHistoryRecord.setDetails("Hello World");
                newHistoryRecords.addHistoryRecordsItem(newHistoryRecord);

                HistoryRecords newInvoiceHistory = accountingApi.createContactHistory(accessToken, xeroTenantId,
                        oneContactID, newHistoryRecords);
                messages.add("Contact History - note added to  : "
                        + newInvoiceHistory.getHistoryRecords().get(0).getDetails());
                 
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("ContactGroups")) {

            /* CONTACT GROUP */
            try {
                // Create contact group
                ContactGroups newCGs = new ContactGroups();
                ContactGroup cg = new ContactGroup();
                cg.setName("NewGroup" + loadRandomNum());
                newCGs.addContactGroupsItem(cg);
                
                ContactGroups newContactGroup = accountingApi.createContactGroup(accessToken, xeroTenantId, newCGs);
                messages.add("Create a ContactGroup - Name : " + newContactGroup.getContactGroups().get(0).getName());

                // UPDATE Contact group
                newCGs.getContactGroups().get(0).setName("Old Group" + loadRandomNum());
                UUID newContactGroupID = newContactGroup.getContactGroups().get(0).getContactGroupID();
                ContactGroups updateContactGroup = accountingApi.updateContactGroup(accessToken, xeroTenantId,
                        newContactGroupID, newCGs);
                messages.add(
                        "Update a ContactGroup - Name : " + updateContactGroup.getContactGroups().get(0).getName());

                // GET all contact groups
                ContactGroups contactGroups = accountingApi.getContactGroups(accessToken, xeroTenantId, where, order);
                messages.add("Get all ContactGroups - Total : " + contactGroups.getContactGroups().size());

                // GET one contact groups
                UUID contactGroupId = contactGroups.getContactGroups().get(0).getContactGroupID();
                ContactGroups oneCg = accountingApi.getContactGroup(accessToken, xeroTenantId, contactGroupId);
                messages.add("Get one ContactGroups - Name : " + oneCg.getContactGroups().get(0).getName());

                // DELETE contact Group
                newCGs.getContactGroups().get(0).setStatus(com.xero.models.accounting.ContactGroup.StatusEnum.DELETED);
                UUID contactGroupID = newContactGroup.getContactGroups().get(0).getContactGroupID();
                ContactGroups deletedContactGroup = accountingApi.updateContactGroup(accessToken, xeroTenantId,
                        contactGroupID, contactGroups);
                messages.add(
                        "Delete a ContactGroup - Name : " + deletedContactGroup.getContactGroups().get(0).getName());

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("ContactGroupContacts")) {
            /* CONTACT GROUP CONTACTS */
            try {
                // Create new Contact Group
                ContactGroups newCGs = new ContactGroups();
                ContactGroup cg = new ContactGroup();
                cg.setName("NewGroup" + loadRandomNum());
                newCGs.addContactGroupsItem(cg);
                ContactGroups newContactGroup = accountingApi.createContactGroup(accessToken, xeroTenantId, newCGs);

                Contacts allContacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);

                // Create Contacts in Group
                Contacts contactList = new Contacts();
                contactList.addContactsItem(allContacts.getContacts().get(0));
                contactList.addContactsItem(allContacts.getContacts().get(1));
                UUID contactGroupID = newContactGroup.getContactGroups().get(0).getContactGroupID();
                Contacts addContacts = accountingApi.createContactGroupContacts(accessToken, xeroTenantId,
                        contactGroupID, contactList);
                messages.add("Add 2 Contacts to Contact Group - Total : " + addContacts.getContacts().size());

                // DELETE all Contacts in Group
                accountingApi.deleteContactGroupContacts(accessToken, xeroTenantId,
                        newContactGroup.getContactGroups().get(0).getContactGroupID());
                messages.add("Delete All Contacts  to Contact Group - no content in response ");

                ContactGroups oneCg = accountingApi.getContactGroup(accessToken, xeroTenantId,
                        newContactGroup.getContactGroups().get(0).getContactGroupID());
                messages.add(
                        "Get ContactGroups - Total Contacts : " + oneCg.getContactGroups().get(0).getContacts().size());

                if (allContacts.getContacts().size() > 3) {
                    // DELETE Single Contact
                    Contacts contactList2 = new Contacts();
                    contactList2.addContactsItem(allContacts.getContacts().get(1));
                    contactList2.addContactsItem(allContacts.getContacts().get(2));
    
                    UUID newContactGroupID = newContactGroup.getContactGroups().get(0).getContactGroupID();
                    Contacts addContacts2 = accountingApi.createContactGroupContacts(accessToken, xeroTenantId,
                            newContactGroupID, contactList2);
                    messages.add("Add 2 Contacts to Contact Group - Total : " + addContacts2.getContacts().size());
    
                    // DELETE Single CONACTS
                    accountingApi.deleteContactGroupContact(accessToken, xeroTenantId,
                            newContactGroup.getContactGroups().get(0).getContactGroupID(),
                            allContacts.getContacts().get(3).getContactID());
                    messages.add("Delete 1 contact from Contact Group - no content in response");
    
                    ContactGroups oneCg2 = accountingApi.getContactGroup(accessToken, xeroTenantId,
                            newContactGroup.getContactGroups().get(0).getContactGroupID());
                    messages.add("Get ContactGroups - Total Contacts : "
                            + oneCg2.getContactGroups().get(0).getContacts().size());
                } else {
                    messages.add("Not enough contacts to delete");
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("CreditNotesPDF")) {
            try {
                // GET CreditNote As a PDF
                CreditNotes creditNotes = accountingApi.getCreditNotes(accessToken, xeroTenantId, ifModifiedSince, where,
                        order, null,unitdp);
                UUID creditNoteId = creditNotes.getCreditNotes().get(0).getCreditNoteID();
                ByteArrayInputStream CreditNoteInput = accountingApi.getCreditNoteAsPdf(accessToken, xeroTenantId, creditNoteId);
                String CreditNoteFileName = "CreditNoteAsPDF.pdf";
    
                String CreditNoteSaveFilePath = saveFile(CreditNoteInput, CreditNoteFileName);
                messages.add("Get CreditNote attachment - save it here: " + CreditNoteSaveFilePath);
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (object.equals("CreditNotes")) {
            // CREDIT NOTE
            try {
                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);

                // Create Credit Note
                List<LineItem> lineItems = new ArrayList<>();
                LineItem li = new LineItem();
                li.setAccountCode("400");
                li.setDescription("Foobar");
                li.setQuantity(2.0999);
                li.setUnitAmount(20.0123);
                lineItems.add(li);

                CreditNotes newCNs = new CreditNotes();
                CreditNote cn = new CreditNote();
                cn.setContact(contacts.getContacts().get(0));
                cn.setLineItems(lineItems);
                cn.setType(com.xero.models.accounting.CreditNote.TypeEnum.ACCPAYCREDIT);
               
                newCNs.addCreditNotesItem(cn);
                newCNs.addCreditNotesItem(cn);
                
                // CREATE MULTIPLE CREDIT NOTES
                CreditNotes newCreditNotes = accountingApi.createCreditNotes(accessToken, xeroTenantId, newCNs,summarizeErrors,unitdp);
                messages.add("Create multiple CreditNotes - count : " + newCreditNotes.getCreditNotes().size());

                // MODIFY credit note and FORCE error to test summarizeErrors
                newCreditNotes.getCreditNotes().get(0).getLineItems().get(0).setAccountCode("4444444");
                
                // UPDATE MULTIPLE CREDIT NOTES
                CreditNotes newCreditNote = accountingApi.updateOrCreateCreditNotes(accessToken, xeroTenantId, newCreditNotes, summarizeErrors,unitdp);
                messages.add("Create a CreditNote - Amount : " + newCreditNote.getCreditNotes().get(0).getTotal());
                UUID newCreditNoteId = newCreditNote.getCreditNotes().get(0).getCreditNoteID();

                // GET all Credit Note
                CreditNotes creditNotes = accountingApi.getCreditNotes(accessToken, xeroTenantId, ifModifiedSince,
                        where, order, null,unitdp);
                messages.add("Get all CreditNotes - Total : " + creditNotes.getCreditNotes().size());

                // GET One Credit Note
                UUID creditNoteID = creditNotes.getCreditNotes().get(0).getCreditNoteID();
                CreditNotes oneCreditNote = accountingApi.getCreditNote(accessToken, xeroTenantId, creditNoteID,unitdp);
                messages.add("Get a CreditNote - Amount : " + oneCreditNote.getCreditNotes().get(0).getTotal());

                // UPDATE Credit Note
                newCNs.getCreditNotes().get(0).setStatus(com.xero.models.accounting.CreditNote.StatusEnum.AUTHORISED);
                CreditNotes updatedCreditNote = accountingApi.updateCreditNote(accessToken, xeroTenantId,
                        newCreditNoteId, newCNs,unitdp);
                messages.add("Update a CreditNote - Ref : " + updatedCreditNote.getCreditNotes().get(0).getReference());

                // Allocate Credit Note
                Allocations allocations = new Allocations();
                Allocation allocation = new Allocation();

                where = "Status==\"AUTHORISED\"&&Type==\"ACCPAY\"";
                Invoices allInvoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                Invoice inv = new Invoice();

                if (allInvoices.getInvoices().size() > 0) {
                    
                    inv.setInvoiceID(allInvoices.getInvoices().get(0).getInvoiceID());
                    allocation.setInvoice(inv);
                    allocation.setAmount(1.0);
                    LocalDate currDate = LocalDate.now();
                    allocation.setDate(currDate);
                    allocations.addAllocationsItem(allocation);
                    where = null;
                    Allocations allocatedCreditNote = accountingApi.createCreditNoteAllocation(accessToken, xeroTenantId,
                            newCreditNoteId, allocations, false);
                    messages.add("Update CreditNote Allocation - Amount : "
                            + allocatedCreditNote.getAllocations().get(0).getAmount());
                } else {
                    messages.add("No invoices found to allocated CreditNote to");
                }

                // Get CreditNote History
                HistoryRecords history = accountingApi.getCreditNoteHistory(accessToken, xeroTenantId, creditNoteID);
                messages.add("History - count : " + history.getHistoryRecords().size());

                // Create CreditNote History
                HistoryRecords newHistoryRecords = new HistoryRecords();
                HistoryRecord newHistoryRecord = new HistoryRecord();
                newHistoryRecord.setDetails("Hello World");
                newHistoryRecords.addHistoryRecordsItem(newHistoryRecord);

                HistoryRecords newHistory = accountingApi.createCreditNoteHistory(accessToken, xeroTenantId,
                        creditNoteID, newHistoryRecords);
                messages.add("History - note added to  : " + newHistory.getHistoryRecords().get(0).getDetails());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Currencies")) {

            /* CURRENCY */
            // JSON - incomplete
            try {
                // Get All
                Currencies currencies = accountingApi.getCurrencies(accessToken, xeroTenantId, where, order);
                messages.add("Get all Currencies - Total : " + currencies.getCurrencies().size());

                // Create New
                 Currency curr = new Currency(); curr.setCode(com.xero.models.accounting.CurrencyCode.SGD); Currencies
                 newCurrency = accountingApi.createCurrency(accessToken, xeroTenantId, curr);
                 messages.add("New Currencies - Code : " +
                 newCurrency.getCurrencies().get(0).getCode());
                 
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Employees")) {
            // EMPLOYEE
            try {
                // Create Multiple Employees
                Employees emps = new Employees();
                Employee employee1 = new Employee();
                employee1.setFirstName("Sid");
                employee1.setLastName("Maestre");
                Employee employee2 = new Employee();
                employee2.setFirstName("SidAllen4");
                emps.addEmployeesItem(employee1);
                emps.addEmployeesItem(employee2);

                Employees newEmployees = accountingApi.updateOrCreateEmployees(accessToken, xeroTenantId, emps, false);
                messages.add("Create multiple Employees - count : " + newEmployees.getEmployees().size());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            try {
                // Get All
                Employees employees = accountingApi.getEmployees(accessToken, xeroTenantId, ifModifiedSince, where, order);
                messages.add("Get all Employees - Total : " + employees.getEmployees().size());
                
                UUID newEmpId = employees.getEmployees().get(0).getEmployeeID();

                // Get One
                Employees oneEmployee = accountingApi.getEmployee(accessToken, xeroTenantId, newEmpId);
                messages.add("Get one Employees - Name : " + oneEmployee.getEmployees().get(0).getFirstName());

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("ExpenseClaims")) {
            // EXPENSE CLAIM
            try {
                // Create
                // where = "IsSubscriber==true";
                Users users = accountingApi.getUsers(accessToken, xeroTenantId, ifModifiedSince, where, order);
                where = null;

                where = "ShowInExpenseClaims==true&&Status==\"ACTIVE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                where = null;

                if (users.getUsers().size() > 0 && accounts.getAccounts().size() > 0) {
                    User user = new User();
                    user.setUserID(users.getUsers().get(0).getUserID());

                    Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                    Contact useContact = new Contact();
                    
                    if (contacts.getContacts().size() > 0 ) {
                      
                        useContact.setContactID(contacts.getContacts().get(0).getContactID());
    
                        // CREATE NEW RECEIPT
                        Receipts receipts = new Receipts();
                        Receipt receipt = new Receipt();
    
                        LineItem li = new LineItem();
                        li.setAccountCode(accounts.getAccounts().get(0).getCode());
                        li.setDescription("Foobar");
                        li.setQuantity(2.0);
                        li.setUnitAmount(20.0);
                        li.setLineAmount(40.0);
                        li.setTaxType("NONE");
    
                        receipt.addLineItemsItem(li);
                        receipt.setUser(user);
                        receipt.lineAmountTypes(LineAmountTypes.NOTAX);
                        receipt.contact(useContact);
                        receipt.setStatus(com.xero.models.accounting.Receipt.StatusEnum.DRAFT);
                        receipts.addReceiptsItem(receipt);
                        Receipts newReceipts = accountingApi.createReceipt(accessToken, xeroTenantId, receipts,unitdp);
    
                        // CREATE EXPENSE CLAIM
                        ExpenseClaim expenseClaim = new ExpenseClaim();
                        ExpenseClaims createExpenseClaims = new ExpenseClaims();
    
                        expenseClaim.setUser(user);
    
                        Receipts myReceipts = new Receipts();
                        Receipt myReceipt = new Receipt();
                        myReceipt.setReceiptID(newReceipts.getReceipts().get(0).getReceiptID());
                        myReceipts.addReceiptsItem(myReceipt);
                        expenseClaim.setReceipts(myReceipts.getReceipts());
                        expenseClaim.setStatus(com.xero.models.accounting.ExpenseClaim.StatusEnum.SUBMITTED);
                        createExpenseClaims.addExpenseClaimsItem(expenseClaim);
    
                        ExpenseClaims newExpenseClaim = accountingApi.createExpenseClaims(accessToken, xeroTenantId, createExpenseClaims);
                        messages.add("Create new Expense Claim - Status : "  + newExpenseClaim.getExpenseClaims().get(0).getStatus());
    
                        // UPDATE EXPENSE CLAIM
                        newExpenseClaim.getExpenseClaims().get(0).setStatus(com.xero.models.accounting.ExpenseClaim.StatusEnum.AUTHORISED);
                        UUID expenseClaimID = newExpenseClaim.getExpenseClaims().get(0).getExpenseClaimID();
                        ExpenseClaims updateExpenseClaims = accountingApi.updateExpenseClaim(accessToken, xeroTenantId, expenseClaimID, newExpenseClaim);
                        messages.add("Update new Expense Claim - Status : " + updateExpenseClaims.getExpenseClaims().get(0).getStatus());
    
                        // Get All Expense Claims
                        ExpenseClaims expenseClaims = accountingApi.getExpenseClaims(accessToken, xeroTenantId, ifModifiedSince, where, order);
                        messages.add("Get all Expense Claim - Total : " + expenseClaims.getExpenseClaims().size());
    
                        // Get One Expense Claim
                        ExpenseClaims oneExpenseClaim = accountingApi.getExpenseClaim(accessToken, xeroTenantId, expenseClaims.getExpenseClaims().get(0).getExpenseClaimID());
                        messages.add("Get one Expense Claim - Total : " + oneExpenseClaim.getExpenseClaims().get(0).getStatus());
    
                        // VOID EXPENSE CLAIM
                        newExpenseClaim.getExpenseClaims().get(0).setStatus(com.xero.models.accounting.ExpenseClaim.StatusEnum.VOIDED);
                        ExpenseClaims voidExpenseClaims = accountingApi.updateExpenseClaim(accessToken, xeroTenantId, expenseClaimID, newExpenseClaim);
                        messages.add("Void new Expense Claim - Status : "
                                + voidExpenseClaims.getExpenseClaims().get(0).getStatus());
    
                        // Get Expense Claim History
                        HistoryRecords history = accountingApi.getExpenseClaimHistory(accessToken, xeroTenantId,
                                expenseClaimID);
                        messages.add("History - count : " + history.getHistoryRecords().size());
                        
                    } else {
                        messages.add("No accounts found");
                    }

                    // Create Expense Claim History
                    // Error: "The document with the supplied id was not found for this endpoint.
                    /*
                    HistoryRecords newHistoryRecords = new HistoryRecords(); HistoryRecord
                    newHistoryRecord = new HistoryRecord();
                    newHistoryRecord.setDetails("Hello World");
                    newHistoryRecords.addHistoryRecordsItem(newHistoryRecord); HistoryRecords
                    newHistory = accountingApi.createExpenseClaimHistory(accessToken, xeroTenantId, expenseClaimID, newHistoryRecords);
                    messages.add("History - note added to  : " + newHistory.getHistoryRecords().get(0).getDetails());
                    */
                } else {
                    messages.add("No users found");
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Invoices")) {
            
            Invoice newInv = new Invoice();
            System.out.println(newInv.toString());
            
            // INVOICE
            try {
                // GET Invoice As a PDF
                Invoices myInvoicesForPDF = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                
                if (myInvoicesForPDF.getInvoices().size() > 0) {
                    UUID invoiceIDForPDF = myInvoicesForPDF.getInvoices().get(0).getInvoiceID();
                    ByteArrayInputStream InvoiceNoteInput = accountingApi.getInvoiceAsPdf(accessToken, xeroTenantId, invoiceIDForPDF);
                    String InvoiceFileName = "InvoiceAsPDF.pdf";
                    String InvoiceSaveFilePath = saveFile(InvoiceNoteInput, InvoiceFileName);
                messages.add("Get Invoice attachment - save it here: " + InvoiceSaveFilePath);
                } else {
                    messages.add("No Invoices for PDF found");
                }
        
                // Create Invoice
                where = "Type==\"REVENUE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                
                if (accounts.getAccounts().size() > 0) {
                    
                    String accountCodeForInvoice = accounts.getAccounts().get(0).getCode();
                    where = null;
        
                    Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                    
                    if (contacts.getContacts().size() > 0) {
                        UUID contactIDForInvoice = contacts.getContacts().get(0).getContactID();
            
                        for (int i = 0; i > contacts.getContacts().size(); i++) {
                            String email = contacts.getContacts().get(i).getEmailAddress().toString();
            
                            if (email != null && !email.isEmpty()) {
                                contactIDForInvoice = contacts.getContacts().get(i).getContactID();
                                break;
                            }
                        }
            
                        //contactIDForInvoice = UUID.fromString("9f857fe0-4a14-408b-b526-4f742db3b079");
                        Contact useContact = new Contact();
                        useContact.setContactID(contactIDForInvoice);
            
                        Invoice myInvoice = new Invoice();
                        LineItem li = new LineItem();
                        li.setAccountCode("140");
                        li.setDescription("Acme Tires");
                        li.setQuantity(2.0000);
                        li.setUnitAmount(20.00);
                        li.setLineAmount(40.00);
                        li.setTaxType("NONE");
                        
            
                        myInvoice.addLineItemsItem(li);
                        myInvoice.setContact(useContact);
                        LocalDate dueDate = LocalDate.of(2018, Month.DECEMBER, 10);
                        //myInvoice.setDueDate(dueDate);
                        LocalDate todayDate = LocalDate.now();
                        //myInvoice.setDate(todayDate);
                        myInvoice.setType(com.xero.models.accounting.Invoice.TypeEnum.ACCPAY);
                        myInvoice.setReference("One Fish, Two Fish");
                        myInvoice.setStatus(com.xero.models.accounting.Invoice.StatusEnum.DRAFT);
            
                        
                        // CREATE multiple INVOICES
                        Invoices myNewInvoices = new Invoices();
                        myNewInvoices.addInvoicesItem(myInvoice);
                        Invoices newInvoices = accountingApi.createInvoices(accessToken, xeroTenantId, myNewInvoices,false,unitdp);
                        messages.add("Create multiple invoice - count : " + newInvoices.getInvoices().size());
            
                        //MODIFY to force ERROR and test SummarizeError
                        //newInvoices.getInvoices().get(0).getLineItems().get(0).setAccountCode("2222222");
                        //newInvoices.getInvoices().add(myInvoice);
            
                        // CREATE OR UPDATE multiple INVOICES
                        Invoices updatedInvoices = accountingApi.updateOrCreateInvoices(accessToken, xeroTenantId, newInvoices, true,unitdp);
                        messages.add("Create OR Update invoice - Reference : " + updatedInvoices.getInvoices().get(0).getReference());                        
                        UUID newInvoiceID = newInvoices.getInvoices().get(0).getInvoiceID();
                        /*
                        RequestEmpty empty = new RequestEmpty();
                        accountingApi.emailInvoice(accessToken, xeroTenantId, newInvoiceID, empty);
                        messages.add("Email invoice - no content in response");
            
                        // UPDATE Invoice
                        Invoices updateInvoices = new Invoices();
                        Invoice updateInvoice = new Invoice();
                        updateInvoice.setInvoiceID(newInvoiceID);
                        updateInvoice.setReference("Red Fish, Blue Fish");
                        updateInvoices.addInvoicesItem(updateInvoice);
            
                        Invoices updatedInvoice = accountingApi.updateInvoice(accessToken, xeroTenantId, newInvoiceID,
                                updateInvoices,unitdp);
                        messages.add("Update invoice - Reference : " + updatedInvoice.getInvoices().get(0).getReference());
                        */
                        // Get All
                        Invoices invoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                        messages.add("Get all invoices - Total : " + invoices.getInvoices().size());
            
                        // Get Invoice If-Modified-Since
                        OffsetDateTime invModified = OffsetDateTime.of(LocalDateTime.of(2019, 12, 06, 15, 00), ZoneOffset.UTC);
                        Invoices invoicesSince = accountingApi.getInvoices(accessToken, xeroTenantId, invModified, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                        messages.add("Get all invoices - Since Modfied Date - Total : " + invoicesSince.getInvoices().size());
        
                        // Get One
                        Invoices oneInvoice = accountingApi.getInvoice(accessToken, xeroTenantId, invoices.getInvoices().get(0).getInvoiceID(), 1);
                        messages.add("Get one invoice - total : " + oneInvoice.getInvoices().get(0).getTotal());
                        LocalDate myDate = oneInvoice.getInvoices().get(0).getDateAsDate();
                        OffsetDateTime myUTC = oneInvoice.getInvoices().get(0).getUpdatedDateUTCAsDate();
                        messages.add("My string" + oneInvoice.getInvoices().get(0).getDate() + " as date : " + myDate.toString());
                        
                        // Get Online Invoice
                        OnlineInvoices onlineInvoice = accountingApi.getOnlineInvoice(accessToken, xeroTenantId, newInvoiceID);
                        messages.add(
                                "Get Online invoice - URL : " + onlineInvoice.getOnlineInvoices().get(0).getOnlineInvoiceUrl());
            
                        // Email Invoice
                        RequestEmpty empty2 = new RequestEmpty();
                        accountingApi.emailInvoice(accessToken, xeroTenantId, newInvoiceID, empty2);
                        messages.add("Email invoice - no content in response");
            
                        // Get Invoice History
                        HistoryRecords history = accountingApi.getInvoiceHistory(accessToken, xeroTenantId, newInvoiceID);
                        messages.add("History - count : " + history.getHistoryRecords().size());
            
                        // Create Invoice History
                        HistoryRecords newHistoryRecords = new HistoryRecords();
                        HistoryRecord newHistoryRecord = new HistoryRecord();
                        newHistoryRecord.setDetails("Hello World");
                        newHistoryRecords.addHistoryRecordsItem(newHistoryRecord);
                        HistoryRecords newHistory = accountingApi.createInvoiceHistory(accessToken, xeroTenantId, newInvoiceID,
                                newHistoryRecords);
                        messages.add("History - note added to  : " + newHistory.getHistoryRecords().get(0).getDetails());
            
                        // CREATE invoice attachment
                        statuses.add("AUTHORISED");
                        Invoices myInvoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                        UUID invoiceID = myInvoices.getInvoices().get(0).getInvoiceID();
            
                        File requestBodyFile = new File(
                                "/Users/sid.maestre/eclipse-workspace/xero-sdk-oauth2-dev-01/resources/youngsid.jpg");
                        String newFileName = requestBodyFile.getName();
            
                        Attachments createdAttachments = accountingApi.createInvoiceAttachmentByFileName(accessToken, xeroTenantId, invoiceID, newFileName, requestBodyFile, null);
                        messages.add("Attachment to Invoice complete - ID: "
                                + createdAttachments.getAttachments().get(0).getAttachmentID());
            
                        // GET Invoice Attachment
                        Attachments attachments = accountingApi.getInvoiceAttachments(accessToken, xeroTenantId, invoiceID);
                        UUID attachementId = attachments.getAttachments().get(0).getAttachmentID();
                        String contentType = attachments.getAttachments().get(0).getMimeType();
                        ByteArrayInputStream InvoiceAttachmentInput = accountingApi.getInvoiceAttachmentById(accessToken,
                                xeroTenantId, invoiceID, attachementId, contentType);
            
                        String InvoiceAttachmentFileName = attachments.getAttachments().get(0).getFileName();
                        String InvoiceAttachmentSaveFilePath = saveFile(InvoiceAttachmentInput, InvoiceAttachmentFileName);
                        messages.add("Get Invoice attachment - save it here: " + InvoiceAttachmentSaveFilePath);
                    } else {
                        messages.add("No contacts found");
                    }
                
                } else {
                    messages.add("No accounts found");
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (object.equals("InvoiceReminders")) {
            // INVOICE REMINDER
            try {
                InvoiceReminders invReminders = accountingApi.getInvoiceReminders(accessToken, xeroTenantId);
                messages.add("Get a Invoice Reminder - Is Enabled: "
                        + invReminders.getInvoiceReminders().get(0).getEnabled());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (object.equals("Items")) {
            // ITEM
            try {
                
                // CREATE MULTIPLE ITEMS
                Items myItems = new Items();
                Item item1 = new Item();
                item1.setCode("abc" + loadRandomNum());
                item1.setDescription("foobar");
                item1.setName("Hello" + loadRandomNum());
                myItems.addItemsItem(item1);

                Item item2 = new Item();
                item2.setCode("abc");
                item2.setDescription("foobar");
                item2.setName("Hello" + loadRandomNum());
                myItems.addItemsItem(item2);

                // CREATE Multiple Items
                Items newItems = accountingApi.createItems(accessToken, xeroTenantId, myItems, false,unitdp);
                messages.add("Create multiple items - count : " + newItems.getItems().size());
                UUID newItemId = newItems.getItems().get(0).getItemID();
                
               /* // Modify Item and FORCE error to test SummarizeError
                Item item3 = new Item();
                item3.setCode("abc"  + loadRandomNum());
                item3.setDescription("foobar");
                item3.setName("Hello" + loadRandomNum());
                newItems.addItemsItem(item3);

                Items newItemsToUpdate = new Items();
                newItemsToUpdate.addItemsItem(item3);
                
                Item item4 = new Item();
                item4.setItemID(newItems.getItems().get(1).getItemID());
                item4.setCode("abc");
                newItemsToUpdate.addItemsItem(item4);
                
                // CREATE or UPDATE multiple items
                Items udpatedItems = accountingApi.updateOrCreateItems(accessToken, xeroTenantId, newItemsToUpdate,false,unitdp);
                System.out.println(udpatedItems.toString());
                messages.add("Create new item - Description : " + udpatedItems.getItems().get(0).getDescription());
               

                // Update Item
                newItems.getItems().get(0).setDescription("Barfoo");
                Items updateItem = accountingApi.updateItem(accessToken, xeroTenantId, newItemId, newItems,4);
                messages.add("Update item - Description : " + updateItem.getItems().get(0).getDescription());
                */
                
                // Get All Items
                Items items = accountingApi.getItems(accessToken, xeroTenantId, ifModifiedSince, where, order, unitdp);
                messages.add("Get all items - Total : " + items.getItems().size());
                
                // Get One Item
                UUID itemId = items.getItems().get(0).getItemID();
                Items oneItem = accountingApi.getItem(accessToken, xeroTenantId, itemId,4);
                messages.add("Get one item - Description : " + oneItem.getItems().get(0).getDescription());

                // Get Invoice History
                HistoryRecords history = accountingApi.getItemHistory(accessToken, xeroTenantId, itemId);
                messages.add("History - count : " + history.getHistoryRecords().size());
     
                // Create Invoice History
                // Error: "The document with the supplied id was not found for this endpoint.
                /*
                 * HistoryRecords newHistoryRecords = new HistoryRecords(); HistoryRecord
                 * newHistoryRecord = new HistoryRecord();
                 * newHistoryRecord.setDetails("Hello World");
                 * newHistoryRecords.addHistoryRecordsItem(newHistoryRecord); HistoryRecords
                 * createdHistory = accountingApi.createItemHistory(itemId,newHistoryRecords);
                 * messages.add("History - note added to  : " +
                 * createdHistory.getHistoryRecords().get(0).getDetails());
                 */

                // Delete
                accountingApi.deleteItem(accessToken, xeroTenantId, newItemId);
                messages.add("Delete one item - no content in response");
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Journals")) {
            // JOURNAL
            try {

                boolean paymentsOnly = true;
                // GET all Journals
                Journals journals = accountingApi.getJournals(accessToken, xeroTenantId, ifModifiedSince, null,
                        paymentsOnly);
                messages.add("Get Journals - total : " + journals.getJournals().size());

                // GET Journal with offset
               Journals journalsOffset = accountingApi.getJournals(accessToken, xeroTenantId, ifModifiedSince, null,
                        paymentsOnly);
                messages.add("Get Journals offset - total : " + journalsOffset.getJournals().size());
  
                // 404 ERROR
                // GET one Journal 
                UUID journalId = journals.getJournals().get(9).getJournalID(); 
                Journals oneJournal = accountingApi.getJournal(accessToken, xeroTenantId,journalId);
                messages.add("Get one Journal - number : " + oneJournal.getJournals().get(0).getJournalNumber());
                 
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("LinkedTransactions")) {
            /* LINKED TRANSACTION */
            try {
                // Create Linked Transaction
                where = "STATUS==\"ACTIVE\"&&Type==\"EXPENSE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                where = null;

                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order,contactIds, null, includeArchived);
                Contact useContact = new Contact();
                useContact.setContactID(contacts.getContacts().get(0).getContactID());

                Invoice myInvoice = new Invoice();

                LineItem li = new LineItem();
                li.setAccountCode(accounts.getAccounts().get(0).getCode());
                li.setDescription("Acme Tires");
                li.setQuantity(2.00);
                li.setUnitAmount(20.00);
                li.setLineAmount(40.00);
                li.setTaxType("NONE");

                myInvoice.addLineItemsItem(li);
                myInvoice.setContact(useContact);
                LocalDate dueDate = LocalDate.of(2018, Month.OCTOBER, 10);
                myInvoice.setDueDate(dueDate);
                LocalDate todayDate = LocalDate.now();
                myInvoice.setDate(todayDate);
                myInvoice.setType(com.xero.models.accounting.Invoice.TypeEnum.ACCPAY);
                myInvoice.setReference("One Fish, Two Fish");
                myInvoice.setStatus(com.xero.models.accounting.Invoice.StatusEnum.AUTHORISED);
                Invoices newInvoices = new Invoices();
                newInvoices.addInvoicesItem(myInvoice);
                Invoices newInvoice = accountingApi.createInvoices(accessToken, xeroTenantId, newInvoices, false,unitdp);

                UUID sourceTransactionID1 = newInvoice.getInvoices().get(0).getInvoiceID();
                UUID sourceLineItemID1 = newInvoice.getInvoices().get(0).getLineItems().get(0).getLineItemID();
                LinkedTransaction newLinkedTransaction = new LinkedTransaction();
                newLinkedTransaction.setSourceTransactionID(sourceTransactionID1);
                newLinkedTransaction.setSourceLineItemID(sourceLineItemID1);
                
                LinkedTransactions createdLinkedTransaction = accountingApi.createLinkedTransaction(accessToken,  xeroTenantId, newLinkedTransaction);
                messages.add("TEST Create LinkedTransaction - Status : "
                        + createdLinkedTransaction.getLinkedTransactions().get(0).getStatus());

                // Created Linked Transaction 2
                Contact contact = new Contact();
                contact.setName("Foo" + loadRandomNum());
                contact.setEmailAddress("sid" + loadRandomNum() + "@blah.com");
                Contacts arrayContacts = new Contacts();
                arrayContacts.addContactsItem(contact);
                Contacts newContact = accountingApi.createContacts(accessToken, xeroTenantId, arrayContacts,false);
                UUID newContactID = newContact.getContacts().get(0).getContactID();

                Invoices newInvoice2 = accountingApi.createInvoices(accessToken, xeroTenantId, newInvoices, true, 1);

                UUID sourceTransactionID2 = newInvoice2.getInvoices().get(0).getInvoiceID();
                UUID sourceLineItemID2 = newInvoice2.getInvoices().get(0).getLineItems().get(0).getLineItemID();
                LinkedTransactions newLinkedTransactions2 = new LinkedTransactions();
                LinkedTransaction newLinkedTransaction2 = new LinkedTransaction();
                newLinkedTransaction2.setSourceTransactionID(sourceTransactionID2);
                newLinkedTransaction2.setSourceLineItemID(sourceLineItemID2);
                newLinkedTransaction2.setContactID(newContactID);
                newLinkedTransactions2.addLinkedTransactionsItem(newLinkedTransaction2);

                LinkedTransactions createdLinkedTransaction2 = accountingApi.createLinkedTransaction(accessToken,xeroTenantId, newLinkedTransaction2);
                messages.add("Create LinkedTransaction 2 - Status : "
                        + createdLinkedTransaction2.getLinkedTransactions().get(0).getStatus());

                // Created Linked Transaction 3
                Invoices newInvoicesAccRec = new Invoices();
                Invoice myInvoiceAccRec = new Invoice();

                myInvoiceAccRec.addLineItemsItem(li);
                myInvoiceAccRec.setContact(useContact);

                myInvoiceAccRec.setDueDate(dueDate);
                myInvoiceAccRec.setDate(todayDate);

                myInvoiceAccRec.setType(com.xero.models.accounting.Invoice.TypeEnum.ACCREC);
                myInvoiceAccRec.setStatus(com.xero.models.accounting.Invoice.StatusEnum.AUTHORISED);
                newInvoicesAccRec.addInvoicesItem(myInvoiceAccRec);
                

                Invoices newInvoiceAccRec = accountingApi.createInvoices(accessToken, xeroTenantId, newInvoicesAccRec, true, 1);
                UUID sourceTransactionID4 = newInvoiceAccRec.getInvoices().get(0).getInvoiceID();
                UUID sourceLineItemID4 = newInvoiceAccRec.getInvoices().get(0).getLineItems().get(0).getLineItemID();

                Invoices newInvoice3 = accountingApi.createInvoices(accessToken, xeroTenantId, newInvoices, true, 1);

                UUID sourceTransactionID3 = newInvoice3.getInvoices().get(0).getInvoiceID();
                UUID sourceLineItemID3 = newInvoice3.getInvoices().get(0).getLineItems().get(0).getLineItemID();
                LinkedTransaction newLinkedTransaction3 = new LinkedTransaction();
                newLinkedTransaction3.setSourceTransactionID(sourceTransactionID3);
                newLinkedTransaction3.setSourceLineItemID(sourceLineItemID3);
                newLinkedTransaction3.setContactID(useContact.getContactID());
                newLinkedTransaction3.setTargetTransactionID(sourceTransactionID4);
                newLinkedTransaction3.setTargetLineItemID(sourceLineItemID4);
                 

                LinkedTransactions createdLinkedTransaction3 = accountingApi.createLinkedTransaction(accessToken, xeroTenantId, newLinkedTransaction3);
                messages.add("Create LinkedTransaction 3 - Status : "
                        + createdLinkedTransaction3.getLinkedTransactions().get(0).getStatus());
                
                LinkedTransactions createdLinkedTransactionSINGLE = accountingApi.createLinkedTransaction(accessToken,
                        xeroTenantId, newLinkedTransaction);
                messages.add("Create LinkedTransaction SINGLE - Status : "
                        + createdLinkedTransactionSINGLE.getLinkedTransactions().get(0).getStatus());

                // GET all Link Transactions

                int page = 1;
                String linkedTransactionID = null;
                String sourceTransactionID = null;
                String targetTransactionID = null;
                String status = null;
                String contactID = null;
                LinkedTransactions linkTransactions = accountingApi.getLinkedTransactions(accessToken, xeroTenantId,
                        page, linkedTransactionID, sourceTransactionID, contactID, status, targetTransactionID);
                messages.add("Get Link Transactions - total : " + linkTransactions.getLinkedTransactions().size());

                // GET all Link Transactions
                UUID linkedTransactionID2 = linkTransactions.getLinkedTransactions().get(0).getLinkedTransactionID();
                LinkedTransactions oneLinkTransaction = accountingApi.getLinkedTransaction(accessToken, xeroTenantId,
                        linkedTransactionID2);
                messages.add("Get one Link Transaction - Status : "
                        + oneLinkTransaction.getLinkedTransactions().get(0).getStatus());
               
                // 500 Error
                /*
                 * // DELETE LINKEDTRANSACTION UUID newLinkedTransactionID =
                 * createdLinkedTransaction.getLinkedTransactions().get(0).
                 * getLinkedTransactionID();
                 * accountingApi.deleteLinkedTransaction(newLinkedTransactionID);
                 * messages.add("Delete LinkedTransaction - no content in response");
                 */
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("ManualJournals")) {
            // MANUAL JOURNAL
            try {
                // Create Manual Journal
                where = "Type==\"EXPENSE\" && Status ==\"ACTIVE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                String accountCode = accounts.getAccounts().get(0).getCode();
                where = null;
                ManualJournals manualJournals = new ManualJournals();
                // create first MJ
                ManualJournal manualJournal = new ManualJournal();
                LocalDate currDate = LocalDate.now();
                manualJournal.setDate(currDate);
                manualJournal.setNarration("Foo bar");

                ManualJournalLine credit = new ManualJournalLine();
                credit.setDescription("Hello there");
                credit.setAccountCode(accountCode);
                credit.setLineAmount(100.00);
                manualJournal.addJournalLinesItem(credit);

                ManualJournalLine debit = new ManualJournalLine();
                debit.setDescription("Goodbye");
                debit.setAccountCode(accountCode);
                debit.setLineAmount(-100.00);
                manualJournal.addJournalLinesItem(debit);
                manualJournals.addManualJournalsItem(manualJournal);
                
                // create second MJ
                ManualJournal manualJournal2 = new ManualJournal();
                ManualJournalLine credit2 = new ManualJournalLine();
                credit2.setDescription("Hello there");
                credit2.setAccountCode(accountCode);
                credit2.setLineAmount(100.00);
                manualJournal2.addJournalLinesItem(credit2);

                ManualJournalLine debit2 = new ManualJournalLine();
                debit2.setDescription("Goodbye");
                debit2.setAccountCode(accountCode);
                debit2.setLineAmount(-100.00);
                manualJournal2.addJournalLinesItem(debit2);
                manualJournals.addManualJournalsItem(manualJournal2);
                
                ManualJournals createdManualJournals = accountingApi.createManualJournals(accessToken, xeroTenantId,
                        manualJournals, false);
                UUID newManualJournalId = createdManualJournals.getManualJournals().get(0).getManualJournalID();
                messages.add("Create Manual Journal - Narration : "
                        + createdManualJournals.getManualJournals().get(0).getNarration());

                
                ManualJournals updateOrcreatedManualJournal = accountingApi.updateOrCreateManualJournals(accessToken, xeroTenantId, manualJournals, false);
                messages.add("Create Single Manual Journal - Narration : "
                        + updateOrcreatedManualJournal.getManualJournals().get(0).getNarration());
                
                // GET all Manual Journal
                ManualJournals getManualJournals = accountingApi.getManualJournals(accessToken, xeroTenantId,
                        ifModifiedSince, where, order, null);
                messages.add("Get Manual Journal - total : " + getManualJournals.getManualJournals().size());

                // GET one Manual Journal
                UUID manualJournalId = getManualJournals.getManualJournals().get(0).getManualJournalID();
                ManualJournals oneManualJournal = accountingApi.getManualJournal(accessToken, xeroTenantId,
                        manualJournalId);
                messages.add("Get one Manual Journal - Narration : "
                        + oneManualJournal.getManualJournals().get(0).getNarration());

                // Update Manual Journal
                ManualJournals updateManualJournals = new ManualJournals();
                ManualJournal updateManualJournal = new ManualJournal();
                updateManualJournal.setManualJournalID(newManualJournalId);
                updateManualJournal.setNarration("Hello Xero");
                updateManualJournals.addManualJournalsItem(updateManualJournal);
                ManualJournals updatedManualJournal = accountingApi.updateManualJournal(accessToken, xeroTenantId,
                        newManualJournalId, updateManualJournals);
                messages.add("Update Manual Journal - Narration : "
                        + updatedManualJournal.getManualJournals().get(0).getNarration());

            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (object.equals("Organisations")) {
            // Organisation
            try {
                Organisations organisations = accountingApi.getOrganisations(accessToken, xeroTenantId);
                messages.add("Get a Organisation - Name : " + organisations.getOrganisations().get(0).getName());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else if (object.equals("Overpayments")) {
            // OVERPAYMENT
            try {
                where = "Status==\"ACTIVE\"&&Type==\"BANK\"";
                Accounts accountsWhere = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                Account bankAccount = new Account();
                bankAccount.setAccountID(accountsWhere.getAccounts().get(0).getAccountID());
                where = null;
    
                where = "SystemAccount==\"DEBTORS\"";
                Accounts arAccounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                Account arAccount = arAccounts.getAccounts().get(0);
                where = null;
    
                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                Contact useContact = new Contact();
                useContact.setContactID(contacts.getContacts().get(0).getContactID());
    
                // Maker sure we have at least 2 banks
                if (accountsWhere.getAccounts().size() > 0) {
                    List<LineItem> lineItems = new ArrayList<>();
                    LineItem li = new LineItem();
                    li.setAccountCode(arAccount.getCode());
                    li.setDescription("Foobar");
                    li.setQuantity(1.0987);
                    li.setUnitAmount(20.0088);
                    lineItems.add(li);
    
                    BankTransaction bt = new BankTransaction();
                    bt.setBankAccount(bankAccount);
                    bt.setContact(useContact);
                    bt.setLineItems(lineItems);
                    bt.setType(com.xero.models.accounting.BankTransaction.TypeEnum.RECEIVE_OVERPAYMENT);
                    BankTransactions bts = new BankTransactions();
                    bts.addBankTransactionsItem(bt);
                    BankTransactions newBankTransaction = accountingApi.createBankTransactions(accessToken, xeroTenantId, bts, false,unitdp);
    
                    Overpayments overpayments = accountingApi.getOverpayments(accessToken, xeroTenantId, null, null, order, null, null);
                    messages.add("Get a Overpayments - Count : " + overpayments.getOverpayments().size());
    
                    if (overpayments.getOverpayments().size() > 0) {
                        UUID overpaymentId = overpayments.getOverpayments().get(0).getOverpaymentID();
                        Overpayments oneOverpayment = accountingApi.getOverpayment(accessToken, xeroTenantId,
                                overpaymentId);
                        messages.add("Get one Overpayment - Total : " + oneOverpayment.getOverpayments().get(0).getTotal());
    
                        where = "Status==\"AUTHORISED\"&&Type==\"ACCREC\"";
                        Invoices allInvoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where,order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                        Invoice inv = new Invoice();
                        inv.setInvoiceID(allInvoices.getInvoices().get(0).getInvoiceID());
                        where = null;
    
                        Allocations allocations = new Allocations();
                        Allocation allocation = new Allocation();
                        allocation.setAmount(1.0);
                        LocalDate currDate = LocalDate.now();
                        allocation.setDate(currDate);
                        allocation.setInvoice(inv);
                        allocations.addAllocationsItem(allocation);
    
                        // 2nd allocations
                        Allocation allocation2 = new Allocation();
                        allocation2.setAmount(-1.0);
                        allocation2.setDate(currDate);
                        allocation2.setInvoice(inv);
                        allocations.addAllocationsItem(allocation2);
    
                        try {
                            Allocations newAllocation = accountingApi.createOverpaymentAllocations(accessToken, xeroTenantId, overpaymentId, allocations, false);
                            messages.add("Create OverPayment allocations - Amt : "      + newAllocation.getAllocations().get(0).getAmount());
                        } catch (XeroBadRequestException e) {
                            this.addBadRequest(e, messages); 
                        } catch (XeroForbiddenException e) {
                            this.addError(e, messages); 
                        } catch (XeroNotFoundException e) {
                            this.addError(e, messages); 
                        } catch (XeroUnauthorizedException e) {
                            this.addError(e, messages); 
                        } catch (XeroMethodNotAllowedException e) {
                            this.addMethodNotAllowedException(e, messages); 
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        
                        // Get History
                        HistoryRecords history = accountingApi.getOverpaymentHistory(accessToken, xeroTenantId,
                                overpaymentId);
                        messages.add("History - count : " + history.getHistoryRecords().size());
    
                        // Create History
                        // Error: "The document with the supplied id was not found for this endpoint.
                        /*
                         * HistoryRecords newHistoryRecords = new HistoryRecords(); HistoryRecord
                         * newHistoryRecord = new HistoryRecord();
                         * newHistoryRecord.setDetails("Hello World");
                         * newHistoryRecords.addHistoryRecordsItem(newHistoryRecord); HistoryRecords
                         * createdHistory =
                         * accountingApi.createOverpaymentHistory(overpaymentId,newHistoryRecords);
                         * messages.add("History - note added to  : " +
                         * createdHistory.getHistoryRecords().get(0).getDetails());
                         */
                    }
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (object.equals("Payments")) {
            /* Payment */
            try {

                // CREATE payment
                where = "Status==\"AUTHORISED\"&&Type==\"ACCREC\"";
                Invoices allInvoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince, where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived, createdByMyApp, null);
                Invoice inv = new Invoice();
                inv.setInvoiceID(allInvoices.getInvoices().get(0).getInvoiceID());
                where = null;

                where = "EnablePaymentsToAccount==true";
                Accounts accountsWhere = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                Account paymentAccount = new Account();
                paymentAccount.setCode(accountsWhere.getAccounts().get(0).getCode());
                where = null;

                Payments createPayments = new Payments();
                Payment createPayment = new Payment();
                createPayment.setAccount(paymentAccount);
                createPayment.setInvoice(inv);
                createPayment.setAmount(1.00);

                LocalDate currDate = LocalDate.now();
                createPayment.setDate(currDate);
                createPayments.addPaymentsItem(createPayment);

                //Payments newPayments = accountingApi.createPayments(accessToken, xeroTenantId, createPayments);
                //messages.add("Create Payments - Amt : " + newPayments.getPayments().get(0).getAmount());

                Payments newPaymentSingle = accountingApi.createPayment(accessToken, xeroTenantId, createPayment);
                messages.add("Create Payments - Amt : " + newPaymentSingle.getPayments().get(0).getAmount());

                
                // GET all Payments
                Payments payments = accountingApi.getPayments(accessToken, xeroTenantId, ifModifiedSince, where, order, 1);
                messages.add("Get Payments - Total : " + payments.getPayments().size());

                // GET one Payment
                UUID paymentID = payments.getPayments().get(0).getPaymentID();
                Payments onePayment = accountingApi.getPayment(accessToken, xeroTenantId, paymentID);
                messages.add("Get Payments - Amount : " + onePayment.getPayments().get(0).getAmount());

                // DELETE Payment
                UUID paymentID2 = payments.getPayments().get(1).getPaymentID();  
                PaymentDelete paymentDelete = new PaymentDelete();
                Payments deletedPayment = accountingApi.deletePayment(accessToken, xeroTenantId, paymentID2, paymentDelete);
                messages.add("DELETE Payment - ID : " + deletedPayment.getPayments().get(0).getPaymentID());
                
                
                // Get History
                HistoryRecords allHistory = accountingApi.getPaymentHistory(accessToken, xeroTenantId, paymentID);
                messages.add("History - count : " + allHistory.getHistoryRecords().size());

                // Create History
                /*
                 * HistoryRecords newHistoryRecords = new HistoryRecords(); HistoryRecord
                 * newHistoryRecord = new HistoryRecord();
                 * newHistoryRecord.setDetails("Hello World");
                 * newHistoryRecords.addHistoryRecordsItem(newHistoryRecord); HistoryRecords
                 * newHistory = accountingApi.createPaymentHistory(paymentID,newHistoryRecords);
                 * messages.add("History - note added to  : " +
                 * newHistory.getHistoryRecords().get(0).getDetails());
                 */
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("PaymentServices")) {
            // Payment Services
            try {
                // CREATE PaymentService
                PaymentServices newPaymentServices = new PaymentServices();
                PaymentService newPaymentService = new PaymentService();
                newPaymentService.setPaymentServiceName("PayUp" + loadRandomNum());
                newPaymentService.setPaymentServiceUrl("https://www.payupnow.com/");
                newPaymentService.setPayNowText("Time To PayUp");
                newPaymentServices.addPaymentServicesItem(newPaymentService);
                PaymentServices createdPaymentService = accountingApi.createPaymentService(accessToken, xeroTenantId,
                        newPaymentServices);
                messages.add("Create PaymentServices - name : "
                        + createdPaymentService.getPaymentServices().get(0).getPaymentServiceName());

                // GET all Payments
                PaymentServices paymentServices = accountingApi.getPaymentServices(accessToken, xeroTenantId);
                messages.add("Get PaymentServices - Total : " + paymentServices.getPaymentServices().size());
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Prepayments")) {
            /* PREPAYMENT */
            try {
                
               
                
                /*
                where = "Status==\"ACTIVE\"&&Type==\"BANK\"";
                Accounts accountsWhere = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                Account bankAccount = new Account();
                bankAccount.setAccountID(accountsWhere.getAccounts().get(0).getAccountID());
                where = null;

                where = "Type==\"EXPENSE\"";
                Accounts arAccounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                Account arAccount = arAccounts.getAccounts().get(0);
                where = null;

                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order,contactIds, null, includeArchived);
                Contact useContact = new Contact();
                useContact.setContactID(contacts.getContacts().get(0).getContactID());

                // Maker sure we have at least 2 banks
                if (accountsWhere.getAccounts().size() > 0) {
                    List<LineItem> lineItems = new ArrayList<>();
                    LineItem li = new LineItem();
                    li.setAccountCode(arAccount.getCode());
                    li.setDescription("Foobar");
                    li.setQuantity(1.0123);
                    li.setTaxType("NONE");
                    li.setUnitAmount(20.0033);
                    lineItems.add(li);

                    BankTransaction bt = new BankTransaction();
                    bt.setBankAccount(bankAccount);
                    bt.setContact(useContact);
                    bt.setLineItems(lineItems);
                    bt.setType(com.xero.models.accounting.BankTransaction.TypeEnum.RECEIVE_PREPAYMENT);
                    
                    BankTransactions bts = new BankTransactions();
                    bts.addBankTransactionsItem(bt);
                    BankTransactions newBankTransaction = accountingApi.createBankTransactions(accessToken, xeroTenantId, bts, false,unitdp);

                    where = "Status==\"AUTHORISED\" && TYPE==\"RECEIVE-PREPAYMENT\"";
                    Prepayments prepayments = accountingApi.getPrepayments(accessToken, xeroTenantId, ifModifiedSince,
                            where, order, null, null);
                    messages.add("Get a Prepayments - Count : " + prepayments.getPrepayments().size());
                    where = null;
                    if (prepayments.getPrepayments().size() > 0) {
                        UUID prepaymentId = prepayments.getPrepayments().get(0).getPrepaymentID();
                        Prepayments onePrepayment = accountingApi.getPrepayment(accessToken, xeroTenantId,
                                prepaymentId);
                        messages.add(
                                "Get one Prepayment - Total : " + onePrepayment.getPrepayments().get(0).getTotal());
                        where = "Status==\"AUTHORISED\"&&Type==\"ACCREC\"";
                        Invoices allInvoices = accountingApi.getInvoices(accessToken, xeroTenantId, ifModifiedSince,  where, order, invoiceIds, invoiceNumbers, contactIds, statuses, null, includeArchived,createdByMyApp, null);
                        Invoice inv = new Invoice();
                        inv.setInvoiceID(allInvoices.getInvoices().get(0).getInvoiceID());
                        where = null;

                        Allocations allocations = new Allocations();
                        Allocation allocation = new Allocation();
                        allocation.setAmount(1.0);
                        LocalDate currDate = LocalDate.now();
                        allocation.setDate(currDate);
                        allocation.setInvoice(inv);
                        allocations.addAllocationsItem(allocation);

                        // Allocations newAllocation =
                        // accountingApi.createPrepaymentAllocation(prepaymentId, allocations);
                        // messages.add("Create PrePayment allocation - Amt : " +
                        // newAllocation.getAllocations().get(0).getAmount());

                        // Get History
                        HistoryRecords history = accountingApi.getPrepaymentHistory(accessToken, xeroTenantId,
                                prepaymentId);
                        messages.add("History - count : " + history.getHistoryRecords().size());

                        // Create History
                        // Error: "The document with the supplied id was not found for this end point.
                       
                         * HistoryRecords newHistoryRecords = new HistoryRecords(); HistoryRecord
                         * newHistoryRecord = new HistoryRecord();
                         * newHistoryRecord.setDetails("Hello World");
                         * newHistoryRecords.addHistoryRecordsItem(newHistoryRecord); HistoryRecords
                         * createdHistory =
                         * accountingApi.createPrepaymentHistory(prepaymentId,newHistoryRecords);
                         * messages.add("History - note added to  : " +
                         * createdHistory.getHistoryRecords().get(0).getDetails());
                         
                    }
                }
        */
       
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("PurchaseOrders")) {
            // PURCHASE ORDERS
            try {
                // CREATE Purchase Order
                where = "Type==\"EXPENSE\"";
                Accounts arAccounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where,
                        order);
                Account arAccount = arAccounts.getAccounts().get(1);
                where = null;

                PurchaseOrder purchaseOrder = new PurchaseOrder();
                LocalDate currDate = LocalDate.now();
                purchaseOrder.setDate(currDate);
                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                Contact useContact = new Contact();
                useContact.setContactID(contacts.getContacts().get(0).getContactID());
                purchaseOrder.setContact(useContact);

                List<LineItem> lineItems = new ArrayList<>();
                LineItem li = new LineItem();
                li.setAccountCode(arAccount.getCode());
                li.setDescription("Foobar");
                li.setQuantity(1.0123);
                li.setUnitAmount(20.0033);
                lineItems.add(li);
                purchaseOrder.setLineItems(lineItems);
                
                UUID id = UUID.fromString("3f93110a-df13-49c7-b82f-a069813df188");
                PurchaseOrder purchaseOrder2 = new PurchaseOrder();
                purchaseOrder2.setDate(currDate);
                Contact useContact2 = new Contact();
                useContact2.setContactID(id);
                purchaseOrder2.setContact(useContact);
                purchaseOrder2.setLineItems(lineItems);
                
                // CREATE MULTIPLE Purchase Orders
                PurchaseOrders purchaseOrders = new PurchaseOrders();
                purchaseOrders.addPurchaseOrdersItem(purchaseOrder);
                purchaseOrders.addPurchaseOrdersItem(purchaseOrder2);
                PurchaseOrders createdPurchaseOrders = accountingApi.createPurchaseOrders(accessToken, xeroTenantId, purchaseOrders, summarizeErrors);
                messages.add("Create multiple Purchase order - count : " + createdPurchaseOrders.getPurchaseOrders().size());

                // UPDATE MULTIPLE Purchase Orders
                createdPurchaseOrders.getPurchaseOrders().get(0).getLineItems().get(0).setAccountCode("11111");
                createdPurchaseOrders.addPurchaseOrdersItem(purchaseOrder);
                PurchaseOrders updatedPurchaseOrders = accountingApi.updateOrCreatePurchaseOrders(accessToken, xeroTenantId,  createdPurchaseOrders, summarizeErrors);
                messages.add("Create Purchase order - total : " + updatedPurchaseOrders.getPurchaseOrders().get(0).getTotal());
              
                // UPDATE Purchase Order
                UUID newPurchaseOrderID = createdPurchaseOrders.getPurchaseOrders().get(0).getPurchaseOrderID();
                createdPurchaseOrders.getPurchaseOrders().get(0).setAttentionTo("Jimmy");
                PurchaseOrders updatePurchaseOrders = accountingApi.updatePurchaseOrder(accessToken, xeroTenantId,
                        newPurchaseOrderID, createdPurchaseOrders);
                messages.add("Update Purchase order - attn : "
                        + updatePurchaseOrders.getPurchaseOrders().get(0).getAttentionTo());

                // GET Purchase Orders
                String status = null;
                String dateFrom = null;
                String dateTo = null;
                PurchaseOrders allPurchaseOrders = accountingApi.getPurchaseOrders(accessToken, xeroTenantId,
                        ifModifiedSince, status, dateFrom, dateTo, order, null);
                messages.add("Get Purchase orders - Count : " + allPurchaseOrders.getPurchaseOrders().size());

                // GET one Purchase Order
                UUID purchaseOrderID = allPurchaseOrders.getPurchaseOrders().get(0).getPurchaseOrderID();
                PurchaseOrders onePurchaseOrder = accountingApi.getPurchaseOrder(accessToken, xeroTenantId,
                        purchaseOrderID);
                messages.add(
                        "Get one Purchase order - Total : " + onePurchaseOrder.getPurchaseOrders().get(0).getTotal());
                
                // GET one Purchase Order BY NUMBER
                String purchaseOrderNum = allPurchaseOrders.getPurchaseOrders().get(0).getPurchaseOrderNumber();
                PurchaseOrders onePurchaseOrderByNum = accountingApi.getPurchaseOrderByNumber(accessToken, xeroTenantId, purchaseOrderNum);
                messages.add("Get one Purchase order BY NUMBER - Total : " + onePurchaseOrderByNum.getPurchaseOrders().get(0).getTotal());

                // DELETE Purchase Orders
                createdPurchaseOrders.getPurchaseOrders().get(0)
                .setStatus(com.xero.models.accounting.PurchaseOrder.StatusEnum.DELETED);
                PurchaseOrders deletePurchaseOrders = accountingApi.updatePurchaseOrder(accessToken, xeroTenantId,
                        newPurchaseOrderID, createdPurchaseOrders);
                messages.add("Delete Purchase order - Status : "
                        + deletePurchaseOrders.getPurchaseOrders().get(0).getStatus());

                // Get History
                HistoryRecords history = accountingApi.getPurchaseOrderHistory(accessToken, xeroTenantId, purchaseOrderID);
                messages.add("History - count : " + history.getHistoryRecords().size());

                // Create History
                HistoryRecords newHistoryRecords = new HistoryRecords();
                HistoryRecord newHistoryRecord = new HistoryRecord();
                newHistoryRecord.setDetails("Hello World");
                newHistoryRecords.addHistoryRecordsItem(newHistoryRecord);
                HistoryRecords newHistory = accountingApi.createPurchaseOrderHistory(accessToken, xeroTenantId,
                        purchaseOrderID, newHistoryRecords);
                messages.add("History - note added to  : " + newHistory.getHistoryRecords().get(0).getDetails());
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        
        } else if (object.equals("Quotes")) {
            // QUOTES
            try {
                LocalDate dateFrom = null;
                LocalDate dateTo = null;
                LocalDate expiryDateFrom = null;
                LocalDate expiryDateTo = null;
                UUID contactID = null;
                String status = null;
                int page = 1;
                
                where = "Type==\"REVENUE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                String accountCodeForInvoice = accounts.getAccounts().get(0).getCode();
                
                Contacts useContact = accountingApi.getContacts(accessToken, xeroTenantId, null, null,null,null,null,null);
                Contact contact = new Contact();
                contact.setContactID(useContact.getContacts().get(0).getContactID());
                
                Quotes quotes = new Quotes();
                Quote quote = new Quote();
                quote.contact(contact);
                LocalDate currDate = LocalDate.now();
                quote.setDate(currDate);
                
                LineItem li = new LineItem();
                li.setAccountCode(accountCodeForInvoice);
                li.setDescription("Foobar");
                li.setQuantity(1.0123);
                li.setUnitAmount(20.0033);
                quote.addLineItemsItem(li);
                quotes.addQuotesItem(quote);

                Quotes createdQuotes = accountingApi.createQuotes(accessToken, xeroTenantId, quotes, summarizeErrors);
                messages.add("Created Quote - ID : " + createdQuotes.getQuotes().get(0).getQuoteID());
                
                Quotes updatedOrCreatedQuotes = accountingApi.updateOrCreateQuotes(accessToken, xeroTenantId, quotes, summarizeErrors);
                messages.add("update OR create Quotes - Count : " + updatedOrCreatedQuotes.getQuotes().size());
              
                Quotes allQuotes = accountingApi.getQuotes(savedAccessToken, xeroTenantId, ifModifiedSince, dateFrom, dateTo, expiryDateFrom, expiryDateTo, contactID, status, page, order);
                messages.add("Get Quotes - Count : " + allQuotes.getQuotes().size());
                UUID quoteID = allQuotes.getQuotes().get(0).getQuoteID();
                
                Quotes readQuotes = accountingApi.getQuote(accessToken, xeroTenantId, quoteID);
                messages.add("Get single Quotes - Count : " + readQuotes.getQuotes().size());                
             
                Quotes upQuotes = new Quotes();
                Quote upQuote = new Quote();
                upQuote.setReference("I am an update");
                upQuote.contact(contact);
                upQuote.setDate(currDate);
                upQuotes.addQuotesItem(upQuote);
                Quotes updateQuotes = accountingApi.updateQuote(accessToken, xeroTenantId, quoteID, upQuotes);
                messages.add("Updated Quote - ID : " + updateQuotes.getQuotes().get(0).getQuoteID());
                                
                // Get History
                HistoryRecords history = accountingApi.getQuoteHistory(accessToken, xeroTenantId, quoteID);
                messages.add("History - count : " + history.getHistoryRecords().size());

                // Create History
                HistoryRecords newHistoryRecords = new HistoryRecords();
                HistoryRecord newHistoryRecord = new HistoryRecord();
                newHistoryRecord.setDetails("Hello World");
                newHistoryRecords.addHistoryRecordsItem(newHistoryRecord);
                HistoryRecords newHistory = accountingApi.createQuoteHistory(accessToken, xeroTenantId, quoteID, newHistoryRecords);
                messages.add("History - note added to  : " + newHistory.getHistoryRecords().get(0).getDetails());     
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        
        } else if (object.equals("Receipts")) {
            /* RECEIPTS */
            try {
                // Create
                where = "IsSubscriber==true";
                Users users = accountingApi.getUsers(accessToken, xeroTenantId, ifModifiedSince, where, order);
                where = null;

                where = "ShowInExpenseClaims==true && Status==\"ACTIVE\"";
                Accounts accounts = accountingApi.getAccounts(accessToken, xeroTenantId, ifModifiedSince, where, order);
                where = null;
                User useUser = new User();

                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order,contactIds, null, includeArchived);
                Contact useContact = new Contact();
                
                if (accounts.getAccounts().size() > 0 && users.getUsers().size() > 0 && contacts.getContacts().size() > 0) {
                    useUser.setUserID(users.getUsers().get(0).getUserID());
    
                    useContact.setContactID(contacts.getContacts().get(0).getContactID());
    
                    // CREATE NEW RECEIPT
                    Receipts receipts = new Receipts();
                    Receipt receipt = new Receipt();
    
                    LineItem li = new LineItem();
                    li.setAccountCode(accounts.getAccounts().get(0).getCode());
                    li.setDescription("Foobar");
                    li.setQuantity(2.0909);
                    li.setUnitAmount(20.0099);
                    li.setLineAmount(40.00);
                    li.setTaxType("NONE");
    
                    receipt.addLineItemsItem(li);
                    receipt.setUser(useUser);
                    receipt.lineAmountTypes(LineAmountTypes.NOTAX);
                    receipt.contact(useContact);
                    receipt.setStatus(com.xero.models.accounting.Receipt.StatusEnum.DRAFT);
                    receipts.addReceiptsItem(receipt);
                    Receipts newReceipts = accountingApi.createReceipt(accessToken, xeroTenantId, receipts,unitdp);
                    messages.add("Create Receipts - Total : " + newReceipts.getReceipts().get(0).getTotal());
    
                    // UPDATE Receipts
                    UUID newReceiptId = newReceipts.getReceipts().get(0).getReceiptID();
                    newReceipts.getReceipts().get(0).setReference("Foobar");
                    Receipts updateReceipts = accountingApi.updateReceipt(accessToken, xeroTenantId, newReceiptId,
                            newReceipts,unitdp);
                    messages.add("Create Receipts - Ref : " + updateReceipts.getReceipts().get(0).getReference());
    
                    // GET all Receipts
                    Receipts allReceipts = accountingApi.getReceipts(accessToken, xeroTenantId, ifModifiedSince, where,
                            order, null);
                    messages.add("Create Receipts - Count : " + allReceipts.getReceipts().size());
    
                    // GET one Receipts
                    UUID receiptID = allReceipts.getReceipts().get(0).getReceiptID();
                    Receipts oneReceipts = accountingApi.getReceipt(accessToken, xeroTenantId, receiptID,unitdp);
                    messages.add("Create Receipts - Total : " + oneReceipts.getReceipts().get(0).getTotal());
    
                    // Get History
                    HistoryRecords history = accountingApi.getReceiptHistory(accessToken, xeroTenantId, receiptID);
                    messages.add("History - count : " + history.getHistoryRecords().size());
    
                    // Create History
                    // Error: "The document with the supplied id was not found for this endpoint.
                    /*
                     * HistoryRecords newHistoryRecords = new HistoryRecords(); HistoryRecord
                     * newHistoryRecord = new HistoryRecord();
                     * newHistoryRecord.setDetails("Hello World");
                     * newHistoryRecords.addHistoryRecordsItem(newHistoryRecord); HistoryRecords
                     * newHistory = accountingApi.createReceiptHistory(receiptID,
                     * newHistoryRecords); messages.add("History - note added to  : " +
                     * newHistory.getHistoryRecords().get(0).getDetails());
                     */
                } else {
                    messages.add("Receipts can't be created - need account, user and contact"); 
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("RepeatingInvoices")) {
            /* REPEATING INVOICE */
            try {
                // GET all Repeating Invoices
                RepeatingInvoices repeatingInvoices = accountingApi.getRepeatingInvoices(accessToken, xeroTenantId,
                        where, order);
                if (repeatingInvoices.getRepeatingInvoices().size() > 0) {
                    messages.add("Repeating Invoice - count : " + repeatingInvoices.getRepeatingInvoices().size());

                    // GET one Repeating Invoices
                    UUID repeatingInvoiceID = repeatingInvoices.getRepeatingInvoices().get(0).getRepeatingInvoiceID();
                    RepeatingInvoices repeatingInvoice = accountingApi.getRepeatingInvoice(accessToken, xeroTenantId,
                            repeatingInvoiceID);
                    messages.add(
                            "Repeating Invoice - total : " + repeatingInvoice.getRepeatingInvoices().get(0).getTotal());

                    // Get History
                    HistoryRecords history = accountingApi.getRepeatingInvoiceHistory(accessToken, xeroTenantId,
                            repeatingInvoiceID);
                    messages.add("History - count : " + history.getHistoryRecords().size());

                    // Create History
                    // Error: "The document with the supplied id was not found for this endpoint.
                    /*
                     * HistoryRecords newHistoryRecords = new HistoryRecords(); HistoryRecord
                     * newHistoryRecord = new HistoryRecord();
                     * newHistoryRecord.setDetails("Hello World");
                     * newHistoryRecords.addHistoryRecordsItem(newHistoryRecord); HistoryRecords
                     * newHistory = accountingApi.createRepeatingInvoiceHistory(repeatingInvoiceID,
                     * newHistoryRecords); messages.add("History - note added to  : " +
                     * newHistory.getHistoryRecords().get(0).getDetails());
                     */
                } else {
                    messages.add("Zero repeating Invoices found");

                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Reports")) {

            /* REPORTS */
            try {

                // TenNinetyNine - US Only String reportYear = null; Reports reports =
                //accountingApi.getReportTenNinetyNine(accessToken, xeroTenantId, "2019");
                
                // AgedPayablesByContact
                String date = null;
                String fromDate = null;
                String toDate = null;
                String profitLossTimeframe = null;
                String trackingOptionID1 = null;
                String trackingOptionID2 = null;
                boolean standardLayout = false;
                boolean paymentsOnly = false;
                String trackingCategoryID = null;
                String trackingCategoryID2 = null;
                String trackingOptionID = null;

                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order,contactIds, null, includeArchived);
                UUID contactId = contacts.getContacts().get(0).getContactID();
                LocalDate xDate = LocalDate.now();
                LocalDate xFromDate = LocalDate.now();
                LocalDate xToDate = LocalDate.now();
/*
                ReportWithRows reportAgedPayablesByContact = accountingApi.getReportAgedPayablesByContact(accessToken,
                        xeroTenantId, contactId, xDate, xFromDate, xToDate);
                messages.add("Get a Reports - Name:" + reportAgedPayablesByContact.getReports().get(0).getReportName());

                // AgedReceivablesByContact
                ReportWithRows reportAgedReceivablesByContact = accountingApi.getReportAgedReceivablesByContact(
                        accessToken, xeroTenantId, contactId, xDate, xFromDate, xToDate);
                messages.add(
                        "Get a Reports - Name:" + reportAgedReceivablesByContact.getReports().get(0).getReportName());

                // reportBalanceSheet
                ReportWithRows reportBalanceSheet = accountingApi.getReportBalanceSheet(accessToken, xeroTenantId,
                        toDate, 3, "MONTH", trackingOptionID1, trackingOptionID2, standardLayout, paymentsOnly);
                messages.add("Get a Reports - Name:" + reportBalanceSheet.getReports().get(0).getReportName());

                // reportBankSummary
                ReportWithRows reportBankSummary = accountingApi.getReportBankSummary(accessToken, xeroTenantId,
                        xFromDate, xToDate);
                messages.add("Get a Reports - Name:" + reportBankSummary.getReports().get(0).getReportName());
*/
                // reportBASorGSTlist - AU and NZ only
                ReportWithRows reportTax = accountingApi.getReportBASorGSTList(accessToken, xeroTenantId);
                System.out.println(reportTax.toString());
                // reportBudgetSummary
                int budgetPeriod = 1;
                int budgetTimeframe = 3;
                ReportWithRows reportBudgetSummary = accountingApi.getReportBudgetSummary(accessToken, xeroTenantId,
                        xToDate, budgetPeriod, budgetTimeframe);
                messages.add("Get a Reports - Name:" + reportBudgetSummary.getReports().get(0).getReportName());

                // reportExecutiveSummary
                ReportWithRows reportExecutiveSummary = accountingApi.getReportExecutiveSummary(accessToken,
                        xeroTenantId, xToDate);
                messages.add("Get a Reports - Name:" + reportExecutiveSummary.getReports().get(0).getReportName());

                // reportProfitandLoss
                fromDate = "2018-01-01";
                toDate = "2018-12-31";
                profitLossTimeframe = "MONTH";
                standardLayout = true;
                paymentsOnly = false;
                ReportWithRows reportProfitLoss = accountingApi.getReportProfitAndLoss(accessToken, xeroTenantId,
                        xFromDate, xToDate, null, profitLossTimeframe, trackingCategoryID, trackingCategoryID2,
                        trackingOptionID, trackingOptionID2, standardLayout, paymentsOnly);
                messages.add("Get a Reports - Name:" + reportProfitLoss.getReports().get(0).getReportName());
                fromDate = null;
                toDate = null;

                // reportTrialBalance
                ReportWithRows reportTrialBalance = accountingApi.getReportTrialBalance(accessToken, xeroTenantId,
                        xToDate, paymentsOnly);
                messages.add("Get a Reports - Name:" + reportTrialBalance.getReports().get(0).getReportName());
           
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("TrackingCategories")) {
            // TRACKING CATEGORIES
            try {
                // GET Tracking Categories
                TrackingCategories trackingCategories = accountingApi.getTrackingCategories(accessToken, xeroTenantId,
                        where, order, includeArchived);
                int count = trackingCategories.getTrackingCategories().size();

                if (count == 2) {
                    // DELETE Tracking Categories
                    UUID trackingCategoryID = trackingCategories.getTrackingCategories().get(0).getTrackingCategoryID();
                    TrackingCategories deletedTrackingCategories = accountingApi.deleteTrackingCategory(accessToken,
                            xeroTenantId, trackingCategoryID);
                    messages.add("DELETED tracking categories - status : "
                            + deletedTrackingCategories.getTrackingCategories().get(0).getStatus());
                }

                // CREATE Tracking Categories
                TrackingCategory newTrackingCategory = new TrackingCategory();
                newTrackingCategory.setName("Foo" + loadRandomNum());
                TrackingCategories createdTrackingCategories = accountingApi.createTrackingCategory(accessToken,
                        xeroTenantId, newTrackingCategory);
                messages.add("CREATED tracking categories - name : "
                        + createdTrackingCategories.getTrackingCategories().get(0).getName());

                // UPDATE Tracking Categories
                UUID newTrackingCategoryID = createdTrackingCategories.getTrackingCategories().get(0)
                        .getTrackingCategoryID();
                newTrackingCategory.setName("Foo" + loadRandomNum());
                TrackingCategories updatedTrackingCategories = accountingApi.updateTrackingCategory(accessToken,
                        xeroTenantId, newTrackingCategoryID, newTrackingCategory);
                messages.add("UPDATED tracking categories - name : "
                        + updatedTrackingCategories.getTrackingCategories().get(0).getName());

                // GET one Tracking Categories
                if (count > 0) {
                    UUID oneTrackingCategoryID = trackingCategories.getTrackingCategories().get(0)
                            .getTrackingCategoryID();
                    TrackingCategories oneTrackingCategories = accountingApi.getTrackingCategory(accessToken,
                            xeroTenantId, oneTrackingCategoryID);
                    messages.add("GET ONE tracking categories - name : "
                            + oneTrackingCategories.getTrackingCategories().get(0).getName());

                    // Create one Option
                    TrackingOption option = new TrackingOption();
                    option.setName("Bar" + loadRandomNum());
                    TrackingOptions newTrackingOptions = accountingApi.createTrackingOptions(accessToken, xeroTenantId,
                            oneTrackingCategoryID, option);
                    messages.add("CREATE option - name : " + newTrackingOptions.getOptions().get(0).getName());

                    // DELETE All options
                    // UUID newOptionId =
                    // newTrackingOptions.getOptions().get(0).getTrackingOptionID();
                    // TrackingOptions deleteOptions =
                    // accountingApi.deleteTrackingOptions(oneTrackingCategoryID, newOptionId);
                    // messages.add("DELETE one option - Status : " +
                    // deleteOptions.getOptions().get(0).getStatus());
                }
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
          
        } else if (object.equals("TaxRates")) {
            // TAX RATE
            try {
                // CREATE Tax Rate
                TaxRates newTaxRates = new TaxRates();
                TaxRate newTaxRate = new TaxRate();
                TaxComponent rate01 = new TaxComponent();
                rate01.setName("State Tax");
                rate01.setRate(2.25);
                newTaxRate.setName("SDKTax" + loadRandomNum());
                newTaxRate.addTaxComponentsItem(rate01);
                newTaxRates.addTaxRatesItem(newTaxRate);

                TaxRates createdTaxRate = accountingApi.createTaxRates(accessToken, xeroTenantId, newTaxRates);
                messages.add("CREATE TaxRate - name : " + createdTaxRate.getTaxRates().get(0).getName());

                // UDPATE Tax Rate
                newTaxRates.getTaxRates().get(0).setStatus(com.xero.models.accounting.TaxRate.StatusEnum.DELETED);
                TaxRates updatedTaxRate = accountingApi.updateTaxRate(accessToken, xeroTenantId, newTaxRates);
                messages.add("UPDATED TaxRate - status : " + updatedTaxRate.getTaxRates().get(0).getStatus());

                // GET Tax Rate
                String taxType = null;
                TaxRates taxRates = accountingApi.getTaxRates(accessToken, xeroTenantId, where, order, taxType);
                messages.add("GET TaxRate - cnt : " + taxRates.getTaxRates().size());

                // GET Tax Rate
                taxType = "CAPEXINPUT2";
                TaxRates taxRatesByType = accountingApi.getTaxRates(accessToken, xeroTenantId, where, order, taxType);
                messages.add("GET TaxRate by Cap Purchase Type : " + taxRatesByType.getTaxRates().size());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Users")) {
            // USER
            try {
                // GET Users
                Users users = accountingApi.getUsers(accessToken, xeroTenantId, ifModifiedSince, where, order);
                messages.add("GET Users - cnt : " + users.getUsers().size());

                // GET One User
                UUID userID = users.getUsers().get(0).getUserID();
                Users user = accountingApi.getUser(accessToken, xeroTenantId, userID);
                messages.add("GET Users - First Name : " + user.getUsers().get(0).getFirstName());
            
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroMethodNotAllowedException e) {
                this.addMethodNotAllowedException(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
        } else if (object.equals("Errors")) {

            try {
                Contact contact = new Contact();
                contact.setName("Sidney Maestre");
                Contacts contacts = new Contacts();
                contacts.addContactsItem(contact);
                Contacts createContact1 = accountingApi.createContacts(accessToken, xeroTenantId, contacts, false);
                Contacts createContact2 = accountingApi.createContacts(accessToken, xeroTenantId, contacts, false);
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                Contacts contacts = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order, contactIds, null, includeArchived);
                Contact useContact = new Contact();
                useContact.setContactID(contacts.getContacts().get(0).getContactID());

                Invoice myInvoice = new Invoice();

                LineItem li = new LineItem();
                li.setAccountCode("123456789");
                li.setDescription("Acme Tires");
                li.setQuantity(2.0909);
                li.setUnitAmount(20.0088);
                li.setLineAmount(40.00);
                li.setTaxType("NONE");

                myInvoice.addLineItemsItem(li);
                myInvoice.setContact(useContact);
                myInvoice.setType(com.xero.models.accounting.Invoice.TypeEnum.ACCREC);
                myInvoice.setReference("One Fish, Two Fish");
                myInvoice.setStatus(com.xero.models.accounting.Invoice.StatusEnum.SUBMITTED);
                Invoices myInvoices = new Invoices();
                myInvoices.addInvoicesItem(myInvoice);
                Invoices newInvoice = accountingApi.createInvoices(accessToken, xeroTenantId, myInvoices, false,unitdp);
                messages.add("Create invoice - Reference : " + newInvoice.getInvoices().get(0).getReference());
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {
                UUID badContactId = UUID.fromString("bd2270c3-0000-4c11-9cfb-000b551c3f51");
                Contacts badContacts = accountingApi.getContact(accessToken, xeroTenantId, badContactId);
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            
            Contacts ContactList = accountingApi.getContacts(accessToken, xeroTenantId, ifModifiedSince, where, order,contactIds, null, includeArchived);
            int num4 = findRandomNum(ContactList.getContacts().size());
            UUID contactId = ContactList.getContacts().get(num4).getContactID();
            try {
                for (int i = 80; i > 1; i--) {
                    Contacts allMyContacts = accountingApi.getContact(accessToken, xeroTenantId, contactId);
                }
                messages.add("Congrats - you made over 60 calls without hitting rate limit");
            } catch (XeroBadRequestException e) {
                this.addBadRequest(e, messages); 
            } catch (XeroForbiddenException e) {
                this.addError(e, messages); 
            } catch (XeroNotFoundException e) {
                this.addError(e, messages); 
            } catch (XeroUnauthorizedException e) {
                this.addError(e, messages); 
            } catch (XeroRateLimitException e) {
                this.addError(e, messages); 
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        for (int i = 0; i < messages.size(); i++) {
            System.out.println(messages.get(i));
            respWriter.println("<p>" + messages.get(i) + "</p>");
        }

        respWriter.println("<hr>end processing request<hr><div class=\"form-group\">");
        respWriter.println("</div></div>");
    }

    protected void addToMapIfNotNull(Map<String, String> map, String key, Object value) {
        if (value != null) {
            map.put(key, value.toString());
        }
    }
    
    protected void addError(XeroForbiddenException e, ArrayList<String> messages) {
        if (e.getMessage() != null) {
           messages.add("Xero Exception: " + e.getStatusCode() );
           messages.add("Error Msg: " + e.getMessage());
           
        } else {
            messages.add("Error status: " + e.getStatusCode());
        }
    }
    
    protected void addError(XeroNotFoundException e, ArrayList<String> messages) {
        if (e.getMessage() != null) {
           messages.add("Xero Exception: " + e.getStatusCode() );
           messages.add("Error Msg: " + e.getMessage());
           
        } else {
            messages.add("Error status: " + e.getStatusCode());
        }
    }
    
    protected void addError(XeroUnauthorizedException e, ArrayList<String> messages) {
        if (e.getMessage() != null) {
           messages.add("Xero Exception: " + e.getStatusCode() );
           messages.add("Error Msg: " + e.getMessage());
           
        } else {
            messages.add("Error status: " + e.getStatusCode());
        }
    }
    
    protected void addError(XeroServerErrorException e, ArrayList<String> messages) {
        if (e.getMessage() != null) {
           messages.add("Xero Exception: " + e.getStatusCode() );
           messages.add("Error Msg: " + e.getMessage());
           
        } else {
            messages.add("Error status: " + e.getStatusCode());
        }
    }
    
    protected void addError(XeroRateLimitException e, ArrayList<String> messages) {
        if (e.getMessage() != null) {
           messages.add("Xero Exception: " + e.getStatusCode() );
           messages.add("Error Msg: " + e.getMessage());
           
        } else {
            messages.add("Error status: " + e.getStatusCode());
        }
    }
    
    protected void addMethodNotAllowedException(XeroMethodNotAllowedException e, ArrayList<String> messages) {
        if (e.getPayrollUkProblem()  != null) {
            messages.add("Xero Exception: " + e.getStatusCode());
            messages.add("Problem title: " + e.getPayrollUkProblem().getTitle());
            messages.add("Problem detail: " + e.getPayrollUkProblem().getDetail());
            if (e.getPayrollUkProblem().getInvalidFields() != null && e.getPayrollUkProblem().getInvalidFields().size() > 0) {
                for (com.xero.models.payrolluk.InvalidField field : e.getPayrollUkProblem().getInvalidFields()) {
                    messages.add("Invalid Field name: " + field.getName());
                    messages.add("Invalid Field reason: " + field.getReason());
                }
            }
       
        }
    }
    
    protected void addBadRequest(XeroBadRequestException e, ArrayList<String> messages) {
         if (e.getElements() != null && e.getElements().size() > 0) {
            messages.add("Xero Exception: " + e.getStatusCode());
            for (Element item : e.getElements()) {
                 for (ValidationError err : item.getValidationErrors()) {
                     messages.add("Accounting Validation Error Msg: " + err.getMessage());
                 }
             }
            
         } else if (e.getFieldValidationErrorsElements()  != null && e.getFieldValidationErrorsElements().size() > 0) {
             messages.add("Xero Exception: " + e.getStatusCode());
             for (FieldValidationErrorsElement ele : e.getFieldValidationErrorsElements()) {
                 messages.add("Asset Field Validation Error Msg: " + ele.getDetail());
             }
             
         } else if (e.getStatementItems()  != null && e.getStatementItems().size() > 0) {
             messages.add("Xero Exception: " + e.getStatusCode());
             for (Statement statement : e.getStatementItems()) {
                 messages.add("Bank Feed - Statement Msg: " + statement.getFeedConnectionId());
                 for (com.xero.models.bankfeeds.Error statementError : statement.getErrors()) {
                     messages.add("Bank Feed - Statement Error Msg: " + statementError.getDetail());
                 }
             }
             
         } else if (e.getEmployeeItems() != null && e.getEmployeeItems().size() > 0) {             
             messages.add("Xero Exception: " + e.getStatusCode());
             for (com.xero.models.payrollau.Employee emp : e.getEmployeeItems()) {
                  for (com.xero.models.payrollau.ValidationError err : emp.getValidationErrors()) {
                      messages.add("Payroll AU Employee Validation Error Msg: " + err.getMessage());
                  }
              }
             
         } else if (e.getPayrollCalendarItems() != null && e.getPayrollCalendarItems().size() > 0) {             
             messages.add("Xero Exception: " + e.getStatusCode());
             for (com.xero.models.payrollau.PayrollCalendar item : e.getPayrollCalendarItems()) {
                  for (com.xero.models.payrollau.ValidationError err : item.getValidationErrors()) {
                      messages.add("Payroll AU Payroll Calendar Validation Error Msg: " + err.getMessage());
                  }
              }
             
         } else if (e.getPayRunItems() != null && e.getPayRunItems().size() > 0) {             
             messages.add("Xero Exception: " + e.getStatusCode());
             for (com.xero.models.payrollau.PayRun item : e.getPayRunItems()) {
                  for (com.xero.models.payrollau.ValidationError err : item.getValidationErrors()) {
                      messages.add("Payroll AU Payroll Calendar Validation Error Msg: " + err.getMessage());
                  }
              }
         
         } else if (e.getSuperFundItems() != null && e.getSuperFundItems().size() > 0) {             
             messages.add("Xero Exception: " + e.getStatusCode());
             for (com.xero.models.payrollau.SuperFund item : e.getSuperFundItems()) {
                  for (com.xero.models.payrollau.ValidationError err : item.getValidationErrors()) {
                      messages.add("Payroll AU SuperFund Validation Error Msg: " + err.getMessage());
                  }
              }
             
         } else if (e.getTimesheetItems() != null && e.getTimesheetItems().size() > 0) {             
             messages.add("Xero Exception: " + e.getStatusCode());
             for (com.xero.models.payrollau.Timesheet item : e.getTimesheetItems()) {
                  for (com.xero.models.payrollau.ValidationError err : item.getValidationErrors()) {
                      messages.add("Payroll AU Timesheet Validation Error Msg: " + err.getMessage());
                  }
              }

         } else if (e.getPayrollUkProblem() != null && ((e.getPayrollUkProblem().getDetail() != null && e.getPayrollUkProblem().getTitle() != null) || (e.getPayrollUkProblem().getInvalidFields() != null && e.getPayrollUkProblem().getInvalidFields().size() > 0)) ) {
             messages.add("Xero Exception: " + e.getStatusCode());
             messages.add("Problem title: " + e.getPayrollUkProblem().getTitle());
             messages.add("Problem detail: " + e.getPayrollUkProblem().getDetail());
             if (e.getPayrollUkProblem().getInvalidFields() != null && e.getPayrollUkProblem().getInvalidFields().size() > 0) {
                 for (com.xero.models.payrolluk.InvalidField field : e.getPayrollUkProblem().getInvalidFields()) {
                     messages.add("Invalid Field name: " + field.getName());
                     messages.add("Invalid Field reason: " + field.getReason());
                 }
             }
             
         } else {
             messages.add("Error Msg: " + e.getMessage());
         }
    }
    
    protected String saveFile(ByteArrayInputStream input, String fileName) {
        String saveFilePath = null;
        File f = new File("./");
        String dirPath;
        try {
            dirPath = f.getCanonicalPath();

            FileOutputStream output = new FileOutputStream(fileName);

            int DEFAULT_BUFFER_SIZE = 1024;
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int n = 0;
            n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
            while (n >= 0) {
                output.write(buffer, 0, n);
                n = input.read(buffer, 0, DEFAULT_BUFFER_SIZE);
            }
            input.close();
            output.close();

            saveFilePath = dirPath + File.separator + fileName;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return saveFilePath;
    }

    public static int loadRandomNum() {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(100000);
        return randomInt;
    }

    public static int findRandomNum(int total) {
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(total);
        return randomInt;
    }
    
    public static String loadRandChar() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
     
        String generatedString = random.ints(leftLimit, rightLimit + 1)
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
     
        return generatedString;
    }
}
