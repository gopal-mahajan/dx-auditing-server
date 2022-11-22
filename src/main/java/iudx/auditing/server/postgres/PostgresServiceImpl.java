package iudx.auditing.server.postgres;

import static iudx.auditing.server.common.Constants.PG_DELETE_QUERY_KEY;
import static iudx.auditing.server.common.Constants.PG_INSERT_QUERY_KEY;
import static iudx.auditing.server.common.Constants.RESULT;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PostgresServiceImpl implements PostgresService {
  private static final Logger LOGGER = LogManager.getLogger(PostgresServiceImpl.class);

  private final PgPool pgPool;

  public PostgresServiceImpl(final PgPool pgclient) {
    this.pgPool = pgclient;
  }

  @Override
  public Future<JsonObject> executeWriteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    pgPool
        .withConnection(
            connection -> connection.query(query.getString(PG_INSERT_QUERY_KEY)).execute())
        .onComplete(
            rows -> {
              if (rows.succeeded()) {
                LOGGER.debug("Postgres Table Updated successfully");
                response.put(RESULT, "Postgres Table Updated Successfully");
                promise.complete(response);
              }
              if (rows.failed()) {
                LOGGER.error("Info failed:" + rows.cause().getMessage());
                response.put(RESULT, rows.cause().getMessage());
                promise.fail(rows.cause().getMessage());
              }
            });
    return promise.future();
  }

  @Override
  public Future<JsonObject> executeDeleteQuery(JsonObject query) {
    Promise<JsonObject> promise = Promise.promise();
    JsonObject response = new JsonObject();
    pgPool
        .withConnection(
            connection -> connection.query(query.getString(PG_DELETE_QUERY_KEY)).execute())
        .onComplete(
            rows -> {
              if (rows.succeeded()) {
                LOGGER.debug("Postgres Table row deleted successfully");

                response.put(RESULT, "Postgres Table row deleted Successfully");
                promise.complete(response);
              }
              if (rows.failed()) {
                LOGGER.error("Info failed:" + rows.cause());
                response.put(RESULT, rows.cause().getMessage());
                promise.fail(rows.cause().getMessage());
              }
            });

    return promise.future();
  }
}