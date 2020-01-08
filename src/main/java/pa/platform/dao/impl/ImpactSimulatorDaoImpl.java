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
			rs = preparedStatement.executeQuery();
			while(rs.next()){
				
				Long scenarioId = rs.getLong("ScenarioId");
				scenarioIds.add(BigInteger.valueOf(scenarioId));
			}
			
		}catch(Exception ex){
			logger.info("Exception occured while fetching scenarioIds for provided projectId");
		}finally{
	
				DaoManager.close(rs);
				DaoManager.close(preparedStatement);
				DaoManager.close(connection);
			
		}
		
		return scenarioIds;
	}

}
