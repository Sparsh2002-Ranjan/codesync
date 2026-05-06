//package com.codesync.execution.config;
//
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitConfig {
//
//    @Value("${execution.queue.jobs}")
//    private String jobsQueue;
//
//    @Value("${execution.queue.results}")
//    private String resultsQueue;
//
//    @Value("${execution.exchange}")
//    private String exchange;
//
//    @Bean
//    public DirectExchange executionExchange() {
//        return new DirectExchange(exchange, true, false);
//    }
//
//    @Bean
//    public Queue jobsQueue() {
//        return QueueBuilder.durable(jobsQueue)
//                .withArgument("x-max-priority", 10)
//                .build();
//    }
//
//    @Bean
//    public Queue resultsQueue() {
//        return QueueBuilder.durable(resultsQueue).build();
//    }
//
//    @Bean
//    public Binding jobsBinding() {
//        return BindingBuilder.bind(jobsQueue()).to(executionExchange()).with("job");
//    }
//
//    @Bean
//    public Binding resultsBinding() {
//        return BindingBuilder.bind(resultsQueue()).to(executionExchange()).with("result");
//    }
//
//    @Bean
//    public Jackson2JsonMessageConverter jsonConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
//        RabbitTemplate t = new RabbitTemplate(cf);
//        t.setMessageConverter(jsonConverter());
//        return t;
//    }
//}
