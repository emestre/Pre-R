package com.prer;

import java.util.HashMap;
import java.util.Map;

public class Procedure {
	public String name;
	public String zipcode;
	public String price;
	public String hospital;
	public String hospital_name;
	public String distance;
	private Map<String , Object> otherProperties = new HashMap<String , Object>();
	
	public Procedure(String name, String zipcode) {
		this.name = name;
		this.zipcode = zipcode;
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
		this.hospital = hospital;
	}

	public String getHospital() {
		return hospital;
	}
	
	public void setHospitalName(String hospital) {
		this.hospital_name = hospital;
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
