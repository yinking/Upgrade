package dao.Upgrade.model;


public class UpgradeAccount {
	Integer id;
	Integer ownerId;
	Double balance;


	public UpgradeAccount() {
	}

	public UpgradeAccount(Integer ownerId, Double balance) {
		this.ownerId = ownerId;
		this.balance = balance;
	}

	public Integer getId() {
		return this.id;
	}
	public Integer getOwnerId() {
		return this.ownerId;
	}
	public Double getBalance() {
		return this.balance;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}
	public void setBalance(Double balance) {
		this.balance = balance;
	}
}
