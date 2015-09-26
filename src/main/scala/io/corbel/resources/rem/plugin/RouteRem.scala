package io.corbel.resources.rem.plugin

import java.util.Optional
import javax.ws.rs.core.Response

import com.google.gson.JsonObject
import grizzled.slf4j.Logging
import io.corbel.resources.rem.BaseRem
import io.corbel.resources.rem.plugin.JavaOptionals._
import io.corbel.resources.rem.request.{RequestParameters, ResourceId, ResourceParameters}
import io.corbel.resources.rem.utils.RemProvider
import org.springframework.http.HttpMethod
import org.springframework.http.HttpMethod._

/**
 * @author Alexander De Leon <alex.deleon@devialab.com>
 */
class RouteRem(resmiProvider: RemProvider[JsonObject], method: HttpMethod) extends BaseRem[JsonObject] with Logging {


  override def resource(`type`: String, id: ResourceId, parameters: RequestParameters[ResourceParameters],
                        entity: Optional[JsonObject]): Response = {
    val optEntity: Option[JsonObject] = toRichOptional(entity).toOption
    optEntity match {
      case Some(json) =>
        val response = resmiProvider(GET).resource("sosms:RegisterPhoneNumber", id,
          RequestParameters.emptyParameters(), Optional.empty())
        response.getStatus match {
          case 200 =>
            if (response.getEntity.asInstanceOf[JsonObject].get("status").getAsString == "OK" &&
              response.getEntity.asInstanceOf[JsonObject].get("password").getAsString
                == json.asInstanceOf[JsonObject].get("password").getAsString) {
              resmiProvider(method).resource("sosms:InternalRoute", id, RequestParameters.emptyParameters(), entity)
            } else {
              Response.status(403).build()
            }
          case 404 => Response.status(404).build()
          case _ => Response.status(401).build()
        }
      case _ => Response.status(400).build()
    }
  }

  override def getType: Class[JsonObject] = classOf[JsonObject]
}
