// @SOURCE:/Users/user/Desktop/new/subsequence/conf/routes
// @HASH:85da9921c53b2338c5eb10ad978b95f5dedb1fae
// @DATE:Wed May 20 14:47:44 CDT 2015

import Routes.{prefix => _prefix, defaultPrefix => _defaultPrefix}
import play.core._
import play.core.Router._
import play.core.Router.HandlerInvokerFactory._
import play.core.j._

import play.api.mvc._
import _root_.controllers.Assets.Asset
import _root_.play.libs.F

import Router.queryString


// @LINE:20
// @LINE:19
// @LINE:16
// @LINE:13
// @LINE:12
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers {

// @LINE:20
// @LINE:19
class ReverseCallbacks {


// @LINE:20
def blockNotify(payload:String): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "callback/blocknotify/" + implicitly[PathBindable[String]].unbind("payload", dynamicString(payload)))
}
                        

// @LINE:19
def txNotify(payload:String): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "callback/txnotify/" + implicitly[PathBindable[String]].unbind("payload", dynamicString(payload)))
}
                        

}
                          

// @LINE:16
// @LINE:13
// @LINE:12
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
class ReverseInterface {


// @LINE:12
def getClusters(): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "clusters/list")
}
                        

// @LINE:13
def getClusterStatus(id:Integer): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "clusters/" + implicitly[PathBindable[Integer]].unbind("id", id) + "/status")
}
                        

// @LINE:6
def getUser(name:String): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "account/" + implicitly[PathBindable[String]].unbind("name", dynamicString(name)))
}
                        

// @LINE:16
def sweepFunds(id:Integer, target:String): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "clusters/" + implicitly[PathBindable[Integer]].unbind("id", id) + "/sweep/" + implicitly[PathBindable[String]].unbind("target", dynamicString(target)))
}
                        

// @LINE:7
def getTransactionsForUser(name:String, page:Integer): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "account/" + implicitly[PathBindable[String]].unbind("name", dynamicString(name)) + "/transactions/" + implicitly[PathBindable[Integer]].unbind("page", page))
}
                        

// @LINE:9
def getNewAddressForUser(name:String): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "account/" + implicitly[PathBindable[String]].unbind("name", dynamicString(name)) + "/addresses/new")
}
                        

// @LINE:8
def getAddressesForUser(name:String): Call = {
   import ReverseRouteContext.empty
   Call("GET", _prefix + { _defaultPrefix } + "account/" + implicitly[PathBindable[String]].unbind("name", dynamicString(name)) + "/addresses")
}
                        

}
                          
}
                  


// @LINE:20
// @LINE:19
// @LINE:16
// @LINE:13
// @LINE:12
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.javascript {
import ReverseRouteContext.empty

// @LINE:20
// @LINE:19
class ReverseCallbacks {


// @LINE:20
def blockNotify : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Callbacks.blockNotify",
   """
      function(payload) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "callback/blocknotify/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("payload", encodeURIComponent(payload))})
      }
   """
)
                        

// @LINE:19
def txNotify : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Callbacks.txNotify",
   """
      function(payload) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "callback/txnotify/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("payload", encodeURIComponent(payload))})
      }
   """
)
                        

}
              

// @LINE:16
// @LINE:13
// @LINE:12
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
class ReverseInterface {


// @LINE:12
def getClusters : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Interface.getClusters",
   """
      function() {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "clusters/list"})
      }
   """
)
                        

// @LINE:13
def getClusterStatus : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Interface.getClusterStatus",
   """
      function(id) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "clusters/" + (""" + implicitly[PathBindable[Integer]].javascriptUnbind + """)("id", id) + "/status"})
      }
   """
)
                        

// @LINE:6
def getUser : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Interface.getUser",
   """
      function(name) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "account/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("name", encodeURIComponent(name))})
      }
   """
)
                        

// @LINE:16
def sweepFunds : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Interface.sweepFunds",
   """
      function(id,target) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "clusters/" + (""" + implicitly[PathBindable[Integer]].javascriptUnbind + """)("id", id) + "/sweep/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("target", encodeURIComponent(target))})
      }
   """
)
                        

