package per.edward.wechatautomationutil.service

import android.accessibilityservice.AccessibilityService
import android.app.Activity
import android.app.Instrumentation
import android.os.Environment
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import per.edward.wechatautomationutil.ui.AutoAddFriendsActivity
import java.io.File
import java.util.ArrayList
import android.R.attr.y
import android.R.attr.x
import android.view.MotionEvent
import android.os.SystemClock
import per.edward.wechatautomationutil.utils.*


class AutoAddFriendsService : AccessibilityService() {
    private var i: Int = 0
    override fun onServiceConnected() {
        loadFile()
    }

    private fun loadFile() {
        var list = Environment.getExternalStorageDirectory().listFiles()
        var filePath: String? = null
        for (i in list) {//寻找指定文件
            Log.e("输出",i.path)
            if (i.name.contains(Constant.WX_NUMBER_FILE_NAME)) {
                filePath = i.path
                break
            }
        }

        if (TextUtils.isEmpty(filePath)) {
            Toast.makeText(baseContext,"没有找到文件",Toast.LENGTH_SHORT).show()
            return
        }

        var file = File(filePath)
        if (file.isFile) {
            var content = FileUtils.readTxtFile(file)
            listNumber = content.split("\n") as ArrayList<String>
        }
    }

    override fun onInterrupt() {
    }

    private val TEMP = 3500
    private var accessibilityNodeInfo: AccessibilityNodeInfo? = null
    private var sendFinish = false
    @Volatile
    private var isCancelClearText=false
    private var listNumber = ArrayList<String>()
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        accessibilityNodeInfo = rootInActiveWindow
        val eventType = event!!.eventType
        var classNameStr = event.className
        LogUtil.e(eventType.toString() + "             " + Integer.toHexString(eventType) + "         " + event.className)
        if (classNameStr == "com.tencent.mm.ui.LauncherUI") {
            i = 0
            sendFinish=false
        }else
//            if (classNameStr == "com.tencent.mm.plugin.subapp.ui.pluginapp.AddMoreFriendsUI") {
//            firstStep()
//        } else
                if (classNameStr == "com.tencent.mm.plugin.fts.ui.FTSAddFriendUI") {//2048             800         android.widget.LinearLayout
            clearText()
        } else if (classNameStr == "com.tencent.mm.plugin.profile.ui.ContactInfoUI") {//1             1         android.widget.RelativeLayout
            addContacts()
        } else if (classNameStr == "com.tencent.mm.plugin.profile.ui.SayHiWithSnsPermissionUI") {
            sayHiToIt()
        }
    }

    private val ADD_FRIENDS_COUNT:Int=15//15
    private val TIME:Long=2*3600//单位:秒2*3600

    private fun firstStep() {
        Handler().postDelayed({
            MockOperationUtils.execShellCmd("input tap 320 240")
        }, TEMP.toLong())
    }

    private fun clearText(){
        Thread({
            Handler(mainLooper).postDelayed({
                if (!isCancelClearText) {
                    isCancelClearText=false
                    if ((i + 1) % ADD_FRIENDS_COUNT == 0) {//每添加15个好友，暂停
                        Toast.makeText(baseContext,"开始休眠"+(TIME * 1000),Toast.LENGTH_SHORT).show()
                        Thread.sleep(TIME * 1000)
                        Toast.makeText(baseContext,"休眠结束",Toast.LENGTH_SHORT).show()
                    }
                    LogUtil.e("开始结束")

                        if (i < listNumber.size) {
                            sendFinish = false
                            MockOperationUtils.execShellCmd("input tap 1039 100")//点击删除按钮
                            MockOperationUtils.execShellCmd("input tap 1035 50")//获取输入框焦点
                            Thread.sleep(500)
                            var friends=listNumber[i++]
                            MockOperationUtils.execShellCmd("input text $friends")
                            Toast.makeText(baseContext,"添加"+friends+"为好友",Toast.LENGTH_SHORT).show()
                            searchFriends()
                        }else{
                            Toast.makeText(baseContext,"好友添加完毕",Toast.LENGTH_SHORT).show()
                        }
                }
            }, TEMP.toLong())
        }).start()
    }

    private fun searchFriends() {
        Handler().postDelayed({
            MockOperationUtils.execShellCmd("input tap 450 200")
        }, TEMP.toLong())
    }

    /**
     * 点击添加好友选项
     */
    private fun addContacts() {
        isCancelClearText=true
        Handler().postDelayed({
            isCancelClearText=false
            if (sendFinish) {
                MockOperationUtils.execShellCmd("input tap 40 100")//点击后退
            }else{
                //通过“送消息”三个字来判断是否好友，如果是则直接返回
                var  clearList = accessibilityNodeInfo?.findAccessibilityNodeInfosByText("发消息")
                if (clearList != null && clearList.size > 0) {
                    MockOperationUtils.execShellCmd("input tap 40 100")//点击后退
                }else{
//                    MockOperationUtils.execShellCmd("input tap 370 810")//点击添加好友
                    var  clearList = accessibilityNodeInfo?.findAccessibilityNodeInfosByText("添加到通讯录")
                    OperationUtils.performClickBtn(clearList)
                }
            }
        }, TEMP.toLong())
    }


    /**
     * 点击搜索好友
     */
    private fun sayHiToIt() {
        Handler().postDelayed({
            MockOperationUtils.execShellCmd("input tap 1020 100")
            sendFinish=true
        }, TEMP.toLong())
    }

