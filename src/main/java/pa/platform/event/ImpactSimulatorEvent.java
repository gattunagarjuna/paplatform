package pa.platform.event;

import java.math.BigInteger;

public class ImpactSimulatorEvent extends Event{
	
	private String action;
	public int brandId;

	private Integer userId;

	private BigInteger project_Id;
	private BigInteger scenario_Id;

	
	
	
	
	public ImpactSimulatorEvent(String action, int brandId, Integer userId,
			BigInteger project_Id, BigInteger scenario_Id) {
		super();
		this.action = action;
		this.brandId = brandId;
		this.userId = userId;
		this.project_Id = project_Id;
		this.scenario_Id = scenario_Id;
	}





	public String getAction() {
		return action;
	}


	public void setAction(String action) {
		this.action = action;
	}



	public int getBrandId() {
		return brandId;
	}



	public void setBrandId(int brandId) {
		this.brandId = brandId;
	}



	public Integer getUserId() {
		return userId;
	}



	public void setUserId(Integer userId) {
		this.userId = userId;
	}



	public BigInteger getProject_Id() {
		return project_Id;
	}



	public void setProject_Id(BigInteger project_Id) {
		this.project_Id = project_Id;
	}



	public BigInteger getScenario_Id() {
		return scenario_Id;
	}



	public void setScenario_Id(BigInteger scenario_Id) {
		this.scenario_Id = scenario_Id;
	}


}
