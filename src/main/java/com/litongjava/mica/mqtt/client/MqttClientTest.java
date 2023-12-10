/*
 * Copyright (c) 2019-2029, Dreamlu 卢春梦 (596392912@qq.com & dreamlu.net).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.litongjava.mica.mqtt.client;

import net.dreamlu.iot.mqtt.codec.MqttPublishMessage;
import net.dreamlu.iot.mqtt.codec.MqttQoS;
import net.dreamlu.iot.mqtt.core.client.IMqttClientMessageListener;
import net.dreamlu.iot.mqtt.core.client.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 客户端测试
 *
 * @author L.cm
 */
public class MqttClientTest {
  private static final Logger logger = LoggerFactory.getLogger(MqttClientTest.class);

  public static void main(String[] args) {
    // 初始化 mqtt 客户端
    MqttClient client = MqttClient.create()
      .ip("192.168.3.9")
      .port(1883)
      .username("mica")
      .password("mica")
//			如果包体过大，建议将此参数设置和 maxBytesInMessage 一样大
//			.readBufferSize(1024 * 10)
//			最大包体长度,如果包体过大需要设置此参数
//			.maxBytesInMessage(1024 * 10)
//			.version(MqttVersion.MQTT_5)
//			连接监听
      .connectListener(new MqttClientConnectListener())
      .willMessage(builder -> {
        builder.topic("/test/offline")
          .messageText("down")
          .retain(false)
          .qos(MqttQoS.AT_MOST_ONCE);    // 遗嘱消息
      })
      // 同步连接，也可以使用 connect() 异步（可以避免 broker 没启动照成启动卡住），但是下面的订阅和发布可能还没连接成功。
      .connectSync();

    client.subQos0("/test/123", new IMqttClientMessageListener() {
      @Override
      public void onSubscribed(ChannelContext context, String topicFilter, MqttQoS mqttQoS) {
        // 订阅成功之后触发，可在此处做一些业务逻辑
        logger.info("topicFilter:{} MqttQoS:{} 订阅成功！！！", topicFilter, mqttQoS);
      }

      @Override
      public void onMessage(ChannelContext context, String topic, MqttPublishMessage message, byte[] payload) {
        logger.info(topic + '\t' + new String(payload, StandardCharsets.UTF_8));
      }
    });

    Timer timer = new Timer();
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        client.publish("/test/client", "mica最牛皮".getBytes(StandardCharsets.UTF_8));
      }
    }, 1000, 2000);
  }
}
