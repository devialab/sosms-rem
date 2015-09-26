package io.corbel.resources.rem.plugin

import com.microtripit.mandrillapp.lutung.MandrillApi
import com.twilio.sdk.TwilioRestClient
import io.corbel.lib.config.ConfigurationIoC
import org.springframework.beans.factory.annotation.{Value, Autowired}
import org.springframework.context.annotation.{Import, Configuration, Bean}
import org.springframework.core.env.Environment

/**
 * @author Alexander De Leon <alex.deleon@devialab.com>
 */
@Configuration
@Import(Array(classOf[ConfigurationIoC]))
class SosmsIoC {

  @Bean def twillioClient(@Value("${twillio.sid}") sid: String, @Value("${twillio.authToken}") authToken: String) = new TwilioRestClient(sid, authToken)

  @Bean def mandrillClient(@Value("${mandrill.appKey}") appKey: String) = new MandrillApi(appKey)
}
