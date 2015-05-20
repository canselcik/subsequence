// @SOURCE:/Users/user/Desktop/new/subsequence/conf/routes
// @HASH:85da9921c53b2338c5eb10ad978b95f5dedb1fae
// @DATE:Wed May 20 14:47:44 CDT 2015


import scala.language.reflectiveCalls
import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset
import _root_.play.libs.F

import Router.queryString

object Routes extends Router.Routes {

import ReverseRouteContext.empty

private var _prefix = "/"

def setPrefix(prefix: String): Unit = {
  _prefix = prefix
  List[(String,Routes)]().foreach {
    case (p, router) => router.setPrefix(prefix + (if(prefix.endsWith("/")) "" else "/") + p)
  }
}

def prefix = _prefix

lazy val defaultPrefix = { if(Routes.prefix.endsWith("/")) "" else "/" }


// @LINE:6
private[this] lazy val controllers_Interface_getUser0_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("account/"),DynamicPart("name", """[^/]+""",true))))
private[this] lazy val controllers_Interface_getUser0_invoker = createInvoker(
controllers.Interface.getUser(fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getUser", Seq(classOf[String]),"GET", """ Get user information (including balance) by id and name""", Routes.prefix + """account/$name<[^/]+>"""))
        

// @LINE:7
private[this] lazy val controllers_Interface_getTransactionsForUser1_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("account/"),DynamicPart("name", """[^/]+""",true),StaticPart("/transactions/"),DynamicPart("page", """[^/]+""",true))))
private[this] lazy val controllers_Interface_getTransactionsForUser1_invoker = createInvoker(
controllers.Interface.getTransactionsForUser(fakeValue[String], fakeValue[Integer]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getTransactionsForUser", Seq(classOf[String], classOf[Integer]),"GET", """""", Routes.prefix + """account/$name<[^/]+>/transactions/$page<[^/]+>"""))
        

// @LINE:8
private[this] lazy val controllers_Interface_getAddressesForUser2_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("account/"),DynamicPart("name", """[^/]+""",true),StaticPart("/addresses"))))
private[this] lazy val controllers_Interface_getAddressesForUser2_invoker = createInvoker(
controllers.Interface.getAddressesForUser(fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getAddressesForUser", Seq(classOf[String]),"GET", """""", Routes.prefix + """account/$name<[^/]+>/addresses"""))
        

// @LINE:9
private[this] lazy val controllers_Interface_getNewAddressForUser3_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("account/"),DynamicPart("name", """[^/]+""",true),StaticPart("/addresses/new"))))
private[this] lazy val controllers_Interface_getNewAddressForUser3_invoker = createInvoker(
controllers.Interface.getNewAddressForUser(fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getNewAddressForUser", Seq(classOf[String]),"GET", """""", Routes.prefix + """account/$name<[^/]+>/addresses/new"""))
        

// @LINE:12
private[this] lazy val controllers_Interface_getClusters4_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("clusters/list"))))
private[this] lazy val controllers_Interface_getClusters4_invoker = createInvoker(
controllers.Interface.getClusters(),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getClusters", Nil,"GET", """ Get cluster information""", Routes.prefix + """clusters/list"""))
        

// @LINE:13
private[this] lazy val controllers_Interface_getClusterStatus5_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("clusters/"),DynamicPart("id", """[^/]+""",true),StaticPart("/status"))))
private[this] lazy val controllers_Interface_getClusterStatus5_invoker = createInvoker(
controllers.Interface.getClusterStatus(fakeValue[Integer]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getClusterStatus", Seq(classOf[Integer]),"GET", """""", Routes.prefix + """clusters/$id<[^/]+>/status"""))
        

// @LINE:16
private[this] lazy val controllers_Interface_sweepFunds6_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("clusters/"),DynamicPart("id", """[^/]+""",true),StaticPart("/sweep/"),DynamicPart("target", """[^/]+""",true))))
private[this] lazy val controllers_Interface_sweepFunds6_invoker = createInvoker(
controllers.Interface.sweepFunds(fakeValue[Integer], fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "sweepFunds", Seq(classOf[Integer], classOf[String]),"GET", """ Sweep and send""", Routes.prefix + """clusters/$id<[^/]+>/sweep/$target<[^/]+>"""))
        

// @LINE:19
private[this] lazy val controllers_Callbacks_txNotify7_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("callback/txnotify/"),DynamicPart("payload", """[^/]+""",true))))
private[this] lazy val controllers_Callbacks_txNotify7_invoker = createInvoker(
controllers.Callbacks.txNotify(fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Callbacks", "txNotify", Seq(classOf[String]),"GET", """ Relevant TX notify and block notify""", Routes.prefix + """callback/txnotify/$payload<[^/]+>"""))
        

// @LINE:20
private[this] lazy val controllers_Callbacks_blockNotify8_route = Route("GET", PathPattern(List(StaticPart(Routes.prefix),StaticPart(Routes.defaultPrefix),StaticPart("callback/blocknotify/"),DynamicPart("payload", """[^/]+""",true))))
private[this] lazy val controllers_Callbacks_blockNotify8_invoker = createInvoker(
controllers.Callbacks.blockNotify(fakeValue[String]),
HandlerDef(this.getClass.getClassLoader, "", "controllers.Callbacks", "blockNotify", Seq(classOf[String]),"GET", """""", Routes.prefix + """callback/blocknotify/$payload<[^/]+>"""))
        
def documentation = List(("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """account/$name<[^/]+>""","""controllers.Interface.getUser(name:String)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """account/$name<[^/]+>/transactions/$page<[^/]+>""","""controllers.Interface.getTransactionsForUser(name:String, page:Integer)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """account/$name<[^/]+>/addresses""","""controllers.Interface.getAddressesForUser(name:String)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """account/$name<[^/]+>/addresses/new""","""controllers.Interface.getNewAddressForUser(name:String)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """clusters/list""","""controllers.Interface.getClusters()"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """clusters/$id<[^/]+>/status""","""controllers.Interface.getClusterStatus(id:Integer)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """clusters/$id<[^/]+>/sweep/$target<[^/]+>""","""controllers.Interface.sweepFunds(id:Integer, target:String)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """callback/txnotify/$payload<[^/]+>""","""controllers.Callbacks.txNotify(payload:String)"""),("""GET""", prefix + (if(prefix.endsWith("/")) "" else "/") + """callback/blocknotify/$payload<[^/]+>""","""controllers.Callbacks.blockNotify(payload:String)""")).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
  case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
  case l => s ++ l.asInstanceOf[List[(String,String,String)]]
}}
      

def routes:PartialFunction[RequestHeader,Handler] = {

// @LINE:6
case controllers_Interface_getUser0_route(params) => {
   call(params.fromPath[String]("name", None)) { (name) =>
        controllers_Interface_getUser0_invoker.call(controllers.Interface.getUser(name))
   }
}
        

// @LINE:7
case controllers_Interface_getTransactionsForUser1_route(params) => {
   call(params.fromPath[String]("name", None), params.fromPath[Integer]("page", None)) { (name, page) =>
        controllers_Interface_getTransactionsForUser1_invoker.call(controllers.Interface.getTransactionsForUser(name, page))
   }
}
        

// @LINE:8
case controllers_Interface_getAddressesForUser2_route(params) => {
   call(params.fromPath[String]("name", None)) { (name) =>
        controllers_Interface_getAddressesForUser2_invoker.call(controllers.Interface.getAddressesForUser(name))
   }
}
        

// @LINE:9
case controllers_Interface_getNewAddressForUser3_route(params) => {
   call(params.fromPath[String]("name", None)) { (name) =>
        controllers_Interface_getNewAddressForUser3_invoker.call(controllers.Interface.getNewAddressForUser(name))
   }
}
        

// @LINE:12
case controllers_Interface_getClusters4_route(params) => {
   call { 
        controllers_Interface_getClusters4_invoker.call(controllers.Interface.getClusters())
   }
}
        

// @LINE:13
case controllers_Interface_getClusterStatus5_route(params) => {
   call(params.fromPath[Integer]("id", None)) { (id) =>
        controllers_Interface_getClusterStatus5_invoker.call(controllers.Interface.getClusterStatus(id))
   }
}
        

// @LINE:16
case controllers_Interface_sweepFunds6_route(params) => {
   call(params.fromPath[Integer]("id", None), params.fromPath[String]("target", None)) { (id, target) =>
        controllers_Interface_sweepFunds6_invoker.call(controllers.Interface.sweepFunds(id, target))
   }
}
        

// @LINE:19
case controllers_Callbacks_txNotify7_route(params) => {
   call(params.fromPath[String]("payload", None)) { (payload) =>
        controllers_Callbacks_txNotify7_invoker.call(controllers.Callbacks.txNotify(payload))
   }
}
        

// @LINE:20
case controllers_Callbacks_blockNotify8_route(params) => {
   call(params.fromPath[String]("payload", None)) { (payload) =>
        controllers_Callbacks_blockNotify8_invoker.call(controllers.Callbacks.blockNotify(payload))
   }
}
        
}

}
     