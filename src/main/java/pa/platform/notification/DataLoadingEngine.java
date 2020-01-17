package pa.platform.notification;

import java.io.FileOutputStream;
import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;

import pa.platform.core.DaoManager;
import pa.platform.dao.ImpactSimulatorDao;
import pa.platform.dao.UserDao;
import pa.platform.dao.impl.ImpactSimulatorDaoImpl;
import pa.platform.dao.impl.UserDaoImpl;
import pa.platform.event.ImpactSimulatorEvent;
import pa.platform.model.Notification;
import pa.platform.model.UserDetails;
import pa.platform.targeting.channel.EmailClient;

public class DataLoadingEngine {
	
	private static Logger logger = Logger.getLogger(DataLoadingEngine.class);
	
	private ImpactSimulatorEvent event;

	public DataLoadingEngine(ImpactSimulatorEvent event) {
		super();
		this.event = event;
	}
	
	public void startLoadingData(){
		
		try{
			ImpactSimulatorDao impactSimulatorDao = new ImpactSimulatorDaoImpl();
			List<BigInteger> scenarioIds = new ArrayList<BigInteger>();
			if(event.getScenario_Id()== BigInteger.valueOf(0)){
				logger.info("Fetched associated ScenarioIds for the provided projectId");
				scenarioIds = impactSimulatorDao.getScenarioIds(event.getProject_Id(), event.getBrandId());
			}else{
				logger.info("Add associated ScenarioIds for the provided projectId");
				scenarioIds.add(event.getScenario_Id());
			}
		
			logger.info("scenarioIds size "+scenarioIds.size());
			List<String> filePaths = new ArrayList<String>();
			if(!scenarioIds.isEmpty()){
				for(BigInteger scenarioId : scenarioIds){
					logger.info("scenarioId =  "+scenarioId);
					String filename= "/tmp/"+"ImpactSimulator"+"_"+event.getBrandId()+"_"+event.getProject_Id()+"_"+scenarioId+"_"+new Random().nextInt()+".xls" ;
					filePaths.add(filename);
					logger.info("File Path : "+filename);
					logger.info("Starttime for data loading in excel file : "+System.currentTimeMillis());
					event.setScenario_Id(scenarioId);
					createAndLoadImpactSimultaorWorkBook(filename,event);
					logger.info("Endtime for data loading in excel file : "+System.currentTimeMillis());
				}
			}
			logger.info("filePaths size "+filePaths.size());
			UserDao userDao = new UserDaoImpl();
			UserDetails userDetails = userDao.getUserDetailsByUserId(event.getUserId());
			Notification notif =  new Notification();
			notif.setEmailAddress(userDetails.getEmail());
			EmailClient emailClient =  new EmailClient(notif);
			emailClient.sendImpactSheetEMail(filePaths);
		}catch(Exception ex){
			logger.info("some exception occured while copying simulator data to excel");
		}
	}
	
	private void createAndLoadImpactSimultaorWorkBook(String fileName,ImpactSimulatorEvent impSimEvent) {
		try{
		HSSFWorkbook hwb=new HSSFWorkbook();
		Connection con = DaoManager.getImpactSimulatorConnection();
		logger.info("Event details = " +impSimEvent.getBrandId()+""+impSimEvent.getProject_Id()+""+impSimEvent.getScenario_Id());
		logger.info("start loading Store_Tier_View data");
		createAndLoadStoreTierViewWorkSheet(con,fileName,impSimEvent,hwb);
		logger.info("Store_Tier_View data has been loaded!");
		logger.info("start loading Menu_Item_Tier_View data");
		createAndLoadMenuTierViewWorkSheet(con, fileName, impSimEvent,hwb);
		logger.info("Menu_Item_Tier_View data has been loaded!");
		logger.info("Start Loading Summary View");
		createAndLoadSummaryViewWorkSheet(con, fileName, impSimEvent,hwb);
		logger.info("Summary View has been loaded");
		con.close();
		
		
		}catch(Exception ex){
			logger.info("connection closed");
		}
	}

