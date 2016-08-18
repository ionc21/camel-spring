package com.pluralsight.orderfulfillment.test;

import org.springframework.jdbc.core.JdbcTemplate;

public class DerbyDatabaseBean {
	
	private JdbcTemplate jdbcTemplate;
	
	public void create() {
		try {
			jdbcTemplate.execute("drop table if exists orders.orderItem");
			jdbcTemplate.execute("drop table if exists orders.\"order\"");
			jdbcTemplate.execute("drop table if exists orders.catalogitem");
			jdbcTemplate.execute("drop table if exists orders.customer");
			jdbcTemplate.execute("drop schema orders");
		} catch (Exception e) {
			System.out.println(e);
			
			jdbcTemplate.execute("");
			jdbcTemplate.execute("");
			jdbcTemplate.execute("");
			jdbcTemplate.execute("");
			jdbcTemplate.execute("");
			jdbcTemplate.execute("");
			jdbcTemplate.execute("");
		}
	}

}
