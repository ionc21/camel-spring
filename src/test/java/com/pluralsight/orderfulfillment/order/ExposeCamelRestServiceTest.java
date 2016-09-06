package com.pluralsight.orderfulfillment.order;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.camel.test.spring.CamelSpringDelegatingTestContextLoader;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

import com.pluralsight.orderfulfillment.fulfillmentcenterone.service.FulfillmentCenterOneProcessor;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ExposeCamelRestServiceTest.TestConfig.class }, loader = CamelSpringDelegatingTestContextLoader.class)
public class ExposeCamelRestServiceTest extends CamelConfiguration {

	@EndpointInject(uri = "mock:direct:result")
	protected MockEndpoint resultEndpoint;

	@Produce(uri = "direct:test")
	protected ProducerTemplate testProducer;

	public static String fulfillmentCenter1Message = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<Order xmlns=\"http://www.pluralsight.com/orderfulfillment/Order\">" + "<OrderType>" + "<FirstName>Jane</FirstName>"
			+ "<LastName>Smith</LastName>" + "<Email>jane@somehow.com</Email>" + "<OrderNumber>1003</OrderNumber>"
			+ "<TimeOrderPlaced>2014-10-24T12:09:21.330-05:00</TimeOrderPlaced>" + "<FulfillmentCenter>"
			+ com.pluralsight.orderfulfillment.generated.FulfillmentCenter.FULFILLMENT_CENTER_ONE.value() + "</FulfillmentCenter>" + "<OrderItems>"
			+ "<ItemNumber>078-1344200444</ItemNumber>" + "<Price>20.00000</Price>" + "<Quantity>1</Quantity>" + "</OrderItems>" + "</OrderType>" + "</Order>";

	private static final String CXF_RS_ENDPOINT_URI = "cxfrs://http://localhost:"
			+ 9000
			+ "/services/orderFulfillment/processXmlOrders?resourceClasses=com.pluralsight.orderfulfillment.fulfillmentcenterone.service.OrderServiceRest&bindingStyle=SimpleConsumer";

	@Configuration
	@ImportResource({ "classpath:META-INF/cxf/cxf.xml", "classpath:META-INF/cxf/cxf-servlet.xml", "classpath:META-INF/cxf/cxf-extension-http.xml",
			"classpath:META-INF/cxf/cxf-extension-http-jetty.xml" })
	public static class TestConfig extends SingleRouteCamelConfiguration {

		@Bean
		public FulfillmentCenterOneProcessor fulfillmentCenterOneProcessor() {
			return new FulfillmentCenterOneProcessor();
		}

		@Bean
		@Override
		public RouteBuilder route() {
			return new RouteBuilder() {

				@Override
				public void configure() throws Exception {
					from("direct:test").to(CXF_RS_ENDPOINT_URI);

					// from("direct:test")

					from(CXF_RS_ENDPOINT_URI).beanRef("fulfillmentCenterOneProcessor", "transformToOrderRequestMessage")
							.setHeader(org.apache.camel.Exchange.CONTENT_TYPE, constant("application/json"))
							.to("http4://localhost:8090/services/orderFulfillment/processOrders").to("mock:direct:result");
				}
			};
		}
	}

	@Test
	public void test_success() throws Exception {

		resultEndpoint.expectedMessageCount(1);

		// 1 - Send the XML as the body of the message through the route
		testProducer.sendBody(fulfillmentCenter1Message);

		// 2 - Wait until aggregation is complete.
		Thread.sleep(10000);
		// 3 - Print out the results to manually verify the aggregated message.

		resultEndpoint.assertIsSatisfied();
		System.err.println(resultEndpoint.getExchanges().size());
	}
}
