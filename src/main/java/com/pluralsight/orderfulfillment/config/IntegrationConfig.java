package com.pluralsight.orderfulfillment.config;

import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.sql.DataSource;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.sql.SqlComponent;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

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

	@Inject
	private Environment environment;

	@Bean
	public ConnectionFactory jmsConnectionFactory() {
		return new ActiveMQConnectionFactory(environment.getProperty("activemq.broker.url"));
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public PooledConnectionFactory pooledConnectionFactory() {
		PooledConnectionFactory factory = new PooledConnectionFactory();
		factory.setConnectionFactory(jmsConnectionFactory());
		factory.setMaxConnections(Integer.parseInt(environment.getProperty("pooledConnectionFactory.maxConnections")));
		return factory;
	}

	@Bean
	public JmsConfiguration jmsConfiguration() {
		JmsConfiguration jmsConfiguration = new JmsConfiguration();
		jmsConfiguration.setConnectionFactory(pooledConnectionFactory());
		return jmsConfiguration;
	}

	@Bean
	public ActiveMQComponent activeMq() {
		ActiveMQComponent activeMQComponent = new ActiveMQComponent();
		activeMQComponent.setConfiguration(jmsConfiguration());
		return activeMQComponent;
	}

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
						.beanRef("orderItemMessageTranslator", "transformToOrderItemMessage").to("activemq:queue:ORDER_ITEM_PROCESSING");
			}
		};
	}

	@Bean
	public RouteBuilder fulfillmentCenterContentBasedRouter() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				Namespaces namespace = new Namespaces("o", "http://www.pluralsight.com/orderfulfillment/Order");
				from("activemq:queue:ORDER_ITEM_PROCESSING")
						.choice()
						.when()
						.xpath("/o:Order/o:OrderType/o:FulfillmentCenter = '"
								+ com.pluralsight.orderfulfillment.generated.FulfillmentCenter.ABC_FULFILLMENT_CENTER.value() + "'", namespace)
						.to("activemq:queue:ABC_FULFILLMENT_REQUEST")
						.when()
						.xpath("/o:Order/o:OrderType/o:FulfillmentCenterOne = '"
								+ com.pluralsight.orderfulfillment.generated.FulfillmentCenter.FULFILLMENT_CENTER_ONE.value() + "'" + namespace)
						.to("activemq:queue:FULFILLMENT_CENTER_REQUEST").otherwise().to("activemq:queue:ERROR_FULFILLMENT_REQUEST");
			}
		};
	}
}
