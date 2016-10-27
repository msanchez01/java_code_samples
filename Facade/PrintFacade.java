package com.spfsolutions.ioms.facades;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.j256.ormlite.dao.ForeignCollection;
import com.spfsolutions.ioms.common.ESCPrinter;
import com.spfsolutions.ioms.common.EmailPrinter;
import com.spfsolutions.ioms.common.PSPrinter;
import com.spfsolutions.ioms.common.Printer;
import com.spfsolutions.ioms.data.OrderErrorEntity;
import com.spfsolutions.ioms.data.OrderedItemEntity;
import com.spfsolutions.ioms.data.PaymentTypeEntity;



// Libraries added for error logging
import java.lang.Process;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Date;

@Component
public class PrintFacade {
    
	private static final Logger log = Logger.getLogger(PrintFacade.class);   
    
    @Value("${autoprintsequence}")
    String autoPrintSequence;
    
    @Value("${tabletprintsequence}")
    String tabletPrintSequence;
	
	@Value("${db.schema}")
    private String schemaName;
	
    @Value("${orderreadyfiles.path}")
    private String orderReadyBoardFileDirectory;
    
    @Value("${printermachineipaddress}")
    private String printerMachineIpAddress;
    
    @Value("${printermachinedirectory}")
    private String printerMachineDirectory;
    
    @Value("${printermachineusername}")
    private String printerMachineUserName;
    
    @Value("${printing.method}")
    private String printingMethod;
    
    @Value("${printing.command}")
	private String printingCommand;
    
    @Value("${printing.gratitude}")
    private String printingGratitude;
    
    @Value("${printermachinepassword}")
    private String printerMachinePassword;
    
    @Value("${printermachineworkgroup}")
    private String printerMachineWorkGroup;
    
    @Value("${printing.emailaddress}")
    private String emailReceiptDistributionAddress;
    
    private String printer;
    
    private FileOutputStream ostream;
    private PrintStream pstream;
    private boolean streamOpenSuccess;
    
    private static final char ESC = 27; //escape
    private static final char AT = 64; //@
    
    private Printer printJob;
        
    public PrintFacade()
    {
        if(SystemUtils.IS_OS_WINDOWS)
        {
            PrintService ps = PrintServiceLookup.lookupDefaultPrintService();
            String defaultPrinter = ps.getName();
            this.printer = defaultPrinter;// + "\\";
        }
    }
    
    public void close() {
        //post: closes the stream, used when printjob ended
        try {
            pstream.close();
            ostream.close();
        } 
        catch (IOException e) { e.printStackTrace(); }
    }
    
    public boolean initialize() throws FileNotFoundException {
        //post: returns true iff stream to network printer successfully opened, streams for writing to esc/p printer created
        streamOpenSuccess = false;
        
        try {
            //create stream objs
            if(SystemUtils.IS_OS_WINDOWS)
            {               
                File f = new File(printer);
        		log.info(f.isFile());
        		log.info(f.isDirectory());
        		log.info(f.getName());
        		log.info(f.getAbsolutePath());
                
                ostream = new FileOutputStream(f);
                pstream = new PrintStream(ostream);
                
                //reset default settings
                pstream.print(ESC);
                pstream.print(AT);
            }
            
        } 
        catch (FileNotFoundException e) {throw e; }
        
        return streamOpenSuccess;
    } 
    
    public String[] getAutoPrintSequence(){
        String[] sequence = autoPrintSequence.split(",");
        return sequence;
    }
    
    public String[] getTabletPrintSequence(){
        String[] sequence = tabletPrintSequence.split(",");
        return sequence;
    }
    
