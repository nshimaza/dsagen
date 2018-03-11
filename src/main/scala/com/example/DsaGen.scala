/**
 * @license
 * Copyright (c) 2016,2018 Cisco and/or its affiliates.
 *
 * This software is licensed to you under the terms of the Cisco Sample
 * Code License, Version 1.0 (the "License"). You may obtain a copy of the
 * License at
 *
 *                https://developer.cisco.com/docs/licenses
 *
 * All use of the material herein must be in accordance with the terms of
 * the License. All rights not expressly granted by the License are
 * reserved. Unless required by applicable law or agreed to separately in
 * writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.
 */

package com.example

import java.util.concurrent.{SynchronousQueue, TimeUnit}

import org.dsa.iot.dslink.node.value.{Value, ValueType}
import org.dsa.iot.dslink.util.Objects
import org.dsa.iot.dslink.{DSLink, DSLinkFactory, DSLinkHandler}
import org.slf4j.LoggerFactory

/**
  * Created by nshimaza on 2016/09/19.
  */
object DsaGen {

  private val log = LoggerFactory.getLogger(getClass)
  private val finishMarker = new SynchronousQueue[Unit]()

  def main(args: Array[String]): Unit = {
    log.info("Starting DsaGen")

    val provider = DSLinkFactory.generate(args.drop(3),
      DsaGenDSLinkHandler(
        numNode = args(0).toInt,
        msgPerSec = args(1).toInt,
        duration = args(2).toInt,
        markFinished = () => finishMarker.put(())
      )
    )

    provider.start()
    finishMarker.take()
    System.exit(0)
  }
}

case class DsaGenDSLinkHandler(numNode: Int,
                               msgPerSec: Int,
                               duration: Int,
                               markFinished: () => Unit
                              ) extends DSLinkHandler {
  private val log = LoggerFactory.getLogger(getClass)
  override val isResponder = true

  val initialDelay = 10000000 // micro seconds
  val interval = 1000000 / msgPerSec
  val agingCount = msgPerSec * 60
  val startCount = agingCount * 2
  val stopAt = msgPerSec * duration + startCount

  var count = 0
  var startTime = 0L

  override def onResponderInitialized(link: DSLink) = {
    log.info("DsaGen base initialized")
    val superRoot = link.getNodeManager.getSuperRoot

    val nodes = for (i <- 1 to numNode) yield {
      superRoot
        .createChild(s"c$i")
        .setDisplayName(s"Count$i")
        .setValueType(ValueType.NUMBER)
        .setValue(new Value(-2))
        .build
    }

    Objects.getDaemonThreadPool
      .scheduleAtFixedRate(new Runnable {
        def run() = {
          count match {
            case n if n < agingCount =>
              nodes.foreach(_.setValue(new Value(count)))
              count = count + 1
            case n if n < startCount =>
              count = count + 1
            case n if n == startCount =>
              startTime = System.currentTimeMillis()
              nodes.foreach(_.setValue(new Value(count)))
              count = count + 1
            case n if n < stopAt =>
              nodes.foreach(_.setValue(new Value(count)))
              count = count + 1
            case n if n == stopAt =>
              nodes.foreach(_.setValue(new Value(-1)))
              count = count + 1
              println(s"finished. elapsed time ${System.currentTimeMillis() - startTime} ms")
              markFinished()
            case _ =>
          }
        }
      }, initialDelay, interval, TimeUnit.MICROSECONDS)

    log.info("DsaGen Generator initialization completed")
  }

  override def onResponderConnected(link: DSLink) = {
    log.info("DsaGen connected")
  }
}
