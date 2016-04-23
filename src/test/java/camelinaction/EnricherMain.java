/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camelinaction;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AggregationStrategy;

/**
 *
 * @author lorr
 */
public class EnricherMain {
    

    public static void main(String args[]) throws Exception {
        // create CamelContext
        CamelContext context = new DefaultCamelContext();
        
        // connect to embedded ActiveMQ JMS broker
        ConnectionFactory connectionFactory = 
            new ActiveMQConnectionFactory("vm://localhost");
        context.addComponent("jms",
            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));

        // add our route to the CamelContext
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                
                from("direct:start")
                //from("file:src/data/quote.json?noop=true") 
                .process(new OrderToCsvProcessor()) 
                //.enrich("restlet:http://localhost:8080/predictions3/resourcesP/json", new AggregationStrategy() {
                .enrich("restlet:http://quotes.rest:80/qod.json", new AggregationStrategy() {
                public Exchange aggregate(Exchange oldExchange,
                                          Exchange newExchange) {
                    if (newExchange == null) {
                        return oldExchange;
                    }
                    String http = oldExchange.getIn()
                                      .getBody(String.class);
                    String ftp = newExchange.getIn()
                                      .getBody(String.class);
                    String body = http + "\n" + ftp;
                    oldExchange.getIn().setBody(body);
                    return oldExchange;
                    }
                }).to("file:src/data?fileName=file.json");

                

            }
        });

        // start the route and let it do its work
        context.start();
        
        String inhouse = "0000004444000001212320091208  1217@1478@2132";
        
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBodyAndHeader("direct:start", inhouse, "Date", "1233");

        Thread.sleep(10000);

        // stop the CamelContext
        context.stop();
    }
}
