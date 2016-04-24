package org.gradle;

import org.springframework.stereotype.Component;

@Component(value="userBean")
public class User {
	private String firstName;
	private String lastName;
	private String age;

	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	
	public String toString(){
		return "UserBean: firstName - "+firstName +", lastName -"+lastName +", Age - "+age;
	}

}