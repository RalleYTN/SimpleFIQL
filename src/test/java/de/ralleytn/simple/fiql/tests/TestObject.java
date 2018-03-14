package de.ralleytn.simple.fiql.tests;

import de.ralleytn.simple.fiql.FIQLValue;

public class TestObject {

	private String systemDocumentID;
	private String id;
	private String number;
	private String company;
	private String website;
	private int salutation;
	private String title;
	private String firstName;
	private String lastName;
	private int country;
	private String city;
	private String zipCode;
	private String street;
	private String email;
	private String phone;
	private String mobile;
	private String fax;
	private String comment;
	
	public void setSystemDocumentID(String systemDocumentID) {
		
		this.systemDocumentID = systemDocumentID;
	}
	
	public void setID(String id) {
		
		this.id = id;
	}
	
	public void setNumber(String number) {
		
		this.number = number;
	}
	
	public void setCompany(String company) {
		
		this.company = company;
	}
	
	public void setWebsite(String website) {
		
		this.website = website;
	}
	
	public void setSalutation(int salutation) {
		
		this.salutation = salutation;
	}
	
	public void setTitle(String title) {
		
		this.title = title;
	}
	
	public void setFirstName(String firstName) {
		
		this.firstName = firstName;
	}
	
	public void setLastName(String lastName) {
		
		this.lastName = lastName;
	}
	
	public void setCountry(int country) {
		
		this.country = country;
	}
	
	public void setCity(String city) {
		
		this.city = city;
	}
	
	public void setZipCode(String zipCode) {
		
		this.zipCode = zipCode;
	}
	
	public void setStreet(String street) {
		
		this.street = street;
	}
	
	public void setEmail(String email) {
		
		this.email = email;
	}
	
	public void setPhone(String phone) {
		
		this.phone = phone;
	}
	
	public void setMobile(String mobile) {
		
		this.mobile = mobile;
	}
	
	public void setFax(String fax) {
		
		this.fax = fax;
	}
	
	public void setComment(String comment) {
		
		this.comment = comment;
	}
	
	@FIQLValue("SystemDocumentID")
	public String getSystemDocumentID() {
		
		return this.systemDocumentID;
	}
	
	@FIQLValue("ID")
	public String getID() {
		
		return this.id;
	}
	
	@FIQLValue("Number")
	public String getNumber() {
		
		return this.number;
	}
	
	@FIQLValue("Company")
	public String getCompany() {
		
		return this.company;
	}
	
	@FIQLValue("Website")
	public String getWebsite() {
		
		return this.website;
	}
	
	@FIQLValue("Salutation")
	public int getSalutation() {
		
		return this.salutation;
	}
	
	@FIQLValue("Title")
	public String getTitle() {
		
		return this.title;
	}
	
	@FIQLValue("FirstName")
	public String getFirstName() {
		
		return this.firstName;
	}
	
	@FIQLValue("LastName")
	public String getLastName() {
		
		return this.lastName;
	}
	
	@FIQLValue("Country")
	public int getCountry() {
		
		return this.country;
	}
	
	@FIQLValue("City")
	public String getCity() {
		
		return this.city;
	}
	
	@FIQLValue("ZipCode")
	public String getZipCode() {
		
		return this.zipCode;
	}
	
	@FIQLValue("Street")
	public String getStreet() {
		
		return this.street;
	}
	
	@FIQLValue("Email")
	public String getEmail() {
		
		return this.email;
	}
	
	@FIQLValue("Phone")
	public String getPhone() {
		
		return this.phone;
	}
	
	@FIQLValue("Mobile")
	public String getMobile() {
		
		return this.mobile;
	}
	
	@FIQLValue("Fax")
	public String getFax() {
		
		return this.fax;
	}
	
	@FIQLValue("Comment")
	public String getComment() {
		
		return this.comment;
	}
}
