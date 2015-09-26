package io.corbel.resources.rem.plugin

import com.microtripit.mandrillapp.lutung.MandrillApi
import com.twilio.sdk.TwilioRestClient
import io.corbel.lib.config.ConfigurationHelper
import io.corbel.resources.rem.RemRegistry
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.http.{HttpMethod, MediaType}
import org.springframework.stereotype.Component

import io.corbel.resources.rem.utils._
/**
 * @author Alexander De Leon <alex.deleon@devialab.com>
 */
@Component
class SosmsRemPlugin extends ScalaRemPlugin {


  override def init(): Unit = {
    ConfigurationHelper.setConfigurationNamespace(getArtifactName)
    context = new AnnotationConfigApplicationContext(classOf[SosmsIoC])
  }

  override def getArtifactName: String = "sosms-rem"

  override def register(registry: RemRegistry): Unit = {
    val smsClient = context.getBean(classOf[TwilioRestClient])
    val emailClient = context.getBean(classOf[MandrillApi])
    registry.registerRem(new MessageRem(smsClient, emailClient, resmi(".*")), "sosms:Message", MediaType.APPLICATION_JSON, HttpMethod.POST)
    registry.registerRem(new PhoneNumberRem(smsClient, resmi(".*")), "sosms:PhoneNumber", MediaType.APPLICATION_JSON, HttpMethod.POST, HttpMethod.PUT)
    registry.registerRem(new RouteRem(resmi(".*"), HttpMethod.PUT), "sosms:Route", MediaType.APPLICATION_JSON, HttpMethod.PUT)
    registry.registerRem(new RouteRem(resmi(".*"), HttpMethod.GET), "sosms:Route", MediaType.APPLICATION_JSON, HttpMethod.GET)
    registry.registerRem(new RouteRem(resmi(".*"), HttpMethod.DELETE), "sosms:Route", MediaType.APPLICATION_JSON, HttpMethod.DELETE)
  }


}
