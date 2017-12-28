#!/usr/bin/env groovy

@Grab('com.github.nao20010128nao:HttpServerJava:3962b51')
@GrabResolver(name='jitpack',root='https://jitpack.io')
import net.freeutils.httpserver.HTTPServer

import java.time.*
import java.lang.reflect.*

// We swap "HTTPServer.status" with a larger array.
({->
  def was=HTTPServer.statuses
  // use larger value if it reaches to 22 century.
  def yearLimit=2100
  def will=Array.newInstance(String,yearLimit)
  Arrays.fill(will, "Unknown Status")
  System.arraycopy(was,0,will,0,was.length)
  
  def unsafeField=sun.misc.Unsafe.getDeclaredField("theUnsafe")
  unsafeField.accessible=true
  def unsafe=unsafeField.get(null)
  assert unsafe

  def statusField=HTTPServer.getDeclaredField("statuses")
  unsafe.putObject(
    unsafe.staticFieldBase(statusField),
    unsafe.staticFieldOffset(statusField),
    will
  )
  assert HTTPServer.statuses.length==yearLimit
})()
// END

HTTPServer.statuses[2018]='Happy New Year'

def basedOn=2018 // change here for an another year


def zone=ZoneId.of("Asia/Tokyo")
def newYear=ZonedDateTime.of(basedOn, 1, 1, 0,  0, 0, 0, zone)

def clock
if(System.env.TEST){
  // In 2018
  clock=Clock.fixed(Instant.parse("$basedOn-01-01T00:00:00Z"), zone)
  // In 2017
  //clock=Clock.fixed(Instant.parse("${basedOn-1}-12-31T00:00:00Z"), zone)
}else{
  clock=Clock.system(zone)
}

def isNewYear={->
  def now=ZonedDateTime.now(clock)
  return now>=newYear
}

def server=new HTTPServer(8080)

server.getVirtualHost(null).with {
  addContext('/'){req,resp->
    println 'Requested'
    if(isNewYear()){
      resp.sendHeaders(2018)
      resp.body.write '''
Happy New Year!
'''.trim().bytes
    }else{
      resp.sendHeaders(200)
      resp.body.write '''
The New Year is not yet!
'''.trim().bytes
    }
    resp.body.write '''
Donate to author:
BTC:     14g1qirif54CdAuwhjRsGR2UyHXg3A8ETg
BitZeny: ZuGdQvycbE9HTfke3EPcSUQEH2joaYqXjj
'''.bytes
    resp.body.close()
    0
  }
}

server.start()

println 'Ready'
