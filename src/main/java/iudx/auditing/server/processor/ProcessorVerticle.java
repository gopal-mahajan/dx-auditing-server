package iudx.auditing.server.processor;

import static iudx.auditing.server.common.Constants.*;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.rabbitmq.RabbitMqService;

public class ProcessorVerticle extends AbstractVerticle {

  private MessageProcessService processor;
  private PostgresService postgresService;
  private ImmudbService immudbService;
  private ServiceBinder binder;
  private MessageConsumer<JsonObject> consumer;
  private RabbitMqService rabbitMqService;

  @Override
  public void start() throws Exception {

    postgresService = PostgresService.createProxy(vertx, PG_SERVICE_ADDRESS);
    immudbService = ImmudbService.createProxy(vertx, IMMUDB_SERVICE_ADDRESS);
    rabbitMqService = RabbitMqService.createProxy(vertx,RMQ_SERVICE_ADDRESS);
    processor = new MessageProcessorImpl(postgresService, immudbService, rabbitMqService, config());

    binder = new ServiceBinder(vertx);

    consumer =
        binder.setAddress(MSG_PROCESS_ADDRESS).register(MessageProcessService.class, processor);
  }

  @Override
  public void stop() {
    binder.unregister(consumer);
  }
}