	private void createAndLoadStoreTierViewWorkSheet(Connection con,String fileName,ImpactSimulatorEvent impSimEvent, HSSFWorkbook hwb) {
		
		try{
			Long noOfRows= 0L;
			int startIndex = 0;
			int endIndex = 100;
			int rowNo = 0;
			//Call the SP to get to know whether there are resultant rows
			String query = "{CALL dbo.StoreTierViewProc(0,1,null,null,null,'Store_Code','ASC',?,?,?)}";
			
			CallableStatement stmt = con.prepareCall(query);
			
			stmt.setLong(1, impSimEvent.getScenario_Id().longValue());
			stmt.setLong(2, impSimEvent.getProject_Id().longValue());
			stmt.setInt(3, impSimEvent.getBrandId());
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				noOfRows = rs.getLong("TotalRows");
				logger.info("total number of records are  : " +noOfRows);
			}
			rs.close();
			stmt.close();
			
			if(noOfRows > 1){
				HSSFSheet sheet =  hwb.createSheet("Store_Tier_View");
				HSSFRow rowhead=   sheet.createRow((short)0);
				rowhead.createCell((short) 0).setCellValue("Store_Sensitivity");
				rowhead.createCell((short) 1).setCellValue("Tier_Change");
				rowhead.createCell((short) 2).setCellValue("Current_Tier");
				rowhead.createCell((short) 3).setCellValue("Market_Name");
				rowhead.createCell((short) 4).setCellValue("Pricing_Power");
				rowhead.createCell((short) 5).setCellValue("Proposed_Tier");
				rowhead.createCell((short) 6).setCellValue("Store_Code");
				rowhead.createCell((short) 7).setCellValue("Store_Name");
				rowhead.createCell((short) 8).setCellValue("Sales_Impact");
				rowhead.createCell((short) 9).setCellValue("New_Sales");
				rowhead.createCell((short) 10).setCellValue("Sales_Impact_Percentage");
				rowhead.createCell((short) 11).setCellValue("Original_Sales");
				rowhead.createCell((short) 12).setCellValue("Quantity");
				int i =1;
				do{
					
					logger.info("start fetching data ....");
					String query1 = "{CALL dbo.StoreTierViewProc(?,?,null,null,null,'Store_Code','ASC',?,?,?)}";
				
					CallableStatement stmt1 = con.prepareCall(query1);
					stmt1.setInt(1,startIndex);
					//stmt1.setInt(2, endIndex);
					stmt1.setInt(2, noOfRows.intValue());
					stmt1.setLong(3, impSimEvent.getScenario_Id().longValue());
					stmt1.setLong(4, impSimEvent.getProject_Id().longValue());
					stmt1.setInt(5, impSimEvent.getBrandId());
					
					ResultSet rs1 = stmt1.executeQuery();
					
					while(rs1.next()){
					HSSFRow row=   sheet.createRow((short)i);
					row.createCell((short) 0).setCellValue(rs1.getString("Store_Sensitivity"));
					row.createCell((short) 1).setCellValue(rs1.getString("Tier_Change"));
					row.createCell((short) 2).setCellValue(rs1.getString("Current_Tier"));
					row.createCell((short) 3).setCellValue(rs1.getString("Market_Name"));
					row.createCell((short) 4).setCellValue(rs1.getString("Pricing_Power"));
					row.createCell((short) 5).setCellValue(rs1.getString("Proposed_Tier"));
					row.createCell((short) 6).setCellValue(rs1.getInt("Store_Code"));
					row.createCell((short) 7).setCellValue(rs1.getString("Store_Name"));
					row.createCell((short) 8).setCellValue(rs1.getDouble("Sales_Impact"));
					row.createCell((short) 9).setCellValue(rs1.getDouble("New_Sales"));
					row.createCell((short) 10).setCellValue(rs1.getDouble("Sales_Impact_Percentage"));
					row.createCell((short) 11).setCellValue(rs1.getDouble("Original_Sales"));
					row.createCell((short) 12).setCellValue(rs1.getLong("Quantity"));
					i++;
					}
					FileOutputStream fileOut =  new FileOutputStream(fileName);
					hwb.write(fileOut);
					fileOut.close();
					
					rs1.close();
					stmt1.close();
					//noOfRows = noOfRows - 100;
					noOfRows = 0L;
					//endIndex=endIndex+100;
					startIndex = startIndex+100;
					rowNo = sheet.getLastRowNum();
				}while(noOfRows > 1);
			}else{
				logger.info("There are no records present for the resultant query");
		}
			
		}catch(Exception ex){
			//
		}
		
	}
	
	
	
	private void createAndLoadSummaryViewWorkSheet(Connection con, String fileName,ImpactSimulatorEvent impSimEvent, HSSFWorkbook hwb) {
		
		
		HSSFCellStyle style = hwb.createCellStyle();
		style.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
		HSSFFont font = hwb.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short)12);
		font.setColor(IndexedColors.BLUE.getIndex());
		
		style.setFont(font);                 
		
		try{
			int i =1;
		
		logger.info("start loading Tier Shift Summary View");
		HSSFSheet summaryTierViewSheet =  hwb.createSheet("Summary");
		String summaryTierViewQuery = "{CALL dbo.SummaryTierView(?,?,?)}";
		CallableStatement summaryTierViewStmt = con.prepareCall(summaryTierViewQuery);
		
		summaryTierViewStmt.setLong(1, impSimEvent.getScenario_Id().longValue());
		summaryTierViewStmt.setLong(2, impSimEvent.getProject_Id().longValue());
		summaryTierViewStmt.setInt(3, impSimEvent.getBrandId());
		
		ResultSet summaryTierViewRs = summaryTierViewStmt.executeQuery();
		
		if(summaryTierViewRs.next()){
			
			HSSFRow summaryTierViewRowTitle=   summaryTierViewSheet.createRow((short)0);
			HSSFCell tierShiftCell = summaryTierViewRowTitle.createCell((short) 0);
			tierShiftCell.setCellValue("Tier Shift");
			tierShiftCell.setCellStyle(style);
			i=i+2;
			HSSFRow summaryTierViewRowHead=   summaryTierViewSheet.createRow((short)i);
			summaryTierViewRowHead.createCell((short) 0).setCellValue("Original Tier");
			summaryTierViewRowHead.createCell((short) 1).setCellValue("New Tier");
			summaryTierViewRowHead.createCell((short) 2).setCellValue("New Store Count");
			summaryTierViewRowHead.createCell((short) 3).setCellValue("Impact By Tier");
			summaryTierViewRowHead.setRowStyle(style);
			//summaryTierViewRowHead.getRowStyle().setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
			i++;
		while(summaryTierViewRs.next()){
			HSSFRow summaryTierViewRow=   summaryTierViewSheet.createRow((short)i);
			summaryTierViewRow.createCell((short) 0).setCellValue(summaryTierViewRs.getString("Current_Tier"));
			summaryTierViewRow.createCell((short) 1).setCellValue(summaryTierViewRs.getString("Proposed_Tier"));
			summaryTierViewRow.createCell((short) 2).setCellValue(summaryTierViewRs.getDouble("Count_Of_Stores"));
			summaryTierViewRow.createCell((short) 3).setCellValue(summaryTierViewRs.getDouble("Impact_by_Tier"));
			i++;
		}
		i = i+2;
		}
		
		logger.info("start loading Categories Summary View");
		String summaryCategoriesQuery = "{CALL dbo.SummaryCategoryView(?,?,?)}";
		CallableStatement summaryCategoriesStmt = con.prepareCall(summaryCategoriesQuery);
		summaryCategoriesStmt.setLong(1, impSimEvent.getScenario_Id().longValue());
		summaryCategoriesStmt.setLong(2, impSimEvent.getProject_Id().longValue());
		summaryCategoriesStmt.setInt(3, impSimEvent.getBrandId());
		
		ResultSet summaryCategoriesRs = summaryCategoriesStmt.executeQuery();
		
		if(summaryCategoriesRs.next()){
			
			HSSFRow summaryCategoriesViewRowTitle=   summaryTierViewSheet.createRow((short)i);
			//HSSFCell categoryCell = summaryCategoriesViewRowTitle.createCell((short) 0).setCellValue("Categories");
			HSSFCell categoryCell = summaryCategoriesViewRowTitle.createCell((short) 0);
			categoryCell.setCellValue("Categories");
			categoryCell.setCellStyle(style);
			i=i+2;
			HSSFRow summaryCategoriesViewRowHead=   summaryTierViewSheet.createRow((short)i);
			summaryCategoriesViewRowHead.createCell((short) 0).setCellValue("Category");
			summaryCategoriesViewRowHead.createCell((short) 1).setCellValue("%of Total Impact");
			//summaryCategoriesViewRowHead.getRowStyle().setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
			i++;
		
		while(summaryCategoriesRs.next()){
			HSSFRow summaryCategoriesViewRow=   summaryTierViewSheet.createRow((short)i);
			summaryCategoriesViewRow.createCell((short) 0).setCellValue(summaryCategoriesRs.getString("Cat3"));
			summaryCategoriesViewRow.createCell((short) 1).setCellValue(summaryCategoriesRs.getDouble("Percet_Of_Total_Impact"));
			i++;
		}
		i = i+2;
		summaryCategoriesRs.close();
		summaryCategoriesStmt.close();
		}
		
		logger.info("start loading Price Sensitivity Summary View");
		String summaryPriceSensitivityQuery = "{CALL dbo.SummaryPriceSensitivity(?,?,?)}";
		CallableStatement summaryPriceSensitivityStmt = con.prepareCall(summaryPriceSensitivityQuery);
		summaryPriceSensitivityStmt.setLong(1, impSimEvent.getScenario_Id().longValue());
		summaryPriceSensitivityStmt.setLong(2, impSimEvent.getProject_Id().longValue());
		summaryPriceSensitivityStmt.setInt(3, impSimEvent.getBrandId());
		
		ResultSet summaryPriceSensitivityStmtRs = summaryPriceSensitivityStmt.executeQuery();

		if(summaryPriceSensitivityStmtRs.next()){
			HSSFRow summaryPriceSensitivityViewRowTitle=   summaryTierViewSheet.createRow((short)i);
			//summaryPriceSensitivityViewRowTitle.createCell((short) 0).setCellValue("Price Sensitivity");
			HSSFCell priceSensitivityCell = summaryPriceSensitivityViewRowTitle.createCell((short) 0);
			priceSensitivityCell.setCellValue("Price Sensitivity");
			priceSensitivityCell.setCellStyle(style);
			i=i+2;
			HSSFRow summaryPriceSensitivityViewRowHead=   summaryTierViewSheet.createRow((short)i);
			summaryPriceSensitivityViewRowHead.createCell((short) 0).setCellValue("Product Price Sensitivity");
			summaryPriceSensitivityViewRowHead.createCell((short) 1).setCellValue("Quantity%");
			summaryPriceSensitivityViewRowHead.createCell((short) 2).setCellValue("Quantity");
			summaryPriceSensitivityViewRowHead.createCell((short) 3).setCellValue("Original Sales$");
			summaryPriceSensitivityViewRowHead.createCell((short) 4).setCellValue("New Sales$");
			summaryPriceSensitivityViewRowHead.createCell((short) 5).setCellValue("Sales Impact$");
			summaryPriceSensitivityViewRowHead.createCell((short) 6).setCellValue("%Impact");
			summaryPriceSensitivityViewRowHead.createCell((short) 7).setCellValue("Impact% of Total");
			
			//summaryPriceSensitivityViewRowHead.getRowStyle().setFillBackgroundColor(IndexedColors.DARK_BLUE.getIndex());
			i++;
			while(summaryPriceSensitivityStmtRs.next()){
				HSSFRow summaryPriceSensitivityViewRow=   summaryTierViewSheet.createRow((short)i);
				summaryPriceSensitivityViewRow.createCell((short) 0).setCellValue(summaryPriceSensitivityStmtRs.getString("Product_Price_Sensitivity"));
				summaryPriceSensitivityViewRow.createCell((short) 1).setCellValue(summaryPriceSensitivityStmtRs.getDouble("Quantity_Percent"));
				summaryPriceSensitivityViewRow.createCell((short) 2).setCellValue(summaryPriceSensitivityStmtRs.getLong("Quantity"));
				summaryPriceSensitivityViewRow.createCell((short) 3).setCellValue(summaryPriceSensitivityStmtRs.getDouble("Original_Sales"));
				summaryPriceSensitivityViewRow.createCell((short) 4).setCellValue(summaryPriceSensitivityStmtRs.getDouble("New_Sales"));
				summaryPriceSensitivityViewRow.createCell((short) 5).setCellValue(summaryPriceSensitivityStmtRs.getDouble("Sales_Impact"));
				summaryPriceSensitivityViewRow.createCell((short) 6).setCellValue(summaryPriceSensitivityStmtRs.getDouble("Sales_Impact_Percent"));
				summaryPriceSensitivityViewRow.createCell((short) 7).setCellValue(summaryPriceSensitivityStmtRs.getDouble("Total_Impact_Percent"));
				i++;
			}
		}
		FileOutputStream fileOut =  new FileOutputStream(fileName);
		hwb.write(fileOut);
		fileOut.close();
		summaryPriceSensitivityStmtRs.close();
		summaryPriceSensitivityStmt.close();
		
	}catch(Exception ex){
		logger.info(ex.getMessage());
	}
	}
	
	private void createAndLoadMenuTierViewWorkSheet(Connection con,String fileName,ImpactSimulatorEvent impSimEvent, HSSFWorkbook hwb) {
		
		try{
			//HSSFWorkbook hwb=new HSSFWorkbook();
			
			Long noOfRows= 0L;
			int startIndex = 0;
			int endIndex = 100;
			
			String query = "{CALL dbo.MenuitemSelectProc(0,1,null,null,null,null,'Product_ID','ASC',?,?,?)}";
			CallableStatement stmt = con.prepareCall(query);
			stmt.setLong(1, impSimEvent.getScenario_Id().longValue());
			stmt.setLong(2, impSimEvent.getProject_Id().longValue());
			stmt.setInt(3, impSimEvent.getBrandId());
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next()){
				noOfRows = rs.getLong("TotalRows");
				logger.info("total number of records are  : " +noOfRows);
			}
			rs.close();
			stmt.close();
			
			if(noOfRows > 1){
				HSSFSheet sheet =  hwb.createSheet("Menu_Item_Price_View");
		
				HSSFRow rowhead=   sheet.createRow((short)0);
				
				//rowhead.createCell((short) 0).setCellValue("Tier_Change");
				rowhead.createCell((short) 0).setCellValue("Cat1");
				rowhead.createCell((short) 1).setCellValue("Cat2");
				rowhead.createCell((short) 2).setCellValue("Cat3");
				//rowhead.createCell((short) 4).setCellValue("Current_Tier");
				rowhead.createCell((short) 3).setCellValue("Product_ID");
				rowhead.createCell((short) 4).setCellValue("Product_Name");
				rowhead.createCell((short) 5).setCellValue("Product_Price_Sensitivity");
				rowhead.createCell((short) 6).setCellValue("Proposed_Tier");
				rowhead.createCell((short) 7).setCellValue("Sales_Impact");
				rowhead.createCell((short) 8).setCellValue("New_Sales");
				rowhead.createCell((short) 9).setCellValue("Sales_Impact_Percentage");
				rowhead.createCell((short) 10).setCellValue("Original_Sales");
				rowhead.createCell((short) 11).setCellValue("Price_Change_Percent");
				rowhead.createCell((short) 12).setCellValue("Price_Change");
				rowhead.createCell((short) 13).setCellValue("New_Price");
				rowhead.createCell((short) 14).setCellValue("Current_Price");
				rowhead.createCell((short) 15).setCellValue("Quantity_TY");
				int i =1;
				
				do{
				
				String query1 = "{CALL dbo.MenuitemSelectProc(?,?,null,null,null,null,'Product_ID','ASC',?,?,?)}";
				
				CallableStatement stmt1 = con.prepareCall(query1);
				
				stmt1.setInt(1,startIndex);
				//stmt1.setInt(2, endIndex);
				stmt1.setInt(2, noOfRows.intValue());
				stmt1.setLong(3, impSimEvent.getScenario_Id().longValue());
				stmt1.setLong(4, impSimEvent.getProject_Id().longValue());
				stmt1.setInt(5, impSimEvent.getBrandId());
				
				ResultSet rs1 = stmt1.executeQuery();
				
				
				while(rs1.next()){
					HSSFRow row=   sheet.createRow((short)i);
					//row.createCell((short) 0).setCellValue(rs.getString("Tier_Change"));
					row.createCell((short) 0).setCellValue(rs1.getString("Cat1"));
					row.createCell((short) 1).setCellValue(rs1.getString("Cat2"));
					row.createCell((short) 2).setCellValue(rs1.getString("Cat3"));
					//row.createCell((short) 4).setCellValue(rs.getString("Current_Tier"));
					row.createCell((short) 3).setCellValue(rs1.getString("Product_ID"));
					row.createCell((short) 4).setCellValue(rs1.getString("Product_Name"));
					row.createCell((short) 5).setCellValue(rs1.getString("Product_Price_Sensitivity"));
					row.createCell((short) 6).setCellValue(rs1.getString("Proposed_Tier"));
					row.createCell((short) 7).setCellValue(rs1.getDouble("Sales_Impact"));
					row.createCell((short) 8).setCellValue(rs1.getDouble("New_Sales"));
					row.createCell((short) 9).setCellValue(rs1.getDouble("Sales_Impact_Percentage"));
					row.createCell((short) 10).setCellValue(rs1.getDouble("Original_Sales"));
					row.createCell((short) 11).setCellValue(rs1.getDouble("Price_Change_Percent"));
					row.createCell((short) 12).setCellValue(rs1.getDouble("Price_Change"));
					row.createCell((short) 13).setCellValue(rs1.getDouble("New_Price"));
					row.createCell((short) 14).setCellValue(rs1.getDouble("Current_Price"));
					row.createCell((short) 15).setCellValue(rs1.getLong("Quantity_TY"));
					i++;
					}
					FileOutputStream fileOut =  new FileOutputStream(fileName);
					hwb.write(fileOut);
					fileOut.close();
					
					rs1.close();
					stmt1.close();
					//noOfRows = noOfRows - 100;
					noOfRows = 0L;
					startIndex = startIndex+100;
				}while(noOfRows > 1);
			}else{
				logger.info("There are no records present for the resultant query");
		}
		}catch(Exception ex){
			//
		}
		
	}

}
