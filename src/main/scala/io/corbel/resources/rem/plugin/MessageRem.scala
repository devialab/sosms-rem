package io.corbel.resources.rem.plugin

import java.net.URI
import java.util.Optional
import javax.ws.rs.core.Response

import com.google.gson.JsonObject
import com.microtripit.mandrillapp.lutung.MandrillApi
import com.microtripit.mandrillapp.lutung.view.MandrillMessage
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient
import com.twilio.sdk.TwilioRestClient
import grizzled.slf4j.Logging
import io.corbel.resources.rem.request.{ResourceId, CollectionParameters, RequestParameters}
import io.corbel.resources.rem.utils.RemProvider
import io.corbel.resources.rem.{BaseRem}
import org.apache.http.message.BasicNameValuePair

import scala.collection.JavaConversions._
import JavaOptionals._
import org.springframework.http.HttpMethod._

/**
 * @author Alexander De Leon <alex.deleon@devialab.com>
 */
class MessageRem(client: TwilioRestClient, emailClient: MandrillApi, resmiProvider: RemProvider[JsonObject]) extends BaseRem[JsonObject] with Logging {


  override def collection(`type`: String, parameters: RequestParameters[CollectionParameters], uri: URI, entity: Optional[JsonObject]): Response = {
    val optEntity: Option[JsonObject] = toRichOptional(entity).toOption
    optEntity match {
      case Some(json) =>
        val to = json.get("to").getAsString
        getRoute(to) match {
          case Some(route) =>
            val msj = routedMessage(to, json.get("message").getAsString)
            route.get("type").getAsString match {
              case "email" => sendEmailMessage(to, route.get("value").getAsString, msj)
              case "phone" => sendSMSMessage(route.get("value").getAsString,msj )
              case t: String => throw new IllegalArgumentException(s"Unsupported Route type: $t")
            }
          case None =>
            sendSMSMessage(to, json.get("message").getAsString)
        }
        Response.accepted().build()


      case _ => Response.status(400).entity("Empty message not allowed").build()
    }
  }

  def routedMessage(to:String, message: String): String = s"Message to: $to.\n$message"


  def getRoute(to:String): Option[JsonObject] = {
    val response =resmiProvider(GET).resource("sosms:Route", new ResourceId(to), RequestParameters.emptyParameters(), Optional.empty())
    response.getStatus match {
      case 404 => None
      case 200 => Some(response.getEntity.asInstanceOf[JsonObject])
      case status: Int => throw new IllegalStateException(s"Failed to get route from RESMI. Status=$status, Msg=${response.getEntity}")
    }
  }

  def sendEmailMessage(to: String, emailAddress:String, message: String): Unit = {
    val email = new MandrillMessage()
    email.setSubject(s"SOSMS for $to")
    email.setText(message)
    email.setFromName("SOSMS")
    email.setFromEmail("sosms@devialab.com")

    val recipient = new Recipient()
    recipient.setEmail(emailAddress)
    email.setTo(List(recipient))

    emailClient.messages().send(email, true)
    debug(s"Email sent to $emailAddress")
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
