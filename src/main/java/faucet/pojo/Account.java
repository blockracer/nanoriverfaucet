package faucet;

public class Account {

	private String account;
	private String idaccountsPaid;

    	public Account(String account, String idAccountsPaid) {

		this.account = account;
		this.idaccountsPaid = idaccountsPaid;
	}

       	public String getId() {
		
		return idaccountsPaid;
	}	
	public String getAccount() {
		
		return account;
	}
}