    public void printMealOrderKitchenReceipt(final int orderId,
            final DateTime orderDate, final DateTime pickupDate,
            final ForeignCollection<OrderedItemEntity> orderedItems,
            final ForeignCollection<OrderErrorEntity> orderErrors,
            final String deliveryInformation, final String contactPhoneNumber,
            final String contactName, final PaymentTypeEntity paymentTypeEntity, final String distributionLabel 
            )
    {        
        final List<OrderedItemEntity> orderedItemsList  =  new ArrayList<OrderedItemEntity>(orderedItems);
        final List<OrderErrorEntity> orderErrorsList  =  new ArrayList<OrderErrorEntity>(orderErrors);
        Runnable task = new Runnable() {

            @Override 
            public void run() { 
                try { 
                    printMealOrderKitchenReceiptAsync(orderId, orderDate, pickupDate, orderedItemsList, orderErrorsList, deliveryInformation, contactPhoneNumber, contactName, paymentTypeEntity, distributionLabel);  
                } catch (Exception ex) { 
                    ex.printStackTrace();
                } 
            } 
        }; 
        new Thread(task, "OrderKitchenPrint").start();
    }
    
    public void printMealOrderCustomerReceipt(final String restaurantName, 
            final String streetAddress,
            final String city, 
            final String state, 
            final String zip, 
            final String phone,
            final int orderId, 
            final BigDecimal subTotal,
            final BigDecimal tax,
            final BigDecimal deliveryFee,
            final BigDecimal tip,
            final BigDecimal totalAmount,
            final PaymentTypeEntity paymentTypeEntity,
            final String paymentType,
            final String cardType,
            final String nameOnCard,
            final String accountNumber,
            final String authCode,
            final String result,
            final DateTime orderDate, 
            final DateTime pickupDate, 
            final ForeignCollection<OrderedItemEntity> orderedItems, 
            final ForeignCollection<OrderErrorEntity> orderErrors,
            final String deliveryInformation, 
            final String contactPhoneNumber,
            final String contactName,
            final String distributionLabel){
        
        final List<OrderedItemEntity> orderedItemsList  =  new ArrayList<OrderedItemEntity>(orderedItems);
        final List<OrderErrorEntity> orderErrorsList  =  new ArrayList<OrderErrorEntity>(orderErrors);
        Runnable task = new Runnable() {

            @Override 
            public void run() { 
                try { 
                    
                    printMealOrderCustomerReceiptAsync(restaurantName, streetAddress, city, state, zip, phone, orderId, subTotal, tax, deliveryFee, tip, totalAmount, paymentTypeEntity, paymentType, cardType, nameOnCard, accountNumber, authCode, result, orderDate, pickupDate, orderedItemsList, orderErrorsList, deliveryInformation, contactPhoneNumber, contactName, distributionLabel);
                } catch (Exception ex) { 
                    ex.printStackTrace();
                } 
            } 
        }; 
        new Thread(task, "OrderKitchenPrint").start();
    }
    
