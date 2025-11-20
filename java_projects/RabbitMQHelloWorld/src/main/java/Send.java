import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class Send {

    // Имя очереди, в которую отправляем сообщение
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        // Фабрика подключений к RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // RabbitMQ запущен на твоём компе

        // try-with-resources: соединение и канал сами закроются в конце блока
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Объявляем очередь (если её нет — создастся, если есть — просто используем)
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // Сообщение, которое отправляем
            String message = "Hello World!";

            // Отправляем сообщение в очередь
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
