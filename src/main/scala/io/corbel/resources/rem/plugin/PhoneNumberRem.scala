package io.corbel.resources.rem.plugin

import java.net.URI
import java.util.{Base64, Optional}
import javax.ws.rs.core.Response

import com.google.gson.JsonObject
import com.microtripit.mandrillapp.lutung.MandrillApi
import com.microtripit.mandrillapp.lutung.view.MandrillMessage
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient
import com.twilio.sdk.TwilioRestClient
import grizzled.slf4j.Logging
import io.corbel.resources.rem.BaseRem
import io.corbel.resources.rem.plugin.JavaOptionals._
import io.corbel.resources.rem.request.{ResourceParameters, CollectionParameters, RequestParameters, ResourceId}
import io.corbel.resources.rem.utils.RemProvider
import org.apache.http.message.BasicNameValuePair
import org.springframework.http.HttpMethod._
import sun.security.provider.MD5

import scala.collection.JavaConversions._

/**
 * @author Alexander De Leon <alex.deleon@devialab.com>
 */
class PhoneNumberRem(client: TwilioRestClient, resmiProvider: RemProvider[JsonObject]) extends BaseRem[JsonObject] with Logging {


  override def collection(`type`: String, parameters: RequestParameters[CollectionParameters], uri: URI, entity: Optional[JsonObject]): Response = {
    val optEntity: Option[JsonObject] = toRichOptional(entity).toOption
    optEntity match {
      case Some(json) =>
        val number = json.get("number").getAsString
        val password = generatePassword()
        val registerPhoneNumber = new JsonObject()
        registerPhoneNumber.addProperty("password", password)
        registerPhoneNumber.addProperty("status", "PENDING")
        resmiProvider(PUT).resource("sosms:RegisterPhoneNumber", new ResourceId(number),
          RequestParameters.emptyParameters(), Optional.of(registerPhoneNumber))
        val verifyMessage = s"Here is your SOSMS password: $password.\nPlease, return to the webpage to verify your number."
        sendSMSMessage(number, verifyMessage)
        Response.accepted().build()
      case _ => Response.status(400).build()
    }
  }


  override def resource(`type`: String, id: ResourceId, parameters: RequestParameters[ResourceParameters],
                        entity: Optional[JsonObject]): Response = {
    val optEntity: Option[JsonObject] = toRichOptional(entity).toOption
    optEntity match {
      case Some(json) =>
        val response = resmiProvider(GET).resource("sosms:RegisterPhoneNumber", id,
          RequestParameters.emptyParameters(), Optional.empty())
        response.getStatus match {
          case 200 =>
            val passwordSaved = response.getEntity.asInstanceOf[JsonObject].get("password").getAsString
            if(passwordSaved == json.get("password").getAsString) {
              val registerPhoneNumber = new JsonObject()
              registerPhoneNumber.addProperty("status", "OK")
              resmiProvider(PUT).resource("sosms:RegisterPhoneNumber", id,
                RequestParameters.emptyParameters(), Optional.of(registerPhoneNumber))
              Response.ok().build()
            } else {
              Response.status(401).build()
            }
          case 404 => Response.status(404).build()
          case _ => Response.status(401).build()
        }
    }
  }

  def generatePassword(): String = {
    return new String(Base64.getEncoder.encode((1000000 % Math.random()).toString.getBytes())).substring(0,6)
  }

  def sendSMSMessage(to: String, message: String): Unit = {
    val params = List(
      new BasicNameValuePair("To", to),
      new BasicNameValuePair("From", "+34928100167"),
      new BasicNameValuePair("Body", message)
    )

    val messageFactory = client.getAccount().getMessageFactory()
    val msj = messageFactory.create(params)
    info(s"Message sent sid=${msj.getSid}")
  }

  override def getType: Class[JsonObject] = classOf[JsonObject]
}
