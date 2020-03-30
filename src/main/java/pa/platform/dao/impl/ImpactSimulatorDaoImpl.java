package pa.platform.dao.impl;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import pa.platform.core.DaoManager;
import pa.platform.dao.ImpactSimulatorDao;

public class ImpactSimulatorDaoImpl implements ImpactSimulatorDao{

	private static Logger logger = Logger.getLogger(ImpactSimulatorDaoImpl.class);
	
	@Override
	public List<BigInteger> getScenarioIds(BigInteger projectId, int brandId) {
		List<BigInteger> scenarioIds = new ArrayList<BigInteger>();
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		
		String query="select ScenarioId from dbo.Scenario where Project_Id = ? and BrandId = ?";
		
		try{
			connection = DaoManager.getImpactSimulatorConnection();
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setLong(1, projectId.longValue());
			preparedStatement.setInt(2, brandId);
			logger.info("projectId and brandId from query = "+projectId+" "+brandId);
			rs = preparedStatement.executeQuery();
			while(rs.next()){
				
				Long scenarioId = rs.getLong("ScenarioId");
				scenarioIds.add(BigInteger.valueOf(scenarioId));
				logger.info("Getting scenarioId from query = "+scenarioId);
			}
			
		}catch(Exception ex){
			logger.error("Exception occured while fetching scenarioIds for provided projectId");
		}finally{
	
				DaoManager.close(rs);
				DaoManager.close(preparedStatement);
				DaoManager.close(connection);
			
		}
		
		return scenarioIds;
	}
	
	@Override
	public BigInteger getDataEntryId(BigInteger projectId, int brandId) {
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		BigInteger dataEntryId = new BigInteger("0");
		String query="select DataEntryId from dbo.Project where ProjectId = ? and BrandId = ?";
		
		try{
			connection = DaoManager.getImpactSimulatorConnection();
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setLong(1, projectId.longValue());
			preparedStatement.setInt(2, brandId);
			logger.info("projectId and brandId from query = "+projectId+" "+brandId);
			rs = preparedStatement.executeQuery();
			while(rs.next()){
				
				dataEntryId = BigInteger.valueOf(rs.getLong("DataEntryId"));
				
				logger.info("Getting DataEntryId from query = "+dataEntryId);
			}
			
		}catch(Exception ex){
			logger.error("Exception occured while fetching scenarioIds for provided projectId");
		}finally{
	
				DaoManager.close(rs);
				DaoManager.close(preparedStatement);
				DaoManager.close(connection);
			
		}
		
		return dataEntryId;
	}

}
