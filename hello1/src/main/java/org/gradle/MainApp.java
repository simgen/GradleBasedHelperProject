package org.gradle;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages="org.gradle")
public class MainApp {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext context=new AnnotationConfigApplicationContext(AppConfig.class);
		// Getting User bean using name given
		User user1=(User)context.getBean("userBean");
		user1.setFirstName("Adam");
		user1.setLastName("Smith");
		user1.setAge("30");
		// Getting same user Bean using Class name. Here we don't need to cast.
		User user2=context.getBean(User.class);
		// Since Spring beans are Singletons by default, we will get user1 bean data to be printed below.
		System.out.println(user2);
	}

}
