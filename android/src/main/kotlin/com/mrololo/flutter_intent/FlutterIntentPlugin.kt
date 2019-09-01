package com.mrololo.flutter_intent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class FlutterIntentPlugin : MethodCallHandler {
    private val TAG = FlutterIntentPlugin::class.java!!.getCanonicalName()
    private val mRegistrar: Registrar

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "com.mrololo/flutter_intent")
            channel.setMethodCallHandler(FlutterIntentPlugin(registrar))
        }
    }

    constructor(registrar: Registrar) {
        mRegistrar = registrar
    }

    private fun convertAction(action: String): String {
        when (action) {
            "action_view" -> return Intent.ACTION_VIEW
            "action_send" -> return Intent.ACTION_SEND
            else -> return action
        }
    }

    private fun getActiveContext(): Context {
        return mRegistrar.activity() ?: mRegistrar.context()
    }

    private fun convertArguments(arguments: Map<String, *>): Bundle {
        val bundle = Bundle()
        for (key in arguments.keys) {
            val value = arguments[key]
            if (value is Int) {
                bundle.putInt(key, value as Int)
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
        val list = value as ArrayList<*>
        for (o in list) {
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
        val map = value as Map<*, *>
        for (key in map.keys) {
            if (!(key == null || key is String)) {
                return false
            }
        }
        return true
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (call.method == "launch") {
            val context = getActiveContext()
            val action: String = convertAction(call.argument("action")!!)
            val intent = Intent(action)
//            mRegistrar.activity()?.let {
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//            if (call.argument("flags") as Int? != null) {
//                intent.addFlags(call.argument("flags")!!)
//            }

            if (call.argument("category") as String? != null) {
                intent.addCategory(call.argument("category")!!)
            }

            if (call.argument("data") as String? != null) {
                intent.setData(Uri.parse(call.argument("data")!!))
            }


            if (call.argument("arguments") as Map<*, *>? != null) {
                intent.putExtras(convertArguments(call.argument("arguments")!!))
            }

            if (call.argument("type") as String? != null) {
                intent.setType(call.argument("type")!!)
            }



            if (call.argument("package") as String? != null) {
                val packageName: String = call.argument("package")!!
                intent.setPackage(packageName)
                if (call.argument("componentName") as String? != null) {
                    Log.i(TAG, "COMPONENT?")
                    intent.setComponent(ComponentName(packageName, call.argument("componentName")!!))
                }
                if (intent.resolveActivity(context.getPackageManager()) == null) {
                    Log.i(TAG, "Cannot resolve explicit intent - ignoring package")
                    Log.i(TAG, "Package name - $packageName")
                    try {
                        //context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                    }
                    result.error("Package not found", null, null)
                }
            }

            Log.i(TAG, "startActivity")
            Log.i(TAG, "Sending intent $intent")
            context.startActivity(intent)
            result.success("gg")
        } else {
            result.notImplemented()
        }
    }
}
