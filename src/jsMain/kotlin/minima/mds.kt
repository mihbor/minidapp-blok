package minima

import kotlinx.browser.window
import org.w3c.dom.WindowOrWorkerGlobalScope
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import self
import kotlin.coroutines.suspendCoroutine
import kotlin.js.Date

typealias Callback = ((dynamic) -> Unit)?

/**
 * The MAIN Minima Callback function
 */
var MDS_MAIN_CALLBACK: Callback = null;

/**
 * Main MINIMA Object for all interaction
 */
object MDS {
  
  //RPC Host for Minima
  var mainhost = ""
  
  //The MiniDAPP UID
  var minidappuid: String? = null
  
  //Is logging RPC enabled
  var logging = false
  
  /**
   * Minima Startup - with the callback function used for all Minima messages
   */
  suspend fun init(minidappuid: String, host: String, port: Int, callback: Callback = null) {
    this.minidappuid = minidappuid
    log("Initialising MDS [$minidappuid]")
  
    mainhost 	= "https://$host:$port/"
    log("MDS MAINHOST : "+ mainhost)
    
    if(logging){
      log("Location : "+window.location)
      log("Host     : "+host)
      log("port     : "+port)
    }
    
    if(logging){
      log("MDS UID  : "+ minidappuid)
    }
    
    val mainport 	= port+1
    
    log("MDS FILEHOST  : https://$host:$port/")
    
    mainhost 	= "https://$host:$mainport/"
    log("MDS MAINHOST : "+ mainhost)
    
    //Store this for poll messages
    MDS_MAIN_CALLBACK = callback
    
    //Start the Long Poll listener
    PollListener()
    
    MDSPostMessage(js("""{ "event" : "inited" }"""))
  }
  
  /**
   * Log some data with a timestamp in a consistent manner to the console
   */
  fun log(output: String){
    console.log("Minima @ ${Date().toLocaleString()} : $output")
  }
  
  /**
   * Runs a function on the Minima Command Line - same format as MInima
   */
  fun cmd(command: String, callback: Callback = null){
    //Send via POST
    httpPostAsync("${mainhost}cmd?uid=$minidappuid", command, callback)
  }
  
  suspend fun cmd(miniFunc: String) = suspendCoroutine<dynamic> { cont ->
    cmd(miniFunc) { response ->
      cont.resumeWith(Result.success(response))
    }
  }
  
  /**
   * Runs a SQL command on this MiniDAPPs SQL Database
   */
  fun sql(command: String, callback: Callback = null){
    httpPostAsync("${mainhost}sql?uid=$minidappuid", command, callback)
  }
  
  suspend fun sql(miniFunc: String) = suspendCoroutine<dynamic> { cont ->
    sql(miniFunc) { response ->
      cont.resumeWith(Result.success(response))
    }
  }
  
  /**
   * Form GET / POST parameters..
   */
  object form{
    
    //Return the GET parameter by scraping the location..
    fun getParams(parameterName: String): String?{
      var result: String? = null
      val items = window.location.search.substring(1).split("&");
      for (item in items) {
        val tmp = item.split("=");
        //console.log("TMP:"+tmp);
        if (tmp[0] == parameterName) result = decodeURIComponent(tmp[1])
      }
      return result
    }
  }
}

/**
 * Post a message to the Minima Event Listeners
 */
fun MDSPostMessage(data: dynamic){
  MDS_MAIN_CALLBACK?.invoke(data)
}

var PollCounter = 0
var PollSeries  = ""

//@Serializable
//data class Msg(
//  val series: String,
//  val counter: Int,
//  val status: Boolean,
//  val message: dynamic? = null,
//  val response: Msg? = null
//)

fun PollListener(){
  
  //The POLL host
  val pollhost = "${MDS.mainhost}poll?uid=${MDS.minidappuid}"
  val polldata = "series=$PollSeries&counter=$PollCounter"
  
  httpPostAsyncPoll(pollhost, polldata) { msg: dynamic ->
    //Are we on the right Series
    if (PollSeries != msg.series) {
      
      //Reset to the right series.
      PollSeries = msg.series
      PollCounter = msg.counter
      
    } else {
      
      //Is there a message ?
      if (msg.status == true && msg.response?.message != null) {
        
        //Get the current counter
        PollCounter = msg.response.counter + 1
        
        MDSPostMessage(msg.response.message)
      }
    }
    
    //And around we go again
    PollListener()
  }
}

external val self: WindowOrWorkerGlobalScope

fun httpPostAsync(url: String, params: String, callback: Callback = null) {
  self.fetch(
    url,
    RequestInit("POST", body = encodeURIComponent(params))
  ).catch {
    MDS.log("httpPostAsync failed: ${it.message} ${it.cause}")
    null as Response?
  }.then { response ->
    if(response?.ok == true) {
      if (MDS.logging) {
        MDS.log("RPC:$url\nPARAMS:$params\nRESPONSE:${response.body}")
      }
      
      if (callback != null) {
        response.json().then(callback)
      }
    }
  }
}

fun httpPostAsyncPoll(url: String, params: String, callback: Callback = null) {
  self.fetch(
    url,
    RequestInit("POST", body = encodeURIComponent(params))
  ).catch {
    MDS.log("httpPostAsyncPoll failed: ${it.message} ${it.cause}")
    null as Response?
  }.then { response ->
    if (response?.ok == true) {
      if (MDS.logging) {
        MDS.log("RPC:$url\nPARAMS:$params\nRESPONSE:${response.body}")
      }
      if (callback != null) {
        response.json().then(callback)
      }
    } else {
      MDS.log("Error Polling - reconnect in 10s")
      self.setTimeout({PollListener()}, 10000)
    }
    Unit
  }
}
