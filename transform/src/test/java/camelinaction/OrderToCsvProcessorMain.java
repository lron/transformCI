/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package camelinaction;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

/**
 *
 * @author lorr
 */
public class OrderToCsvProcessorMain {
    

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
                // format inhouse to csv using a processor
                .process(new OrderToCsvProcessor())
                // and save it to a file
                .to("file://target/orders/received?fileName=report-${header.Date}.csv");
                
            }
        });

        // start the route and let it do its work
        context.start();
        
        String inhouse = "0000004444000001212320091208  1217@1478@2132";
        
        ProducerTemplate template = context.createProducerTemplate();
        template.sendBodyAndHeader("direct:start", inhouse, "Date", "20091208");

        Thread.sleep(10000);

        // stop the CamelContext
        context.stop();
    }
}
