package com.respawn.services;

import com.respawn.services.rabbitmq.producer.RabbitMQTestImpl;
import com.respawnmarket.RabbitMQTestRequest;
import com.respawnmarket.RabbitMQTestResponse;
import com.respawnmarket.RabbitMQTestServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQTestServiceImpl extends RabbitMQTestServiceGrpc.RabbitMQTestServiceImplBase
{
    private final RabbitMQTestImpl rabbitMQTest;

    @Autowired
    public RabbitMQTestServiceImpl(RabbitMQTestImpl rabbitMQTest){
        this.rabbitMQTest = rabbitMQTest;
    }

    @Override
    public void executeTest(RabbitMQTestRequest request, StreamObserver<RabbitMQTestResponse> responseObserver)
    {
        String string = "RabbitMQ is Work!!!!";
        RabbitMQTestResponse response = RabbitMQTestResponse.newBuilder().setResponse(string).build();
        rabbitMQTest.sendTestEvent();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        IO.println(string);
    }
}
