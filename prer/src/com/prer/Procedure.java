package com.prer;

import java.util.HashMap;
import java.util.Map;

public class Procedure {
	public String name;
	public String zipcode;
	public String price;
	public String hospital_addr;
	public String hospital_name;
	public String distance;
	public String cpt_code;
	public String phone_number;
	public String website;
	private Map<String , Object> otherProperties = new HashMap<String , Object>();
	
	public Procedure(String name, String zipcode) {
		this.name = name;
		this.zipcode = zipcode;
	}
	
	public void setHospitalPhone(String phone_number) {
		this.phone_number = phone_number;
	}
	
	public void setHospitalWebsite(String website) {
		this.website = website;
	}
	
	public String getHospitalWebsite() {
		return website;
	}

	public String getHospitalPhoneNumber() {
		return phone_number;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setZipcodeS(String zipcode) {
		this.zipcode = zipcode;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public void setHospital(String hospital) {
		this.hospital_addr = hospital;
	}

	public String getHospitalAddr() {
		return hospital_addr;
	}
	
	public void setHospitalName(String hospital) {
		this.hospital_name = hospital;
	}
	
	public void setCptCode(String cpt_code) {
		this.cpt_code = cpt_code;
	}

	public String getCptCode() {
		return cpt_code;
	}


	public String getHospitalName() {
		return hospital_name;
	}

	public String getName() {
		return name;
	}

	public String getPrice() {
		return price;
	}

	public String getZipcode() {
		return zipcode;
	}

	public String getDistance() {
		return distance;
	}

	public Object get(String name) {
		return otherProperties.get(name);
	}
}
