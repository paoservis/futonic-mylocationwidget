package com.futonredemption.mylocation.data;

import android.location.Address;
import android.location.Location;

public class ResolvedLocation {

	private Location location;
	private Address address;
	
	public ResolvedLocation() {
	}
	
	public void setLocation(Location location) {
		this.location = location; 
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}
	
	public boolean hasLocation() {
		return location != null;
	}
	
	public boolean hasAddress() {
		return address != null;
	}
	public Location getLocation() {
		return location;
	}
	
	public Address getAddress() {
		return address;
	}
}