    private void printMealOrderCustomerReceiptAsync(String restaurantName, 
            String streetAddress,
            String city, 
            String state, 
            String zip, 
            String phone,
            //BigDecimal taxRate,
            int orderId, 
            BigDecimal subTotal,            
            BigDecimal tax,
            BigDecimal deliveryFee,
            BigDecimal tip,
            BigDecimal totalAmount,
            PaymentTypeEntity paymentTypeEntity,
            String paymentType,
            String cardType,
             String nameOnCard,
             String accountNumber,
             String authCode,
            String result,
            DateTime orderDate, 
            DateTime pickupDate, 
            List<OrderedItemEntity> orderedItems, 
            List<OrderErrorEntity> orderErrors,            
            final String deliveryInformation, final String contactPhoneNumber,
            final String contactName, final String distributionLabel){
        if(printingMethod.contains("STAR-ESC")){
            printJob = new ESCPrinter();    
        }
        else if(printingMethod.contains("LP-PS")){
            printJob = new PSPrinter(printingGratitude);
        }
        else if(printingMethod.contains("EMAIL")){
            printJob = new EmailPrinter();
        }
        String finalString = printJob.printMealOrderCustomerReceipt(restaurantName, streetAddress, city, state, zip, phone, orderId, subTotal, tax, deliveryFee, tip, totalAmount, paymentTypeEntity, paymentType, cardType, nameOnCard, accountNumber, authCode, result, orderDate, pickupDate, orderedItems, orderErrors, deliveryInformation, contactPhoneNumber, contactName, distributionLabel);
        this.print(finalString, orderId, "Customer_Receipt");
    }
    private void printMealOrderKitchenReceiptAsync(int orderId, DateTime orderDate, DateTime pickupDate, List<OrderedItemEntity> orderedItems, List<OrderErrorEntity> orderErrors, String deliveryInformation, String contactPhoneNumber, String contactName, PaymentTypeEntity paymentTypeEntity, String distributionLabel)    
    {
        if(printingMethod.equalsIgnoreCase("STAR-ESC")){
            printJob = new ESCPrinter();    
        }
        else if(printingMethod.equalsIgnoreCase("LP-PS")){
            printJob = new PSPrinter(printingGratitude);
        }
        else if(printingMethod.equalsIgnoreCase("EMAIL")){
            printJob = new EmailPrinter();
        }
        String finalString = printJob.printMealOrderKitchenReceipt(orderId, orderDate, pickupDate, orderedItems, orderErrors, deliveryInformation, contactPhoneNumber, contactName, paymentTypeEntity, distributionLabel);        
        this.print(finalString, orderId, "Kitchen_Receipt");
        
    }
    
   
    private void print(String printStream, int orderNumber,String printingType)
    {
		log.info(String.format("Printing type is %s", printingMethod));
        File f = new File("/etc/junk/"+schemaName+ "_order_" + orderNumber + "_" + printingType);
        
        try {
            if(!f.exists())
                f.createNewFile();
            
            FileWriter writer = new FileWriter(f);
    		log.info("Information : \n" + printStream);
            writer.write(printStream);
            writer.flush();
            writer.close();
            String path = f.getAbsolutePath();
            String completeCommand = null;
            
            if(printingMethod.equalsIgnoreCase("STAR-ESC")){
    			log.info("Entered SMB mode!");
    			String smbCommand = !printingCommand.equalsIgnoreCase("none")  &&
    					printingCommand.contains("%s") 
    					? printingCommand : "smbclient --user " + printerMachineUserName + " -W " + printerMachineWorkGroup +" -I " + printerMachineIpAddress + " \"" + printerMachineDirectory+ "\" -c \"print %s\" " + printerMachinePassword;
                
                completeCommand = String.format(smbCommand, path);
            }
            else if(printingMethod.equalsIgnoreCase("LP-PS")){
    			log.info("Entered PS mode!"); 
    			String lpCommand = !printingCommand.equalsIgnoreCase("none")   &&
    					printingCommand.contains("%s") 
    					? printingCommand : "lp %s";
    			completeCommand = String.format(lpCommand, path);
            }
            else if(printingMethod.equalsIgnoreCase("EMAIL")){
    			log.info("Enter E-mail mode!");
                completeCommand = String.format("sendmail -F %s -f %s %s < %s",printingType+"_Order_"+orderNumber,"ticket@spfsolutions.biz",emailReceiptDistributionAddress, path);
            }
            
            
    		log.info(completeCommand);
            
            String[] cmd = {
                    "/bin/sh",
                    "-c",
                    completeCommand
                    };
                    
            // Form the prefix for error logging messages
            String logString = "#" + orderNumber + " " + new Date().toString() + ": ";
                    
            // Print the command string
			log.info(logString + "Executing " + completeCommand);
            
            // Execute the command
            Process execProcess = Runtime.getRuntime().exec(cmd);
        
            // Display the console output
			log.info(logString + "Errors from print command execution:");
            Scanner outputScanner = new Scanner(execProcess.getInputStream()).useDelimiter("\\A");
			log.info(logString + (outputScanner.hasNext() ? outputScanner.next() : "---NO ERRORS---"));
			execProcess.getInputStream().close();
			outputScanner.close();
            
            // Print the command string again
			log.info(logString + "Executed " + completeCommand);
            
            //f.delete();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
            
    }    
        
}