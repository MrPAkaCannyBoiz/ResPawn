import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class NewTask {

    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            boolean durable = true;
            channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);

            String message = String.join(" ", argv);
            if (message.isEmpty()) {
                message = "Hello…"; // или какая-то дефолтная строка
            }

            // Сделаем сообщение “persistent” — чтобы не терялось при падении брокера
            channel.basicPublish("", TASK_QUEUE_NAME,
                    com.rabbitmq.client.MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