//    companion object {
////        var clearList:List<AccessibilityNodeInfo>?=null
//        var pasteList:List<AccessibilityNodeInfo>?=null
//    }
//
//    private fun clearPasteFriendsNumber() {//com.tencent.mm.plugin.fts.ui.FTSAddFriendUI
//        if (!stepThree) {
//            Handler().postDelayed({
//                stepThree = true
//                var  clearList = accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hv")//点击清除微信号文本按钮
//                if (clearList != null && clearList.size != 0) {
//                    OperationUtils.performClickBtn(clearList[0])
//                }
//                pasteFriendsNumber()
//            }, TEMP.toLong())
//        }
//    }
//

//
//    /**
//     * 复制好友微信号
//     */
//    private fun pasteFriendsNumber() {
//        Handler().postDelayed({
//            if (i < listNumber.size) {
//                var pasteList = accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hz")
//                if (pasteList != null && pasteList.size != 0) {
//                    var temp = OperationUtils.pasteContent(this, pasteList[0], listNumber[i++])
//                    if (temp) {
//                        goSearchFriends()
//                    }
//                }
//            } else {
//                LogUtil.e("已添加" + listNumber.size + "个好友，好友添加完毕！")
//                Toast.makeText(baseContext, "已添加" + listNumber.size + "个好友，好友添加完毕！", Toast.LENGTH_SHORT).show()
//            }
//        }, TEMP.toLong())
//
//    }
//
//    /**
//     * 开始搜索好友
//     */
//    private fun goSearchFriends() {
//        Handler().postDelayed({
//            val ac = accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bcq")
//            if (ac != null && ac.size != 0) {
//                var ac1 = ac[0]
//                if (ac1 != null && ac1.childCount != 0) {
//                    OperationUtils.performClickBtn(ac1.getChild(0))
//                }
//            }
//        }, TEMP.toLong())
//    }
//
//    /**
//     * 添加好友到联系人列表
//     */
//    private fun addToContacts() {
//        if (sendFinish) {
//            goBackSearch()
//        } else {
//            Handler().postDelayed({
//                val ac = accessibilityNodeInfo?.findAccessibilityNodeInfosByText("添加到通讯录")
//                if (ac != null && ac.size != 0) {
//                    OperationUtils.performClickBtn(ac[0])
//                }
//            }, TEMP.toLong())
//        }
//    }
//
//    /**
//     * 发送完成
//     */
//    private fun sendFinish() {
//        Handler().postDelayed({
//            val ac = accessibilityNodeInfo?.findAccessibilityNodeInfosByText("发送")
//            if (ac != null && ac.size != 0) {
//                sendFinish = OperationUtils.performClickBtn(ac[1])
//            }
//        }, TEMP.toLong())
//    }
//
//    /**
//     * 返回搜索页面
//     */
//    private fun goBackSearch() {
//        stepThree = false
//        sendFinish = false
//        Handler().postDelayed({
//            val ac = accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hs")
//            if (ac != null && ac.size != 0) {
//                OperationUtils.performClickBtn(ac)
//            }
//        }, TEMP.toLong())
//
//    }


}








