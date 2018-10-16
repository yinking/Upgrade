package services.dao.Upgrade.model;

import java.sql.Date;

public class UpgradeTransaction {
	Integer id;
	Integer fromAccountId;
	Integer toAccountId;
	Double amount;
	Date time;
	public Integer getId() {
		return this.id;
	}
	public Integer getFromAccountId() {
		return this.fromAccountId;
	}
	public Integer getToAccountId() {
		return this.toAccountId;
	}
	public Double getAmount() {
		return this.amount;
	}
	public Date getTime() {
		return this.time;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setFromAccountId(Integer fromAccountId) {
		this.fromAccountId = fromAccountId;
	}
	public void setToAccountId(Integer toAccountId) {
		this.toAccountId = toAccountId;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public void setTime(Date time) {
		this.time = time;
	}
}
