package com.mrololo.flutter_intent

import android.R
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat.startActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


class FlutterIntentPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private val _tag = FlutterIntentPlugin::class.java.canonicalName
    private var channel: MethodChannel? = null

    private lateinit var context: Context
    private var activity: Activity? = null

    override fun onAttachedToEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        context = binding.applicationContext

        channel = MethodChannel(binding.binaryMessenger, "com.mrololo/flutter_intent")
        channel?.setMethodCallHandler(this)

    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel?.let {
            it.setMethodCallHandler(null)
            channel = null
        }

    }

    private fun convertAction(action: String): String {
        return when (action) {
            "action_view" -> Intent.ACTION_VIEW
            "action_send" -> Intent.ACTION_SEND
            else -> action
        }
    }

    private fun convertArguments(arguments: Map<String, *>): Bundle {
        val bundle = Bundle()
        for (key in arguments.keys) {
            val value = arguments[key]
            if (value is Int) {
                bundle.putInt(key, value)
            } else if (value is String) {
                bundle.putString(key, value)
            } else if (value is Boolean) {
                bundle.putBoolean(key, value)
            } else if (value is Double) {
                bundle.putDouble(key, value)
            } else if (value is Long) {
                bundle.putLong(key, value)
            } else if (value is ByteArray) {
                bundle.putByteArray(key, value)
            } else if (value is IntArray) {
                bundle.putIntArray(key, value)
            } else if (value is LongArray) {
                bundle.putLongArray(key, value)
            } else if (value is DoubleArray) {
                bundle.putDoubleArray(key, value)
            } else if (isTypedArrayList(value, Integer::class.java)) {
                bundle.putIntegerArrayList(key, value as ArrayList<Int>)
            } else if (isTypedArrayList(value, String::class.java)) {
                bundle.putStringArrayList(key, value as ArrayList<String>)
            } else if (isStringKeyedMap(value)) {
                bundle.putBundle(key, convertArguments(value as Map<String, *>))
            } else {
                throw UnsupportedOperationException("Unsupported type $value")
            }
        }
        return bundle
    }

    private fun isTypedArrayList(value: Any?, type: Class<*>): Boolean {
        if (value !is ArrayList<*>) {
            return false
        }
        for (o in value) {
            if (!(o == null || type.isInstance(o))) {
                return false
            }
        }
        return true
    }

    private fun isStringKeyedMap(value: Any?): Boolean {
        if (value !is Map<*, *>) {
            return false
        }
        for (key in value.keys) {
            if (!(key == null || key is String)) {
                return false
            }
        }
        return true
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "startActivity") {
            val action: String = convertAction(call.argument("action")!!)
            val intent = Intent(action)

            if (call.argument("category") as String? != null) {
                intent.addCategory(call.argument("category")!!)
            }

            if (call.argument("data") as String? != null) {
                intent.data = Uri.parse(call.argument("data")!!)
            }


            if (call.argument("arguments") as Map<*, *>? != null) {
                intent.putExtras(convertArguments(call.argument("arguments")!!))
            }

            if (call.argument("type") as String? != null) {
                intent.type = call.argument("type")!!
            }
            if (call.argument("package") as String? != null) {
                val packageName: String = call.argument("package")!!
                intent.setPackage(packageName)
                if (call.argument("componentName") as String? != null) {
                    Log.i(_tag, "COMPONENT?")
                    intent.component = ComponentName(packageName, call.argument("componentName")!!)
                }
                if (activity != null) {
                    Log.i(_tag, "startActivity")
                    Log.i(_tag, "Sending intent $intent")

                    try {
                        activity!!.startActivity(intent)
                        result.success(null)
                    } catch (e: ActivityNotFoundException) {
                        Log.i(_tag, "Cannot resolve explicit intent - ignoring package")
                        Log.i(_tag, "Package name - $packageName")

                        try {
                            //context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                            activity!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                            result.success(null)
                        } catch (anfe: android.content.ActivityNotFoundException) {
                            activity!!.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                            result.success(null)


                        }
                    }
                } else {
                    result.error("Activity is null", null, null)
                }

                return

            }

            if (activity != null) {
                Log.i(_tag, "startActivity")
                Log.i(_tag, "Sending intent $intent")
                activity!!.startActivity(intent)
                result.success(null)
            } else {
                result.error("Activity is null", null, null)
            }


        } else if (call.method == "startShareActivity") {
            val action: String = convertAction(call.argument("action")!!)
            val intent = Intent(action)
            val name: String = call.argument("name")!!
            if (call.argument("category") as String? != null) {
                intent.addCategory(call.argument("category")!!)
            }

            if (call.argument("data") as String? != null) {
                intent.data = Uri.parse(call.argument("data")!!)
            }

            if (call.argument("arguments") as Map<*, *>? != null) {
                intent.putExtras(convertArguments(call.argument("arguments")!!))
            }

            if (call.argument("type") as String? != null) {
                intent.type = call.argument("type")!!
            }

            Log.i(_tag, "startShareActivity")
            Log.i(_tag, "Sending intent $intent")
            if (activity != null) {
                activity!!.startActivity(Intent.createChooser(intent, name))
                result.success(null)
            } else {
                result.error("Activity is null", null, null)
            }

        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }


    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

}
