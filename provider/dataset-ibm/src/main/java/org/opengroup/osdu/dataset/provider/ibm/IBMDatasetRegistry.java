/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.dataset.provider.ibm;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:swagger.properties")
@ComponentScan({"org.opengroup.osdu"})
public class IBMDatasetRegistry {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(IBMDatasetRegistry.class, args);
		
	}

}