// @LINE:7
def getTransactionsForUser : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Interface.getTransactionsForUser",
   """
      function(name,page) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "account/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("name", encodeURIComponent(name)) + "/transactions/" + (""" + implicitly[PathBindable[Integer]].javascriptUnbind + """)("page", page)})
      }
   """
)
                        

// @LINE:9
def getNewAddressForUser : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Interface.getNewAddressForUser",
   """
      function(name) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "account/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("name", encodeURIComponent(name)) + "/addresses/new"})
      }
   """
)
                        

// @LINE:8
def getAddressesForUser : JavascriptReverseRoute = JavascriptReverseRoute(
   "controllers.Interface.getAddressesForUser",
   """
      function(name) {
      return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "account/" + (""" + implicitly[PathBindable[String]].javascriptUnbind + """)("name", encodeURIComponent(name)) + "/addresses"})
      }
   """
)
                        

}
              
}
        


// @LINE:20
// @LINE:19
// @LINE:16
// @LINE:13
// @LINE:12
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
package controllers.ref {


// @LINE:20
// @LINE:19
class ReverseCallbacks {


// @LINE:20
def blockNotify(payload:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Callbacks.blockNotify(payload), HandlerDef(this.getClass.getClassLoader, "", "controllers.Callbacks", "blockNotify", Seq(classOf[String]), "GET", """""", _prefix + """callback/blocknotify/$payload<[^/]+>""")
)
                      

// @LINE:19
def txNotify(payload:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Callbacks.txNotify(payload), HandlerDef(this.getClass.getClassLoader, "", "controllers.Callbacks", "txNotify", Seq(classOf[String]), "GET", """ Relevant TX notify and block notify""", _prefix + """callback/txnotify/$payload<[^/]+>""")
)
                      

}
                          

// @LINE:16
// @LINE:13
// @LINE:12
// @LINE:9
// @LINE:8
// @LINE:7
// @LINE:6
class ReverseInterface {


// @LINE:12
def getClusters(): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Interface.getClusters(), HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getClusters", Seq(), "GET", """ Get cluster information""", _prefix + """clusters/list""")
)
                      

// @LINE:13
def getClusterStatus(id:Integer): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Interface.getClusterStatus(id), HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getClusterStatus", Seq(classOf[Integer]), "GET", """""", _prefix + """clusters/$id<[^/]+>/status""")
)
                      

// @LINE:6
def getUser(name:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Interface.getUser(name), HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getUser", Seq(classOf[String]), "GET", """ Get user information (including balance) by id and name""", _prefix + """account/$name<[^/]+>""")
)
                      

// @LINE:16
def sweepFunds(id:Integer, target:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Interface.sweepFunds(id, target), HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "sweepFunds", Seq(classOf[Integer], classOf[String]), "GET", """ Sweep and send""", _prefix + """clusters/$id<[^/]+>/sweep/$target<[^/]+>""")
)
                      

// @LINE:7
def getTransactionsForUser(name:String, page:Integer): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Interface.getTransactionsForUser(name, page), HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getTransactionsForUser", Seq(classOf[String], classOf[Integer]), "GET", """""", _prefix + """account/$name<[^/]+>/transactions/$page<[^/]+>""")
)
                      

// @LINE:9
def getNewAddressForUser(name:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Interface.getNewAddressForUser(name), HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getNewAddressForUser", Seq(classOf[String]), "GET", """""", _prefix + """account/$name<[^/]+>/addresses/new""")
)
                      

// @LINE:8
def getAddressesForUser(name:String): play.api.mvc.HandlerRef[_] = new play.api.mvc.HandlerRef(
   controllers.Interface.getAddressesForUser(name), HandlerDef(this.getClass.getClassLoader, "", "controllers.Interface", "getAddressesForUser", Seq(classOf[String]), "GET", """""", _prefix + """account/$name<[^/]+>/addresses""")
)
                      

}
                          
}
        
    