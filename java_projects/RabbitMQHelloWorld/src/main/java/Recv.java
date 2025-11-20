import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;


public class Recv {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // Фабрика подключений
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        // Обычный try, без автозакрытия: нам надо "вечно" слушать очередь
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Убедиться, что очередь существует (можно запустить получателя раньше отправителя)
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // Что делать, когда приходит сообщение
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };

        // Подписываемся на очередь: true = auto-ack (авто-подтверждение сообщений)
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
