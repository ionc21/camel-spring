package com.pluralsight.orderfulfillment.fulfillmentcenterone.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Main resource service provider for orders. Services are available through the
 * path /orderFulfillment.
 *
 * @author Michael Hoffman, Pluralsight
 *
 */
@Path("/orderFulfillment")
public class OrderServiceRest {

	/**
	 * Processing an order request. Simply writes out the order request to
	 * System.err and then returns a fulfillment response object with the status
	 * code 200 and status as success.
	 *
	 * @param orderRequest
	 * @return
	 */
	@POST
	@Path("/processOrders")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public FulfillmentResponse processOrders(OrderRequest orderRequest) {
		System.err.println(orderRequest);
		FulfillmentResponse response = new FulfillmentResponse(200, "Success!");
		return response;
	}

	@POST
	@Path("/processXmlOrders")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.APPLICATION_JSON)
	public FulfillmentResponse processXmlOrders(Order order) {
		System.err.println(order);
		// FulfillmentResponse response = new FulfillmentResponse(200, "Success!");
		// return response;
		return null;

	}

	@GET
	public void ping() {
		// strangely, this method is not called, only serves to configure the endpoint
	}
}
