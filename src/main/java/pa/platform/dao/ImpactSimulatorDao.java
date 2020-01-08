package pa.platform.dao;

import java.math.BigInteger;
import java.util.List;

public interface ImpactSimulatorDao {
	
	public List<BigInteger> getScenarioIds(BigInteger projectId,int brandId);

}
