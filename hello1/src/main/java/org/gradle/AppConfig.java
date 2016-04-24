package org.gradle;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/*
 * Sample Gradle project with build script that can be used
 * as standalone jar project
 * Build Steps : gradle fatJar (this will create jar with all spring dependencies )
 * Jar will be created in the lib folder of your project 
 * Deploy : deploy the jar on any linux server
 * Run : java -jar hello1-all-1.0.jar
 * 
 *  
 */
@Configuration
@ComponentScan(basePackages="org.gradle")
public class AppConfig {

	
	
}