package com.pluralsight.orderfulfillment.config;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pluralsight.orderfulfillment.order.OrderStatus;

/**
 * Spring configuration for Apache Camel
 *
 * @author Michael Hoffman, Pluralsight
 */
@Configuration
public class IntegrationConfig extends CamelConfiguration {

	@Inject
	private DataSource dataSource;

	@Bean
	public SqlComponent sql() {
		SqlComponent sqlComponent = new SqlComponent();
		sqlComponent.setDataSource(dataSource);
		return sqlComponent;
	}

	/**
	 * Camel RouteBuilder for routing orders from the orders database. Routes any
	 * orders with status set to new, then updates the order status to be in
	 * process. The route sends the message exchange to a log component.
	 *
	 * @return
	 */
	@Bean
	public RouteBuilder newWebsiteOrderRoute() {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				// Send from the SQL component to the Log component.
				from(
						"sql:" + "select id from orders.\"order\" where status = '" + OrderStatus.NEW.getCode() + "'" + "?"
								+ "consumer.onConsume=update orders.\"order\" set status = '" + OrderStatus.PROCESSING.getCode() + "' where id = :#id")
						.beanRef("orderItemMessageTranslator", "transformToOrderItemMessage").to("log:com.pluralsight.orderfulfillment.order?level=INFO");
			}
		};
	}
}
